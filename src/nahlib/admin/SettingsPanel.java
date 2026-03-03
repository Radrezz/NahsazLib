package nahlib.admin;

import nahlib.Lang;
import nahlib.DB;
import nahlib.Utils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SettingsPanel extends JPanel {
    private final AdminPage adminPage;
    private final JTextField libName = Utils.input("nama perpustakaan");
    private final JTextField maxDays = Utils.input("maks hari (angka)");
    private final JTextField maxBooks = Utils.input("maks buku (angka)");
    private final JTextField fine = Utils.input("denda per hari (angka)");
    private final JTextField maxBorrowUser = Utils.input("maks pinjam per user (angka)");
    private JButton btnSave;
    private final JLabel logoPreview = new JLabel();
    private final List<DayRow> scheduleRows = new ArrayList<>();

    private class DayRow {
        int index;
        JCheckBox open;
        JSpinner start, end;
        JPanel panel;
        
        DayRow(int idx) {
            this.index = idx;
            panel = new JPanel(new GridBagLayout());
            panel.setOpaque(false);
            panel.setPreferredSize(new Dimension(500, 36));
            
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(0, 0, 0, 10);
            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill = GridBagConstraints.NONE;
            
            // Day name label - fixed width
            JLabel nameLabel = new JLabel(Lang.get("day." + idx).toUpperCase());
            nameLabel.setForeground(Utils.TEXT);
            nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
            nameLabel.setPreferredSize(new Dimension(90, 30));
            gbc.gridx = 0;
            gbc.weightx = 0;
            panel.add(nameLabel, gbc);
            
            // Open checkbox - fixed width
            open = new JCheckBox(Lang.get("label.buka"));
            open.setForeground(Utils.TEXT);
            open.setOpaque(false);
            open.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            open.setPreferredSize(new Dimension(70, 30));
            gbc.gridx = 1;
            panel.add(open, gbc);
            
            // Start time spinner
            start = createTimeSpinner();
            gbc.gridx = 2;
            panel.add(start, gbc);
            
            // Separator
            JLabel separator = new JLabel("-");
            separator.setForeground(Utils.MUTED);
            separator.setFont(new Font("Segoe UI", Font.BOLD, 14));
            separator.setPreferredSize(new Dimension(20, 30));
            separator.setHorizontalAlignment(SwingConstants.CENTER);
            gbc.gridx = 3;
            gbc.insets = new Insets(0, 0, 0, 10);
            panel.add(separator, gbc);
            
            // End time spinner
            end = createTimeSpinner();
            gbc.gridx = 4;
            gbc.weightx = 1.0;
            gbc.insets = new Insets(0, 0, 0, 0);
            panel.add(end, gbc);
            
            open.addActionListener(e -> toggle(open.isSelected()));
        }
        
        void toggle(boolean active) {
            start.setEnabled(active);
            end.setEnabled(active);
        }
        
        private JSpinner createTimeSpinner() {
            JSpinner s = new JSpinner(new SpinnerDateModel());
            JSpinner.DateEditor de = new JSpinner.DateEditor(s, "HH:mm");
            s.setEditor(de);
            s.setPreferredSize(new Dimension(90, 30));
            s.setMinimumSize(new Dimension(90, 30));
            s.setMaximumSize(new Dimension(90, 30));
            s.setBackground(Utils.CARD2);
            s.setBorder(BorderFactory.createLineBorder(Utils.BORDER));
            return s;
        }
    }

    public SettingsPanel(AdminPage adminPage) {
        this.adminPage = adminPage;
        setBackground(Utils.BG);
        setLayout(new BorderLayout());
        
        JPanel header = createHeader();
        
        // Main content with 2 columns
        JPanel content = new JPanel(new GridLayout(1, 2, 16, 0));
        content.setBackground(Utils.BG);
        content.setBorder(new EmptyBorder(10, 20, 20, 20));
        
        // LEFT COLUMN - Logo + Operational Hours with 1:3 ratio
        JPanel leftColumn = new JPanel(new GridBagLayout());
        leftColumn.setOpaque(false);
        GridBagConstraints gbcLeft = new GridBagConstraints();
        gbcLeft.fill = GridBagConstraints.BOTH;
        gbcLeft.weightx = 1.0;
        gbcLeft.gridx = 0;
        gbcLeft.insets = new Insets(0, 0, 8, 0);
        
        // Logo section - 1 part (20%)
        gbcLeft.gridy = 0;
        gbcLeft.weighty = 0.2;
        leftColumn.add(createLogoSettings(), gbcLeft);
        
        // Operational Hours section - 3 parts (80%)
        gbcLeft.gridy = 1;
        gbcLeft.weighty = 0.8;
        gbcLeft.insets = new Insets(8, 0, 0, 0);
        leftColumn.add(createOperationalHours(), gbcLeft);
        
        // RIGHT COLUMN - General Settings + Tools
        JPanel rightColumn = new JPanel(new GridLayout(2, 1, 0, 16));
        rightColumn.setOpaque(false);
        rightColumn.add(createGeneralSettings());
        rightColumn.add(createToolSection());
        
        content.add(leftColumn);
        content.add(rightColumn);
        
        add(header, BorderLayout.NORTH);
        add(content, BorderLayout.CENTER);
        
        refresh();
    }
    
    private JPanel createHeader() {
        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(Utils.BG);
        h.setBorder(new EmptyBorder(25, 30, 15, 30));
        
        JLabel title = new JLabel(Lang.get("admin.settings.title"));
        title.setForeground(Utils.TEXT);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        
        JLabel subtitle = new JLabel(Lang.get("admin.settings.subtitle"));
        subtitle.setForeground(Utils.MUTED);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        JPanel tp = new JPanel(new BorderLayout());
        tp.setOpaque(false);
        tp.add(title, BorderLayout.NORTH);
        tp.add(subtitle, BorderLayout.SOUTH);
        
        btnSave = adminPage.createPrimaryButton(Lang.get("btn.save"));
        btnSave.addActionListener(e -> saveSettings());
        
        h.add(tp, BorderLayout.WEST);
        h.add(btnSave, BorderLayout.EAST);
        return h;
    }
    
    private JPanel createGeneralSettings() {
        JPanel p = Utils.card();
        p.setLayout(new GridLayout(5, 1, 0, 12));
        
        p.add(createFieldRow(Lang.get("settings.lib_name"), libName));
        p.add(createFieldRow(Lang.get("settings.max_days"), maxDays));
        p.add(createFieldRow(Lang.get("settings.max_books"), maxBooks));
        p.add(createFieldRow(Lang.get("settings.fine"), fine));
        p.add(createFieldRow(Lang.get("settings.max_borrow"), maxBorrowUser));
        
        return p;
    }
    
    private JPanel createLogoSettings() {
        JPanel p = Utils.card();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createCompoundBorder(
            p.getBorder(),
            new EmptyBorder(10, 10, 10, 10)
        ));
        
        JLabel title = new JLabel(Lang.get("settings.logo"));
        title.setForeground(Utils.TEXT);
        title.setFont(Utils.FONT_B);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(title);
        
        p.add(Box.createVerticalStrut(10));
        
        // Logo preview - reduced size
        logoPreview.setPreferredSize(new Dimension(80, 80));
        logoPreview.setMaximumSize(new Dimension(80, 80));
        logoPreview.setHorizontalAlignment(SwingConstants.CENTER);
        logoPreview.setBorder(BorderFactory.createLineBorder(Utils.BORDER));
        logoPreview.setAlignmentX(Component.LEFT_ALIGNMENT);
        loadLogo();
        
        p.add(logoPreview);
        p.add(Box.createVerticalStrut(8));
        
        JButton btnChange = adminPage.createSecondaryButton(Lang.get("settings.btn_change_logo"));
        btnChange.addActionListener(e -> changeLogo());
        btnChange.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnChange.setMaximumSize(new Dimension(200, 35));
        
        p.add(btnChange);
        p.add(Box.createVerticalGlue());
        
        return p;
    }
    
    private void loadLogo() {
        try {
            // Menggunakan utilitas terpusat agar konsisten dengan database
            ImageIcon icon = Utils.getAppLogo(70);
            if (icon != null) {
                logoPreview.setIcon(icon);
            }
        } catch (Exception ignored) {}
    }
    
    private void changeLogo() {
        FileDialog fd = new FileDialog(adminPage, Lang.get("settings.logo.pick"), FileDialog.LOAD);
        fd.setFile("*.png;*.jpg;*.jpeg");
        fd.setVisible(true);
        if (fd.getFile() != null) {
            File src = new File(fd.getDirectory(), fd.getFile());
            try {
                // Buat folder eksternal untuk menyimpan logo
                File dir = new File("uploads/logo");
                if (!dir.exists()) dir.mkdirs();
                
                String ext = fd.getFile().substring(fd.getFile().lastIndexOf("."));
                String filename = "app_logo" + ext;
                File dest = new File(dir, filename);
                
                // Copy file ke folder eksternal (di luar EXE/JAR)
                Files.copy(src.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                
                // Simpan PATH RELATIF ke database
                String relativePath = "uploads/logo/" + filename;
                DB.exec("UPDATE settings SET setting_value=? WHERE setting_key='library_logo'", relativePath);
                
                loadLogo();
                adminPage.refreshApp();
                adminPage.showMessageDialog(Lang.get("msg.success"), Lang.get("msg.success"));
                DB.audit(Long.valueOf(adminPage.idValue()), "UPDATE", "system", "logo", "Changed application logo to " + relativePath);
            } catch (Exception ex) {
                adminPage.showErrorDialog("Error", "Gagal mengganti logo: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }
    
    private JPanel createOperationalHours() {
        JPanel p = Utils.card();
        p.setLayout(new BorderLayout());
        
        JLabel title = new JLabel(Lang.get("settings.operational"));
        title.setForeground(Utils.TEXT);
        title.setFont(Utils.FONT_B);
        title.setBorder(new EmptyBorder(0, 0, 10, 0));
        p.add(title, BorderLayout.NORTH);
        
        JPanel list = new JPanel();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setOpaque(false);
        
        // Create rows for days 0-6 (Minggu to Sabtu) to match database
        for (int i = 0; i < 7; i++) {
            DayRow row = new DayRow(i);
            scheduleRows.add(row);
            list.add(row.panel);
            if (i < 6) list.add(Box.createVerticalStrut(3));
        }
        
        p.add(list, BorderLayout.CENTER);
        return p;
    }
    
    private JPanel createToolSection() {
        JPanel p = Utils.card();
        p.setLayout(new GridLayout(2, 1, 0, 10));
        
        JButton csv = adminPage.createSecondaryButton(Lang.get("settings.btn_export"));
        csv.addActionListener(e -> exportToCSV());
        
        JButton backup = adminPage.createSecondaryButton(Lang.get("settings.btn_backup"));
        backup.addActionListener(e -> backupDatabase());
        
        p.add(csv);
        p.add(backup);
        return p;
    }
    
    private JPanel createFieldRow(String label, JTextField field) {
        JPanel r = new JPanel(new BorderLayout(10, 0));
        r.setOpaque(false);
        JLabel l = new JLabel(label);
        l.setForeground(Utils.MUTED);
        l.setFont(Utils.FONT);
        l.setPreferredSize(new Dimension(180, 0));
        r.add(l, BorderLayout.WEST);
        r.add(field, BorderLayout.CENTER);
        return r;
    }
    
    public void refresh() {
        try {
            var s = DB.settings();
            libName.setText(s.getOrDefault("library_name","Nahsaz Library"));
            
            var r = DB.rules();
            maxDays.setText(String.valueOf(r.get("max_days")));
            maxBooks.setText(String.valueOf(r.get("max_books")));
            fine.setText(String.valueOf(r.get("fine_per_day")));
            maxBorrowUser.setText(String.valueOf(r.get("max_borrow_per_user")));
            
            // Operational Hours - database uses day_index 0-6
            var hours = DB.query("SELECT * FROM operational_hours ORDER BY day_index");
            for (var row : hours) {
                int idx = Integer.parseInt(row.get("day_index"));
                if (idx >= 0 && idx < scheduleRows.size()) {
                    DayRow dr = scheduleRows.get(idx);
                    dr.open.setSelected("1".equals(row.get("is_open")));
                    
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm:ss");
                    dr.start.setValue(sdf.parse(row.get("open_time")));
                    dr.end.setValue(sdf.parse(row.get("close_time")));
                    dr.toggle(dr.open.isSelected());
                }
            }
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
    }
    
    private void saveSettings() {
        try {
             // Validation
            int d = Integer.parseInt(maxDays.getText().trim());
            int b = Integer.parseInt(maxBooks.getText().trim());
            int f = Integer.parseInt(fine.getText().trim());
            int mu = Integer.parseInt(maxBorrowUser.getText().trim());
            
            if (d <= 0 || b <= 0 || f < 0 || mu <= 0) {
                adminPage.showMessageDialog("Peringatan", "Semua nilai harus bernilai positif.");
                return;
            }

            if (!adminPage.confirmDialog("Konfirmasi", "Simpan pengaturan perpustakaan?")) return;
            
            DB.exec("UPDATE settings SET setting_value=? WHERE setting_key='library_name'", libName.getText().trim());
            DB.exec("UPDATE rules SET max_days=?, max_books=?, fine_per_day=?, max_borrow_per_user=? WHERE rule_id=1", 
                d, b, f, mu);
            
            // Operational Hours
            for (DayRow dr : scheduleRows) {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm:ss");
                DB.exec("UPDATE operational_hours SET is_open=?, open_time=?, close_time=? WHERE day_index=?",
                    dr.open.isSelected() ? 1:0, 
                    sdf.format(dr.start.getValue()), 
                    sdf.format(dr.end.getValue()), 
                    dr.index);
            }
            
            DB.audit(Long.valueOf(adminPage.idValue()), "UPDATE", "settings", "rules", "Update settings & rules");
            adminPage.refreshApp();
            adminPage.showMessageDialog(Lang.get("msg.success"), Lang.get("msg.success"));
        } catch (Exception ex) {
            adminPage.showErrorDialog("Error", "Gagal menyimpan settings: " + ex.getMessage());
        }
    }
    
    private void exportToCSV() {
        String[] tables = {"users","books","loans","loan_items","returns","wishlist","categories","racks","audit_log"};
        String pick = (String) JOptionPane.showInputDialog(adminPage, Lang.get("report.period") + ":", Lang.get("report.generate") + " CSV",
                JOptionPane.PLAIN_MESSAGE, null, tables, tables[0]);
        if (pick == null) return;

        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File(pick + ".csv"));
        if (fc.showSaveDialog(adminPage) != JFileChooser.APPROVE_OPTION) return;

        try {
            DB.exportTableToCSV(pick, fc.getSelectedFile());
            DB.audit(Long.valueOf(adminPage.idValue()), "EXPORT", "csv", pick, "Export CSV");
            adminPage.showMessageDialog(Lang.get("msg.success"), Lang.get("msg.success") + ": " + fc.getSelectedFile().getName());
        } catch (Exception ex) {
            adminPage.showErrorDialog(Lang.get("msg.error"), Lang.get("msg.error") + ": " + ex.getMessage());
        }
    }
    
    private void backupDatabase() {
        if (!adminPage.confirmDialog("Backup Database", 
            "Apakah Anda yakin ingin membuat backup database?\n\n" +
            "Backup akan mengekspor semua tabel ke file SQL.")) return;
        
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("nahsaz_library_backup_" + 
            java.time.LocalDate.now().toString() + ".sql"));
        
        if (fc.showSaveDialog(adminPage) == JFileChooser.APPROVE_OPTION) {
            try {
                adminPage.showMessageDialog(Lang.get("msg.info"), Lang.get("msg.info") + "\n" +
                    "Gunakan Export CSV untuk mengekspor data tabel.");
            } catch (Exception ex) {
                adminPage.showErrorDialog(Lang.get("msg.error"), Lang.get("msg.error"));
            }
        }
    }
}
