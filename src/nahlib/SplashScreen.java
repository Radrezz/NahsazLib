package nahlib;

import javax.swing.*;
import java.awt.*;

public class SplashScreen extends JFrame {

    private JProgressBar progressBar;
    private Timer timer;
    private int progress = 0;

    public SplashScreen() {
        setUndecorated(true);
        setSize(420, 420);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(40, 42, 55),
                        0, getHeight(), new Color(58, 62, 75)
                );
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

         JLabel title = new JLabel(Utils.getLibraryName());
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(new Color(33, 143, 237));
        title.setAlignmentX(CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("School Library Management System");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(Color.LIGHT_GRAY);
        subtitle.setAlignmentX(CENTER_ALIGNMENT);

        progressBar = new JProgressBar(0, 100);
        progressBar.setMaximumSize(new Dimension(300, 18));
        progressBar.setForeground(new Color(33, 143, 237));
        progressBar.setBackground(new Color(70, 74, 87));
        progressBar.setBorderPainted(false);
        progressBar.setAlignmentX(CENTER_ALIGNMENT);

        JLabel status = new JLabel("Initializing...");
        status.setForeground(Color.LIGHT_GRAY);
        status.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        status.setAlignmentX(CENTER_ALIGNMENT);

        panel.add(Box.createVerticalGlue());
        panel.add(title);
        panel.add(Box.createRigidArea(new Dimension(0, 6)));
        panel.add(subtitle);
        panel.add(Box.createRigidArea(new Dimension(0, 30)));
        panel.add(progressBar);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(status);
        panel.add(Box.createVerticalGlue());

        add(panel);
        setVisible(true);

        startLoading(status);
    }

    private void startLoading(JLabel status) {
        timer = new Timer(30, e -> {
            progress++;
            progressBar.setValue(progress);

            if (progress < 40) {
                status.setText("Loading components...");
            } else if (progress < 70) {
                status.setText("Connecting to database...");
            } else {
                status.setText("Starting application...");
            }

            if (progress >= 100) {
                timer.stop();
                openLogin();
            }
        });
        timer.start();
    }

    private void openLogin() {
        dispose();

        if (DB.connect() == null) {
            Utils.msg("Gagal terhubung ke database MySQL!");
            System.exit(0);
        }

        new LoginPage();
    }
}
