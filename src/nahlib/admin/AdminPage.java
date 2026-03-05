package nahlib.admin;

import nahlib.Lang;
import nahlib.DB;
import nahlib.LoginPage;
import nahlib.Utils;
import nahlib.CustomIcon;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

public class AdminPage extends JFrame {

    private final Map<String,String> me;
    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);
    private final Map<String, JPanel> panels = new HashMap<>();
    private final JButton[] navButtons = new JButton[7]; // Store references to nav buttons
    private JLabel appLogo;
    private JLabel appTitle;

    // badge counts
    private int badgeOverdue = 0;

    public void setBadgeOverdue(int count) {
        this.badgeOverdue = count;
    }

    public int idValue() { 
        return Integer.parseInt(me.get("user_id")); 
    }

    public AdminPage(Map<String,String> me) {
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
        
        JLabel subtitle = new JLabel(" | " + Lang.get("admin.dash.title"));
        subtitle.setForeground(Utils.MUTED);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        leftTop.add(appLogo);
        leftTop.add(appTitle);
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
        
        // User icon using Graphics2D
        JLabel userIcon = new JLabel(new CustomIcon(CustomIcon.Type.STAFF, 20, Utils.ACCENT));
        
        JLabel who = new JLabel(me.get("nama_lengkap"));
        who.setForeground(Utils.TEXT);
        who.setFont(Utils.FONT_B);
        
        JLabel role = new JLabel(Lang.get("role.admin"));
        role.setForeground(Color.WHITE);
        role.setBackground(Utils.ACCENT);
        role.setOpaque(true);
        role.setFont(new Font("Segoe UI", Font.BOLD, 10));
        role.setBorder(new EmptyBorder(2, 6, 2, 6));
        
        userCard.add(userIcon);
        userCard.add(who);
        userCard.add(role);
        
        JButton logout = new JButton(Lang.get("btn.logout"), new CustomIcon(CustomIcon.Type.LOGOUT, 16, Color.WHITE));
        logout.setFont(Utils.FONT_B);
        logout.setForeground(Color.WHITE);
        logout.setBackground(new Color(230, 57, 70)); // Red color
        logout.setBorder(new EmptyBorder(8, 16, 8, 16));
        logout.setFocusPainted(false);
        logout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logout.addActionListener(e -> {
            if (confirmDialog(Lang.get("btn.logout"), Lang.get("msg.confirm_logout"))) {
                DB.audit(Long.valueOf(idValue()), "LOGOUT", "users", me.get("user_id"), "Logout");
                dispose();
                new LoginPage();
            }
        });
        
        // Hover effect for logout
        logout.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                logout.setBackground(new Color(214, 40, 40)); // Darker red on hover
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                logout.setBackground(new Color(230, 57, 70)); // Original red
            }
        });
        
        // Language Switcher Button
        String currentLangText = Lang.getLanguage() == 0 ? "ID" : "EN";
        JButton langBtn = new JButton(currentLangText);
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
            // Toggle language
            int newLang = Lang.getLanguage() == 0 ? 1 : 0;
            Lang.setLanguage(newLang);
            
            // Reload page
            dispose();
            new AdminPage(me).setVisible(true);
        });
        
        // Add Globe Icon to langBtn
        langBtn.setIcon(new CustomIcon(CustomIcon.Type.GLOBE, 16, Utils.TEXT));
        langBtn.setHorizontalTextPosition(SwingConstants.RIGHT);
        langBtn.setIconTextGap(8);
        
        // Hover effect for language button
        langBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                langBtn.setBackground(Utils.CARD2);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                langBtn.setBackground(Utils.CARD);
            }
        });

        // Global Refresh Button
        JButton refreshBtn = new JButton(Lang.get("btn.refresh"));
        refreshBtn.setFont(Utils.FONT_B);
        refreshBtn.setForeground(Utils.TEXT);
        refreshBtn.setBackground(Utils.CARD);
        refreshBtn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Utils.BORDER),
            new EmptyBorder(8, 16, 8, 16)
        ));
        refreshBtn.setFocusPainted(false);
        refreshBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshBtn.addActionListener(e -> refreshApp());
        
        // Hover effect for refresh button
        refreshBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) { refreshBtn.setBackground(Utils.CARD2); }
            public void mouseExited(java.awt.event.MouseEvent evt) { refreshBtn.setBackground(Utils.CARD); }
        });

        rightTop.add(refreshBtn);
        rightTop.add(langBtn);
        rightTop.add(userCard);
        rightTop.add(logout);

        top.add(leftTop, BorderLayout.WEST);
        top.add(rightTop, BorderLayout.EAST);

        // =========== CONTENT PANELS ===========
        content.setBackground(Utils.BG);
        
        final DashboardPanel dashboard = new DashboardPanel((AdminPage)this);
        final PetugasPanel petugas = new PetugasPanel((AdminPage)this);
        final AnggotaPanel anggota = new AnggotaPanel((AdminPage)this);
        final BukuPanel buku = new BukuPanel((AdminPage)this);
        final LaporanPanel laporan = new LaporanPanel((AdminPage)this);
        final SettingsPanel settings = new SettingsPanel((AdminPage)this);
        final AuditPanel audit = new AuditPanel((AdminPage)this);
        
        panels.put("dash", dashboard);
        panels.put("petugas", petugas);
        panels.put("anggota", anggota);
        panels.put("buku", buku);
        panels.put("laporan", laporan);
        panels.put("settings", settings);
        panels.put("audit", audit);
        
        content.add(dashboard, "dash");
        content.add(petugas, "petugas");
        content.add(anggota, "anggota");
        content.add(buku, "buku");
        content.add(laporan, "laporan");
        content.add(settings, "settings");
        content.add(audit, "audit");

        // =========== BOTTOM NAVIGATION ===========
        String[] labels = new String[]{
            Lang.get("nav.dashboard"), 
            Lang.get("nav.staff"), 
            Lang.get("nav.members"), 
            Lang.get("nav.books"), 
            Lang.get("nav.reports"), 
            Lang.get("nav.audit"),
            Lang.get("nav.settings")
        };
        
        // Nav Actions
        Runnable[] actions = new Runnable[]{
            () -> { dashboard.refresh(); show("dash", 0); },
            () -> { petugas.refresh(); show("petugas", 1); },
            () -> { anggota.refresh(); show("anggota", 2); },
            () -> { buku.refresh(); show("buku", 3); },
            () -> { laporan.refresh(); show("laporan", 4); },
            () -> { audit.refresh(); show("audit", 5); },
            () -> { settings.refresh(); show("settings", 6); }
        };
        
        // Icon types for each nav item
        CustomIcon.Type[] icons = new CustomIcon.Type[]{
            CustomIcon.Type.DASHBOARD,  // Dashboard
            CustomIcon.Type.STAFF,      // Staff
            CustomIcon.Type.USERS,      // Members
            CustomIcon.Type.BOOKS,      // Books
            CustomIcon.Type.REPORTS,    // Reports
            CustomIcon.Type.AUDIT,      // Activity Log
            CustomIcon.Type.SETTINGS    // Settings
        };
        
        JPanel nav = createBottomNav(labels, icons, actions, 0);

        // =========== ASSEMBLE ROOT ===========
        root.add(top, BorderLayout.NORTH);
        root.add(content, BorderLayout.CENTER);
        root.add(nav, BorderLayout.SOUTH);

        setContentPane(root);
        dashboard.refresh();
        setVisible(true);
    }


    private void show(String key, int activeIndex) {
        cards.show(content, key);
        updateNavHighlight(activeIndex);
    }
    
    private JPanel createBottomNav(String[] labels, CustomIcon.Type[] icons, Runnable[] actions, int activeIndex) {
        JPanel bar = new JPanel(new GridLayout(1, labels.length, 1, 0));
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(0, 0, 0, 0));
        
        for (int i = 0; i < labels.length; i++) {
            JButton btn = createNavButton(labels[i], icons[i], i == activeIndex);
            navButtons[i] = btn; // Store reference
            
            int idx = i;
            btn.addActionListener(e -> {
                actions[idx].run();
                // updateNavHighlight will be called by actions[idx] -> show(...)
            });
            bar.add(btn);
        }
        return bar;
    }
    
    private JButton createNavButton(String text, CustomIcon.Type iconType, boolean active) {
        JButton btn = new JButton(text);
        btn.putClientProperty("iconType", iconType); // Store icon type
        btn.setFont(Utils.FONT);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        applyButtonStyle(btn, active);
        
        // Hover effects
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                // Only change if not active (simple logic check, though active style overrides)
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
        
        // Icon color update (recreate icon from stored type)
        CustomIcon.Type icType = (CustomIcon.Type) btn.getClientProperty("iconType");
        if (icType == null) icType = CustomIcon.Type.DASHBOARD;
        
        btn.setIcon(new CustomIcon(icType, 18, active ? Color.WHITE : Utils.MUTED));
    }
    
    // Refresh Global Assets & Active Panel
    public void refreshApp() {
        // 1. Update Logo & Title
        updateLogo();
        if (appTitle != null) appTitle.setText(Utils.getLibraryName());
        
        // 2. Refresh active panels via reflection
        for (JPanel p : panels.values()) {
            if (p.isVisible()) {
                try {
                    // Call void refresh() method if exists
                    p.getClass().getMethod("refresh").invoke(p);
                } catch (Exception e) {
                    // Panel might not have refresh method or is inaccessible
                }
            }
        }
        
        // 3. Repaint top bar
        if (appLogo != null) {
            appLogo.revalidate();
            appLogo.repaint();
        }
    }
    
    private void updateLogo() {
        if (appLogo != null) {
            ImageIcon icon = Utils.getAppLogo(36);
            if (icon != null) {
                appLogo.setIcon(icon);
            }
        }
    }

    private void updateNavHighlight(int activeIndex) {
        for (int i = 0; i < navButtons.length; i++) {
            if (navButtons[i] != null) {
                applyButtonStyle(navButtons[i], i == activeIndex);
            }
        }
    }
    
    public void updateNavBadge(int index, int count) {
        if (index >= 0 && index < navButtons.length && navButtons[index] != null) {
            String label = navButtons[index].getText().replaceAll(" \\(\\d+\\)", "");
            if (count > 0) label += " (" + count + ")";
            navButtons[index].setText(label);
        }
    }
    
    // Helper methods for consistent UI
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