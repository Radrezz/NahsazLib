package nahlib.admin;

import nahlib.Lang;
import nahlib.DB;
import nahlib.Utils;
import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class AuditPanel extends JPanel {
    private final AdminPage adminPage;
    private final DefaultTableModel model = new DefaultTableModel(new String[]{"Waktu","Aktor","Aksi","Entitas","ID Entitas","Detail", Lang.get("btn.view_detail")}, 0);
    private final JTable table = new JTable(model);
    private final JDateChooser dateFrom = Utils.dateChooser();
    private final JDateChooser dateTo = Utils.dateChooser();
    private JTextField searchField;
    private JLabel statsLabel;

    public AuditPanel(AdminPage adminPage) {
        this.adminPage = adminPage;
        setBackground(Utils.BG);
        setLayout(new BorderLayout());
        
        // Header
        JPanel header = createPanelHeader(Lang.get("report.audit.title"), 
            Lang.get("report.audit.subtitle"));
        
        // Filter panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterPanel.setBackground(Utils.BG);
        filterPanel.setBorder(new EmptyBorder(0, 20, 10, 20));
        
        JLabel labelDari = new JLabel(Lang.get("report.label.from") + ":");
        labelDari.setForeground(Utils.TEXT);
        filterPanel.add(labelDari);
        
        dateFrom.setPreferredSize(new Dimension(150, 35));
        filterPanel.add(dateFrom);
        
        JLabel labelSampai = new JLabel(Lang.get("report.label.to") + ":");
        labelSampai.setForeground(Utils.TEXT);
        filterPanel.add(labelSampai);
        
        dateTo.setPreferredSize(new Dimension(150, 35));
        filterPanel.add(dateTo);
        
        
        // Live Date Listeners for JDateChooser
        dateFrom.addPropertyChangeListener("date", evt -> refresh());
        dateTo.addPropertyChangeListener("date", evt -> refresh());
        
        JButton clearBtn = adminPage.createSecondaryButton(Lang.get("btn.clear"));
        JButton clearLogsBtn = adminPage.createSecondaryButton("Clear Logs"); 
        
        clearBtn.addActionListener(e -> { 
            dateFrom.setDate(null); 
            dateTo.setDate(null); 
            // refresh() called by listeners
        });
        
        clearLogsBtn.addActionListener(e -> clearOldLogs());
        clearLogsBtn.setForeground(new Color(234, 67, 53)); // Red text for danger action
        
        filterPanel.add(clearBtn);
        filterPanel.add(new JLabel("  |  "));
        filterPanel.add(clearLogsBtn);
        
        // Table
        styleTable();
        new nahlib.TableButton(Lang.get("btn.view_detail"), r -> {
            int modelRow = table.convertRowIndexToModel(r);
            java.util.Map<String, String> data = new java.util.HashMap<>();
            for (int i = 0; i < model.getColumnCount(); i++) {
                data.put(model.getColumnName(i), String.valueOf(model.getValueAt(modelRow, i)));
            }
            new nahlib.DetailPage(adminPage, Lang.get("report.audit.title"), data);
        }).install(table, 6);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new EmptyBorder(0, 20, 20, 20));
        scroll.getViewport().setBackground(Utils.CARD);
        
        add(header, BorderLayout.NORTH);
        add(filterPanel, BorderLayout.CENTER);
        add(scroll, BorderLayout.SOUTH);
        
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
        
        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        searchPanel.setOpaque(false);
        
        searchField = new JTextField(20);
        searchField.setFont(Utils.FONT);
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Utils.BORDER),
            new EmptyBorder(6, 10, 6, 10)
        ));
        searchField.putClientProperty("JTextField.placeholderText", Lang.get("petugas.search.placeholder"));
        
        // Live search listener
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { filterTable(); }
            @Override
            public void removeUpdate(DocumentEvent e) { filterTable(); }
            @Override
            public void changedUpdate(DocumentEvent e) { filterTable(); }
            
            private void filterTable() {
                String searchText = searchField.getText().toLowerCase();
                TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
                table.setRowSorter(sorter);
                if (searchText.trim().length() == 0) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + searchText));
                }
            }
        });
        
        JLabel searchIcon = new JLabel(Lang.get("btn.search"));
        searchIcon.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchIcon.setForeground(Utils.TEXT);
        
        searchPanel.add(searchIcon);
        searchPanel.add(searchField);
        
        // Statistics panel instead of refresh button
        statsLabel = new JLabel();
        statsLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        statsLabel.setForeground(Utils.TEXT);
        statsLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Utils.ACCENT, 2),
            new EmptyBorder(6, 12, 6, 12)
        ));
        statsLabel.setOpaque(true);
        statsLabel.setBackground(new Color(66, 133, 244, 20));
        
        searchPanel.add(statsLabel);
        
        header.add(titlePanel, BorderLayout.WEST);
        header.add(searchPanel, BorderLayout.EAST);
        
        return header;
    }
    
    private void styleTable() {
        table.setRowHeight(40);
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
                
                // Color code actions
                if (column == 2 && value != null) {
                    String action = value.toString();
                    switch (action) {
                        case "CREATE":
                            setForeground(new Color(52, 168, 83));
                            break;
                        case "UPDATE":
                            setForeground(new Color(66, 133, 244));
                            break;
                        case "DELETE":
                            setForeground(new Color(234, 67, 53));
                            break;
                        case "LOGIN":
                            setForeground(new Color(251, 188, 5));
                            break;
                        case "LOGOUT":
                            setForeground(new Color(154, 160, 166));
                            break;
                    }
                }
                
                setBorder(noFocusBorder);
                return c;
            }
        });
        
        // Enable row sorter
        table.setRowSorter(new TableRowSorter<>(model));
    }

    public void refresh() {
        try {
            model.setRowCount(0);
            String f = dateFrom.getDate() != null ? new java.text.SimpleDateFormat("yyyy-MM-dd").format(dateFrom.getDate()) : "";
            String t = dateTo.getDate() != null ? new java.text.SimpleDateFormat("yyyy-MM-dd").format(dateTo.getDate()) : "";
            List<Map<String,String>> rows;
            
            if (!f.isEmpty() && !t.isEmpty()) {
                rows = DB.query(
                    "SELECT a.created_at, u.nama_lengkap actor, a.action, a.entity, a.entity_id, a.detail " +
                    "FROM audit_log a LEFT JOIN users u ON a.actor_id=u.user_id " +
                    "WHERE DATE(a.created_at) BETWEEN ? AND ? " +
                    "ORDER BY a.audit_id DESC LIMIT 500", f, t
                );
            } else if (!f.isEmpty()) {
                rows = DB.query(
                    "SELECT a.created_at, u.nama_lengkap actor, a.action, a.entity, a.entity_id, a.detail " +
                    "FROM audit_log a LEFT JOIN users u ON a.actor_id=u.user_id " +
                    "WHERE DATE(a.created_at) >= ? " +
                    "ORDER BY a.audit_id DESC LIMIT 500", f
                );
            } else if (!t.isEmpty()) {
                rows = DB.query(
                    "SELECT a.created_at, u.nama_lengkap actor, a.action, a.entity, a.entity_id, a.detail " +
                    "FROM audit_log a LEFT JOIN users u ON a.actor_id=u.user_id " +
                    "WHERE DATE(a.created_at) <= ? " +
                    "ORDER BY a.audit_id DESC LIMIT 500", t
                );
            } else {
                rows = DB.query(
                    "SELECT a.created_at, u.nama_lengkap actor, a.action, a.entity, a.entity_id, a.detail " +
                    "FROM audit_log a LEFT JOIN users u ON a.actor_id=u.user_id " +
                    "ORDER BY a.audit_id DESC LIMIT 500"
                );
            }
            
            for (var r: rows) {
                model.addRow(new Object[]{
                    r.get("created_at"),
                    r.get("actor")==null?"-":r.get("actor"),
                    r.get("action"), r.get("entity"), r.get("entity_id"), r.get("detail"),
                    "" // Button placeholder
                });
            }
            
            // Update statistics
            int creates = 0, updates = 0, deletes = 0, logins = 0;
            for (var r: rows) {
                String action = r.get("action");
                if ("CREATE".equals(action)) creates++;
                else if ("UPDATE".equals(action)) updates++;
                else if ("DELETE".equals(action)) deletes++;
                else if ("LOGIN".equals(action)) logins++;
            }
            
            statsLabel.setText(String.format(
                "<html><b>%d</b> logs | <span style='color:#4285F4'>+%d</span> | <span style='color:#5F6368'>~%d</span> | <span style='color:#80868B'>-%d</span> | <span style='color:#1A73E8'>⚡%d</span></html>",
                rows.size(), creates, updates, deletes, logins
            ));
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
    }
    
    private void clearOldLogs() {
        if (!adminPage.confirmDialog("Clear Logs", 
            "Apakah Anda yakin ingin menghapus log audit yang berumur > 30 hari?\n\n" +
            "Tindakan ini tidak dapat dibatalkan!")) return;
        
        try {
            int deleted = (int) DB.exec("DELETE FROM audit_log WHERE created_at < DATE_SUB(NOW(), INTERVAL 30 DAY)");
            DB.audit(Long.valueOf(adminPage.idValue()), "DELETE", "audit_log", "batch", "Clear old logs (" + deleted + " records)");
            refresh();
            adminPage.showMessageDialog("Sukses", "Berhasil menghapus " + deleted + " log lama.");
        } catch (Exception ex) {
            adminPage.showErrorDialog("Error", "Gagal menghapus log.");
        }
    }
}
