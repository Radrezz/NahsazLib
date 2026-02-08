package nahlib.petugas;

import nahlib.Lang;
import nahlib.DB;
import nahlib.Utils;
import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LaporanSayaPanel extends JPanel {
    private final PetugasPage petugasPage;
    private DefaultTableModel model = new DefaultTableModel(new String[]{
        Lang.get("loan.table.id"), Lang.get("table.date"), Lang.get("loan.table.duedate"), 
        Lang.get("loan.table.member"), Lang.get("table.status"), Lang.get("table.total")
    },0);
    private JTable table = new JTable(model);
    private JDateChooser from = Utils.dateChooser();
    private JDateChooser to = Utils.dateChooser();
    private JTextField search = Utils.input(Lang.get("petugas.search.placeholder"));

    public LaporanSayaPanel(PetugasPage petugasPage) {
        this.petugasPage = petugasPage;
        setBackground(Utils.BG);
        setLayout(new BorderLayout());
        
        // Header
        JPanel header = createPanelHeader(Lang.get("petugas.report.title"), 
            Lang.get("petugas.report.subtitle"));
        
        // Filter panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterPanel.setBackground(Utils.BG);
        filterPanel.setBorder(new EmptyBorder(0, 20, 10, 20));
        
        JLabel lblFrom = new JLabel(Lang.get("report.from") + ":");
        lblFrom.setForeground(Color.WHITE);
        lblFrom.setFont(Utils.FONT_B);
        filterPanel.add(lblFrom);
        filterPanel.add(from);
        
        JLabel lblTo = new JLabel(Lang.get("report.to") + ":");
        lblTo.setForeground(Color.WHITE);
        lblTo.setFont(Utils.FONT_B);
        filterPanel.add(lblTo);
        filterPanel.add(to);
        
        JLabel lblSearch = new JLabel("  " + Lang.get("btn.search") + ":");
        lblSearch.setForeground(Color.WHITE);
        lblSearch.setFont(Utils.FONT_B);
        filterPanel.add(lblSearch);
        search.setPreferredSize(new Dimension(200, 35));
        filterPanel.add(search);
        
        
        // Live Listeners for JDateChooser
        from.addPropertyChangeListener("date", evt -> refresh());
        to.addPropertyChangeListener("date", evt -> refresh());
        
        JButton clearBtn = petugasPage.createSecondaryButton(Lang.get("btn.reset"));
        
        clearBtn.addActionListener(e -> { 
            from.setDate(null); 
            to.setDate(null); 
            search.setText("");
            // refresh via listeners
        });
        
        filterPanel.add(clearBtn);
        
        // Real-time search
        search.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent e) { refresh(); }
        });
        
        // Table
        styleTable();
        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(Utils.CARD);
        scroll.setBorder(new EmptyBorder(0, 20, 20, 20));
        
        // Summary panel
        JPanel summaryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        summaryPanel.setBackground(Utils.BG);
        summaryPanel.setBorder(new EmptyBorder(0, 20, 20, 20));
        
        JLabel totalLabel = new JLabel(String.format(Lang.get("petugas.report.total_tx"), 0));
        totalLabel.setForeground(Utils.TEXT);
        totalLabel.setFont(Utils.FONT_B);
        
        JLabel activeLabel = new JLabel(Lang.get("status.active") + ": 0");
        activeLabel.setForeground(new Color(66, 133, 244));
        activeLabel.setFont(Utils.FONT_B);
        
        JLabel completedLabel = new JLabel(Lang.get("status.finished") + ": 0");
        completedLabel.setForeground(new Color(52, 168, 83));
        completedLabel.setFont(Utils.FONT_B);
        
        JLabel cancelledLabel = new JLabel(Lang.get("status.cancelled") + ": 0");
        cancelledLabel.setForeground(new Color(234, 67, 53));
        cancelledLabel.setFont(Utils.FONT_B);
        
        summaryPanel.add(totalLabel);
        summaryPanel.add(activeLabel);
        summaryPanel.add(completedLabel);
        summaryPanel.add(cancelledLabel);
        
        JButton detailBtn = petugasPage.createSecondaryButton(Lang.get("btn.view_detail"));
        detailBtn.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r < 0) { 
                petugasPage.showMessageDialog(Lang.get("msg.warning"), Lang.get("msg.info")); 
                return; 
            }
            int modelRow = table.convertRowIndexToModel(r);
            Map<String, String> data = new java.util.HashMap<>();
            for (int i = 0; i < model.getColumnCount(); i++) {
                data.put(model.getColumnName(i), String.valueOf(model.getValueAt(modelRow, i)));
            }
            
            try {
                String loanId = data.get(Lang.get("loan.table.id"));
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
            } catch (Exception ex) { ex.printStackTrace(); }

            new nahlib.DetailPage(petugasPage, Lang.get("petugas.report.title"), data);
        });
        summaryPanel.add(detailBtn);
        
        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        content.add(filterPanel, BorderLayout.NORTH);
        content.add(scroll, BorderLayout.CENTER);
        
        add(header, BorderLayout.NORTH);
        add(content, BorderLayout.CENTER);
        add(summaryPanel, BorderLayout.SOUTH);
        
        refresh();
    }
    
    private JPanel createPanelHeader(String title, String subtitle) {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Utils.BG);
        header.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(Utils.TEXT);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        
        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setForeground(Utils.MUTED);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.add(titleLabel, BorderLayout.NORTH);
        titlePanel.add(subtitleLabel, BorderLayout.SOUTH);
        
        JButton refreshBtn = new JButton(Lang.get("btn.refresh"));
        refreshBtn.setFont(Utils.FONT);
        refreshBtn.setBackground(Utils.CARD);
        refreshBtn.setForeground(Utils.TEXT);
        refreshBtn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Utils.BORDER),
            new EmptyBorder(6, 12, 6, 12)
        ));
        refreshBtn.setFocusPainted(false);
        refreshBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshBtn.addActionListener(e -> refresh());
        
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.setOpaque(false);
        rightPanel.add(refreshBtn);
        
        header.add(titlePanel, BorderLayout.WEST);
        header.add(rightPanel, BorderLayout.EAST);
        
        return header;
    }
    
    private void styleTable() {
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
                
                if (column == 4) {
                    if ("AKTIF".equals(value)) {
                        setForeground(new Color(66, 133, 244));
                    } else if ("SELESAI".equals(value)) {
                        setForeground(new Color(52, 168, 83));
                    } else if ("BATAL".equals(value)) {
                        setForeground(new Color(234, 67, 53));
                    }
                }
                
                if (column == 2 && "AKTIF".equals(table.getValueAt(row, 4))) {
                    try {
                        String dueDate = table.getValueAt(row, 2).toString();
                        java.sql.Date due = java.sql.Date.valueOf(dueDate);
                        java.sql.Date today = new java.sql.Date(System.currentTimeMillis());
                        if (due.before(today)) {
                            setForeground(new Color(234, 67, 53));
                        }
                    } catch (Exception e) {
                        // Ignore
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
            String f = from.getDate() != null ? new java.text.SimpleDateFormat("yyyy-MM-dd").format(from.getDate()) : "";
            String t = to.getDate() != null ? new java.text.SimpleDateFormat("yyyy-MM-dd").format(to.getDate()) : "";

            StringBuilder where = new StringBuilder("WHERE l.petugas_id = ?");
            List<Object> params = new ArrayList<>();
            params.add(Integer.parseInt(petugasPage.getMe().get("user_id")));

            if (!f.isEmpty() && !t.isEmpty()) {
                where.append(" AND l.tanggal_pinjam BETWEEN ? AND ?");
                params.add(f);
                params.add(t);
            } else if (!f.isEmpty()) {
                where.append(" AND l.tanggal_pinjam >= ?");
                params.add(f);
            } else if (!t.isEmpty()) {
                where.append(" AND l.tanggal_pinjam <= ?");
                params.add(t);
            }

            String sQuery = search.getText().trim();
            if (!sQuery.isEmpty()) {
                where.append(" AND (l.loan_id LIKE ? OR u.nama_lengkap LIKE ?)");
                params.add("%" + sQuery + "%");
                params.add("%" + sQuery + "%");
            }

            String sql = "SELECT l.loan_id, l.tanggal_pinjam, l.jatuh_tempo, u.nama_lengkap as anggota, l.status, " +
                       "(SELECT SUM(qty) FROM loan_items li WHERE li.loan_id = l.loan_id) as total_item " +
                       "FROM loans l JOIN users u ON l.user_id = u.user_id " +
                       where.toString() + " ORDER BY l.loan_id DESC";

            List<Map<String, String>> rows = DB.query(sql, params.toArray());

            int total = 0, aktif = 0, selesai = 0, batal = 0;
            
            for (Map<String, String> r: rows) {
                model.addRow(new Object[]{
                    r.get("loan_id"), r.get("tanggal_pinjam"), r.get("jatuh_tempo"),
                    r.get("anggota"), r.get("status"), r.get("total_item")
                });
                
                total++;
                String status = r.get("status");
                if ("AKTIF".equals(status)) aktif++;
                else if ("SELESAI".equals(status)) selesai++;
                else if ("BATAL".equals(status)) batal++;
            }
            
            // Update summary labels
            JPanel summaryPanel = (JPanel) getComponent(2);
            ((JLabel)summaryPanel.getComponent(0)).setText("<html>" + Lang.get("petugas.report.total_tx").replace("%d", "<b>"+total+"</b>") + "</html>");
            ((JLabel)summaryPanel.getComponent(1)).setText("<html>" + Lang.get("status.active") + ": <b style='color:#4285F4'>" + aktif + "</b></html>");
            ((JLabel)summaryPanel.getComponent(2)).setText("<html>" + Lang.get("status.finished") + ": <b style='color:#34A853'>" + selesai + "</b></html>");
            ((JLabel)summaryPanel.getComponent(3)).setText("<html>" + Lang.get("status.cancelled") + ": <b style='color:#EA4335'>" + batal + "</b></html>");
            
        } catch (Exception e) {
            e.printStackTrace();
            petugasPage.showErrorDialog(Lang.get("msg.error"), Lang.get("msg.error") + ": " + e.getMessage());
        }
    }
}
