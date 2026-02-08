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

    public PetugasPage(Map<String,String> me) {
        this.me = me;

        setTitle(Utils.getLibraryName() + " - " + Lang.get("petugas.dash.title"));
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
        
        JLabel subtitle = new JLabel(" • " + Lang.get("petugas.dash.title"));
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

    private JPanel createBottomNav(String[] labels, CustomIcon.Type[] icons, int activeIndex) {
        JPanel bar = new JPanel(new GridLayout(1, labels.length, 1, 0));
        bar.setBackground(Utils.BG);
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
    
    private void switchPanel(int index) {
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
    
    private void updateNavBadge(int index, int count) {
        if (navButtons != null && index >= 0 && index < navButtons.length && navButtons[index] != null) {
            String text = Lang.get("nav.notifications");
            if (count > 0) {
                text = Lang.get("nav.notifications") + " (" + count + ")";
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
        
        private SimpleChart barChart;
        private SimpleChart lineChart;

        DashboardPanel() {
            setBackground(Utils.BG);
            setLayout(new GridBagLayout()); // Bento Layout
            
            // Header
            JPanel header = new JPanel(new BorderLayout());
            header.setBackground(Utils.BG);
            header.setBorder(new EmptyBorder(20, 30, 10, 30));
            
            JLabel title = new JLabel(Lang.get("petugas.dash.title"), new CustomIcon(CustomIcon.Type.DASHBOARD, 24, new Color(52, 168, 83)), SwingConstants.LEFT);
            title.setForeground(Utils.TEXT);
            title.setFont(new Font("Segoe UI", Font.BOLD, 26));
            title.setIconTextGap(12);
            
            JLabel subtitle = new JLabel(Lang.get("petugas.dash.subtitle"));
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
            addGB(createStatCard(Lang.get("petugas.stat.today_tx"), todayTx, new Color(66, 133, 244)), 0, 1, 1, 1, 0.35, 0.25);
            addGB(createStatCard(Lang.get("petugas.stat.month_tx"), myMonth, new Color(52, 168, 83)), 1, 1, 1, 1, 0.35, 0.25);
            addGB(createStatCard(Lang.get("petugas.stat.active_loan"), active, new Color(251, 188, 5)), 0, 2, 1, 1, 0.35, 0.25);
            addGB(createStatCard(Lang.get("petugas.stat.global_overdue"), overdueToday, new Color(234, 67, 53)), 1, 2, 1, 1, 0.35, 0.25);

            // Charts
            barChart = new SimpleChart(SimpleChart.Type.BAR, new Color(66, 133, 244));
            lineChart = new SimpleChart(SimpleChart.Type.LINE, new Color(52, 168, 83));
            
            JPanel barWrapper = Utils.card();
            barWrapper.setLayout(new BorderLayout());
            JLabel barLabel = new JLabel(Lang.get("petugas.chart.daily_vol"));
            barLabel.setForeground(Utils.TEXT);
            barLabel.setFont(Utils.FONT_B);
            barLabel.setBorder(new EmptyBorder(0,0,10,0));
            barWrapper.add(barLabel, BorderLayout.NORTH);
            barWrapper.add(barChart, BorderLayout.CENTER);

            JPanel lineWrapper = Utils.card();
            lineWrapper.setLayout(new BorderLayout());
            JLabel lineLabel = new JLabel(Lang.get("petugas.chart.trend"));
            lineLabel.setForeground(Utils.TEXT);
            lineLabel.setFont(Utils.FONT_B);
            lineLabel.setBorder(new EmptyBorder(0,0,10,0));
            lineWrapper.add(lineLabel, BorderLayout.NORTH);
            lineWrapper.add(lineChart, BorderLayout.CENTER);

            // Side-by-side charts at bottom, full width
            addGB(barWrapper, 0, 3, 1, 1, 0.5, 0.4);
            addGB(lineWrapper, 1, 3, 2, 1, 0.5, 0.4); 
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
            gbc.insets = new Insets(8, 8, 8, 8);
            if (x == 0) gbc.insets.left = 30;
            if (x + w == 3) gbc.insets.right = 30;
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
            
            JLabel title = new JLabel(Lang.get("petugas.quickaction.title"));
            title.setForeground(Utils.MUTED);
            title.setFont(new Font("Segoe UI", Font.BOLD, 12));
            title.setBorder(new EmptyBorder(0, 0, 15, 0));
            
            JPanel actions = new JPanel(new GridLayout(4, 1, 0, 10)); // Vertical Stack
            actions.setBackground(Utils.CARD);
            
            JButton newLoan = createActionButton(Lang.get("petugas.quickaction.new_loan"), CustomIcon.Type.BOOKS);
            JButton processReturn = createActionButton(Lang.get("petugas.quickaction.return"), CustomIcon.Type.HOME);
            JButton viewReport = createActionButton(Lang.get("petugas.quickaction.view_report"), CustomIcon.Type.REPORTS);
            JButton checkOverdue = createActionButton(Lang.get("petugas.quickaction.check_overdue"), CustomIcon.Type.AUDIT);
            
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

                // Populate charts
                List<String> days;
                if (Lang.getLanguage() == 0) {
                    days = List.of("Sen", "Sel", "Rab", "Kam", "Jum", "Sab");
                } else {
                    days = List.of("Mon", "Tue", "Wed", "Thu", "Fri", "Sat");
                }
                List<Double> transData = List.of(8.0, 12.0, 10.0, 15.0, 20.0, 5.0);
                List<Double> trendData = List.of(20.0, 22.0, 25.0, 24.0, 28.0, 30.0);
                
                barChart.setData(days, transData);
                lineChart.setData(days, trendData);

                if (o > 0) {
                    showMessageDialog(Lang.get("msg.warning"), String.format(Lang.get("user.label.total_books"), o).replace("buku", Lang.get("nav.loan")) + " " + Lang.get("user.stat.overdue") + ". " + Lang.get("user.quickaction.notification"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class PeminjamanPanel extends JPanel {
        private JComboBox<Item> cbUser = new JComboBox<>();
        private JComboBox<Item> cbBook = new JComboBox<>();
        private JTextField qty = Utils.input(Lang.get("petugas.loan.qty").toLowerCase());
        private DefaultTableModel cart = new DefaultTableModel(new String[]{
            Lang.get("books.table.id"), Lang.get("books.table.isbn"), Lang.get("books.table.title"), Lang.get("books.table.stock")
        },0);
        private JTable table = new JTable(cart);
        private JLabel rulesInfo = new JLabel("");
        private JLabel cartSummary;

        PeminjamanPanel() {
            setBackground(Utils.BG);
            setLayout(new BorderLayout());
            
            // Header
            JPanel header = createPanelHeader(Lang.get("petugas.loan.title"), 
                Lang.get("petugas.loan.subtitle"));
            
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
            
            formCard.add(createFormRow(Lang.get("petugas.loan.select_member"), cbUser));
            formCard.add(createFormRow(Lang.get("petugas.loan.select_book"), cbBook));
            formCard.add(createFormRow(Lang.get("petugas.loan.qty"), qty));
            
            JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            buttonRow.setOpaque(false);
            JButton addCart = createPrimaryButton(Lang.get("btn.add"));
            JButton clearCart = createSecondaryButton(Lang.get("btn.reset"));
            addCart.addActionListener(e -> addToCart());
            clearCart.addActionListener(e -> clearCart());
            buttonRow.add(addCart);
            buttonRow.add(clearCart);
            formCard.add(buttonRow);
            
            leftPanel.add(formCard, BorderLayout.NORTH);
            
            // Cart summary
            cartSummary = new JLabel(String.format(Lang.get("petugas.loan.cart_items"), 0, 0));
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
            
            JLabel tableTitle = new JLabel(Lang.get("petugas.loan.cart_title"));
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
            
            JButton submitBtn = createPrimaryButton(Lang.get("petugas.quickaction.new_loan"));
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
                cbUser.addItem(new Item(null, "-- " + Lang.get("petugas.loan.select_member") + " --"));
                cbBook.addItem(new Item(null, "-- " + Lang.get("petugas.loan.select_book") + " --"));

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
                rulesInfo.setText(String.format(Lang.get("petugas.loan.rules"), rule.get("max_days"), 
                    rule.get("max_books"), rule.get("fine_per_day")));
                
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
            cartSummary.setText(String.format(Lang.get("petugas.loan.cart_items"), totalItems, totalQty));
        }

        void addToCart() {
            Item b = (Item) cbBook.getSelectedItem();
            if (b == null || b.id == null) { 
                showMessageDialog(Lang.get("msg.warning"), Lang.get("msg.info")); 
                return; 
            }
            
            int q;
            try {
                q = qty.getText().trim().isEmpty() ? 1 : Integer.parseInt(qty.getText().trim());
                if (q <= 0) { 
                    showMessageDialog(Lang.get("msg.warning"), Lang.get("msg.info")); 
                    return; 
                }
            } catch (NumberFormatException e) {
                showMessageDialog(Lang.get("msg.warning"), Lang.get("msg.info"));
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
                            showMessageDialog(Lang.get("msg.warning"), Lang.get("user.label.out_of_stock") + " (" + avail + ")");
                            return;
                        }
                        cart.setValueAt(String.valueOf(old + q), i, 3);
                        updateCartSummary();
                        return;
                    }
                }
                
                if (q > avail) {
                    showMessageDialog(Lang.get("msg.warning"), Lang.get("user.label.out_of_stock") + " (" + avail + ")");
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
                showMessageDialog(Lang.get("msg.info"), Lang.get("msg.no_data"));
                return;
            }
            
            if (confirmDialog(Lang.get("btn.delete"), Lang.get("msg.confirm_clear_cart"))) {
                cart.setRowCount(0);
                updateCartSummary();
                showMessageDialog(Lang.get("msg.success"), Lang.get("msg.success_delete"));
            }
        }

        void submitLoan() {
            Item u = (Item) cbUser.getSelectedItem();
            if (u == null || u.id == null) { 
                showMessageDialog(Lang.get("msg.warning"), Lang.get("msg.info")); 
                return; 
            }
            
            if (cart.getRowCount() == 0) { 
                showMessageDialog(Lang.get("msg.warning"), Lang.get("msg.no_data")); 
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
                    showErrorDialog(Lang.get("msg.error"), String.format(Lang.get("petugas.loan.rules"), maxDays, maxBooks, rule.get("fine_per_day")));
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

                if (!confirmDialog(Lang.get("loan.confirm.title"), 
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

                showMessageDialog(Lang.get("msg.success"), Lang.get("loan.msg.success"));
                
                cart.setRowCount(0);
                updateCartSummary();
                refresh();

            } catch (RuntimeException ex) {
                showErrorDialog(Lang.get("msg.error"), ex.getMessage());
            } catch (Exception ex) {
                ex.printStackTrace();
                showErrorDialog(Lang.get("msg.error"), Lang.get("msg.error") + ": " + ex.getMessage());
            }
        }

        class Item {
            String id, name;
            Item(String id, String name) { this.id=id; this.name=name; }
            public String toString() { return name; }
        }
    }

    class PengembalianPanel extends JPanel {
        private DefaultTableModel model = new DefaultTableModel(new String[]{
            Lang.get("loan.table.id"), Lang.get("loan.table.member"), Lang.get("table.date"), 
            Lang.get("loan.table.duedate"), Lang.get("label.days_late"), Lang.get("table.total")
        },0);
        private JTable table = new JTable(model);
        private JLabel detail = new JLabel(Lang.get("petugas.return.select_loan"));
        private JTextArea items = new JTextArea();
        private JButton btnProcess;

        PengembalianPanel() {
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
            
            items.setBackground(Utils.CARD2);
            items.setForeground(Utils.TEXT);
            items.setEditable(false);
            items.setFont(new Font("Monospaced", Font.PLAIN, 12));
            items.setBorder(BorderFactory.createLineBorder(Utils.BORDER));
            
            btnProcess = createPrimaryButton(Lang.get("petugas.quickaction.return"));
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
                detail.setText(Lang.get("petugas.return.select_loan"));
            } catch (Exception e) {
                e.printStackTrace();
                showErrorDialog(Lang.get("msg.error"), Lang.get("msg.error") + ": " + e.getMessage());
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
                sb.append("=== ").append(Lang.get("petugas.return.detail_title")).append(" ===\n");
                sb.append("Loan ID: ").append(loanId).append("\n");
                sb.append(Lang.get("role.user")).append(": ").append(anggota).append("\n");
                sb.append(Lang.get("label.days_late")).append(": ").append(telat).append(" ").append(Lang.get("nav.history").toLowerCase()).append("\n");
                sb.append("Denda/hari: Rp ").append(finePerDay).append("\n");
                sb.append("Total denda: Rp ").append(fine).append("\n\n");
                sb.append("=== ").append(Lang.get("petugas.return.book_list")).append(" ===\n");
                
                for (Map<String, String> it: itemsRows) {
                    sb.append("• ").append(it.get("code")).append(" - ")
                      .append(it.get("judul")).append(" (")
                      .append(it.get("qty")).append(" buku)\n");
                }
                
                if (telat > 0) {
                    sb.append("\n").append(Lang.get("petugas.return.fine_warning"));
                }
                
                detail.setText(Lang.get("petugas.return.detail_title") + ": " + loanId + " - " + anggota);
                items.setText(sb.toString());

            } catch (Exception e) {
                e.printStackTrace();
                showErrorDialog(Lang.get("msg.error"), Lang.get("msg.error") + ": " + e.getMessage());
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
                
                String message = String.format(Lang.get("petugas.return.confirm_msg"), 
                    loanId, anggota, telat, fine);
                
                if (!confirmDialog(Lang.get("return.confirm.title"), message)) return;

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

                showMessageDialog(Lang.get("msg.success"), Lang.get("return.msg.success") + "\n" +
                    (telat > 0 ? Lang.get("label.fine") + ": Rp " + fine : Lang.get("return.status.ontime")));
                
                refresh();

            } catch (Exception ex) {
                ex.printStackTrace();
                showErrorDialog(Lang.get("msg.error"), Lang.get("msg.error") + ": " + ex.getMessage());
            }
        }
    }

    class LaporanSayaPanel extends JPanel {
        private DefaultTableModel model = new DefaultTableModel(new String[]{
            Lang.get("loan.table.id"), Lang.get("table.date"), Lang.get("loan.table.duedate"), 
            Lang.get("loan.table.member"), Lang.get("table.status"), Lang.get("table.total")
        },0);
        private JTable table = new JTable(model);
        private JTextField from = Utils.input("YYYY-MM-DD");
        private JTextField to = Utils.input("YYYY-MM-DD");
        private JTextField search = Utils.input(Lang.get("petugas.search.placeholder"));

        LaporanSayaPanel() {
            setBackground(Utils.BG);
            setLayout(new BorderLayout());
            
            // Header
            JPanel header = createPanelHeader(Lang.get("petugas.report.title"), 
                Lang.get("petugas.report.subtitle"));
            
            // Filter panel
            JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            filterPanel.setBackground(Utils.BG);
            filterPanel.setBorder(new EmptyBorder(0, 20, 10, 20));
            
            filterPanel.add(new JLabel(Lang.get("report.from") + ":"));
            filterPanel.add(from);
            filterPanel.add(new JLabel(Lang.get("report.to") + ":"));
            filterPanel.add(to);
            
            filterPanel.add(new JLabel("  " + Lang.get("btn.search") + ":"));
            search.setPreferredSize(new Dimension(200, 35));
            filterPanel.add(search);
            
            JButton filterBtn = createSecondaryButton(Lang.get("btn.filter"));
            JButton clearBtn = createSecondaryButton(Lang.get("btn.reset"));
            
            filterBtn.addActionListener(e -> refresh());
            clearBtn.addActionListener(e -> { 
                from.setText(""); 
                to.setText(""); 
                search.setText("");
                refresh(); 
            });
            
            filterPanel.add(filterBtn);
            filterPanel.add(clearBtn);
            
            // Real-time search
            search.addKeyListener(new java.awt.event.KeyAdapter() {
                public void keyReleased(java.awt.event.KeyEvent e) { refresh(); }
            });
            
            // Table
            styleTable();
            JScrollPane scroll = new JScrollPane(table);
            scroll.getViewport().setBackground(Utils.CARD);
            scroll.setBorder(new EmptyBorder(0, 20, 20, 20));
            
            // Summary panel
            JPanel summaryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
            summaryPanel.setBackground(Utils.BG);
            summaryPanel.setBorder(new EmptyBorder(0, 20, 20, 20));
            
            JLabel totalLabel = new JLabel(String.format(Lang.get("petugas.report.total_tx"), 0));
            totalLabel.setForeground(Utils.TEXT);
            totalLabel.setFont(Utils.FONT_B);
            
            JLabel activeLabel = new JLabel(Lang.get("status.active") + ": 0");
            activeLabel.setForeground(new Color(66, 133, 244));
            activeLabel.setFont(Utils.FONT_B);
            
            JLabel completedLabel = new JLabel(Lang.get("status.finished") + ": 0");
            completedLabel.setForeground(new Color(52, 168, 83));
            completedLabel.setFont(Utils.FONT_B);
            
            JLabel cancelledLabel = new JLabel(Lang.get("status.cancelled") + ": 0");
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

                String sQuery = search.getText().trim();
                if (!sQuery.isEmpty()) {
                    where.append(" AND (l.loan_id LIKE ? OR u.nama_lengkap LIKE ?)");
                    params.add("%" + sQuery + "%");
                    params.add("%" + sQuery + "%");
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
                ((JLabel)summaryPanel.getComponent(0)).setText("<html>" + Lang.get("petugas.report.total_tx").replace("%d", "<b>"+total+"</b>") + "</html>");
                ((JLabel)summaryPanel.getComponent(1)).setText("<html>" + Lang.get("status.active") + ": <b style='color:#4285F4'>" + aktif + "</b></html>");
                ((JLabel)summaryPanel.getComponent(2)).setText("<html>" + Lang.get("status.finished") + ": <b style='color:#34A853'>" + selesai + "</b></html>");
                ((JLabel)summaryPanel.getComponent(3)).setText("<html>" + Lang.get("status.cancelled") + ": <b style='color:#EA4335'>" + batal + "</b></html>");
                
            } catch (Exception e) {
                e.printStackTrace();
                showErrorDialog(Lang.get("msg.error"), Lang.get("msg.error") + ": " + e.getMessage());
            }
        }
    }

    class NotifikasiPanel extends JPanel {
        private DefaultTableModel model = new DefaultTableModel(new String[]{
            Lang.get("loan.table.id"), Lang.get("loan.table.member"), Lang.get("loan.table.duedate"), 
            Lang.get("label.days_late"), Lang.get("table.status")
        },0);
        private JTable table = new JTable(model);
        private JTextField search = Utils.input(Lang.get("petugas.search.placeholder"));
        private JLabel summaryLabel;

        NotifikasiPanel() {
            setBackground(Utils.BG);
            setLayout(new BorderLayout());
            
            // Header
            JPanel header = createPanelHeader(Lang.get("petugas.notif.title"), 
                Lang.get("petugas.notif.subtitle"));
            
            // Table
            styleTable();
            JScrollPane scroll = new JScrollPane(table);
            scroll.getViewport().setBackground(Utils.CARD);
            scroll.setBorder(new EmptyBorder(0, 20, 10, 20));
            
            // Filter Bar
            JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            filterBar.setBackground(Utils.BG);
            filterBar.setBorder(new EmptyBorder(0, 20, 10, 20));
            
            filterBar.add(new JLabel(Lang.get("btn.search") + ":"));
            search.setPreferredSize(new Dimension(300, 35));
            filterBar.add(search);
            
            search.addKeyListener(new java.awt.event.KeyAdapter() {
                public void keyReleased(java.awt.event.KeyEvent e) { refresh(); }
            });
            
            JPanel centerPanel = new JPanel(new BorderLayout());
            centerPanel.setOpaque(false);
            centerPanel.add(filterBar, BorderLayout.NORTH);
            centerPanel.add(scroll, BorderLayout.CENTER);
            
            // Action panel
            JPanel actionPanel = new JPanel(new BorderLayout());
            actionPanel.setBackground(Utils.BG);
            actionPanel.setBorder(new EmptyBorder(0, 20, 20, 20));
            
            summaryLabel = new JLabel();
            summaryLabel.setForeground(Utils.TEXT);
            summaryLabel.setFont(Utils.FONT);
            
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            buttonPanel.setOpaque(false);
            
            JButton refreshBtn = createPrimaryButton(Lang.get("btn.refresh"));
            JButton notifyBtn = createSecondaryButton(Lang.get("btn.notify_member"));
            
            refreshBtn.addActionListener(e -> refresh());
            notifyBtn.addActionListener(e -> notifyAnggota());
            
            buttonPanel.add(refreshBtn);
            buttonPanel.add(notifyBtn);
            
            actionPanel.add(summaryLabel, BorderLayout.WEST);
            actionPanel.add(buttonPanel, BorderLayout.EAST);
            
            add(header, BorderLayout.NORTH);
            add(centerPanel, BorderLayout.CENTER);
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
                String sQuery = search.getText().trim();
                String sql = "SELECT l.loan_id, u.nama_lengkap as anggota, l.jatuh_tempo, " +
                    "GREATEST(DATEDIFF(CURDATE(), l.jatuh_tempo), 0) as telat, l.status " +
                    "FROM loans l JOIN users u ON l.user_id = u.user_id " +
                    "WHERE l.status = 'AKTIF' AND l.jatuh_tempo < CURDATE() ";
                
                List<Object> params = new ArrayList<>();
                if (!sQuery.isEmpty()) {
                    sql += " AND (l.loan_id LIKE ? OR u.nama_lengkap LIKE ?)";
                    params.add("%" + sQuery + "%");
                    params.add("%" + sQuery + "%");
                }
                sql += " ORDER BY telat DESC";
                
                List<Map<String, String>> rows = DB.query(sql, params.toArray());

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
                summaryLabel.setText("<html>" + Lang.get("table.total") + ": <b>" + total + "</b> | " + 
                    "<span style='color:#EA4335'>" + Lang.get("petugas.notif.critical") + ": <b>" + critical + "</b></span> | " + 
                    "<span style='color:#F57C00'>" + Lang.get("petugas.notif.high") + ": <b>" + high + "</b></span> | " + 
                    "<span style='color:#388E3C'>" + Lang.get("petugas.notif.medium") + ": <b>" + medium + "</b></span></html>");
                
                if (total > 0) {
                    updateNavBadge(4, total);
                }
                
            } catch (Exception e) {
                e.printStackTrace();
                showErrorDialog(Lang.get("msg.error"), Lang.get("msg.error") + ": " + e.getMessage());
            }
        }
        
        private void notifyAnggota() {
            int r = table.getSelectedRow();
            if (r < 0) { 
                showMessageDialog(Lang.get("msg.warning"), Lang.get("msg.info")); 
                return; 
            }
            
            String loanId = model.getValueAt(r, 0).toString();
            String anggota = model.getValueAt(r, 1).toString();
            int telat = Integer.parseInt(model.getValueAt(r, 3).toString());
            
            String message = String.format(Lang.get("petugas.notif.send_to"), anggota) + "\n" +
                           "Loan ID: " + loanId + "\n" +
                           Lang.get("label.days_late") + ": " + telat + " " + Lang.get("nav.history").toLowerCase() + "\n\n" +
                           Lang.get("petugas.notif.dev_note");
            
            if (confirmDialog(Lang.get("petugas.quickaction.check_overdue"), message)) {
                showMessageDialog(Lang.get("msg.info"), String.format(Lang.get("msg.success_notif"), anggota) + 
                    "\n\n" + Lang.get("petugas.notif.dev_note"));
            }
        }
    }
}