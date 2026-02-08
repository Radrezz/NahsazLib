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

public class NotifPanel extends JPanel {
    private final UserPage userPage;
    private DefaultTableModel model = new DefaultTableModel(new String[]{
        Lang.get("loan.table.id"), Lang.get("loan.table.loandate"), Lang.get("loan.table.duedate"), Lang.get("table.status"), 
        Lang.get("table.total"), Lang.get("table.days"), Lang.get("table.description"), Lang.get("return.table.fine")
    }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    
    private JTable table = new JTable(model);
    private JButton btnRefresh;
    private JLabel summaryLabel;

    public NotifPanel(UserPage userPage) {
        this.userPage = userPage;
        setBackground(Utils.BG);
        setLayout(new BorderLayout());
        
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Utils.BG);
        header.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JLabel title = new JLabel(Lang.get("nav.notifications"));
        title.setForeground(Utils.TEXT);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        
        JLabel subtitle = new JLabel(Lang.get("user.wishlist.subtitle")); // Reuse or add new if needed
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
        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(Utils.CARD);
        scroll.setBorder(new EmptyBorder(0, 20, 20, 20));
        
        // Action panel
        JPanel actionPanel = new JPanel(new BorderLayout());
        actionPanel.setBackground(Utils.BG);
        actionPanel.setBorder(new EmptyBorder(0, 20, 20, 20));
        
        summaryLabel = new JLabel();
        summaryLabel.setForeground(Utils.TEXT);
        summaryLabel.setFont(Utils.FONT);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttonPanel.setOpaque(false);
        
         JButton remindBtn = userPage.createPrimaryButton(Lang.get("user.quickaction.notification"));
        
        btnRefresh.addActionListener(e -> refresh());
        remindBtn.addActionListener(e -> createReminder());
        
        buttonPanel.add(btnRefresh);
        buttonPanel.add(remindBtn);
        
        actionPanel.add(summaryLabel, BorderLayout.WEST);
        actionPanel.add(buttonPanel, BorderLayout.EAST);
        
        add(header, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(actionPanel, BorderLayout.SOUTH);
        
        refresh();
    }
    
    private void styleTable() {
        // Set column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(80);   // Loan ID
        table.getColumnModel().getColumn(1).setPreferredWidth(100);  // Tanggal Pinjam
        table.getColumnModel().getColumn(2).setPreferredWidth(100);  // Jatuh Tempo
        table.getColumnModel().getColumn(3).setPreferredWidth(80);   // Status
        table.getColumnModel().getColumn(4).setPreferredWidth(80);   // Total Item
        table.getColumnModel().getColumn(5).setPreferredWidth(80);   // Sisa Hari
        table.getColumnModel().getColumn(6).setPreferredWidth(150);  // Keterangan
        table.getColumnModel().getColumn(7).setPreferredWidth(120);  // Estimasi Denda
        
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
                
                // Color coding based on days and denda
                if (column == 5) { // Sisa Hari column
                    if (value != null) {
                        try {
                            int days = Integer.parseInt(value.toString());
                            if (days < 0) {
                                setBackground(new Color(255, 235, 238));
                                setForeground(new Color(183, 28, 28));
                                setFont(getFont().deriveFont(Font.BOLD));
                            } else if (days == 0) {
                                setBackground(new Color(255, 243, 224));
                                setForeground(new Color(245, 124, 0));
                                setFont(getFont().deriveFont(Font.BOLD));
                            } else if (days <= 1) {
                                setBackground(new Color(232, 245, 233));
                                setForeground(new Color(56, 142, 60));
                            }
                        } catch (NumberFormatException e) {}
                    }
                }
                
                // Estimasi Denda column (column 7)
                if (column == 7 && value != null) {
                    if (!value.toString().equals("Rp 0")) {
                        setForeground(new Color(234, 67, 53));
                        setFont(getFont().deriveFont(Font.BOLD));
                    }
                }
                
                if (isSelected) {
                    setBackground(Utils.ACCENT);
                    setForeground(Color.WHITE);
                }
                
                setBorder(noFocusBorder);
                return c;
            }
        });
    }

    public int countDueSoonOrOverdue() {
        try {
            int due = Integer.parseInt(DB.query(
                    "SELECT COUNT(DISTINCT l.loan_id) as c FROM loans l " +
                    "WHERE l.user_id=? AND l.status='AKTIF' " +
                    "AND l.jatuh_tempo BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 1 DAY)",
                    userPage.id()
            ).get(0).get("c"));
            int over = Integer.parseInt(DB.query(
                    "SELECT COUNT(DISTINCT l.loan_id) as c FROM loans l " +
                    "WHERE l.user_id=? AND l.status='AKTIF' AND l.jatuh_tempo < CURDATE()",
                    userPage.id()
            ).get(0).get("c"));
            return due + over;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public void refresh() {
        try {
            model.setRowCount(0);
            List<Map<String,String>> rows = DB.query(
                    "SELECT l.loan_id, l.tanggal_pinjam, l.jatuh_tempo, l.status, " +
                    "(SELECT SUM(li.qty) FROM loan_items li WHERE li.loan_id=l.loan_id) as total_item, " +
                    "DATEDIFF(l.jatuh_tempo, CURDATE()) as sisa_hari, " +
                    "CASE " +
                    " WHEN l.jatuh_tempo < CURDATE() THEN CONCAT('Terlambat ', DATEDIFF(CURDATE(), l.jatuh_tempo), ' hari') " +
                    " WHEN l.jatuh_tempo = CURDATE() THEN 'Jatuh tempo hari ini' " +
                    " WHEN l.jatuh_tempo = DATE_ADD(CURDATE(), INTERVAL 1 DAY) THEN 'Jatuh tempo besok' " +
                    " ELSE CONCAT('Aktif (', DATEDIFF(l.jatuh_tempo, CURDATE()), ' hari lagi)') END as keterangan, " +
                    "CASE " +
                    " WHEN l.jatuh_tempo < CURDATE() THEN CONCAT('Rp ', " +
                    "  (SELECT fine_per_day FROM rules LIMIT 1) * ABS(DATEDIFF(CURDATE(), l.jatuh_tempo))) " +
                    " ELSE 'Rp 0' END as estimasi_denda " +
                    "FROM loans l " +
                    "WHERE l.user_id=? AND l.status='AKTIF' " +
                    "AND l.jatuh_tempo <= DATE_ADD(CURDATE(), INTERVAL 1 DAY) " +
                    "ORDER BY l.jatuh_tempo ASC", 
                    userPage.id()
            );
            
            int total = 0;
            int terlambat = 0;
            int dueToday = 0;
            int dueTomorrow = 0;
            int totalEstimasiDenda = 0;
            
            for (Map<String,String> r: rows) {
                String ket = r.get("keterangan");
                if (ket.contains("Terlambat")) {
                    ket = Lang.get("user.label.overdue_days").replace("%d", ket.replaceAll("[^0-9]", ""));
                } else if (ket.contains("hari ini")) {
                    ket = Lang.get("user.label.due_today");
                } else if (ket.contains("besok")) {
                    ket = Lang.get("user.label.due_tomorrow");
                } else {
                    // For "Aktif (n hari lagi)" - this could be localized further but for now simplified
                    ket = Lang.get("status.active");
                }

                model.addRow(new Object[]{ 
                    r.get("loan_id"), 
                    r.get("tanggal_pinjam"), 
                    r.get("jatuh_tempo"), 
                    r.get("status"), 
                    r.get("total_item"),
                    r.get("sisa_hari"),
                    ket,
                    r.get("estimasi_denda")
                });
                
                total++;
                String keterangan = r.get("keterangan").toString();
                if (keterangan.contains("Terlambat")) {
                    terlambat++;
                    // Extract denda from string like "Rp 1000"
                    String dendaStr = r.get("estimasi_denda").toString().replace("Rp ", "");
                    try {
                        totalEstimasiDenda += Integer.parseInt(dendaStr);
                    } catch (NumberFormatException e) {}
                } else if (keterangan.contains("hari ini")) {
                    dueToday++;
                } else if (keterangan.contains("besok")) {
                    dueTomorrow++;
                }
            }
            
            btnRefresh.setText(total + " " + Lang.get("nav.notifications"));
            summaryLabel.setText(Lang.get("table.total") + ": " + total + " | " + Lang.get("user.stat.overdue") + ": " + terlambat + 
                " | " + Lang.get("user.label.due_today") + ": " + dueToday + " | " + Lang.get("user.label.due_tomorrow") + ": " + dueTomorrow +
                (totalEstimasiDenda > 0 ? " | Total Estimasi Denda: Rp " + totalEstimasiDenda : ""));
            
            // Note: In UserPage, updateNavBadge logic might need to be adjusted if called from here without infinite loop or side effects.
            // But UserPage.refreshApp calls refresh on panels. If panel.refresh calls updateNavBadge, it's fine.
            if (total > 0) {
                userPage.updateNavBadge(4, total);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            userPage.showErrorDialog("Error", "Gagal memuat notifikasi: " + e.getMessage());
        }
    }
    
    private void createReminder() {
        int r = table.getSelectedRow();
        if (r < 0) { 
            userPage.showMessageDialog(Lang.get("msg.warning"), Lang.get("msg.info")); 
            return; 
        }
        
        String loanId = model.getValueAt(r, 0).toString();
        String jatuhTempo = model.getValueAt(r, 2).toString();
        String keterangan = model.getValueAt(r, 6).toString();
        String estimasiDenda = model.getValueAt(r, 7).toString();
        
        String message = "📌 **" + Lang.get("user.quickaction.notification").toUpperCase() + "**\n\n" +
                        "Loan ID: #" + loanId + "\n" +
                        "Jatuh Tempo: " + jatuhTempo + "\n" +
                        "Status: " + keterangan + "\n";
        
        if (!estimasiDenda.equals("Rp 0")) {
            message += "Estimasi Denda: " + estimasiDenda + "\n\n";
        } else {
            message += "\n";
        }
        
        message += "Segera kembalikan buku untuk menghindari denda!";
        
        JOptionPane.showMessageDialog(this, 
            "<html><div style='width: 300px;'>" + 
            message.replace("\n", "<br>") + 
            "</div></html>", 
            "Pengingat", 
            JOptionPane.WARNING_MESSAGE);
    }
}
