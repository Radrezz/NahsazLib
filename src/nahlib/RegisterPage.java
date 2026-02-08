package nahlib;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class RegisterPage extends JFrame {

    private final JTextField tfUsername = Utils.input("");
    private final JPasswordField tfPass = Utils.passInput("");
    private final JTextField tfNama = Utils.input("");
    private final JComboBox<String> cbKelas = new JComboBox<>();
    private final JComboBox<String> cbGender = new JComboBox<>(new String[]{"Laki-laki", "Perempuan"});
    private final JTextField tfTelp = Utils.input("");
    private final JTextField tfEmail = Utils.input("");
    private final JTextArea taAlamat = new JTextArea(3, 20);
    private final JCheckBox showPasswordCheck = new JCheckBox("Show Password");

    public RegisterPage() {
        setUndecorated(true);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        
        // Populate class options (VII-A to IX-E)
        cbKelas.addItem(""); // Empty option
        String[] grades = {"VII", "VIII", "IX"};
        String[] sections = {"A", "B", "C", "D", "E"};
        for (String grade : grades) {
            for (String section : sections) {
                cbKelas.addItem(grade + "-" + section);
            }
        }
        
        Utils.numericOnly(tfTelp);

        JPanel root = new JPanel(new GridLayout(1, 2, 0, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(25, 28, 40), getWidth(), getHeight(), new Color(40, 45, 62));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                
                g2.setColor(new Color(255, 255, 255, 6));
                g2.fillOval(-150, -150, 450, 450);
                g2.fillOval(getWidth()-400, getHeight()-350, 600, 600);
            }
        };

        // ========== LEFT SIDE: FULL HEIGHT SPLIT ==========
        JPanel leftSide = new JPanel(new GridBagLayout());
        leftSide.setOpaque(false);
        leftSide.setBorder(BorderFactory.createEmptyBorder(0, 30, 0, 15));

        // Glass Card Wrapper
        JPanel cardWrap = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 10));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        cardWrap.setOpaque(false);
        cardWrap.setPreferredSize(new Dimension(550, 650));

        JPanel card = new JPanel(new BorderLayout(0, 15));
        card.setOpaque(false);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 255, 255, 22), 1),
            BorderFactory.createEmptyBorder(25, 30, 25, 30)
        ));
        cardWrap.add(card);

        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);
        
        JLabel h = new JLabel("Join Membership");
        h.setForeground(Color.WHITE);
        h.setFont(new Font("Segoe UI", Font.BOLD, 28));
        
        JLabel s = new JLabel("Complete the form to create your library account");
        s.setForeground(new Color(176, 179, 190));
        s.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        
        headerPanel.add(h);
        headerPanel.add(Box.createVerticalStrut(4));
        headerPanel.add(s);
        
        card.add(headerPanel, BorderLayout.NORTH);

        // Compact Grid Form
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.5;
        gbc.insets = new Insets(0, 5, 10, 5);

        // Style all inputs
        styleField(tfUsername);
        styleField(tfPass);
        styleField(tfNama);
        styleField(tfTelp);
        styleField(tfEmail);
        
        // Style combo boxes
        styleComboBox(cbKelas);
        styleComboBox(cbGender);
        
        // Fix background rendering artifacts
        JTextField[] tfs = {tfUsername, tfNama, tfTelp, tfEmail};
        for (JTextField f : tfs) f.setOpaque(false);
        tfPass.setOpaque(false);
        
        // Enter Navigation (Chain)
        tfUsername.addActionListener(e -> tfPass.requestFocus());
        tfPass.addActionListener(e -> tfNama.requestFocus());
        tfNama.addActionListener(e -> cbKelas.requestFocus());
        tfTelp.addActionListener(e -> tfEmail.requestFocus());
        tfEmail.addActionListener(e -> taAlamat.requestFocus());
        
        taAlamat.setOpaque(false);
        taAlamat.setBackground(new Color(255, 255, 255, 14));
        taAlamat.setForeground(Color.WHITE);
        taAlamat.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        taAlamat.setCaretColor(Color.WHITE);
        taAlamat.setLineWrap(true);
        taAlamat.setWrapStyleWord(true);
        taAlamat.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));
        
        JScrollPane scrollAlamat = new JScrollPane(taAlamat);
        scrollAlamat.setOpaque(false);
        scrollAlamat.getViewport().setOpaque(false);
        scrollAlamat.setBorder(BorderFactory.createLineBorder(new Color(255, 255, 255, 25)));
        scrollAlamat.setPreferredSize(new Dimension(0, 80)); // Slightly taller for better UX

        // Row 1: Username & Password
        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(createLabel("USERNAME *"), gbc);
        gbc.gridx = 1; formPanel.add(createLabel("PASSWORD *"), gbc);
        gbc.gridx = 0; gbc.gridy = 1; formPanel.add(tfUsername, gbc);
        gbc.gridx = 1; formPanel.add(tfPass, gbc);

        // Row 2: Show Pass
        gbc.gridx = 1; gbc.gridy = 2; gbc.insets = new Insets(-5, 5, 5, 5);
        showPasswordCheck.setOpaque(false);
        showPasswordCheck.setForeground(new Color(176,179,190));
        showPasswordCheck.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        showPasswordCheck.addActionListener(e -> tfPass.setEchoChar(showPasswordCheck.isSelected() ? (char)0 : '•'));
        formPanel.add(showPasswordCheck, gbc);

        // Row 3: Nama & Kelas
        gbc.insets = new Insets(0, 5, 10, 5);
        gbc.gridx = 0; gbc.gridy = 3; formPanel.add(createLabel("FULL NAME *"), gbc);
        gbc.gridx = 1; formPanel.add(createLabel("CLASS"), gbc);
        gbc.gridx = 0; gbc.gridy = 4; formPanel.add(tfNama, gbc);
        gbc.gridx = 1; formPanel.add(cbKelas, gbc);

        // Row 4: Gender & Telp
        gbc.gridx = 0; gbc.gridy = 5; formPanel.add(createLabel("GENDER"), gbc);
        gbc.gridx = 1; gbc.gridy = 5; formPanel.add(createLabel("PHONE"), gbc);
        gbc.gridx = 0; gbc.gridy = 6; formPanel.add(cbGender, gbc);
        gbc.gridx = 1; formPanel.add(tfTelp, gbc);

        // Row 5: Email
        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 2; formPanel.add(createLabel("EMAIL ADDRESS"), gbc);
        gbc.gridy = 8; formPanel.add(tfEmail, gbc);

        // Row 6: Alamat
        gbc.gridy = 9; formPanel.add(createLabel("ADDRESS"), gbc);
        gbc.gridy = 10; formPanel.add(scrollAlamat, gbc);

        card.add(formPanel, BorderLayout.CENTER);

        // Footer Actions
        JPanel footer = new JPanel(new BorderLayout(0, 10));
        footer.setOpaque(false);
        
        JButton btnSubmit = Utils.primaryButton("Create Account");
        btnSubmit.setPreferredSize(new Dimension(0, 42));
        btnSubmit.addActionListener(e -> doRegister());
        
        JButton btnBack = new JButton("Already have an account? Sign In");
        btnBack.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnBack.setForeground(new Color(66, 133, 244));
        btnBack.setContentAreaFilled(false);
        btnBack.setBorderPainted(false);
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBack.addActionListener(e -> { dispose(); new LoginPage(); });
        
        footer.add(btnSubmit, BorderLayout.NORTH);
        footer.add(btnBack, BorderLayout.SOUTH);
        
        card.add(footer, BorderLayout.SOUTH);
        
        GridBagConstraints gbcL = new GridBagConstraints();
        gbcL.gridy = 0; gbcL.weighty = 1.0; gbcL.fill = GridBagConstraints.NONE;
        gbcL.anchor = GridBagConstraints.CENTER;
        leftSide.add(cardWrap, gbcL);

        // ========== RIGHT SIDE: FULL HERO INFO ==========
        JPanel rightSide = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(255, 255, 255, 3));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        rightSide.setOpaque(false);
        rightSide.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 20)); // Minimal top-right corner gap

        // Red Circular Exit Button
        JButton btnClose = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Red Circle Background
                g2.setColor(getModel().isRollover() ? new Color(255, 80, 80) : new Color(220, 50, 50));
                g2.fillOval(2, 2, getWidth()-4, getHeight()-4);
                
                // White X
                g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.setColor(Color.WHITE);
                int p = 10;
                int sz = getWidth() - (p * 2);
                g2.drawLine(p, p, p + sz, p + sz);
                g2.drawLine(p + sz, p, p, p + sz);
                
                g2.dispose();
            }
        };
        btnClose.setPreferredSize(new Dimension(34, 34));
        btnClose.setContentAreaFilled(false);
        btnClose.setBorderPainted(false);
        btnClose.setFocusPainted(false);
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> {
            if (Utils.confirm(this, "Apakah Anda yakin ingin keluar?")) System.exit(0);
        });
        
        // Absolute corner placement
        JPanel closePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        closePanel.setOpaque(false);
        closePanel.add(btnClose);

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);
        
        JLabel logo = new JLabel();
        try {
            java.io.File f = new java.io.File("src/nahlib/nahsazlibrary.png");
            Image img = null;
            if (f.exists()) {
                ImageIcon ic = new ImageIcon(f.getAbsolutePath());
                ic.getImage().flush(); // Clear Java's image cache
                img = ic.getImage();
            } else {
                java.net.URL url = getClass().getResource("/nahlib/nahsazlibrary.png");
                if (url != null) img = new ImageIcon(url).getImage();
            }
            if (img != null) {
                logo.setIcon(new ImageIcon(Utils.makeCircularImage(img, 240)));
            }
        } catch (Exception e) {}
        logo.setAlignmentX(CENTER_ALIGNMENT);
        
        JLabel brand = new JLabel("Start Your Journey");
        brand.setFont(new Font("Segoe UI", Font.BOLD, 48));
        brand.setForeground(Color.WHITE);
        brand.setAlignmentX(CENTER_ALIGNMENT);
        
        JLabel sub = new JLabel("<html><center>Unlock thousands of books and resources.<br><font color='#A8B0B8'>Bergabung dengan komunitas kami hari ini.</font></center></html>");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        sub.setForeground(new Color(176, 179, 190));
        sub.setAlignmentX(CENTER_ALIGNMENT);
        sub.setHorizontalAlignment(SwingConstants.CENTER);
        sub.setMaximumSize(new Dimension(800, 60));
        
        infoPanel.add(logo);
        infoPanel.add(Box.createVerticalStrut(30));
        infoPanel.add(brand);
        infoPanel.add(Box.createVerticalStrut(15));
        infoPanel.add(sub);
        infoPanel.add(Box.createVerticalStrut(60));

        // Real Benefits Descriptions
        String[][] benefits = {
            {"Katalog Terpadu", "Akses ribuan data buku fisik dalam satu platform manajemen."},
            {"Manajemen Efisien", "Proses sirkulasi buku yang termonitor dengan baik untuk tiap anggota."},
            {"Sistem Notifikasi", "Membantu petugas memantau keterlambatan pengembalian buku."}
        };

        for (String[] b : benefits) {
            JPanel fBox = new JPanel(new BorderLayout(20, 0));
            fBox.setOpaque(false);
            fBox.setMaximumSize(new Dimension(500, 85));
            fBox.setAlignmentX(CENTER_ALIGNMENT);
            
            JLabel dot = new JLabel("●");
            dot.setForeground(new Color(66, 133, 244));
            dot.setFont(new Font("Arial", Font.BOLD, 20));
            dot.setVerticalAlignment(SwingConstants.TOP);
            
            JPanel fTxt = new JPanel();
            fTxt.setLayout(new BoxLayout(fTxt, BoxLayout.Y_AXIS));
            fTxt.setOpaque(false);
            
            JLabel fTitle = new JLabel(b[0]);
            fTitle.setForeground(Color.WHITE);
            fTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
            
            JLabel fDesc = new JLabel("<html><div style='width: 360px; line-height: 1.3;'>" + b[1] + "</div></html>");
            fDesc.setForeground(new Color(176, 179, 190));
            fDesc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            
            fTxt.add(fTitle);
            fTxt.add(Box.createVerticalStrut(5));
            fTxt.add(fDesc);
            
            fBox.add(dot, BorderLayout.WEST);
            fBox.add(fTxt, BorderLayout.CENTER);
            fBox.setBorder(BorderFactory.createEmptyBorder(0, 0, 25, 0));
            
            infoPanel.add(fBox);
        }
        
        GridBagConstraints gbcR = new GridBagConstraints();
        gbcR.gridx = 0; gbcR.gridy = 0; gbcR.weightx = 1.0; gbcR.weighty = 0.0;
        gbcR.anchor = GridBagConstraints.NORTHEAST;
        rightSide.add(closePanel, gbcR);

        gbcR.gridy = 1; gbcR.weighty = 1.0; gbcR.anchor = GridBagConstraints.CENTER;
        rightSide.add(infoPanel, gbcR);

        root.add(leftSide);
        root.add(rightSide);

        setContentPane(root);
        setVisible(true);
    }

    private void styleField(JTextField f) {
        f.setOpaque(false); 
        f.setBackground(new Color(255, 255, 255, 14));
        f.setForeground(Color.WHITE);
        f.setCaretColor(Color.WHITE);
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 255, 255, 25), 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        f.setPreferredSize(new Dimension(0, 35));
        
        if (f instanceof JPasswordField) {
            f.setUI(new javax.swing.plaf.basic.BasicPasswordFieldUI() {
                @Override
                protected void paintBackground(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(f.getBackground());
                    g2.fillRoundRect(0, 0, f.getWidth(), f.getHeight(), 5, 5);
                }
            });
        } else {
            f.setUI(new javax.swing.plaf.basic.BasicTextFieldUI() {
                @Override
                protected void paintBackground(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(f.getBackground());
                    g2.fillRoundRect(0, 0, f.getWidth(), f.getHeight(), 5, 5);
                }
            });
        }
    }

    private void styleComboBox(JComboBox<?> cb) {
        cb.setBackground(new Color(255, 255, 255, 14));
        cb.setForeground(Color.WHITE);
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cb.setBorder(BorderFactory.createLineBorder(new Color(255, 255, 255, 25)));
        cb.setFocusable(false);
        cb.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                l.setBackground(isSelected ? new Color(66, 133, 244) : new Color(40, 45, 62));
                l.setForeground(Color.WHITE);
                l.setBorder(new EmptyBorder(5, 10, 5, 10));
                return l;
            }
        });
        
        // Fix for unreadable white popup background
        Object child = cb.getAccessibleContext().getAccessibleChild(0);
        if (child instanceof javax.swing.plaf.basic.BasicComboPopup) {
            ((javax.swing.plaf.basic.BasicComboPopup)child).getList().setBackground(new Color(40, 45, 62));
            ((javax.swing.plaf.basic.BasicComboPopup)child).getList().setForeground(Color.WHITE);
        }
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 10));
        label.setForeground(new Color(255, 255, 255, 130));
        return label;
    }

    private void doRegister() {
        try {
            String username = tfUsername.getText().trim();
            String pass = new String(tfPass.getPassword());
            String nama = tfNama.getText().trim();
            String kelas = (String) cbKelas.getSelectedItem();
            if (kelas != null) kelas = kelas.trim();
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