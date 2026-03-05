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

public class PengembalianPanel extends JPanel {
    private final PetugasPage petugasPage;
    private DefaultTableModel model = new DefaultTableModel(new String[]{
        Lang.get("loan.table.id"), Lang.get("loan.table.member"), Lang.get("table.date"), 
        Lang.get("loan.table.duedate"), Lang.get("label.days_late"), Lang.get("table.total")
    },0);
    private JTable table = new JTable(model);
    private JLabel detail = new JLabel(Lang.get("petugas.return.select_loan"));
    private JPanel coversContainer = new JPanel();
    private JTextArea items = new JTextArea();
    private JButton btnProcess;

    public PengembalianPanel(PetugasPage petugasPage) {
        this.petugasPage = petugasPage;
        setBackground(Utils.BG);
        setLayout(new BorderLayout());
        
        // Header
        JPanel header = createPanelHeader(Lang.get("petugas.return.title"), 
            Lang.get("petugas.return.subtitle"));
        
        // Main split pane
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setDividerLocation(650);
        split.setBorder(null);
        
        // Left panel: Transaction list
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(Utils.BG);
        leftPanel.setBorder(new EmptyBorder(0, 20, 20, 10));
        
        styleTable();
        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(Utils.CARD);
        scroll.setBorder(BorderFactory.createLineBorder(Utils.BORDER));
        
        leftPanel.add(scroll, BorderLayout.CENTER);
        
        // Right panel: Details
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(Utils.BG);
        rightPanel.setBorder(new EmptyBorder(0, 10, 20, 20));
        
        JPanel detailsCard = Utils.card();
        detailsCard.setLayout(new BorderLayout(10, 10));
        
        detail.setForeground(Utils.TEXT);
        detail.setFont(Utils.FONT_B);
        detail.setBorder(new EmptyBorder(0, 0, 10, 0));
        
        coversContainer.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 0));
        coversContainer.setOpaque(false);
        JScrollPane coversScroll = new JScrollPane(coversContainer);
        coversScroll.setPreferredSize(new Dimension(0, 200));
        coversScroll.setOpaque(false);
        coversScroll.getViewport().setOpaque(false);
        coversScroll.setBorder(null);
        coversScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        
        items.setBackground(Utils.CARD2);
        items.setForeground(Utils.TEXT);
        items.setEditable(false);
        items.setFont(new Font("Monospaced", Font.PLAIN, 12));
        items.setBorder(BorderFactory.createLineBorder(Utils.BORDER));
        
        btnProcess = petugasPage.createPrimaryButton(Lang.get("petugas.quickaction.return"));
        btnProcess.setEnabled(false);
        btnProcess.addActionListener(e -> processReturn());
        
        detailsCard.add(detail, BorderLayout.NORTH);
        
        JPanel centerContent = new JPanel(new BorderLayout(0, 10));
        centerContent.setOpaque(false);
        centerContent.add(coversScroll, BorderLayout.SOUTH);
        centerContent.add(new JScrollPane(items), BorderLayout.CENTER);
        
        detailsCard.add(centerContent, BorderLayout.CENTER);
        detailsCard.add(btnProcess, BorderLayout.SOUTH);
        
        rightPanel.add(detailsCard, BorderLayout.CENTER);
        
        split.setLeftComponent(leftPanel);
        split.setRightComponent(rightPanel);
        
        // Bottom panel
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomPanel.setBackground(Utils.BG);
        bottomPanel.setBorder(new EmptyBorder(0, 20, 20, 20));
        
        JButton refreshBtn = petugasPage.createSecondaryButton("Refresh List");
        JButton detailBtn = petugasPage.createSecondaryButton(Lang.get("btn.view_detail"));
        
        refreshBtn.addActionListener(e -> refresh());
        detailBtn.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r < 0) { 
                petugasPage.showMessageDialog(Lang.get("msg.warning"), Lang.get("msg.info")); 
                return; 
            }
            int modelRow = table.convertRowIndexToModel(r);
            String loanId = String.valueOf(model.getValueAt(modelRow, 0));
            
            try {
                // Fetch full loan details including items and their covers
                List<Map<String, String>> itemsRows = DB.query(
                    "SELECT b.code, b.judul, b.cover, li.qty " +
                    "FROM loan_items li JOIN books b ON li.book_id = b.book_id " +
                    "WHERE li.loan_id = ?",
                    Integer.parseInt(loanId)
                );
                
                Map<String, String> data = new java.util.HashMap<>();
                data.put("ID PINJAM", loanId);
                data.put(Lang.get("loan.table.member"), String.valueOf(model.getValueAt(modelRow, 1)));
                data.put(Lang.get("table.date"), String.valueOf(model.getValueAt(modelRow, 2)));
                data.put(Lang.get("loan.table.duedate"), String.valueOf(model.getValueAt(modelRow, 3)));
                data.put(Lang.get("label.days_late"), String.valueOf(model.getValueAt(modelRow, 4)));
                
                // Add all covers for the detail page
                if (!itemsRows.isEmpty()) {
                    StringBuilder allCovers = new StringBuilder();
                    for (Map<String, String> it : itemsRows) {
                        String cp = it.get("cover");
                        if (cp != null && !cp.isEmpty()) {
                            if (allCovers.length() > 0) allCovers.append("|");
                            allCovers.append(cp);
                        }
                    }
                    if (allCovers.length() > 0) data.put("cover", allCovers.toString());
                }
                
                // Build a list string for the books
                StringBuilder booksList = new StringBuilder();
                for (Map<String, String> it : itemsRows) {
                    booksList.append("• ").append(it.get("judul"))
                             .append(" (").append(it.get("qty")).append(" buku)<br>");
                }
                data.put("LIST BUKU", booksList.toString());
                
                new nahlib.DetailPage(petugasPage, Lang.get("return.title"), data);
            } catch (Exception ex) {
                ex.printStackTrace();
                petugasPage.showErrorDialog("Error", "Gagal memuat detail pengembalian.");
            }
        });
        
        bottomPanel.add(detailBtn);
        bottomPanel.add(refreshBtn);
        
        add(header, BorderLayout.NORTH);
        add(split, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
        
        // Selection listener
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadDetail();
                btnProcess.setEnabled(table.getSelectedRow() >= 0);
            }
        });
        
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
                
                if (column == 4 && value != null) {
                    try {
                        int daysLate = Integer.parseInt(value.toString());
                        if (daysLate > 0) {
                            setForeground(new Color(234, 67, 53));
                            setFont(getFont().deriveFont(Font.BOLD));
                        }
                    } catch (NumberFormatException e) {
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
            List<Map<String, String>> rows = DB.query(
                "SELECT l.loan_id, IFNULL(u.nama_lengkap, l.guest_name) as anggota, l.tanggal_pinjam, l.jatuh_tempo, " +
                "GREATEST(DATEDIFF(CURDATE(), l.jatuh_tempo), 0) as telat, " +
                "(SELECT SUM(qty) FROM loan_items li WHERE li.loan_id = l.loan_id) as total_item " +
                "FROM loans l LEFT JOIN users u ON l.user_id = u.user_id " +
                "WHERE l.status = 'AKTIF' AND l.petugas_id = ? " +
                "ORDER BY l.loan_id DESC",
                Integer.parseInt(petugasPage.getMe().get("user_id"))
            );
            
            for (Map<String, String> r: rows) {
                model.addRow(new Object[]{
                    r.get("loan_id"), r.get("anggota"), r.get("tanggal_pinjam"),
                    r.get("jatuh_tempo"), r.get("telat"), r.get("total_item")
                });
            }
            
            items.setText("");
            detail.setText(Lang.get("petugas.return.select_loan"));
        } catch (Exception e) {
            e.printStackTrace();
            petugasPage.showErrorDialog(Lang.get("msg.error"), Lang.get("msg.error") + ": " + e.getMessage());
        }
    }

    void loadDetail() {
        int r = table.getSelectedRow();
        if (r < 0) return;
        
        String loanId = model.getValueAt(r, 0).toString();
        String anggota = model.getValueAt(r, 1).toString();
        int telat = Integer.parseInt(model.getValueAt(r, 4).toString());

        try {
            Map<String, Integer> rule = DB.rules();
            int finePerDay = rule.get("fine_per_day");

            List<Map<String, String>> itemsRows = DB.query(
                "SELECT b.code, b.judul, b.cover, li.qty " +
                "FROM loan_items li JOIN books b ON li.book_id = b.book_id " +
                "WHERE li.loan_id = ?",
                Integer.parseInt(loanId)
            );
            
            int fine = telat * finePerDay;

            StringBuilder sb = new StringBuilder();
            sb.append("=== ").append(Lang.get("petugas.return.detail_title")).append(" ===\n");
            sb.append("Loan ID: ").append(loanId).append("\n");
            sb.append(Lang.get("role.user")).append(": ").append(anggota).append("\n");
            sb.append(Lang.get("label.days_late")).append(": ").append(telat).append(" ").append(Lang.get("nav.history").toLowerCase()).append("\n");
            sb.append("Denda/hari: Rp ").append(finePerDay).append("\n");
            sb.append("Total denda: Rp ").append(fine).append("\n\n");
            sb.append("=== ").append(Lang.get("petugas.return.book_list")).append(" ===\n");
            
            coversContainer.removeAll();
            for (Map<String, String> it: itemsRows) {
                sb.append("• ").append(it.get("code")).append(" - ")
                  .append(it.get("judul")).append(" (")
                  .append(it.get("qty")).append(" buku)\n");

                JLabel lblCover = new JLabel();
                lblCover.setHorizontalAlignment(SwingConstants.CENTER);
                lblCover.setBorder(new EmptyBorder(0, 0, 0, 0));
                
                if (it.get("cover") != null) {
                    ImageIcon icon = Utils.getCover(it.get("cover"), 110, 165);
                    if (icon != null) lblCover.setIcon(icon);
                    else lblCover.setText("No Image");
                } else {
                    lblCover.setText("No Image");
                    lblCover.setForeground(Utils.MUTED);
                }
                coversContainer.add(lblCover);
            }
            coversContainer.revalidate();
            coversContainer.repaint();
            
            if (telat > 0) {
                sb.append("\n").append(Lang.get("petugas.return.fine_warning"));
            }
            
            detail.setText(Lang.get("petugas.return.detail_title") + ": " + loanId + " - " + anggota);
            items.setText(sb.toString());

        } catch (Exception e) {
            e.printStackTrace();
            petugasPage.showErrorDialog(Lang.get("msg.error"), Lang.get("msg.error") + ": " + e.getMessage());
        }
    }

    void processReturn() {
        int r = table.getSelectedRow();
        if (r < 0) { 
            petugasPage.showMessageDialog("Peringatan", "Pilih transaksi terlebih dahulu."); 
            return; 
        }
        
        String loanId = model.getValueAt(r, 0).toString();
        String anggota = model.getValueAt(r, 1).toString();
        int telat = Integer.parseInt(model.getValueAt(r, 4).toString());

        try {
            Map<String, Integer> rule = DB.rules();
            int fine = telat * rule.get("fine_per_day");
            
            String message = String.format(Lang.get("petugas.return.confirm_msg"), 
                loanId, anggota, telat, fine);
            
            if (!petugasPage.confirmDialog(Lang.get("return.confirm.title"), message)) return;

            DB.tx(() -> {
                // Restore stock
                List<Map<String, String>> itemsRows = DB.query(
                    "SELECT book_id, qty FROM loan_items WHERE loan_id = ?", 
                    Integer.parseInt(loanId)
                );
                
                for (Map<String, String> it: itemsRows) {
                    DB.exec(
                        "UPDATE books SET stok_tersedia = stok_tersedia + ? WHERE book_id = ?",
                        Integer.parseInt(it.get("qty")), Integer.parseInt(it.get("book_id"))
                    );
                }
                
                // Update loan status
                DB.exec("UPDATE loans SET status = 'SELESAI' WHERE loan_id = ?", Integer.parseInt(loanId));
                
                // Record return
                DB.exec(
                    "INSERT INTO returns(loan_id, tanggal_kembali, fine_total, note) VALUES (?, CURDATE(), ?, ?)",
                    Integer.parseInt(loanId), fine, telat > 0 ? "Telat " + telat + " hari" : "Tepat waktu"
                );

                DB.audit(Long.parseLong(petugasPage.getMe().get("user_id")), "UPDATE", "loans", loanId, 
                    "Pengembalian selesai untuk " + anggota);
            });

            petugasPage.showMessageDialog(Lang.get("msg.success"), Lang.get("return.msg.success") + "\n" +
                (telat > 0 ? Lang.get("label.fine") + ": Rp " + fine : Lang.get("return.status.ontime")));
            
            refresh();

        } catch (Exception ex) {
            ex.printStackTrace();
            petugasPage.showErrorDialog(Lang.get("msg.error"), Lang.get("msg.error") + ": " + ex.getMessage());
        }
    }
}
