package nahlib.user;

import nahlib.Lang;
import nahlib.DB;
import nahlib.LoginPage;
import nahlib.Utils;
import nahlib.CustomIcon;
import nahlib.SimpleChart;

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
    private JLabel appLogo;
    private JLabel appTitle;

    public UserPage(Map<String,String> me) {
        this.me = me;

        setTitle(Utils.getLibraryName() + " - " + Lang.get("user.dash.title"));
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
        JPanel leftTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        leftTop.setOpaque(false);
        
        appLogo = new JLabel();
        updateLogo();
        
        appTitle = new JLabel(Utils.getLibraryName());
        appTitle.setForeground(Utils.ACCENT);
        appTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        
        JLabel subtitle = new JLabel(" • " + Lang.get("user.dash.title"));
        subtitle.setForeground(Utils.TEXT);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        leftTop.add(appLogo);
        leftTop.add(appTitle);
        leftTop.add(subtitle);

        // Right side: User info + Logout + Refresh
        JPanel rightTop = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightTop.setOpaque(false);
        
        JPanel userCard = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        userCard.setBackground(Utils.CARD);
        userCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Utils.BORDER),
            new EmptyBorder(6, 12, 6, 12)
        ));
        
        // User icon using Graphics2D
        JLabel userIcon = new JLabel(new CustomIcon(CustomIcon.Type.USERS, 20, new Color(66, 133, 244)));
        
        JLabel who = new JLabel(me.get("nama_lengkap"));
        who.setForeground(Utils.TEXT);
        who.setFont(Utils.FONT_B);
        
        JLabel kelas = new JLabel(Lang.get("label.kelas") + ": " + me.get("kelas"));
        kelas.setForeground(Utils.MUTED);
        kelas.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        
        JLabel role = new JLabel(Lang.get("role.user"));
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
        
        JButton refreshBtn = new JButton(Lang.get("btn.refresh"));
        refreshBtn.setFont(Utils.FONT);
        refreshBtn.setForeground(Utils.TEXT);
        refreshBtn.setBackground(Utils.CARD);
        refreshBtn.setBorder(BorderFactory.createCompoundBorder(
             BorderFactory.createLineBorder(Utils.BORDER),
             new EmptyBorder(8, 12, 8, 12)
        ));
        refreshBtn.setFocusPainted(false);
        refreshBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshBtn.addActionListener(e -> refreshApp());
        
        // Language Switcher Button
        String currentLangText = Lang.getLanguage() == 0 ? "ID" : "EN";
        JButton langBtn = new JButton(currentLangText, new CustomIcon(CustomIcon.Type.GLOBE, 16, Utils.TEXT));
        langBtn.setHorizontalTextPosition(SwingConstants.RIGHT);
        langBtn.setIconTextGap(8);
        langBtn.setFont(Utils.FONT_B);
        langBtn.setForeground(Utils.TEXT);
        langBtn.setBackground(Utils.CARD);
        langBtn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Utils.BORDER),
            new EmptyBorder(8, 12, 8, 12)
        ));
        langBtn.setFocusPainted(false);
        langBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        langBtn.setToolTipText(Lang.getLanguage() == 0 ? "Switch to English" : "Ganti ke Bahasa Indonesia");
        langBtn.addActionListener(e -> {
            int newLang = Lang.getLanguage() == 0 ? 1 : 0;
            Lang.setLanguage(newLang);
            dispose();
            new UserPage(me).setVisible(true);
        });
        
        JButton logout = new JButton(Lang.get("btn.logout"));
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
            // Fix method id() not found - use me.get("user_id")
            if (confirmDialog(Lang.get("btn.logout"), Lang.get("msg.confirm_logout"))) {
                DB.audit(Long.parseLong(me.get("user_id")), "LOGOUT", "users", me.get("user_id"), "Logout");
                dispose();
                new LoginPage();
            }
        });
        
        java.awt.event.MouseAdapter hover = new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                ((JButton)evt.getSource()).setBackground(Utils.CARD2);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                ((JButton)evt.getSource()).setBackground(Utils.CARD);
            }
        };
        logout.addMouseListener(hover);
        refreshBtn.addMouseListener(hover);
        langBtn.addMouseListener(hover);

        rightTop.add(refreshBtn);
        rightTop.add(langBtn);
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
            Lang.get("nav.dashboard"), 
            Lang.get("nav.browse"), 
            Lang.get("nav.wishlist"), 
            Lang.get("nav.history"), 
            Lang.get("nav.notifications")
        };
        
        CustomIcon.Type[] icons = new CustomIcon.Type[]{
            CustomIcon.Type.DASHBOARD,
            CustomIcon.Type.SEARCH,
            CustomIcon.Type.BOOKS,
            CustomIcon.Type.REPORTS,
            CustomIcon.Type.AUDIT
        };
        
        Runnable[] actions = new Runnable[]{
            () -> { dash.refresh(); showPanel("dash", 0); },
            () -> { browse.refresh(); showPanel("browse", 1); },
            () -> { wish.refresh(); showPanel("wish", 2); },
            () -> { hist.refresh(); showPanel("hist", 3); },
            () -> { notif.refresh(); showPanel("notif", 4); }
        };
        
        JPanel nav = createBottomNav(labels, icons, actions, 0);

        // =========== ASSEMBLE ROOT ===========
        root.add(top, BorderLayout.NORTH);
        root.add(content, BorderLayout.CENTER);
        root.add(nav, BorderLayout.SOUTH);

        setContentPane(root);
        dash.refresh();
        setVisible(true);
    }
    
    private void refreshApp() {
        updateLogo();
        appTitle.setText(Utils.getLibraryName());
        for (JPanel p : panels.values()) {
            if (p.isVisible()) {
                try {
                    p.getClass().getMethod("refresh").invoke(p);
                } catch (Exception e) {}
            }
        }
    }
    
    private void updateLogo() {
        try {
            java.io.File f = new java.io.File("src/nahlib/nahsazlibrary.png");
            if (f.exists()) {
                ImageIcon ic = new ImageIcon(f.getAbsolutePath());
                ic.getImage().flush();
                appLogo.setIcon(new ImageIcon(Utils.makeCircularImage(ic.getImage(), 32)));
            } else {
                java.net.URL imgURL = getClass().getResource("/nahlib/nahsazlibrary.png");
                if (imgURL != null) {
                    ImageIcon ic = new ImageIcon(imgURL);
                    appLogo.setIcon(new ImageIcon(Utils.makeCircularImage(ic.getImage(), 32)));
                }
            }
        } catch (Exception e) {}
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
    
    private JPanel createBottomNav(String[] labels, CustomIcon.Type[] icons, Runnable[] actions, int activeIndex) {
        JPanel bar = new JPanel(new GridLayout(1, labels.length, 1, 0));
        bar.setBackground(Utils.BG);
        bar.setBorder(new EmptyBorder(0, 0, 0, 0));
        
        for (int i = 0; i < labels.length; i++) {
            JButton btn = createNavButton(labels[i], icons[i], i == activeIndex);
            final int idx = i;
            btn.addActionListener(e -> {
                actions[idx].run();
            });
            bar.add(btn);
        }
        return bar;
    }
    
    private JButton createNavButton(String text, CustomIcon.Type iconType, boolean active) {
        JButton btn = new JButton(text);
        btn.putClientProperty("iconType", iconType);
        btn.setFont(Utils.FONT);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        applyButtonStyle(btn, active);
        
        if (!active) {
            btn.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    if (btn.getBackground() != Utils.ACCENT) {
                        btn.setBackground(Utils.CARD2);
                    }
                }
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    if (btn.getBackground() != Utils.ACCENT) {
                        btn.setBackground(Utils.CARD);
                    }
                }
            });
        }
        
        return btn;
    }

    private void applyButtonStyle(JButton btn, boolean active) {
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
        }
        
        CustomIcon.Type icType = (CustomIcon.Type) btn.getClientProperty("iconType");
        if (icType == null) icType = CustomIcon.Type.DASHBOARD;
        btn.setIcon(new CustomIcon(icType, 18, active ? Color.WHITE : Utils.MUTED));
    }
    
    private void updateNavHighlight(int activeIndex) {
        JPanel root = (JPanel) getContentPane();
        BorderLayout layout = (BorderLayout) root.getLayout();
        Component southComponent = layout.getLayoutComponent(BorderLayout.SOUTH);
        
        if (southComponent != null) {
            root.remove(southComponent);
            
            String[] labels = new String[]{
                Lang.get("nav.dashboard"), 
                Lang.get("nav.browse"), 
                Lang.get("nav.wishlist"), 
                Lang.get("nav.history"), 
                Lang.get("nav.notifications")
            };
            
            CustomIcon.Type[] icons = new CustomIcon.Type[]{
                CustomIcon.Type.DASHBOARD,
                CustomIcon.Type.SEARCH,
                CustomIcon.Type.BOOKS,
                CustomIcon.Type.REPORTS,
                CustomIcon.Type.AUDIT
            };
            
            Runnable[] actions = new Runnable[]{
                () -> { ((DashboardPanel)panels.get("dash")).refresh(); showPanel("dash", 0); },
                () -> { ((BrowsePanel)panels.get("browse")).refresh(); showPanel("browse", 1); },
                () -> { ((WishlistPanel)panels.get("wish")).refresh(); showPanel("wish", 2); },
                () -> { ((RiwayatPanel)panels.get("hist")).refresh(); showPanel("hist", 3); },
                () -> { ((NotifPanel)panels.get("notif")).refresh(); showPanel("notif", 4); }
            };
            
            JPanel newNav = createBottomNav(labels, icons, actions, activeIndex);
            root.add(newNav, BorderLayout.SOUTH);
            root.revalidate();
            root.repaint();
        }
    }
    
    private void updateNavBadge(int index, int count) {
        JPanel root = (JPanel) getContentPane();
        BorderLayout layout = (BorderLayout) root.getLayout();
        JPanel nav = (JPanel) layout.getLayoutComponent(BorderLayout.SOUTH);
        
        if (nav != null && nav.getComponent(index) instanceof JButton) {
            JButton notifBtn = (JButton) nav.getComponent(index);
            String text = Lang.get("nav.notifications");
            if (count > 0) text += " (" + count + ")";
            notifBtn.setText(text);
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
        
        private SimpleChart barChart;
        private SimpleChart lineChart;

        DashboardPanel() {
            setBackground(Utils.BG);
            setLayout(new GridBagLayout()); // Bento Layout
            
            // Header
            JPanel header = new JPanel(new BorderLayout());
            header.setBackground(Utils.BG);
            header.setBorder(new EmptyBorder(20, 30, 10, 30));
            
            JLabel title = new JLabel(Lang.get("user.dash.title"), new CustomIcon(CustomIcon.Type.DASHBOARD, 24, new Color(66, 133, 244)), SwingConstants.LEFT);
            title.setForeground(Utils.TEXT);
            title.setFont(new Font("Segoe UI", Font.BOLD, 26));
            title.setIconTextGap(12);
            
            JLabel subtitle = new JLabel(String.format(Lang.get("user.dash.subtitle"), me.get("nama_lengkap")));
            subtitle.setForeground(Utils.MUTED);
            subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            
            JPanel titlePanel = new JPanel(new BorderLayout());
            titlePanel.setOpaque(false);
            titlePanel.add(title, BorderLayout.NORTH);
            titlePanel.add(subtitle, BorderLayout.SOUTH);
            
            header.add(titlePanel, BorderLayout.WEST);
            
            addGB(header, 0, 0, 3, 1, 1.0, 0.0);

            // === BENTO GRID CONTENT ===
            // Quick Actions (Right Side - Tall - Spans 2 Rows)
            addGB(createQuickActionsPanel(), 2, 1, 1, 2, 0.3, 0.5);

            // Stats Grid (Left Side - 2x2)
            addGB(createStatCard(Lang.get("user.stat.borrowed"), borrowed, new Color(66, 133, 244)), 0, 1, 1, 1, 0.35, 0.25);
            addGB(createStatCard(Lang.get("user.stat.duesoon"), dueSoon, new Color(251, 188, 5)), 1, 1, 1, 1, 0.35, 0.25);
            addGB(createStatCard(Lang.get("user.stat.overdue"), overdue, new Color(234, 67, 53)), 0, 2, 1, 1, 0.35, 0.25);
            addGB(createStatCard(Lang.get("user.stat.wishlist"), wishlist, new Color(52, 168, 83)), 1, 2, 1, 1, 0.35, 0.25);

            // Charts
            barChart = new SimpleChart(SimpleChart.Type.BAR, new Color(66, 133, 244));
            lineChart = new SimpleChart(SimpleChart.Type.LINE, new Color(52, 168, 83));
            
            JPanel barWrapper = Utils.card();
            barWrapper.setLayout(new BorderLayout());
            JLabel barLabel = new JLabel(Lang.get("user.chart.loan"));
            barLabel.setForeground(Utils.TEXT);
            barLabel.setFont(Utils.FONT_B);
            barLabel.setBorder(new EmptyBorder(0,0,10,0));
            barWrapper.add(barLabel, BorderLayout.NORTH);
            barWrapper.add(barChart, BorderLayout.CENTER);

            JPanel lineWrapper = Utils.card();
            lineWrapper.setLayout(new BorderLayout());
            JLabel lineLabel = new JLabel(Lang.get("user.chart.read"));
            lineLabel.setForeground(Utils.TEXT);
            lineLabel.setFont(Utils.FONT_B);
            lineLabel.setBorder(new EmptyBorder(0,0,10,0));
            lineWrapper.add(lineLabel, BorderLayout.NORTH);
            lineWrapper.add(lineChart, BorderLayout.CENTER);

            // Side-by-side charts at bottom, full width
            addGB(barWrapper, 0, 3, 1, 1, 0.5, 0.4);
            addGB(lineWrapper, 1, 3, 2, 1, 0.5, 0.4);
        }
        
        // Helper
        private void addGB(Component comp, int x, int y, int w, int h, double wx, double wy) {
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = x;
            gbc.gridy = y;
            gbc.gridwidth = w;
            gbc.gridheight = h;
            gbc.weightx = wx;
            gbc.weighty = wy;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.insets = new Insets(8, 8, 8, 8);
            if (x == 0) gbc.insets.left = 30;
            if (x + w == 3) gbc.insets.right = 30; // Assuming 3 cols
            if (y == 3) gbc.insets.bottom = 30;
            add(comp, gbc);
        }
        
        private JLabel createStatLabel(String text) {
            JLabel label = new JLabel(text, SwingConstants.LEFT);
            label.setForeground(Utils.TEXT);
            label.setFont(new Font("Segoe UI", Font.BOLD, 32));
            return label;
        }
        
        private JPanel createStatCard(String title, JLabel value, Color accentColor) {
            JPanel card = new JPanel(new BorderLayout());
            card.setBackground(Utils.CARD);
            card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 0, Utils.BORDER), 
                new EmptyBorder(20, 20, 20, 20)
            ));
            
            JPanel headerObj = new JPanel(new BorderLayout());
            headerObj.setOpaque(false);
            JLabel titleLabel = new JLabel(title.toUpperCase());
            titleLabel.setForeground(Utils.MUTED);
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
            titleLabel.setBorder(new EmptyBorder(0, 0, 5, 0));
            headerObj.add(titleLabel, BorderLayout.CENTER);
            
            card.add(headerObj, BorderLayout.NORTH);
            card.add(value, BorderLayout.CENTER);
            
            JPanel bottomBar = new JPanel();
            bottomBar.setBackground(accentColor);
            bottomBar.setPreferredSize(new Dimension(0, 3));
            card.add(bottomBar, BorderLayout.SOUTH);
            
            return card;
        }
        
        private JPanel createQuickActionsPanel() {
            JPanel panel = new JPanel(new BorderLayout());
            panel.setBackground(Utils.CARD);
            panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 0, Utils.BORDER), 
                new EmptyBorder(20, 20, 20, 20)
            ));
            
            JLabel title = new JLabel(Lang.get("user.quickaction.title"));
            title.setForeground(Utils.MUTED);
            title.setFont(new Font("Segoe UI", Font.BOLD, 12));
            title.setBorder(new EmptyBorder(0, 0, 15, 0));
            
            JPanel actions = new JPanel(new GridLayout(4, 1, 0, 10));
            actions.setBackground(Utils.CARD);
            
            JButton browseBtn = createActionButton(Lang.get("user.quickaction.browse"), CustomIcon.Type.SEARCH);
            JButton wishlistBtn = createActionButton(Lang.get("user.quickaction.wishlist"), CustomIcon.Type.BOOKS);
            JButton historyBtn = createActionButton(Lang.get("user.quickaction.history"), CustomIcon.Type.REPORTS);
            JButton notificationBtn = createActionButton(Lang.get("user.quickaction.notification"), CustomIcon.Type.AUDIT);
            
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
        
        private JButton createActionButton(String text, CustomIcon.Type iconType) {
            JButton btn = new JButton(text, new CustomIcon(iconType, 16, Utils.ACCENT));
            btn.setFont(Utils.FONT);
            btn.setForeground(Utils.TEXT);
            btn.setBackground(Utils.BG);
            btn.setHorizontalAlignment(SwingConstants.LEFT);
            btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Utils.BORDER),
                new EmptyBorder(10, 15, 10, 15)
            ));
            btn.setFocusPainted(false);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    btn.setBackground(Utils.CARD2);
                }
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    btn.setBackground(Utils.BG);
                }
            });
            return btn;
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

                // Populate charts
                List<String> months;
                if (Lang.getLanguage() == 0) {
                    months = List.of("Jan", "Feb", "Mar", "Apr", "Mei", "Jun");
                } else {
                    months = List.of("Jan", "Feb", "Mar", "Apr", "May", "Jun");
                }
                List<Double> readData = List.of(2.0, 5.0, 3.0, 7.0, 4.0, 8.0);
                List<Double> activityData = List.of(10.0, 15.0, 25.0, 20.0, 30.0, 45.0);
                
                barChart.setData(months, readData);
                lineChart.setData(months, activityData);

                if (d > 0 || o > 0) {
                    // showMessageDialog("Peringatan", ... );
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

class BrowsePanel extends JPanel {
    private DefaultTableModel model = new DefaultTableModel(new String[]{
        "ID", Lang.get("books.table.id"), "ISBN", Lang.get("books.table.title"), 
        Lang.get("books.table.author"), Lang.get("books.table.publisher"), 
        Lang.get("books.table.year"), Lang.get("books.table.category"), 
        Lang.get("books.form.location"), Lang.get("books.table.stock"), Lang.get("books.table.available")
    }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    
    private JTable table = new JTable(model);
    private JTextField search = Utils.input(Lang.get("user.browse.placeholder"));
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
        
        JLabel title = new JLabel(Lang.get("user.browse.title"));
        title.setForeground(Utils.TEXT);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        
        JLabel subtitle = new JLabel(Lang.get("user.browse.subtitle"));
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
        JLabel searchIcon = new JLabel(Lang.get("user.browse.search_label"));
        searchIcon.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        searchIcon.setForeground(Utils.TEXT);
        
        // Live search field (lebih kecil dari field utama)
        JTextField liveSearchField = Utils.input(Lang.get("user.browse.live_search"));
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
        
        JButton searchBtn = createPrimaryButton(Lang.get("btn.search"));
        searchBtn.addActionListener(e -> refresh());
        
        JButton clearBtn = createSecondaryButton(Lang.get("btn.reset"));
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
        
        btnAddWishlist = createPrimaryButton(Lang.get("user.quickaction.wishlist"));
        
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
                            if (column == 10) setText(Lang.get("user.label.out_of_stock"));
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
            
            btnRefresh.setText(String.format(Lang.get("user.label.total_books"), totalBooks));
            summaryLabel.setText(String.format(Lang.get("user.label.total_books"), totalBooks) + " | " + 
                String.format(Lang.get("user.label.available_books"), availableBooks) + " | " + 
                String.format(Lang.get("user.label.total_stock"), totalStock));
            
            btnAddWishlist.setEnabled(table.getSelectedRow() >= 0);
            
        } catch (Exception e) {
            e.printStackTrace();
            showErrorDialog("Error", "Gagal memuat data buku: " + e.getMessage());
        }
    }

    void addWishlist() {
        int r = table.getSelectedRow();
        if (r < 0) { 
            showMessageDialog(Lang.get("msg.warning"), Lang.get("msg.info")); 
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
                showMessageDialog("Info", String.format(Lang.get("user.msg.already_wishlist"), judul));
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            showErrorDialog("Error", "Gagal memeriksa wishlist: " + e.getMessage());
            return;
        }
        
        if (!confirmDialog(Lang.get("user.quickaction.wishlist"), 
            String.format(Lang.get("user.msg.confirm_wishlist"), judul))) return;
        
        try {
            DB.exec("INSERT INTO wishlist(user_id,book_id) VALUES (?,?)", id(), bookId);
            DB.audit(id(), "CREATE", "wishlist", String.valueOf(bookId), "Tambah wishlist: " + judul);
            showMessageDialog(Lang.get("msg.success"), String.format(Lang.get("user.msg.success_wishlist"), judul));
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
            "ID", Lang.get("books.table.id"), "ISBN", Lang.get("books.table.title"), 
            Lang.get("books.table.author"), Lang.get("books.table.publisher"), 
            Lang.get("books.table.year"), Lang.get("books.table.category"), 
            Lang.get("books.form.location"), Lang.get("books.table.stock"), Lang.get("books.table.available"), 
            Lang.get("table.status")
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
            
            JLabel title = new JLabel(Lang.get("user.wishlist.title"));
            title.setForeground(Utils.TEXT);
            title.setFont(new Font("Segoe UI", Font.BOLD, 24));
            
            JLabel subtitle = new JLabel(Lang.get("user.wishlist.subtitle"));
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
            
            btnRemove = createPrimaryButton(Lang.get("btn.delete"));
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
                            setText(Lang.get("user.label.available"));
                        } else if ("TERBATAS".equals(status)) {
                            setForeground(new Color(251, 188, 5));
                            setText(Lang.get("user.label.limited"));
                        } else if ("HABIS".equals(status)) {
                            setForeground(new Color(234, 67, 53));
                            setText(Lang.get("user.label.out_of_stock"));
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
                
                btnRefresh.setText(String.format(Lang.get("user.label.total_books"), total));
                summaryLabel.setText(String.format(Lang.get("user.label.total_books"), total) + " | " + 
                    Lang.get("user.label.available") + ": " + available + " | " + 
                    Lang.get("user.label.limited") + ": " + limited + " | " + 
                    Lang.get("user.label.out_of_stock") + ": " + outOfStock);
                
                btnRemove.setEnabled(table.getSelectedRow() >= 0);
                
            } catch (Exception e) {
                e.printStackTrace();
                showErrorDialog("Error", "Gagal memuat wishlist: " + e.getMessage());
            }
        }

        void removeWish() {
            int r = table.getSelectedRow();
            if (r < 0) { 
                showMessageDialog(Lang.get("msg.warning"), Lang.get("msg.info")); 
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
            
            if (!confirmDialog(Lang.get("btn.delete"), 
                String.format(Lang.get("user.msg.confirm_remove_wishlist"), judul))) return;
            
            try {
                DB.exec("DELETE FROM wishlist WHERE user_id=? AND book_id=?", id(), bookId);
                DB.audit(id(), "DELETE", "wishlist", String.valueOf(bookId), "Hapus wishlist: " + judul);
                showMessageDialog(Lang.get("msg.success"), String.format(Lang.get("user.msg.success_remove_wishlist"), judul));
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
            Lang.get("loan.table.id"), Lang.get("loan.table.loandate"), Lang.get("loan.table.duedate"), Lang.get("table.status"), 
            Lang.get("table.total"), Lang.get("nav.staff"), Lang.get("return.table.returndate"), Lang.get("return.table.fine"), Lang.get("table.description")
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
            
            JLabel title = new JLabel(Lang.get("user.history.title"));
            title.setForeground(Utils.TEXT);
            title.setFont(new Font("Segoe UI", Font.BOLD, 24));
            
            JLabel subtitle = new JLabel(Lang.get("user.history.subtitle"));
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
            
            JButton detailBtn = createPrimaryButton(Lang.get("user.history.btn_detail"));
            
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
                            setText(Lang.get("status.active"));
                        } else if ("SELESAI".equals(value)) {
                            setForeground(new Color(52, 168, 83));
                            setText(Lang.get("status.finished"));
                        } else if ("BATAL".equals(value)) {
                            setForeground(new Color(234, 67, 53));
                            setText(Lang.get("status.cancelled"));
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
                    
                    // Localize keterangan logic
                    String statusStr = r.get("status");
                    String ket = r.get("keterangan");
                    if ("AKTIF".equals(statusStr)) {
                        if (ket.contains("Terlambat")) {
                            // Extract days if possible, or just use a generic localized string
                            ket = Lang.get("user.label.overdue_days").replace("%d", ket.replaceAll("[^0-9]", ""));
                        } else if (ket.contains("hari ini")) {
                            ket = Lang.get("user.label.due_today");
                        } else if (ket.contains("besok")) {
                            ket = Lang.get("user.label.due_tomorrow");
                        } else {
                            ket = Lang.get("status.active");
                        }
                    } else if ("SELESAI".equals(statusStr)) {
                        ket = Lang.get("status.finished");
                    } else if ("BATAL".equals(statusStr)) {
                        ket = Lang.get("status.cancelled");
                    }

                    model.addRow(new Object[]{ 
                        r.get("loan_id"), 
                        r.get("tanggal_pinjam"), 
                        r.get("jatuh_tempo"), 
                        statusStr, 
                        r.get("total_item"),
                        r.get("petugas"),
                        r.get("tanggal_kembali"),
                        fineTotal,
                        ket
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
                
                btnRefresh.setText(total + " " + Lang.get("nav.history"));
                summaryLabel.setText(Lang.get("table.total") + ": " + total + " | " + Lang.get("status.active") + ": " + aktif + 
                    " | " + Lang.get("status.finished") + ": " + selesai + " | " + Lang.get("status.cancelled") + ": " + batal + 
                    (terlambat > 0 ? " | ⚠️" + Lang.get("user.stat.overdue") + ": " + terlambat : ""));
                
            } catch (Exception e) {
                e.printStackTrace();
                showErrorDialog("Error", "Gagal memuat riwayat: " + e.getMessage());
            }
        }
        
        private void showLoanDetail() {
            int r = table.getSelectedRow();
            if (r < 0) { 
                showMessageDialog(Lang.get("msg.warning"), Lang.get("msg.info")); 
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
                detail.append("=== ").append(Lang.get("user.history.detail")).append(" ===\n");
                detail.append("Loan ID: ").append(loanId).append("\n\n");
                
                int totalBuku = 0;
                for (Map<String,String> item : items) {
                    detail.append("• ").append(item.get("code")).append(" - ")
                          .append(item.get("judul")).append("\n")
                          .append("  ").append(Lang.get("books.table.author")).append(": ").append(item.get("penulis")).append("\n")
                          .append("  ").append(Lang.get("books.table.publisher")).append(": ").append(item.get("penerbit")).append("\n")
                          .append("  ").append(Lang.get("table.total")).append(": ").append(item.get("qty")).append(" ").append(Lang.get("nav.books")).append("\n\n");
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

        NotifPanel() {
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
            
             JButton remindBtn = createPrimaryButton(Lang.get("user.quickaction.notification"));
            
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
                showMessageDialog(Lang.get("msg.warning"), Lang.get("msg.info")); 
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
}