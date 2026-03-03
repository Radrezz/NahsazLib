package nahlib;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.Random;

public class SplashScreen extends JFrame {

    private JProgressBar progressBar;
    private Timer timer;
    private int progress = 0;
    
    // Quotes for aesthetics
    private final String[] quotes = {
        "\"A library is not a luxury but one of the necessities of life.\" - Henry Ward Beecher",
        "\"When in doubt go to the library.\" - J.K. Rowling",
        "\"I have always imagined that Paradise will be a kind of library.\" - Jorge Luis Borges",
        "\"The only thing that you absolutely have to know, is the location of the library.\" - Albert Einstein",
        "\"Libraries store the energy that fuels the imagination.\" - Sidney Sheldon",
        "\"Orang boleh pandai setinggi langit, tapi selama ia tidak menulis, ia akan hilang di dalam masyarakat dan dari sejarah.\" - Pramoedya Ananta Toer",
        "\"Aku rela dipenjara asalkan bersama buku, karena dengan buku aku bebas.\" - Mohammad Hatta",
        "\"Keberhasilan bukanlah milik orang pintar, keberhasilan adalah kepunyaan mereka yang senantiasa berusaha.\" - B.J. Habibie",
        "\"Membaca adalah jendela dunia.\" - Pepatah Indonesia",
        "\"Ilmu tanpa amal adalah hampa, amal tanpa ilmu adalah sesat.\" - Buya Hamka",
        "\"Reading is to the mind what exercise is to the body.\" - Richard Steele",
        "\"Books are a uniquely portable magic.\" - Stephen King",
        "\"Today a reader, tomorrow a leader.\" - Margaret Fuller",
        "\"The more that you read, the more things you will know.\" - Dr. Seuss",
        "\"Whatever the cost of our libraries, the price is cheap compared to that of an ignorant nation.\" - Walter Cronkite"
    };

    public SplashScreen() {
        setUndecorated(true);
        setSize(850, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        
        // Rounded corners (Java 9+ feature, safe to try or fallback)
        try {
            setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 30, 30));
        } catch (Exception e) {
            // Fallback for older systems or if not supported
        }

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Modern Dark Gradient Background
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(30, 33, 48),       // Darker top-left
                        getWidth(), getHeight(), new Color(45, 52, 70) // Lighter bottom-right
                );
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                
                // Add subtle geometric accents (optional)
                g2.setColor(new Color(255, 255, 255, 10)); // Very faint white
                g2.fillOval(-50, -50, 200, 200);
                g2.fillOval(getWidth() - 150, getHeight() - 150, 300, 300);
            }
        };
        panel.setLayout(new BorderLayout());
        
        // ================== CENTER CONTENT (Logo & Title) ==================
        JPanel centerPanel = new JPanel();
        centerPanel.setOpaque(false);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(60, 0, 0, 0)); // Push down

        // Logo
        JLabel logo = new JLabel();
        logo.setAlignmentX(CENTER_ALIGNMENT);
        try {
            // Menggunakan utilitas terpusat agar konsisten dengan database dan settings
            ImageIcon icon = Utils.getAppLogo(160);
            if (icon != null) {
                logo.setIcon(icon);
            }
        } catch (Exception e) {}

        // App Title
        JLabel title = new JLabel(Utils.getLibraryName());
        title.setFont(new Font("Segoe UI", Font.BOLD, 42));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(CENTER_ALIGNMENT);
        
        // Subtitle / Version
        JLabel subtitle = new JLabel("Version 1.0 | School Library System");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtitle.setForeground(new Color(176, 179, 190)); // Soft grey
        subtitle.setAlignmentX(CENTER_ALIGNMENT);
        
        centerPanel.add(logo);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 25)));
        centerPanel.add(title);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        centerPanel.add(subtitle);
        
        panel.add(centerPanel, BorderLayout.CENTER);

        // ================== BOTTOM (Quote & Progress) ==================
        JPanel bottomPanel = new JPanel();
        bottomPanel.setOpaque(false);
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 50, 40, 50)); // Padding

        // Loading Text
        JLabel statusLabel = new JLabel("Initializing modules...");
        statusLabel.setForeground(new Color(200, 200, 200));
        statusLabel.setFont(new Font("Consolas", Font.PLAIN, 12));
        statusLabel.setAlignmentX(CENTER_ALIGNMENT);

        // Progress Bar
        progressBar = new JProgressBar(0, 100);
        progressBar.setPreferredSize(new Dimension(850, 6)); // Thin sleek bar
        progressBar.setMaximumSize(new Dimension(9999, 6));
        progressBar.setForeground(new Color(66, 133, 244)); // Google Blue
        progressBar.setBackground(new Color(60, 63, 75));   // Dark track
        progressBar.setBorderPainted(false);
        progressBar.setAlignmentX(CENTER_ALIGNMENT);
        
        // Random Quote
        String q = quotes[new Random().nextInt(quotes.length)];
        JLabel quoteLabel = new JLabel("<html><center><i>" + q + "</i></center></html>", SwingConstants.CENTER);
        quoteLabel.setForeground(new Color(140, 144, 160));
        quoteLabel.setFont(new Font("Serif", Font.ITALIC, 16));
        quoteLabel.setAlignmentX(CENTER_ALIGNMENT);
        quoteLabel.setMaximumSize(new Dimension(750, 100)); // Fill width, restrict height
        
        bottomPanel.add(quoteLabel);
        bottomPanel.add(Box.createRigidArea(new Dimension(0, 25)));
        bottomPanel.add(statusLabel);
        bottomPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        bottomPanel.add(progressBar);

        panel.add(bottomPanel, BorderLayout.SOUTH);

        add(panel);
        
        // Start loading
        startLoading(statusLabel);
        
        setVisible(true);
    }

    private void startLoading(JLabel status) {
        timer = new Timer(30, e -> {
            progress++;
            
            // Smooth easing for progress visual (optional, sticking to linear for simplicity)
            progressBar.setValue(progress);

            if (progress < 20) status.setText("Loading Application Resources...");
            else if (progress < 50) status.setText("Connecting to Database System...");
            else if (progress < 80) status.setText("Initializing User Interface...");
            else status.setText("Finalizing Startup...");

            if (progress >= 100) {
                ((Timer)e.getSource()).stop();
                openLogin();
            }
        });
        timer.start();
    }

    private void openLogin() {
        dispose();
        // Check DB connection safely
        try {
            if (DB.connect() == null) {
                JOptionPane.showMessageDialog(null, 
                    "Failed to connect to MySQL Database!\nPlease check your server.", 
                    "Connection Error", JOptionPane.ERROR_MESSAGE);
                System.exit(0);
            }
            new LoginPage();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
