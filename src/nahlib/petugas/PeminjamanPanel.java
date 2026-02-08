package nahlib.petugas;

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

public class PeminjamanPanel extends JPanel {
    private final PetugasPage petugasPage;
    
    // UI Components
    private JComboBox<Item> cbUser = new JComboBox<>();
    private JComboBox<Item> cbBook = new JComboBox<>();
    private JTextField qty = Utils.input(Lang.get("petugas.loan.qty").toLowerCase());
    
    private JLabel rulesInfo = new JLabel("");
    private JTextArea cartSummary;
    private JLabel coverPreview = new JLabel();
    private DefaultTableModel cart = new DefaultTableModel(new String[]{
        Lang.get("books.table.id"), Lang.get("books.table.isbn"), Lang.get("books.table.title"), 
        Lang.get("books.table.stock"), Lang.get("table.action")
    },0);
    private JTable table = new JTable(cart);

    // State
    private java.util.List<Item> originalUsers = new java.util.ArrayList<>();
    private java.util.List<Item> originalBooks = new java.util.ArrayList<>();

    public PeminjamanPanel(PetugasPage petugasPage) {
        this.petugasPage = petugasPage;
        setBackground(Utils.BG);
        setLayout(new BorderLayout());
        
        // Header
        JPanel header = createPanelHeader(Lang.get("petugas.loan.title"), 
            Lang.get("petugas.loan.subtitle"));
        
        // Main content
        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplit.setDividerLocation(450);
        mainSplit.setBorder(null);
        
        // Left panel: Form
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(Utils.BG);
        leftPanel.setBorder(new EmptyBorder(0, 20, 20, 10));
        
        JPanel formCard = Utils.card();
        formCard.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        gbc.insets = new Insets(0, 0, 15, 0);

        styleCombo(cbUser);
        styleCombo(cbBook);
        Utils.numericOnly(qty);

        setupSearchableCombo(cbUser, originalUsers);
        setupSearchableCombo(cbBook, originalBooks);

        gbc.gridy = 0;
        formCard.add(createInputGroup(Lang.get("petugas.loan.select_member"), cbUser), gbc);
        
        gbc.gridy = 1;
        formCard.add(createInputGroup(Lang.get("petugas.loan.select_book"), cbBook), gbc);
        
        gbc.gridy = 2;
        formCard.add(createInputGroup(Lang.get("petugas.loan.qty"), qty), gbc);
        
        // Preview
        gbc.gridy = 3;
        gbc.insets = new Insets(5, 0, 20, 0);
        coverPreview.setHorizontalAlignment(SwingConstants.CENTER);
        coverPreview.setPreferredSize(new Dimension(120, 180));
        coverPreview.setBorder(BorderFactory.createLineBorder(Utils.BORDER));
        JPanel coverWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        coverWrap.setOpaque(false);
        coverWrap.add(coverPreview);
        formCard.add(coverWrap, gbc);

        // Buttons
        gbc.gridy = 4;
        gbc.insets = new Insets(0, 0, 15, 0);
        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttonRow.setOpaque(false);
        JButton addCart = petugasPage.createPrimaryButton(Lang.get("btn.add"));
        JButton clearCart = petugasPage.createSecondaryButton(Lang.get("btn.reset"));
        addCart.addActionListener(e -> addToCart());
        clearCart.addActionListener(e -> clearCart());
        buttonRow.add(addCart);
        buttonRow.add(clearCart);
        formCard.add(buttonRow, gbc);

        leftPanel.add(formCard, BorderLayout.NORTH);

        // Cart Summary - Always visible at bottom of form
        cartSummary = new JTextArea();
        cartSummary.setEditable(false);
        cartSummary.setOpaque(false);
        cartSummary.setLineWrap(true);
        cartSummary.setWrapStyleWord(true);
        cartSummary.setForeground(Utils.TEXT);
        cartSummary.setFont(Utils.FONT_B);
        cartSummary.setBorder(new EmptyBorder(10, 5, 10, 5));
        leftPanel.add(cartSummary, BorderLayout.CENTER);

        // Book Selection Listener for preview
        cbBook.addActionListener(e -> updateBookPreview());
        
        // Right panel: Cart table
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(Utils.BG);
        rightPanel.setBorder(new EmptyBorder(0, 5, 20, 20));
        
        JPanel tableHeader = new JPanel(new BorderLayout());
        tableHeader.setBackground(Utils.BG);
        tableHeader.setBorder(new EmptyBorder(0, 0, 10, 0));
        
        JLabel tableTitle = new JLabel(Lang.get("petugas.loan.cart_title"));
        tableTitle.setForeground(Utils.TEXT);
        tableTitle.setFont(Utils.FONT_B);
        tableHeader.add(tableTitle, BorderLayout.WEST);
        
        styleTable();
        
        // Add Edit & Delete buttons in one cell
        nahlib.TableButton actionBar = new nahlib.TableButton();
        actionBar.addButton(Lang.get("btn.edit"), Utils.ACCENT, r -> editCartItem(r));
        actionBar.addButton(Lang.get("btn.delete"), new Color(0xEA, 0x43, 0x35), r -> removeCartItem(r));
        actionBar.install(table, 4);
        
        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.getViewport().setBackground(Utils.CARD);
        tableScroll.setBorder(BorderFactory.createLineBorder(Utils.BORDER));
        
        rightPanel.add(tableHeader, BorderLayout.NORTH);
        rightPanel.add(tableScroll, BorderLayout.CENTER);
        
        mainSplit.setLeftComponent(leftPanel);
        mainSplit.setRightComponent(rightPanel);
        
        // Bottom panel
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(Utils.BG);
        bottomPanel.setBorder(new EmptyBorder(0, 20, 20, 20));
        
        rulesInfo.setForeground(Utils.MUTED);
        rulesInfo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        JButton submitBtn = petugasPage.createPrimaryButton(Lang.get("petugas.quickaction.new_loan"));
        submitBtn.addActionListener(e -> submitLoan());
        
        bottomPanel.add(rulesInfo, BorderLayout.WEST);
        bottomPanel.add(submitBtn, BorderLayout.EAST);
        
        add(header, BorderLayout.NORTH);
        add(mainSplit, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
        
        refresh();
    }

    private boolean isItemSelected(JComboBox<Item> cb) {
        Object selected = cb.getSelectedItem();
        return (selected instanceof Item) && ((Item)selected).id != null;
    }

    private void setupSearchableCombo(JComboBox<Item> cb, List<Item> originalItems) {
        cb.setEditable(true);
        JTextField tf = (JTextField) cb.getEditor().getEditorComponent();
        
        // Handle "Enter" or manual input validation
        tf.addActionListener(e -> {
            String text = tf.getText().toLowerCase().trim();
            if (text.isEmpty()) return;
            
            boolean found = false;
            // 1. Try exact match on Search Key (Code/Username)
            for (Item it : originalItems) {
                if ((it.searchKey != null && it.searchKey.toLowerCase().equals(text)) || 
                    it.id.equals(text)) {
                    cb.setSelectedItem(it);
                    tf.setText(it.name); // Show full name
                    cb.hidePopup();
                    found = true;
                    break;
                }
            }
            
            // 2. Try match on Name if not found yet
            if (!found) {
                for (Item it : originalItems) {
                    if (it.name.toLowerCase().contains(text)) {
                        cb.setSelectedItem(it);
                        tf.setText(it.name);
                        cb.hidePopup();
                        found = true;
                        break;
                    }
                }
            }
            
            if (!found) {
               // Optional: Visual feedback for not found?
               // For now just keep text as is
            }
        });
    }

    private JPanel createInputGroup(String label, JComponent component) {
        JPanel group = new JPanel(new BorderLayout(0, 8));
        group.setOpaque(false);
        
        JLabel lbl = new JLabel(label);
        lbl.setForeground(Utils.TEXT);
        lbl.setFont(Utils.FONT_B);
        lbl.setHorizontalAlignment(SwingConstants.LEFT);
        
        group.add(lbl, BorderLayout.NORTH);
        group.add(component, BorderLayout.CENTER);
        return group;
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
    


    private void updateBookPreview() {
        Object selected = cbBook.getSelectedItem();
        if (!(selected instanceof Item)) return;
        Item b = (Item) selected;
        if (b.id == null) {
            coverPreview.setIcon(null);
            coverPreview.setText("No Preview");
            coverPreview.setForeground(Utils.MUTED);
            return;
        }

        try {
            List<Map<String, String>> res = DB.query("SELECT cover, description FROM books WHERE book_id = ?", Integer.parseInt(b.id));
            if (!res.isEmpty()) {
                String cover = res.get(0).get("cover");
                String description = res.get(0).get("description");
                
                if (cover != null && !cover.isEmpty()) {
                    ImageIcon icon = Utils.getCover(cover, 120, 180);
                    coverPreview.setIcon(icon);
                    coverPreview.setText("");
                } else {
                    coverPreview.setIcon(null);
                    coverPreview.setText("No Cover");
                }
                
                if (description != null && !description.isEmpty()) {
                    coverPreview.setToolTipText("<html><p width=\"300\">" + description + "</p></html>");
                } else {
                    coverPreview.setToolTipText(null);
                }
            } else {
                coverPreview.setIcon(null);
                coverPreview.setText("No Data");
            }
        } catch (Exception ex) {
            coverPreview.setIcon(null);
            coverPreview.setText("Error");
        }
    }
    




    private void styleCombo(JComboBox<?> cb) {
        cb.setBackground(Utils.BG);
        cb.setForeground(Utils.TEXT);
        cb.setBorder(BorderFactory.createLineBorder(Utils.BORDER));
        cb.setFocusable(false);
        cb.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                l.setBackground(isSelected ? Utils.ACCENT : Utils.CARD2);
                l.setForeground(Utils.TEXT);
                l.setBorder(new EmptyBorder(5, 10, 5, 10));
                return l;
            }
        });
        // Set the popup list background
        Object child = cb.getAccessibleContext().getAccessibleChild(0);
        if (child instanceof javax.swing.plaf.basic.BasicComboPopup) {
            ((javax.swing.plaf.basic.BasicComboPopup)child).getList().setBackground(Utils.CARD2);
        }
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
                setBorder(noFocusBorder);
                return c;
            }
        });
        
        // Adjust column widths
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0); // Hide ID
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.getColumnModel().getColumn(2).setPreferredWidth(250);
        table.getColumnModel().getColumn(3).setPreferredWidth(60);
        table.getColumnModel().getColumn(4).setPreferredWidth(180);
    }

    private void editCartItem(int row) {
        int modelRow = table.convertRowIndexToModel(row);
        String bookId = cart.getValueAt(modelRow, 0).toString();
        String title = cart.getValueAt(modelRow, 2).toString();
        String currentQty = cart.getValueAt(modelRow, 3).toString();
        
        String input = (String) JOptionPane.showInputDialog(petugasPage, 
            "Edit " + Lang.get("petugas.loan.qty") + " - " + title, 
            Lang.get("btn.edit"), JOptionPane.PLAIN_MESSAGE, null, null, currentQty);
            
        if (input != null && !input.trim().isEmpty()) {
            try {
                int newQty = Integer.parseInt(input.trim());
                if (newQty <= 0) {
                    petugasPage.showMessageDialog(Lang.get("msg.warning"), Lang.get("msg.info"));
                    return;
                }
                
                int avail = Integer.parseInt(DB.query(
                    "SELECT stok_tersedia FROM books WHERE book_id = ?", 
                    Integer.parseInt(bookId)
                ).get(0).get("stok_tersedia"));
                
                if (newQty > avail) {
                    petugasPage.showMessageDialog(Lang.get("msg.warning"), Lang.get("user.label.out_of_stock") + " (" + avail + ")");
                    return;
                }
                
                cart.setValueAt(String.valueOf(newQty), modelRow, 3);
                updateCartSummary();
            } catch (Exception e) {
                petugasPage.showMessageDialog(Lang.get("msg.error"), Lang.get("msg.error"));
            }
        }
    }
    
    private void removeCartItem(int row) {
        int modelRow = table.convertRowIndexToModel(row);
        if (petugasPage.confirmDialog(Lang.get("btn.delete"), Lang.get("msg.confirm_delete"))) {
            cart.removeRow(modelRow);
            updateCartSummary();
        }
    }

    public void refresh() {
        try {
            originalUsers.clear();
            originalBooks.clear();
            
            // Get active users
            List<Map<String, String>> users = DB.query(
                "SELECT user_id as id, username, CONCAT(nama_lengkap, ' (', username, ')') as name " +
                "FROM users WHERE role = 'USER' AND status_aktif = 1 ORDER BY nama_lengkap"
            );
            for (Map<String, String> r: users) {
                originalUsers.add(new Item(r.get("id"), r.get("name"), r.get("username")));
            }

            // Get available books
            List<Map<String, String>> books = DB.query(
                "SELECT book_id as id, code, CONCAT(code, ' - ', judul, ' [', stok_tersedia, ']') as name " +
                "FROM books WHERE stok_tersedia > 0 ORDER BY judul"
            );
            for (Map<String, String> r: books) {
                originalBooks.add(new Item(r.get("id"), r.get("name"), r.get("code")));
            }

            cbUser.removeAllItems();
            cbUser.addItem(new Item(null, "-- " + Lang.get("petugas.loan.select_member") + " --", null));
            for (Item it : originalUsers) cbUser.addItem(it);

            cbBook.removeAllItems();
            cbBook.addItem(new Item(null, "-- " + Lang.get("petugas.loan.select_book") + " --", null));
            for (Item it : originalBooks) cbBook.addItem(it);

            // Get rules
            Map<String, Integer> rule = DB.rules();
            rulesInfo.setText(String.format(Lang.get("petugas.loan.rules"), rule.get("max_days"), 
                rule.get("max_books"), rule.get("fine_per_day")));
            
            updateCartSummary();
        } catch (Exception e) {
            e.printStackTrace();
            petugasPage.showErrorDialog("Error", "Gagal memuat data: " + e.getMessage());
        }
    }
    
    private void updateCartSummary() {
        int totalItems = cart.getRowCount();
        int totalQty = 0;
        for (int i = 0; i < totalItems; i++) {
            totalQty += Integer.parseInt(cart.getValueAt(i, 3).toString());
        }
        cartSummary.setText(String.format(Lang.get("petugas.loan.cart_items"), totalItems, totalQty));
    }

    void addToCart() {
        if (!isItemSelected(cbUser)) {
            petugasPage.showMessageDialog(Lang.get("msg.warning"), Lang.get("msg.select_member"));
            return;
        }
        if (!isItemSelected(cbBook)) {
            petugasPage.showMessageDialog(Lang.get("msg.warning"), Lang.get("msg.select_book"));
            return;
        }
        Item b = (Item) cbBook.getSelectedItem();
        
        int q;
        try {
            q = qty.getText().trim().isEmpty() ? 1 : Integer.parseInt(qty.getText().trim());
            if (q <= 0) { 
                petugasPage.showMessageDialog(Lang.get("msg.warning"), Lang.get("msg.invalid_qty")); 
                return; 
            }
        } catch (NumberFormatException e) {
            petugasPage.showMessageDialog(Lang.get("msg.warning"), Lang.get("msg.invalid_qty"));
            return;
        }
        
        try {
            int avail = Integer.parseInt(DB.query(
                "SELECT stok_tersedia FROM books WHERE book_id = ?", 
                Integer.parseInt(b.id)
            ).get(0).get("stok_tersedia"));
            
            // Check if book already in cart
            for (int i = 0; i < cart.getRowCount(); i++) {
                if (cart.getValueAt(i, 0).toString().equals(b.id)) {
                    int old = Integer.parseInt(cart.getValueAt(i, 3).toString());
                    if (old + q > avail) {
                        petugasPage.showMessageDialog(Lang.get("msg.warning"), Lang.get("user.label.out_of_stock") + " (" + avail + ")");
                        return;
                    }
                    cart.setValueAt(String.valueOf(old + q), i, 3);
                    updateCartSummary();
                    return;
                }
            }
            
            if (q > avail) {
                petugasPage.showMessageDialog(Lang.get("msg.warning"), Lang.get("user.label.out_of_stock") + " (" + avail + ")");
                return;
            }
            
            Map<String, String> r = DB.query(
                "SELECT code, judul FROM books WHERE book_id = ?", 
                Integer.parseInt(b.id)
            ).get(0);
            cart.addRow(new Object[]{ b.id, r.get("code"), r.get("judul"), String.valueOf(q), null });
            updateCartSummary();
            
        } catch (Exception ex) {
            ex.printStackTrace();
            petugasPage.showErrorDialog("Error", "Gagal menambahkan ke keranjang: " + ex.getMessage());
        }
    }
    
    private void clearCart() {
        if (cart.getRowCount() == 0) {
            petugasPage.showMessageDialog(Lang.get("msg.info"), Lang.get("msg.no_data"));
            return;
        }
        
        if (petugasPage.confirmDialog(Lang.get("btn.delete"), Lang.get("msg.confirm_clear_cart"))) {
            cart.setRowCount(0);
            updateCartSummary();
            petugasPage.showMessageDialog(Lang.get("msg.success"), Lang.get("msg.success_delete"));
        }
    }

    void submitLoan() {
        if (!isItemSelected(cbUser)) {
            petugasPage.showMessageDialog(Lang.get("msg.warning"), Lang.get("msg.select_member"));
            return;
        }
        Item u = (Item) cbUser.getSelectedItem();
        
        if (cart.getRowCount() == 0) { 
            petugasPage.showMessageDialog(Lang.get("msg.warning"), Lang.get("msg.no_data")); 
            return; 
        }

        try {
            Map<String, Integer> rule = DB.rules();
            int maxDays = rule.get("max_days");
            int maxBooks = rule.get("max_books");

            int totalQty = 0;
            for (int i = 0; i < cart.getRowCount(); i++) {
                totalQty += Integer.parseInt(cart.getValueAt(i, 3).toString());
            }
            
            if (totalQty > maxBooks) {
                petugasPage.showErrorDialog(Lang.get("msg.error"), String.format(Lang.get("petugas.loan.rules"), maxDays, maxBooks, rule.get("fine_per_day")));
                return;
            }

            // Confirmation dialog with details
            StringBuilder details = new StringBuilder();
            details.append(Lang.get("role.user")).append(": ").append(u.name).append("\n");
            details.append(Lang.get("table.total")).append(": ").append(totalQty).append("\n");
            details.append(Lang.get("loan.table.duedate")).append(": ").append(maxDays).append(" ").append(Lang.get("nav.history").toLowerCase()).append("\n\n");
            details.append(Lang.get("petugas.return.book_list")).append(":\n");
            
            for (int i = 0; i < cart.getRowCount(); i++) {
                details.append("- ").append(cart.getValueAt(i, 2)).append(" (")
                      .append(cart.getValueAt(i, 3)).append(" buku)\n");
            }

            if (!petugasPage.confirmDialog(Lang.get("loan.confirm.title"), 
                String.format(Lang.get("petugas.loan.confirm_msg"), details.toString()))) return;

            DB.tx(() -> {
                // Check stock again before processing
                for (int i = 0; i < cart.getRowCount(); i++) {
                    String bookId = cart.getValueAt(i, 0).toString();
                    int q = Integer.parseInt(cart.getValueAt(i, 3).toString());
                    int avail = Integer.parseInt(DB.query(
                        "SELECT stok_tersedia FROM books WHERE book_id = ?", 
                        Integer.parseInt(bookId)
                    ).get(0).get("stok_tersedia"));
                    if (avail < q) {
                        throw new RuntimeException("Stok tidak cukup untuk " + cart.getValueAt(i, 2));
                    }
                }

                long loanId = DB.exec(
                    "INSERT INTO loans(user_id, petugas_id, tanggal_pinjam, jatuh_tempo, status) " +
                    "VALUES (?, ?, CURDATE(), DATE_ADD(CURDATE(), INTERVAL ? DAY), 'AKTIF')",
                    Integer.parseInt(u.id), Integer.parseInt(petugasPage.getMe().get("user_id")), maxDays
                );

                for (int i = 0; i < cart.getRowCount(); i++) {
                    String bookId = cart.getValueAt(i, 0).toString();
                    int q = Integer.parseInt(cart.getValueAt(i, 3).toString());

                    DB.exec(
                        "INSERT INTO loan_items(loan_id, book_id, qty) VALUES (?, ?, ?)", 
                        loanId, Integer.parseInt(bookId), q
                    );
                    DB.exec(
                        "UPDATE books SET stok_tersedia = stok_tersedia - ? WHERE book_id = ?", 
                        q, Integer.parseInt(bookId)
                    );
                }

                DB.audit(Long.parseLong(petugasPage.getMe().get("user_id")), "CREATE", "loans", 
                    String.valueOf(loanId), "Peminjaman baru untuk " + u.name);
            });

            petugasPage.showMessageDialog(Lang.get("msg.success"), Lang.get("loan.msg.success"));
            
            cart.setRowCount(0);
            updateCartSummary();
            refresh();

        } catch (RuntimeException ex) {
            petugasPage.showErrorDialog(Lang.get("msg.error"), ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            petugasPage.showErrorDialog(Lang.get("msg.error"), Lang.get("msg.error") + ": " + ex.getMessage());
        }
    }

    static class Item {
        String id, name, searchKey;
        Item(String id, String name, String searchKey) { 
            this.id=id; 
            this.name=name; 
            this.searchKey=searchKey;
        }
        public String toString() { return name; }
    }
}
