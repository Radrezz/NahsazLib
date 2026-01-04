package nahlib;

import nahlib.admin.AdminPage;
import nahlib.petugas.PetugasPage;
import nahlib.user.UserPage;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.net.URL;
import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

public class LoginPage extends JFrame {

    private final JTextField tfUser = Utils.input("");
    private final JPasswordField tfPass = Utils.passInput("");
    private final JCheckBox showPasswordCheck = new JCheckBox("Show Password");

    public LoginPage() {
        setTitle(Utils.getLibraryName() + " - Login");
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        
        // Set agar langsung maximize
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Utils.BG);

        // Container utama
        JPanel mainContainer = new JPanel(new GridLayout(1, 2, 0, 0));
        mainContainer.setBackground(Utils.BG);

        // ========== LEFT PANEL: FORM ==========
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(Utils.BG);
        leftPanel.setBorder(BorderFactory.createEmptyBorder(50, 80, 50, 40));

        JPanel card = Utils.card();
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        // Header dengan logo
        JPanel headerPanel = new JPanel(new BorderLayout(10, 0));
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 40, 0));
        
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);
        
        JLabel h = new JLabel("Login");
        h.setForeground(Utils.TEXT);
        h.setFont(new Font("Segoe UI", Font.BOLD, 28));
        h.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel s = new JLabel("Masuk ke akun Anda untuk melanjutkan");
        s.setForeground(Utils.MUTED);
        s.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        s.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        titlePanel.add(h);
        titlePanel.add(Box.createVerticalStrut(8));
        titlePanel.add(s);
        
        headerPanel.add(titlePanel, BorderLayout.CENTER);
        
        card.add(headerPanel, BorderLayout.NORTH);

        // Form fields
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridBagLayout());
        formPanel.setOpaque(false);
        formPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 0, 15, 0);
        gbc.weightx = 1.0;
        gbc.gridwidth = 2;
        
        // Style fields
        tfUser.setFont(Utils.FONT);
        tfPass.setFont(Utils.FONT);
        tfPass.setEchoChar('•');
        
        tfUser.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Utils.BORDER, 1),
            BorderFactory.createEmptyBorder(12, 15, 12, 15)
        ));
        tfPass.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Utils.BORDER, 1),
            BorderFactory.createEmptyBorder(12, 15, 12, 15)
        ));
        
        // Username field
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(createLabel("Username"), gbc);
        gbc.gridy = 1;
        formPanel.add(tfUser, gbc);
        
        // Password field
        gbc.gridy = 2;
        formPanel.add(createLabel("Password"), gbc);
        gbc.gridy = 3;
        formPanel.add(tfPass, gbc);
        
        // Show Password Checkbox
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        showPasswordCheck.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        showPasswordCheck.setForeground(Utils.MUTED);
        showPasswordCheck.setBackground(Utils.CARD);
        showPasswordCheck.setFocusPainted(false);
        showPasswordCheck.addActionListener(e -> {
            if (showPasswordCheck.isSelected()) {
                tfPass.setEchoChar((char) 0);
            } else {
                tfPass.setEchoChar('•');
            }
        });
        formPanel.add(showPasswordCheck, gbc);
        
        card.add(formPanel, BorderLayout.CENTER);

        // Button Panel
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        // Buttons row
        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        buttonRow.setOpaque(false);
        
        JButton btnLogin = Utils.primaryButton("Masuk");
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogin.setPreferredSize(new Dimension(180, 48));
        
        JButton btnReg = Utils.ghostButton("Buat Akun Baru");
        btnReg.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnReg.setPreferredSize(new Dimension(180, 48));
        
        btnLogin.addActionListener(e -> doLogin());
        btnReg.addActionListener(e -> { dispose(); new RegisterPage(); });
        
        buttonRow.add(btnLogin);
        buttonRow.add(Box.createHorizontalStrut(15));
        buttonRow.add(btnReg);
        
        bottomPanel.add(buttonRow);
        
        card.add(bottomPanel, BorderLayout.SOUTH);
        
        leftPanel.add(card, BorderLayout.CENTER);

        // ========== RIGHT PANEL: HERO ==========
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(Utils.BG);
        
        // Gradient Panel
        JPanel heroPanel = new JPanel() {
            @Override 
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(
                    0, 0, Utils.CARD2, 
                    getWidth(), getHeight(), Utils.BG
                );
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        heroPanel.setLayout(new BorderLayout());
        heroPanel.setBorder(BorderFactory.createEmptyBorder(80, 60, 80, 60));
        
        // Logo di tengah atas
        JLabel bigLogoLabel = new JLabel("", SwingConstants.CENTER);
        try {
            URL imageURL = getClass().getResource("/nahlib/nahsazlibrary.png");
            if (imageURL != null) {
                ImageIcon largeLogoIcon = new ImageIcon(imageURL);
                Image largeScaledImage = largeLogoIcon.getImage().getScaledInstance(180, 180, Image.SCALE_SMOOTH);
                ImageIcon largeLogo = new ImageIcon(largeScaledImage);
                bigLogoLabel.setIcon(largeLogo);
            }
        } catch (Exception e) {
            // Fallback jika logo tidak ditemukan
            bigLogoLabel.setText("📚");
            bigLogoLabel.setFont(new Font("Segoe UI", Font.BOLD, 60));
            bigLogoLabel.setForeground(Utils.TEXT);
        }
        bigLogoLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));
        
        // Content panel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        
        // Title
       JLabel heroTitle = new JLabel(Utils.getLibraryName());
        heroTitle.setFont(new Font("Segoe UI", Font.BOLD, 30));
        heroTitle.setForeground(Utils.TEXT);
        heroTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel heroSub = new JLabel("Sistem manajemen perpustakaan modern dengan antarmuka intuitif dan fitur lengkap.");
        heroSub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        heroSub.setForeground(Utils.MUTED);
        heroSub.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Features list
        JPanel featuresPanel = new JPanel();
        featuresPanel.setLayout(new BoxLayout(featuresPanel, BoxLayout.Y_AXIS));
        featuresPanel.setOpaque(false);
        featuresPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        featuresPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        
        String[] features = {
            "Manajemen buku dan peminjaman",
            "Notifikasi jatuh tempo otomatis",
            "Laporan dan statistik lengkap",
            "Multi-role: Admin, Petugas, Anggota"
        };
        
        for (String feature : features) {
            JLabel featureLabel = new JLabel(feature);
            featureLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            featureLabel.setForeground(Utils.MUTED);
            featureLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            featureLabel.setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));
            featuresPanel.add(featureLabel);
        }

        contentPanel.add(heroTitle);
        contentPanel.add(Box.createVerticalStrut(15));
        contentPanel.add(heroSub);
        contentPanel.add(Box.createVerticalStrut(20));
        contentPanel.add(featuresPanel);
        
        heroPanel.add(bigLogoLabel, BorderLayout.NORTH);
        heroPanel.add(contentPanel, BorderLayout.CENTER);

        rightPanel.add(heroPanel, BorderLayout.CENTER);

        mainContainer.add(leftPanel);
        mainContainer.add(rightPanel);

        root.add(mainContainer, BorderLayout.CENTER);

        setContentPane(root);
        setVisible(true);
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(Utils.MUTED);
        return label;
    }

    private void doLogin() {
        try {
            String u = tfUser.getText().trim();
            String p = new String(tfPass.getPassword());
            if (u.isEmpty() || p.isEmpty()) { 
                Utils.msg("Username dan password wajib diisi."); 
                return; 
            }

            // Menggunakan DB.query dari DB.java yang sudah ada
            List<Map<String,String>> rows = DB.query(
                    "SELECT * FROM users WHERE username = ? AND password_hash = ? AND status_aktif = 1",
                    u, Utils.sha256(p)
            );
            
            if (rows.isEmpty()) { 
                Utils.msg("Login gagal. Cek username/password."); 
                return; 
            }

            Map<String,String> me = rows.get(0);
            
            // Menggunakan DB.audit dari DB.java yang sudah ada
            Long userId = Long.parseLong(me.get("user_id"));
            DB.audit(userId, "LOGIN", "users", me.get("user_id"), "Login sukses");

            dispose();
            String role = me.get("role");
            
            // Navigasi berdasarkan role
            if ("ADMIN".equalsIgnoreCase(role)) {
                new AdminPage(me);
            } else if ("PETUGAS".equalsIgnoreCase(role)) {
                new PetugasPage(me);
            } else {
                // Default: ANGGOTA/USER
                new UserPage(me);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            Utils.msg("Terjadi error saat login: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        // Koneksi database saat aplikasi dimulai
        try {
            DB.connect();
            System.out.println("Database connected successfully!");
            
            // Tampilkan halaman login
            SwingUtilities.invokeLater(() -> {
                new LoginPage();
            });
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, 
                "Gagal terhubung ke database. Pastikan database berjalan.\n" + e.getMessage(), 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }
}