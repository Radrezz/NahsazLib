package nahlib.petugas;

import nahlib.Lang;
import nahlib.DB;
import nahlib.Utils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NotifikasiPanel extends JPanel {
    private final PetugasPage petugasPage;
    private DefaultTableModel model = new DefaultTableModel(new String[]{
        Lang.get("loan.table.id"), Lang.get("loan.table.member"), Lang.get("loan.table.duedate"), 
        Lang.get("label.days_late"), Lang.get("table.status")
    },0);
    private JTable table = new JTable(model);
    private JTextField search = Utils.input(Lang.get("petugas.search.placeholder"));
    private JLabel summaryLabel;

    public NotifikasiPanel(PetugasPage petugasPage) {
        this.petugasPage = petugasPage;
        setBackground(Utils.BG);
        setLayout(new BorderLayout());
        
        // Header
        JPanel header = createPanelHeader(Lang.get("petugas.notif.title"), 
            Lang.get("petugas.notif.subtitle"));
        
        // Table
        styleTable();
        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(Utils.CARD);
        scroll.setBorder(new EmptyBorder(0, 20, 10, 20));
        
        // Filter Bar
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterBar.setBackground(Utils.BG);
        filterBar.setBorder(new EmptyBorder(0, 20, 10, 20));
        
        JLabel lblSearch = new JLabel(Lang.get("btn.search") + ":");
        lblSearch.setForeground(Utils.TEXT);
        filterBar.add(lblSearch);
        search.setPreferredSize(new Dimension(300, 35));
        filterBar.add(search);
        
        search.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent e) { refresh(); }
        });
        
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.add(filterBar, BorderLayout.NORTH);
        centerPanel.add(scroll, BorderLayout.CENTER);
        
        // Action panel
        JPanel actionPanel = new JPanel(new BorderLayout());
        actionPanel.setBackground(Utils.BG);
        actionPanel.setBorder(new EmptyBorder(0, 20, 20, 20));
        
        summaryLabel = new JLabel();
        summaryLabel.setForeground(Utils.TEXT);
        summaryLabel.setFont(Utils.FONT);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttonPanel.setOpaque(false);
        
        JButton refreshBtn = petugasPage.createPrimaryButton(Lang.get("btn.refresh"));
        JButton notifyBtn = petugasPage.createSecondaryButton(Lang.get("btn.notify_member"));
        
        refreshBtn.addActionListener(e -> refresh());
        notifyBtn.addActionListener(e -> notifyAnggota());
        
        buttonPanel.add(refreshBtn);
        buttonPanel.add(notifyBtn);
        
        actionPanel.add(summaryLabel, BorderLayout.WEST);
        actionPanel.add(buttonPanel, BorderLayout.EAST);
        
        add(header, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(actionPanel, BorderLayout.SOUTH);
        
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
        
        JButton refreshBtn = new JButton("Refresh");
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
                
                if (column == 3 && value != null) {
                    try {
                        int daysLate = Integer.parseInt(value.toString());
                        if (daysLate >= 7) {
                            setBackground(new Color(255, 235, 238));
                            setForeground(new Color(183, 28, 28));
                        } else if (daysLate >= 3) {
                            setBackground(new Color(255, 243, 224));
                            setForeground(new Color(245, 124, 0));
                        } else if (daysLate > 0) {
                            setBackground(new Color(232, 245, 233));
                            setForeground(new Color(56, 142, 60));
                        }
                    } catch (NumberFormatException e) {
                        // Ignore
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

    int countOverdue() {
        try {
            String result = DB.query(
                "SELECT COUNT(*) as c FROM loans WHERE status = 'AKTIF' AND jatuh_tempo < CURDATE()"
            ).get(0).get("c");
            return Integer.parseInt(result);
        } catch (Exception e) {
            return 0;
        }
    }

    public void refresh() {
        try {
            model.setRowCount(0);
            String sQuery = search.getText().trim();
            String sql = "SELECT l.loan_id, u.nama_lengkap as anggota, l.jatuh_tempo, " +
                "GREATEST(DATEDIFF(CURDATE(), l.jatuh_tempo), 0) as telat, l.status " +
                "FROM loans l JOIN users u ON l.user_id = u.user_id " +
                "WHERE l.status = 'AKTIF' AND l.jatuh_tempo < CURDATE() ";
            
            List<Object> params = new ArrayList<>();
            if (!sQuery.isEmpty()) {
                sql += " AND (l.loan_id LIKE ? OR u.nama_lengkap LIKE ?)";
                params.add("%" + sQuery + "%");
                params.add("%" + sQuery + "%");
            }
            sql += " ORDER BY telat DESC";
            
            List<Map<String, String>> rows = DB.query(sql, params.toArray());

            int critical = 0, high = 0, medium = 0;
            
            for (Map<String, String> r: rows) {
                model.addRow(new Object[]{
                    r.get("loan_id"), r.get("anggota"), r.get("jatuh_tempo"), 
                    r.get("telat"), r.get("status")
                });
                
                int daysLate = Integer.parseInt(r.get("telat"));
                if (daysLate >= 7) critical++;
                else if (daysLate >= 3) high++;
                else medium++;
            }
            
            int total = rows.size();
            summaryLabel.setText("<html>" + Lang.get("table.total") + ": <b>" + total + "</b> | " + 
                "<span style='color:#EA4335'>" + Lang.get("petugas.notif.critical") + ": <b>" + critical + "</b></span> | " + 
                "<span style='color:#F57C00'>" + Lang.get("petugas.notif.high") + ": <b>" + high + "</b></span> | " + 
                "<span style='color:#388E3C'>" + Lang.get("petugas.notif.medium") + ": <b>" + medium + "</b></span></html>");
            
            if (total > 0) {
                petugasPage.updateNavBadge(4, total);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            petugasPage.showErrorDialog(Lang.get("msg.error"), Lang.get("msg.error") + ": " + e.getMessage());
        }
    }
    
    private void notifyAnggota() {
        int r = table.getSelectedRow();
        if (r < 0) { 
            petugasPage.showMessageDialog(Lang.get("msg.warning"), Lang.get("msg.info")); 
            return; 
        }
        
        String loanId = model.getValueAt(r, 0).toString();
        String anggota = model.getValueAt(r, 1).toString();
        int telat = Integer.parseInt(model.getValueAt(r, 3).toString());
        
        String message = String.format(Lang.get("petugas.notif.send_to"), anggota) + "\n" +
                       "Loan ID: " + loanId + "\n" +
                       Lang.get("label.days_late") + ": " + telat + " " + Lang.get("nav.history").toLowerCase() + "\n\n" +
                       Lang.get("petugas.notif.dev_note");
        
        if (petugasPage.confirmDialog(Lang.get("petugas.quickaction.check_overdue"), message)) {
            petugasPage.showMessageDialog(Lang.get("msg.info"), String.format(Lang.get("msg.success_notif"), anggota) + 
                "\n\n" + Lang.get("petugas.notif.dev_note"));
        }
    }
}
