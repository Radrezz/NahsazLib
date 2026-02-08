package nahlib.user;

import nahlib.Lang;
import nahlib.DB;
import nahlib.LoginPage;
import nahlib.Utils;
import nahlib.CustomIcon;
import nahlib.SimpleChart;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
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
        setUndecorated(true);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        JPanel root = Utils.createRootPanel(new BorderLayout());

        // =========== TOP BAR ===========
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
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
        
        JLabel subtitle = new JLabel(" | " + Lang.get("user.dash.title"));
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
        
        JButton logout = new JButton(Lang.get("btn.logout"), new CustomIcon(CustomIcon.Type.LOGOUT, 16, Color.WHITE));
        logout.setFont(Utils.FONT_B);
        logout.setForeground(Color.WHITE);
        logout.setBackground(new Color(230, 57, 70)); // Red color
        logout.setBorder(new EmptyBorder(8, 16, 8, 16));
        logout.setFocusPainted(false);
        logout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logout.addActionListener(e -> {
            if (confirmDialog(Lang.get("btn.logout"), Lang.get("msg.confirm_logout"))) {
                try {
                    DB.audit(Long.parseLong(me.get("user_id")), "LOGOUT", "users", me.get("user_id"), "Logout");
                } catch (Exception ex) {}
                dispose();
                new LoginPage();
            }
        });
        
        java.awt.event.MouseAdapter hoverGeneric = new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                ((JButton)evt.getSource()).setBackground(Utils.CARD2);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                ((JButton)evt.getSource()).setBackground(Utils.CARD);
            }
        };
        
        // Separate hover for logout button (red theme)
        logout.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                logout.setBackground(new Color(214, 40, 40)); // Darker red on hover
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                logout.setBackground(new Color(230, 57, 70)); // Original red
            }
        });
        
        refreshBtn.addMouseListener(hoverGeneric);
        langBtn.addMouseListener(hoverGeneric);

        rightTop.add(refreshBtn);
        rightTop.add(langBtn);
        rightTop.add(userCard);
        rightTop.add(logout);

        top.add(leftTop, BorderLayout.WEST);
        top.add(rightTop, BorderLayout.EAST);

        // =========== CONTENT PANELS ===========
        content.setBackground(Utils.BG);
        
        // Instantiate external panel classes with 'this' reference
        DashboardPanel dash = new DashboardPanel(this);
        BrowsePanel browse = new BrowsePanel(this);
        WishlistPanel wish = new WishlistPanel(this);
        RiwayatPanel hist = new RiwayatPanel(this);
        NotifPanel notif = new NotifPanel(this);
        
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
    
    // Public Getter for Me
    public Map<String,String> getMe() {
        return me;
    }
    
    // Public getter for Panels
    public JPanel getPanel(String key) {
        return panels.get(key);
    }
    
    public void refreshApp() {
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
        if (appLogo != null) {
            ImageIcon icon = Utils.getAppLogo(32); // Keep 32 for user page if desired, or 36
            if (icon != null) {
                appLogo.setIcon(icon);
            }
        }
    }

    public long id() { 
        return Long.parseLong(me.get("user_id")); 
    }

    public void showPanel(String key, int activeIndex) {
        cards.show(content, key);
        updateNavHighlight(activeIndex);
        
        if (key.equals("notif")) {
            int due = ((NotifPanel)panels.get("notif")).countDueSoonOrOverdue();
            updateNavBadge(4, due);
        }
    }
    
    private JPanel createBottomNav(String[] labels, CustomIcon.Type[] icons, Runnable[] actions, int activeIndex) {
        JPanel bar = new JPanel(new GridLayout(1, labels.length, 1, 0));
        bar.setOpaque(false);
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
    
    public void updateNavBadge(int index, int count) {
        JPanel root = (JPanel) getContentPane();
        BorderLayout layout = (BorderLayout) root.getLayout();
        Component southComp = layout.getLayoutComponent(BorderLayout.SOUTH);
        if (southComp instanceof JPanel) {
             JPanel nav = (JPanel) southComp;
             // Check if nav has component at index
             if (index >= 0 && index < nav.getComponentCount()) {
                 Component c = nav.getComponent(index);
                 if (c instanceof JButton) {
                     JButton notifBtn = (JButton) c;
                     String text = Lang.get("nav.notifications");
                     if (count > 0) text += " (" + count + ")";
                     notifBtn.setText(text);
                 }
             }
        }
    }
    
    public JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(Utils.FONT_B);
        btn.setForeground(Color.WHITE);
        btn.setBackground(Utils.ACCENT);
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
    
    public JButton createSecondaryButton(String text) {
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
    
    public boolean confirmDialog(String title, String message) {
        int result = JOptionPane.showConfirmDialog(this, message, title, 
            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        return result == JOptionPane.YES_OPTION;
    }
    
    public void showMessageDialog(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);
    }
    
    public void showErrorDialog(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }
}