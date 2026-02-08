package nahlib;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;

public class DB {
    private static Connection conn;

    // ====== KONFIGURASI MYSQL ======
    private static final String URL = "jdbc:mysql://localhost:3306/nahsaz_library?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASS = "";

    // ====== CONNECT ======
    public static Connection connect() {
        try {
            if (conn != null && !conn.isClosed()) return conn;
            conn = DriverManager.getConnection(URL, USER, PASS);
            initOperationalHours();
            return conn;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // ====== QUERY (SELECT) ======
    public static List<Map<String,String>> query(String sql, Object... params) throws Exception {
        ensure();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                List<Map<String,String>> list = new ArrayList<>();
                ResultSetMetaData md = rs.getMetaData();
                int cols = md.getColumnCount();

                while (rs.next()) {
                    Map<String,String> row = new LinkedHashMap<>();
                    for (int i = 1; i <= cols; i++) {
                        row.put(md.getColumnLabel(i), rs.getString(i));
                    }
                    list.add(row);
                }
                return list;
            }
        }
    }

    // ====== EXEC (INSERT / UPDATE / DELETE) ======
    public static long exec(String sql, Object... params) throws Exception {
        ensure();
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, params);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
            return 0;
        }
    }

    // ====== TRANSACTION ======
    public static void tx(RunnableWithException job) throws Exception {
        ensure();
        boolean auto = conn.getAutoCommit();
        conn.setAutoCommit(false);
        try {
            job.run();
            conn.commit();
        } catch (Exception e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(auto);
        }
    }

    // ====== AUDIT LOG ======
    public static void audit(Long actorId, String action, String entity, String entityId, String detail) {
        try {
            exec(
                "INSERT INTO audit_log(actor_id, action, entity, entity_id, detail) VALUES (?, ?, ?, ?, ?)",
                actorId, action, entity, entityId, detail
            );
        } catch (Exception ignored) {}
    }

    // ====== SETTINGS ======
    public static Map<String,String> settings() {
        try {
            Map<String,String> map = new HashMap<>();
            // PERUBAHAN: field name sesuai database
            for (var r : query("SELECT setting_key, setting_value FROM settings")) {
                map.put(r.get("setting_key"), r.get("setting_value"));
            }
            return map;
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    // ====== RULES ======
    public static Map<String,Integer> rules() {
        try {
            // PERUBAHAN: query sesuai struktur tabel rules
            var r = query("SELECT max_days, max_books, fine_per_day, max_borrow_per_user FROM rules WHERE rule_id = 1").get(0);
            Map<String,Integer> m = new HashMap<>();
            m.put("max_days", toInt(r.get("max_days"), 7));
            m.put("max_books", toInt(r.get("max_books"), 3));
            m.put("fine_per_day", toInt(r.get("fine_per_day"), 1000));
            m.put("max_borrow_per_user", toInt(r.get("max_borrow_per_user"), 5));
            return m;
        } catch (Exception e) {
            Map<String,Integer> d = new HashMap<>();
            d.put("max_days", 7);
            d.put("max_books", 3);
            d.put("fine_per_day", 1000);
            d.put("max_borrow_per_user", 5);
            return d;
        }
    }

    // ====== EXPORT CSV ======
    public static void exportTableToCSV(String table, File file) throws Exception {
        List<Map<String,String>> rows = query("SELECT * FROM " + table);
        if (rows.isEmpty()) return;

        try (Writer w = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            String[] headers = rows.get(0).keySet().toArray(new String[0]);
            w.write(String.join(",", headers) + "\n");

            for (var row : rows) {
                List<String> vals = new ArrayList<>();
                for (String h : headers) {
                    vals.add(csvEscape(row.get(h)));
                }
                w.write(String.join(",", vals) + "\n");
            }
        }
    }

    // ====== METODE BARU UNTUK PERPUSTAKAAN ======
    
    // Get buku yang tersedia
    public static List<Map<String,String>> getAvailableBooks() throws Exception {
        return query("SELECT b.*, c.name as category_name, r.code as rack_code " +
                     "FROM books b " +
                     "LEFT JOIN categories c ON b.category_id = c.category_id " +
                     "LEFT JOIN racks r ON b.rack_id = r.rack_id " +
                     "WHERE b.stok_tersedia > 0 " +
                     "ORDER BY b.judul");
    }
    
    // Get peminjaman aktif user
    public static List<Map<String,String>> getUserActiveLoans(int userId) throws Exception {
        return query("SELECT l.*, " +
                     "GROUP_CONCAT(b.judul SEPARATOR ', ') as buku, " +
                     "DATEDIFF(l.jatuh_tempo, CURDATE()) as hari_tersisa " +
                     "FROM loans l " +
                     "JOIN loan_items li ON l.loan_id = li.loan_id " +
                     "JOIN books b ON li.book_id = b.book_id " +
                     "WHERE l.user_id = ? AND l.status = 'AKTIF' " +
                     "GROUP BY l.loan_id", userId);
    }
    
    // Cek apakah buku bisa dipinjam
    public static boolean canBorrowBook(int userId, int bookId) throws Exception {
        var rules = rules();
        
        // Cek stok tersedia
        var stok = query("SELECT stok_tersedia FROM books WHERE book_id = ?", bookId);
        if (stok.isEmpty() || toInt(stok.get(0).get("stok_tersedia"), 0) <= 0) {
            return false;
        }
        
        // Cek jumlah peminjaman aktif
        var activeLoans = query("SELECT COUNT(DISTINCT l.loan_id) as total " +
                               "FROM loans l " +
                               "WHERE l.user_id = ? AND l.status = 'AKTIF'", userId);
        int currentLoans = toInt(activeLoans.get(0).get("total"), 0);
        if (currentLoans >= rules.get("max_borrow_per_user")) {
            return false;
        }
        
        // Cek apakah sudah meminjam buku yang sama
        var sameBook = query("SELECT COUNT(*) as total " +
                            "FROM loans l " +
                            "JOIN loan_items li ON l.loan_id = li.loan_id " +
                            "WHERE l.user_id = ? AND li.book_id = ? AND l.status = 'AKTIF'", 
                            userId, bookId);
        return toInt(sameBook.get(0).get("total"), 0) == 0;
    }
    
    // Proses peminjaman
    public static long borrowBook(int userId, int petugasId, int bookId, String note) throws Exception {
        var rules = rules();
        
        return exec("INSERT INTO loans (user_id, petugas_id, tanggal_pinjam, jatuh_tempo, status, note) " +
                   "VALUES (?, ?, CURDATE(), DATE_ADD(CURDATE(), INTERVAL ? DAY), 'AKTIF', ?)",
                   userId, petugasId, rules.get("max_days"), note);
    }
    
    // ====== INTERNAL HELPERS ======
    private static void bind(PreparedStatement ps, Object... params) throws Exception {
        for (int i = 0; i < params.length; i++) {
            ps.setObject(i + 1, params[i]);
        }
    }

    private static void ensure() throws Exception {
        if (conn == null || conn.isClosed()) {
            if (connect() == null) {
                throw new SQLException("Database belum terhubung");
            }
        }
    }

    private static int toInt(String v, int def) {
        if (v == null || v.trim().isEmpty()) return def;
        try { 
            return Integer.parseInt(v.trim()); 
        } catch (Exception e) { 
            return def; 
        }
    }

    private static String csvEscape(String s) {
        if (s == null) return "";
        boolean need = s.contains(",") || s.contains("\"") || s.contains("\n");
        String v = s.replace("\"", "\"\"");
        return need ? "\"" + v + "\"" : v;
    }

    // ====== FUNCTIONAL INTERFACE ======
    public interface RunnableWithException {
        void run() throws Exception;
    }

    private static void initOperationalHours() {
        try {
            // Create table if not exists
            exec("CREATE TABLE IF NOT EXISTS operational_hours (" +
                 "day_index INT PRIMARY KEY, " +
                 "day_name VARCHAR(10), " +
                 "is_open TINYINT DEFAULT 1, " +
                 "open_time TIME DEFAULT '08:00:00', " +
                 "close_time TIME DEFAULT '17:00:00')");
            
            // Populate if empty
            var count = query("SELECT COUNT(*) as total FROM operational_hours").get(0).get("total");
            if (toInt(count, 0) == 0) {
                String[] days = {"Minggu", "Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu"};
                for (int i = 0; i < days.length; i++) {
                    exec("INSERT INTO operational_hours (day_index, day_name, is_open, open_time, close_time) " +
                         "VALUES (?, ?, ?, ?, ?)", i, days[i], (i == 0 ? 0 : 1), "08:00:00", "17:00:00");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}