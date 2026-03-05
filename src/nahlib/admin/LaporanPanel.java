package nahlib.admin;

import nahlib.Lang;
import nahlib.DB;
import nahlib.Utils;
import com.toedter.calendar.JDateChooser;
import nahlib.CustomIcon;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class LaporanPanel extends JPanel {
    private final AdminPage adminPage;
    private final DefaultTableModel model = new DefaultTableModel(new String[]{
        Lang.get("loan.table.id"), 
        Lang.get("loan.table.loandate"), 
        Lang.get("loan.table.duedate"), 
        Lang.get("loan.table.member"), 
        Lang.get("nav.staff"), 
        Lang.get("loan.table.status"), 
        Lang.get("table.total"),
        Lang.get("btn.view_detail")
    }, 0);
    private final JTable table = new JTable(model);
    private JButton btnRefresh;
    private final JDateChooser from = Utils.dateChooser();
    private final JDateChooser to = Utils.dateChooser();
    private JComboBox<String> statusFilter;
    private JTextField searchField;
    
    // Summary Labels
    private final JLabel lblTotal = createStatLabel("0");
    private final JLabel lblActive = createStatLabel("0");
    private final JLabel lblReturned = createStatLabel("0");
    private final JLabel lblOverdue = createStatLabel("0");

    public LaporanPanel(AdminPage adminPage) {
        this.adminPage = adminPage;
        setBackground(Utils.BG);
        setLayout(new BorderLayout());
        
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setBackground(Utils.BG);
        
        // 1. Header
        JPanel header = createHeader();
        topContainer.add(header);
        
        // 2. Summary Cards
        JPanel cards = createSummaryCards();
        topContainer.add(cards);
        
        // 3. Filters
        JPanel filters = createFilters();
        topContainer.add(filters);
        
        add(topContainer, BorderLayout.NORTH);
        
        // 4. Table
        styleTable();
        new nahlib.TableButton(Lang.get("btn.view_detail"), r -> {
            int modelRow = table.convertRowIndexToModel(r);
            Map<String, String> data = new HashMap<>();
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

            new nahlib.DetailPage(adminPage, Lang.get("admin.laporan.title"), data);
        }).install(table, 7); // Detail button is at index 7
        
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new EmptyBorder(0, 20, 20, 20));
        scroll.getViewport().setBackground(Utils.CARD);
        
        add(scroll, BorderLayout.CENTER);
        
        refresh();
    }
    
    private JPanel createHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Utils.BG);
        p.setBorder(new EmptyBorder(20, 20, 10, 20));
        
        JLabel title = new JLabel(Lang.get("admin.laporan.title"));
        title.setForeground(Utils.TEXT);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        
        JLabel subtitle = new JLabel(Lang.get("admin.laporan.subtitle"));
        subtitle.setForeground(Utils.MUTED);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        JPanel txt = new JPanel(new BorderLayout());
        txt.setOpaque(false);
        txt.add(title, BorderLayout.NORTH);
        txt.add(subtitle, BorderLayout.SOUTH);
        
        p.add(txt, BorderLayout.WEST);
        return p;
    }
    
    private JPanel createSummaryCards() {
        JPanel p = new JPanel(new GridLayout(1, 4, 15, 0));
        p.setBackground(Utils.BG);
        p.setBorder(new EmptyBorder(10, 20, 20, 20));
        
        p.add(createCard(Lang.get("report.card.total"), lblTotal, new Color(66, 133, 244))); // Blue
        p.add(createCard(Lang.get("report.card.active"), lblActive, new Color(251, 188, 5))); // Yellow
        p.add(createCard(Lang.get("report.card.returned"), lblReturned, new Color(52, 168, 83))); // Green
        p.add(createCard(Lang.get("report.card.overdue"), lblOverdue, new Color(234, 67, 53))); // Red
        
        return p;
    }
    
    private JPanel createCard(String title, JLabel valueLabel, Color accent) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Utils.CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 0, 0, Utils.BORDER),
            new EmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel t = new JLabel(title.toUpperCase());
        t.setForeground(Utils.MUTED);
        t.setFont(new Font("Segoe UI", Font.BOLD, 11));
        
        card.add(t, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        
        JPanel bar = new JPanel();
        bar.setBackground(accent);
        bar.setPreferredSize(new Dimension(3, 0));
        
        JPanel cont = new JPanel(new BorderLayout());
        cont.setOpaque(false);
        cont.add(bar, BorderLayout.WEST);
        cont.add(card, BorderLayout.CENTER);
        
        return cont;
    }
    
    private JLabel createStatLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(Utils.TEXT);
        l.setFont(new Font("Segoe UI", Font.BOLD, 24));
        l.setBorder(new EmptyBorder(5, 0, 0, 0));
        return l;
    }
    
    private JPanel createFilters() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        p.setBackground(Utils.BG);
        p.setBorder(new EmptyBorder(0, 10, 10, 10));
        
        // Date Range
        p.add(createLabel(Lang.get("report.label.from")));
        from.setPreferredSize(new Dimension(120, 35));
        p.add(from);
        
        p.add(createLabel(Lang.get("report.label.to")));
        to.setPreferredSize(new Dimension(120, 35));
        p.add(to);
        
        // Status Filter
        p.add(createLabel(Lang.get("filter.status")));
        statusFilter = new JComboBox<>(new String[]{
            Lang.get("filter.all_status"), "AKTIF", "SELESAI", "BATAL"
        });
        statusFilter.setPreferredSize(new Dimension(140, 35));
        statusFilter.setBackground(Utils.CARD);
        statusFilter.setForeground(Utils.TEXT);
        p.add(statusFilter);
        
        // Live Listeners for JDateChooser
        from.addPropertyChangeListener("date", evt -> refresh());
        to.addPropertyChangeListener("date", evt -> refresh());
        
        statusFilter.addActionListener(e -> refresh());
        
        // Buttons
        JButton clearBtn = adminPage.createSecondaryButton(Lang.get("btn.clear"));
        JButton exportBtn = adminPage.createPrimaryButton(Lang.get("btn.export") + " CSV");
        
        clearBtn.addActionListener(e -> {
            from.setDate(null);
            to.setDate(null);
            statusFilter.setSelectedIndex(0);
            searchField.setText("");
            // refresh() will be called automatically by listeners
        });
        exportBtn.addActionListener(e -> exportCSV());
        
        p.add(clearBtn);
        p.add(new JLabel("  |  "));
        
        // Search
        searchField = new JTextField(15);
        searchField.setFont(Utils.FONT);
        searchField.setPreferredSize(new Dimension(150, 35));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Utils.BORDER),
            new EmptyBorder(6, 10, 6, 10)
        ));
        searchField.putClientProperty("JTextField.placeholderText", Lang.get("btn.search") + "...");
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filterTable(); }
            public void removeUpdate(DocumentEvent e) { filterTable(); }
            public void changedUpdate(DocumentEvent e) { filterTable(); }
        });
        
        p.add(searchField);
        p.add(exportBtn);
        
        return p;
    }
    
    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text + ":");
        l.setForeground(Utils.TEXT);
        l.setFont(Utils.FONT);
        return l;
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
                
                // Status column styling (Limit index check to avoid errors if columns change)
                if (column == 5) {
                    if ("AKTIF".equals(value)) {
                        setForeground(new Color(66, 133, 244));
                        setFont(getFont().deriveFont(Font.BOLD));
                    } else if ("SELESAI".equals(value)) {
                        setForeground(new Color(52, 168, 83));
                    } else if ("BATAL".equals(value)) {
                        setForeground(new Color(234, 67, 53));
                    }
                }
                
                // Overdue highlight
                if (column == 2 && value != null) {
                    try {
                        String dueDate = value.toString();
                        // Access status safely from model to avoid view index confusion
                        int modelRow = table.convertRowIndexToModel(row);
                        String status = (String) model.getValueAt(modelRow, 5);
                        
                        if ("AKTIF".equals(status)) {
                            java.sql.Date due = java.sql.Date.valueOf(dueDate);
                            java.sql.Date today = new java.sql.Date(System.currentTimeMillis());
                            if (due.before(today)) {
                                setForeground(new Color(234, 67, 53));
                                setFont(getFont().deriveFont(Font.BOLD));
                            }
                        }
                    } catch (Exception e) {}
                }
                
                setBorder(noFocusBorder);
                return c;
            }
        });
        
        table.setRowSorter(new TableRowSorter<>(model));
    }
    
    private void filterTable() {
        String text = searchField.getText();
        TableRowSorter<DefaultTableModel> sorter = (TableRowSorter<DefaultTableModel>) table.getRowSorter();
        if (text.trim().length() == 0) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
        }
    }

    public void refresh() {
        try {
            model.setRowCount(0);
            
            String f = from.getDate() != null ? new java.text.SimpleDateFormat("yyyy-MM-dd").format(from.getDate()) : "";
            String t = to.getDate() != null ? new java.text.SimpleDateFormat("yyyy-MM-dd").format(to.getDate()) : "";
            String s = (String) statusFilter.getSelectedItem();
            boolean formatStatus = s != null && !s.equals(Lang.get("filter.all_status"));
            
            StringBuilder query = new StringBuilder(
                "SELECT l.loan_id, l.tanggal_pinjam, l.jatuh_tempo, " +
                "IFNULL(u.nama_lengkap, l.guest_name) anggota, p.nama_lengkap petugas, l.status, " +
                "(SELECT SUM(qty) FROM loan_items li WHERE li.loan_id=l.loan_id) total_item " +
                "FROM loans l " +
                "LEFT JOIN users u ON l.user_id=u.user_id " +
                "JOIN users p ON l.petugas_id=p.user_id " +
                "WHERE 1=1 "
            );
            
            if (!f.isEmpty()) query.append("AND l.tanggal_pinjam >= '").append(f).append("' ");
            if (!t.isEmpty()) query.append("AND l.tanggal_pinjam <= '").append(t).append("' ");
            if (formatStatus) query.append("AND l.status = '").append(s).append("' ");
            
            query.append("ORDER BY l.loan_id DESC");
            
            List<Map<String,String>> rows = DB.query(query.toString());
            
            int total = 0;
            int active = 0;
            int returned = 0;
            int overdue = 0;
            
            for (var r: rows) {
                String st = r.get("status");
                String due = r.get("jatuh_tempo");
                
                model.addRow(new Object[]{
                    r.get("loan_id"), r.get("tanggal_pinjam"), r.get("jatuh_tempo"),
                    r.get("anggota"), r.get("petugas"), st, r.get("total_item"),
                    "" // Button placeholder
                });
                
                total++;
                if ("AKTIF".equals(st)) {
                    active++;
                    // Check overdue
                    try {
                        if (java.sql.Date.valueOf(due).before(new java.sql.Date(System.currentTimeMillis()))) {
                            overdue++;
                        }
                    } catch (Exception e) {}
                } else if ("SELESAI".equals(st)) {
                    returned++;
                }
            }
            
            // Update cards
            lblTotal.setText(String.valueOf(total));
            lblActive.setText(String.valueOf(active));
            lblReturned.setText(String.valueOf(returned));
            lblOverdue.setText(String.valueOf(overdue));
            
            if (overdue > 0) lblOverdue.setForeground(new Color(234, 67, 53));
            else lblOverdue.setForeground(Utils.TEXT);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void exportCSV() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("laporan_peminjaman_" + java.time.LocalDate.now().toString() + ".csv"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                DB.exportTableToCSV("loans", fc.getSelectedFile()); // Ideally export filtered view, but for now full table dump or model dump
                // actually better to export current model data
                
                java.io.PrintWriter pw = new java.io.PrintWriter(fc.getSelectedFile());
                StringBuilder sb = new StringBuilder();
                
                // Headers
                for (int i = 0; i < model.getColumnCount(); i++) {
                    sb.append(model.getColumnName(i));
                    if (i < model.getColumnCount() - 1) sb.append(",");
                }
                sb.append("\n");
                
                // Rows
                for (int i = 0; i < model.getRowCount(); i++) {
                    for (int j = 0; j < model.getColumnCount(); j++) {
                        Object val = model.getValueAt(i, j);
                        sb.append(val != null ? val.toString() : "");
                        if (j < model.getColumnCount() - 1) sb.append(",");
                    }
                    sb.append("\n");
                }
                
                pw.write(sb.toString());
                pw.close();
                
                adminPage.showMessageDialog(Lang.get("msg.success"), Lang.get("msg.success"));
                DB.audit(Long.valueOf(adminPage.idValue()), "EXPORT", "loans", "csv", "Export Loan Report CSV");
                
            } catch (Exception ex) {
                adminPage.showErrorDialog("Error", "Export failed: " + ex.getMessage());
            }
        }
    }
}
