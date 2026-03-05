package nahlib;

import nahlib.admin.AdminPage;
import nahlib.petugas.PetugasPage;
import nahlib.user.UserPage;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.net.URL;
import nahlib.CustomIcon;
import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

public class LoginPage extends JFrame {

    private final JTextField tfUser = Utils.input("");
    private final JPasswordField tfPass = Utils.passInput("");
    private final JCheckBox showPasswordCheck = new JCheckBox("Show Password");

    public LoginPage() {
        setUndecorated(true);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        
        JPanel root = new JPanel(new GridLayout(1, 2, 0, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(25, 28, 40), getWidth(), getHeight(), new Color(40, 45, 62));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                
                g2.setColor(new Color(255, 255, 255, 5));
                g2.fillOval(-100, -100, 400, 400);
                g2.fillOval(getWidth()-300, getHeight()-300, 500, 500);
            }
        };

        // ========== LEFT SIDE: CENTERED FORM ==========
        JPanel leftSide = new JPanel(new GridBagLayout());
        leftSide.setOpaque(false);
        leftSide.setBorder(BorderFactory.createEmptyBorder(0, 40, 0, 40));

        // Glass Card Wrapper
        JPanel cardWrapper = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Frost effect
                g2.setColor(new Color(255, 255, 255, 10));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        cardWrapper.setOpaque(false);
        cardWrapper.setPreferredSize(new Dimension(480, 550));

        JPanel card = new JPanel(new BorderLayout(0, 20));
        card.setOpaque(false);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 255, 255, 20), 1),
            BorderFactory.createEmptyBorder(40, 45, 40, 45)
        ));
        cardWrapper.add(card);
        
        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);
        
        JLabel h = new JLabel("Welcome Back");
        h.setForeground(Color.WHITE);
        h.setFont(new Font("Segoe UI", Font.BOLD, 32));
        h.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel s = new JLabel("Please enter your details to sign in");
        s.setForeground(new Color(176, 179, 190));
        s.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        s.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        headerPanel.add(h);
        headerPanel.add(Box.createVerticalStrut(5));
        headerPanel.add(s);
        
        card.add(headerPanel, BorderLayout.NORTH);

        // Form
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        
        // Input Fields
        styleField(tfUser, "Username");
        styleField(tfPass, "Password");
        tfPass.setEchoChar('•');
        
        gbc.gridy = 0; gbc.insets = new Insets(0, 0, 8, 0);
        formPanel.add(createLabel("USERNAME"), gbc);
        gbc.gridy = 1; gbc.insets = new Insets(0, 0, 20, 0);
        tfUser.addActionListener(e -> tfPass.requestFocus());
        tfPass.addActionListener(e -> doLogin());
        
        formPanel.add(tfUser, gbc);
        
        gbc.gridy = 2; gbc.insets = new Insets(0, 0, 8, 0);
        formPanel.add(createLabel("PASSWORD"), gbc);
        gbc.gridy = 3; gbc.insets = new Insets(0, 0, 10, 0);
        formPanel.add(tfPass, gbc);
        
        // Options row (Show Password)
        JPanel optRow = new JPanel(new BorderLayout());
        optRow.setOpaque(false);
        showPasswordCheck.setOpaque(false);
        showPasswordCheck.setForeground(new Color(176, 179, 190));
        showPasswordCheck.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        showPasswordCheck.setFocusPainted(false);
        showPasswordCheck.addActionListener(e -> tfPass.setEchoChar(showPasswordCheck.isSelected() ? (char)0 : '•'));
        optRow.add(showPasswordCheck, BorderLayout.WEST);
        
        JButton btnForgot = new JButton("Lupa Sandi?");
        btnForgot.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnForgot.setForeground(new Color(230, 50, 50));
        btnForgot.setContentAreaFilled(false);
        btnForgot.setBorderPainted(false);
        btnForgot.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnForgot.addActionListener(e -> showForgotWindow());
        optRow.add(btnForgot, BorderLayout.EAST);
        
        gbc.gridy = 4; gbc.insets = new Insets(0, 0, 25, 0);
        formPanel.add(optRow, gbc);
        
        card.add(formPanel, BorderLayout.CENTER);
        
        // Fix background rendering artifacts
        tfUser.setOpaque(false);
        tfPass.setOpaque(false);
        
        // Ensure inputs are correctly transparent to fix rendering artifacts
        tfUser.setOpaque(false);
        tfPass.setOpaque(false);

        // Buttons
        JPanel btnPanel = new JPanel(new GridLayout(1, 1, 0, 12));
        btnPanel.setOpaque(false);
        
        JButton btnLogin = Utils.primaryButton("Sign In");
        btnLogin.setPreferredSize(new Dimension(0, 45));
        btnLogin.addActionListener(e -> doLogin());
        
        btnPanel.add(btnLogin);
        
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footerPanel.setOpaque(false);
        JLabel noAcc = new JLabel("Don't have an account?");
        noAcc.setForeground(new Color(176, 179, 190));
        noAcc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        
        JButton btnReg = new JButton("Create Account");
        btnReg.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnReg.setForeground(new Color(66, 133, 244));
        btnReg.setContentAreaFilled(false);
        btnReg.setBorderPainted(false);
        btnReg.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnReg.addActionListener(e -> { dispose(); new RegisterPage(); });
        
        footerPanel.add(noAcc);
        footerPanel.add(btnReg);
        
        JPanel bottomWrap = new JPanel(new BorderLayout(0, 10));
        bottomWrap.setOpaque(false);
        bottomWrap.add(btnPanel, BorderLayout.NORTH);
        bottomWrap.add(footerPanel, BorderLayout.SOUTH);
        
        card.add(bottomWrap, BorderLayout.SOUTH);
        
        GridBagConstraints gbcL = new GridBagConstraints();
        gbcL.gridy = 0; gbcL.weighty = 1.0; gbcL.fill = GridBagConstraints.NONE;
        gbcL.anchor = GridBagConstraints.CENTER;
        leftSide.add(cardWrapper, gbcL);

        // ========== RIGHT SIDE: FULL HERO INFO ==========
        JPanel rightSide = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                // Subtle split background effect
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
                g2.setColor(getModel().isRollover() ? new Color(255, 80, 80) : new Color(230, 50, 50));
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
        
        // Hero Content
        JLabel logoIcon = new JLabel();
        logoIcon.setIcon(Utils.getAppLogo(240));
        logoIcon.setAlignmentX(CENTER_ALIGNMENT);
        
        JLabel brandNameLabel = new JLabel(Utils.getLibraryName());
        brandNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 48));
        brandNameLabel.setForeground(Color.WHITE);
        brandNameLabel.setAlignmentX(CENTER_ALIGNMENT);

        JLabel tagline = new JLabel("<html><center>Modern Library Management System<br><font color='#A8B0B8'>v2.1 Stable Release</font></center></html>", SwingConstants.CENTER);
        tagline.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        tagline.setForeground(new Color(176, 179, 190));
        tagline.setAlignmentX(CENTER_ALIGNMENT);
        tagline.setHorizontalAlignment(SwingConstants.CENTER);
        tagline.setMaximumSize(new Dimension(800, 60));

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        infoPanel.add(logoIcon);
        infoPanel.add(Box.createVerticalStrut(30));
        infoPanel.add(brandNameLabel);
        infoPanel.add(Box.createVerticalStrut(15));
        infoPanel.add(tagline);
        infoPanel.add(Box.createVerticalStrut(60));

        // Real Features Descriptions
        String[][] highlights = {
            {"Integrasi Data Terpadu", "Sistem manajemen koleksi buku dan anggota yang terstruktur dengan database real-time."},
            {"Sirkulasi Efisien", "Proses peminjaman dan pengembalian yang cepat dengan pencatatan otomatis."},
            {"Audit & Keamanan", "Seluruh transaksi terekam secara transparan untuk keamanan data perpustakaan."}
        };

        for (String[] hgh : highlights) {
            JPanel fBox = new JPanel(new BorderLayout(20, 0));
            fBox.setOpaque(false);
            fBox.setMaximumSize(new Dimension(500, 85));
            fBox.setAlignmentX(CENTER_ALIGNMENT);
            
            JLabel icon = new JLabel("●");
            icon.setForeground(new Color(66, 133, 244));
            icon.setFont(new Font("Arial", Font.BOLD, 20));
            icon.setVerticalAlignment(SwingConstants.TOP);
            
            JPanel fTxt = new JPanel();
            fTxt.setLayout(new BoxLayout(fTxt, BoxLayout.Y_AXIS));
            fTxt.setOpaque(false);
            
            JLabel fTitle = new JLabel(hgh[0]);
            fTitle.setForeground(Color.WHITE);
            fTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
            
            JLabel fDesc = new JLabel("<html><div style='width: 360px; line-height: 1.3;'>" + hgh[1] + "</div></html>");
            fDesc.setForeground(new Color(176, 179, 190));
            fDesc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            
            fTxt.add(fTitle);
            fTxt.add(Box.createVerticalStrut(5));
            fTxt.add(fDesc);
            
            fBox.add(icon, BorderLayout.WEST);
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

    private void styleField(JTextField f, String ph) {
        f.setOpaque(false); 
        f.setBackground(new Color(255, 255, 255, 12));
        f.setForeground(Color.WHITE);
        f.setCaretColor(Color.WHITE);
        f.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 255, 255, 25), 1),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));

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

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 10));
        label.setForeground(new Color(255, 255, 255, 150));
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

            // Login with Username OR Email
            List<Map<String,String>> rows = DB.query(
                    "SELECT * FROM users WHERE (username = ? OR email = ?) AND password_hash = ? AND status_aktif = 1",
                    u, u, Utils.sha256(p)
            );
            
            if (rows.isEmpty()) { 
                Utils.msg("Login gagal. Cek username/password."); 
                return; 
            }

            Map<String,String> me = rows.get(0);
            String role = me.get("role");
            
            // Check operational hours for PETUGAS and USER (not ADMIN)
            if (!"ADMIN".equalsIgnoreCase(role)) {
                if (!DB.isLibraryOpen()) {
                    String statusMsg = DB.getLibraryStatusMessage();
                    Utils.msg("<html><center><b>Perpustakaan Sedang Tutup</b><br><br>" +
                             statusMsg + "<br><br>" +
                             "Silakan login saat jam operasional perpustakaan.</center></html>");
                    return;
                }
            }
            
            // Menggunakan DB.audit dari DB.java yang sudah ada
            Long userId = Long.parseLong(me.get("user_id"));
            DB.audit(userId, "LOGIN", "users", me.get("user_id"), "Login sukses");

            dispose();
            
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

    private void showForgotWindow() {
        JDialog d = new JDialog(this, "Permintaan Lupa Sandi", true);
        d.setSize(400, 400);
        d.setLocationRelativeTo(this);
        d.setLayout(new GridBagLayout());
        d.getContentPane().setBackground(new Color(25, 28, 40));
        
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1.0;
        
        JLabel hint = new JLabel("<html><center>Masukkan Username/Email dan sandi baru Anda.<br>" +
                               "Permintaan akan dikirimkan ke Admin untuk disetujui.</center></html>");
        hint.setForeground(new Color(176, 179, 190));
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        hint.setHorizontalAlignment(SwingConstants.CENTER);
        
        g.gridy = 0; g.insets = new Insets(20, 20, 20, 20);
        d.add(hint, g);
        
        g.insets = new Insets(8, 20, 8, 20);
        JTextField tfId = Utils.input("Username/Email");
        JPasswordField tfNew = Utils.passInput("Sandi Baru");
        JPasswordField tfConf = Utils.passInput("Konfirmasi Sandi Baru");
        
        g.gridy = 1; d.add(createLabel("USERNAME / EMAIL"), g);
        g.gridy = 2; d.add(tfId, g);
        g.gridy = 3; d.add(createLabel("SANDI BARU"), g);
        g.gridy = 4; d.add(tfNew, g);
        g.gridy = 5; d.add(createLabel("KONFIRMASI SANDI"), g);
        g.gridy = 6; d.add(tfConf, g);
        
        JButton submit = Utils.primaryButton("Kirim Permintaan");
        submit.addActionListener(e -> {
            try {
                String id = tfId.getText().trim();
                String np = new String(tfNew.getPassword());
                String cp = new String(tfConf.getPassword());
                
                if (id.isEmpty() || np.isEmpty()) { Utils.msg("Mohon isi semua data."); return; }
                if (!np.equals(cp)) { Utils.msg("Konfirmasi sandi tidak sesuai."); return; }
                
                var userRow = DB.query("SELECT user_id FROM users WHERE username=? OR email=?", id, id);
                if (userRow.isEmpty()) { Utils.msg("User tidak ditemukan."); return; }
                
                int uid = Integer.parseInt(userRow.get(0).get("user_id"));
                DB.exec("INSERT INTO password_requests(user_id, new_password_hash) VALUES(?, ?)", 
                        uid, Utils.sha256(np));
                
                Utils.msg("Permintaan berhasil terkirim. Mohon tunggu konfirmasi Admin.");
                d.dispose();
            } catch (Exception ex) {
                ex.printStackTrace();
                Utils.msg("Gagal: " + ex.getMessage());
            }
        });
        
        g.gridy = 7; g.insets = new Insets(20, 20, 10, 20);
        d.add(submit, g);
        
        d.setVisible(true);
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