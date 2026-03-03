package nahlib;

import com.toedter.calendar.JDateChooser;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.text.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javax.imageio.ImageIO;

public class Utils {
    public static Image makeCircularImage(Image src, int size) {
        if (src == null) return null;
        
        // Ensure image is fully loaded
        src = new ImageIcon(src).getImage();
        int w = src.getWidth(null);
        int h = src.getHeight(null);
        
        if (w <= 0 || h <= 0) return null;
        
        // Create a high-quality scaled version first (Master Image)
        // We use a larger intermediate size (e.g., 4x target size) for better downsampling quality
        int renderSize = Math.max(size * 4, Math.min(w, h));
        
        BufferedImage master = new BufferedImage(renderSize, renderSize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = master.createGraphics();
        
        // Set Ultra-High Quality Rendering Hints
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        // Circular Clip
        g2.setClip(new Ellipse2D.Double(0, 0, renderSize, renderSize));
        
        // Draw image centered and covering the area (Cover Mode)
        // Calculate scaling to cover the renderSize
        double scale = Math.max((double)renderSize/w, (double)renderSize/h);
        int dw = (int)(w * scale);
        int dh = (int)(h * scale);
        int dx = (renderSize - dw) / 2;
        int dy = (renderSize - dh) / 2;
        
        g2.drawImage(src, dx, dy, dw, dh, null);
        
        g2.dispose();
        
        // High quality downscale to final target size
        return master.getScaledInstance(size, size, Image.SCALE_SMOOTH);
    }
    
    // Palette sesuai tema gelap + aksen biru
    public static final Color BG = new Color(0x28,0x2A,0x37);
    public static final Color CARD = new Color(0x30,0x34,0x42);
    public static final Color CARD2 = new Color(0x3A,0x3E,0x4B);
    public static final Color BORDER = new Color(0x46,0x4A,0x57);
    public static final Color TEXT = new Color(0xE2,0xE7,0xEC);
    public static final Color MUTED = new Color(0xA8,0xB0,0xB8);
    public static final Color ACCENT = new Color(0x21,0x8F,0xED);

    public static final Font FONT = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_B = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font H1 = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font H2 = new Font("Segoe UI", Font.BOLD, 16);

    // Padding & Margin constants for consistency
    public static final int PADDING = 20;
    public static final int MARGIN = 16;
    public static final EmptyBorder UI_PADDING = new EmptyBorder(PADDING, PADDING, PADDING, PADDING);
    public static final EmptyBorder UI_MARGIN = new EmptyBorder(MARGIN, MARGIN, MARGIN, MARGIN);
    
    private static Component parent;

    public static void applyBaseUI() {
        UIManager.put("Label.font", FONT);
        UIManager.put("Button.font", FONT_B);
        UIManager.put("TextField.font", FONT);
        UIManager.put("PasswordField.font", FONT);
        UIManager.put("Table.font", FONT);
        UIManager.put("TableHeader.font", FONT_B);
    }

    public static JPanel card() {
        JPanel p = new JPanel();
        p.setBackground(CARD);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(14,14,14,14)
        ));
        return p;
    }

    public static JButton primaryButton(String text) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setBackground(ACCENT);
        b.setForeground(Color.WHITE);
        b.setBorder(BorderFactory.createEmptyBorder(10,14,10,14));
        return b;
    }

    public static JButton ghostButton(String text) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setBackground(CARD2);
        b.setForeground(TEXT);
        b.setBorder(BorderFactory.createLineBorder(BORDER));
        return b;
    }

    public static JTextField input(String placeholder) {
        JTextField t = new JTextField();
        t.setBackground(CARD2);
        t.setForeground(TEXT);
        t.setCaretColor(TEXT);
        t.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(10,10,10,10)
        ));
        t.setToolTipText(placeholder);
        return t;
    }

    public static JPasswordField passInput(String placeholder) {
        JPasswordField t = new JPasswordField();
        t.setBackground(CARD2);
        t.setForeground(TEXT);
        t.setCaretColor(TEXT);
        t.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(10,10,10,10)
        ));
        t.setToolTipText(placeholder);
        return t;
    }

    public static JDateChooser dateChooser() {
        JDateChooser dc = new JDateChooser();
        dc.setDateFormatString("yyyy-MM-dd");
        dc.setBackground(CARD2);
        dc.setForeground(TEXT);
        dc.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER),
            new EmptyBorder(5, 5, 5, 5)
        ));
        
        // Style the text field inside JDateChooser
        JTextField dateField = ((JTextField) dc.getDateEditor().getUiComponent());
        dateField.setBackground(CARD2);
        dateField.setForeground(TEXT);
        dateField.setCaretColor(TEXT);
        dateField.setFont(FONT);
        dateField.setBorder(new EmptyBorder(5, 5, 5, 5));
        
        return dc;
    }

    // numeric-only sesuai perintah: field angka tidak boleh huruf
    public static void numericOnly(JTextField field) {
        ((AbstractDocument) field.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                if (string != null && string.matches("\\d+")) super.insertString(fb, offset, string, attr);
            }
            @Override public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                if (text != null && text.matches("\\d*")) super.replace(fb, offset, length, text, attrs);
            }
        });
    }

    public static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }

    public static void msg(String text) {
        JOptionPane.showMessageDialog(parent, text);
    }

    public static boolean confirm(Component parent, String text) {
        return JOptionPane.showConfirmDialog(parent, text, "Konfirmasi", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    public static String today() { return LocalDate.now().toString(); }
    public static String addDays(int days) { return LocalDate.now().plusDays(days).toString(); }

    public static void styleTable(JTable table) {
        table.setRowHeight(28);
        table.setBackground(CARD2);
        table.setForeground(TEXT);
        table.setGridColor(BORDER);
        table.getTableHeader().setBackground(CARD);
        table.getTableHeader().setForeground(TEXT);
        DefaultTableCellRenderer r = new DefaultTableCellRenderer();
        r.setBackground(CARD2);
        r.setForeground(TEXT);
        table.setDefaultRenderer(Object.class, r);
    }

    // Updated: Image Utils with native FileDialog for "not old-fashioned" look & previews
    public static File selectImage(Component parent) {
        Window window = SwingUtilities.getWindowAncestor(parent);
        FileDialog fd;
        if (window instanceof Frame) {
            fd = new FileDialog((Frame) window, "Pilih Gambar Sampul", FileDialog.LOAD);
        } else if (window instanceof Dialog) {
            fd = new FileDialog((Dialog) window, "Pilih Gambar Sampul", FileDialog.LOAD);
        } else {
            fd = new FileDialog((Frame) null, "Pilih Gambar Sampul", FileDialog.LOAD);
        }
        
        fd.setFile("*.jpg;*.jpeg;*.png;*.webp");
        fd.setVisible(true);
        
        if (fd.getFile() != null) {
            return new File(fd.getDirectory(), fd.getFile());
        }
        return null;
    }

    public static String saveCover(File file) {
        if (file == null) return null;
        try {
            File dir = new File("uploads/covers");
            if (!dir.exists()) dir.mkdirs();
            
            String ext = file.getName().substring(file.getName().lastIndexOf("."));
            String filename = "cover_" + System.currentTimeMillis() + ext;
            File dest = new File(dir, filename);
            
            java.nio.file.Files.copy(file.toPath(), dest.toPath());
            return "uploads/covers/" + filename;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ImageIcon getCover(String path, int width, int height) {
        if (path == null || path.isEmpty()) return null;
        try {
            File f = new File(path);
            
            // Fallback: Jika tidak ditemukan (kasus beda working directory saat build)
            if (!f.exists()) {
                // Coba cek di folder dist (jika run dari NetBeans)
                f = new File("dist/" + path);
                if (!f.exists()) {
                    // Coba cek di folder src (jika di dev environment)
                    f = new File("src/" + path);
                }
            }
            
            if (!f.exists()) return null;
            BufferedImage img = ImageIO.read(f);
            if (img == null) return null;
            
            return new ImageIcon(smartScale(img, width, height));
        } catch (Exception e) {
            return null;
        }
    }

    public static Image scaleImage(Image src, int w, int h) {
        if (src instanceof BufferedImage) {
            return smartScale((BufferedImage) src, w, h);
        }
        return src.getScaledInstance(w, h, Image.SCALE_SMOOTH);
    }

    public static BufferedImage smartScale(BufferedImage src, int targetW, int targetH) {
        int w = src.getWidth();
        int h = src.getHeight();
        
        double ratio = (double) targetW / targetH;
        double imgRatio = (double) w / h;
        
        int finalW, finalH;
        if (imgRatio > ratio) {
            // Image is wider than target ratio (landscape-ish)
            finalH = targetH;
            finalW = (int) (targetH * imgRatio);
        } else {
            // Image is taller than target ratio (portrait-ish)
            finalW = targetW;
            finalH = (int) (targetW / imgRatio);
        }
        
        Image scaled = src.getScaledInstance(finalW, finalH, Image.SCALE_SMOOTH);
        BufferedImage output = new BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = output.createGraphics();
        
        // High quality rendering hints
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        
        // Center crop logic: calculate offsets
        int x = (targetW - finalW) / 2;
        int y = (targetH - finalH) / 2;
        
        g2.drawImage(scaled, x, y, null);
        g2.dispose();
        
        return output;
    }

    public static JPanel bottomNav(String[] labels, Runnable[] actions, int activeIndex) {
        JPanel bar = new JPanel(new GridLayout(1, labels.length, 10, 0));
        bar.setBackground(BG);
        bar.setBorder(new EmptyBorder(10,10,10,10));
        for (int i=0;i<labels.length;i++) {
            JButton b = new JButton(labels[i]);
            b.setFocusPainted(false);
            b.setBorder(BorderFactory.createLineBorder(BORDER));
            b.setBackground(i==activeIndex ? ACCENT : CARD2);
            b.setForeground(Color.WHITE);
            int idx = i;
            b.addActionListener(e -> actions[idx].run());
            bar.add(b);
        }
        return bar;
    }

    public static JPanel createRootPanel(LayoutManager layout) {
        return new JPanel(layout) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Same gradient as LoginPage for consistency
                GradientPaint gp = new GradientPaint(0, 0, new Color(25, 28, 40), getWidth(), getHeight(), new Color(40, 45, 62));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                
                // Decorative circles
                g2.setColor(new Color(255, 255, 255, 5));
                g2.fillOval(-100, -100, 400, 400);
                g2.fillOval(getWidth() - 300, getHeight() - 300, 500, 500);
            }
        };
    }

    public static JButton createCloseButton(Window window) {
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
            if (confirm(window, "Apakah Anda yakin ingin keluar?")) System.exit(0);
        });
        return btnClose;
    }

    public static String getLibraryName() {
        try {
            // Ambil dari tabel settings di database
            var result = DB.query("SELECT setting_value FROM settings WHERE setting_key = 'library_name'");
            if (!result.isEmpty() && result.get(0).get("setting_value") != null) {
                return result.get(0).get("setting_value");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "NAHSAZ LIBRARY"; // Default fallback
    }
    public static ImageIcon getAppLogo(int size) {
        try {
            BufferedImage img = null;
            
            // --- NEW: Dynamic Logo from Database ---
            try {
                var settings = DB.settings();
                String dbLogoPath = settings.get("library_logo");
                if (dbLogoPath != null && !dbLogoPath.isEmpty()) {
                    File f = new File(dbLogoPath);
                    if (f.exists()) {
                        img = ImageIO.read(f);
                    }
                }
            } catch (Exception e) {
                System.err.println("Gagal memuat logo dari database: " + e.getMessage());
            }

            // 1. Try external file system (Manual override next to EXE/JAR)
            if (img == null) {
                File externalFile = new File("resources/nahsazlibrary.png");
                if (externalFile.exists()) {
                    try {
                        img = ImageIO.read(externalFile);
                    } catch (Exception e) {}
                }
            }

            // 2. Try file system (DEV ENV fallback)
            if (img == null) {
                String[] paths = {
                    "src/nahlib/nahsazlibrary.png",
                    "Netbeans/NahLib/src/nahlib/nahsazlibrary.png", 
                    "nahsazlibrary.png"
                };
                
                for (String p : paths) {
                    File f = new File(p);
                    if (f.exists()) {
                        try {
                            img = ImageIO.read(f);
                            if (img != null) break;
                        } catch (Exception e) { /* continue */ }
                    }
                }
            }
            
            // 3. Fallback to resource (CLASSPATH - Internal JAR default)
            if (img == null) {
                java.net.URL imgURL = Utils.class.getResource("/nahlib/nahsazlibrary.png");
                if (imgURL != null) {
                    try {
                        img = ImageIO.read(imgURL);
                    } catch (Exception e) { /* continue */ }
                } 
            }
            
            // 4. Fallback: Placeholder
            if (img == null) {
                img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = img.createGraphics();
                g.setColor(ACCENT);
                g.fillOval(0,0,size,size);
                g.setColor(Color.WHITE);
                g.setFont(new Font("Segoe UI", Font.BOLD, size/2));
                FontMetrics fm = g.getFontMetrics();
                String text = "N";
                int x = (size - fm.stringWidth(text)) / 2;
                int y = ((size - fm.getHeight()) / 2) + fm.getAscent();
                g.drawString(text, x, y);
                g.dispose();
                return new ImageIcon(img);
            }

            // High-quality circular rendering
            return new ImageIcon(makeCircularImage(img, size));

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

