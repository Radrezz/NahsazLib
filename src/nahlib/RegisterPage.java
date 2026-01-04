package nahlib;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class RegisterPage extends JFrame {

    private final JTextField tfUsername = Utils.input("");
    private final JPasswordField tfPass = Utils.passInput("");
    private final JTextField tfNama = Utils.input("");
    private final JTextField tfKelas = Utils.input("");
    private final JComboBox<String> cbGender = new JComboBox<>(new String[]{"Laki-laki", "Perempuan"});
    private final JTextField tfTelp = Utils.input("");
    private final JTextField tfEmail = Utils.input("");
    private final JTextArea taAlamat = new JTextArea(3, 20);
    private final JCheckBox showPasswordCheck = new JCheckBox("Show Password");

    public RegisterPage() {
        setTitle(Utils.getLibraryName() + " - Register");
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        
        // Set agar langsung maximize
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        Utils.numericOnly(tfTelp);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Utils.BG);

        // Container utama
        JPanel mainContainer = new JPanel(new GridLayout(1, 2, 0, 0));
        mainContainer.setBackground(Utils.BG);

        // ========== LEFT PANEL: FORM ==========
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(Utils.BG);
        leftPanel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 30));

        // Gunakan ScrollPane untuk form yang panjang
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Utils.BG);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Utils.BG);
        
        JPanel card = Utils.card();
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout(10, 0));
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);
        
        JLabel h = new JLabel("Daftar Anggota");
        h.setForeground(Utils.TEXT);
        h.setFont(new Font("Segoe UI", Font.BOLD, 26));
        h.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel s = new JLabel("Buat akun baru untuk mengakses layanan perpustakaan");
        s.setForeground(Utils.MUTED);
        s.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        s.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        titlePanel.add(h);
        titlePanel.add(Box.createVerticalStrut(6));
        titlePanel.add(s);
        
        headerPanel.add(titlePanel, BorderLayout.CENTER);
        
        card.add(headerPanel, BorderLayout.NORTH);

        // Form fields dengan GridLayout 2 kolom
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        formPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 15, 0));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 0, 8, 15);
        gbc.weightx = 1.0;
        
        // Style untuk semua komponen
        Component[] fields = {tfUsername, tfPass, tfNama, tfKelas, tfTelp, tfEmail};
        for (Component field : fields) {
            field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            if (field instanceof JTextField || field instanceof JPasswordField) {
                ((JComponent) field).setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Utils.BORDER, 1),
                    BorderFactory.createEmptyBorder(8, 10, 8, 10)
                ));
                ((JComponent) field).setPreferredSize(new Dimension(0, 38));
            }
        }
        
        // Password field styling
        tfPass.setEchoChar('•');
        
        // ComboBox styling - hanya Laki-laki dan Perempuan sesuai database
        cbGender.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cbGender.setBackground(Utils.CARD2);
        cbGender.setForeground(Utils.TEXT);
        cbGender.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Utils.BORDER, 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        cbGender.setPreferredSize(new Dimension(0, 38));
        
        // TextArea untuk alamat
        taAlamat.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        taAlamat.setLineWrap(true);
        taAlamat.setWrapStyleWord(true);
        taAlamat.setBackground(Utils.CARD2);
        taAlamat.setForeground(Utils.TEXT);
        taAlamat.setCaretColor(Utils.TEXT);
        taAlamat.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Utils.BORDER, 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        JScrollPane scrollAlamat = new JScrollPane(taAlamat);
        scrollAlamat.setBorder(null);
        scrollAlamat.getViewport().setBackground(Utils.CARD2);
        scrollAlamat.setPreferredSize(new Dimension(0, 80));
        
        // Baris 1: Username dan Password
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 1;
        formPanel.add(createLabel("Username *"), gbc);
        gbc.gridx = 1;
        formPanel.add(createLabel("Password *"), gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(tfUsername, gbc);
        gbc.gridx = 1;
        formPanel.add(tfPass, gbc);
        
        // Show Password Checkbox
        gbc.gridx = 1; gbc.gridy = 2;
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
        
        // Reset gbc untuk baris berikutnya
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 1;
        
        // Baris 4: Nama dan Kelas
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(createLabel("Nama Lengkap *"), gbc);
        gbc.gridx = 1;
        formPanel.add(createLabel("Kelas"), gbc);
        
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(tfNama, gbc);
        gbc.gridx = 1;
        formPanel.add(tfKelas, gbc);
        
        // Baris 5: Gender dan Telp
        gbc.gridx = 0; gbc.gridy = 5;
        formPanel.add(createLabel("Jenis Kelamin"), gbc);
        gbc.gridx = 1;
        formPanel.add(createLabel("No. Telepon"), gbc);
        
        gbc.gridx = 0; gbc.gridy = 6;
        formPanel.add(cbGender, gbc);
        gbc.gridx = 1;
        formPanel.add(tfTelp, gbc);
        
        // Baris 6: Email (full width)
        gbc.gridx = 0; gbc.gridy = 7;
        gbc.gridwidth = 2;
        formPanel.add(createLabel("Email"), gbc);
        
        gbc.gridy = 8;
        formPanel.add(tfEmail, gbc);
        
        // Baris 7: Alamat (full width)
        gbc.gridy = 9;
        formPanel.add(createLabel("Alamat"), gbc);
        
        gbc.gridy = 10;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 0.5;
        formPanel.add(scrollAlamat, gbc);
        
        card.add(formPanel, BorderLayout.CENTER);

        // Button Panel
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        
        // Buttons
        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        buttonRow.setOpaque(false);
        
        JButton submit = Utils.primaryButton("Daftar Sekarang");
        submit.setFont(new Font("Segoe UI", Font.BOLD, 14));
        submit.setPreferredSize(new Dimension(160, 42));
        
        JButton back = Utils.ghostButton("← Kembali ke Login");
        back.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        back.setPreferredSize(new Dimension(160, 42));
        
        submit.addActionListener(e -> doRegister());
        back.addActionListener(e -> { dispose(); new LoginPage(); });
        
        buttonRow.add(submit);
        buttonRow.add(Box.createHorizontalStrut(12));
        buttonRow.add(back);
        
        // Info text
        JLabel infoLabel = new JLabel("* Menandakan field yang wajib diisi");
        infoLabel.setFont(new Font("Segoe UI", Font.ITALIC, 9));
        infoLabel.setForeground(Utils.MUTED);
        infoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        bottomPanel.add(infoLabel);
        bottomPanel.add(Box.createVerticalStrut(6));
        bottomPanel.add(buttonRow);
        
        card.add(bottomPanel, BorderLayout.SOUTH);
        
        contentPanel.add(card, BorderLayout.CENTER);
        scrollPane.setViewportView(contentPanel);
        leftPanel.add(scrollPane, BorderLayout.CENTER);

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
        heroPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));
        
        // Logo di atas
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
            bigLogoLabel.setFont(new Font("Segoe UI", Font.BOLD, 50));
            bigLogoLabel.setForeground(Utils.TEXT);
        }
        bigLogoLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 25, 0));
        
        // Content panel
        JPanel contentPanelRight = new JPanel();
        contentPanelRight.setLayout(new BoxLayout(contentPanelRight, BoxLayout.Y_AXIS));
        contentPanelRight.setOpaque(false);
        
        // Title
        JLabel heroTitle = new JLabel("Bergabung dengan Komunitas Membaca");
        heroTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        heroTitle.setForeground(Utils.TEXT);
        heroTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel heroSub = new JLabel("Dengan menjadi anggota, Anda mendapatkan akses ke:");
        heroSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        heroSub.setForeground(Utils.MUTED);
        heroSub.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Features list
        JPanel featuresPanel = new JPanel();
        featuresPanel.setLayout(new BoxLayout(featuresPanel, BoxLayout.Y_AXIS));
        featuresPanel.setOpaque(false);
        featuresPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        featuresPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        
        String[] features = {
            "Koleksi buku terlengkap",
            "Sistem peminjaman online",
            "Notifikasi dan pengingat",
            "Riwayat baca pribadi"
        };
        
        for (String feature : features) {
            JLabel featureLabel = new JLabel(feature);
            featureLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            featureLabel.setForeground(Utils.MUTED);
            featureLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            featureLabel.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
            featuresPanel.add(featureLabel);
        }

        contentPanelRight.add(heroTitle);
        contentPanelRight.add(Box.createVerticalStrut(10));
        contentPanelRight.add(heroSub);
        contentPanelRight.add(Box.createVerticalStrut(15));
        contentPanelRight.add(featuresPanel);
        
        heroPanel.add(bigLogoLabel, BorderLayout.NORTH);
        heroPanel.add(contentPanelRight, BorderLayout.CENTER);

        rightPanel.add(heroPanel, BorderLayout.CENTER);

        mainContainer.add(leftPanel);
        mainContainer.add(rightPanel);

        root.add(mainContainer, BorderLayout.CENTER);

        setContentPane(root);
        setVisible(true);
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(Utils.MUTED);
        return label;
    }

    private void doRegister() {
        try {
            String username = tfUsername.getText().trim();
            String pass = new String(tfPass.getPassword());
            String nama = tfNama.getText().trim();
            String kelas = tfKelas.getText().trim();
            String gender = (String) cbGender.getSelectedItem();
            String telp = tfTelp.getText().trim();
            String email = tfEmail.getText().trim();
            String alamat = taAlamat.getText().trim();

            // Validasi input
            if (username.isEmpty() || pass.isEmpty() || nama.isEmpty()) {
                Utils.msg("Username, password, dan nama lengkap wajib diisi.");
                return;
            }

            if (pass.length() < 6) {
                Utils.msg("Password harus minimal 6 karakter.");
                return;
            }

            // Validasi gender sesuai dengan ENUM di database
            if (!gender.equals("Laki-laki") && !gender.equals("Perempuan")) {
                Utils.msg("Jenis kelamin harus Laki-laki atau Perempuan.");
                return;
            }

            // Cek apakah username sudah ada
            List<Map<String, String>> existingUsers = DB.query(
                "SELECT user_id FROM users WHERE username = ?", 
                username
            );
            
            if (!existingUsers.isEmpty()) {
                Utils.msg("Username sudah digunakan. Silakan pilih username lain.");
                return;
            }

            // Insert user baru menggunakan DB.exec()
            // Sesuai dengan struktur database: password_hash VARCHAR(255)
            long newUserId = DB.exec(
                "INSERT INTO users(username, password_hash, role, kelas, gender, no_telp, alamat, email, nama_lengkap, status_aktif) " +
                "VALUES (?, ?, 'USER', ?, ?, ?, ?, ?, ?, 1)",
                username, Utils.sha256(pass), kelas, gender, telp, alamat, email, nama
            );

            if (newUserId > 0) {
                // Log audit menggunakan DB.audit()
                DB.audit(newUserId, "REGISTER", "users", String.valueOf(newUserId), "Registrasi anggota baru");
                
                Utils.msg("Registrasi berhasil! Akun Anda telah aktif. Silakan login.");
                dispose();
                new LoginPage();
            } else {
                Utils.msg("Registrasi gagal. Silakan coba lagi.");
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            Utils.msg("Registrasi gagal: " + ex.getMessage());
        }
    }
    
    // Main method untuk testing
    public static void main(String[] args) {
        // Koneksi database saat aplikasi dimulai
        try {
            DB.connect();
            System.out.println("Database connected successfully!");
            
            // Tampilkan halaman registrasi
            SwingUtilities.invokeLater(() -> {
                new RegisterPage();
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