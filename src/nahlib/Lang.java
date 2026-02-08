package nahlib;

import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;

public class Lang {
    // ID = 0, EN = 1
    private static int currentLang = 0; 
    private static final Map<String, String[]> dictionary = new HashMap<>();
    private static final Preferences prefs = Preferences.userNodeForPackage(Lang.class);

    static {
        // Load saved language preference (default 0/ID)
        currentLang = prefs.getInt("language", 0);
        loadDictionary();
    }

    public static void setLanguage(int langCode) {
        currentLang = langCode;
        prefs.putInt("language", langCode); // Save to system registry/prefs
    }

    public static int getLanguage() {
        return currentLang;
    }

    public static String get(String key) {
        if (dictionary.containsKey(key)) {
            String[] values = dictionary.get(key);
            if (currentLang < values.length) {
                return values[currentLang];
            }
        }
        return key; // Fallback to key if not found
    }

    private static void loadDictionary() {
        // Format: key -> { Indonesian, English }
        
        // === GENERAL ===
        add("app.loading", "Memuat Aplikasi...", "Loading Application...");
        add("app.ready", "Aplikasi Siap", "Application Ready");
        add("btn.save", "Simpan Perubahan", "Save Changes");
        add("btn.cancel", "Batal", "Cancel");
        add("btn.upload", "Upload Logo", "Upload Logo");
        add("btn.logout", "Keluar", "Logout");
        add("btn.refresh", "Refresh", "Refresh");
        add("btn.add", "Tambah", "Add");
        add("btn.edit", "Edit", "Edit");
        add("btn.delete", "Hapus", "Delete");
        add("btn.search", "Cari", "Search");
        add("btn.filter", "Filter", "Filter");
        add("btn.export", "Export Data", "Export Data");
        add("btn.backup", "Backup DB", "Backup DB");
        add("btn.submit", "Kirim", "Submit");
        add("btn.close", "Tutup", "Close");
        add("btn.print", "Cetak", "Print");
        add("btn.choose", "Pilih", "Choose");
        add("btn.reset", "Reset", "Reset");
        add("btn.view_detail", "Lihat Detail", "View Detail");
        
        add("msg.confirm_logout", "Apakah Anda yakin ingin logout?", "Are you sure you want to logout?");
        add("msg.confirm_delete", "Apakah Anda yakin ingin menghapus data ini?", "Are you sure you want to delete this data?");
        add("msg.confirm", "Apakah Anda yakin?", "Are you sure?");
        add("msg.warn_delete_borrowed", "PERHATIAN: Data yang sedang digunakan/dipinjam tidak dapat dihapus!", "WARNING: Data currently in use/borrowed cannot be deleted!");
        add("msg.success", "Sukses", "Success");
        add("msg.error", "Error", "Error");
        add("msg.warning", "Peringatan", "Warning");
        add("msg.info", "Pilih data terlebih dahulu", "Please select data first");
        add("msg.success_logo", "Logo berhasil diganti!", "Logo changed successfully!");
        add("msg.success_save", "Data berhasil disimpan!", "Data saved successfully!");
        add("msg.success_delete", "Data berhasil dihapus!", "Data deleted successfully!");
        add("msg.success_update", "Data berhasil diperbarui!", "Data updated successfully!");
        add("msg.loading", "Memuat data...", "Loading data...");
        add("msg.no_data", "Tidak ada data", "No data available");
        add("msg.select_member", "Silakan pilih anggota terlebih dahulu", "Please select a member first");
        add("msg.select_book", "Silakan pilih buku terlebih dahulu", "Please select a book first");
        add("msg.invalid_qty", "Jumlah tidak valid", "Invalid quantity");
        
        // === NAVIGATION ===
        add("nav.dashboard", "Dashboard", "Dashboard");
        add("nav.books", "Konfigurasi Buku", "Books Configuration");
        add("nav.members", "Data Member", "Members Data");
        add("nav.staff", "Petugas", "Staff");
        add("nav.loans", "Peminjaman", "Loans");
        add("nav.loan", "Peminjaman", "Loan");
        add("nav.returns", "Pengembalian", "Returns");
        add("nav.return", "Pengembalian", "Return");
        add("nav.reports", "Laporan", "Reports");
        add("nav.settings", "Settings", "Settings");
        add("nav.audit", "Activity Log", "Activity Log");
        add("nav.notifications", "Notifikasi", "Notifications");
        add("nav.browse", "Telusuri Buku", "Browse Books");
        add("nav.wishlist", "Wishlist", "Wishlist");
        add("nav.history", "Riwayat", "History");
        
        // === ROLES ===
        add("role.admin", "ADMIN", "ADMIN");
        add("role.staff", "PETUGAS", "STAFF");
        add("role.user", "ANGGOTA", "MEMBER");
        
        // === SETTINGS PANEL ===
        add("settings.title", "Pengaturan Sistem", "System Settings");
        add("settings.subtitle", "Konfigurasi identitas & aturan perpustakaan", "Configure library identity & rules");
        add("settings.card.identity", "Identitas Aplikasi", "Application Identity");
        add("settings.card.rules", "Aturan Peminjaman", "Borrowing Rules");
        add("settings.card.lang", "Bahasa & Tampilan", "Language & Appearance");
        add("settings.lang.label", "Pilih Bahasa", "Select Language");
        
        add("label.libname", "Nama Perpustakaan", "Library Name");
        add("label.hours", "Jam Operasional", "Operational Hours");
        add("label.maxdays", "Maks. Lama Pinjam (Hari)", "Max Loan Days");
        add("label.maxbooks", "Maks. Buku per Transaksi", "Max Books per Loan");
        add("label.maxborrow", "Maks. Pinjam per User", "Max Loans per User");
        add("label.fine", "Denda per Hari (Rp)", "Fine per Day");
        add("label.kelas", "Kelas", "Class");
        
        // === STAFF PANEL (Petugas) ===
        add("staff.title", "Kelola Petugas", "Manage Staff");
        add("staff.subtitle", "Tambah, edit, atau hapus akun petugas perpustakaan", "Add, edit, or delete library staff accounts");
        add("staff.add_title", "Tambah Petugas Baru", "Add New Staff");
        add("staff.edit_title", "Edit Data Petugas", "Edit Staff Data");
        add("staff.table.id", "ID Petugas", "Staff ID");
        add("staff.table.name", "Nama Lengkap", "Full Name");
        add("staff.table.username", "Username", "Username");
        add("staff.table.email", "Email", "Email");
        add("staff.table.phone", "No. Telepon", "Phone Number");
        add("staff.table.status", "Status", "Status");
        add("staff.table.actions", "Aksi", "Actions");
        add("staff.form.fullname", "Nama Lengkap", "Full Name");
        add("staff.form.username", "Username", "Username");
        add("staff.form.password", "Password", "Password");
        add("staff.form.email", "Email", "Email");
        add("staff.form.phone", "No. Telepon", "Phone Number");
        add("staff.form.address", "Alamat", "Address");
        add("staff.status.active", "Aktif", "Active");
        add("staff.status.inactive", "Nonaktif", "Inactive");
        
        // === MEMBER PANEL (Anggota) ===
        add("member.title", "Kelola Anggota", "Manage Members");
        add("member.subtitle", "Data anggota perpustakaan yang terdaftar", "Registered library members data");
        add("member.add_title", "Tambah Anggota Baru", "Add New Member");
        add("member.edit_title", "Edit Data Anggota", "Edit Member Data");
        add("member.table.id", "ID Anggota", "Member ID");
        add("member.table.name", "Nama Lengkap", "Full Name");
        add("member.table.username", "Username", "Username");
        add("member.table.email", "Email", "Email");
        add("member.table.phone", "No. Telepon", "Phone Number");
        add("member.table.joindate", "Tgl. Bergabung", "Join Date");
        add("member.table.status", "Status", "Status");
        add("member.form.fullname", "Nama Lengkap", "Full Name");
        add("member.form.username", "Username", "Username");
        add("member.form.password", "Password", "Password");
        add("member.form.email", "Email", "Email");
        add("member.form.phone", "No. Telepon", "Phone Number");
        add("member.form.address", "Alamat", "Address");
        
        // === BOOKS PANEL ===
        add("books.title", "Konfigurasi Buku", "Books Configuration");
        add("books.subtitle", "Kelola data buku perpustakaan", "Manage library books data");
        add("books.add_title", "Tambah Buku Baru", "Add New Book");
        add("books.edit_title", "Edit Data Buku", "Edit Book Data");
        add("books.table.id", "ID Buku", "Book ID");
        add("books.table.isbn", "ISBN", "ISBN");
        add("books.table.title", "Judul Buku", "Book Title");
        add("books.table.stock", "Stok", "Stock");
        add("books.table.available", "Tersedia", "Available");
        add("books.table.author", "Pengarang", "Author");
        add("books.table.publisher", "Penerbit", "Publisher");
        add("books.table.category", "Kategori", "Category");
        add("books.table.year", "Tahun", "Year");
        add("books.table.actions", "Aksi", "Actions");
        add("books.table.available", "Tersedia", "Available");
        add("books.form.title", "Judul Buku", "Book Title");
        add("books.form.author", "Pengarang", "Author");
        add("books.form.publisher", "Penerbit", "Publisher");
        add("books.form.year", "Tahun Terbit", "Publication Year");
        add("books.form.isbn", "ISBN", "ISBN");
        add("books.form.category", "Kategori", "Category");
        add("books.form.stock", "Jumlah Stok", "Stock Quantity");
        add("books.form.location", "Lokasi Rak", "Shelf Location");
        add("books.form.description", "Deskripsi", "Description");
        
        // === DASHBOARD ===
        add("dash.welcome", "Selamat Datang", "Welcome");
        add("dash.total_books", "Total Buku", "Total Books");
        add("dash.total_members", "Total Anggota", "Total Members");
        add("dash.active_loans", "Peminjaman Aktif", "Active Loans");
        add("dash.overdue", "Terlambat", "Overdue");
        add("dash.today_loans", "Peminjaman Hari Ini", "Today's Loans");
        add("dash.today_returns", "Pengembalian Hari Ini", "Today's Returns");
        add("dash.revenue", "Pendapatan Denda", "Fine Revenue");
        add("dash.recent_activity", "Aktivitas Terbaru", "Recent Activity");
        
        // === LOANS & RETURNS ===
        add("loan.title", "Peminjaman Buku", "Book Loans");
        add("loan.subtitle", "Kelola transaksi peminjaman buku", "Manage book loan transactions");
        add("loan.new", "Peminjaman Baru", "New Loan");
        add("loan.table.id", "ID Transaksi", "Transaction ID");
        add("loan.table.member", "Anggota", "Member");
        add("loan.table.book", "Buku", "Book");
        add("loan.table.loandate", "Tgl. Pinjam", "Loan Date");
        add("loan.table.duedate", "Tgl. Jatuh Tempo", "Due Date");
        add("loan.table.status", "Status", "Status");
        
        add("return.title", "Pengembalian Buku", "Book Returns");
        add("return.subtitle", "Proses pengembalian buku yang dipinjam", "Process borrowed book returns");
        add("return.table.returndate", "Tgl. Kembali", "Return Date");
        add("return.table.fine", "Denda", "Fine");
        add("return.status.ontime", "Tepat Waktu", "On Time");
        add("return.status.late", "Terlambat", "Late");
        
        // === REPORTS ===
        add("report.title", "Laporan", "Reports");
        add("report.subtitle", "Laporan dan statistik perpustakaan", "Library reports and statistics");
        add("report.period", "Periode", "Period");
        add("report.from", "Dari", "From");
        add("report.to", "Sampai", "To");
        add("report.generate", "Buat Laporan", "Generate Report");
        add("report.type.loans", "Laporan Peminjaman", "Loans Report");
        add("report.type.returns", "Laporan Pengembalian", "Returns Report");
        add("report.type.fines", "Laporan Denda", "Fines Report");
        add("report.type.members", "Laporan Anggota", "Members Report");
        add("report.type.books", "Laporan Buku", "Books Report");
        
        add("report.card.total", "Total Peminjaman", "Total Loans");
        add("report.card.active", "Peminjaman Aktif", "Active Loans");
        add("report.card.overdue", "Terlambat Kembali", "Overdue Returns");
        add("report.card.returned", "Buku Kembali", "Returned Books");
        add("report.card.fine", "Total Denda", "Total Fines");
        
        add("filter.all_status", "Semua Status", "All Status");
        add("filter.status", "Status", "Status");
        
        // === LOGIN ===
        add("login.title", "Masuk ke Sistem", "Login to System");
        add("login.username", "Username", "Username");
        add("login.password", "Password", "Password");
        add("login.btn", "Masuk", "Login");
        add("login.forgot", "Lupa Password?", "Forgot Password?");
        add("login.register", "Daftar Akun Baru", "Register New Account");
        
        // === COMMON TABLE HEADERS ===
        add("table.no", "No", "No");
        add("table.action", "Aksi", "Action");
        add("table.status", "Status", "Status");
        add("table.date", "Tanggal", "Date");
        add("table.total", "Total", "Total");
        add("table.days", "Sisa Hari", "Days Left");
        add("table.description", "Keterangan", "Description");
        
        // === STATUS ===
        add("status.active", "Aktif", "Active");
        add("status.inactive", "Nonaktif", "Inactive");
        add("status.pending", "Menunggu", "Pending");
        add("status.approved", "Disetujui", "Approved");
        add("status.rejected", "Ditolak", "Rejected");
        add("status.returned", "Dikembalikan", "Returned");
        add("status.borrowed", "Dipinjam", "Borrowed");
        add("status.finished", "Selesai", "Finished");
        add("status.cancelled", "Dibatalkan", "Cancelled");

        // === USER PAGE SPECIFIC ===
        add("user.dash.title", "Dashboard Anggota", "Member Dashboard");
        add("user.dash.subtitle", "Selamat datang, %s! Pantau aktivitas meminjam Anda.", "Welcome, %s! Track your borrowing activities.");
        add("user.stat.borrowed", "Sedang Dipinjam", "Borrowed");
        add("user.stat.duesoon", "Jatuh Tempo", "Due Soon");
        add("user.stat.overdue", "Terlambat", "Overdue");
        add("user.stat.wishlist", "Wishlist", "Wishlist");
        add("user.chart.loan", "Statistik Peminjaman Saya", "My Loan Statistics");
        add("user.chart.read", "Riwayat Membaca", "Reading History");
        add("user.quickaction.title", "AKSI CEPAT", "QUICK ACTIONS");
        add("user.quickaction.browse", "Telusuri Buku", "Browse Books");
        add("user.quickaction.wishlist", "Lihat Wishlist", "View Wishlist");
        add("user.quickaction.history", "Lihat Riwayat", "View History");
        add("user.quickaction.notification", "Cek Notifikasi", "Check Notifications");
        
        add("user.browse.title", "Telusuri Koleksi", "Browse Collection");
        add("user.browse.subtitle", "Jelajahi koleksi buku perpustakaan", "Explore library book collections");
        add("user.browse.search_label", "Cari Buku", "Search Books");
        add("user.browse.live_search", "Live search...", "Live search...");
        add("user.browse.placeholder", "Cari judul/kode/penulis/penerbit...", "Search title/code/author/publisher...");
        
        add("user.wishlist.title", "Wishlist / Favorit", "Wishlist / Favorites");
        add("user.wishlist.subtitle", "Daftar buku yang ingin Anda pinjam", "List of books you want to borrow");
        
        add("user.history.title", "Riwayat Peminjaman", "Loan History");
        add("user.history.subtitle", "Histori peminjaman buku Anda", "Your book loan history");
        add("user.history.detail", "Detail Buku yang Dipinjam", "Borrowed Book Details");
        add("user.history.btn_detail", "Lihat Detail Buku", "View Book Details");
        
        add("user.label.total_books", "Total: %d buku", "Total: %d books");
        add("user.label.available_books", "Tersedia: %d buku", "Available: %d books");
        add("user.label.total_stock", "Total Stok: %d", "Total Stock: %d");
        add("user.label.out_of_stock", "HABIS", "OUT OF STOCK");
        add("user.label.limited", "TERBATAS", "LIMITED");
        add("user.label.available", "TERSEDIA", "AVAILABLE");
        
        add("user.label.due_today", "Jatuh tempo hari ini", "Due today");
        add("user.label.due_tomorrow", "Jatuh tempo besok", "Due tomorrow");
        add("user.label.overdue_days", "Terlambat %d hari", "Overdue %d days");
        
        add("user.msg.already_wishlist", "Buku '%s' sudah ada di wishlist Anda.", "Book '%s' is already in your wishlist.");
        add("user.msg.confirm_wishlist", "Tambahkan buku '%s' ke wishlist Anda?", "Add book '%s' to your wishlist?");
        add("user.msg.success_wishlist", "Buku '%s' berhasil ditambahkan ke wishlist.", "Book '%s' successfully added to wishlist.");
        add("user.msg.confirm_remove_wishlist", "Hapus buku '%s' dari wishlist Anda?", "Remove book '%s' from your wishlist?");
        add("user.msg.success_remove_wishlist", "Buku '%s' berhasil dihapus dari wishlist.", "Book '%s' successfully removed from wishlist.");
        
        // === STAFF PAGE SPECIFIC ===
        add("petugas.dash.title", "Dashboard Petugas", "Staff Dashboard");
        add("petugas.dash.subtitle", "Ringkasan aktivitas dan performa kerja Anda", "Summary of your work activity and performance");
        add("petugas.stat.today_tx", "Transaksi Hari Ini", "Today's Transactions");
        add("petugas.stat.month_tx", "Transaksi Bulan Ini", "This Month's Transactions");
        add("petugas.stat.active_loan", "Peminjaman Aktif", "Active Loans");
        add("petugas.stat.global_overdue", "Terlambat (Global)", "Overdue (Global)");
        add("petugas.chart.daily_vol", "Volume Transaksi Harian", "Daily Transaction Volume");
        add("petugas.chart.trend", "Trend Peminjaman Anggota", "Member Loan Trend");
        add("petugas.quickaction.title", "AKSI CEPAT", "QUICK ACTIONS");
        add("petugas.quickaction.new_loan", "Buat Peminjaman", "Create Loan");
        add("petugas.quickaction.return", "Pengembalian", "Returns");
        add("petugas.quickaction.view_report", "Lihat Laporan", "View Reports");
        add("petugas.quickaction.check_overdue", "Cek Keterlambatan", "Check Overdue");
        
        add("petugas.loan.title", "Peminjaman Buku", "Book Loans");
        add("petugas.loan.subtitle", "Proses peminjaman buku untuk anggota (multi-buku)", "Process book loans for members (multi-books)");
        add("petugas.loan.select_member", "Pilih Anggota", "Select Member");
        add("petugas.loan.select_book", "Pilih Buku", "Select Book");
        add("petugas.loan.qty", "Jumlah (Qty)", "Quantity (Qty)");
        add("petugas.loan.cart_title", "Keranjang Peminjaman", "Loan Cart");
        add("petugas.loan.cart_items", "Keranjang: %d item (%d buku)", "Cart: %d items (%d books)");
        add("petugas.loan.rules", "Rules: maks %d hari, maks %d buku, denda Rp %d/hari", "Rules: max %d days, max %d books, fine Rp %d/day");
        add("petugas.loan.confirm_msg", "Proses peminjaman untuk:\n\n%s\nApakah data sudah benar?", "Process loan for:\n\n%s\nIs the data correct?");
        
        add("petugas.return.title", "Pengembalian Buku", "Book Returns");
        add("petugas.return.subtitle", "Proses pengembalian dan perhitungan denda otomatis", "Process returns and automated fine calculation");
        add("petugas.return.select_loan", "Pilih transaksi untuk melihat detail & proses pengembalian.", "Select a transaction to view details & process return.");
        add("petugas.return.detail_title", "Detail Transaksi", "Transaction Details");
        add("petugas.return.book_list", "Daftar Buku", "Book List");
        add("petugas.return.fine_warning", "⚠️ PERHATIAN: Transaksi terlambat!", "⚠️ WARNING: Transaction is overdue!");
        add("petugas.return.confirm_msg", "Konfirmasi pengembalian:\n\nLoan ID: %s\nAnggota: %s\nKeterlambatan: %d hari\nTotal denda: Rp %d\n\nApakah Anda yakin?", "Confirm return:\n\nLoan ID: %s\nMember: %s\nDelay: %d days\nTotal fine: Rp %d\n\nAre you sure?");
        
        add("petugas.report.title", "Laporan Saya", "My Reports");
        add("petugas.report.subtitle", "Riwayat transaksi yang Anda proses", "History of transactions you processed");
        add("petugas.report.total_tx", "Total: %d transaksi", "Total: %d transactions");
        add("petugas.search.placeholder", "Cari ID atau Nama Anggota...", "Search ID or Member Name...");
        add("petugas.notif.title", "Notifikasi Keterlambatan", "Overdue Notifications");
        add("petugas.notif.subtitle", "Daftar peminjaman yang telah melewati jatuh tempo", "List of loans that have passed their due date");
        add("petugas.notif.critical", "Kritis", "Critical");
        add("petugas.notif.high", "Tinggi", "High");
        add("petugas.notif.medium", "Sedang", "Medium");
        add("petugas.notif.send_to", "Kirim notifikasi kepada %s?", "Send notification to %s?");
        add("petugas.notif.dev_note", "(Fitur notifikasi email/SMS dalam pengembangan)", "(Email/SMS notification feature in development)");
        add("msg.success_notif", "Notifikasi berhasil dikirim kepada %s", "Notification successfully sent to %s");
        add("btn.notify_member", "Notifikasi Anggota", "Notify Member");
        
        add("label.days_late", "Telat (hari)", "Days Late");
        add("msg.clear_cart_confirm", "Apakah Anda yakin ingin mengosongkan keranjang?", "Are you sure you want to clear the cart?");

        // === ADMIN PAGE SPECIFIC ===
        add("admin.dash.title", "Dashboard Admin", "Admin Dashboard");
        add("admin.dash.books", "Total Buku", "Total Books");
        add("admin.dash.members", "Total Anggota", "Total Members");
        add("admin.dash.active_loans", "Peminjaman Aktif", "Active Loans");
        add("admin.dash.overdue", "Terlambat", "Overdue");
        add("admin.dash.loan_trend", "Tren Peminjaman", "Loan Trend");
        add("admin.dash.user_trend", "Pertumbuhan Anggota", "User Growth");
        add("admin.dash.subtitle", "Ringkasan statistik perpustakaan dan tren data", "General library statistics and data trends");
        add("admin.members.title", "Kelola Anggota", "Manage Members");
        add("admin.members.subtitle", "Data anggota perpustakaan yang terdaftar", "Registered library members data");
        add("admin.books.title", "Kelola Buku", "Manage Books");
        add("admin.books.subtitle", "Kelola koleksi buku, kategori, dan rak", "Manage book collections, categories, and racks");
        add("admin.audit.title", "Log Aktivitas", "Activity Log");
        add("admin.audit.subtitle", "Riwayat aktivitas sistem", "System activity history");
        add("admin.laporan.title", "Laporan Peminjaman", "Loan Reports");
        add("admin.laporan.subtitle", "Lihat dan filter data peminjaman buku", "View and filter book loan data");
        
        add("table.actor", "Aktor", "Actor");
        add("table.entity", "Entitas", "Entity");
        add("table.detail", "Detail", "Detail");
        add("staff.btn.reset_password", "Reset Password", "Reset Password");
        add("staff.btn.activate", "Aktifkan", "Activate");
        add("staff.btn.deactivate", "Nonaktifkan", "Deactivate");
        add("books.btn.manage_cat", "Kelola Kategori", "Manage Categories");
        add("books.btn.manage_rack", "Kelola Rak", "Manage Racks");

        add("day.0", "Minggu", "Sunday");
        add("day.1", "Senin", "Monday");
        add("day.2", "Selasa", "Tuesday");
        add("day.3", "Rabu", "Wednesday");
        add("day.4", "Kamis", "Thursday");
        add("day.5", "Jumat", "Friday");
        add("day.6", "Sabtu", "Saturday");
        add("label.buka", "BUKA", "OPEN");
        add("label.tutup", "TUTUP", "CLOSED");
        add("btn.save_schedule", "Simpan Jadwal Operasional", "Save Operational Schedule");
        
        // === AUDIT / ACTIVITY LOG ===
        add("report.audit.title", "Log Aktivitas", "Activity Log");
        add("report.audit.subtitle", "Riwayat aktivitas dan audit sistem", "System activity and audit history");
        add("report.label.from", "Dari Tanggal", "From Date");
        add("report.label.to", "Sampai Tanggal", "To Date");
        add("btn.clear", "Bersihkan", "Clear");
        
        // === ADMIN SETTINGS ===
        add("admin.settings.title", "Pengaturan Sistem", "System Settings");
        add("admin.settings.subtitle", "Konfigurasi identitas & aturan perpustakaan", "Configure library identity & rules");
        add("settings.lib_name", "Nama Perpustakaan", "Library Name");
        add("settings.max_days", "Maks. Lama Pinjam (Hari)", "Max Loan Days");
        add("settings.max_books", "Maks. Buku per Transaksi", "Max Books per Loan");
        add("settings.fine", "Denda per Hari (Rp)", "Fine per Day");
        add("settings.max_borrow", "Maks. Pinjam per User", "Max Loans per User");
        add("settings.logo", "Logo Aplikasi", "Application Logo");
        add("settings.btn_change_logo", "Ganti Logo", "Change Logo");
        add("settings.logo.pick", "Pilih Logo Aplikasi", "Select Application Logo");
        add("settings.operational", "Jam Operasional", "Operational Hours");
        add("settings.btn_export", "Export Data (CSV)", "Export Data (CSV)");
        add("settings.btn_backup", "Backup Database (SQL)", "Backup Database (SQL)");
        
        add("day.7", "Minggu", "Sunday");
    }

    private static void add(String key, String id, String en) {
        dictionary.put(key, new String[]{id, en});
    }
}
