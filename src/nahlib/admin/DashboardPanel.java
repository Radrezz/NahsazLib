package nahlib.admin;

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
    private final AdminPage adminPage;
    private final JLabel lbBooks = createStatLabel("0");
    private final JLabel lbMembers = createStatLabel("0");
    private final JLabel lbActive = createStatLabel("0");
    private final JLabel lbOverdue = createStatLabel("0");
    
    private SimpleChart barChart;
    private SimpleChart lineChart;

    public DashboardPanel(AdminPage adminPage) {
        this.adminPage = adminPage;
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
        
        headerObj.add(titleLabel, BorderLayout.CENTER);
        
        card.add(headerObj, BorderLayout.NORTH);
        card.add(value, BorderLayout.CENTER);
        
        // Add a subtle bottom border with accent color
        JPanel bottomBar = new JPanel();
        bottomBar.setBackground(accentColor);
        bottomBar.setPreferredSize(new Dimension(0, 3));
        card.add(bottomBar, BorderLayout.SOUTH);
        
        return card;
    }

    public void refresh() {
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

            adminPage.setBadgeOverdue(overdue);
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
