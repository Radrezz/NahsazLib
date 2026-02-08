package nahlib.admin;

import nahlib.Lang;
import nahlib.DB;
import nahlib.LoginPage;
import nahlib.Utils;
import nahlib.CustomIcon;
import nahlib.SimpleChart;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Date;

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

    public AdminPage(Map<String,String> me) {
        this.me = me;

        setTitle(Utils.getLibraryName() + " - " + Lang.get("user.dash.title"));
        setSize(1280, 780);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        
        // Tambahkan kode ini untuk maximize
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
        
        JLabel subtitle = new JLabel(" • " + Lang.get("admin.dash.title"));
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
        
        JButton logout = new JButton(Lang.get("btn.logout"), new CustomIcon(CustomIcon.Type.LOGOUT, 16, Utils.TEXT));
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
            if (confirmDialog(Lang.get("btn.logout"), Lang.get("msg.confirm_logout"))) {
                DB.audit(Long.valueOf(id()), "LOGOUT", "users", me.get("user_id"), "Logout");
                dispose();
                new LoginPage();
            }
        });
        
        // Hover effect for logout
        logout.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                logout.setBackground(Utils.CARD2);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                logout.setBackground(Utils.CARD);
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
        
        DashboardPanel dashboard = new DashboardPanel();
        PetugasPanel petugas = new PetugasPanel();
        AnggotaPanel anggota = new AnggotaPanel();
        BukuPanel buku = new BukuPanel();
        LaporanPanel laporan = new LaporanPanel();
        SettingsPanel settings = new SettingsPanel();
        AuditPanel audit = new AuditPanel();
        
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

    private int id() { 
        return Integer.parseInt(me.get("user_id")); 
    }

    private void show(String key, int activeIndex) {
        cards.show(content, key);
        updateNavHighlight(activeIndex);
    }
    
    private JPanel createBottomNav(String[] labels, CustomIcon.Type[] icons, Runnable[] actions, int activeIndex) {
        JPanel bar = new JPanel(new GridLayout(1, labels.length, 1, 0));
        bar.setBackground(Utils.BG);
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
    private void refreshApp() {
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
        try {
            // First try loading from file system (user uploaded)
            File f = new File("src/nahlib/nahsazlibrary.png");
            if (f.exists()) {
                ImageIcon ic = new ImageIcon(f.getAbsolutePath());
                // Flush to ensure no caching of old image
                ic.getImage().flush();
                if (appLogo != null) {
                    appLogo.setIcon(new ImageIcon(Utils.makeCircularImage(ic.getImage(), 32)));
                }
            } else {
                // Fallback to resource
                java.net.URL imgURL = getClass().getResource("/nahlib/nahsazlibrary.png");
                if (imgURL != null) {
                    ImageIcon ic = new ImageIcon(imgURL);
                    if (appLogo != null) {
                        appLogo.setIcon(new ImageIcon(Utils.makeCircularImage(ic.getImage(), 32)));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateNavHighlight(int activeIndex) {
        for (int i = 0; i < navButtons.length; i++) {
            if (navButtons[i] != null) {
                applyButtonStyle(navButtons[i], i == activeIndex);
            }
        }
    }
    
    // Helper methods for consistent UI
    JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(Utils.FONT_B);
        btn.setForeground(Color.WHITE);
        btn.setBackground(Utils.ACCENT);
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
    
    JButton createSecondaryButton(String text) {
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
    
    boolean confirmDialog(String title, String message) {
        int result = JOptionPane.showConfirmDialog(this, message, title, 
            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        return result == JOptionPane.YES_OPTION;
    }
    
    void showMessageDialog(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);
    }
    
    void showErrorDialog(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }

    // ========================= PANELS =========================

    class DashboardPanel extends JPanel {
        private final JLabel lbBooks = createStatLabel("0");
        private final JLabel lbMembers = createStatLabel("0");
        private final JLabel lbActive = createStatLabel("0");
        private final JLabel lbOverdue = createStatLabel("0");
        
        private SimpleChart barChart;
        private SimpleChart lineChart;

        DashboardPanel() {
            setBackground(Utils.BG);
            setLayout(new GridBagLayout()); // Bento Layout
            
            // Header
            JPanel header = new JPanel(new BorderLayout());
            header.setBackground(Utils.BG);
            header.setBorder(new EmptyBorder(20, 30, 10, 30));
            
            JLabel title = new JLabel(Lang.get("admin.dash.title"), new CustomIcon(CustomIcon.Type.DASHBOARD, 24, Utils.ACCENT), SwingConstants.LEFT);
            title.setForeground(Utils.TEXT);
            title.setFont(new Font("Segoe UI", Font.BOLD, 26));
            title.setIconTextGap(12);
            
            JLabel subtitle = new JLabel(Lang.get("admin.dash.subtitle"));
            subtitle.setForeground(Utils.MUTED);
            subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            
            JPanel titlePanel = new JPanel(new BorderLayout());
            titlePanel.setOpaque(false);
            titlePanel.add(title, BorderLayout.NORTH);
            titlePanel.add(subtitle, BorderLayout.SOUTH);
            
            header.add(titlePanel, BorderLayout.WEST);
            
            // Add Header (Top, Span 3)
            addGB(header, 0, 0, 3, 1, 1.0, 0.0);

            // === BENTO GRID CONTENT ===
            // Stats Block (Left Side - 2x2 Grid)
            // Stat 1: Books (Top Left)
            addGB(createStatCard(Lang.get("admin.dash.books"), lbBooks, Utils.ACCENT), 0, 1, 1, 1, 0.25, 0.25);
            // Stat 2: Members (Top Middle)
            addGB(createStatCard(Lang.get("admin.dash.members"), lbMembers, new Color(52, 168, 83)), 1, 1, 1, 1, 0.25, 0.25);
            // Stat 3: Active Loans (Bottom Left)
            addGB(createStatCard(Lang.get("admin.dash.active_loans"), lbActive, new Color(251, 188, 5)), 0, 2, 1, 1, 0.25, 0.25);
            // Stat 4: Overdue (Bottom Middle)
            addGB(createStatCard(Lang.get("admin.dash.overdue"), lbOverdue, new Color(234, 67, 53)), 1, 2, 1, 1, 0.25, 0.25);

            // Charts
            barChart = new SimpleChart(SimpleChart.Type.BAR, Utils.ACCENT);
            lineChart = new SimpleChart(SimpleChart.Type.LINE, new Color(52, 168, 83));

            // Main Chart (Right Side - Tall - Spans 2 Rows)
            JPanel barWrapper = Utils.card();
            barWrapper.setLayout(new BorderLayout());
            JLabel barTitle = new JLabel(Lang.get("admin.dash.loan_trend"));
            barTitle.setForeground(Utils.TEXT);
            barTitle.setFont(Utils.FONT_B);
            barTitle.setBorder(new EmptyBorder(0,0,10,0));
            barWrapper.add(barTitle, BorderLayout.NORTH);
            barWrapper.add(barChart, BorderLayout.CENTER);
            
            addGB(barWrapper, 2, 1, 1, 2, 0.5, 0.5); // Spans 2 rows on the right
            
            // Secondary Chart (Bottom - Wide - Spans 3 Cols)
            JPanel lineWrapper = Utils.card();
            lineWrapper.setLayout(new BorderLayout());
            JLabel lineTitle = new JLabel(Lang.get("admin.dash.user_trend"));
            lineTitle.setForeground(Utils.TEXT);
            lineTitle.setFont(Utils.FONT_B);
            lineTitle.setBorder(new EmptyBorder(0,0,10,0));
            lineWrapper.add(lineTitle, BorderLayout.NORTH);
            lineWrapper.add(lineChart, BorderLayout.CENTER);
            
            addGB(lineWrapper, 0, 3, 3, 1, 1.0, 0.4); // Wide chart at bottom
        }
        
        // Helper for GridBagLayout
        private void addGB(Component comp, int x, int y, int w, int h, double wx, double wy) {
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = x;
            gbc.gridy = y;
            gbc.gridwidth = w;
            gbc.gridheight = h;
            gbc.weightx = wx;
            gbc.weighty = wy;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.insets = new Insets(8, 8, 8, 8); // Padding between bento boxes
            if (x == 0) gbc.insets.left = 30; // Left margin
            if (x + w == 3) gbc.insets.right = 30; // Right margin for last col (assuming 3 cols total)
            if (y == 3) gbc.insets.bottom = 30; // Bottom margin
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
                BorderFactory.createMatteBorder(0, 0, 0, 0, Utils.BORDER), // Clean border
                new EmptyBorder(20, 20, 20, 20)
            ));
            
            // Modern "Chip" styling (Rounded accent indicator)
            JPanel headerObj = new JPanel(new BorderLayout());
            headerObj.setOpaque(false);
            
            JLabel titleLabel = new JLabel(title.toUpperCase());
            titleLabel.setForeground(Utils.MUTED);
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
            titleLabel.setBorder(new EmptyBorder(0, 0, 5, 0));
            
            JPanel dot = new JPanel();
            dot.setPreferredSize(new Dimension(8, 8));
            dot.setBackground(accentColor);
            dot.setBorder(new EmptyBorder(4, 4, 4, 4)); // Pseudo-rounding
            
            headerObj.add(titleLabel, BorderLayout.CENTER);
            // headerObj.add(dot, BorderLayout.EAST); // Optional dot
            
            card.add(headerObj, BorderLayout.NORTH);
            card.add(value, BorderLayout.CENTER);
            
            // Add a subtle bottom border with accent color
            JPanel bottomBar = new JPanel();
            bottomBar.setBackground(accentColor);
            bottomBar.setPreferredSize(new Dimension(0, 3));
            card.add(bottomBar, BorderLayout.SOUTH);
            
            return card;
        }

        void refresh() {
            try {
                int books = Integer.parseInt(DB.query("SELECT COUNT(*) c FROM books").get(0).get("c"));
                int members = Integer.parseInt(DB.query("SELECT COUNT(*) c FROM users WHERE role='USER'").get(0).get("c"));
                int active = Integer.parseInt(DB.query("SELECT COUNT(*) c FROM loans WHERE status='AKTIF'").get(0).get("c"));
                int overdue = Integer.parseInt(DB.query("SELECT COUNT(*) c FROM loans WHERE status='AKTIF' AND jatuh_tempo < CURDATE()").get(0).get("c"));

                lbBooks.setText(String.valueOf(books));
                lbMembers.setText(String.valueOf(members));
                lbActive.setText(String.valueOf(active));
                lbOverdue.setText(String.valueOf(overdue));

                // Dummy data for charts
                List<String> months = List.of("Jan", "Feb", "Mar", "Apr", "May", "Jun");
                List<Double> loanData = List.of(12.0, 19.0, 15.0, 25.0, 22.0, 30.0);
                List<Double> userData = List.of(5.0, 8.0, 15.0, 20.0, 25.0, 35.0);
                
                barChart.setData(months, loanData);
                lineChart.setData(months, userData);

                badgeOverdue = overdue;
                if (overdue > 0) {
                     lbOverdue.setForeground(new Color(234, 67, 53));
                } else {
                     lbOverdue.setForeground(Utils.TEXT);
                }
            } catch (Exception ignored) {
                ignored.printStackTrace();
            }
        }
    }


    class PetugasPanel extends JPanel {
        DefaultTableModel model = new DefaultTableModel(new String[]{
            "ID", Lang.get("staff.table.username"), Lang.get("staff.table.name"), 
            Lang.get("label.gender"), Lang.get("staff.form.address"), 
            Lang.get("staff.table.phone"), Lang.get("staff.table.email"), 
            Lang.get("staff.table.status")
        }, 0);
        JTable table = new JTable(model);
        private JButton btnRefresh;
        private JTextField searchField;

        PetugasPanel() {
            setBackground(Utils.BG);
            setLayout(new BorderLayout());
            
            // Header dengan live search
            JPanel header = createPanelHeader(Lang.get("staff.title"), 
                Lang.get("staff.subtitle"));
            
            // Table
            styleTable();
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
            searchField.putClientProperty("JTextField.placeholderText", Lang.get("petugas.search.placeholder"));
            
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
            
            btnRefresh = new JButton(Lang.get("btn.refresh"));
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
            
            searchPanel.add(btnRefresh);
            
            header.add(titlePanel, BorderLayout.WEST);
            header.add(searchPanel, BorderLayout.EAST);
            
            return header;
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
                    
                    if (column == 7) { // Status column
                        if ("Aktif".equals(value)) {
                            setForeground(new Color(52, 168, 83));
                        } else if ("Nonaktif".equals(value)) {
                            setForeground(new Color(234, 67, 53));
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
            
            JButton add = createPrimaryButton(Lang.get("staff.add_title"));
            JButton edit = createSecondaryButton(Lang.get("btn.edit"));
            JButton activate = createSecondaryButton(Lang.get("staff.btn.activate"));
            JButton deactivate = createSecondaryButton(Lang.get("staff.btn.deactivate"));
            JButton reset = createSecondaryButton(Lang.get("staff.btn.reset_password"));
            
            add.addActionListener(e -> openForm(null));
            edit.addActionListener(e -> {
                int r = table.getSelectedRow();
                if (r < 0) { 
                    showMessageDialog(Lang.get("msg.warning"), Lang.get("msg.info")); 
                    return; 
                }
                int modelRow = table.convertRowIndexToModel(r);
                openForm(getRowData(modelRow));
            });
            activate.addActionListener(e -> setPetugasStatus(true));
            deactivate.addActionListener(e -> setPetugasStatus(false));
            reset.addActionListener(e -> resetPassword());
            
            actions.add(add);
            actions.add(edit);
            actions.add(activate);
            actions.add(deactivate);
            actions.add(reset);
            
            return actions;
        }
        
        private Map<String,String> getRowData(int row) {
            // PERBAIKAN: Gunakan HashMap untuk banyak parameter
            Map<String, String> data = new HashMap<>();
            data.put("user_id", String.valueOf(model.getValueAt(row,0)));
            data.put("username", String.valueOf(model.getValueAt(row,1)));
            data.put("nama_lengkap", String.valueOf(model.getValueAt(row,2)));
            data.put("gender", String.valueOf(model.getValueAt(row,3)));
            data.put("alamat", String.valueOf(model.getValueAt(row,4)));
            data.put("no_telp", String.valueOf(model.getValueAt(row,5)));
            data.put("email", String.valueOf(model.getValueAt(row,6)));
            data.put("status_aktif", Lang.get("staff.status.active").equals(model.getValueAt(row,7)) ? "1":"0");
            return data;
        }

        void refresh() {
            try {
                model.setRowCount(0);
                var rows = DB.query(
                    "SELECT user_id,username,nama_lengkap,gender,alamat,no_telp,email,status_aktif " +
                    "FROM users WHERE role='PETUGAS' ORDER BY user_id DESC"
                );
                for (var r: rows) {
                    model.addRow(new Object[]{
                        r.get("user_id"), 
                        r.get("username"), 
                        r.get("nama_lengkap"),
                        r.get("gender") == null ? "" : r.get("gender"),
                        r.get("alamat") == null ? "" : r.get("alamat"),
                        r.get("no_telp") == null ? "" : r.get("no_telp"),
                        r.get("email") == null ? "" : r.get("email"),
                        "1".equals(r.get("status_aktif")) ? Lang.get("staff.status.active"):Lang.get("staff.status.inactive")
                    });
                }
                btnRefresh.setText(Lang.get("btn.refresh") + " (" + rows.size() + " " + Lang.get("nav.staff").toLowerCase() + ")");
            } catch (Exception ignored) {
                ignored.printStackTrace();
            }
        }
        
        private void setPetugasStatus(boolean active) {
            int r = table.getSelectedRow();
            if (r < 0) { 
                showMessageDialog(Lang.get("msg.warning"), Lang.get("msg.info")); 
                return; 
            }
            
            int modelRow = table.convertRowIndexToModel(r);
            String id = String.valueOf(model.getValueAt(modelRow,0));
            String nama = String.valueOf(model.getValueAt(modelRow,2));
            String currentStatus = String.valueOf(model.getValueAt(modelRow,7));
            
            if ((active && "Aktif".equals(currentStatus)) || (!active && "Nonaktif".equals(currentStatus))) {
                showMessageDialog(Lang.get("msg.info"), Lang.get("msg.info"));
                return;
            }
            
            if (!confirmDialog(Lang.get("msg.confirm"), (active ? Lang.get("staff.btn.activate") : Lang.get("staff.btn.deactivate")) + " petugas:\n" + nama + "?")) return;
            
            try {
                DB.exec("UPDATE users SET status_aktif=? WHERE user_id=? AND role='PETUGAS'", active?1:0, id);
                DB.audit(Long.valueOf(AdminPage.this.id()), "UPDATE", "users", id, 
                    (active?"Aktifkan":"Nonaktifkan")+" petugas");
                refresh();
                showMessageDialog(Lang.get("msg.success"), Lang.get("msg.success"));
            } catch (Exception ex) { 
                showErrorDialog(Lang.get("msg.error"), Lang.get("msg.error")); 
            }
        }
        
        private void resetPassword() {
            int r = table.getSelectedRow();
            if (r < 0) { 
                showMessageDialog(Lang.get("msg.warning"), Lang.get("msg.info")); 
                return; 
            }
            
            int modelRow = table.convertRowIndexToModel(r);
            String id = String.valueOf(model.getValueAt(modelRow,0));
            String nama = String.valueOf(model.getValueAt(modelRow,2));
            
            String np = JOptionPane.showInputDialog(this, 
                Lang.get("staff.btn.reset_password") + ":\n" + nama, 
                Lang.get("staff.btn.reset_password"), 
                JOptionPane.QUESTION_MESSAGE);
            
            if (np == null || np.trim().isEmpty()) return;
            
            try {
                DB.exec("UPDATE users SET password_hash=? WHERE user_id=? AND role='PETUGAS'", 
                    Utils.sha256(np), id);
                DB.audit(Long.valueOf(AdminPage.this.id()), "UPDATE", "users", id, "Reset password petugas");
                showMessageDialog("Sukses", "Password berhasil direset.");
            } catch (Exception ex) { 
                showErrorDialog("Error", "Gagal mereset password."); 
            }
        }

        void openForm(Map<String,String> data) {
            JDialog d = new JDialog(AdminPage.this, true);
            d.setTitle(data==null ? "Tambah Petugas Baru":"Edit Data Petugas");
            d.setSize(550, 550);
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
            
            JLabel title = new JLabel(data==null ? "Tambah Petugas Baru" : "Edit Data Petugas");
            title.setForeground(Utils.TEXT);
            title.setFont(new Font("Segoe UI", Font.BOLD, 18));
            title.setBorder(new EmptyBorder(0, 0, 20, 0));
            
            JPanel fields = new JPanel(new GridLayout(7, 1, 10, 15));
            fields.setOpaque(false);
            
            JTextField u = Utils.input("username");
            JPasswordField pw = Utils.passInput("password (isi jika tambah/reset)");
            JTextField nama = Utils.input("nama lengkap");
            
            JComboBox<String> genderCombo = new JComboBox<>(new String[]{"", "Laki-laki", "Perempuan"});
            genderCombo.setBackground(Utils.CARD2);
            genderCombo.setForeground(Utils.TEXT);
            genderCombo.setBorder(BorderFactory.createLineBorder(Utils.BORDER));
            
            JTextField alamat = Utils.input("alamat");
            JTextField telp = Utils.input("no telp");
            Utils.numericOnly(telp);
            JTextField email = Utils.input("email");
            
            if (data != null) {
                u.setText(data.get("username"));
                nama.setText(data.get("nama_lengkap"));
                telp.setText(data.get("no_telp"));
                email.setText(data.get("email"));
                if (data.get("gender") != null) {
                    genderCombo.setSelectedItem(data.get("gender"));
                }
                if (data.get("alamat") != null) {
                    alamat.setText(data.get("alamat"));
                }
            }
            
            fields.add(createFormRow("Username*", u));
            fields.add(createFormRow("Password", pw));
            fields.add(createFormRow("Nama Lengkap*", nama));
            fields.add(createFormRow("Gender", genderCombo));
            fields.add(createFormRow("Alamat", alamat));
            fields.add(createFormRow("No. Telepon", telp));
            fields.add(createFormRow("Email", email));
            
            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            buttons.setOpaque(false);
            
            JButton cancel = createSecondaryButton("Batal");
            JButton save = createPrimaryButton(data==null ? "Simpan" : "Update");
            
            cancel.addActionListener(e -> dialog.dispose());
            save.addActionListener(e -> savePetugas(data, u, pw, nama, genderCombo, alamat, telp, email, dialog));
            
            buttons.add(cancel);
            buttons.add(save);
            
            panel.add(title, BorderLayout.NORTH);
            panel.add(fields, BorderLayout.CENTER);
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
        
        private void savePetugas(Map<String,String> data, JTextField u, JPasswordField pw, 
                                JTextField nama, JComboBox<String> genderCombo, 
                                JTextField alamat, JTextField telp, JTextField email, JDialog dialog) {
            try {
                if (u.getText().trim().isEmpty() || nama.getText().trim().isEmpty()) { 
                    showMessageDialog("Peringatan", "Username dan nama lengkap wajib diisi."); 
                    return; 
                }
                
                String gender = genderCombo.getSelectedItem() != null ? 
                               genderCombo.getSelectedItem().toString() : "";
                
                if (data == null) {
                    if (new String(pw.getPassword()).isEmpty()) { 
                        showMessageDialog("Peringatan", "Password wajib untuk petugas baru."); 
                        return; 
                    }
                    long idNew = DB.exec(
                        "INSERT INTO users(username,password_hash,role,nama_lengkap,gender,alamat,no_telp,email,status_aktif) " +
                        "VALUES (?,?,?,?,?,?,?,?,1)",
                        u.getText().trim(), 
                        Utils.sha256(new String(pw.getPassword())), 
                        "PETUGAS",
                        nama.getText().trim(),
                        gender.isEmpty() ? null : gender,
                        alamat.getText().trim().isEmpty() ? null : alamat.getText().trim(),
                        telp.getText().trim().isEmpty() ? null : telp.getText().trim(),
                        email.getText().trim().isEmpty() ? null : email.getText().trim()
                    );
                    DB.audit(Long.valueOf(AdminPage.this.id()), "CREATE", "users", String.valueOf(idNew), "Tambah petugas");
                    showMessageDialog("Sukses", "Petugas berhasil ditambahkan.");
                } else {
                    DB.exec("UPDATE users SET username=?, nama_lengkap=?, gender=?, alamat=?, no_telp=?, email=? WHERE user_id=? AND role='PETUGAS'",
                        u.getText().trim(), 
                        nama.getText().trim(),
                        gender.isEmpty() ? null : gender,
                        alamat.getText().trim().isEmpty() ? null : alamat.getText().trim(),
                        telp.getText().trim().isEmpty() ? null : telp.getText().trim(),
                        email.getText().trim().isEmpty() ? null : email.getText().trim(),
                        data.get("user_id")
                    );
                    if (!new String(pw.getPassword()).isEmpty()) {
                        DB.exec("UPDATE users SET password_hash=? WHERE user_id=?", 
                            Utils.sha256(new String(pw.getPassword())), data.get("user_id"));
                    }
                    DB.audit(Long.valueOf(AdminPage.this.id()), "UPDATE", "users", data.get("user_id"), "Edit petugas");
                    showMessageDialog("Sukses", "Data petugas berhasil diperbarui.");
                }
                refresh();
                dialog.dispose();
            } catch (Exception ex) {
                ex.printStackTrace();
                showErrorDialog("Error", "Gagal menyimpan. Username mungkin sudah digunakan.");
            }
        }
    }

    class AnggotaPanel extends JPanel {
        DefaultTableModel model = new DefaultTableModel(new String[]{
            "ID", Lang.get("staff.table.username"), Lang.get("staff.table.name"), 
            "Kelas", Lang.get("label.gender"), Lang.get("staff.form.address"), 
            Lang.get("staff.table.phone"), Lang.get("staff.table.email"), 
            Lang.get("staff.table.status")
        }, 0);
        JTable table = new JTable(model);
        private JButton btnRefresh;
        private JTextField searchField;

        AnggotaPanel() {
            setBackground(Utils.BG);
            setLayout(new BorderLayout());
            
            // Header dengan live search
            JPanel header = createPanelHeader("Kelola Anggota", 
                "Kelola data anggota perpustakaan");
            
            // Table
            styleTable();
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
            searchField.putClientProperty("JTextField.placeholderText", Lang.get("petugas.search.placeholder"));
            
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
            
            JLabel searchIcon = new JLabel("Cari");
            searchIcon.setFont(new Font("Segoe UI", Font.PLAIN, 14));
             searchIcon.setForeground(Utils.TEXT);
            
            searchPanel.add(searchIcon);
            searchPanel.add(searchField);
            
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
            
            searchPanel.add(btnRefresh);
            
            header.add(titlePanel, BorderLayout.WEST);
            header.add(searchPanel, BorderLayout.EAST);
            
            return header;
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
                    
                    if (column == 8) { // Status column
                        if ("Aktif".equals(value)) {
                            setForeground(new Color(52, 168, 83));
                        } else if ("Nonaktif".equals(value)) {
                            setForeground(new Color(234, 67, 53));
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
            
            JButton edit = createSecondaryButton(Lang.get("btn.edit"));
            JButton activate = createSecondaryButton(Lang.get("staff.btn.activate"));
            JButton deactivate = createSecondaryButton(Lang.get("staff.btn.deactivate"));
            JButton reset = createSecondaryButton(Lang.get("staff.btn.reset_password"));
            
            edit.addActionListener(e -> {
                int r = table.getSelectedRow();
                if (r < 0) { 
                    showMessageDialog(Lang.get("msg.warning"), Lang.get("msg.info")); 
                    return; 
                }
                int modelRow = table.convertRowIndexToModel(r);
                openForm(getRowData(modelRow));
            });
            activate.addActionListener(e -> setAnggotaStatus(true));
            deactivate.addActionListener(e -> setAnggotaStatus(false));
            reset.addActionListener(e -> resetPassword());
            
            actions.add(edit);
            actions.add(activate);
            actions.add(deactivate);
            actions.add(reset);
            
            return actions;
        }
        
        private Map<String,String> getRowData(int row) {
            // PERBAIKAN: Gunakan HashMap untuk banyak parameter
            Map<String, String> data = new HashMap<>();
            data.put("user_id", String.valueOf(model.getValueAt(row,0)));
            data.put("username", String.valueOf(model.getValueAt(row,1)));
            data.put("nama_lengkap", String.valueOf(model.getValueAt(row,2)));
            data.put("kelas", String.valueOf(model.getValueAt(row,3)));
            data.put("gender", String.valueOf(model.getValueAt(row,4)));
            data.put("alamat", String.valueOf(model.getValueAt(row,5)));
            data.put("no_telp", String.valueOf(model.getValueAt(row,6)));
            data.put("email", String.valueOf(model.getValueAt(row,7)));
            return data;
        }

        void refresh() {
            try {
                model.setRowCount(0);
                var rows = DB.query(
                    "SELECT user_id,username,nama_lengkap,kelas,gender,alamat,no_telp,email,status_aktif " +
                    "FROM users WHERE role='USER' ORDER BY user_id DESC"
                );
                for (var r: rows) {
                    model.addRow(new Object[]{
                        r.get("user_id"), 
                        r.get("username"), 
                        r.get("nama_lengkap"),
                        r.get("kelas") == null ? "" : r.get("kelas"),
                        r.get("gender") == null ? "" : r.get("gender"),
                        r.get("alamat") == null ? "" : r.get("alamat"),
                        r.get("no_telp") == null ? "" : r.get("no_telp"),
                        r.get("email") == null ? "" : r.get("email"),
                        "1".equals(r.get("status_aktif")) ? "Aktif":"Nonaktif"
                    });
                }
                btnRefresh.setText("Refresh (" + rows.size() + " anggota)");
            } catch (Exception ignored) {}
        }
        
        private void setAnggotaStatus(boolean active) {
            int r = table.getSelectedRow();
            if (r < 0) { 
                showMessageDialog("Peringatan", "Pilih anggota terlebih dahulu."); 
                return; 
            }
            
            int modelRow = table.convertRowIndexToModel(r);
            String id = String.valueOf(model.getValueAt(modelRow,0));
            String nama = String.valueOf(model.getValueAt(modelRow,2));
            String currentStatus = String.valueOf(model.getValueAt(modelRow,8));
            
            if ((active && "Aktif".equals(currentStatus)) || (!active && "Nonaktif".equals(currentStatus))) {
                showMessageDialog("Informasi", "Status sudah sesuai.");
                return;
            }
            
            if (!confirmDialog(Lang.get("msg.confirm"), (active ? Lang.get("staff.btn.activate") : Lang.get("staff.btn.deactivate")) + " anggota:\n" + nama + "?")) return;
            
            try {
                DB.exec("UPDATE users SET status_aktif=? WHERE user_id=? AND role='USER'", active?1:0, id);
                DB.audit(Long.valueOf(AdminPage.this.id()), "UPDATE", "users", id, 
                    (active?"Aktifkan":"Nonaktifkan")+" anggota");
                refresh();
                showMessageDialog(Lang.get("msg.success"), Lang.get("msg.success"));
            } catch (Exception ex) { 
                showErrorDialog(Lang.get("msg.error"), Lang.get("msg.error")); 
            }
        }
        
        private void resetPassword() {
            int r = table.getSelectedRow();
            if (r < 0) { 
                showMessageDialog("Peringatan", "Pilih anggota terlebih dahulu."); 
                return; 
            }
            
            int modelRow = table.convertRowIndexToModel(r);
            String id = String.valueOf(model.getValueAt(modelRow,0));
            String nama = String.valueOf(model.getValueAt(modelRow,2));
            
            String np = JOptionPane.showInputDialog(this, 
                Lang.get("staff.btn.reset_password") + ":\n" + nama, 
                Lang.get("staff.btn.reset_password"), 
                JOptionPane.QUESTION_MESSAGE);
            
            if (np == null || np.trim().isEmpty()) return;
            
            try {
                DB.exec("UPDATE users SET password_hash=? WHERE user_id=? AND role='USER'", 
                    Utils.sha256(np), id);
                DB.audit(Long.valueOf(AdminPage.this.id()), "UPDATE", "users", id, "Reset password anggota");
                showMessageDialog(Lang.get("msg.success"), Lang.get("msg.success"));
            } catch (Exception ex) { 
                showErrorDialog(Lang.get("msg.error"), Lang.get("msg.error")); 
            }
        }

        void openForm(Map<String,String> data) {
            JDialog d = new JDialog(AdminPage.this, true);
            d.setTitle("Edit Data Anggota");
            d.setSize(550, 600);
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
            
            JLabel title = new JLabel("Edit Data Anggota");
            title.setForeground(Utils.TEXT);
            title.setFont(new Font("Segoe UI", Font.BOLD, 18));
            title.setBorder(new EmptyBorder(0, 0, 20, 0));
            
            JPanel fields = new JPanel(new GridLayout(8, 1, 10, 15));
            fields.setOpaque(false);
            
            JTextField u = Utils.input("username");
            JTextField nama = Utils.input("nama lengkap");
            JTextField kelas = Utils.input("kelas");
            
            JComboBox<String> genderCombo = new JComboBox<>(new String[]{"", "Laki-laki", "Perempuan"});
            genderCombo.setBackground(Utils.CARD2);
            genderCombo.setForeground(Utils.TEXT);
            genderCombo.setBorder(BorderFactory.createLineBorder(Utils.BORDER));
            
            JTextField alamat = Utils.input("alamat");
            JTextField telp = Utils.input("no telp");
            Utils.numericOnly(telp);
            JTextField email = Utils.input("email");
            
            u.setText(data.get("username"));
            nama.setText(data.get("nama_lengkap"));
            kelas.setText(data.get("kelas"));
            telp.setText(data.get("no_telp"));
            
            if (data.get("gender") != null) {
                genderCombo.setSelectedItem(data.get("gender"));
            }
            if (data.get("alamat") != null) {
                alamat.setText(data.get("alamat"));
            }
            if (data.get("email") != null) {
                email.setText(data.get("email"));
            }
            
            fields.add(createFormRow("Username*", u));
            fields.add(createFormRow("Nama Lengkap*", nama));
            fields.add(createFormRow("Kelas", kelas));
            fields.add(createFormRow("Gender", genderCombo));
            fields.add(createFormRow("Alamat", alamat));
            fields.add(createFormRow("No. Telepon", telp));
            fields.add(createFormRow("Email", email));
            
            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            buttons.setOpaque(false);
            
            JButton cancel = createSecondaryButton("Batal");
            JButton save = createPrimaryButton("Update");
            
            cancel.addActionListener(e -> dialog.dispose());
            save.addActionListener(e -> saveAnggota(data, u, nama, kelas, genderCombo, alamat, telp, email, dialog));
            
            buttons.add(cancel);
            buttons.add(save);
            
            panel.add(title, BorderLayout.NORTH);
            panel.add(fields, BorderLayout.CENTER);
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
        
        private void saveAnggota(Map<String,String> data, JTextField u, JTextField nama, 
                                JTextField kelas, JComboBox<String> genderCombo,
                                JTextField alamat, JTextField telp, JTextField email, JDialog dialog) {
            try {
                if (u.getText().trim().isEmpty() || nama.getText().trim().isEmpty()) { 
                    showMessageDialog("Peringatan", "Username dan nama lengkap wajib diisi."); 
                    return; 
                }
                
                String gender = genderCombo.getSelectedItem() != null ? 
                               genderCombo.getSelectedItem().toString() : "";
                
                DB.exec("UPDATE users SET username=?, nama_lengkap=?, kelas=?, gender=?, alamat=?, no_telp=?, email=? WHERE user_id=? AND role='USER'",
                    u.getText().trim(), 
                    nama.getText().trim(), 
                    kelas.getText().trim().isEmpty() ? null : kelas.getText().trim(),
                    gender.isEmpty() ? null : gender,
                    alamat.getText().trim().isEmpty() ? null : alamat.getText().trim(),
                    telp.getText().trim().isEmpty() ? null : telp.getText().trim(),
                    email.getText().trim().isEmpty() ? null : email.getText().trim(),
                    data.get("user_id")
                );
                DB.audit(Long.valueOf(AdminPage.this.id()), "UPDATE", "users", data.get("user_id"), "Edit anggota");
                showMessageDialog("Sukses", "Data anggota berhasil diperbarui.");
                refresh();
                dialog.dispose();
            } catch (Exception ex) {
                ex.printStackTrace();
                showErrorDialog("Error", "Gagal menyimpan. Username mungkin sudah digunakan.");
            }
        }
    }

    class BukuPanel extends JPanel {
        DefaultTableModel model = new DefaultTableModel(new String[]{
            "ID", "Kode", "ISBN", Lang.get("books.form.title"), Lang.get("books.form.author"), 
            Lang.get("books.form.publisher"), Lang.get("books.table.year"), 
            Lang.get("books.table.category"), Lang.get("books.form.location"), 
            Lang.get("books.table.stock"), Lang.get("books.table.available")
        }, 0);
        JTable table = new JTable(model);
        private JButton btnRefresh;
        private JTextField searchField;

        BukuPanel() {
            setBackground(Utils.BG);
            setLayout(new BorderLayout());
            
            // Header dengan live search
            JPanel header = createPanelHeader(Lang.get("admin.books.title"), 
                Lang.get("admin.books.subtitle"));
            
            // Table
            styleTable();
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
            
            JLabel searchIcon = new JLabel("Cari");
            searchIcon.setFont(new Font("Segoe UI", Font.PLAIN, 14));
             searchIcon.setForeground(Utils.TEXT);
            
            searchPanel.add(searchIcon);
            searchPanel.add(searchField);
            
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
            
            searchPanel.add(btnRefresh);
            
            header.add(titlePanel, BorderLayout.WEST);
            header.add(searchPanel, BorderLayout.EAST);
            
            return header;
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
            
            JButton add = createPrimaryButton(Lang.get("books.add_title"));
            JButton edit = createSecondaryButton(Lang.get("btn.edit"));
            JButton delete = createSecondaryButton(Lang.get("btn.delete"));
            JButton kategori = createSecondaryButton(Lang.get("books.btn.manage_cat"));
            JButton rak = createSecondaryButton(Lang.get("books.btn.manage_rack"));
            
            add.addActionListener(e -> openForm(null));
            edit.addActionListener(e -> {
                int r = table.getSelectedRow();
                if (r < 0) { 
                    showMessageDialog(Lang.get("msg.warning"), Lang.get("msg.info")); 
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
                // PERBAIKAN: Gunakan HashMap untuk banyak parameter
                Map<String, String> data = new HashMap<>();
                
                String bookId = String.valueOf(model.getValueAt(row, 0));
                var bookData = DB.query("SELECT * FROM books WHERE book_id = ?", bookId).get(0);
                
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
                data.put("stok_tersedia", String.valueOf(model.getValueAt(row,10)));
                
                return data;
            } catch (Exception e) {
                // Fallback jika query gagal
                Map<String, String> data = new HashMap<>();
                data.put("book_id", String.valueOf(model.getValueAt(row,0)));
                data.put("code", String.valueOf(model.getValueAt(row,1)));
                data.put("isbn", String.valueOf(model.getValueAt(row,2)));
                data.put("judul", String.valueOf(model.getValueAt(row,3)));
                data.put("penulis", String.valueOf(model.getValueAt(row,4)));
                data.put("penerbit", String.valueOf(model.getValueAt(row,5)));
                data.put("tahun", String.valueOf(model.getValueAt(row,6)));
                data.put("category_id", "");
                data.put("rack_id", "");
                data.put("stok_total", String.valueOf(model.getValueAt(row,9)));
                data.put("stok_tersedia", String.valueOf(model.getValueAt(row,10)));
                
                return data;
            }
        }

        void refresh() {
            try {
                model.setRowCount(0);
                var rows = DB.query(
                    "SELECT b.book_id,b.code,b.isbn,b.judul,b.penulis,b.penerbit,b.tahun, " +
                    "c.name kategori, r.code rak, b.stok_total,b.stok_tersedia " +
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
                        r.get("stok_tersedia")
                    });
                }
                btnRefresh.setText("Refresh (" + rows.size() + " buku)");
            } catch (Exception ignored) {
                ignored.printStackTrace();
            }
        }
        
        private void deleteBuku() {
            int r = table.getSelectedRow();
            if (r < 0) { 
                showMessageDialog(Lang.get("msg.warning"), Lang.get("msg.info")); 
                return; 
            }
            
            int modelRow = table.convertRowIndexToModel(r);
            String id = String.valueOf(model.getValueAt(modelRow,0));
            String judul = String.valueOf(model.getValueAt(modelRow,3));
            
            if (!confirmDialog(Lang.get("btn.delete"), 
                Lang.get("msg.confirm") + " " + Lang.get("books.form.title") + ":\n" + 
                judul + "\n\n" +
                Lang.get("msg.warn_delete_borrowed"))) return;
            
            try {
                DB.exec("DELETE FROM books WHERE book_id=?", id);
                DB.audit(Long.valueOf(AdminPage.this.id()), "DELETE", "books", id, "Hapus buku");
                refresh();
                showMessageDialog(Lang.get("msg.success"), Lang.get("msg.success"));
            } catch (Exception ex) { 
                showErrorDialog(Lang.get("msg.error"), Lang.get("msg.error")); 
            }
        }
        
        class Item {
            String id; String name;
            Item(String id, String name) { this.id=id; this.name=name; }
            @Override
            public String toString() { return name; }
        }

        void openForm(Map<String,String> data) {
            JDialog d = new JDialog(AdminPage.this, true);
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
            
            // Form fields
            JPanel fields = new JPanel(new GridLayout(10, 1, 10, 15));
            fields.setOpaque(false);
            
            JTextField code = Utils.input("kode buku");
            JTextField isbn = Utils.input("isbn");
            JTextField judul = Utils.input("judul");
            JTextField penulis = Utils.input("penulis");
            JTextField penerbit = Utils.input("penerbit");
            JTextField tahun = Utils.input("tahun (angka)");
            Utils.numericOnly(tahun);
            JTextField stok = Utils.input("stok total (angka)");
            Utils.numericOnly(stok);
            
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
            }
            
            fields.add(createFormRow("Kode Buku*", code));
            fields.add(createFormRow("ISBN", isbn));
            fields.add(createFormRow("Judul*", judul));
            fields.add(createFormRow("Penulis", penulis));
            fields.add(createFormRow("Penerbit", penerbit));
            fields.add(createFormRow("Tahun", tahun));
            fields.add(createFormRow("Kategori", cbKat));
            fields.add(createFormRow("Rak", cbRak));
            fields.add(createFormRow("Stok Total*", stok));
            
            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            buttons.setOpaque(false);
            
            JButton cancel = createSecondaryButton("Batal");
            JButton save = createPrimaryButton(data==null ? "Simpan" : "Update");
            
            cancel.addActionListener(e -> dialog.dispose());
            save.addActionListener(e -> saveBuku(data, code, isbn, judul, penulis, penerbit, tahun, stok, cbKat, cbRak, dialog));
            
            buttons.add(cancel);
            buttons.add(save);
            
            panel.add(title, BorderLayout.NORTH);
            panel.add(fields, BorderLayout.CENTER);
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
                             JTextField penulis, JTextField penerbit, JTextField tahun, JTextField stok,
                             JComboBox<Item> cbKat, JComboBox<Item> cbRak, JDialog dialog) {
            try {
                if (code.getText().trim().isEmpty() || judul.getText().trim().isEmpty() || stok.getText().trim().isEmpty()) { 
                    showMessageDialog("Peringatan", "Kode, judul, dan stok wajib diisi."); 
                    return; 
                }
                
                int stokTotal;
                try {
                    stokTotal = Integer.parseInt(stok.getText().trim());
                    if (stokTotal < 0) {
                        showMessageDialog("Peringatan", "Stok tidak boleh negatif.");
                        return;
                    }
                } catch (NumberFormatException e) {
                    showErrorDialog("Error", "Format angka tidak valid pada stok.");
                    return;
                }
                
                Integer tahunVal = null;
                if (!tahun.getText().trim().isEmpty()) {
                    try {
                        tahunVal = Integer.parseInt(tahun.getText().trim());
                    } catch (NumberFormatException e) {
                        showErrorDialog("Error", "Format tahun tidak valid.");
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
                        "INSERT INTO books(code,isbn,judul,penulis,penerbit,tahun,category_id,rack_id,stok_total,stok_tersedia) " +
                        "VALUES (?,?,?,?,?,?,?,?,?,?)",
                        code.getText().trim(), 
                        isbn.getText().trim().isEmpty() ? null : isbn.getText().trim(), 
                        judul.getText().trim(),
                        penulis.getText().trim().isEmpty() ? null : penulis.getText().trim(), 
                        penerbit.getText().trim().isEmpty() ? null : penerbit.getText().trim(),
                        tahunVal,
                        katId,
                        rakId,
                        stokTotal, 
                        stokTotal
                    );
                    DB.audit(Long.valueOf(AdminPage.this.id()), "CREATE", "books", String.valueOf(idNew), "Tambah buku");
                    showMessageDialog("Sukses", "Buku berhasil ditambahkan.");
                } else {
                    // Update buku yang ada
                    DB.exec(
                        "UPDATE books SET code=?,isbn=?,judul=?,penulis=?,penerbit=?,tahun=?,category_id=?,rack_id=?,stok_total=?,stok_tersedia=? WHERE book_id=?",
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
                        data.get("book_id")
                    );
                    DB.audit(Long.valueOf(AdminPage.this.id()), "UPDATE", "books", data.get("book_id"), "Edit buku");
                    showMessageDialog("Sukses", "Data buku berhasil diperbarui.");
                }
                refresh();
                dialog.dispose();
            } catch (NumberFormatException e) {
                showErrorDialog("Error", "Format angka tidak valid.");
            } catch (Exception ex) {
                ex.printStackTrace();
                showErrorDialog("Error", "Gagal menyimpan. Kode buku mungkin sudah digunakan.");
            }
        }
        
        void openKategoriManager() {
            ManagerDialog mg = new ManagerDialog(AdminPage.this, "Kategori", "categories", "category_id", "name", AdminPage.this.id());
            mg.open();
            refresh();
        }

        void openRakManager() {
            ManagerDialog mg = new ManagerDialog(AdminPage.this, "Rak", "racks", "rack_id", "code", AdminPage.this.id());
            mg.open();
            refresh();
        }

        class ManagerDialog {
            JFrame owner;
            String title;
            String table, idCol, nameCol;
            int actorId;
            DefaultTableModel m = new DefaultTableModel(new String[]{"ID","Nama"},0);
            JTable t = new JTable(m);

            ManagerDialog(JFrame owner, String title, String table, String idCol, String nameCol, int actorId) {
                this.owner=owner; this.title=title; this.table=table; this.idCol=idCol; this.nameCol=nameCol; this.actorId=actorId;
            }

            void open() {
                JDialog d = new JDialog(owner, true);
                d.setTitle("Kelola " + title);
                d.setSize(500, 400);
                d.setLocationRelativeTo(owner);
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
                
                JButton add = createPrimaryButton("Tambah");
                JButton edit = createSecondaryButton("Edit");
                JButton del = createSecondaryButton("Hapus");
                
                add.addActionListener(e -> form(d, null));
                edit.addActionListener(e -> {
                    int r = t.getSelectedRow();
                    if (r<0) { 
                        showMessageDialog("Peringatan", "Pilih data terlebih dahulu."); 
                        return; 
                    }
                    // PERBAIKAN: Gunakan HashMap
                    Map<String, String> rowData = new HashMap<>();
                    rowData.put("id", String.valueOf(m.getValueAt(r,0)));
                    rowData.put("name", String.valueOf(m.getValueAt(r,1)));
                    form(d, rowData);
                });
                del.addActionListener(e -> {
                    int r = t.getSelectedRow();
                    if (r<0) { 
                        showMessageDialog("Peringatan", "Pilih data terlebih dahulu."); 
                        return; 
                    }
                    String id = String.valueOf(m.getValueAt(r,0));
                    String name = String.valueOf(m.getValueAt(r,1));
                    
                    if (!confirmDialog("Konfirmasi Hapus", 
                        "Apakah Anda yakin ingin menghapus " + title.toLowerCase() + ":\n" + 
                        name + "\n\n" +
                        "PERHATIAN: " + title + " yang digunakan oleh buku tidak dapat dihapus!")) return;
                    
                    try {
                        DB.exec("DELETE FROM "+table+" WHERE "+idCol+"=?", id);
                        DB.audit(Long.valueOf(actorId), "DELETE", table, id, "Hapus "+title);
                        refresh();
                        showMessageDialog("Sukses", title + " berhasil dihapus.");
                    } catch (Exception ex) { 
                        showErrorDialog("Error", "Gagal hapus. " + title + " mungkin masih digunakan."); 
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
                        showMessageDialog("Sukses", title + " berhasil ditambahkan.");
                    } else {
                        DB.exec("UPDATE "+table+" SET "+nameCol+"=? WHERE "+idCol+"=?", input, data.get("id"));
                        DB.audit(Long.valueOf(actorId), "UPDATE", table, data.get("id"), "Edit "+title);
                        showMessageDialog("Sukses", title + " berhasil diperbarui.");
                    }
                    refresh();
                } catch (Exception ex) {
                    showErrorDialog("Error", "Gagal menyimpan. Nama mungkin sudah digunakan.");
                }
            }
        }
    }

    class LaporanPanel extends JPanel {
        DefaultTableModel model = new DefaultTableModel(new String[]{"ID","Tanggal","Jatuh Tempo","Anggota","Petugas","Status","Total Item"},0);
        JTable table = new JTable(model);
        private JButton btnRefresh;
        private JTextField from = Utils.input("YYYY-MM-DD");
        private JTextField to = Utils.input("YYYY-MM-DD");
        private JTextField searchField;

        LaporanPanel() {
            setBackground(Utils.BG);
            setLayout(new BorderLayout());
            
            // Header dengan live search
            JPanel header = createPanelHeader(Lang.get("admin.laporan.title"), 
                Lang.get("admin.laporan.subtitle"));
            
           // Filter panel dengan validasi tanggal
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterPanel.setBackground(Utils.BG);
        filterPanel.setBorder(new EmptyBorder(0, 20, 10, 20));
        
        // MODIFIKASI: Mengubah warna label menjadi Utils.TEXT
        JLabel labelDari = new JLabel("Dari:");
        labelDari.setForeground(Utils.TEXT);
        filterPanel.add(labelDari);
        
        // MODIFIKASI: Mengatur ukuran textfield
        from.setPreferredSize(new Dimension(150, 35)); // Lebar 150px, tinggi 30px
        
        // Validasi input tanggal - hanya angka dan dash
        from.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyTyped(java.awt.event.KeyEvent e) {
                char c = e.getKeyChar();
                if (!((c >= '0' && c <= '9') || c == '-' || c == '\b')) {
                    e.consume();
                }
            }
        });
        
        filterPanel.add(from);
        
        // MODIFIKASI: Mengubah warna label menjadi Utils.TEXT
        JLabel labelSampai = new JLabel("Sampai:");
        labelSampai.setForeground(Utils.TEXT);
        filterPanel.add(labelSampai);
        
        // MODIFIKASI: Mengatur ukuran textfield
        to.setPreferredSize(new Dimension(150, 35)); // Lebar 150px, tinggi 30px
        
        to.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyTyped(java.awt.event.KeyEvent e) {
                char c = e.getKeyChar();
                if (!((c >= '0' && c <= '9') || c == '-' || c == '\b')) {
                    e.consume();
                }
            }
        });
        
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
        
        // Table dengan live search
        styleTable();
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new EmptyBorder(0, 20, 20, 20));
        scroll.getViewport().setBackground(Utils.CARD);
        
        add(header, BorderLayout.NORTH);
        add(filterPanel, BorderLayout.CENTER);
        add(scroll, BorderLayout.SOUTH);
        
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
            searchField.putClientProperty("JTextField.placeholderText", Lang.get("petugas.search.placeholder"));
            
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
            
            JLabel searchIcon = new JLabel("Cari");
            searchIcon.setFont(new Font("Segoe UI", Font.PLAIN, 14));
             searchIcon.setForeground(Utils.TEXT);
            
            searchPanel.add(searchIcon);
            searchPanel.add(searchField);
            
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
            
            searchPanel.add(btnRefresh);
            
            header.add(titlePanel, BorderLayout.WEST);
            header.add(searchPanel, BorderLayout.EAST);
            
            return header;
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
                    
                    // Status column styling
                    if (column == 5) {
                        if ("AKTIF".equals(value)) {
                            setForeground(new Color(66, 133, 244));
                        } else if ("SELESAI".equals(value)) {
                            setForeground(new Color(52, 168, 83));
                        } else if ("BATAL".equals(value)) {
                            setForeground(new Color(234, 67, 53));
                        }
                    }
                    
                    // Date comparison for overdue
                    if (column == 2 && value != null) {
                        try {
                            String dueDate = value.toString();
                            String status = (String) table.getValueAt(row, 5);
                            if ("AKTIF".equals(status)) {
                                java.sql.Date due = java.sql.Date.valueOf(dueDate);
                                java.sql.Date today = new java.sql.Date(System.currentTimeMillis());
                                if (due.before(today)) {
                                    setForeground(new Color(234, 67, 53));
                                }
                            }
                        } catch (Exception e) {
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

        void refresh() {
            try {
                model.setRowCount(0);
                String f = from.getText().trim();
                String t = to.getText().trim();
                List<Map<String,String>> rows;
                
                if (!f.isEmpty() && !t.isEmpty()) {
                    rows = DB.query(
                        "SELECT l.loan_id, l.tanggal_pinjam, l.jatuh_tempo, u.nama_lengkap anggota, p.nama_lengkap petugas, l.status, " +
                        "(SELECT SUM(qty) FROM loan_items li WHERE li.loan_id=l.loan_id) total_item " +
                        "FROM loans l " +
                        "JOIN users u ON l.user_id=u.user_id " +
                        "JOIN users p ON l.petugas_id=p.user_id " +
                        "WHERE l.tanggal_pinjam BETWEEN ? AND ? " +
                        "ORDER BY l.loan_id DESC", f, t
                    );
                } else if (!f.isEmpty()) {
                    rows = DB.query(
                        "SELECT l.loan_id, l.tanggal_pinjam, l.jatuh_tempo, u.nama_lengkap anggota, p.nama_lengkap petugas, l.status, " +
                        "(SELECT SUM(qty) FROM loan_items li WHERE li.loan_id=l.loan_id) total_item " +
                        "FROM loans l " +
                        "JOIN users u ON l.user_id=u.user_id " +
                        "JOIN users p ON l.petugas_id=p.user_id " +
                        "WHERE l.tanggal_pinjam >= ? " +
                        "ORDER BY l.loan_id DESC", f
                    );
                } else if (!t.isEmpty()) {
                    rows = DB.query(
                        "SELECT l.loan_id, l.tanggal_pinjam, l.jatuh_tempo, u.nama_lengkap anggota, p.nama_lengkap petugas, l.status, " +
                        "(SELECT SUM(qty) FROM loan_items li WHERE li.loan_id=l.loan_id) total_item " +
                        "FROM loans l " +
                        "JOIN users u ON l.user_id=u.user_id " +
                        "JOIN users p ON l.petugas_id=p.user_id " +
                        "WHERE l.tanggal_pinjam <= ? " +
                        "ORDER BY l.loan_id DESC", t
                    );
                } else {
                    rows = DB.query(
                        "SELECT l.loan_id, l.tanggal_pinjam, l.jatuh_tempo, u.nama_lengkap anggota, p.nama_lengkap petugas, l.status, " +
                        "(SELECT SUM(qty) FROM loan_items li WHERE li.loan_id=l.loan_id) total_item " +
                        "FROM loans l " +
                        "JOIN users u ON l.user_id=u.user_id " +
                        "JOIN users p ON l.petugas_id=p.user_id " +
                        "ORDER BY l.loan_id DESC"
                    );
                }
                
                for (var r: rows) {
                    model.addRow(new Object[]{
                        r.get("loan_id"), r.get("tanggal_pinjam"), r.get("jatuh_tempo"),
                        r.get("anggota"), r.get("petugas"), r.get("status"), r.get("total_item")
                    });
                }
                
                int total = rows.size();
                int aktif = 0;
                int selesai = 0;
                int batal = 0;
                
                for (var r : rows) {
                    String status = r.get("status");
                    if ("AKTIF".equals(status)) aktif++;
                    else if ("SELESAI".equals(status)) selesai++;
                    else if ("BATAL".equals(status)) batal++;
                }
                
                btnRefresh.setText("<html><body style='text-align:center'>🔄 " + Lang.get("table.total") + ": <b>" + total + "</b> | " +
                    "<span style='color:#4285F4'>" + Lang.get("status.active") + ": <b>" + aktif + "</b></span> | " +
                    "<span style='color:#34A853'>" + Lang.get("status.finished") + ": <b>" + selesai + "</b></span> | " +
                    "<span style='color:#EA4335'>" + Lang.get("status.cancelled") + ": <b>" + batal + "</b></span></body></html>");
            } catch (Exception ignored) {
                ignored.printStackTrace();
            }
        }
    }

    class SettingsPanel extends JPanel {
        private JTextField libName = Utils.input("nama perpustakaan");
        private JTextField maxDays = Utils.input("maks hari (angka)");
        private JTextField maxBooks = Utils.input("maks buku (angka)");
        private JTextField fine = Utils.input("denda per hari (angka)");
        private JTextField maxBorrowUser = Utils.input("maks pinjam per user (angka)");
        private JButton btnSave;
        private JLabel logoPreview;
        private List<DayRow> scheduleRows = new ArrayList<>();

        class DayRow {
            int index;
            JCheckBox open;
            JSpinner start, end;
            JPanel panel;
            
            DayRow(int idx) {
                this.index = idx;
                panel = new JPanel(new GridBagLayout());
                panel.setOpaque(false);
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.insets = new Insets(2, 5, 2, 5);
                gbc.fill = GridBagConstraints.HORIZONTAL;
                
                JLabel nameLabel = new JLabel(Lang.get("day." + idx).toUpperCase());
                nameLabel.setForeground(Utils.TEXT);
                nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
                nameLabel.setPreferredSize(new Dimension(85, 25));
                
                open = new JCheckBox(Lang.get("label.buka"));
                open.setForeground(Utils.TEXT);
                open.setOpaque(false);
                open.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                
                start = createTimeSpinner();
                end = createTimeSpinner();
                
                JLabel separator = new JLabel("-");
                separator.setForeground(Utils.MUTED);
                
                gbc.gridx = 0; gbc.weightx = 0.3;
                panel.add(nameLabel, gbc);
                
                gbc.gridx = 1; gbc.weightx = 0.2;
                panel.add(open, gbc);
                
                gbc.gridx = 2; gbc.weightx = 0.2;
                panel.add(start, gbc);
                
                gbc.gridx = 3; gbc.weightx = 0.05;
                panel.add(separator, gbc);
                
                gbc.gridx = 4; gbc.weightx = 0.2;
                panel.add(end, gbc);
                
                open.addActionListener(e -> toggle(open.isSelected()));
            }
            
            void toggle(boolean active) {
                start.setEnabled(active);
                end.setEnabled(active);
            }
            
            private JSpinner createTimeSpinner() {
                SpinnerDateModel model = new SpinnerDateModel();
                JSpinner s = new JSpinner(model);
                JSpinner.DateEditor editor = new JSpinner.DateEditor(s, "HH:mm");
                s.setEditor(editor);
                s.setPreferredSize(new Dimension(70, 25));
                s.setBackground(Utils.CARD2);
                s.setBorder(BorderFactory.createLineBorder(Utils.BORDER));
                return s;
            }
        }

        SettingsPanel() {
            setBackground(Utils.BG);
            setLayout(new BorderLayout());
            
            // Header
            JPanel header = createPanelHeader(Lang.get("settings.title"), Lang.get("settings.subtitle"));
            add(header, BorderLayout.NORTH);
            
            // Main Content with Bento Grid
            JPanel content = new JPanel(new GridBagLayout());
            content.setBackground(Utils.BG);
            content.setBorder(new EmptyBorder(0, 20, 20, 20));
            
            Utils.numericOnly(maxDays);
            Utils.numericOnly(maxBooks);
            Utils.numericOnly(fine);
            Utils.numericOnly(maxBorrowUser);
            
            // 1. Identity Card
            JPanel identityCard = createBentoCard(Lang.get("settings.card.identity"));
            identityCard.setLayout(new BorderLayout(15, 15));
            
            JPanel logoPreviewPanel = new JPanel(new BorderLayout(0, 5));
            logoPreviewPanel.setOpaque(false);
            logoPreview = new JLabel();
            logoPreview.setPreferredSize(new Dimension(80, 80));
            logoPreview.setBorder(BorderFactory.createLineBorder(Utils.BORDER));
            logoPreview.setHorizontalAlignment(SwingConstants.CENTER);
            loadLogo();
            
            JButton btnChangeLogo = createSecondaryButton(Lang.get("btn.upload"));
            btnChangeLogo.addActionListener(e -> changeLogo());
            logoPreviewPanel.add(logoPreview, BorderLayout.CENTER);
            logoPreviewPanel.add(btnChangeLogo, BorderLayout.SOUTH);
            
            JPanel idForm = new JPanel(new GridLayout(1, 1, 0, 10)); // Changed from (2,1)
            idForm.setOpaque(false);
            idForm.add(createInputGroup(Lang.get("label.libname"), libName));
            
            identityCard.add(logoPreviewPanel, BorderLayout.WEST);
            identityCard.add(idForm, BorderLayout.CENTER);
            
            // 2. Rules Card
            JPanel rulesCard = createBentoCard(Lang.get("settings.card.rules"));
            rulesCard.setLayout(new GridLayout(2, 2, 15, 15));
            rulesCard.add(createInputGroup(Lang.get("label.maxdays"), maxDays));
            rulesCard.add(createInputGroup(Lang.get("label.maxbooks"), maxBooks));
            rulesCard.add(createInputGroup(Lang.get("label.maxborrow"), maxBorrowUser));
            rulesCard.add(createInputGroup(Lang.get("label.fine"), fine));
            
            // 3. Operational Schedule Card
            JPanel scheduleCard = createBentoCard(Lang.get("label.hours"));
            JPanel scheduleList = new JPanel(new GridLayout(7, 1, 0, 5));
            scheduleList.setOpaque(false);
            
            for (int i = 0; i < 7; i++) {
                DayRow dr = new DayRow(i);
                scheduleRows.add(dr);
                scheduleList.add(dr.panel);
            }
            
            JButton btnSaveSchedule = createPrimaryButton(Lang.get("btn.save_schedule"));
            btnSaveSchedule.addActionListener(e -> saveSchedule());
            
            JPanel scheduleFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            scheduleFooter.setOpaque(false);
            scheduleFooter.add(btnSaveSchedule);
            
            scheduleCard.add(scheduleList, BorderLayout.CENTER);
            scheduleCard.add(scheduleFooter, BorderLayout.SOUTH);
            
            // Actions Card
            JPanel actionsCard = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            actionsCard.setBackground(Utils.BG);
            actionsCard.setBorder(new EmptyBorder(10, 0, 0, 0));
            
            btnSave = createPrimaryButton(Lang.get("btn.save"));
            JButton export = createSecondaryButton(Lang.get("report.generate") + " CSV");
            JButton backup = createSecondaryButton("Backup DB");
            
            btnSave.addActionListener(e -> saveSettings());
            export.addActionListener(e -> exportCSV());
            backup.addActionListener(e -> backupDatabase());
            
            actionsCard.add(backup);
            actionsCard.add(export);
            actionsCard.add(btnSave);
            
            // === Add to GridBag (Compact Layout) ===
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.fill = GridBagConstraints.BOTH;
            
            // Left Column: Identity & Rules
            gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.45; gbc.weighty = 0.0;
            content.add(identityCard, gbc);
            
            gbc.gridx = 0; gbc.gridy = 1; gbc.weighty = 0.5;
            content.add(rulesCard, gbc);
            
            // Right Column: Schedule
            gbc.gridx = 1; gbc.gridy = 0; gbc.gridheight = 2; gbc.weightx = 0.55; gbc.weighty = 1.0;
            content.add(scheduleCard, gbc);
            
            // Bottom Row: Actions
            gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; gbc.weighty = 0.0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            content.add(actionsCard, gbc);
            
            add(content, BorderLayout.CENTER);
            refresh();
        }
        
        private void loadLogo() {
            try {
                File f = new File("src/nahlib/nahsazlibrary.png");
                if (f.exists()) {
                    ImageIcon ic = new ImageIcon(f.getAbsolutePath());
                    logoPreview.setIcon(new ImageIcon(ic.getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH)));
                } else {
                    java.net.URL imgURL = getClass().getResource("/nahlib/nahsazlibrary.png");
                    if (imgURL != null) {
                        ImageIcon ic = new ImageIcon(imgURL);
                        logoPreview.setIcon(new ImageIcon(ic.getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH)));
                    }
                }
            } catch (Exception e) {}
        }
        
        private JPanel createBentoCard(String title) {
            JPanel card = new JPanel();
            card.setBackground(Utils.CARD);
            card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Utils.BORDER),
                new EmptyBorder(12, 12, 12, 12)
            ));
            
            // Wrap internal content with title
            JPanel wrapper = new JPanel(new BorderLayout(0, 8));
            wrapper.setOpaque(false);
            
            JLabel lbl = new JLabel(title);
            lbl.setForeground(Utils.MUTED);
            lbl.setFont(Utils.FONT_B);
            
            wrapper.add(lbl, BorderLayout.NORTH);
            wrapper.add(card, BorderLayout.CENTER); // Reuse card as container? No, logic twist.
            
            // Fix: wrapper is the outer styling? No.
            // Let's make the card THE panel.
            card.setLayout(new BorderLayout(0, 15));
            card.add(lbl, BorderLayout.NORTH);
            return card;
        }

        private JPanel createInputGroup(String label, JTextField field) {
            JPanel p = new JPanel(new BorderLayout(0, 5));
            p.setOpaque(false);
            JLabel l = new JLabel(label);
            l.setForeground(Utils.TEXT);
            l.setFont(Utils.FONT);
            p.add(l, BorderLayout.NORTH);
            p.add(field, BorderLayout.CENTER);
            return p;
        }

        private void changeLogo() {
            // Gunakan FileDialog (AWT) untuk mendapatkan dialog native Windows yang modern
            FileDialog fd = new FileDialog(AdminPage.this, "Pilih Logo Baru", FileDialog.LOAD);
            // setFile filter pattern untuk Windows
            fd.setFile("*.png;*.jpg;*.jpeg"); 
            fd.setVisible(true);

            if (fd.getFile() != null) {
                File src = new File(fd.getDirectory(), fd.getFile());
                String targetPath = "C:\\Users\\briya\\OneDrive\\Dokumen\\MAPEL RPL SMKANTR2\\Projek Akhir RPL2\\Netbeans\\NahLib\\src\\nahlib\\nahsazlibrary.png";
                Path dest = Paths.get(targetPath);
                
                try {
                    Files.copy(src.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);
                    loadLogo(); // Reload preview di settings
                    AdminPage.this.refreshApp(); // Update logo di top bar (AdminPage)
                    
                    showMessageDialog("Sukses", "Logo berhasil diganti!");
                    DB.audit(Long.valueOf(AdminPage.this.id()), "UPDATE", "system", "logo", "Changed application logo");
                } catch (Exception ex) {
                    showErrorDialog("Error", "Gagal mengganti logo: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        }
        
        private JPanel createPanelHeader(String title, String subtitle) {
            JPanel header = new JPanel(new BorderLayout());
            header.setBackground(Utils.BG);
            header.setBorder(new EmptyBorder(20, 20, 10, 20));
            
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
            
            header.add(titlePanel, BorderLayout.WEST);
            return header;
        }

        public void refresh() {
            try {
                var s = DB.settings();
                libName.setText(s.getOrDefault("library_name","Nahsaz Library"));
                
                var r = DB.rules();
                maxDays.setText(String.valueOf(r.get("max_days")));
                maxBooks.setText(String.valueOf(r.get("max_books")));
                fine.setText(String.valueOf(r.get("fine_per_day")));
                maxBorrowUser.setText(String.valueOf(r.get("max_borrow_per_user")));

                // Load Schedule
                var oh = DB.query("SELECT * FROM operational_hours ORDER BY day_index");
                SimpleDateFormat parser = new SimpleDateFormat("HH:mm:ss");
                for (var row : oh) {
                    int idx = Integer.parseInt(row.get("day_index"));
                    if (idx < scheduleRows.size()) {
                        DayRow dr = scheduleRows.get(idx);
                        dr.open.setSelected("1".equals(row.get("is_open")));
                        dr.start.setValue(parser.parse(row.get("open_time")));
                        dr.end.setValue(parser.parse(row.get("close_time")));
                        dr.toggle(dr.open.isSelected());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                showErrorDialog("Error", "Gagal memuat pengaturan.");
            }
        }

        private void saveSchedule() {
            try {
                if (!confirmDialog(Lang.get("btn.save_schedule"), Lang.get("msg.confirm"))) return;
                
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                for (DayRow dr : scheduleRows) {
                    String startStr = sdf.format((Date) dr.start.getValue());
                    String endStr = sdf.format((Date) dr.end.getValue());
                    int open = dr.open.isSelected() ? 1 : 0;
                    
                    DB.exec("UPDATE operational_hours SET is_open=?, open_time=?, close_time=? WHERE day_index=?",
                            open, startStr, endStr, dr.index);
                }
                
                DB.audit(Long.valueOf(AdminPage.this.id()), "UPDATE", "operational_hours", "all", "Update operational schedule");
                showMessageDialog(Lang.get("msg.success"), Lang.get("msg.success_save"));
            } catch (Exception ex) {
                ex.printStackTrace();
                showErrorDialog(Lang.get("msg.error"), "Gagal menyimpan jadwal: " + ex.getMessage());
            }
        }
        
        private void saveSettings() {
            try {
                // Validation
                int d = Integer.parseInt(maxDays.getText().trim());
                int b = Integer.parseInt(maxBooks.getText().trim());
                int f = Integer.parseInt(fine.getText().trim());
                int mu = Integer.parseInt(maxBorrowUser.getText().trim());
                
                if (d <= 0 || b <= 0 || f < 0 || mu <= 0) {
                    showMessageDialog("Peringatan", "Semua nilai harus bernilai positif.");
                    return;
                }
                
                if (!confirmDialog("Konfirmasi", "Simpan pengaturan perpustakaan?")) return;
                
                DB.exec("UPDATE settings SET setting_value=? WHERE setting_key='library_name'", libName.getText().trim());
                DB.exec("UPDATE rules SET max_days=?, max_books=?, fine_per_day=?, max_borrow_per_user=? WHERE rule_id=1", 
                    d, b, f, mu);

                DB.audit(Long.valueOf(AdminPage.this.id()), "UPDATE", "settings", "rules", "Update settings & rules");
                showMessageDialog(Lang.get("msg.success"), Lang.get("msg.success_save"));
            } catch (NumberFormatException e) {
                showErrorDialog(Lang.get("msg.error"), Lang.get("msg.error"));
            } catch (Exception ex) {
                ex.printStackTrace();
                showErrorDialog(Lang.get("msg.error"), Lang.get("msg.error") + ": " + ex.getMessage());
            }
        }
        
        private void exportCSV() {
            String[] tables = {"users","books","loans","loan_items","returns","wishlist","categories","racks","audit_log"};
            String pick = (String) JOptionPane.showInputDialog(this, Lang.get("report.period") + ":", Lang.get("report.generate") + " CSV",
                    JOptionPane.PLAIN_MESSAGE, null, tables, tables[0]);
            if (pick == null) return;

            JFileChooser fc = new JFileChooser();
            fc.setSelectedFile(new File(pick + ".csv"));
            if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

            try {
                DB.exportTableToCSV(pick, fc.getSelectedFile());
                DB.audit(Long.valueOf(AdminPage.this.id()), "EXPORT", "csv", pick, "Export CSV");
                showMessageDialog(Lang.get("msg.success"), Lang.get("msg.success") + ": " + fc.getSelectedFile().getName());
            } catch (Exception ex) {
                showErrorDialog(Lang.get("msg.error"), Lang.get("msg.error") + ": " + ex.getMessage());
            }
        }
        
        private void backupDatabase() {
            if (!confirmDialog("Backup Database", 
                "Apakah Anda yakin ingin membuat backup database?\n\n" +
                "Backup akan mengekspor semua tabel ke file SQL.")) return;
            
            JFileChooser fc = new JFileChooser();
            fc.setSelectedFile(new File("nahsaz_library_backup_" + 
                java.time.LocalDate.now().toString() + ".sql"));
            
            if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    showMessageDialog(Lang.get("msg.info"), Lang.get("msg.info") + "\n" +
                        "Gunakan Export CSV untuk mengekspor data tabel.");
                } catch (Exception ex) {
                    showErrorDialog(Lang.get("msg.error"), Lang.get("msg.error"));
                }
            }
        }
    }

    class AuditPanel extends JPanel {
        DefaultTableModel model = new DefaultTableModel(new String[]{"Waktu","Actor","Action","Entity","EntityID","Detail"},0);
        JTable table = new JTable(model);
        private JButton btnRefresh;
        private JTextField dateFrom = Utils.input("YYYY-MM-DD");
        private JTextField dateTo = Utils.input("YYYY-MM-DD");
        private JTextField searchField;

        AuditPanel() {
            setBackground(Utils.BG);
            setLayout(new BorderLayout());
            
            // Header
            JPanel header = createPanelHeader(Lang.get("admin.audit.title"), 
                Lang.get("admin.audit.subtitle"));
            
            // Filter tanggal di pojok kanan atas
            JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            filterPanel.setBackground(Utils.BG);
            filterPanel.setBorder(new EmptyBorder(10, 20, 10, 20));
            
            filterPanel.add(new JLabel("Dari:"));
            
            // Validasi input tanggal
            dateFrom.addKeyListener(new java.awt.event.KeyAdapter() {
                @Override
                public void keyTyped(java.awt.event.KeyEvent e) {
                    char c = e.getKeyChar();
                    if (!((c >= '0' && c <= '9') || c == '-' || c == '\b')) {
                        e.consume();
                    }
                }
            });
            
            dateFrom.setPreferredSize(new Dimension(100, 30));
            filterPanel.add(dateFrom);
            
            filterPanel.add(new JLabel("Sampai:"));
            
            dateTo.addKeyListener(new java.awt.event.KeyAdapter() {
                @Override
                public void keyTyped(java.awt.event.KeyEvent e) {
                    char c = e.getKeyChar();
                    if (!((c >= '0' && c <= '9') || c == '-' || c == '\b')) {
                        e.consume();
                    }
                }
            });
            
            dateTo.setPreferredSize(new Dimension(100, 30));
            filterPanel.add(dateTo);
            
            JButton filterBtn = createSecondaryButton("Filter");
            JButton clearBtn = createSecondaryButton("Clear");
            
            filterBtn.addActionListener(e -> refresh());
            clearBtn.addActionListener(e -> { 
                dateFrom.setText(""); 
                dateTo.setText(""); 
                refresh(); 
            });
            
            filterPanel.add(filterBtn);
            filterPanel.add(clearBtn);
            
            // Table
            styleTable();
            JScrollPane scroll = new JScrollPane(table);
            scroll.setBorder(new EmptyBorder(0, 20, 20, 20));
            scroll.getViewport().setBackground(Utils.CARD);
            
            // Action buttons
            JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            actions.setBackground(Utils.BG);
            actions.setBorder(new EmptyBorder(0, 20, 20, 20));
            
            btnRefresh = createSecondaryButton("Refresh");
            JButton clear = createSecondaryButton("Clear Old Logs");
            
            btnRefresh.addActionListener(e -> refresh());
            clear.addActionListener(e -> clearOldLogs());
            
            actions.add(btnRefresh);
            actions.add(clear);
            
            add(header, BorderLayout.NORTH);
            add(filterPanel, BorderLayout.CENTER);
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
            searchField.putClientProperty("JTextField.placeholderText", Lang.get("petugas.search.placeholder"));
            
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
            
            JLabel searchIcon = new JLabel("Cari");
            searchIcon.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            searchIcon.setForeground(Utils.TEXT);
            
            searchPanel.add(searchIcon);
            searchPanel.add(searchField);
            
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
            
            searchPanel.add(btnRefresh);
            
            header.add(titlePanel, BorderLayout.WEST);
            header.add(searchPanel, BorderLayout.EAST);
            
            return header;
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
                    
                    // Color code actions
                    if (column == 2 && value != null) {
                        String action = value.toString();
                        switch (action) {
                            case "CREATE":
                                setForeground(new Color(52, 168, 83));
                                break;
                            case "UPDATE":
                                setForeground(new Color(66, 133, 244));
                                break;
                            case "DELETE":
                                setForeground(new Color(234, 67, 53));
                                break;
                            case "LOGIN":
                                setForeground(new Color(251, 188, 5));
                                break;
                            case "LOGOUT":
                                setForeground(new Color(154, 160, 166));
                                break;
                        }
                    }
                    
                    setBorder(noFocusBorder);
                    return c;
                }
            });
            
            // Enable row sorter
            table.setRowSorter(new TableRowSorter<>(model));
        }

        void refresh() {
            try {
                model.setRowCount(0);
                String f = dateFrom.getText().trim();
                String t = dateTo.getText().trim();
                List<Map<String,String>> rows;
                
                if (!f.isEmpty() && !t.isEmpty()) {
                    rows = DB.query(
                        "SELECT a.created_at, u.nama_lengkap actor, a.action, a.entity, a.entity_id, a.detail " +
                        "FROM audit_log a LEFT JOIN users u ON a.actor_id=u.user_id " +
                        "WHERE DATE(a.created_at) BETWEEN ? AND ? " +
                        "ORDER BY a.audit_id DESC LIMIT 500", f, t
                    );
                } else if (!f.isEmpty()) {
                    rows = DB.query(
                        "SELECT a.created_at, u.nama_lengkap actor, a.action, a.entity, a.entity_id, a.detail " +
                        "FROM audit_log a LEFT JOIN users u ON a.actor_id=u.user_id " +
                        "WHERE DATE(a.created_at) >= ? " +
                        "ORDER BY a.audit_id DESC LIMIT 500", f
                    );
                } else if (!t.isEmpty()) {
                    rows = DB.query(
                        "SELECT a.created_at, u.nama_lengkap actor, a.action, a.entity, a.entity_id, a.detail " +
                        "FROM audit_log a LEFT JOIN users u ON a.actor_id=u.user_id " +
                        "WHERE DATE(a.created_at) <= ? " +
                        "ORDER BY a.audit_id DESC LIMIT 500", t
                    );
                } else {
                    rows = DB.query(
                        "SELECT a.created_at, u.nama_lengkap actor, a.action, a.entity, a.entity_id, a.detail " +
                        "FROM audit_log a LEFT JOIN users u ON a.actor_id=u.user_id " +
                        "ORDER BY a.audit_id DESC LIMIT 500"
                    );
                }
                
                for (var r: rows) {
                    model.addRow(new Object[]{
                        r.get("created_at"),
                        r.get("actor")==null?"-":r.get("actor"),
                        r.get("action"), r.get("entity"), r.get("entity_id"), r.get("detail")
                    });
                }
                btnRefresh.setText("<html><body>🔄 " + Lang.get("table.total") + ": <b>" + rows.size() + "</b> log</body></html>");
            } catch (Exception ignored) {
                ignored.printStackTrace();
            }
        }
        
        private void clearOldLogs() {
            if (!confirmDialog("Clear Logs", 
                "Apakah Anda yakin ingin menghapus log audit yang berumur > 30 hari?\n\n" +
                "Tindakan ini tidak dapat dibatalkan!")) return;
            
            try {
                int deleted = (int) DB.exec("DELETE FROM audit_log WHERE created_at < DATE_SUB(NOW(), INTERVAL 30 DAY)");
                DB.audit(Long.valueOf(AdminPage.this.id()), "DELETE", "audit_log", "batch", "Clear old logs (" + deleted + " records)");
                refresh();
                showMessageDialog("Sukses", "Berhasil menghapus " + deleted + " log lama.");
            } catch (Exception ex) {
                showErrorDialog("Error", "Gagal menghapus log.");
            }
        }
    }
}