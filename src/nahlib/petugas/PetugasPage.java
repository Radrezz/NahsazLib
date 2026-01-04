package nahlib.petugas;

import nahlib.DB;
import nahlib.LoginPage;
import nahlib.Utils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.*;
import java.util.List;

public class PetugasPage extends JFrame {

    private final Map<String,String> me;
    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);
    private final Map<String, JPanel> panels = new HashMap<>();
    private final JPanel navPanel;
    private final JButton[] navButtons;

    public PetugasPage(Map<String,String> me) {
        this.me = me;

        setTitle(Utils.getLibraryName() + " - Petugas Dashboard");
        setSize(1280, 780);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Utils.BG);

        // =========== TOP BAR ===========
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(Utils.BG);
        top.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, Utils.BORDER),
            new EmptyBorder(16, 20, 16, 20)
        ));

        // Left side: Logo + Title
        JPanel leftTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftTop.setOpaque(false);
        
        JLabel title = new JLabel(Utils.getLibraryName());
        title.setForeground(Utils.ACCENT);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        
        JLabel subtitle = new JLabel(" • Petugas Dashboard");
        subtitle.setForeground(Utils.TEXT);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        leftTop.add(title);
        leftTop.add(subtitle);

        // Right side: User info + Logout
        JPanel rightTop = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightTop.setOpaque(false);
        
        JPanel userCard = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        userCard.setBackground(Utils.CARD);
        userCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Utils.BORDER),
            new EmptyBorder(6, 12, 6, 12)
        ));
        
        
        JLabel who = new JLabel(me.get("nama_lengkap"));
        who.setForeground(Utils.TEXT);
        who.setFont(Utils.FONT_B);
        
        JLabel role = new JLabel("PETUGAS");
        role.setForeground(new Color(52, 168, 83)); // Green for petugas
        role.setFont(new Font("Segoe UI", Font.BOLD, 10));
        role.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(52, 168, 83)),
            new EmptyBorder(2, 6, 2, 6)
        ));
        
        userCard.add(who);
        userCard.add(role);
        
        JButton logout = new JButton("Logout");
        logout.setFont(Utils.FONT_B);
        logout.setForeground(Utils.TEXT);
        logout.setBackground(Utils.CARD);
        logout.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Utils.BORDER),
            new EmptyBorder(8, 16, 8, 16)
        ));
        logout.setFocusPainted(false);
        logout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logout.addActionListener(e -> {
            if (confirmDialog("Keluar", "Apakah Anda yakin ingin logout?")) {
                DB.audit(Long.parseLong(me.get("user_id")), "LOGOUT", "users", me.get("user_id"), "Logout");
                dispose();
                new LoginPage();
            }
        });
        
        logout.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                logout.setBackground(Utils.CARD2);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                logout.setBackground(Utils.CARD);
            }
        });

        rightTop.add(userCard);
        rightTop.add(logout);

        top.add(leftTop, BorderLayout.WEST);
        top.add(rightTop, BorderLayout.EAST);

        // =========== CONTENT PANELS ===========
        content.setBackground(Utils.BG);
        
        DashboardPanel dash = new DashboardPanel();
        PeminjamanPanel pinjam = new PeminjamanPanel();
        PengembalianPanel kembali = new PengembalianPanel();
        LaporanSayaPanel laporan = new LaporanSayaPanel();
        NotifikasiPanel notif = new NotifikasiPanel();
        
        panels.put("dash", dash);
        panels.put("pinjam", pinjam);
        panels.put("kembali", kembali);
        panels.put("laporan", laporan);
        panels.put("notif", notif);
        
        content.add(dash, "dash");
        content.add(pinjam, "pinjam");
        content.add(kembali, "kembali");
        content.add(laporan, "laporan");
        content.add(notif, "notif");

        // =========== BOTTOM NAVIGATION ===========
        String[] labels = new String[]{" Dashboard", " Peminjaman", "Pengembalian", "Laporan Saya", "Notifikasi"};
        navButtons = new JButton[labels.length];
        navPanel = createBottomNav(labels, 0);

        root.add(top, BorderLayout.NORTH);
        root.add(content, BorderLayout.CENTER);
        root.add(navPanel, BorderLayout.SOUTH);

        setContentPane(root);
        dash.refresh();
        setVisible(true);
    }

    private JPanel createBottomNav(String[] labels, int activeIndex) {
        JPanel bar = new JPanel(new GridLayout(1, labels.length, 1, 0));
        bar.setBackground(Utils.BG);
        bar.setBorder(new EmptyBorder(0, 0, 0, 0));
        
        for (int i = 0; i < labels.length; i++) {
            JButton btn = createNavButton(labels[i], i == activeIndex);
            navButtons[i] = btn;
            final int idx = i;
            btn.addActionListener(e -> {
                switchPanel(idx);
            });
            bar.add(btn);
        }
        return bar;
    }
    
    private JButton createNavButton(String text, boolean active) {
        JButton btn = new JButton(text);
        btn.setFont(Utils.FONT);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        if (active) {
            btn.setBackground(Utils.ACCENT);
            btn.setForeground(Color.WHITE);
            btn.setBorder(BorderFactory.createEmptyBorder(14, 0, 14, 0));
        } else {
            btn.setBackground(Utils.CARD);
            btn.setForeground(Utils.TEXT);
            btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 1, Utils.BORDER),
                new EmptyBorder(14, 0, 14, 0)
            ));
            
            btn.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    btn.setBackground(Utils.CARD2);
                }
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    btn.setBackground(Utils.CARD);
                }
            });
        }
        
        return btn;
    }
    
    private void switchPanel(int index) {
        String[] panelKeys = {"dash", "pinjam", "kembali", "laporan", "notif"};
        String key = panelKeys[index];
        
        // Update button highlights
        for (int i = 0; i < navButtons.length; i++) {
            JButton btn = navButtons[i];
            if (i == index) {
                btn.setBackground(Utils.ACCENT);
                btn.setForeground(Color.WHITE);
            } else {
                btn.setBackground(Utils.CARD);
                btn.setForeground(Utils.TEXT);
            }
        }
        
        // Show panel and refresh
        cards.show(content, key);
        
        switch (key) {
            case "dash":
                ((DashboardPanel)panels.get(key)).refresh();
                break;
            case "pinjam":
                ((PeminjamanPanel)panels.get(key)).refresh();
                break;
            case "kembali":
                ((PengembalianPanel)panels.get(key)).refresh();
                break;
            case "laporan":
                ((LaporanSayaPanel)panels.get(key)).refresh();
                break;
            case "notif":
                ((NotifikasiPanel)panels.get(key)).refresh();
                break;
        }
        
        // Update notification badge
        if (key.equals("notif")) {
            int overdue = ((NotifikasiPanel)panels.get("notif")).countOverdue();
            updateNavBadge(4, overdue);
        }
    }
    
    private void updateNavBadge(int index, int count) {
        if (navButtons[index] != null) {
            String text = "Notifikasi";
            if (count > 0) {
                text = "Notifikasi (" + count + ")";
            }
            navButtons[index].setText(text);
        }
    }
    
    // Helper methods
    private JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(Utils.FONT_B);
        btn.setForeground(Color.WHITE);
        btn.setBackground(Utils.ACCENT);
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
    
    private JButton createSecondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(Utils.FONT);
        btn.setForeground(Utils.TEXT);
        btn.setBackground(Utils.CARD);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Utils.BORDER),
            new EmptyBorder(8, 16, 8, 16)
        ));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(Utils.CARD2);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(Utils.CARD);
            }
        });
        
        return btn;
    }
    
    private boolean confirmDialog(String title, String message) {
        int result = JOptionPane.showConfirmDialog(this, message, title, 
            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        return result == JOptionPane.YES_OPTION;
    }
    
    private void showMessageDialog(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void showErrorDialog(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }

    // ========================= PANELS =========================

    class DashboardPanel extends JPanel {
        private final JLabel todayTx = createStatLabel("0");
        private final JLabel myMonth = createStatLabel("0");
        private final JLabel active = createStatLabel("0");
        private final JLabel overdueToday = createStatLabel("0");

        DashboardPanel() {
            setBackground(Utils.BG);
            setLayout(new BorderLayout());
            
            // Header
            JPanel header = new JPanel(new BorderLayout());
            header.setBackground(Utils.BG);
            header.setBorder(new EmptyBorder(20, 20, 20, 20));
            
            JLabel title = new JLabel("Dashboard Petugas");
            title.setForeground(Utils.TEXT);
            title.setFont(new Font("Segoe UI", Font.BOLD, 24));
            
            JLabel subtitle = new JLabel("Ringkasan aktivitas Anda sebagai petugas");
            subtitle.setForeground(Utils.MUTED);
            subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            
            JPanel titlePanel = new JPanel(new BorderLayout());
            titlePanel.setOpaque(false);
            titlePanel.add(title, BorderLayout.NORTH);
            titlePanel.add(subtitle, BorderLayout.SOUTH);
            
            header.add(titlePanel, BorderLayout.WEST);
            add(header, BorderLayout.NORTH);

            // Stats Grid
            JPanel grid = new JPanel(new GridLayout(2, 2, 16, 16));
            grid.setBackground(Utils.BG);
            grid.setBorder(new EmptyBorder(0, 20, 20, 20));
            
            grid.add(createStatCard("Transaksi Hari Ini", todayTx, new Color(66, 133, 244)));
            grid.add(createStatCard("Transaksi Bulan Ini", myMonth, new Color(66, 133, 244)));
            grid.add(createStatCard("Peminjaman Aktif", active, new Color(251, 188, 5)));
            grid.add(createStatCard("Terlambat (Global)", overdueToday, new Color(251, 188, 5)));
            
            add(grid, BorderLayout.CENTER);
            
            // Quick Actions Panel
            add(createQuickActionsPanel(), BorderLayout.SOUTH);
        }
        
        private JLabel createStatLabel(String text) {
            JLabel label = new JLabel(text, SwingConstants.CENTER);
            label.setForeground(Utils.TEXT);
            label.setFont(new Font("Segoe UI", Font.BOLD, 36));
            return label;
        }
        
        private JPanel createStatCard(String title, JLabel value, Color accentColor) {
            JPanel card = new JPanel(new BorderLayout());
            card.setBackground(Utils.CARD);
            card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Utils.BORDER),
                new EmptyBorder(20, 20, 20, 20)
            ));
            
            JPanel accentBar = new JPanel();
            accentBar.setBackground(accentColor);
            accentBar.setPreferredSize(new Dimension(card.getWidth(), 4));
            card.add(accentBar, BorderLayout.NORTH);
            
            JPanel content = new JPanel(new BorderLayout(0, 10));
            content.setOpaque(false);
            content.add(value, BorderLayout.CENTER);
            
            JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
            titleLabel.setForeground(Utils.MUTED);
            titleLabel.setFont(Utils.FONT);
            content.add(titleLabel, BorderLayout.SOUTH);
            
            card.add(content, BorderLayout.CENTER);
            return card;
        }
        
        private JPanel createQuickActionsPanel() {
            JPanel panel = new JPanel(new BorderLayout());
            panel.setBackground(Utils.BG);
            panel.setBorder(new EmptyBorder(0, 20, 20, 20));
            
            JLabel title = new JLabel("Aksi Cepat");
            title.setForeground(Utils.TEXT);
            title.setFont(new Font("Segoe UI", Font.BOLD, 16));
            title.setBorder(new EmptyBorder(0, 0, 10, 0));
            
            JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
            actions.setBackground(Utils.BG);
            
            JButton newLoan = createSecondaryButton("Buat Peminjaman Baru");
            JButton processReturn = createSecondaryButton("Proses Pengembalian");
            JButton viewReport = createSecondaryButton(" Lihat Laporan");
            JButton checkOverdue = createSecondaryButton("Cek Keterlambatan");
            
            newLoan.addActionListener(e -> switchPanel(1));
            processReturn.addActionListener(e -> switchPanel(2));
            viewReport.addActionListener(e -> switchPanel(3));
            checkOverdue.addActionListener(e -> switchPanel(4));
            
            actions.add(newLoan);
            actions.add(processReturn);
            actions.add(viewReport);
            actions.add(checkOverdue);
            
            panel.add(title, BorderLayout.NORTH);
            panel.add(actions, BorderLayout.CENTER);
            
            return panel;
        }

        void refresh() {
            try {
                String userId = me.get("user_id");
                
                // Transaksi hari ini
                int t = Integer.parseInt(DB.query(
                    "SELECT COUNT(*) as c FROM loans WHERE petugas_id = ? AND tanggal_pinjam = CURDATE()",
                    Integer.parseInt(userId)
                ).get(0).get("c"));
                
                // Transaksi bulan ini
                int m = Integer.parseInt(DB.query(
                    "SELECT COUNT(*) as c FROM loans WHERE petugas_id = ? AND MONTH(tanggal_pinjam) = MONTH(CURDATE()) AND YEAR(tanggal_pinjam) = YEAR(CURDATE())",
                    Integer.parseInt(userId)
                ).get(0).get("c"));
                
                // Peminjaman aktif
                int a = Integer.parseInt(DB.query(
                    "SELECT COUNT(*) as c FROM loans WHERE petugas_id = ? AND status = 'AKTIF'",
                    Integer.parseInt(userId)
                ).get(0).get("c"));
                
                // Terlambat global
                int o = Integer.parseInt(DB.query(
                    "SELECT COUNT(*) as c FROM loans WHERE status = 'AKTIF' AND jatuh_tempo < CURDATE()"
                ).get(0).get("c"));

                todayTx.setText(String.valueOf(t));
                myMonth.setText(String.valueOf(m));
                active.setText(String.valueOf(a));
                overdueToday.setText(String.valueOf(o));

                if (o > 0) {
                    showMessageDialog("Peringatan", "Ada " + o + " transaksi terlambat (global). Cek halaman Notifikasi.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class PeminjamanPanel extends JPanel {
        private JComboBox<Item> cbUser = new JComboBox<>();
        private JComboBox<Item> cbBook = new JComboBox<>();
        private JTextField qty = Utils.input("qty (angka)");
        private DefaultTableModel cart = new DefaultTableModel(new String[]{"BookID","Kode","Judul","Qty"},0);
        private JTable table = new JTable(cart);
        private JLabel rulesInfo = new JLabel("");
        private JLabel cartSummary;

        PeminjamanPanel() {
            setBackground(Utils.BG);
            setLayout(new BorderLayout());
            
            // Header
            JPanel header = createPanelHeader("Peminjaman Buku", 
                "Proses peminjaman buku untuk anggota (multi-buku)");
            
            // Main content with split pane
            JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
            mainSplit.setDividerLocation(500);
            mainSplit.setBorder(null);
            
            // Left panel: Form
            JPanel leftPanel = new JPanel(new BorderLayout());
            leftPanel.setBackground(Utils.BG);
            leftPanel.setBorder(new EmptyBorder(0, 20, 20, 10));
            
            JPanel formCard = Utils.card();
            formCard.setLayout(new GridLayout(4, 1, 10, 15));
            
            styleCombo(cbUser);
            styleCombo(cbBook);
            Utils.numericOnly(qty);
            
            formCard.add(createFormRow("Pilih Anggota", cbUser));
            formCard.add(createFormRow("Pilih Buku", cbBook));
            formCard.add(createFormRow("Jumlah (Qty)", qty));
            
            JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            buttonRow.setOpaque(false);
            JButton addCart = createPrimaryButton("Tambah ke Keranjang");
            JButton clearCart = createSecondaryButton("Kosongkan");
            addCart.addActionListener(e -> addToCart());
            clearCart.addActionListener(e -> clearCart());
            buttonRow.add(addCart);
            buttonRow.add(clearCart);
            formCard.add(buttonRow);
            
            leftPanel.add(formCard, BorderLayout.NORTH);
            
            // Cart summary
            cartSummary = new JLabel("Keranjang: 0 item");
            cartSummary.setForeground(Utils.MUTED);
            cartSummary.setFont(Utils.FONT);
            cartSummary.setBorder(new EmptyBorder(10, 0, 0, 0));
            leftPanel.add(cartSummary, BorderLayout.SOUTH);
            
            // Right panel: Cart table
            JPanel rightPanel = new JPanel(new BorderLayout());
            rightPanel.setBackground(Utils.BG);
            rightPanel.setBorder(new EmptyBorder(0, 10, 20, 20));
            
            JPanel tableHeader = new JPanel(new BorderLayout());
            tableHeader.setBackground(Utils.BG);
            tableHeader.setBorder(new EmptyBorder(0, 0, 10, 0));
            
            JLabel tableTitle = new JLabel("Keranjang Peminjaman");
            tableTitle.setForeground(Utils.TEXT);
            tableTitle.setFont(Utils.FONT_B);
            tableHeader.add(tableTitle, BorderLayout.WEST);
            
            // Style table
            styleTable();
            JScrollPane scroll = new JScrollPane(table);
            scroll.getViewport().setBackground(Utils.CARD);
            scroll.setBorder(BorderFactory.createLineBorder(Utils.BORDER));
            
            rightPanel.add(tableHeader, BorderLayout.NORTH);
            rightPanel.add(scroll, BorderLayout.CENTER);
            
            mainSplit.setLeftComponent(leftPanel);
            mainSplit.setRightComponent(rightPanel);
            
            // Bottom panel: Rules info and submit button
            JPanel bottomPanel = new JPanel(new BorderLayout());
            bottomPanel.setBackground(Utils.BG);
            bottomPanel.setBorder(new EmptyBorder(0, 20, 20, 20));
            
            rulesInfo.setForeground(Utils.MUTED);
            rulesInfo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            
            JButton submitBtn = createPrimaryButton("Proses Peminjaman");
            submitBtn.addActionListener(e -> submitLoan());
            
            bottomPanel.add(rulesInfo, BorderLayout.WEST);
            bottomPanel.add(submitBtn, BorderLayout.EAST);
            
            add(header, BorderLayout.NORTH);
            add(mainSplit, BorderLayout.CENTER);
            add(bottomPanel, BorderLayout.SOUTH);
            
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
        }

        void refresh() {
            try {
                cbUser.removeAllItems();
                cbBook.removeAllItems();
                cbUser.addItem(new Item(null, "-- Pilih Anggota --"));
                cbBook.addItem(new Item(null, "-- Pilih Buku --"));

                // Get active users
                List<Map<String, String>> users = DB.query(
                    "SELECT user_id as id, CONCAT(nama_lengkap, ' (', username, ')') as name " +
                    "FROM users WHERE role = 'USER' AND status_aktif = 1 ORDER BY nama_lengkap"
                );
                for (Map<String, String> r: users) {
                    cbUser.addItem(new Item(r.get("id"), r.get("name")));
                }

                // Get available books
                List<Map<String, String>> books = DB.query(
                    "SELECT book_id as id, CONCAT(code, ' - ', judul, ' [', stok_tersedia, ']') as name " +
                    "FROM books WHERE stok_tersedia > 0 ORDER BY judul"
                );
                for (Map<String, String> r: books) {
                    cbBook.addItem(new Item(r.get("id"), r.get("name")));
                }

                // Get rules
                Map<String, Integer> rule = DB.rules();
                rulesInfo.setText("Rules: maks " + rule.get("max_days") + " hari, maks " + 
                    rule.get("max_books") + " buku, denda Rp " + rule.get("fine_per_day") + "/hari");
                
                updateCartSummary();
            } catch (Exception e) {
                e.printStackTrace();
                showErrorDialog("Error", "Gagal memuat data: " + e.getMessage());
            }
        }
        
        private void updateCartSummary() {
            int totalItems = cart.getRowCount();
            int totalQty = 0;
            for (int i = 0; i < totalItems; i++) {
                totalQty += Integer.parseInt(cart.getValueAt(i, 3).toString());
            }
            cartSummary.setText("Keranjang: " + totalItems + " item (" + totalQty + " buku)");
        }

        void addToCart() {
            Item b = (Item) cbBook.getSelectedItem();
            if (b == null || b.id == null) { 
                showMessageDialog("Peringatan", "Pilih buku terlebih dahulu."); 
                return; 
            }
            
            int q;
            try {
                q = qty.getText().trim().isEmpty() ? 1 : Integer.parseInt(qty.getText().trim());
                if (q <= 0) { 
                    showMessageDialog("Peringatan", "Jumlah minimal 1."); 
                    return; 
                }
            } catch (NumberFormatException e) {
                showMessageDialog("Peringatan", "Jumlah harus berupa angka.");
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
                            showMessageDialog("Peringatan", "Stok tidak cukup. Stok tersedia: " + avail);
                            return;
                        }
                        cart.setValueAt(String.valueOf(old + q), i, 3);
                        updateCartSummary();
                        return;
                    }
                }
                
                if (q > avail) {
                    showMessageDialog("Peringatan", "Stok tidak cukup. Stok tersedia: " + avail);
                    return;
                }
                
                Map<String, String> r = DB.query(
                    "SELECT code, judul FROM books WHERE book_id = ?", 
                    Integer.parseInt(b.id)
                ).get(0);
                cart.addRow(new Object[]{ b.id, r.get("code"), r.get("judul"), String.valueOf(q) });
                updateCartSummary();
                
            } catch (Exception ex) {
                ex.printStackTrace();
                showErrorDialog("Error", "Gagal menambahkan ke keranjang: " + ex.getMessage());
            }
        }
        
        private void clearCart() {
            if (cart.getRowCount() == 0) {
                showMessageDialog("Info", "Keranjang sudah kosong.");
                return;
            }
            
            if (confirmDialog("Konfirmasi", "Apakah Anda yakin ingin mengosongkan keranjang?")) {
                cart.setRowCount(0);
                updateCartSummary();
                showMessageDialog("Sukses", "Keranjang berhasil dikosongkan.");
            }
        }

        void submitLoan() {
            Item u = (Item) cbUser.getSelectedItem();
            if (u == null || u.id == null) { 
                showMessageDialog("Peringatan", "Pilih anggota terlebih dahulu."); 
                return; 
            }
            
            if (cart.getRowCount() == 0) { 
                showMessageDialog("Peringatan", "Keranjang kosong. Tambahkan buku terlebih dahulu."); 
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
                    showErrorDialog("Error", "Melebihi batas maksimum buku per transaksi (" + maxBooks + ").");
                    return;
                }

                // Confirmation dialog with details
                StringBuilder details = new StringBuilder();
                details.append("Anggota: ").append(u.name).append("\n");
                details.append("Jumlah buku: ").append(totalQty).append("\n");
                details.append("Jatuh tempo: ").append(maxDays).append(" hari\n\n");
                details.append("Detail buku:\n");
                
                for (int i = 0; i < cart.getRowCount(); i++) {
                    details.append("- ").append(cart.getValueAt(i, 2)).append(" (")
                          .append(cart.getValueAt(i, 3)).append(" buku)\n");
                }

                if (!confirmDialog("Konfirmasi Peminjaman", 
                    "Proses peminjaman untuk:\n\n" + details.toString() + 
                    "\nApakah data sudah benar?")) return;

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
                        Integer.parseInt(u.id), Integer.parseInt(me.get("user_id")), maxDays
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

                    DB.audit(Long.parseLong(me.get("user_id")), "CREATE", "loans", 
                        String.valueOf(loanId), "Peminjaman baru untuk " + u.name);
                });

                showMessageDialog("Sukses", "Peminjaman berhasil diproses.");
                
                cart.setRowCount(0);
                updateCartSummary();
                refresh();

            } catch (RuntimeException ex) {
                showErrorDialog("Error", ex.getMessage());
            } catch (Exception ex) {
                ex.printStackTrace();
                showErrorDialog("Error", "Gagal memproses peminjaman: " + ex.getMessage());
            }
        }

        class Item {
            String id, name;
            Item(String id, String name) { this.id=id; this.name=name; }
            public String toString() { return name; }
        }
    }

    class PengembalianPanel extends JPanel {
        private DefaultTableModel model = new DefaultTableModel(new String[]{"LoanID","Anggota","Tanggal","Jatuh Tempo","Telat (hari)","Total Item"},0);
        private JTable table = new JTable(model);
        private JLabel detail = new JLabel("Pilih transaksi untuk melihat detail & proses pengembalian.");
        private JTextArea items = new JTextArea();
        private JButton btnProcess;

        PengembalianPanel() {
            setBackground(Utils.BG);
            setLayout(new BorderLayout());
            
            // Header
            JPanel header = createPanelHeader("Pengembalian Buku", 
                "Proses pengembalian dan perhitungan denda otomatis");
            
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
            
            items.setBackground(Utils.CARD2);
            items.setForeground(Utils.TEXT);
            items.setEditable(false);
            items.setFont(new Font("Monospaced", Font.PLAIN, 12));
            items.setBorder(BorderFactory.createLineBorder(Utils.BORDER));
            
            btnProcess = createPrimaryButton("Proses Pengembalian");
            btnProcess.setEnabled(false);
            btnProcess.addActionListener(e -> processReturn());
            
            detailsCard.add(detail, BorderLayout.NORTH);
            detailsCard.add(new JScrollPane(items), BorderLayout.CENTER);
            detailsCard.add(btnProcess, BorderLayout.SOUTH);
            
            rightPanel.add(detailsCard, BorderLayout.CENTER);
            
            split.setLeftComponent(leftPanel);
            split.setRightComponent(rightPanel);
            
            // Bottom panel
            JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            bottomPanel.setBackground(Utils.BG);
            bottomPanel.setBorder(new EmptyBorder(0, 20, 20, 20));
            
            JButton refreshBtn = createSecondaryButton("Refresh List");
            refreshBtn.addActionListener(e -> refresh());
            
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

        void refresh() {
            try {
                model.setRowCount(0);
                List<Map<String, String>> rows = DB.query(
                    "SELECT l.loan_id, u.nama_lengkap as anggota, l.tanggal_pinjam, l.jatuh_tempo, " +
                    "GREATEST(DATEDIFF(CURDATE(), l.jatuh_tempo), 0) as telat, " +
                    "(SELECT SUM(qty) FROM loan_items li WHERE li.loan_id = l.loan_id) as total_item " +
                    "FROM loans l JOIN users u ON l.user_id = u.user_id " +
                    "WHERE l.status = 'AKTIF' AND l.petugas_id = ? " +
                    "ORDER BY l.loan_id DESC",
                    Integer.parseInt(me.get("user_id"))
                );
                
                for (Map<String, String> r: rows) {
                    model.addRow(new Object[]{
                        r.get("loan_id"), r.get("anggota"), r.get("tanggal_pinjam"),
                        r.get("jatuh_tempo"), r.get("telat"), r.get("total_item")
                    });
                }
                
                items.setText("");
                detail.setText("Pilih transaksi untuk melihat detail & proses pengembalian.");
            } catch (Exception e) {
                e.printStackTrace();
                showErrorDialog("Error", "Gagal memuat data: " + e.getMessage());
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
                    "SELECT b.code, b.judul, li.qty FROM loan_items li JOIN books b ON li.book_id = b.book_id WHERE li.loan_id = ?",
                    Integer.parseInt(loanId)
                );
                
                int fine = telat * finePerDay;

                StringBuilder sb = new StringBuilder();
                sb.append("=== Detail Transaksi ===\n");
                sb.append("Loan ID: ").append(loanId).append("\n");
                sb.append("Anggota: ").append(anggota).append("\n");
                sb.append("Telat: ").append(telat).append(" hari\n");
                sb.append("Denda/hari: Rp ").append(finePerDay).append("\n");
                sb.append("Total denda: Rp ").append(fine).append("\n\n");
                sb.append("=== Daftar Buku ===\n");
                
                for (Map<String, String> it: itemsRows) {
                    sb.append("• ").append(it.get("code")).append(" - ")
                      .append(it.get("judul")).append(" (")
                      .append(it.get("qty")).append(" buku)\n");
                }
                
                if (telat > 0) {
                    sb.append("\n⚠️ PERHATIAN: Transaksi terlambat!");
                }

                detail.setText("Detail Transaksi: " + loanId + " - " + anggota);
                items.setText(sb.toString());

            } catch (Exception e) {
                e.printStackTrace();
                showErrorDialog("Error", "Gagal memuat detail: " + e.getMessage());
            }
        }

        void processReturn() {
            int r = table.getSelectedRow();
            if (r < 0) { 
                showMessageDialog("Peringatan", "Pilih transaksi terlebih dahulu."); 
                return; 
            }
            
            String loanId = model.getValueAt(r, 0).toString();
            String anggota = model.getValueAt(r, 1).toString();
            int telat = Integer.parseInt(model.getValueAt(r, 4).toString());

            try {
                Map<String, Integer> rule = DB.rules();
                int fine = telat * rule.get("fine_per_day");
                
                String message = "Konfirmasi pengembalian:\n\n" +
                               "Loan ID: " + loanId + "\n" +
                               "Anggota: " + anggota + "\n" +
                               "Keterlambatan: " + telat + " hari\n" +
                               "Total denda: Rp " + fine + "\n\n" +
                               "Apakah Anda yakin ingin memproses pengembalian ini?";
                
                if (!confirmDialog("Konfirmasi Pengembalian", message)) return;

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

                    DB.audit(Long.parseLong(me.get("user_id")), "UPDATE", "loans", loanId, 
                        "Pengembalian selesai untuk " + anggota);
                });

                showMessageDialog("Sukses", "Pengembalian berhasil diproses.\n" +
                    (telat > 0 ? "Denda: Rp " + fine : "Tidak ada denda."));
                
                refresh();

            } catch (Exception ex) {
                ex.printStackTrace();
                showErrorDialog("Error", "Gagal memproses pengembalian: " + ex.getMessage());
            }
        }
    }

    class LaporanSayaPanel extends JPanel {
        private DefaultTableModel model = new DefaultTableModel(new String[]{"LoanID","Tanggal","Jatuh Tempo","Anggota","Status","Total Item"},0);
        private JTable table = new JTable(model);
        private JTextField from = Utils.input("YYYY-MM-DD");
        private JTextField to = Utils.input("YYYY-MM-DD");

        LaporanSayaPanel() {
            setBackground(Utils.BG);
            setLayout(new BorderLayout());
            
            // Header
            JPanel header = createPanelHeader("Laporan Saya", 
                "Riwayat transaksi yang Anda proses");
            
            // Filter panel
            JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            filterPanel.setBackground(Utils.BG);
            filterPanel.setBorder(new EmptyBorder(0, 20, 10, 20));
            
            filterPanel.add(new JLabel("Dari:"));
            filterPanel.add(from);
            filterPanel.add(new JLabel("Sampai:"));
            filterPanel.add(to);
            
            JButton filterBtn = createSecondaryButton("Filter");
            JButton clearBtn = createSecondaryButton("Clear");
            
            filterBtn.addActionListener(e -> refresh());
            clearBtn.addActionListener(e -> { 
                from.setText(""); 
                to.setText(""); 
                refresh(); 
            });
            
            filterPanel.add(filterBtn);
            filterPanel.add(clearBtn);
            
            // Table
            styleTable();
            JScrollPane scroll = new JScrollPane(table);
            scroll.getViewport().setBackground(Utils.CARD);
            scroll.setBorder(new EmptyBorder(0, 20, 20, 20));
            
            // Summary panel
            JPanel summaryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
            summaryPanel.setBackground(Utils.BG);
            summaryPanel.setBorder(new EmptyBorder(0, 20, 20, 20));
            
            JLabel totalLabel = new JLabel("Total: 0 transaksi");
            totalLabel.setForeground(Utils.TEXT);
            totalLabel.setFont(Utils.FONT_B);
            
            JLabel activeLabel = new JLabel("Aktif: 0");
            activeLabel.setForeground(new Color(66, 133, 244));
            activeLabel.setFont(Utils.FONT_B);
            
            JLabel completedLabel = new JLabel("Selesai: 0");
            completedLabel.setForeground(new Color(52, 168, 83));
            completedLabel.setFont(Utils.FONT_B);
            
            JLabel cancelledLabel = new JLabel("Batal: 0");
            cancelledLabel.setForeground(new Color(234, 67, 53));
            cancelledLabel.setFont(Utils.FONT_B);
            
            summaryPanel.add(totalLabel);
            summaryPanel.add(activeLabel);
            summaryPanel.add(completedLabel);
            summaryPanel.add(cancelledLabel);
            
            add(header, BorderLayout.NORTH);
            add(filterPanel, BorderLayout.CENTER);
            add(scroll, BorderLayout.CENTER);
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

        void refresh() {
            try {
                model.setRowCount(0);
                String f = from.getText().trim();
                String t = to.getText().trim();

                StringBuilder where = new StringBuilder("WHERE l.petugas_id = ?");
                List<Object> params = new ArrayList<>();
                params.add(Integer.parseInt(me.get("user_id")));

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
                JPanel summaryPanel = (JPanel) getComponent(3);
                ((JLabel)summaryPanel.getComponent(0)).setText("Total: " + total + " transaksi");
                ((JLabel)summaryPanel.getComponent(1)).setText("Aktif: " + aktif);
                ((JLabel)summaryPanel.getComponent(2)).setText("Selesai: " + selesai);
                ((JLabel)summaryPanel.getComponent(3)).setText("Batal: " + batal);
                
            } catch (Exception e) {
                e.printStackTrace();
                showErrorDialog("Error", "Gagal memuat laporan: " + e.getMessage());
            }
        }
    }

    class NotifikasiPanel extends JPanel {
        private DefaultTableModel model = new DefaultTableModel(new String[]{"LoanID","Anggota","Jatuh Tempo","Telat (hari)","Status"},0);
        private JTable table = new JTable(model);
        private JLabel summaryLabel;

        NotifikasiPanel() {
            setBackground(Utils.BG);
            setLayout(new BorderLayout());
            
            // Header
            JPanel header = createPanelHeader("Notifikasi Keterlambatan", 
                "Peminjaman yang melewati jatuh tempo");
            
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
            
            JButton refreshBtn = createPrimaryButton("Refresh");
            JButton notifyBtn = createSecondaryButton("Notifikasi Anggota");
            
            refreshBtn.addActionListener(e -> refresh());
            notifyBtn.addActionListener(e -> notifyAnggota());
            
            buttonPanel.add(refreshBtn);
            buttonPanel.add(notifyBtn);
            
            actionPanel.add(summaryLabel, BorderLayout.WEST);
            actionPanel.add(buttonPanel, BorderLayout.EAST);
            
            add(header, BorderLayout.NORTH);
            add(scroll, BorderLayout.CENTER);
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

        void refresh() {
            try {
                model.setRowCount(0);
                List<Map<String, String>> rows = DB.query(
                    "SELECT l.loan_id, u.nama_lengkap as anggota, l.jatuh_tempo, " +
                    "GREATEST(DATEDIFF(CURDATE(), l.jatuh_tempo), 0) as telat, l.status " +
                    "FROM loans l JOIN users u ON l.user_id = u.user_id " +
                    "WHERE l.status = 'AKTIF' AND l.jatuh_tempo < CURDATE() " +
                    "ORDER BY telat DESC"
                );

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
                summaryLabel.setText("Total: " + total + " | Kritis: " + critical + 
                    " | Tinggi: " + high + " | Sedang: " + medium);
                
                if (total > 0) {
                    updateNavBadge(4, total);
                }
                
            } catch (Exception e) {
                e.printStackTrace();
                showErrorDialog("Error", "Gagal memuat notifikasi: " + e.getMessage());
            }
        }
        
        private void notifyAnggota() {
            int r = table.getSelectedRow();
            if (r < 0) { 
                showMessageDialog("Peringatan", "Pilih transaksi terlebih dahulu."); 
                return; 
            }
            
            String loanId = model.getValueAt(r, 0).toString();
            String anggota = model.getValueAt(r, 1).toString();
            int telat = Integer.parseInt(model.getValueAt(r, 3).toString());
            
            String message = "Kirim notifikasi kepada " + anggota + "?\n" +
                           "Loan ID: " + loanId + "\n" +
                           "Telat: " + telat + " hari\n\n" +
                           "(Fitur notifikasi email/SMS dalam pengembangan)";
            
            if (confirmDialog("Kirim Notifikasi", message)) {
                showMessageDialog("Info", "Notifikasi berhasil dikirim ke " + anggota + 
                    "\n\nCatatan: Fitur notifikasi email/SMS sedang dalam pengembangan.");
            }
        }
    }
}