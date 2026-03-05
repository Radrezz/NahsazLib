package nahlib.user;

import nahlib.Lang;
import nahlib.DB;
import nahlib.Utils;
import nahlib.CustomIcon;
import nahlib.SimpleChart;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class DashboardPanel extends JPanel {
    private final UserPage userPage;
    private final JLabel borrowed = createStatLabel("0");
    private final JLabel dueSoon = createStatLabel("0");
    private final JLabel overdue = createStatLabel("0");
    private final JLabel wishlist = createStatLabel("0");
    
    private SimpleChart barChart;

    public DashboardPanel(UserPage userPage) {
        this.userPage = userPage;
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
        
        JLabel subtitle = new JLabel(String.format(Lang.get("user.dash.subtitle"), userPage.getMe().get("nama_lengkap")));
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
        
        JPanel barWrapper = Utils.card();
        barWrapper.setLayout(new BorderLayout());
        JLabel barLabel = new JLabel(Lang.get("user.chart.loan"));
        barLabel.setForeground(Utils.TEXT);
        barLabel.setFont(Utils.FONT_B);
        barLabel.setBorder(new EmptyBorder(0,0,10,0));
        barWrapper.add(barLabel, BorderLayout.NORTH);
        barWrapper.add(barChart, BorderLayout.CENTER);

        // New Arrivals Panel
        JPanel newBooks = Utils.card();
        newBooks.setLayout(new BorderLayout());
        JLabel newLabel = new JLabel("Buku Terbaru");
        newLabel.setForeground(Utils.TEXT);
        newLabel.setFont(Utils.FONT_B);
        newLabel.setBorder(new EmptyBorder(0,0,15,0));
        newBooks.add(newLabel, BorderLayout.NORTH);
        
        JPanel booksGrid = new JPanel(new GridLayout(1, 4, 15, 0));
        booksGrid.setOpaque(false);
        try {
            var rows = DB.query("SELECT * FROM books ORDER BY book_id DESC LIMIT 4");
            for (var r : rows) {
                booksGrid.add(createMiniCard(r));
            }
        } catch (Exception ignored) {}
        newBooks.add(booksGrid, BorderLayout.CENTER);

        // Layout everything in Bento Grid
        addGB(barWrapper, 0, 3, 1, 1, 0.4, 0.4);
        addGB(newBooks, 1, 3, 2, 1, 0.6, 0.4);
    }

    private JPanel createMiniCard(java.util.Map<String, String> data) {
        JPanel p = new JPanel(new BorderLayout(0, 5));
        p.setOpaque(false);
        
        JLabel img = new JLabel();
        img.setHorizontalAlignment(SwingConstants.CENTER);
        img.setPreferredSize(new Dimension(80, 120)); // Professional 4:6 ratio
        img.setBackground(Utils.CARD2);
        img.setOpaque(true);
        img.setBorder(BorderFactory.createLineBorder(Utils.BORDER));
        
        String path = data.get("cover");
        if (path != null && !path.equals("null")) {
            ImageIcon icon = Utils.getCover(path, 80, 120);
            if (icon != null) img.setIcon(icon);
        }
        
        JLabel txt = new JLabel("<html><center>" + data.get("judul") + "</center></html>");
        txt.setForeground(Utils.TEXT);
        txt.setFont(new Font("Segoe UI", Font.BOLD, 11));
        txt.setHorizontalAlignment(SwingConstants.CENTER);
        
        p.add(img, BorderLayout.CENTER);
        p.add(txt, BorderLayout.SOUTH);
        
        p.setCursor(new Cursor(Cursor.HAND_CURSOR));
        p.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                new nahlib.DetailPage(userPage, "Detail Buku", data);
            }
        });
        
        return p;
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
            ((BrowsePanel)userPage.getPanel("browse")).refresh();
            userPage.showPanel("browse", 1);
        });
        
        wishlistBtn.addActionListener(e -> {
            ((WishlistPanel)userPage.getPanel("wish")).refresh();
            userPage.showPanel("wish", 2);
        });
        
        historyBtn.addActionListener(e -> {
            ((RiwayatPanel)userPage.getPanel("hist")).refresh();
            userPage.showPanel("hist", 3);
        });
        
        notificationBtn.addActionListener(e -> {
            ((NotifPanel)userPage.getPanel("notif")).refresh();
            userPage.showPanel("notif", 4);
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

    public void refresh() {
        try {
            long userId = userPage.id();
            
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
            barChart.setData(months, readData);

            if (o > 0) {
                userPage.showMessageDialog(Lang.get("msg.warning"), 
                    "Sistem mendeteksi ada " + o + " peminjaman yang TERLAMBAT. Mohon segera cek menu notifikasi.");
            } else if (d > 0) {
                userPage.showMessageDialog(Lang.get("msg.info"), 
                    "Anda memiliki " + d + " peminjaman yang akan segera jatuh tempo dalam 24 jam ke depan.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
