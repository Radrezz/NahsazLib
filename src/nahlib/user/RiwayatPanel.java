package nahlib.user;

import nahlib.Lang;
import nahlib.DB;
import nahlib.Utils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class RiwayatPanel extends JPanel {
    private final UserPage userPage;
    private DefaultTableModel model = new DefaultTableModel(new String[]{
        Lang.get("loan.table.id"), Lang.get("loan.table.loandate"), Lang.get("loan.table.duedate"), Lang.get("table.status"), 
        Lang.get("table.total"), Lang.get("nav.staff"), Lang.get("return.table.returndate"), Lang.get("return.table.fine"), 
        Lang.get("table.description"), Lang.get("btn.view_detail")
    }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return column == 9; // Button is editable
        }
    };
    
    private JTable table = new JTable(model);
    private JButton btnRefresh;
    private JLabel summaryLabel;

    public RiwayatPanel(UserPage userPage) {
        this.userPage = userPage;
        setBackground(Utils.BG);
        setLayout(new BorderLayout());
        
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Utils.BG);
        header.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JLabel title = new JLabel(Lang.get("user.history.title"));
        title.setForeground(Utils.TEXT);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        
        JLabel subtitle = new JLabel(Lang.get("user.history.subtitle"));
        subtitle.setForeground(Utils.MUTED);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.add(title, BorderLayout.NORTH);
        titlePanel.add(subtitle, BorderLayout.SOUTH);
        
        btnRefresh = new JButton("Refresh");
        btnRefresh.setFont(Utils.FONT);
        btnRefresh.setBackground(Utils.CARD);
        btnRefresh.setForeground(Utils.TEXT);
        btnRefresh.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Utils.BORDER),
            new EmptyBorder(6, 12, 6, 12)
        ));
        btnRefresh.setFocusPainted(false);
        btnRefresh.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRefresh.addActionListener(e -> refresh());
        
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.setOpaque(false);
        rightPanel.add(btnRefresh);
        
        header.add(titlePanel, BorderLayout.WEST);
        header.add(rightPanel, BorderLayout.EAST);
        
        // Table
        styleTable();
        new nahlib.TableButton(Lang.get("btn.view_detail"), this::showLoanDetail).install(table, 9);

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(Utils.CARD);
        scroll.setBorder(new EmptyBorder(0, 20, 20, 20));
        
        // Summary panel
        JPanel summaryPanel = new JPanel(new BorderLayout());
        summaryPanel.setBackground(Utils.BG);
        summaryPanel.setBorder(new EmptyBorder(0, 20, 20, 20));
        
        summaryLabel = new JLabel();
        summaryLabel.setForeground(Utils.TEXT);
        summaryLabel.setFont(Utils.FONT);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttonPanel.setOpaque(false);
        
        btnRefresh.addActionListener(e -> refresh());
        
        buttonPanel.add(btnRefresh);
        
        summaryPanel.add(summaryLabel, BorderLayout.WEST);
        summaryPanel.add(buttonPanel, BorderLayout.EAST);
        
        add(header, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(summaryPanel, BorderLayout.SOUTH);
        
        refresh();
    }
    
    private void styleTable() {
        // Set column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(80);   // Loan ID
        table.getColumnModel().getColumn(1).setPreferredWidth(100);  // Tanggal Pinjam
        table.getColumnModel().getColumn(2).setPreferredWidth(100);  // Jatuh Tempo
        table.getColumnModel().getColumn(3).setPreferredWidth(80);   // Status
        table.getColumnModel().getColumn(4).setPreferredWidth(80);   // Total Item
        table.getColumnModel().getColumn(5).setPreferredWidth(150);  // Petugas
        table.getColumnModel().getColumn(6).setPreferredWidth(100);  // Tanggal Kembali
        table.getColumnModel().getColumn(7).setPreferredWidth(80);   // Denda
        table.getColumnModel().getColumn(8).setPreferredWidth(150);  // Keterangan
        
        table.setRowHeight(35);
        table.setBackground(Utils.CARD);
        table.setForeground(Utils.TEXT);
        table.setGridColor(Utils.BORDER);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        
        JTableHeader header = table.getTableHeader();
        header.setBackground(Utils.CARD2);
        header.setForeground(Utils.TEXT);
        header.setFont(Utils.FONT_B);
        header.setBorder(BorderFactory.createLineBorder(Utils.BORDER));
        header.setReorderingAllowed(false);
        
        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setBackground(isSelected ? Utils.ACCENT : Utils.CARD);
                c.setForeground(isSelected ? Color.WHITE : Utils.TEXT);
                
                // Status column styling (column 3)
                if (column == 3) {
                    if ("AKTIF".equals(value)) {
                        setForeground(new Color(66, 133, 244));
                        setFont(getFont().deriveFont(Font.BOLD));
                        setText(Lang.get("status.active"));
                    } else if ("SELESAI".equals(value)) {
                        setForeground(new Color(52, 168, 83));
                        setText(Lang.get("status.finished"));
                    } else if ("BATAL".equals(value)) {
                        setForeground(new Color(234, 67, 53));
                        setText(Lang.get("status.cancelled"));
                    }
                }
                
                // Denda column (column 7)
                if (column == 7 && value != null) {
                    try {
                        int denda = Integer.parseInt(value.toString());
                        if (denda > 0) {
                            setForeground(new Color(234, 67, 53));
                            setFont(getFont().deriveFont(Font.BOLD));
                        }
                    } catch (NumberFormatException e) {}
                }
                
                // Keterangan column (column 8)
                if (column == 8 && value != null) {
                    String keterangan = value.toString();
                    if (keterangan.contains("Terlambat")) {
                        setForeground(new Color(234, 67, 53));
                    } else if (keterangan.contains("Jatuh tempo")) {
                        setForeground(new Color(251, 188, 5));
                    }
                }
                
                setBorder(noFocusBorder);
                return c;
            }
        });
    }

    public void refresh() {
        try {
            model.setRowCount(0);
            long userId = Long.parseLong(userPage.getMe().get("user_id"));
            List<Map<String,String>> rows = DB.query(
                    "SELECT l.loan_id, l.tanggal_pinjam, l.jatuh_tempo, l.status, " +
                    "(SELECT SUM(li.qty) FROM loan_items li WHERE li.loan_id=l.loan_id) as total_item, " +
                    "u.nama_lengkap as petugas, r.tanggal_kembali, r.fine_total, " +
                    "CASE " +
                    " WHEN l.status='AKTIF' AND l.jatuh_tempo < CURDATE() THEN CONCAT('Terlambat ', DATEDIFF(CURDATE(), l.jatuh_tempo), ' hari') " +
                    " WHEN l.status='AKTIF' AND l.jatuh_tempo = CURDATE() THEN 'Jatuh tempo hari ini' " +
                    " WHEN l.status='AKTIF' AND l.jatuh_tempo = DATE_ADD(CURDATE(), INTERVAL 1 DAY) THEN 'Jatuh tempo besok' " +
                    " WHEN l.status='SELESAI' THEN 'Selesai' " +
                    " WHEN l.status='BATAL' THEN 'Dibatalkan' " +
                    " ELSE 'Aktif' END as keterangan " +
                    "FROM loans l " +
                    "LEFT JOIN users u ON l.petugas_id = u.user_id " +
                    "LEFT JOIN returns r ON l.loan_id = r.loan_id " +
                    "WHERE l.user_id=? ORDER BY l.loan_id DESC", 
                    userId
            );
            
            int total = 0;
            int aktif = 0;
            int selesai = 0;
            int batal = 0;
            int terlambat = 0;
            
            for (Map<String,String> r: rows) {
                String fineTotal = r.get("fine_total") != null ? r.get("fine_total") : "0";
                
                // Localize keterangan logic
                String statusStr = r.get("status");
                String ket = r.get("keterangan");
                if ("AKTIF".equals(statusStr)) {
                    if (ket.contains("Terlambat")) {
                        // Extract days if possible, or just use a generic localized string
                        ket = Lang.get("user.label.overdue_days").replace("%d", ket.replaceAll("[^0-9]", ""));
                    } else if (ket.contains("hari ini")) {
                        ket = Lang.get("user.label.due_today");
                    } else if (ket.contains("besok")) {
                        ket = Lang.get("user.label.due_tomorrow");
                    } else {
                        ket = Lang.get("status.active");
                    }
                } else if ("SELESAI".equals(statusStr)) {
                    ket = Lang.get("status.finished");
                } else if ("BATAL".equals(statusStr)) {
                    ket = Lang.get("status.cancelled");
                }

                model.addRow(new Object[]{ 
                    r.get("loan_id"), 
                    r.get("tanggal_pinjam"), 
                    r.get("jatuh_tempo"), 
                    statusStr, 
                    r.get("total_item"),
                    r.get("petugas"),
                    r.get("tanggal_kembali"),
                    fineTotal,
                    ket,
                    "" // Button placeholder
                });
                
                total++;
                String status = r.get("status");
                if ("AKTIF".equals(status)) {
                    aktif++;
                    if (r.get("keterangan").toString().contains("Terlambat")) {
                        terlambat++;
                    }
                } else if ("SELESAI".equals(status)) {
                    selesai++;
                } else if ("BATAL".equals(status)) {
                    batal++;
                }
            }
            
            btnRefresh.setText(total + " " + Lang.get("nav.history"));
            summaryLabel.setText(Lang.get("table.total") + ": " + total + " | " + Lang.get("status.active") + ": " + aktif + 
                " | " + Lang.get("status.finished") + ": " + selesai + " | " + Lang.get("status.cancelled") + ": " + batal + 
                (terlambat > 0 ? " | ⚠️" + Lang.get("user.stat.overdue") + ": " + terlambat : ""));
            
        } catch (Exception e) {
            e.printStackTrace();
            userPage.showErrorDialog("Error", "Gagal memuat riwayat: " + e.getMessage());
        }
    }
    
    private void showLoanDetail(int r) {
        if (r < 0) { 
            userPage.showMessageDialog(Lang.get("msg.warning"), Lang.get("msg.info")); 
            return; 
        }
        
        int modelRow = table.convertRowIndexToModel(r);
        String loanId = model.getValueAt(modelRow, 0).toString();
        
        try {
            // Get full current data from row
            Map<String, String> data = new java.util.HashMap<>();
            for (int i = 0; i < model.getColumnCount(); i++) {
                data.put(model.getColumnName(i), String.valueOf(model.getValueAt(modelRow, i)));
            }
            
            // Add details about items in the loan including covers
            List<Map<String,String>> items = DB.query(
                "SELECT b.judul, b.cover, li.qty FROM loan_items li JOIN books b ON li.book_id = b.book_id WHERE li.loan_id = ?", 
                Integer.parseInt(loanId)
            );
            
            StringBuilder itemDetail = new StringBuilder();
            StringBuilder allCovers = new StringBuilder();
            for (Map<String,String> item : items) {
                if (itemDetail.length() > 0) itemDetail.append("<br>");
                itemDetail.append("• ").append(item.get("judul")).append(" (").append(item.get("qty")).append(")");
                
                String cp = item.get("cover");
                if (cp != null && !cp.isEmpty()) {
                    if (allCovers.length() > 0) allCovers.append("|");
                    allCovers.append(cp);
                }
            }
            data.put("DAFTAR BUKU", itemDetail.toString());
            if (allCovers.length() > 0) data.put("cover", allCovers.toString());
            
            new nahlib.DetailPage(userPage, Lang.get("user.history.title"), data);
            
        } catch (Exception ex) {
            ex.printStackTrace();
            userPage.showErrorDialog("Error", "Gagal memuat detail: " + ex.getMessage());
        }
    }
}
