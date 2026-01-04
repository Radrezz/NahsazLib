package nahlib;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.text.*;
import java.awt.*;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Utils {
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
}

