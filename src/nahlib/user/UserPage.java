package nahlib.user;

import nahlib.DB;
import nahlib.LoginPage;
import nahlib.Utils;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class UserPage extends JFrame {

    private final Map<String,String> me;
    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);
    private final Map<String, JPanel> panels = new HashMap<>();

    public UserPage(Map<String,String> me) {
        this.me = me;

        setTitle(Utils.getLibraryName() + " - Anggota Dashboard");
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
        
        JLabel subtitle = new JLabel(" • Anggota Dashboard");
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
        
        JLabel userIcon = new JLabel("👤");
        userIcon.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        
        JLabel who = new JLabel(me.get("nama_lengkap"));
        who.setForeground(Utils.TEXT);
        who.setFont(Utils.FONT_B);
        
        JLabel kelas = new JLabel("Kelas: " + me.get("kelas"));
        kelas.setForeground(Utils.MUTED);
        kelas.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        
        JLabel role = new JLabel("ANGGOTA");
        role.setForeground(new Color(66, 133, 244));
        role.setFont(new Font("Segoe UI", Font.BOLD, 10));
        role.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(66, 133, 244)),
            new EmptyBorder(2, 6, 2, 6)
        ));
        
        userCard.add(userIcon);
        userCard.add(who);
        userCard.add(role);
        userCard.add(kelas);
        
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
                DB.audit(id(), "LOGOUT", "users", me.get("user_id"), "Logout");
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
        BrowsePanel browse = new BrowsePanel();
        WishlistPanel wish = new WishlistPanel();
        RiwayatPanel hist = new RiwayatPanel();
        NotifPanel notif = new NotifPanel();
        
        panels.put("dash", dash);
        panels.put("browse", browse);
        panels.put("wish", wish);
        panels.put("hist", hist);
        panels.put("notif", notif);
        
        content.add(dash, "dash");
        content.add(browse, "browse");
        content.add(wish, "wish");
        content.add(hist, "hist");
        content.add(notif, "notif");

        // =========== BOTTOM NAVIGATION ===========
        String[] labels = new String[]{
            "Dashboard", "Browse Books", "Wishlist", 
            "Riwayat", "Notifikasi"
        };
        
        Runnable[] actions = new Runnable[]{
            () -> { dash.refresh(); showPanel("dash", 0); },
            () -> { browse.refresh(); showPanel("browse", 1); },
            () -> { wish.refresh(); showPanel("wish", 2); },
            () -> { hist.refresh(); showPanel("hist", 3); },
            () -> { notif.refresh(); showPanel("notif", 4); }
        };
        
        JPanel nav = createBottomNav(labels, actions, 0);

        // =========== ASSEMBLE ROOT ===========
        root.add(top, BorderLayout.NORTH);
        root.add(content, BorderLayout.CENTER);
        root.add(nav, BorderLayout.SOUTH);

        setContentPane(root);
        dash.refresh();
        setVisible(true);
    }

    private long id() { 
        return Long.parseLong(me.get("user_id")); 
    }

    private void showPanel(String key, int activeIndex) {
        cards.show(content, key);
        updateNavHighlight(activeIndex);
        
        if (key.equals("notif")) {
            int due = ((NotifPanel)panels.get("notif")).countDueSoonOrOverdue();
            updateNavBadge(4, due);
        }
    }
    
    private JPanel createBottomNav(String[] labels, Runnable[] actions, int activeIndex) {
        JPanel bar = new JPanel(new GridLayout(1, labels.length, 1, 0));
        bar.setBackground(Utils.BG);
        bar.setBorder(new EmptyBorder(0, 0, 0, 0));
        
        for (int i = 0; i < labels.length; i++) {
            JButton btn = createNavButton(labels[i], i == activeIndex);
            final int idx = i;
            btn.addActionListener(e -> {
                actions[idx].run();
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
    
    private void updateNavHighlight(int activeIndex) {
        JPanel root = (JPanel) getContentPane();
        
        // Cari panel navigasi (posisi BorderLayout.SOUTH = 2)
        Component southComponent = null;
        BorderLayout layout = (BorderLayout) root.getLayout();
        if (layout != null) {
            southComponent = layout.getLayoutComponent(BorderLayout.SOUTH);
        }
        
        if (southComponent != null) {
            root.remove(southComponent);
            
            String[] labels = new String[]{
                "Dashboard", "Browse Books", "Wishlist", 
                "Riwayat", "Notifikasi"
            };
            
            Runnable[] actions = new Runnable[]{
                () -> { ((DashboardPanel)panels.get("dash")).refresh(); showPanel("dash", 0); },
                () -> { ((BrowsePanel)panels.get("browse")).refresh(); showPanel("browse", 1); },
                () -> { ((WishlistPanel)panels.get("wish")).refresh(); showPanel("wish", 2); },
                () -> { ((RiwayatPanel)panels.get("hist")).refresh(); showPanel("hist", 3); },
                () -> { ((NotifPanel)panels.get("notif")).refresh(); showPanel("notif", 4); }
            };
            
            JPanel newNav = createBottomNav(labels, actions, activeIndex);
            root.add(newNav, BorderLayout.SOUTH);
            root.revalidate();
            root.repaint();
        }
    }
    
    private void updateNavBadge(int index, int count) {
        if (count <= 0) return;
        
        JPanel root = (JPanel) getContentPane();
        BorderLayout layout = (BorderLayout) root.getLayout();
        JPanel nav = (JPanel) layout.getLayoutComponent(BorderLayout.SOUTH);
        
        if (nav != null && nav.getComponent(index) instanceof JButton) {
            JButton notifBtn = (JButton) nav.getComponent(index);
            notifBtn.setText("Notifikasi (" + count + ")");
        }
    }
    
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
        private final JLabel borrowed = createStatLabel("0");
        private final JLabel dueSoon = createStatLabel("0");
        private final JLabel overdue = createStatLabel("0");
        private final JLabel wishlist = createStatLabel("0");

        DashboardPanel() {
            setBackground(Utils.BG);
            setLayout(new BorderLayout());
            
            // Header
            JPanel header = new JPanel(new BorderLayout());
            header.setBackground(Utils.BG);
            header.setBorder(new EmptyBorder(20, 20, 20, 20));
            
            JLabel title = new JLabel("Dashboard Anggota");
            title.setForeground(Utils.TEXT);
            title.setFont(new Font("Segoe UI", Font.BOLD, 24));
            
            JLabel subtitle = new JLabel("Selamat datang, " + me.get("nama_lengkap") + "! Status peminjaman Anda:");
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
            
            grid.add(createStatCard("Sedang Dipinjam", borrowed, new Color(66, 133, 244)));
            grid.add(createStatCard("Jatuh Tempo (≤1 hari)", dueSoon, new Color(66, 133, 244)));
            grid.add(createStatCard("Terlambat", overdue, new Color(251, 188, 5)));
            grid.add(createStatCard("Wishlist", wishlist, new Color(251, 188, 5)));
            
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
            
            JButton browseBtn = createSecondaryButton("Browse Books");
            JButton wishlistBtn = createSecondaryButton("Lihat Wishlist");
            JButton historyBtn = createSecondaryButton("Lihat Riwayat");
            JButton notificationBtn = createSecondaryButton("Cek Notifikasi");
            
            browseBtn.addActionListener(e -> {
                ((BrowsePanel)panels.get("browse")).refresh();
                showPanel("browse", 1);
            });
            
            wishlistBtn.addActionListener(e -> {
                ((WishlistPanel)panels.get("wish")).refresh();
                showPanel("wish", 2);
            });
            
            historyBtn.addActionListener(e -> {
                ((RiwayatPanel)panels.get("hist")).refresh();
                showPanel("hist", 3);
            });
            
            notificationBtn.addActionListener(e -> {
                ((NotifPanel)panels.get("notif")).refresh();
                showPanel("notif", 4);
            });
            
            actions.add(browseBtn);
            actions.add(wishlistBtn);
            actions.add(historyBtn);
            actions.add(notificationBtn);
            
            panel.add(title, BorderLayout.NORTH);
            panel.add(actions, BorderLayout.CENTER);
            
            return panel;
        }

        void refresh() {
            try {
                long userId = id();
                
                // Sedang Dipinjam (AKTIF)
                int b = Integer.parseInt(DB.query(
                    "SELECT COUNT(DISTINCT l.loan_id) as c FROM loans l " +
                    "WHERE l.user_id=? AND l.status='AKTIF'", 
                    userId).get(0).get("c"));
                
                // Jatuh Tempo (≤1 hari)
                int d = Integer.parseInt(DB.query(
                    "SELECT COUNT(DISTINCT l.loan_id) as c FROM loans l " +
                    "WHERE l.user_id=? AND l.status='AKTIF' " +
                    "AND l.jatuh_tempo BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 1 DAY)", 
                    userId).get(0).get("c"));
                
                // Terlambat
                int o = Integer.parseInt(DB.query(
                    "SELECT COUNT(DISTINCT l.loan_id) as c FROM loans l " +
                    "WHERE l.user_id=? AND l.status='AKTIF' AND l.jatuh_tempo < CURDATE()", 
                    userId).get(0).get("c"));
                
                // Wishlist
                int w = Integer.parseInt(DB.query(
                    "SELECT COUNT(*) as c FROM wishlist WHERE user_id=?", 
                    userId).get(0).get("c"));

                borrowed.setText(String.valueOf(b));
                dueSoon.setText(String.valueOf(d));
                overdue.setText(String.valueOf(o));
                wishlist.setText(String.valueOf(w));

                if (d > 0 || o > 0) {
                    showMessageDialog("Peringatan", 
                        "Ada " + (d + o) + " buku yang perlu perhatian:\n" +
                        "• " + d + " buku akan jatuh tempo dalam 1 hari\n" +
                        "• " + o + " buku sudah terlambat\n\n" +
                        "Silakan cek menu Notifikasi untuk detail lebih lanjut.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

class BrowsePanel extends JPanel {
    private DefaultTableModel model = new DefaultTableModel(new String[]{
        "ID", "Kode", "ISBN", "Judul", "Penulis", "Penerbit", 
        "Tahun", "Kategori", "Rak", "Stok Total", "Stok Tersedia"
    }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    
    private JTable table = new JTable(model);
    private JTextField search = Utils.input("Cari judul/kode/penulis/penerbit...");
    private JButton btnRefresh;
    private JButton btnAddWishlist;
    private JLabel summaryLabel;
    private javax.swing.Timer searchTimer; // Timer untuk live search

    BrowsePanel() {
        setBackground(Utils.BG);
        setLayout(new BorderLayout());
        
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Utils.BG);
        header.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JLabel title = new JLabel("Browse Books");
        title.setForeground(Utils.TEXT);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        
        JLabel subtitle = new JLabel("Jelajahi koleksi buku perpustakaan");
        subtitle.setForeground(Utils.MUTED);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.add(title, BorderLayout.NORTH);
        titlePanel.add(subtitle, BorderLayout.SOUTH);
        
        // === LIVE SEARCH PANEL DI POJOK KANAN ATAS ===
        JPanel liveSearchPanel = new JPanel(new BorderLayout(10, 0));
        liveSearchPanel.setOpaque(false);
        
        // Search icon
        JLabel searchIcon = new JLabel("Cari Buku");
        searchIcon.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        searchIcon.setForeground(Utils.TEXT);
        
        // Live search field (lebih kecil dari field utama)
        JTextField liveSearchField = Utils.input("Live search...");
        liveSearchField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        liveSearchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Utils.BORDER),
            new EmptyBorder(6, 10, 6, 10)
        ));
        liveSearchField.setPreferredSize(new Dimension(200, 35));
        
        // Clear button for live search
        JButton clearLiveSearch = new JButton("×");
        clearLiveSearch.setFont(new Font("Segoe UI", Font.BOLD, 14));
        clearLiveSearch.setForeground(Utils.MUTED);
        clearLiveSearch.setBackground(Utils.CARD);
        clearLiveSearch.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Utils.BORDER),
            new EmptyBorder(2, 8, 2, 8)
        ));
        clearLiveSearch.setFocusPainted(false);
        clearLiveSearch.setCursor(new Cursor(Cursor.HAND_CURSOR));
        clearLiveSearch.setToolTipText("Clear live search");
        clearLiveSearch.addActionListener(e -> {
            liveSearchField.setText("");
            search.setText("");
            refresh();
        });
        
        liveSearchPanel.add(searchIcon, BorderLayout.WEST);
        liveSearchPanel.add(liveSearchField, BorderLayout.CENTER);
        liveSearchPanel.add(clearLiveSearch, BorderLayout.EAST);
        
        // Setup timer untuk live search (delay 500ms setelah user berhenti mengetik)
        searchTimer = new javax.swing.Timer(500, e -> {
            search.setText(liveSearchField.getText().trim());
            refresh();
        });
        searchTimer.setRepeats(false);
        
        liveSearchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                searchTimer.restart();
            }
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                searchTimer.restart();
            }
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                searchTimer.restart();
            }
        });
        
        // Container untuk live search + refresh button
        JPanel rightTopContainer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightTopContainer.setOpaque(false);
        
        rightTopContainer.add(liveSearchPanel);
        
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
        
        rightTopContainer.add(btnRefresh);
        
        header.add(titlePanel, BorderLayout.WEST);
        header.add(rightTopContainer, BorderLayout.EAST);
        
        // Search panel utama (tetap ada untuk UI yang familiar)
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBackground(Utils.BG);
        searchPanel.setBorder(new EmptyBorder(0, 20, 10, 20));
        
        JPanel searchBox = new JPanel(new BorderLayout(10, 0));
        searchBox.setOpaque(false);
        
        search.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Utils.BORDER),
            new EmptyBorder(10, 15, 10, 15)
        ));
        
        JButton searchBtn = createPrimaryButton("Search");
        searchBtn.addActionListener(e -> refresh());
        
        JButton clearBtn = createSecondaryButton("Clear");
        clearBtn.addActionListener(e -> { 
            search.setText(""); 
            if (liveSearchField != null) {
                liveSearchField.setText("");
            }
            refresh(); 
        });
        
        searchBox.add(search, BorderLayout.CENTER);
        
        JPanel searchButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchButtons.setOpaque(false);
        searchButtons.add(searchBtn);
        searchButtons.add(clearBtn);
        
        searchBox.add(searchButtons, BorderLayout.EAST);
        searchPanel.add(searchBox, BorderLayout.NORTH);
        
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
        
        btnAddWishlist = createPrimaryButton("Tambah ke Wishlist");
        
        btnRefresh.addActionListener(e -> refresh());
        btnAddWishlist.addActionListener(e -> addWishlist());
        
        buttonPanel.add(btnRefresh);
        buttonPanel.add(btnAddWishlist);
        
        actionPanel.add(summaryLabel, BorderLayout.WEST);
        actionPanel.add(buttonPanel, BorderLayout.EAST);
        
        add(header, BorderLayout.NORTH);
        add(searchPanel, BorderLayout.CENTER);
        add(scroll, BorderLayout.CENTER);
        add(actionPanel, BorderLayout.SOUTH);
        
        refresh();
    }
    
    private void styleTable() {
        // Set column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(50);   // ID
        table.getColumnModel().getColumn(1).setPreferredWidth(80);   // Kode
        table.getColumnModel().getColumn(2).setPreferredWidth(120);  // ISBN
        table.getColumnModel().getColumn(3).setPreferredWidth(300);  // Judul
        table.getColumnModel().getColumn(4).setPreferredWidth(150);  // Penulis
        table.getColumnModel().getColumn(5).setPreferredWidth(150);  // Penerbit
        table.getColumnModel().getColumn(6).setPreferredWidth(60);   // Tahun
        table.getColumnModel().getColumn(7).setPreferredWidth(100);  // Kategori
        table.getColumnModel().getColumn(8).setPreferredWidth(80);   // Rak
        table.getColumnModel().getColumn(9).setPreferredWidth(80);   // Stok Total
        table.getColumnModel().getColumn(10).setPreferredWidth(80);  // Stok Tersedia
        
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
                
                // Color stock columns (column 9 & 10)
                if ((column == 9 || column == 10) && value != null) {
                    try {
                        int stock = Integer.parseInt(value.toString());
                        if (stock <= 0) {
                            setForeground(new Color(234, 67, 53));
                            if (column == 10) setText("HABIS");
                        } else if (stock <= 2) {
                            setForeground(new Color(251, 188, 5));
                        } else {
                            setForeground(new Color(52, 168, 83));
                        }
                    } catch (NumberFormatException e) {
                        // Ignore
                    }
                }
                
                setBorder(noFocusBorder);
                return c;
            }
        });
        
        // Add selection listener
        table.getSelectionModel().addListSelectionListener(e -> {
            btnAddWishlist.setEnabled(table.getSelectedRow() >= 0);
        });
    }

    void refresh() {
        try {
            model.setRowCount(0);
            String q = search.getText().trim();
            String sql =
                    "SELECT b.book_id, b.code, b.isbn, b.judul, b.penulis, b.penerbit, " +
                    "b.tahun, c.name kategori, r.code rak, b.stok_total, b.stok_tersedia " +
                    "FROM books b " +
                    "LEFT JOIN categories c ON b.category_id=c.category_id " +
                    "LEFT JOIN racks r ON b.rack_id=r.rack_id ";
            
            List<Map<String,String>> rows;
            if (q.isEmpty()) {
                rows = DB.query(sql + " ORDER BY b.judul");
            } else {
                rows = DB.query(sql + " WHERE b.judul LIKE ? OR b.code LIKE ? OR b.penulis LIKE ? OR b.penerbit LIKE ? ORDER BY b.judul", 
                    "%"+q+"%", "%"+q+"%", "%"+q+"%", "%"+q+"%");
            }
            
            int totalBooks = 0;
            int availableBooks = 0;
            int totalStock = 0;
            
            for (Map<String,String> r: rows) {
                model.addRow(new Object[]{
                    r.get("book_id"), r.get("code"), r.get("isbn"), 
                    r.get("judul"), r.get("penulis"), r.get("penerbit"),
                    r.get("tahun"), r.get("kategori"), r.get("rak"),
                    r.get("stok_total"), r.get("stok_tersedia")
                });
                
                totalBooks++;
                int stock = Integer.parseInt(r.get("stok_tersedia"));
                totalStock += Integer.parseInt(r.get("stok_total"));
                if (stock > 0) {
                    availableBooks++;
                }
            }
            
            btnRefresh.setText(totalBooks + " buku");
            summaryLabel.setText("Total: " + totalBooks + " buku | Tersedia: " + availableBooks + 
                " buku | Total Stok: " + totalStock);
            
            btnAddWishlist.setEnabled(table.getSelectedRow() >= 0);
            
        } catch (Exception e) {
            e.printStackTrace();
            showErrorDialog("Error", "Gagal memuat data buku: " + e.getMessage());
        }
    }

    void addWishlist() {
        int r = table.getSelectedRow();
        if (r < 0) { 
            showMessageDialog("Peringatan", "Pilih buku terlebih dahulu."); 
            return; 
        }
        
        String bookIdStr = model.getValueAt(r, 0).toString();
        String judul = model.getValueAt(r, 3).toString();
        
        long bookId;
        try {
            bookId = Long.parseLong(bookIdStr);
        } catch (NumberFormatException e) {
            showErrorDialog("Error", "ID buku tidak valid.");
            return;
        }
        
        try {
            List<Map<String,String>> existing = DB.query(
                "SELECT COUNT(*) as c FROM wishlist WHERE user_id=? AND book_id=?", 
                id(), bookId);
            
            int count = Integer.parseInt(existing.get(0).get("c"));
            
            if (count > 0) {
                showMessageDialog("Info", "Buku '" + judul + "' sudah ada di wishlist Anda.");
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            showErrorDialog("Error", "Gagal memeriksa wishlist: " + e.getMessage());
            return;
        }
        
        if (!confirmDialog("Tambah ke Wishlist", 
            "Tambahkan buku '" + judul + "' ke wishlist Anda?")) return;
        
        try {
            DB.exec("INSERT INTO wishlist(user_id,book_id) VALUES (?,?)", id(), bookId);
            DB.audit(id(), "CREATE", "wishlist", String.valueOf(bookId), "Tambah wishlist: " + judul);
            showMessageDialog("Sukses", "Buku '" + judul + "' berhasil ditambahkan ke wishlist.");
            refresh();
            
            // Refresh dashboard untuk update statistik wishlist
            ((DashboardPanel)panels.get("dash")).refresh();
            
            // Refresh wishlist panel jika sedang aktif
            if (panels.get("wish") != null) {
                ((WishlistPanel)panels.get("wish")).refresh();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            showErrorDialog("Error", "Gagal menambahkan ke wishlist: " + ex.getMessage());
        }
    }
}

    class WishlistPanel extends JPanel {
        private DefaultTableModel model = new DefaultTableModel(new String[]{
            "ID", "Kode", "ISBN", "Judul", "Penulis", "Penerbit", 
            "Tahun", "Kategori", "Rak", "Stok Total", "Stok Tersedia", "Status"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        private JTable table = new JTable(model);
        private JButton btnRefresh;
        private JButton btnRemove;
        private JLabel summaryLabel;

        WishlistPanel() {
            setBackground(Utils.BG);
            setLayout(new BorderLayout());
            
            // Header
            JPanel header = new JPanel(new BorderLayout());
            header.setBackground(Utils.BG);
            header.setBorder(new EmptyBorder(20, 20, 20, 20));
            
            JLabel title = new JLabel("Wishlist / Favorit");
            title.setForeground(Utils.TEXT);
            title.setFont(new Font("Segoe UI", Font.BOLD, 24));
            
            JLabel subtitle = new JLabel("Daftar buku yang ingin Anda pinjam");
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
            
            btnRemove = createPrimaryButton("Hapus dari Wishlist");
            btnRemove.setEnabled(false);
            
            btnRefresh.addActionListener(e -> refresh());
            btnRemove.addActionListener(e -> removeWish());
            
            buttonPanel.add(btnRefresh);
            buttonPanel.add(btnRemove);
            
            actionPanel.add(summaryLabel, BorderLayout.WEST);
            actionPanel.add(buttonPanel, BorderLayout.EAST);
            
            table.getSelectionModel().addListSelectionListener(e -> {
                btnRemove.setEnabled(table.getSelectedRow() >= 0);
            });
            
            add(header, BorderLayout.NORTH);
            add(scroll, BorderLayout.CENTER);
            add(actionPanel, BorderLayout.SOUTH);
            
            refresh();
        }
        
        private void styleTable() {
            // Set column widths
            table.getColumnModel().getColumn(0).setPreferredWidth(50);   // ID
            table.getColumnModel().getColumn(1).setPreferredWidth(80);   // Kode
            table.getColumnModel().getColumn(2).setPreferredWidth(120);  // ISBN
            table.getColumnModel().getColumn(3).setPreferredWidth(250);  // Judul
            table.getColumnModel().getColumn(4).setPreferredWidth(150);  // Penulis
            table.getColumnModel().getColumn(5).setPreferredWidth(150);  // Penerbit
            table.getColumnModel().getColumn(6).setPreferredWidth(60);   // Tahun
            table.getColumnModel().getColumn(7).setPreferredWidth(100);  // Kategori
            table.getColumnModel().getColumn(8).setPreferredWidth(80);   // Rak
            table.getColumnModel().getColumn(9).setPreferredWidth(80);   // Stok Total
            table.getColumnModel().getColumn(10).setPreferredWidth(80);  // Stok Tersedia
            table.getColumnModel().getColumn(11).setPreferredWidth(80);  // Status
            
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
                    
                    // Stock columns
                    if ((column == 9 || column == 10) && value != null) {
                        try {
                            int stock = Integer.parseInt(value.toString());
                            if (stock <= 0) {
                                setForeground(new Color(234, 67, 53));
                            } else if (stock <= 2) {
                                setForeground(new Color(251, 188, 5));
                            } else {
                                setForeground(new Color(52, 168, 83));
                            }
                        } catch (NumberFormatException e) {}
                    }
                    
                    // Status column (column 11)
                    if (column == 11 && value != null) {
                        String status = value.toString();
                        if ("TERSEDIA".equals(status)) {
                            setForeground(new Color(52, 168, 83));
                        } else if ("TERBATAS".equals(status)) {
                            setForeground(new Color(251, 188, 5));
                        } else if ("HABIS".equals(status)) {
                            setForeground(new Color(234, 67, 53));
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
                List<Map<String,String>> rows = DB.query(
                        "SELECT b.book_id, b.code, b.isbn, b.judul, b.penulis, b.penerbit, " +
                        "b.tahun, c.name kategori, r.code rak, b.stok_total, b.stok_tersedia, " +
                        "CASE " +
                        " WHEN b.stok_tersedia <= 0 THEN 'HABIS' " +
                        " WHEN b.stok_tersedia <= 2 THEN 'TERBATAS' " +
                        " ELSE 'TERSEDIA' END as status " +
                        "FROM wishlist w " +
                        "JOIN books b ON w.book_id=b.book_id " +
                        "LEFT JOIN categories c ON b.category_id=c.category_id " +
                        "LEFT JOIN racks r ON b.rack_id=r.rack_id " +
                        "WHERE w.user_id=? ORDER BY b.judul", 
                        id()
                );
                
                int total = 0;
                int available = 0;
                int limited = 0;
                int outOfStock = 0;
                
                for (Map<String,String> r: rows) {
                    model.addRow(new Object[]{ 
                        r.get("book_id"), 
                        r.get("code"), 
                        r.get("isbn"), 
                        r.get("judul"), 
                        r.get("penulis"),
                        r.get("penerbit"),
                        r.get("tahun"),
                        r.get("kategori"),
                        r.get("rak"),
                        r.get("stok_total"),
                        r.get("stok_tersedia"),
                        r.get("status")
                    });
                    
                    total++;
                    int stock = Integer.parseInt(r.get("stok_tersedia"));
                    if (stock <= 0) {
                        outOfStock++;
                    } else if (stock <= 2) {
                        limited++;
                    } else {
                        available++;
                    }
                }
                
                btnRefresh.setText(total + " buku");
                summaryLabel.setText("Total: " + total + " buku | Tersedia: " + available + 
                    " | Terbatas: " + limited + " | Habis: " + outOfStock);
                
                btnRemove.setEnabled(table.getSelectedRow() >= 0);
                
            } catch (Exception e) {
                e.printStackTrace();
                showErrorDialog("Error", "Gagal memuat wishlist: " + e.getMessage());
            }
        }

        void removeWish() {
            int r = table.getSelectedRow();
            if (r < 0) { 
                showMessageDialog("Peringatan", "Pilih buku terlebih dahulu."); 
                return; 
            }
            
            String bookIdStr = model.getValueAt(r, 0).toString();
            String judul = model.getValueAt(r, 3).toString();
            
            long bookId;
            try {
                bookId = Long.parseLong(bookIdStr);
            } catch (NumberFormatException e) {
                showErrorDialog("Error", "ID buku tidak valid.");
                return;
            }
            
            if (!confirmDialog("Hapus dari Wishlist", 
                "Hapus buku '" + judul + "' dari wishlist Anda?")) return;
            
            try {
                DB.exec("DELETE FROM wishlist WHERE user_id=? AND book_id=?", id(), bookId);
                DB.audit(id(), "DELETE", "wishlist", String.valueOf(bookId), "Hapus wishlist: " + judul);
                showMessageDialog("Sukses", "Buku '" + judul + "' berhasil dihapus dari wishlist.");
                refresh();
                
                // Refresh dashboard untuk update statistik wishlist
                ((DashboardPanel)panels.get("dash")).refresh();
            } catch (Exception ex) {
                ex.printStackTrace();
                showErrorDialog("Error", "Gagal menghapus dari wishlist: " + ex.getMessage());
            }
        }
    }

    class RiwayatPanel extends JPanel {
        private DefaultTableModel model = new DefaultTableModel(new String[]{
            "Loan ID", "Tanggal Pinjam", "Jatuh Tempo", "Status", 
            "Total Item", "Petugas", "Tanggal Kembali", "Denda", "Keterangan"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        private JTable table = new JTable(model);
        private JButton btnRefresh;
        private JLabel summaryLabel;

        RiwayatPanel() {
            setBackground(Utils.BG);
            setLayout(new BorderLayout());
            
            // Header
            JPanel header = new JPanel(new BorderLayout());
            header.setBackground(Utils.BG);
            header.setBorder(new EmptyBorder(20, 20, 20, 20));
            
            JLabel title = new JLabel("Riwayat Peminjaman");
            title.setForeground(Utils.TEXT);
            title.setFont(new Font("Segoe UI", Font.BOLD, 24));
            
            JLabel subtitle = new JLabel("Histori peminjaman buku Anda");
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
            
            // Summary panel
            JPanel summaryPanel = new JPanel(new BorderLayout());
            summaryPanel.setBackground(Utils.BG);
            summaryPanel.setBorder(new EmptyBorder(0, 20, 20, 20));
            
            summaryLabel = new JLabel();
            summaryLabel.setForeground(Utils.TEXT);
            summaryLabel.setFont(Utils.FONT);
            
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            buttonPanel.setOpaque(false);
            
            JButton detailBtn = createPrimaryButton("Lihat Detail Buku");
            
            btnRefresh.addActionListener(e -> refresh());
            detailBtn.addActionListener(e -> showLoanDetail());
            
            buttonPanel.add(btnRefresh);
            buttonPanel.add(detailBtn);
            
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
                        } else if ("SELESAI".equals(value)) {
                            setForeground(new Color(52, 168, 83));
                        } else if ("BATAL".equals(value)) {
                            setForeground(new Color(234, 67, 53));
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

        void refresh() {
            try {
                model.setRowCount(0);
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
                        id()
                );
                
                int total = 0;
                int aktif = 0;
                int selesai = 0;
                int batal = 0;
                int terlambat = 0;
                int totalDenda = 0;
                
                for (Map<String,String> r: rows) {
                    String fineTotal = r.get("fine_total") != null ? r.get("fine_total") : "0";
                    model.addRow(new Object[]{ 
                        r.get("loan_id"), 
                        r.get("tanggal_pinjam"), 
                        r.get("jatuh_tempo"), 
                        r.get("status"), 
                        r.get("total_item"),
                        r.get("petugas"),
                        r.get("tanggal_kembali"),
                        fineTotal,
                        r.get("keterangan")
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
                    
                    if (r.get("fine_total") != null) {
                        try {
                            totalDenda += Integer.parseInt(r.get("fine_total"));
                        } catch (NumberFormatException e) {}
                    }
                }
                
                btnRefresh.setText(total + " riwayat");
                summaryLabel.setText("Total: " + total + " | Aktif: " + aktif + 
                    " | Selesai: " + selesai + " | Batal: " + batal + 
                    (terlambat > 0 ? " | ⚠️Terlambat: " + terlambat : "") +
                    (totalDenda > 0 ? " | Total Denda: Rp " + totalDenda : ""));
                
            } catch (Exception e) {
                e.printStackTrace();
                showErrorDialog("Error", "Gagal memuat riwayat: " + e.getMessage());
            }
        }
        
        private void showLoanDetail() {
            int r = table.getSelectedRow();
            if (r < 0) { 
                showMessageDialog("Peringatan", "Pilih riwayat peminjaman terlebih dahulu."); 
                return; 
            }
            
            String loanId = model.getValueAt(r, 0).toString();
            
            try {
                List<Map<String,String>> items = DB.query(
                    "SELECT b.code, b.judul, b.penulis, b.penerbit, li.qty " +
                    "FROM loan_items li " +
                    "JOIN books b ON li.book_id = b.book_id " +
                    "WHERE li.loan_id = ?", 
                    loanId
                );
                
                StringBuilder detail = new StringBuilder();
                detail.append("=== Detail Buku yang Dipinjam ===\n");
                detail.append("Loan ID: ").append(loanId).append("\n\n");
                
                int totalBuku = 0;
                for (Map<String,String> item : items) {
                    detail.append("• ").append(item.get("code")).append(" - ")
                          .append(item.get("judul")).append("\n")
                          .append("  Penulis: ").append(item.get("penulis")).append("\n")
                          .append("  Penerbit: ").append(item.get("penerbit")).append("\n")
                          .append("  Jumlah: ").append(item.get("qty")).append(" buku\n\n");
                    totalBuku += Integer.parseInt(item.get("qty"));
                }
                
                detail.append("Total Buku: ").append(totalBuku).append(" buku");
                
                JTextArea textArea = new JTextArea(detail.toString());
                textArea.setEditable(false);
                textArea.setBackground(Utils.CARD);
                textArea.setForeground(Utils.TEXT);
                textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
                
                JScrollPane scrollPane = new JScrollPane(textArea);
                scrollPane.setPreferredSize(new Dimension(500, 300));
                
                JOptionPane.showMessageDialog(this, scrollPane, "Detail Buku - Loan #" + loanId, 
                    JOptionPane.INFORMATION_MESSAGE);
                
            } catch (Exception ex) {
                ex.printStackTrace();
                showErrorDialog("Error", "Gagal memuat detail buku: " + ex.getMessage());
            }
        }
    }

    class NotifPanel extends JPanel {
        private DefaultTableModel model = new DefaultTableModel(new String[]{
            "Loan ID", "Tanggal Pinjam", "Jatuh Tempo", "Status", 
            "Total Item", "Sisa Hari", "Keterangan", "Estimasi Denda"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        private JTable table = new JTable(model);
        private JButton btnRefresh;
        private JLabel summaryLabel;

        NotifPanel() {
            setBackground(Utils.BG);
            setLayout(new BorderLayout());
            
            // Header
            JPanel header = new JPanel(new BorderLayout());
            header.setBackground(Utils.BG);
            header.setBorder(new EmptyBorder(20, 20, 20, 20));
            
            JLabel title = new JLabel("Notifikasi");
            title.setForeground(Utils.TEXT);
            title.setFont(new Font("Segoe UI", Font.BOLD, 24));
            
            JLabel subtitle = new JLabel("Peringatan jatuh tempo dan keterlambatan peminjaman");
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
            
             JButton remindBtn = createPrimaryButton("Buat Pengingat");
            
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

        int countDueSoonOrOverdue() {
            try {
                int due = Integer.parseInt(DB.query(
                        "SELECT COUNT(DISTINCT l.loan_id) as c FROM loans l " +
                        "WHERE l.user_id=? AND l.status='AKTIF' " +
                        "AND l.jatuh_tempo BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 1 DAY)",
                        id()
                ).get(0).get("c"));
                int over = Integer.parseInt(DB.query(
                        "SELECT COUNT(DISTINCT l.loan_id) as c FROM loans l " +
                        "WHERE l.user_id=? AND l.status='AKTIF' AND l.jatuh_tempo < CURDATE()",
                        id()
                ).get(0).get("c"));
                return due + over;
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
        }

        void refresh() {
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
                        id()
                );
                
                int total = 0;
                int terlambat = 0;
                int dueToday = 0;
                int dueTomorrow = 0;
                int totalEstimasiDenda = 0;
                
                for (Map<String,String> r: rows) {
                    model.addRow(new Object[]{ 
                        r.get("loan_id"), 
                        r.get("tanggal_pinjam"), 
                        r.get("jatuh_tempo"), 
                        r.get("status"), 
                        r.get("total_item"),
                        r.get("sisa_hari"),
                        r.get("keterangan"),
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
                
                btnRefresh.setText(total + " notifikasi");
                summaryLabel.setText("Total: " + total + " | Terlambat: " + terlambat + 
                    " | Hari ini: " + dueToday + " | Besok: " + dueTomorrow +
                    (totalEstimasiDenda > 0 ? " | Total Estimasi Denda: Rp " + totalEstimasiDenda : ""));
                
                if (total > 0) {
                    updateNavBadge(4, total);
                }
                
            } catch (Exception e) {
                e.printStackTrace();
                showErrorDialog("Error", "Gagal memuat notifikasi: " + e.getMessage());
            }
        }
        
        private void createReminder() {
            int r = table.getSelectedRow();
            if (r < 0) { 
                showMessageDialog("Peringatan", "Pilih notifikasi terlebih dahulu."); 
                return; 
            }
            
            String loanId = model.getValueAt(r, 0).toString();
            String jatuhTempo = model.getValueAt(r, 2).toString();
            String keterangan = model.getValueAt(r, 6).toString();
            String estimasiDenda = model.getValueAt(r, 7).toString();
            
            String message = "📌 **PENGINGAT PEMINJAMAN**\n\n" +
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
}