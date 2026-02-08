package nahlib.petugas;

import nahlib.Lang;
import nahlib.DB;
import nahlib.LoginPage;
import nahlib.Utils;
import nahlib.CustomIcon;
import nahlib.SimpleChart;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

public class PetugasPage extends JFrame {

    private final Map<String,String> me;
    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);
    private final Map<String, JPanel> panels = new HashMap<>();
    private final JPanel navPanel;
    private JButton[] navButtons; // Declare as field
    
    private JLabel appLogo;
    private JLabel appTitle;

    public Map<String, String> getMe() {
        return me;
    }

    public PetugasPage(Map<String,String> me) {
        this.me = me;

        setTitle(Utils.getLibraryName() + " - " + Lang.get("petugas.dash.title"));
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
        
        JLabel subtitle = new JLabel(" | " + Lang.get("petugas.dash.title"));
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
        JLabel userIcon = new JLabel(new CustomIcon(CustomIcon.Type.STAFF, 20, new Color(52, 168, 83)));
        
        JLabel who = new JLabel(me.get("nama_lengkap"));
        who.setForeground(Utils.TEXT);
        who.setFont(Utils.FONT_B);
        
        JLabel role = new JLabel(Lang.get("role.staff"));
        role.setForeground(new Color(52, 168, 83)); // Green for petugas
        role.setFont(new Font("Segoe UI", Font.BOLD, 10));
        role.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(52, 168, 83)),
            new EmptyBorder(2, 6, 2, 6)
        ));
        
        userCard.add(userIcon);
        userCard.add(who);
        userCard.add(role);
        
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
            new PetugasPage(me).setVisible(true);
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
                DB.audit(Long.parseLong(me.get("user_id")), "LOGOUT", "users", me.get("user_id"), "Logout");
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
        
        DashboardPanel dash = new DashboardPanel(this);
        PeminjamanPanel pinjam = new PeminjamanPanel(this);
        PengembalianPanel kembali = new PengembalianPanel(this);
        LaporanSayaPanel laporan = new LaporanSayaPanel(this);
        NotifikasiPanel notif = new NotifikasiPanel(this);
        
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
        String[] labels = new String[]{
            Lang.get("nav.dashboard"), 
            Lang.get("nav.loan"), 
            Lang.get("nav.return"), 
            Lang.get("nav.reports"), 
            Lang.get("nav.notifications")
        };
        CustomIcon.Type[] icons = new CustomIcon.Type[]{
            CustomIcon.Type.DASHBOARD,
            CustomIcon.Type.BOOKS,
            CustomIcon.Type.HOME,
            CustomIcon.Type.REPORTS,
            CustomIcon.Type.AUDIT
        };
        navButtons = new JButton[labels.length]; // Array init before use
        navPanel = createBottomNav(labels, icons, 0);

        root.add(top, BorderLayout.NORTH);
        root.add(content, BorderLayout.CENTER);
        root.add(navPanel, BorderLayout.SOUTH);

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
        if (appLogo != null) {
            ImageIcon icon = Utils.getAppLogo(36);
            if (icon != null) {
                appLogo.setIcon(icon);
            }
        }
    }

    private JPanel createBottomNav(String[] labels, CustomIcon.Type[] icons, int activeIndex) {
        JPanel bar = new JPanel(new GridLayout(1, labels.length, 1, 0));
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(0, 0, 0, 0));
        
        for (int i = 0; i < labels.length; i++) {
            JButton btn = createNavButton(labels[i], icons[i], i == activeIndex);
            if (i < navButtons.length) {
                navButtons[i] = btn;
            }
            final int idx = i;
            btn.addActionListener(e -> {
                switchPanel(idx);
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
    
    public void switchPanel(int index) {
        String[] panelKeys = {"dash", "pinjam", "kembali", "laporan", "notif"};
        String key = panelKeys[index];
        
        // Update button highlights
        for (int i = 0; i < navButtons.length; i++) {
            if (navButtons[i] != null) {
                applyButtonStyle(navButtons[i], i == index);
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
    
    public void updateNavBadge(int index, int count) {
        if (navButtons != null && index >= 0 && index < navButtons.length && navButtons[index] != null) {
            String text = Lang.get("nav.notifications");
            if (count > 0) {
                text = Lang.get("nav.notifications") + " (" + count + ")";
            }
            navButtons[index].setText(text);
        }
    }
    
    // Helper methods
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
