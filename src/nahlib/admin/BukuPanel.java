package nahlib.admin;

import nahlib.Lang;
import nahlib.DB;
import nahlib.Utils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class BukuPanel extends JPanel {
    private final AdminPage adminPage;
    private final DefaultTableModel model = new DefaultTableModel(new String[]{
        "ID", "Kode", "ISBN", Lang.get("books.form.title"), Lang.get("books.form.author"), 
        Lang.get("books.form.publisher"), Lang.get("books.table.year"), 
        Lang.get("books.table.category"), Lang.get("books.form.location"), 
        Lang.get("books.table.stock"), Lang.get("books.table.available"),
        Lang.get("btn.view_detail"), "cover", "description" // Hidden columns
    }, 0);
    private final JTable table = new JTable(model);
    private JTextField searchField;
    private JLabel statsLabel;

    public BukuPanel(AdminPage adminPage) {
        this.adminPage = adminPage;
        setBackground(Utils.BG);
        setLayout(new BorderLayout());
        
        // Header dengan live search
        JPanel header = createPanelHeader(Lang.get("admin.books.title"), 
            Lang.get("admin.books.subtitle"));
        
        // Table
        styleTable();
        new nahlib.TableButton(Lang.get("btn.view_detail"), r -> {
            int modelRow = table.convertRowIndexToModel(r);
            new nahlib.DetailPage(adminPage, Lang.get("books.title"), getRowData(modelRow));
        }).install(table, 11);
        
        // Hide cover column
        table.getColumnModel().getColumn(12).setMinWidth(0);
        table.getColumnModel().getColumn(12).setMaxWidth(0);
        table.getColumnModel().getColumn(12).setPreferredWidth(0);
        
        table.getColumnModel().getColumn(13).setMinWidth(0); // Hide sinopsis
        table.getColumnModel().getColumn(13).setMaxWidth(0);
        table.getColumnModel().getColumn(13).setPreferredWidth(0);
        
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new EmptyBorder(0, 20, 20, 20));
        scroll.getViewport().setBackground(Utils.CARD);
        
        // Action buttons
        JPanel actions = createActionPanel();
        
        add(header, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(actions, BorderLayout.SOUTH);
        
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
        
        // Search panel di pojok kanan atas
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        searchPanel.setOpaque(false);
        
        searchField = new JTextField(20);
        searchField.setFont(Utils.FONT);
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Utils.BORDER),
            new EmptyBorder(6, 10, 6, 10)
        ));
        searchField.putClientProperty("JTextField.placeholderText", "Cari buku...");
        
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
        
        // Statistics label instead of refresh button
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
        table.setRowHeight(80); // Increased row height for thumbnail
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
        
        table.getColumnModel().getColumn(3).setPreferredWidth(300); // Judul

        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                // Add thumbnail to Judul column (index 3)
                if (column == 3) {
                    JPanel p = new JPanel(new BorderLayout(15, 0));
                    p.setBackground(isSelected ? Utils.ACCENT : Utils.CARD);
                    p.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                    
                    int modelRow = table.convertRowIndexToModel(row);
                    String coverPath = (String) table.getModel().getValueAt(modelRow, 12);
                    
                    JLabel img = new JLabel();
                    if (coverPath != null && !coverPath.equals("null") && !coverPath.isEmpty()) {
                        ImageIcon icon = Utils.getCover(coverPath, 48, 72);
                        if (icon != null) img.setIcon(icon);
                    } else {
                        img.setOpaque(true);
                        img.setBackground(Utils.CARD2);
                        img.setPreferredSize(new Dimension(48, 72));
                        img.setHorizontalAlignment(SwingConstants.CENTER);
                        img.setText("?");
                        img.setForeground(Utils.MUTED);
                    }
                    
                    JLabel txt = new JLabel(String.valueOf(value));
                    txt.setForeground(isSelected ? Color.WHITE : Utils.TEXT);
                    txt.setFont(new Font("Segoe UI", Font.BOLD, 14));
                    
                    p.add(img, BorderLayout.WEST);
                    p.add(txt, BorderLayout.CENTER);
                    return p;
                }
                
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setBackground(isSelected ? Utils.ACCENT : Utils.CARD);
                c.setForeground(isSelected ? Color.WHITE : Utils.TEXT);
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                
                // Warn if available stock is low (column 10 - stok tersedia)
                if (column == 10 && value != null) {
                    try {
                        int available = Integer.parseInt(value.toString());
                        if (available <= 0) {
                            setForeground(new Color(234, 67, 53));
                        } else if (available <= 2) {
                            setForeground(new Color(251, 188, 5));
                        }
                    } catch (NumberFormatException e) {
                        // Ignore
                    }
                }
                
                setBorder(noFocusBorder);
                return c;
            }
        });
        
        // Enable row sorter
        table.setRowSorter(new TableRowSorter<>(model));
    }
    
    private JPanel createActionPanel() {
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actions.setBackground(Utils.BG);
        actions.setBorder(new EmptyBorder(0, 20, 20, 20));
        
        JButton add = adminPage.createPrimaryButton(Lang.get("books.add_title"));
        JButton edit = adminPage.createSecondaryButton(Lang.get("btn.edit"));
        JButton delete = adminPage.createSecondaryButton(Lang.get("btn.delete"));
        JButton kategori = adminPage.createSecondaryButton(Lang.get("books.btn.manage_cat"));
        JButton rak = adminPage.createSecondaryButton(Lang.get("books.btn.manage_rack"));
        
        add.addActionListener(e -> openForm(null));
        edit.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r < 0) { 
                adminPage.showMessageDialog(Lang.get("msg.warning"), Lang.get("msg.info")); 
                return; 
            }
            int modelRow = table.convertRowIndexToModel(r);
            openForm(getRowData(modelRow));
        });
        
        delete.addActionListener(e -> deleteBuku());
        kategori.addActionListener(e -> openKategoriManager());
        rak.addActionListener(e -> openRakManager());
        
        actions.add(add);
        actions.add(edit);
        actions.add(delete);
        actions.add(kategori);
        actions.add(rak);
        
        return actions;
    }
    
    private Map<String,String> getRowData(int row) {
        try {
            Map<String, String> data = new HashMap<>();
            
            String bookId = String.valueOf(model.getValueAt(row, 0));
            var bookDataArr = DB.query("SELECT * FROM books WHERE book_id = ?", bookId);
            if (bookDataArr.isEmpty()) return data;
            var bookData = bookDataArr.get(0);
            
            data.put("book_id", bookId);
            data.put("code", String.valueOf(model.getValueAt(row,1)));
            data.put("isbn", String.valueOf(model.getValueAt(row,2)));
            data.put("judul", String.valueOf(model.getValueAt(row,3)));
            data.put("penulis", String.valueOf(model.getValueAt(row,4)));
            data.put("penerbit", String.valueOf(model.getValueAt(row,5)));
            data.put("tahun", String.valueOf(model.getValueAt(row,6)));
            data.put("category_id", bookData.get("category_id") != null ? bookData.get("category_id") : "");
            data.put("rack_id", bookData.get("rack_id") != null ? bookData.get("rack_id") : "");
            data.put("stok_total", String.valueOf(model.getValueAt(row,9)));
            data.put("stok_itersedia", String.valueOf(model.getValueAt(row,10)));
            data.put("cover", bookData.get("cover") != null ? bookData.get("cover") : null);
            data.put("description", bookData.get("description") != null ? bookData.get("description") : "");
            
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    public void refresh() {
        try {
            model.setRowCount(0);
            var rows = DB.query(
                "SELECT b.book_id,b.code,b.isbn,b.judul,b.penulis,b.penerbit,b.tahun, " +
                "c.name kategori, r.code rak, b.stok_total,b.stok_tersedia, b.cover, b.description " +
                "FROM books b " +
                "LEFT JOIN categories c ON b.category_id=c.category_id " +
                "LEFT JOIN racks r ON b.rack_id=r.rack_id " +
                "ORDER BY b.book_id DESC"
            );
            for (var r: rows) {
                model.addRow(new Object[]{
                    r.get("book_id"), 
                    r.get("code"), 
                    r.get("isbn") == null ? "" : r.get("isbn"),
                    r.get("judul"),
                    r.get("penulis") == null ? "" : r.get("penulis"),
                    r.get("penerbit") == null ? "" : r.get("penerbit"),
                    r.get("tahun") == null ? "" : r.get("tahun"),
                    r.get("kategori") == null ? "" : r.get("kategori"), 
                    r.get("rak") == null ? "" : r.get("rak"),
                    r.get("stok_total"), 
                    r.get("stok_tersedia"),
                    "", // Button placeholder
                    r.get("cover"), // Added cover to model
                    r.get("description") // Added description to model
                });
            }
            
            // Update statistics
            int totalStok = 0, tersedia = 0, lowStock = 0;
            for (var r: rows) {
                int stokTotal = Integer.parseInt(r.get("stok_total"));
                int stokTersedia = Integer.parseInt(r.get("stok_tersedia"));
                totalStok += stokTotal;
                tersedia += stokTersedia;
                if (stokTersedia > 0 && stokTersedia <= 2) lowStock++;
            }
            
            statsLabel.setText(String.format(
                "<html><b>%d</b> buku | <span style='color:#4285F4'>✓ %d</span> | <span style='color:#5F6368'>⚠ %d</span> | <span style='color:#1A73E8'>📚 %d</span></html>",
                rows.size(), tersedia, lowStock, totalStok
            ));
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
    }
    
    private void deleteBuku() {
        int r = table.getSelectedRow();
        if (r < 0) { 
            adminPage.showMessageDialog(Lang.get("msg.warning"), Lang.get("msg.info")); 
            return; 
        }
        
        int modelRow = table.convertRowIndexToModel(r);
        String id = String.valueOf(model.getValueAt(modelRow,0));
        String judul = String.valueOf(model.getValueAt(modelRow,3));
        
        if (!adminPage.confirmDialog(Lang.get("btn.delete"), 
            Lang.get("msg.confirm") + " " + Lang.get("books.form.title") + ":\n" + 
            judul + "\n\n" +
            Lang.get("msg.warn_delete_borrowed"))) return;
        
        try {
            DB.exec("DELETE FROM books WHERE book_id=?", id);
            DB.audit(Long.valueOf(adminPage.idValue()), "DELETE", "books", id, "Hapus buku");
            refresh();
            adminPage.showMessageDialog(Lang.get("msg.success"), Lang.get("msg.success"));
        } catch (Exception ex) { 
            adminPage.showErrorDialog(Lang.get("msg.error"), Lang.get("msg.error")); 
        }
    }
    
    private static class Item {
        String id; String name;
        Item(String id, String name) { this.id=id; this.name=name; }
        @Override
        public String toString() { return name; }
    }

    public void openForm(Map<String,String> data) {
        JDialog d = new JDialog(adminPage, true);
        d.setTitle(data==null ? "Tambah Buku Baru":"Edit Data Buku");
        d.setSize(700, 650);
        d.setLocationRelativeTo(this);
        d.getContentPane().setBackground(Utils.BG);

        JPanel form = createFormPanel(data, d);
        d.setContentPane(form);
        d.setVisible(true);
    }
    
    private JPanel createFormPanel(Map<String,String> data, JDialog dialog) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Utils.BG);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JLabel title = new JLabel(data==null ? "Tambah Buku Baru" : "Edit Data Buku");
        title.setForeground(Utils.TEXT);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setBorder(new EmptyBorder(0, 0, 20, 0));
        
        // Load combo box data
        JComboBox<Item> cbKat = new JComboBox<>();
        JComboBox<Item> cbRak = new JComboBox<>();
        loadCombo(cbKat, "SELECT category_id id, name name FROM categories ORDER BY name");
        loadCombo(cbRak, "SELECT rack_id id, code name FROM racks ORDER BY code");
        
        // Form fields in a ScrollPane for better flexibility
        JPanel fields = new JPanel();
        fields.setLayout(new BoxLayout(fields, BoxLayout.Y_AXIS));
        fields.setOpaque(false);
        fields.setBorder(new EmptyBorder(0, 5, 0, 5));
        
        JTextField code = Utils.input("kode buku");
        JTextField isbn = Utils.input("isbn");
        JTextField judul = Utils.input("judul");
        JTextField penulis = Utils.input("penulis");
        JTextField penerbit = Utils.input("penerbit");
        JTextField tahun = Utils.input("tahun (angka)");
        Utils.numericOnly(tahun);
        JTextField stok = Utils.input("stok total (angka)");
        Utils.numericOnly(stok);
        
        JTextArea description = new JTextArea(5, 20);
        description.setLineWrap(true);
        description.setWrapStyleWord(true);
        description.setFont(Utils.FONT);
        description.setForeground(Utils.TEXT);
        description.setBackground(Utils.CARD2); // Input background
        description.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Utils.BORDER),
            new EmptyBorder(5, 5, 5, 5)
        ));
        JScrollPane descriptionScroll = new JScrollPane(description);
        descriptionScroll.setBorder(null);
        descriptionScroll.getViewport().setOpaque(false);
        descriptionScroll.setOpaque(false);
        
        // Modern Cover Picker (Styled like Logo Settings)
        JPanel coverPanel = new JPanel();
        coverPanel.setLayout(new BoxLayout(coverPanel, BoxLayout.Y_AXIS));
        coverPanel.setOpaque(false);
        
        JLabel lblCover = new JLabel("Pilih Sampul");
        lblCover.setForeground(Utils.MUTED);
        lblCover.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblCover.setHorizontalAlignment(SwingConstants.CENTER);
        lblCover.setPreferredSize(new Dimension(120, 180));
        lblCover.setMaximumSize(new Dimension(120, 180));
        lblCover.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Utils.BORDER),
            new EmptyBorder(5, 5, 5, 5)
        ));
        lblCover.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JButton btnPick = adminPage.createSecondaryButton("Ganti Gambar");
        btnPick.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnPick.setMaximumSize(new Dimension(120, 35));
        
        String[] currentCover = { data != null ? data.get("cover") : null };
        
        if (currentCover[0] != null) {
            ImageIcon icon = Utils.getCover(currentCover[0], 110, 170);
            if (icon != null) {
                lblCover.setIcon(icon);
                lblCover.setText("");
            }
        }
        
        btnPick.addActionListener(e -> {
            File f = Utils.selectImage(dialog);
            if (f != null) {
                String newPath = Utils.saveCover(f);
                if (newPath != null) {
                    currentCover[0] = newPath;
                    ImageIcon icon = Utils.getCover(newPath, 110, 170);
                    lblCover.setIcon(icon);
                    lblCover.setText("");
                }
            }
        });
        
        coverPanel.add(lblCover);
        coverPanel.add(Box.createVerticalStrut(10));
        coverPanel.add(btnPick);
        
        // Style comboboxes
        styleCombo(cbKat);
        styleCombo(cbRak);
        
        if (data != null) {
            code.setText(data.get("code"));
            isbn.setText(data.get("isbn"));
            judul.setText(data.get("judul"));
            penulis.setText(data.get("penulis"));
            penerbit.setText(data.get("penerbit"));
            tahun.setText(data.get("tahun"));
            stok.setText(data.get("stok_total"));
            
            selectCombo(cbKat, data.get("category_id"));
            selectCombo(cbRak, data.get("rack_id"));
            description.setText(data.get("description"));
        }
        
        fields.add(coverPanel);
        fields.add(Box.createVerticalStrut(20));
        fields.add(createFormRow("Kode Buku*", code));
        fields.add(Box.createVerticalStrut(10));
        fields.add(createFormRow("ISBN", isbn));
        fields.add(Box.createVerticalStrut(10));
        fields.add(createFormRow("Judul*", judul));
        fields.add(Box.createVerticalStrut(10));
        fields.add(createFormRow("Penulis", penulis));
        fields.add(Box.createVerticalStrut(10));
        fields.add(createFormRow("Penerbit", penerbit));
        fields.add(Box.createVerticalStrut(10));
        fields.add(createFormRow("Tahun", tahun));
        fields.add(Box.createVerticalStrut(10));
        fields.add(createFormRow("Kategori", cbKat));
        fields.add(Box.createVerticalStrut(10));
        fields.add(createFormRow("Rak", cbRak));
        fields.add(Box.createVerticalStrut(10));
        fields.add(createFormRow("Stok Total*", stok));
        fields.add(Box.createVerticalStrut(10));
        fields.add(createFormRow("Deskripsi", descriptionScroll));
        
        JScrollPane scroll = new JScrollPane(fields);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(12);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 20));
        buttons.setOpaque(false);
        
        JButton cancel = adminPage.createSecondaryButton("Batal");
        JButton save = adminPage.createPrimaryButton(data==null ? "Simpan" : "Update");
        
        cancel.addActionListener(e -> dialog.dispose());
        save.addActionListener(e -> saveBuku(data, code, isbn, judul, penulis, penerbit, tahun, stok, description, cbKat, cbRak, currentCover[0], dialog));
        
        buttons.add(cancel);
        buttons.add(save);
        
        panel.add(title, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createFormRow(String label, JComponent component) {
        JPanel row = new JPanel(new BorderLayout(10, 5));
        row.setOpaque(false);
        
        JLabel lbl = new JLabel(label + ":");
        lbl.setForeground(Utils.TEXT);
        lbl.setFont(Utils.FONT);
        lbl.setPreferredSize(new Dimension(150, 30));
        
        row.add(lbl, BorderLayout.WEST);
        row.add(component, BorderLayout.CENTER);
        
        return row;
    }
    
    private void styleCombo(JComboBox<?> cb) {
        cb.setBackground(Utils.CARD2);
        cb.setForeground(Utils.TEXT);
        cb.setBorder(BorderFactory.createLineBorder(Utils.BORDER));
    }
    
    private void loadCombo(JComboBox<Item> cb, String sql) {
        try {
            cb.removeAllItems();
            cb.addItem(new Item(null, "-- Pilih --"));
            var rows = DB.query(sql);
            for (var r: rows) cb.addItem(new Item(r.get("id"), r.get("name")));
        } catch (Exception ignored) {}
    }
    
    private void selectCombo(JComboBox<Item> cb, String id) {
        if (id == null || id.isEmpty()) {
            cb.setSelectedIndex(0);
            return;
        }
        for (int i=0; i<cb.getItemCount(); i++) {
            Item it = cb.getItemAt(i);
            if (it != null && it.id != null && it.id.equals(id)) { 
                cb.setSelectedIndex(i); 
                break; 
            }
        }
    }
    
    private void saveBuku(Map<String,String> data, JTextField code, JTextField isbn, JTextField judul, 
                         JTextField penulis, JTextField penerbit, JTextField tahun, JTextField stok, JTextArea description,
                         JComboBox<Item> cbKat, JComboBox<Item> cbRak, String coverPath, JDialog dialog) {
        try {
            if (code.getText().trim().isEmpty() || judul.getText().trim().isEmpty() || stok.getText().trim().isEmpty()) { 
                adminPage.showMessageDialog("Peringatan", "Kode, judul, dan stok wajib diisi."); 
                return; 
            }
            
            int stokTotal;
            try {
                stokTotal = Integer.parseInt(stok.getText().trim());
                if (stokTotal < 0) {
                    adminPage.showMessageDialog("Peringatan", "Stok tidak boleh negatif.");
                    return;
                }
            } catch (NumberFormatException e) {
                adminPage.showErrorDialog("Error", "Format angka tidak valid pada stok.");
                return;
            }
            
            Integer tahunVal = null;
            if (!tahun.getText().trim().isEmpty()) {
                try {
                    tahunVal = Integer.parseInt(tahun.getText().trim());
                } catch (NumberFormatException e) {
                    adminPage.showErrorDialog("Error", "Format tahun tidak valid.");
                    return;
                }
            }
            
            Item kat = (Item) cbKat.getSelectedItem();
            Item rak = (Item) cbRak.getSelectedItem();
            
            String katId = (kat == null || kat.id == null) ? null : kat.id;
            String rakId = (rak == null || rak.id == null) ? null : rak.id;
            
            if (data == null) {
                // Tambah buku baru
                long idNew = DB.exec(
                    "INSERT INTO books(code,isbn,judul,penulis,penerbit,tahun,category_id,rack_id,stok_total,stok_tersedia,cover,description) " +
                    "VALUES (?,?,?,?,?,?,?,?,?,?,?,?)",
                    code.getText().trim(), 
                    isbn.getText().trim().isEmpty() ? null : isbn.getText().trim(), 
                    judul.getText().trim(),
                    penulis.getText().trim().isEmpty() ? null : penulis.getText().trim(), 
                    penerbit.getText().trim().isEmpty() ? null : penerbit.getText().trim(),
                    tahunVal,
                    katId,
                    rakId,
                    stokTotal, 
                    stokTotal,
                    coverPath,
                    description.getText().trim()
                );
                DB.audit(Long.valueOf(adminPage.idValue()), "CREATE", "books", String.valueOf(idNew), "Tambah buku");
                adminPage.showMessageDialog("Sukses", "Buku berhasil ditambahkan.");
            } else {
                // Update buku yang ada
                DB.exec(
                    "UPDATE books SET code=?,isbn=?,judul=?,penulis=?,penerbit=?,tahun=?,category_id=?,rack_id=?,stok_total=?,stok_tersedia=?,cover=?,description=? WHERE book_id=?",
                    code.getText().trim(), 
                    isbn.getText().trim().isEmpty() ? null : isbn.getText().trim(), 
                    judul.getText().trim(),
                    penulis.getText().trim().isEmpty() ? null : penulis.getText().trim(), 
                    penerbit.getText().trim().isEmpty() ? null : penerbit.getText().trim(),
                    tahunVal,
                    katId,
                    rakId,
                    stokTotal, 
                    stokTotal, 
                    coverPath,
                    description.getText().trim(),
                    data.get("book_id")
                );
                DB.audit(Long.valueOf(adminPage.idValue()), "UPDATE", "books", data.get("book_id"), "Edit buku");
                adminPage.showMessageDialog("Sukses", "Data buku berhasil diperbarui.");
            }
            refresh();
            dialog.dispose();
        } catch (NumberFormatException e) {
            adminPage.showErrorDialog("Error", "Format angka tidak valid.");
        } catch (Exception ex) {
            ex.printStackTrace();
            adminPage.showErrorDialog("Error", "Gagal menyimpan. Kode buku mungkin sudah digunakan.");
        }
    }
    
    void openKategoriManager() {
        ManagerDialog mg = new ManagerDialog(adminPage, "Kategori", "categories", "category_id", "name", adminPage.idValue());
        mg.open();
        refresh();
    }

    void openRakManager() {
        ManagerDialog mg = new ManagerDialog(adminPage, "Rak", "racks", "rack_id", "code", adminPage.idValue());
        mg.open();
        refresh();
    }

    private class ManagerDialog {
        AdminPage adminPage;
        String title;
        String table, idCol, nameCol;
        int actorId;
        DefaultTableModel m = new DefaultTableModel(new String[]{"ID","Nama"},0);
        JTable t = new JTable(m);

        ManagerDialog(AdminPage adminPage, String title, String table, String idCol, String nameCol, int actorId) {
            this.adminPage=adminPage; this.title=title; this.table=table; this.idCol=idCol; this.nameCol=nameCol; this.actorId=actorId;
        }

        void open() {
            JDialog d = new JDialog(adminPage, true);
            d.setTitle("Kelola " + title);
            d.setSize(500, 400);
            d.setLocationRelativeTo(adminPage);
            d.getContentPane().setBackground(Utils.BG);

            JPanel panel = new JPanel(new BorderLayout());
            panel.setBackground(Utils.BG);
            panel.setBorder(new EmptyBorder(20, 20, 20, 20));
            
            JLabel header = new JLabel("Kelola " + title);
            header.setForeground(Utils.TEXT);
            header.setFont(new Font("Segoe UI", Font.BOLD, 18));
            header.setBorder(new EmptyBorder(0, 0, 20, 0));
            
            Utils.styleTable(t);
            t.setRowHeight(35);
            JScrollPane sp = new JScrollPane(t);
            sp.getViewport().setBackground(Utils.CARD);
            sp.setBorder(BorderFactory.createLineBorder(Utils.BORDER));
            
            JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            actions.setOpaque(false);
            
            JButton add = adminPage.createPrimaryButton("Tambah");
            JButton edit = adminPage.createSecondaryButton("Edit");
            JButton del = adminPage.createSecondaryButton("Hapus");
            
            add.addActionListener(e -> form(d, null));
            edit.addActionListener(e -> {
                int r = t.getSelectedRow();
                if (r<0) { 
                    adminPage.showMessageDialog("Peringatan", "Pilih data terlebih dahulu."); 
                    return; 
                }
                Map<String, String> rowData = new HashMap<>();
                rowData.put("id", String.valueOf(m.getValueAt(r,0)));
                rowData.put("name", String.valueOf(m.getValueAt(r,1)));
                form(d, rowData);
            });
            del.addActionListener(e -> {
                int r = t.getSelectedRow();
                if (r<0) { 
                    adminPage.showMessageDialog("Peringatan", "Pilih data terlebih dahulu."); 
                    return; 
                }
                String id = String.valueOf(m.getValueAt(r,0));
                String name = String.valueOf(m.getValueAt(r,1));
                
                if (!adminPage.confirmDialog("Konfirmasi Hapus", 
                    "Apakah Anda yakin ingin menghapus " + title.toLowerCase() + ":\n" + 
                    name + "\n\n" +
                    "PERHATIAN: " + title + " yang digunakan oleh buku tidak dapat dihapus!")) return;
                
                try {
                    DB.exec("DELETE FROM "+table+" WHERE "+idCol+"=?", id);
                    DB.audit(Long.valueOf(actorId), "DELETE", table, id, "Hapus "+title);
                    refresh();
                    adminPage.showMessageDialog("Sukses", title + " berhasil dihapus.");
                } catch (Exception ex) { 
                    adminPage.showErrorDialog("Error", "Gagal hapus. " + title + " mungkin masih digunakan."); 
                }
            });

            actions.add(add); actions.add(edit); actions.add(del);
            
            panel.add(header, BorderLayout.NORTH);
            panel.add(sp, BorderLayout.CENTER);
            panel.add(actions, BorderLayout.SOUTH);

            d.setContentPane(panel);
            refresh();
            d.setVisible(true);
        }

        void refresh() {
            try {
                m.setRowCount(0);
                var rows = DB.query("SELECT "+idCol+" id, "+nameCol+" name FROM "+table+" ORDER BY "+nameCol);
                for (var r: rows) m.addRow(new Object[]{ r.get("id"), r.get("name") });
            } catch (Exception ignored) {}
        }

        void form(JDialog parent, Map<String,String> data) {
            boolean edit = data != null;
            String val = edit ? data.get("name") : "";
            
            String input = (String) JOptionPane.showInputDialog(parent, 
                (edit?"Edit":"Tambah")+" " + title + ":", 
                edit ? "Edit " + title : "Tambah " + title, 
                JOptionPane.QUESTION_MESSAGE, null, null, val);
            
            if (input == null) return;
            input = input.trim();
            if (input.isEmpty()) return;

            try {
                if (!edit) {
                    long idNew = DB.exec("INSERT INTO "+table+"("+nameCol+") VALUES (?)", input);
                    DB.audit(Long.valueOf(actorId), "CREATE", table, String.valueOf(idNew), "Tambah "+title);
                    adminPage.showMessageDialog("Sukses", title + " berhasil ditambahkan.");
                } else {
                    DB.exec("UPDATE "+table+" SET "+nameCol+"=? WHERE "+idCol+"=?", input, data.get("id"));
                    DB.audit(Long.valueOf(actorId), "UPDATE", table, data.get("id"), "Edit "+title);
                    adminPage.showMessageDialog("Sukses", title + " berhasil diperbarui.");
                }
                refresh();
            } catch (Exception ex) {
                adminPage.showErrorDialog("Error", "Gagal menyimpan. Nama mungkin sudah digunakan.");
            }
        }
    }
}
