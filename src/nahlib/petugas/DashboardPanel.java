package nahlib.petugas;

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
    private final PetugasPage petugasPage;
    private final JLabel todayTx = createStatLabel("0");
    private final JLabel myMonth = createStatLabel("0");
    private final JLabel active = createStatLabel("0");
    private final JLabel overdueToday = createStatLabel("0");
    
    private SimpleChart barChart;
    private SimpleChart lineChart;

    public DashboardPanel(PetugasPage petugasPage) {
        this.petugasPage = petugasPage;
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
        
        newLoan.addActionListener(e -> petugasPage.switchPanel(1));
        processReturn.addActionListener(e -> petugasPage.switchPanel(2));
        viewReport.addActionListener(e -> petugasPage.switchPanel(3));
        checkOverdue.addActionListener(e -> petugasPage.switchPanel(4));
        
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

    public void refresh() {
        try {
            String userId = petugasPage.getMe().get("user_id");
            
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
                petugasPage.showMessageDialog(Lang.get("msg.warning"), String.format(Lang.get("user.label.total_books"), o).replace("buku", Lang.get("nav.loan")) + " " + Lang.get("user.stat.overdue") + ". " + Lang.get("user.quickaction.notification"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
