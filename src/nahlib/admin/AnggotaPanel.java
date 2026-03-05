package nahlib.admin;

import nahlib.Lang;
import nahlib.DB;
import nahlib.Utils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class AnggotaPanel extends JPanel {
    private final AdminPage adminPage;
    private final DefaultTableModel model = new DefaultTableModel(new String[]{
        "ID", Lang.get("staff.table.username"), Lang.get("staff.table.name"), 
        "Kelas", Lang.get("label.gender"), Lang.get("staff.form.address"), 
        Lang.get("staff.table.phone"), Lang.get("staff.table.email"), 
        Lang.get("staff.table.status"), Lang.get("btn.view_detail"), "description", "req_id", "req_hash"
    }, 0);
    private final JTable table = new JTable(model);
    private JTextField searchField;
    private JLabel statsLabel;

    public AnggotaPanel(AdminPage adminPage) {
        this.adminPage = adminPage;
        setBackground(Utils.BG);
        setLayout(new BorderLayout());
        
        // Header dengan live search
        JPanel header = createPanelHeader("Kelola Anggota", 
            "Kelola data anggota perpustakaan");
        
        // Table
        styleTable();
        new nahlib.TableButton(Lang.get("btn.view_detail"), r -> {
            int modelRow = table.convertRowIndexToModel(r);
            new nahlib.DetailPage(adminPage, Lang.get("member.title"), getRowData(modelRow));
        }).install(table, 9);
        
        // Hide meta columns
        table.getColumnModel().getColumn(10).setMinWidth(0);
        table.getColumnModel().getColumn(10).setMaxWidth(0);
        table.getColumnModel().getColumn(11).setMinWidth(0);
        table.getColumnModel().getColumn(11).setMaxWidth(0);
        table.getColumnModel().getColumn(12).setMinWidth(0);
        table.getColumnModel().getColumn(12).setMaxWidth(0);
        
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new EmptyBorder(0, 20, 20, 20));
        scroll.getViewport().setBackground(Utils.CARD);
        
        // Action buttons
        JPanel actions = createActionPanel();
        
        add(header, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(actions, BorderLayout.SOUTH);
        
        refresh();
    }
    
    private JPanel createPanelHeader(String title, String subtitle) {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Utils.BG);
        header.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(Utils.TEXT);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        
        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setForeground(Utils.MUTED);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.add(titleLabel, BorderLayout.NORTH);
        titlePanel.add(subtitleLabel, BorderLayout.SOUTH);
        
        // Search panel di pojok kanan atas
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        searchPanel.setOpaque(false);
        
        searchField = new JTextField(20);
        searchField.setFont(Utils.FONT);
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Utils.BORDER),
            new EmptyBorder(6, 10, 6, 10)
        ));
        searchField.putClientProperty("JTextField.placeholderText", Lang.get("petugas.search.placeholder"));
        
        // Live search listener
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { filterTable(); }
            @Override
            public void removeUpdate(DocumentEvent e) { filterTable(); }
            @Override
            public void changedUpdate(DocumentEvent e) { filterTable(); }
            
            private void filterTable() {
                String searchText = searchField.getText().toLowerCase();
                TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
                table.setRowSorter(sorter);
                if (searchText.trim().length() == 0) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + searchText));
                }
            }
        });
        
        JLabel searchIcon = new JLabel(Lang.get("btn.search"));
        searchIcon.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchIcon.setForeground(Utils.TEXT);
        
        searchPanel.add(searchIcon);
        searchPanel.add(searchField);
        
        // Statistics label instead of refresh button
        statsLabel = new JLabel();
        statsLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        statsLabel.setForeground(Utils.TEXT);
        statsLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Utils.ACCENT, 2),
            new EmptyBorder(6, 12, 6, 12)
        ));
        statsLabel.setOpaque(true);
        statsLabel.setBackground(new Color(66, 133, 244, 20));
        
        searchPanel.add(statsLabel);
        
        header.add(titlePanel, BorderLayout.WEST);
        header.add(searchPanel, BorderLayout.EAST);
        
        return header;
    }
    
    private void styleTable() {
        table.setRowHeight(40);
        table.setBackground(Utils.CARD);
        table.setForeground(Utils.TEXT);
        table.setGridColor(Utils.BORDER);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        
        JTableHeader header = table.getTableHeader();
        header.setBackground(Utils.CARD2);
        header.setForeground(Utils.TEXT);
        header.setFont(Utils.FONT_B);
        header.setBorder(BorderFactory.createLineBorder(Utils.BORDER));
        header.setReorderingAllowed(false);
        
        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setBackground(isSelected ? Utils.ACCENT : Utils.CARD);
                c.setForeground(isSelected ? Color.WHITE : Utils.TEXT);
                
                if (column == 8) { // Status column
                    if ("Aktif".equals(value)) {
                        setForeground(new Color(52, 168, 83));
                    } else if ("Nonaktif".equals(value)) {
                        setForeground(new Color(234, 67, 53));
                    }
                } else if (column == 1) { // Username column
                    Object rid = table.getValueAt(row, 11);
                    if (rid != null && !rid.toString().isEmpty()) {
                        setForeground(new Color(251, 188, 5)); // Yellow for requests
                        setFont(getFont().deriveFont(Font.BOLD));
                    }
                }
                
                setBorder(noFocusBorder);
                return c;
            }
        });
        
        // Enable row sorter
        table.setRowSorter(new TableRowSorter<>(model));
    }
    
    private JPanel createActionPanel() {
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actions.setBackground(Utils.BG);
        actions.setBorder(new EmptyBorder(0, 20, 20, 20));
        
        JButton add = adminPage.createPrimaryButton(Lang.get("btn.add"));
        JButton edit = adminPage.createSecondaryButton(Lang.get("btn.edit"));
        JButton activate = adminPage.createSecondaryButton(Lang.get("staff.btn.activate"));
        JButton deactivate = adminPage.createSecondaryButton(Lang.get("staff.btn.deactivate"));
        JButton reset = adminPage.createSecondaryButton(Lang.get("staff.btn.reset_password"));
        
        JButton approveReq = adminPage.createPrimaryButton("Setujui Sandi (!)");
        approveReq.setBackground(new Color(52, 168, 83));
        approveReq.setVisible(false);
        
        add.addActionListener(e -> openForm(null));
        edit.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r < 0) { 
                adminPage.showMessageDialog(Lang.get("msg.warning"), Lang.get("msg.info")); 
                return; 
            }
            int modelRow = table.convertRowIndexToModel(r);
            openForm(getRowData(modelRow));
        });

        activate.addActionListener(e -> setAnggotaStatus(true));
        deactivate.addActionListener(e -> setAnggotaStatus(false));
        reset.addActionListener(e -> resetPassword());
        approveReq.addActionListener(e -> processPasswordRequest());
        
        table.getSelectionModel().addListSelectionListener(e -> {
            int r = table.getSelectedRow();
            if (r >= 0) {
                int mr = table.convertRowIndexToModel(r);
                Object rid = model.getValueAt(mr, 11);
                approveReq.setVisible(rid != null && !rid.toString().isEmpty());
            } else {
                approveReq.setVisible(false);
            }
            actions.revalidate();
            actions.repaint();
        });

        actions.add(add);
        actions.add(edit);
        actions.add(activate);
        actions.add(deactivate);
        actions.add(reset);
        actions.add(approveReq);
        
        return actions;
    }
    
    private Map<String,String> getRowData(int row) {
        Map<String, String> data = new HashMap<>();
        data.put("user_id", String.valueOf(model.getValueAt(row,0)));
        data.put("username", String.valueOf(model.getValueAt(row,1)));
        data.put("nama_lengkap", String.valueOf(model.getValueAt(row,2)));
        data.put("kelas", String.valueOf(model.getValueAt(row,3)));
        data.put("gender", String.valueOf(model.getValueAt(row,4)));
        data.put("alamat", String.valueOf(model.getValueAt(row,5)));
        data.put("no_telp", String.valueOf(model.getValueAt(row,6)));
        data.put("email", String.valueOf(model.getValueAt(row,7)));
        data.put("email", String.valueOf(model.getValueAt(row,7)));
        data.put("description", String.valueOf(model.getValueAt(row,10))); // Fetch description
        return data;
    }

    public void refresh() {
        try {
            model.setRowCount(0);
            var rows = DB.query(
                "SELECT u.*, pr.request_id, pr.new_password_hash " +
                "FROM users u " +
                "LEFT JOIN password_requests pr ON u.user_id = pr.user_id AND pr.status = 'PENDING' " +
                "WHERE u.role='USER' ORDER BY u.user_id DESC"
            );
            int pendingCount = 0;
            for (var r: rows) {
                if (r.get("request_id") != null) pendingCount++;
                model.addRow(new Object[]{
                    r.get("user_id"), 
                    r.get("username"), 
                    r.get("nama_lengkap"),
                    r.get("kelas") == null ? "" : r.get("kelas"),
                    r.get("gender") == null ? "" : r.get("gender"),
                    r.get("alamat") == null ? "" : r.get("alamat"),
                    r.get("no_telp") == null ? "" : r.get("no_telp"),
                    r.get("email") == null ? "" : r.get("email"),
                    "1".equals(r.get("status_aktif")) ? "Aktif":"Nonaktif",
                    "", // Button placeholder
                    r.get("description") == null ? "" : r.get("description"),
                    r.get("request_id"),
                    r.get("new_password_hash")
                });
            }
            
            adminPage.updateNavBadge(2, pendingCount); // 2 is Members (indexed)
            
            // Update statistics
            int aktif = 0, nonaktif = 0;
            for (var r: rows) {
                if ("1".equals(r.get("status_aktif"))) aktif++;
                else nonaktif++;
            }
            
            statsLabel.setText(String.format(
                "<html><b>%d</b> anggota | <span style='color:#4285F4'>✓ %d</span> | <span style='color:#80868B'>✗ %d</span></html>",
                rows.size(), aktif, nonaktif
            ));
        } catch (Exception ignored) {}
    }
    
    private void setAnggotaStatus(boolean active) {
        int r = table.getSelectedRow();
        if (r < 0) { 
            adminPage.showMessageDialog(Lang.get("msg.warning"), "Pilih anggota terlebih dahulu."); 
            return; 
        }
        
        int modelRow = table.convertRowIndexToModel(r);
        String id = String.valueOf(model.getValueAt(modelRow,0));
        String nama = String.valueOf(model.getValueAt(modelRow,2));
        String currentStatus = String.valueOf(model.getValueAt(modelRow,8));
        
        if ((active && "Aktif".equals(currentStatus)) || (!active && "Nonaktif".equals(currentStatus))) {
            adminPage.showMessageDialog(Lang.get("msg.info"), "Status sudah sesuai.");
            return;
        }
        
        if (!adminPage.confirmDialog(Lang.get("msg.confirm"), (active ? Lang.get("staff.btn.activate") : Lang.get("staff.btn.deactivate")) + " anggota:\n" + nama + "?")) return;
        
        try {
            DB.exec("UPDATE users SET status_aktif=? WHERE user_id=? AND role='USER'", active?1:0, id);
            DB.audit(Long.valueOf(adminPage.idValue()), "UPDATE", "users", id, 
                (active?"Aktifkan":"Nonaktifkan")+" anggota");
            refresh();
            adminPage.showMessageDialog(Lang.get("msg.success"), Lang.get("msg.success"));
        } catch (Exception ex) { 
            adminPage.showErrorDialog(Lang.get("msg.error"), Lang.get("msg.error")); 
        }
    }
    
    private void resetPassword() {
        int r = table.getSelectedRow();
        if (r < 0) { 
            adminPage.showMessageDialog(Lang.get("msg.warning"), "Pilih anggota terlebih dahulu."); 
            return; 
        }
        
        int modelRow = table.convertRowIndexToModel(r);
        String id = String.valueOf(model.getValueAt(modelRow,0));
        String nama = String.valueOf(model.getValueAt(modelRow,2));
        
        String np = JOptionPane.showInputDialog(this, 
            Lang.get("staff.btn.reset_password") + ":\n" + nama, 
            Lang.get("staff.btn.reset_password"), 
            JOptionPane.QUESTION_MESSAGE);
        
        if (np == null || np.trim().isEmpty()) return;
        
        try {
            DB.exec("UPDATE users SET password_hash=? WHERE user_id=? AND role='USER'", 
                Utils.sha256(np), id);
            DB.audit(Long.valueOf(adminPage.idValue()), "UPDATE", "users", id, "Reset password anggota");
            adminPage.showMessageDialog(Lang.get("msg.success"), Lang.get("msg.success"));
        } catch (Exception ex) { 
            adminPage.showErrorDialog(Lang.get("msg.error"), Lang.get("msg.error")); 
        }
    }

    public void openForm(Map<String,String> data) {
        JDialog d = new JDialog(adminPage, true);
        d.setTitle(data == null ? "Tambah Anggota Baru" : "Edit Data Anggota");
        d.setSize(550, 750); // Increased height for password and description
        d.setLocationRelativeTo(this);
        d.getContentPane().setBackground(Utils.BG);

        JPanel form = createFormPanel(data, d);
        d.setContentPane(form);
        d.setVisible(true);
    }
    
    private JPanel createFormPanel(Map<String,String> data, JDialog dialog) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Utils.BG);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JLabel title = new JLabel(data == null ? "Tambah Anggota Baru" : "Edit Data Anggota");
        title.setForeground(Utils.TEXT);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setBorder(new EmptyBorder(0, 0, 20, 0));
        
        JPanel fields = new JPanel(new GridLayout(9, 1, 10, 15)); // Increased row count for password
        fields.setOpaque(false);
        
        JTextField u = Utils.input("username");
        JPasswordField pw = Utils.passInput("password (isi jika tambah/reset)");
        JTextField nama = Utils.input("nama lengkap");
        
        // Create class combo box
        JComboBox<String> kelasCombo = new JComboBox<>();
        kelasCombo.addItem(""); // Empty option
        String[] grades = {"VII", "VIII", "IX"};
        String[] sections = {"A", "B", "C", "D", "E"};
        for (String grade : grades) {
            for (String section : sections) {
                kelasCombo.addItem(grade + "-" + section);
            }
        }
        kelasCombo.setBackground(Utils.CARD2);
        kelasCombo.setForeground(Utils.TEXT);
        kelasCombo.setBorder(BorderFactory.createLineBorder(Utils.BORDER));
        
        JComboBox<String> genderCombo = new JComboBox<>(new String[]{"", "Laki-laki", "Perempuan"});
        genderCombo.setBackground(Utils.CARD2);
        genderCombo.setForeground(Utils.TEXT);
        genderCombo.setBorder(BorderFactory.createLineBorder(Utils.BORDER));
        
        JTextField alamat = Utils.input("alamat");
        JTextField telp = Utils.input("no telp");
        Utils.numericOnly(telp);
        JTextField email = Utils.input("email");
        
        JTextArea descArea = new JTextArea(3, 20);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setFont(Utils.FONT);
        descArea.setBackground(Utils.CARD2);
        descArea.setForeground(Utils.TEXT);
        descArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Utils.BORDER),
            new EmptyBorder(5, 5, 5, 5)
        ));
        JScrollPane descScroll = new JScrollPane(descArea);
        descScroll.setBorder(null);
        descScroll.getViewport().setOpaque(false);
        descScroll.setOpaque(false);
        
        if (data != null) {
            u.setText(data.get("username"));
            nama.setText(data.get("nama_lengkap"));
            
            // Set selected class
            if (data.get("kelas") != null && !data.get("kelas").isEmpty()) {
                kelasCombo.setSelectedItem(data.get("kelas"));
            }
            
            telp.setText(data.get("no_telp"));
            
            if (data.get("gender") != null) {
                genderCombo.setSelectedItem(data.get("gender"));
            }
            if (data.get("alamat") != null) {
                alamat.setText(data.get("alamat"));
            }
            if (data.get("email") != null) {
                email.setText(data.get("email"));
            }
            if (data.get("description") != null) {
                descArea.setText(data.get("description"));
            }
        }
        
        fields.add(createFormRow("Username*", u));
        fields.add(createFormRow("Password", pw));
        fields.add(createFormRow("Nama Lengkap*", nama));
        fields.add(createFormRow("Kelas", kelasCombo));
        fields.add(createFormRow("Gender", genderCombo));
        fields.add(createFormRow("Alamat", alamat));
        fields.add(createFormRow("No. Telepon", telp));
        fields.add(createFormRow("Email", email));
        fields.add(createFormRow("Deskripsi", descScroll));
        
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setOpaque(false);
        
        JButton cancel = adminPage.createSecondaryButton("Batal");
        JButton save = adminPage.createPrimaryButton(data == null ? "Simpan" : "Update");
        
        cancel.addActionListener(e -> dialog.dispose());
        save.addActionListener(e -> saveAnggota(data, u, pw, nama, kelasCombo, genderCombo, alamat, telp, email, descArea, dialog));
        
        buttons.add(cancel);
        buttons.add(save);
        
        panel.add(title, BorderLayout.NORTH);
        panel.add(fields, BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createFormRow(String label, JComponent component) {
        JPanel row = new JPanel(new BorderLayout(10, 5));
        row.setOpaque(false);
        
        JLabel lbl = new JLabel(label + ":");
        lbl.setForeground(Utils.TEXT);
        lbl.setFont(Utils.FONT);
        lbl.setPreferredSize(new Dimension(150, 30));
        
        row.add(lbl, BorderLayout.WEST);
        row.add(component, BorderLayout.CENTER);
        
        return row;
    }
    
    private void saveAnggota(Map<String,String> data, JTextField u, JPasswordField pw, JTextField nama, 
                            JComboBox<String> kelasCombo, JComboBox<String> genderCombo,
                            JTextField alamat, JTextField telp, JTextField email, JTextArea desc, JDialog dialog) {
        try {
            if (u.getText().trim().isEmpty() || nama.getText().trim().isEmpty()) { 
                adminPage.showMessageDialog("Peringatan", "Username dan nama lengkap wajib diisi."); 
                return; 
            }
            
            String gender = genderCombo.getSelectedItem() != null ? 
                           genderCombo.getSelectedItem().toString() : "";
            
            String kelasValue = kelasCombo.getSelectedItem() != null ? 
                               kelasCombo.getSelectedItem().toString().trim() : "";
            
            if (data == null) {
                // INSERT Mode
                if (new String(pw.getPassword()).isEmpty()) {
                    adminPage.showMessageDialog("Peringatan", "Password wajib untuk anggota baru.");
                    return;
                }
                
                long idNew = DB.exec(
                    "INSERT INTO users(username, password_hash, role, nama_lengkap, kelas, gender, alamat, no_telp, email, status_aktif, description) " +
                    "VALUES (?, ?, 'USER', ?, ?, ?, ?, ?, ?, 1, ?)",
                    u.getText().trim(),
                    Utils.sha256(new String(pw.getPassword())),
                    nama.getText().trim(),
                    kelasValue.isEmpty() ? null : kelasValue,
                    gender.isEmpty() ? null : gender,
                    alamat.getText().trim().isEmpty() ? null : alamat.getText().trim(),
                    telp.getText().trim().isEmpty() ? null : telp.getText().trim(),
                    email.getText().trim().isEmpty() ? null : email.getText().trim(),
                    desc.getText().trim()
                );
                
                // Get the generated ID
                String lastId = DB.query("SELECT LAST_INSERT_ID() as id").get(0).get("id");
                
                DB.audit(Long.valueOf(adminPage.idValue()), "CREATE", "users", lastId, "Tambah anggota");
                adminPage.showMessageDialog(Lang.get("msg.success"), "Anggota baru berhasil ditambahkan.");
            } else {
                // UPDATE Mode
                DB.exec("UPDATE users SET username=?, nama_lengkap=?, kelas=?, gender=?, alamat=?, no_telp=?, email=?, description=? WHERE user_id=? AND role='USER'",
                    u.getText().trim(), 
                    nama.getText().trim(), 
                    kelasValue.isEmpty() ? null : kelasValue,
                    gender.isEmpty() ? null : gender,
                    alamat.getText().trim().isEmpty() ? null : alamat.getText().trim(),
                    telp.getText().trim().isEmpty() ? null : telp.getText().trim(),
                    email.getText().trim().isEmpty() ? null : email.getText().trim(),
                    desc.getText().trim(),
                    data.get("user_id")
                );
                
                if (!new String(pw.getPassword()).isEmpty()) {
                    DB.exec("UPDATE users SET password_hash=? WHERE user_id=? AND role='USER'", 
                        Utils.sha256(new String(pw.getPassword())), data.get("user_id"));
                }
                
                DB.audit(Long.valueOf(adminPage.idValue()), "UPDATE", "users", data.get("user_id"), "Edit anggota");
                adminPage.showMessageDialog(Lang.get("msg.success"), "Data anggota berhasil diperbarui.");
            }
            refresh();
            dialog.dispose();
        } catch (Exception ex) {
            ex.printStackTrace();
            adminPage.showErrorDialog("Error", "Gagal menyimpan. Username mungkin sudah digunakan.");
        }
    }

    private void processPasswordRequest() {
        int r = table.getSelectedRow();
        if (r < 0) return;
        
        int modelRow = table.convertRowIndexToModel(r);
        String rid = String.valueOf(model.getValueAt(modelRow, 11));
        String hash = String.valueOf(model.getValueAt(modelRow, 12));
        String uid = String.valueOf(model.getValueAt(modelRow, 0));
        String nama = String.valueOf(model.getValueAt(modelRow, 2));

        int opt = JOptionPane.showOptionDialog(this, 
            "Proses permintaan sandi baru untuk: " + nama + "?",
            "Konfirmasi Permintaan Sandi",
            JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,
            new String[]{"Setujui", "Tolak", "Batal"}, "Setujui");

        try {
            if (opt == 0) { // Setujui
                DB.tx(() -> {
                    DB.exec("UPDATE users SET password_hash=? WHERE user_id=?", hash, uid);
                    DB.exec("UPDATE password_requests SET status='APPROVED' WHERE request_id=?", rid);
                });
                DB.audit(Long.valueOf(adminPage.idValue()), "UPDATE", "users", uid, "Setujui permintaan sandi");
                adminPage.showMessageDialog("Sukses", "Password baru telah diterapkan.");
            } else if (opt == 1) { // Tolak
                DB.exec("UPDATE password_requests SET status='REJECTED' WHERE request_id=?", rid);
                DB.audit(Long.valueOf(adminPage.idValue()), "UPDATE", "users", uid, "Tolak permintaan sandi");
                adminPage.showMessageDialog("Info", "Permintaan ditolak.");
            }
            refresh();
        } catch (Exception ex) {
            ex.printStackTrace();
            adminPage.showErrorDialog("Error", "Gagal memproses permintaan.");
        }
    }
}
