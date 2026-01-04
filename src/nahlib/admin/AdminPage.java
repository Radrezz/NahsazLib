package nahlib.admin;

import nahlib.DB;
import nahlib.LoginPage;
import nahlib.Utils;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

public class AdminPage extends JFrame {

    private final Map<String,String> me;
    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);
    private final Map<String, JPanel> panels = new HashMap<>();
    
    // badge counts
    private int badgeOverdue = 0;

    public AdminPage(Map<String,String> me) {
        this.me = me;

        setTitle(Utils.getLibraryName() + " - Admin Dashboard");
        setSize(1280, 780);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        
        // Tambahkan kode ini untuk maximize
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Utils.BG);

        // =========== TOP BAR ===========
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(Utils.BG);
        top.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, Utils.BORDER),
            new EmptyBorder(16, 20, 16, 20)
        ));

        // Left side: Logo + Title
        JPanel leftTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftTop.setOpaque(false);
        
        JLabel title = new JLabel(Utils.getLibraryName());
        title.setForeground(Utils.ACCENT);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        
        JLabel subtitle = new JLabel(" • Admin Dashboard");
        subtitle.setForeground(Utils.TEXT);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        leftTop.add(title);
        leftTop.add(subtitle);

        // Right side: User info + Logout
        JPanel rightTop = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightTop.setOpaque(false);
        
        JPanel userCard = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        userCard.setBackground(Utils.CARD);
        userCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Utils.BORDER),
            new EmptyBorder(6, 12, 6, 12)
        ));
        
        // User icon placeholder
        JLabel userIcon = new JLabel("👤");
        userIcon.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        
        JLabel who = new JLabel(me.get("nama_lengkap"));
        who.setForeground(Utils.TEXT);
        who.setFont(Utils.FONT_B);
        
        JLabel role = new JLabel("ADMIN");
        role.setForeground(Utils.ACCENT);
        role.setFont(new Font("Segoe UI", Font.BOLD, 10));
        role.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Utils.ACCENT),
            new EmptyBorder(2, 6, 2, 6)
        ));
        
        userCard.add(userIcon);
        userCard.add(who);
        userCard.add(role);
        
        JButton logout = new JButton("Logout");
        logout.setFont(Utils.FONT_B);
        logout.setForeground(Utils.TEXT);
        logout.setBackground(Utils.CARD);
        logout.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Utils.BORDER),
            new EmptyBorder(8, 16, 8, 16)
        ));
        logout.setFocusPainted(false);
        logout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logout.addActionListener(e -> {
            if (confirmDialog("Keluar", "Apakah Anda yakin ingin logout?")) {
                DB.audit(Long.valueOf(id()), "LOGOUT", "users", me.get("user_id"), "Logout");
                dispose();
                new LoginPage();
            }
        });
        
        // Hover effect for logout
        logout.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                logout.setBackground(Utils.CARD2);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                logout.setBackground(Utils.CARD);
            }
        });

        rightTop.add(userCard);
        rightTop.add(logout);

        top.add(leftTop, BorderLayout.WEST);
        top.add(rightTop, BorderLayout.EAST);

        // =========== CONTENT PANELS ===========
        content.setBackground(Utils.BG);
        
        DashboardPanel dashboard = new DashboardPanel();
        PetugasPanel petugas = new PetugasPanel();
        AnggotaPanel anggota = new AnggotaPanel();
        BukuPanel buku = new BukuPanel();
        LaporanPanel laporan = new LaporanPanel();
        SettingsPanel settings = new SettingsPanel();
        AuditPanel audit = new AuditPanel();
        
        panels.put("dash", dashboard);
        panels.put("petugas", petugas);
        panels.put("anggota", anggota);
        panels.put("buku", buku);
        panels.put("laporan", laporan);
        panels.put("settings", settings);
        panels.put("audit", audit);
        
        content.add(dashboard, "dash");
        content.add(petugas, "petugas");
        content.add(anggota, "anggota");
        content.add(buku, "buku");
        content.add(laporan, "laporan");
        content.add(settings, "settings");
        content.add(audit, "audit");

        // =========== BOTTOM NAVIGATION ===========
        String[] labels = new String[]{
            "Dashboard", "Petugas", "Anggota", 
            "Buku", "Laporan", "️Settings", "Activity Log"
        };
        
        Runnable[] actions = new Runnable[]{
            () -> { dashboard.refresh(); show("dash", 0); },
            () -> { petugas.refresh(); show("petugas", 1); },
            () -> { anggota.refresh(); show("anggota", 2); },
            () -> { buku.refresh(); show("buku", 3); },
            () -> { laporan.refresh(); show("laporan", 4); },
            () -> { settings.refresh(); show("settings", 5); },
            () -> { audit.refresh(); show("audit", 6); }
        };
        
        JPanel nav = createBottomNav(labels, actions, 0);

        // =========== ASSEMBLE ROOT ===========
        root.add(top, BorderLayout.NORTH);
        root.add(content, BorderLayout.CENTER);
        root.add(nav, BorderLayout.SOUTH);

        setContentPane(root);
        dashboard.refresh();
        setVisible(true);
    }

    private int id() { 
        return Integer.parseInt(me.get("user_id")); 
    }

    private void show(String key, int activeIndex) {
        cards.show(content, key);
        updateNavHighlight(activeIndex);
    }
    
    private JPanel createBottomNav(String[] labels, Runnable[] actions, int activeIndex) {
        JPanel bar = new JPanel(new GridLayout(1, labels.length, 1, 0));
        bar.setBackground(Utils.BG);
        bar.setBorder(new EmptyBorder(0, 0, 0, 0));
        
        for (int i = 0; i < labels.length; i++) {
            JButton btn = createNavButton(labels[i], i == activeIndex);
            int idx = i;
            btn.addActionListener(e -> {
                actions[idx].run();
                updateNavHighlight(idx);
            });
            bar.add(btn);
        }
        return bar;
    }
    
    private JButton createNavButton(String text, boolean active) {
        JButton btn = new JButton(text);
        btn.setFont(Utils.FONT);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        if (active) {
            btn.setBackground(Utils.ACCENT);
            btn.setForeground(Color.WHITE);
            btn.setBorder(BorderFactory.createEmptyBorder(14, 0, 14, 0));
        } else {
            btn.setBackground(Utils.CARD);
            btn.setForeground(Utils.TEXT);
            btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 1, Utils.BORDER),
                new EmptyBorder(14, 0, 14, 0)
            ));
            
            btn.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    btn.setBackground(Utils.CARD2);
                }
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    btn.setBackground(Utils.CARD);
                }
            });
        }
        
        return btn;
    }
    
    private void updateNavHighlight(int activeIndex) {
        JPanel root = (JPanel) getContentPane();
        Component[] comps = root.getComponents();
        
        for (int i = 0; i < comps.length; i++) {
            if (comps[i] instanceof JPanel) {
                JPanel panel = (JPanel) comps[i];
                // PERBAIKAN: Gunakan instanceof Container dan cek komponennya
                if (panel.getComponentCount() > 0) {
                    Component child = panel.getComponent(0);
                    if (child instanceof JPanel) {
                        JPanel childPanel = (JPanel) child;
                        // Cek jumlah komponen dalam child panel
                        if (childPanel.getComponentCount() >= 7) {
                            root.remove(i);
                            
                            String[] labels = new String[]{
                                "Dashboard", "Petugas", "Anggota", 
                                "Buku", "Laporan", "Settings", "Activity Log"
                            };
                            
                            Runnable[] actions = new Runnable[]{
                                () -> { ((DashboardPanel)panels.get("dash")).refresh(); show("dash", 0); },
                                () -> { ((PetugasPanel)panels.get("petugas")).refresh(); show("petugas", 1); },
                                () -> { ((AnggotaPanel)panels.get("anggota")).refresh(); show("anggota", 2); },
                                () -> { ((BukuPanel)panels.get("buku")).refresh(); show("buku", 3); },
                                () -> { ((LaporanPanel)panels.get("laporan")).refresh(); show("laporan", 4); },
                                () -> { ((SettingsPanel)panels.get("settings")).refresh(); show("settings", 5); },
                                () -> { ((AuditPanel)panels.get("audit")).refresh(); show("audit", 6); }
                            };
                            
                            JPanel newNav = createBottomNav(labels, actions, activeIndex);
                            root.add(newNav, BorderLayout.SOUTH);
                            root.revalidate();
                            root.repaint();
                            break;
                        }
                    }
                }
            }
        }
    }
    
    // Helper methods for consistent UI
    JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(Utils.FONT_B);
        btn.setForeground(Color.WHITE);
        btn.setBackground(Utils.ACCENT);
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
    
    JButton createSecondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(Utils.FONT);
        btn.setForeground(Utils.TEXT);
        btn.setBackground(Utils.CARD);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Utils.BORDER),
            new EmptyBorder(8, 16, 8, 16)
        ));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(Utils.CARD2);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(Utils.CARD);
            }
        });
        
        return btn;
    }
    
    boolean confirmDialog(String title, String message) {
        int result = JOptionPane.showConfirmDialog(this, message, title, 
            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        return result == JOptionPane.YES_OPTION;
    }
    
    void showMessageDialog(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);
    }
    
    void showErrorDialog(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }

    // ========================= PANELS =========================

    class DashboardPanel extends JPanel {
        private final JLabel lbBooks = createStatLabel("0");
        private final JLabel lbMembers = createStatLabel("0");
        private final JLabel lbActive = createStatLabel("0");
        private final JLabel lbOverdue = createStatLabel("0");

        DashboardPanel() {
            setBackground(Utils.BG);
            setLayout(new BorderLayout());
            
            // Header
            JPanel header = new JPanel(new BorderLayout());
            header.setBackground(Utils.BG);
            header.setBorder(new EmptyBorder(20, 20, 20, 20));
            
            JLabel title = new JLabel("Dashboard Overview");
            title.setForeground(Utils.TEXT);
            title.setFont(new Font("Segoe UI", Font.BOLD, 24));
            
            JLabel subtitle = new JLabel("Ringkasan statistik perpustakaan");
            subtitle.setForeground(Utils.MUTED);
            subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            
            JPanel titlePanel = new JPanel(new BorderLayout());
            titlePanel.setOpaque(false);
            titlePanel.add(title, BorderLayout.NORTH);
            titlePanel.add(subtitle, BorderLayout.SOUTH);
            
            header.add(titlePanel, BorderLayout.WEST);
            add(header, BorderLayout.NORTH);

            // Stats Grid
            JPanel grid = new JPanel(new GridLayout(2, 2, 16, 16));
            grid.setBackground(Utils.BG);
            grid.setBorder(new EmptyBorder(0, 20, 20, 20));
            
            grid.add(createStatCard("Total Buku", lbBooks, Utils.ACCENT));
            grid.add(createStatCard("Total Anggota", lbMembers, Utils.ACCENT));
            grid.add(createStatCard("Peminjaman Aktif", lbActive, new Color(251, 188, 5)));
            grid.add(createStatCard("Terlambat", lbOverdue, new Color(251, 188, 5)));
            
            add(grid, BorderLayout.CENTER);
        }
        
        private JLabel createStatLabel(String text) {
            JLabel label = new JLabel(text, SwingConstants.CENTER);
            label.setForeground(Utils.TEXT);
            label.setFont(new Font("Segoe UI", Font.BOLD, 36));
            return label;
        }
        
        private JPanel createStatCard(String title, JLabel value, Color accentColor) {
            JPanel card = new JPanel(new BorderLayout());
            card.setBackground(Utils.CARD);
            card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Utils.BORDER),
                new EmptyBorder(20, 20, 20, 20)
            ));
            
            JPanel accentBar = new JPanel();
            accentBar.setBackground(accentColor);
            accentBar.setPreferredSize(new Dimension(card.getWidth(), 4));
            card.add(accentBar, BorderLayout.NORTH);
            
            JPanel content = new JPanel(new BorderLayout(0, 10));
            content.setOpaque(false);
            content.add(value, BorderLayout.CENTER);
            
            JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
            titleLabel.setForeground(Utils.MUTED);
            titleLabel.setFont(Utils.FONT);
            content.add(titleLabel, BorderLayout.SOUTH);
            
            card.add(content, BorderLayout.CENTER);
            return card;
        }

        void refresh() {
            try {
                int books = Integer.parseInt(DB.query("SELECT COUNT(*) c FROM books").get(0).get("c"));
                int members = Integer.parseInt(DB.query("SELECT COUNT(*) c FROM users WHERE role='USER'").get(0).get("c"));
                int active = Integer.parseInt(DB.query("SELECT COUNT(*) c FROM loans WHERE status='AKTIF'").get(0).get("c"));
                int overdue = Integer.parseInt(DB.query("SELECT COUNT(*) c FROM loans WHERE status='AKTIF' AND jatuh_tempo < CURDATE()").get(0).get("c"));

                lbBooks.setText(String.valueOf(books));
                lbMembers.setText(String.valueOf(members));
                lbActive.setText(String.valueOf(active));
                lbOverdue.setText(String.valueOf(overdue));

                badgeOverdue = overdue;
                if (overdue > 0) {
                    lbOverdue.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(234, 67, 53)),
                        new EmptyBorder(10, 20, 10, 20)
                    ));
                }
            } catch (Exception ignored) {}
        }
    }

    class PetugasPanel extends JPanel {
        DefaultTableModel model = new DefaultTableModel(new String[]{"ID","Username","Nama Lengkap","Gender","Alamat","Telp","Email","Status"},0);
        JTable table = new JTable(model);
        private JButton btnRefresh;
        private JTextField searchField;

        PetugasPanel() {
            setBackground(Utils.BG);
            setLayout(new BorderLayout());
            
            // Header dengan live search
            JPanel header = createPanelHeader("Kelola Petugas", 
                "Tambah, edit, dan kelola akun petugas perpustakaan");
            
            // Table
            styleTable();
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
            searchField.putClientProperty("JTextField.placeholderText", "Cari petugas...");
            
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
            
            JLabel searchIcon = new JLabel("Cari");
            searchIcon.setFont(new Font("Segoe UI", Font.PLAIN, 14));
             searchIcon.setForeground(Utils.TEXT);
            
            searchPanel.add(searchIcon);
            searchPanel.add(searchField);
            
            btnRefresh = new JButton("Refresh");
            btnRefresh.setFont(Utils.FONT);
            btnRefresh.setBackground(Utils.CARD);
            btnRefresh.setForeground(Utils.TEXT);
            btnRefresh.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Utils.BORDER),
                new EmptyBorder(6, 12, 6, 12)
            ));
            btnRefresh.setFocusPainted(false);
            btnRefresh.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnRefresh.addActionListener(e -> refresh());
            
            searchPanel.add(btnRefresh);
            
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
                    
                    if (column == 7) { // Status column
                        if ("Aktif".equals(value)) {
                            setForeground(new Color(52, 168, 83));
                        } else if ("Nonaktif".equals(value)) {
                            setForeground(new Color(234, 67, 53));
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
            
            JButton add = createPrimaryButton("Tambah Petugas");
            JButton edit = createSecondaryButton("Edit");
            JButton activate = createSecondaryButton("Aktifkan");
            JButton deactivate = createSecondaryButton("Nonaktifkan");
            JButton reset = createSecondaryButton("Reset Password");
            
            add.addActionListener(e -> openForm(null));
            edit.addActionListener(e -> {
                int r = table.getSelectedRow();
                if (r < 0) { 
                    showMessageDialog("Peringatan", "Pilih petugas terlebih dahulu."); 
                    return; 
                }
                int modelRow = table.convertRowIndexToModel(r);
                openForm(getRowData(modelRow));
            });
            activate.addActionListener(e -> setPetugasStatus(true));
            deactivate.addActionListener(e -> setPetugasStatus(false));
            reset.addActionListener(e -> resetPassword());
            
            actions.add(add);
            actions.add(edit);
            actions.add(activate);
            actions.add(deactivate);
            actions.add(reset);
            
            return actions;
        }
        
        private Map<String,String> getRowData(int row) {
            // PERBAIKAN: Gunakan HashMap untuk banyak parameter
            Map<String, String> data = new HashMap<>();
            data.put("user_id", String.valueOf(model.getValueAt(row,0)));
            data.put("username", String.valueOf(model.getValueAt(row,1)));
            data.put("nama_lengkap", String.valueOf(model.getValueAt(row,2)));
            data.put("gender", String.valueOf(model.getValueAt(row,3)));
            data.put("alamat", String.valueOf(model.getValueAt(row,4)));
            data.put("no_telp", String.valueOf(model.getValueAt(row,5)));
            data.put("email", String.valueOf(model.getValueAt(row,6)));
            data.put("status_aktif", "Aktif".equals(model.getValueAt(row,7)) ? "1":"0");
            return data;
        }

        void refresh() {
            try {
                model.setRowCount(0);
                var rows = DB.query(
                    "SELECT user_id,username,nama_lengkap,gender,alamat,no_telp,email,status_aktif " +
                    "FROM users WHERE role='PETUGAS' ORDER BY user_id DESC"
                );
                for (var r: rows) {
                    model.addRow(new Object[]{
                        r.get("user_id"), 
                        r.get("username"), 
                        r.get("nama_lengkap"),
                        r.get("gender") == null ? "" : r.get("gender"),
                        r.get("alamat") == null ? "" : r.get("alamat"),
                        r.get("no_telp") == null ? "" : r.get("no_telp"),
                        r.get("email") == null ? "" : r.get("email"),
                        "1".equals(r.get("status_aktif")) ? "Aktif":"Nonaktif"
                    });
                }
                btnRefresh.setText("Refresh (" + rows.size() + " petugas)");
            } catch (Exception ignored) {
                ignored.printStackTrace();
            }
        }
        
        private void setPetugasStatus(boolean active) {
            int r = table.getSelectedRow();
            if (r < 0) { 
                showMessageDialog("Peringatan", "Pilih petugas terlebih dahulu."); 
                return; 
            }
            
            int modelRow = table.convertRowIndexToModel(r);
            String id = String.valueOf(model.getValueAt(modelRow,0));
            String nama = String.valueOf(model.getValueAt(modelRow,2));
            String currentStatus = String.valueOf(model.getValueAt(modelRow,7));
            
            if ((active && "Aktif".equals(currentStatus)) || (!active && "Nonaktif".equals(currentStatus))) {
                showMessageDialog("Informasi", "Status sudah sesuai.");
                return;
            }
            
            String action = active ? "mengaktifkan" : "menonaktifkan";
            if (!confirmDialog("Konfirmasi", "Apakah Anda yakin ingin " + action + " petugas:\n" + nama + "?")) return;
            
            try {
                DB.exec("UPDATE users SET status_aktif=? WHERE user_id=? AND role='PETUGAS'", active?1:0, id);
                DB.audit(Long.valueOf(AdminPage.this.id()), "UPDATE", "users", id, 
                    (active?"Aktifkan":"Nonaktifkan")+" petugas");
                refresh();
                showMessageDialog("Sukses", "Status petugas berhasil diperbarui.");
            } catch (Exception ex) { 
                showErrorDialog("Error", "Gagal memperbarui status."); 
            }
        }
        
        private void resetPassword() {
            int r = table.getSelectedRow();
            if (r < 0) { 
                showMessageDialog("Peringatan", "Pilih petugas terlebih dahulu."); 
                return; 
            }
            
            int modelRow = table.convertRowIndexToModel(r);
            String id = String.valueOf(model.getValueAt(modelRow,0));
            String nama = String.valueOf(model.getValueAt(modelRow,2));
            
            String np = JOptionPane.showInputDialog(this, 
                "Masukkan password baru untuk petugas:\n" + nama, 
                "Reset Password", 
                JOptionPane.QUESTION_MESSAGE);
            
            if (np == null || np.trim().isEmpty()) return;
            
            try {
                DB.exec("UPDATE users SET password_hash=? WHERE user_id=? AND role='PETUGAS'", 
                    Utils.sha256(np), id);
                DB.audit(Long.valueOf(AdminPage.this.id()), "UPDATE", "users", id, "Reset password petugas");
                showMessageDialog("Sukses", "Password berhasil direset.");
            } catch (Exception ex) { 
                showErrorDialog("Error", "Gagal mereset password."); 
            }
        }

        void openForm(Map<String,String> data) {
            JDialog d = new JDialog(AdminPage.this, true);
            d.setTitle(data==null ? "Tambah Petugas Baru":"Edit Data Petugas");
            d.setSize(550, 550);
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
            
            JLabel title = new JLabel(data==null ? "Tambah Petugas Baru" : "Edit Data Petugas");
            title.setForeground(Utils.TEXT);
            title.setFont(new Font("Segoe UI", Font.BOLD, 18));
            title.setBorder(new EmptyBorder(0, 0, 20, 0));
            
            JPanel fields = new JPanel(new GridLayout(7, 1, 10, 15));
            fields.setOpaque(false);
            
            JTextField u = Utils.input("username");
            JPasswordField pw = Utils.passInput("password (isi jika tambah/reset)");
            JTextField nama = Utils.input("nama lengkap");
            
            JComboBox<String> genderCombo = new JComboBox<>(new String[]{"", "Laki-laki", "Perempuan"});
            genderCombo.setBackground(Utils.CARD2);
            genderCombo.setForeground(Utils.TEXT);
            genderCombo.setBorder(BorderFactory.createLineBorder(Utils.BORDER));
            
            JTextField alamat = Utils.input("alamat");
            JTextField telp = Utils.input("no telp");
            Utils.numericOnly(telp);
            JTextField email = Utils.input("email");
            
            if (data != null) {
                u.setText(data.get("username"));
                nama.setText(data.get("nama_lengkap"));
                telp.setText(data.get("no_telp"));
                email.setText(data.get("email"));
                if (data.get("gender") != null) {
                    genderCombo.setSelectedItem(data.get("gender"));
                }
                if (data.get("alamat") != null) {
                    alamat.setText(data.get("alamat"));
                }
            }
            
            fields.add(createFormRow("Username*", u));
            fields.add(createFormRow("Password", pw));
            fields.add(createFormRow("Nama Lengkap*", nama));
            fields.add(createFormRow("Gender", genderCombo));
            fields.add(createFormRow("Alamat", alamat));
            fields.add(createFormRow("No. Telepon", telp));
            fields.add(createFormRow("Email", email));
            
            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            buttons.setOpaque(false);
            
            JButton cancel = createSecondaryButton("Batal");
            JButton save = createPrimaryButton(data==null ? "Simpan" : "Update");
            
            cancel.addActionListener(e -> dialog.dispose());
            save.addActionListener(e -> savePetugas(data, u, pw, nama, genderCombo, alamat, telp, email, dialog));
            
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
        
        private void savePetugas(Map<String,String> data, JTextField u, JPasswordField pw, 
                                JTextField nama, JComboBox<String> genderCombo, 
                                JTextField alamat, JTextField telp, JTextField email, JDialog dialog) {
            try {
                if (u.getText().trim().isEmpty() || nama.getText().trim().isEmpty()) { 
                    showMessageDialog("Peringatan", "Username dan nama lengkap wajib diisi."); 
                    return; 
                }
                
                String gender = genderCombo.getSelectedItem() != null ? 
                               genderCombo.getSelectedItem().toString() : "";
                
                if (data == null) {
                    if (new String(pw.getPassword()).isEmpty()) { 
                        showMessageDialog("Peringatan", "Password wajib untuk petugas baru."); 
                        return; 
                    }
                    long idNew = DB.exec(
                        "INSERT INTO users(username,password_hash,role,nama_lengkap,gender,alamat,no_telp,email,status_aktif) " +
                        "VALUES (?,?,?,?,?,?,?,?,1)",
                        u.getText().trim(), 
                        Utils.sha256(new String(pw.getPassword())), 
                        "PETUGAS",
                        nama.getText().trim(),
                        gender.isEmpty() ? null : gender,
                        alamat.getText().trim().isEmpty() ? null : alamat.getText().trim(),
                        telp.getText().trim().isEmpty() ? null : telp.getText().trim(),
                        email.getText().trim().isEmpty() ? null : email.getText().trim()
                    );
                    DB.audit(Long.valueOf(AdminPage.this.id()), "CREATE", "users", String.valueOf(idNew), "Tambah petugas");
                    showMessageDialog("Sukses", "Petugas berhasil ditambahkan.");
                } else {
                    DB.exec("UPDATE users SET username=?, nama_lengkap=?, gender=?, alamat=?, no_telp=?, email=? WHERE user_id=? AND role='PETUGAS'",
                        u.getText().trim(), 
                        nama.getText().trim(),
                        gender.isEmpty() ? null : gender,
                        alamat.getText().trim().isEmpty() ? null : alamat.getText().trim(),
                        telp.getText().trim().isEmpty() ? null : telp.getText().trim(),
                        email.getText().trim().isEmpty() ? null : email.getText().trim(),
                        data.get("user_id")
                    );
                    if (!new String(pw.getPassword()).isEmpty()) {
                        DB.exec("UPDATE users SET password_hash=? WHERE user_id=?", 
                            Utils.sha256(new String(pw.getPassword())), data.get("user_id"));
                    }
                    DB.audit(Long.valueOf(AdminPage.this.id()), "UPDATE", "users", data.get("user_id"), "Edit petugas");
                    showMessageDialog("Sukses", "Data petugas berhasil diperbarui.");
                }
                refresh();
                dialog.dispose();
            } catch (Exception ex) {
                ex.printStackTrace();
                showErrorDialog("Error", "Gagal menyimpan. Username mungkin sudah digunakan.");
            }
        }
    }

    class AnggotaPanel extends JPanel {
        DefaultTableModel model = new DefaultTableModel(new String[]{"ID","Username","Nama","Kelas","Gender","Alamat","Telp","Email","Status"},0);
        JTable table = new JTable(model);
        private JButton btnRefresh;
        private JTextField searchField;

        AnggotaPanel() {
            setBackground(Utils.BG);
            setLayout(new BorderLayout());
            
            // Header dengan live search
            JPanel header = createPanelHeader("Kelola Anggota", 
                "Kelola data anggota perpustakaan");
            
            // Table
            styleTable();
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
            searchField.putClientProperty("JTextField.placeholderText", "Cari anggota...");
            
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
            
            JLabel searchIcon = new JLabel("Cari");
            searchIcon.setFont(new Font("Segoe UI", Font.PLAIN, 14));
             searchIcon.setForeground(Utils.TEXT);
            
            searchPanel.add(searchIcon);
            searchPanel.add(searchField);
            
            btnRefresh = new JButton("Refresh");
            btnRefresh.setFont(Utils.FONT);
            btnRefresh.setBackground(Utils.CARD);
            btnRefresh.setForeground(Utils.TEXT);
            btnRefresh.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Utils.BORDER),
                new EmptyBorder(6, 12, 6, 12)
            ));
            btnRefresh.setFocusPainted(false);
            btnRefresh.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnRefresh.addActionListener(e -> refresh());
            
            searchPanel.add(btnRefresh);
            
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
            
            JButton edit = createSecondaryButton("Edit");
            JButton activate = createSecondaryButton("Aktifkan");
            JButton deactivate = createSecondaryButton("Nonaktifkan");
            JButton reset = createSecondaryButton("Reset Password");
            
            edit.addActionListener(e -> {
                int r = table.getSelectedRow();
                if (r < 0) { 
                    showMessageDialog("Peringatan", "Pilih anggota terlebih dahulu."); 
                    return; 
                }
                int modelRow = table.convertRowIndexToModel(r);
                openForm(getRowData(modelRow));
            });
            activate.addActionListener(e -> setAnggotaStatus(true));
            deactivate.addActionListener(e -> setAnggotaStatus(false));
            reset.addActionListener(e -> resetPassword());
            
            actions.add(edit);
            actions.add(activate);
            actions.add(deactivate);
            actions.add(reset);
            
            return actions;
        }
        
        private Map<String,String> getRowData(int row) {
            // PERBAIKAN: Gunakan HashMap untuk banyak parameter
            Map<String, String> data = new HashMap<>();
            data.put("user_id", String.valueOf(model.getValueAt(row,0)));
            data.put("username", String.valueOf(model.getValueAt(row,1)));
            data.put("nama_lengkap", String.valueOf(model.getValueAt(row,2)));
            data.put("kelas", String.valueOf(model.getValueAt(row,3)));
            data.put("gender", String.valueOf(model.getValueAt(row,4)));
            data.put("alamat", String.valueOf(model.getValueAt(row,5)));
            data.put("no_telp", String.valueOf(model.getValueAt(row,6)));
            data.put("email", String.valueOf(model.getValueAt(row,7)));
            return data;
        }

        void refresh() {
            try {
                model.setRowCount(0);
                var rows = DB.query(
                    "SELECT user_id,username,nama_lengkap,kelas,gender,alamat,no_telp,email,status_aktif " +
                    "FROM users WHERE role='USER' ORDER BY user_id DESC"
                );
                for (var r: rows) {
                    model.addRow(new Object[]{
                        r.get("user_id"), 
                        r.get("username"), 
                        r.get("nama_lengkap"),
                        r.get("kelas") == null ? "" : r.get("kelas"),
                        r.get("gender") == null ? "" : r.get("gender"),
                        r.get("alamat") == null ? "" : r.get("alamat"),
                        r.get("no_telp") == null ? "" : r.get("no_telp"),
                        r.get("email") == null ? "" : r.get("email"),
                        "1".equals(r.get("status_aktif")) ? "Aktif":"Nonaktif"
                    });
                }
                btnRefresh.setText("Refresh (" + rows.size() + " anggota)");
            } catch (Exception ignored) {}
        }
        
        private void setAnggotaStatus(boolean active) {
            int r = table.getSelectedRow();
            if (r < 0) { 
                showMessageDialog("Peringatan", "Pilih anggota terlebih dahulu."); 
                return; 
            }
            
            int modelRow = table.convertRowIndexToModel(r);
            String id = String.valueOf(model.getValueAt(modelRow,0));
            String nama = String.valueOf(model.getValueAt(modelRow,2));
            String currentStatus = String.valueOf(model.getValueAt(modelRow,8));
            
            if ((active && "Aktif".equals(currentStatus)) || (!active && "Nonaktif".equals(currentStatus))) {
                showMessageDialog("Informasi", "Status sudah sesuai.");
                return;
            }
            
            String action = active ? "mengaktifkan" : "menonaktifkan";
            if (!confirmDialog("Konfirmasi", "Apakah Anda yakin ingin " + action + " anggota:\n" + nama + "?")) return;
            
            try {
                DB.exec("UPDATE users SET status_aktif=? WHERE user_id=? AND role='USER'", active?1:0, id);
                DB.audit(Long.valueOf(AdminPage.this.id()), "UPDATE", "users", id, 
                    (active?"Aktifkan":"Nonaktifkan")+" anggota");
                refresh();
                showMessageDialog("Sukses", "Status anggota berhasil diperbarui.");
            } catch (Exception ex) { 
                showErrorDialog("Error", "Gagal memperbarui status."); 
            }
        }
        
        private void resetPassword() {
            int r = table.getSelectedRow();
            if (r < 0) { 
                showMessageDialog("Peringatan", "Pilih anggota terlebih dahulu."); 
                return; 
            }
            
            int modelRow = table.convertRowIndexToModel(r);
            String id = String.valueOf(model.getValueAt(modelRow,0));
            String nama = String.valueOf(model.getValueAt(modelRow,2));
            
            String np = JOptionPane.showInputDialog(this, 
                "Masukkan password baru untuk anggota:\n" + nama, 
                "Reset Password", 
                JOptionPane.QUESTION_MESSAGE);
            
            if (np == null || np.trim().isEmpty()) return;
            
            try {
                DB.exec("UPDATE users SET password_hash=? WHERE user_id=? AND role='USER'", 
                    Utils.sha256(np), id);
                DB.audit(Long.valueOf(AdminPage.this.id()), "UPDATE", "users", id, "Reset password anggota");
                showMessageDialog("Sukses", "Password berhasil direset.");
            } catch (Exception ex) { 
                showErrorDialog("Error", "Gagal mereset password."); 
            }
        }

        void openForm(Map<String,String> data) {
            JDialog d = new JDialog(AdminPage.this, true);
            d.setTitle("Edit Data Anggota");
            d.setSize(550, 600);
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
            
            JLabel title = new JLabel("Edit Data Anggota");
            title.setForeground(Utils.TEXT);
            title.setFont(new Font("Segoe UI", Font.BOLD, 18));
            title.setBorder(new EmptyBorder(0, 0, 20, 0));
            
            JPanel fields = new JPanel(new GridLayout(8, 1, 10, 15));
            fields.setOpaque(false);
            
            JTextField u = Utils.input("username");
            JTextField nama = Utils.input("nama lengkap");
            JTextField kelas = Utils.input("kelas");
            
            JComboBox<String> genderCombo = new JComboBox<>(new String[]{"", "Laki-laki", "Perempuan"});
            genderCombo.setBackground(Utils.CARD2);
            genderCombo.setForeground(Utils.TEXT);
            genderCombo.setBorder(BorderFactory.createLineBorder(Utils.BORDER));
            
            JTextField alamat = Utils.input("alamat");
            JTextField telp = Utils.input("no telp");
            Utils.numericOnly(telp);
            JTextField email = Utils.input("email");
            
            u.setText(data.get("username"));
            nama.setText(data.get("nama_lengkap"));
            kelas.setText(data.get("kelas"));
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
            
            fields.add(createFormRow("Username*", u));
            fields.add(createFormRow("Nama Lengkap*", nama));
            fields.add(createFormRow("Kelas", kelas));
            fields.add(createFormRow("Gender", genderCombo));
            fields.add(createFormRow("Alamat", alamat));
            fields.add(createFormRow("No. Telepon", telp));
            fields.add(createFormRow("Email", email));
            
            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            buttons.setOpaque(false);
            
            JButton cancel = createSecondaryButton("Batal");
            JButton save = createPrimaryButton("Update");
            
            cancel.addActionListener(e -> dialog.dispose());
            save.addActionListener(e -> saveAnggota(data, u, nama, kelas, genderCombo, alamat, telp, email, dialog));
            
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
        
        private void saveAnggota(Map<String,String> data, JTextField u, JTextField nama, 
                                JTextField kelas, JComboBox<String> genderCombo,
                                JTextField alamat, JTextField telp, JTextField email, JDialog dialog) {
            try {
                if (u.getText().trim().isEmpty() || nama.getText().trim().isEmpty()) { 
                    showMessageDialog("Peringatan", "Username dan nama lengkap wajib diisi."); 
                    return; 
                }
                
                String gender = genderCombo.getSelectedItem() != null ? 
                               genderCombo.getSelectedItem().toString() : "";
                
                DB.exec("UPDATE users SET username=?, nama_lengkap=?, kelas=?, gender=?, alamat=?, no_telp=?, email=? WHERE user_id=? AND role='USER'",
                    u.getText().trim(), 
                    nama.getText().trim(), 
                    kelas.getText().trim().isEmpty() ? null : kelas.getText().trim(),
                    gender.isEmpty() ? null : gender,
                    alamat.getText().trim().isEmpty() ? null : alamat.getText().trim(),
                    telp.getText().trim().isEmpty() ? null : telp.getText().trim(),
                    email.getText().trim().isEmpty() ? null : email.getText().trim(),
                    data.get("user_id")
                );
                DB.audit(Long.valueOf(AdminPage.this.id()), "UPDATE", "users", data.get("user_id"), "Edit anggota");
                showMessageDialog("Sukses", "Data anggota berhasil diperbarui.");
                refresh();
                dialog.dispose();
            } catch (Exception ex) {
                ex.printStackTrace();
                showErrorDialog("Error", "Gagal menyimpan. Username mungkin sudah digunakan.");
            }
        }
    }

    class BukuPanel extends JPanel {
        DefaultTableModel model = new DefaultTableModel(new String[]{
            "ID","Kode","ISBN","Judul","Penulis","Penerbit","Tahun","Kategori","Rak","Stok Total","Stok Tersedia"
        },0);
        JTable table = new JTable(model);
        private JButton btnRefresh;
        private JTextField searchField;

        BukuPanel() {
            setBackground(Utils.BG);
            setLayout(new BorderLayout());
            
            // Header dengan live search
            JPanel header = createPanelHeader("Kelola Buku", 
                "Kelola koleksi buku, kategori, dan rak");
            
            // Table
            styleTable();
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
            searchField.putClientProperty("JTextField.placeholderText", "Cari buku...");
            
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
            
            JLabel searchIcon = new JLabel("Cari");
            searchIcon.setFont(new Font("Segoe UI", Font.PLAIN, 14));
             searchIcon.setForeground(Utils.TEXT);
            
            searchPanel.add(searchIcon);
            searchPanel.add(searchField);
            
            btnRefresh = new JButton("Refresh");
            btnRefresh.setFont(Utils.FONT);
            btnRefresh.setBackground(Utils.CARD);
            btnRefresh.setForeground(Utils.TEXT);
            btnRefresh.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Utils.BORDER),
                new EmptyBorder(6, 12, 6, 12)
            ));
            btnRefresh.setFocusPainted(false);
            btnRefresh.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnRefresh.addActionListener(e -> refresh());
            
            searchPanel.add(btnRefresh);
            
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
                    
                    // Warn if available stock is low (column 10 - stok tersedia)
                    if (column == 10 && value != null) {
                        try {
                            int available = Integer.parseInt(value.toString());
                            if (available <= 0) {
                                setForeground(new Color(234, 67, 53));
                            } else if (available <= 2) {
                                setForeground(new Color(251, 188, 5));
                            }
                        } catch (NumberFormatException e) {
                            // Ignore
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
            
            JButton add = createPrimaryButton("Tambah Buku");
            JButton edit = createSecondaryButton("Edit");
            JButton delete = createSecondaryButton("Hapus");
            JButton kategori = createSecondaryButton("Kelola Kategori");
            JButton rak = createSecondaryButton("Kelola Rak");
            
            add.addActionListener(e -> openForm(null));
            edit.addActionListener(e -> {
                int r = table.getSelectedRow();
                if (r < 0) { 
                    showMessageDialog("Peringatan", "Pilih buku terlebih dahulu."); 
                    return; 
                }
                int modelRow = table.convertRowIndexToModel(r);
                openForm(getRowData(modelRow));
            });
            delete.addActionListener(e -> deleteBuku());
            kategori.addActionListener(e -> openKategoriManager());
            rak.addActionListener(e -> openRakManager());
            
            actions.add(add);
            actions.add(edit);
            actions.add(delete);
            actions.add(kategori);
            actions.add(rak);
            
            return actions;
        }
        
        private Map<String,String> getRowData(int row) {
            try {
                // PERBAIKAN: Gunakan HashMap untuk banyak parameter
                Map<String, String> data = new HashMap<>();
                
                String bookId = String.valueOf(model.getValueAt(row, 0));
                var bookData = DB.query("SELECT * FROM books WHERE book_id = ?", bookId).get(0);
                
                data.put("book_id", bookId);
                data.put("code", String.valueOf(model.getValueAt(row,1)));
                data.put("isbn", String.valueOf(model.getValueAt(row,2)));
                data.put("judul", String.valueOf(model.getValueAt(row,3)));
                data.put("penulis", String.valueOf(model.getValueAt(row,4)));
                data.put("penerbit", String.valueOf(model.getValueAt(row,5)));
                data.put("tahun", String.valueOf(model.getValueAt(row,6)));
                data.put("category_id", bookData.get("category_id") != null ? bookData.get("category_id") : "");
                data.put("rack_id", bookData.get("rack_id") != null ? bookData.get("rack_id") : "");
                data.put("stok_total", String.valueOf(model.getValueAt(row,9)));
                data.put("stok_tersedia", String.valueOf(model.getValueAt(row,10)));
                
                return data;
            } catch (Exception e) {
                // Fallback jika query gagal
                Map<String, String> data = new HashMap<>();
                data.put("book_id", String.valueOf(model.getValueAt(row,0)));
                data.put("code", String.valueOf(model.getValueAt(row,1)));
                data.put("isbn", String.valueOf(model.getValueAt(row,2)));
                data.put("judul", String.valueOf(model.getValueAt(row,3)));
                data.put("penulis", String.valueOf(model.getValueAt(row,4)));
                data.put("penerbit", String.valueOf(model.getValueAt(row,5)));
                data.put("tahun", String.valueOf(model.getValueAt(row,6)));
                data.put("category_id", "");
                data.put("rack_id", "");
                data.put("stok_total", String.valueOf(model.getValueAt(row,9)));
                data.put("stok_tersedia", String.valueOf(model.getValueAt(row,10)));
                
                return data;
            }
        }

        void refresh() {
            try {
                model.setRowCount(0);
                var rows = DB.query(
                    "SELECT b.book_id,b.code,b.isbn,b.judul,b.penulis,b.penerbit,b.tahun, " +
                    "c.name kategori, r.code rak, b.stok_total,b.stok_tersedia " +
                    "FROM books b " +
                    "LEFT JOIN categories c ON b.category_id=c.category_id " +
                    "LEFT JOIN racks r ON b.rack_id=r.rack_id " +
                    "ORDER BY b.book_id DESC"
                );
                for (var r: rows) {
                    model.addRow(new Object[]{
                        r.get("book_id"), 
                        r.get("code"), 
                        r.get("isbn") == null ? "" : r.get("isbn"),
                        r.get("judul"),
                        r.get("penulis") == null ? "" : r.get("penulis"),
                        r.get("penerbit") == null ? "" : r.get("penerbit"),
                        r.get("tahun") == null ? "" : r.get("tahun"),
                        r.get("kategori") == null ? "" : r.get("kategori"), 
                        r.get("rak") == null ? "" : r.get("rak"),
                        r.get("stok_total"), 
                        r.get("stok_tersedia")
                    });
                }
                btnRefresh.setText("Refresh (" + rows.size() + " buku)");
            } catch (Exception ignored) {
                ignored.printStackTrace();
            }
        }
        
        private void deleteBuku() {
            int r = table.getSelectedRow();
            if (r < 0) { 
                showMessageDialog("Peringatan", "Pilih buku terlebih dahulu."); 
                return; 
            }
            
            int modelRow = table.convertRowIndexToModel(r);
            String id = String.valueOf(model.getValueAt(modelRow,0));
            String judul = String.valueOf(model.getValueAt(modelRow,3));
            
            if (!confirmDialog("Konfirmasi Hapus", 
                "Apakah Anda yakin ingin menghapus buku:\n" + 
                "Judul: " + judul + "\n\n" +
                "PERHATIAN: Buku yang sedang dipinjam tidak dapat dihapus!")) return;
            
            try {
                DB.exec("DELETE FROM books WHERE book_id=?", id);
                DB.audit(Long.valueOf(AdminPage.this.id()), "DELETE", "books", id, "Hapus buku");
                refresh();
                showMessageDialog("Sukses", "Buku berhasil dihapus.");
            } catch (Exception ex) { 
                showErrorDialog("Error", "Gagal menghapus. Pastikan buku tidak sedang dipinjam."); 
            }
        }
        
        class Item {
            String id; String name;
            Item(String id, String name) { this.id=id; this.name=name; }
            @Override
            public String toString() { return name; }
        }

        void openForm(Map<String,String> data) {
            JDialog d = new JDialog(AdminPage.this, true);
            d.setTitle(data==null ? "Tambah Buku Baru":"Edit Data Buku");
            d.setSize(700, 650);
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
            
            JLabel title = new JLabel(data==null ? "Tambah Buku Baru" : "Edit Data Buku");
            title.setForeground(Utils.TEXT);
            title.setFont(new Font("Segoe UI", Font.BOLD, 18));
            title.setBorder(new EmptyBorder(0, 0, 20, 0));
            
            // Load combo box data
            JComboBox<Item> cbKat = new JComboBox<>();
            JComboBox<Item> cbRak = new JComboBox<>();
            loadCombo(cbKat, "SELECT category_id id, name name FROM categories ORDER BY name");
            loadCombo(cbRak, "SELECT rack_id id, code name FROM racks ORDER BY code");
            
            // Form fields
            JPanel fields = new JPanel(new GridLayout(10, 1, 10, 15));
            fields.setOpaque(false);
            
            JTextField code = Utils.input("kode buku");
            JTextField isbn = Utils.input("isbn");
            JTextField judul = Utils.input("judul");
            JTextField penulis = Utils.input("penulis");
            JTextField penerbit = Utils.input("penerbit");
            JTextField tahun = Utils.input("tahun (angka)");
            Utils.numericOnly(tahun);
            JTextField stok = Utils.input("stok total (angka)");
            Utils.numericOnly(stok);
            
            // Style comboboxes
            styleCombo(cbKat);
            styleCombo(cbRak);
            
            if (data != null) {
                code.setText(data.get("code"));
                isbn.setText(data.get("isbn"));
                judul.setText(data.get("judul"));
                penulis.setText(data.get("penulis"));
                penerbit.setText(data.get("penerbit"));
                tahun.setText(data.get("tahun"));
                stok.setText(data.get("stok_total"));
                
                selectCombo(cbKat, data.get("category_id"));
                selectCombo(cbRak, data.get("rack_id"));
            }
            
            fields.add(createFormRow("Kode Buku*", code));
            fields.add(createFormRow("ISBN", isbn));
            fields.add(createFormRow("Judul*", judul));
            fields.add(createFormRow("Penulis", penulis));
            fields.add(createFormRow("Penerbit", penerbit));
            fields.add(createFormRow("Tahun", tahun));
            fields.add(createFormRow("Kategori", cbKat));
            fields.add(createFormRow("Rak", cbRak));
            fields.add(createFormRow("Stok Total*", stok));
            
            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            buttons.setOpaque(false);
            
            JButton cancel = createSecondaryButton("Batal");
            JButton save = createPrimaryButton(data==null ? "Simpan" : "Update");
            
            cancel.addActionListener(e -> dialog.dispose());
            save.addActionListener(e -> saveBuku(data, code, isbn, judul, penulis, penerbit, tahun, stok, cbKat, cbRak, dialog));
            
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
        
        private void styleCombo(JComboBox<?> cb) {
            cb.setBackground(Utils.CARD2);
            cb.setForeground(Utils.TEXT);
            cb.setBorder(BorderFactory.createLineBorder(Utils.BORDER));
        }
        
        private void loadCombo(JComboBox<Item> cb, String sql) {
            try {
                cb.removeAllItems();
                cb.addItem(new Item(null, "-- Pilih --"));
                var rows = DB.query(sql);
                for (var r: rows) cb.addItem(new Item(r.get("id"), r.get("name")));
            } catch (Exception ignored) {}
        }
        
        private void selectCombo(JComboBox<Item> cb, String id) {
            if (id == null || id.isEmpty()) {
                cb.setSelectedIndex(0);
                return;
            }
            for (int i=0; i<cb.getItemCount(); i++) {
                Item it = cb.getItemAt(i);
                if (it != null && it.id != null && it.id.equals(id)) { 
                    cb.setSelectedIndex(i); 
                    break; 
                }
            }
        }
        
        private void saveBuku(Map<String,String> data, JTextField code, JTextField isbn, JTextField judul, 
                             JTextField penulis, JTextField penerbit, JTextField tahun, JTextField stok,
                             JComboBox<Item> cbKat, JComboBox<Item> cbRak, JDialog dialog) {
            try {
                if (code.getText().trim().isEmpty() || judul.getText().trim().isEmpty() || stok.getText().trim().isEmpty()) { 
                    showMessageDialog("Peringatan", "Kode, judul, dan stok wajib diisi."); 
                    return; 
                }
                
                int stokTotal;
                try {
                    stokTotal = Integer.parseInt(stok.getText().trim());
                    if (stokTotal < 0) {
                        showMessageDialog("Peringatan", "Stok tidak boleh negatif.");
                        return;
                    }
                } catch (NumberFormatException e) {
                    showErrorDialog("Error", "Format angka tidak valid pada stok.");
                    return;
                }
                
                Integer tahunVal = null;
                if (!tahun.getText().trim().isEmpty()) {
                    try {
                        tahunVal = Integer.parseInt(tahun.getText().trim());
                    } catch (NumberFormatException e) {
                        showErrorDialog("Error", "Format tahun tidak valid.");
                        return;
                    }
                }
                
                Item kat = (Item) cbKat.getSelectedItem();
                Item rak = (Item) cbRak.getSelectedItem();
                
                String katId = (kat == null || kat.id == null) ? null : kat.id;
                String rakId = (rak == null || rak.id == null) ? null : rak.id;
                
                if (data == null) {
                    // Tambah buku baru
                    long idNew = DB.exec(
                        "INSERT INTO books(code,isbn,judul,penulis,penerbit,tahun,category_id,rack_id,stok_total,stok_tersedia) " +
                        "VALUES (?,?,?,?,?,?,?,?,?,?)",
                        code.getText().trim(), 
                        isbn.getText().trim().isEmpty() ? null : isbn.getText().trim(), 
                        judul.getText().trim(),
                        penulis.getText().trim().isEmpty() ? null : penulis.getText().trim(), 
                        penerbit.getText().trim().isEmpty() ? null : penerbit.getText().trim(),
                        tahunVal,
                        katId,
                        rakId,
                        stokTotal, 
                        stokTotal
                    );
                    DB.audit(Long.valueOf(AdminPage.this.id()), "CREATE", "books", String.valueOf(idNew), "Tambah buku");
                    showMessageDialog("Sukses", "Buku berhasil ditambahkan.");
                } else {
                    // Update buku yang ada
                    DB.exec(
                        "UPDATE books SET code=?,isbn=?,judul=?,penulis=?,penerbit=?,tahun=?,category_id=?,rack_id=?,stok_total=?,stok_tersedia=? WHERE book_id=?",
                        code.getText().trim(), 
                        isbn.getText().trim().isEmpty() ? null : isbn.getText().trim(), 
                        judul.getText().trim(),
                        penulis.getText().trim().isEmpty() ? null : penulis.getText().trim(), 
                        penerbit.getText().trim().isEmpty() ? null : penerbit.getText().trim(),
                        tahunVal,
                        katId,
                        rakId,
                        stokTotal, 
                        stokTotal, 
                        data.get("book_id")
                    );
                    DB.audit(Long.valueOf(AdminPage.this.id()), "UPDATE", "books", data.get("book_id"), "Edit buku");
                    showMessageDialog("Sukses", "Data buku berhasil diperbarui.");
                }
                refresh();
                dialog.dispose();
            } catch (NumberFormatException e) {
                showErrorDialog("Error", "Format angka tidak valid.");
            } catch (Exception ex) {
                ex.printStackTrace();
                showErrorDialog("Error", "Gagal menyimpan. Kode buku mungkin sudah digunakan.");
            }
        }
        
        void openKategoriManager() {
            ManagerDialog mg = new ManagerDialog(AdminPage.this, "Kategori", "categories", "category_id", "name", AdminPage.this.id());
            mg.open();
            refresh();
        }

        void openRakManager() {
            ManagerDialog mg = new ManagerDialog(AdminPage.this, "Rak", "racks", "rack_id", "code", AdminPage.this.id());
            mg.open();
            refresh();
        }

        class ManagerDialog {
            JFrame owner;
            String title;
            String table, idCol, nameCol;
            int actorId;
            DefaultTableModel m = new DefaultTableModel(new String[]{"ID","Nama"},0);
            JTable t = new JTable(m);

            ManagerDialog(JFrame owner, String title, String table, String idCol, String nameCol, int actorId) {
                this.owner=owner; this.title=title; this.table=table; this.idCol=idCol; this.nameCol=nameCol; this.actorId=actorId;
            }

            void open() {
                JDialog d = new JDialog(owner, true);
                d.setTitle("Kelola " + title);
                d.setSize(500, 400);
                d.setLocationRelativeTo(owner);
                d.getContentPane().setBackground(Utils.BG);

                JPanel panel = new JPanel(new BorderLayout());
                panel.setBackground(Utils.BG);
                panel.setBorder(new EmptyBorder(20, 20, 20, 20));
                
                JLabel header = new JLabel("Kelola " + title);
                header.setForeground(Utils.TEXT);
                header.setFont(new Font("Segoe UI", Font.BOLD, 18));
                header.setBorder(new EmptyBorder(0, 0, 20, 0));
                
                Utils.styleTable(t);
                t.setRowHeight(35);
                JScrollPane sp = new JScrollPane(t);
                sp.getViewport().setBackground(Utils.CARD);
                sp.setBorder(BorderFactory.createLineBorder(Utils.BORDER));
                
                JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
                actions.setOpaque(false);
                
                JButton add = createPrimaryButton("Tambah");
                JButton edit = createSecondaryButton("Edit");
                JButton del = createSecondaryButton("Hapus");
                
                add.addActionListener(e -> form(d, null));
                edit.addActionListener(e -> {
                    int r = t.getSelectedRow();
                    if (r<0) { 
                        showMessageDialog("Peringatan", "Pilih data terlebih dahulu."); 
                        return; 
                    }
                    // PERBAIKAN: Gunakan HashMap
                    Map<String, String> rowData = new HashMap<>();
                    rowData.put("id", String.valueOf(m.getValueAt(r,0)));
                    rowData.put("name", String.valueOf(m.getValueAt(r,1)));
                    form(d, rowData);
                });
                del.addActionListener(e -> {
                    int r = t.getSelectedRow();
                    if (r<0) { 
                        showMessageDialog("Peringatan", "Pilih data terlebih dahulu."); 
                        return; 
                    }
                    String id = String.valueOf(m.getValueAt(r,0));
                    String name = String.valueOf(m.getValueAt(r,1));
                    
                    if (!confirmDialog("Konfirmasi Hapus", 
                        "Apakah Anda yakin ingin menghapus " + title.toLowerCase() + ":\n" + 
                        name + "\n\n" +
                        "PERHATIAN: " + title + " yang digunakan oleh buku tidak dapat dihapus!")) return;
                    
                    try {
                        DB.exec("DELETE FROM "+table+" WHERE "+idCol+"=?", id);
                        DB.audit(Long.valueOf(actorId), "DELETE", table, id, "Hapus "+title);
                        refresh();
                        showMessageDialog("Sukses", title + " berhasil dihapus.");
                    } catch (Exception ex) { 
                        showErrorDialog("Error", "Gagal hapus. " + title + " mungkin masih digunakan."); 
                    }
                });

                actions.add(add); actions.add(edit); actions.add(del);
                
                panel.add(header, BorderLayout.NORTH);
                panel.add(sp, BorderLayout.CENTER);
                panel.add(actions, BorderLayout.SOUTH);

                d.setContentPane(panel);
                refresh();
                d.setVisible(true);
            }

            void refresh() {
                try {
                    m.setRowCount(0);
                    var rows = DB.query("SELECT "+idCol+" id, "+nameCol+" name FROM "+table+" ORDER BY "+nameCol);
                    for (var r: rows) m.addRow(new Object[]{ r.get("id"), r.get("name") });
                } catch (Exception ignored) {}
            }

            void form(JDialog parent, Map<String,String> data) {
                boolean edit = data != null;
                String val = edit ? data.get("name") : "";
                
                String input = (String) JOptionPane.showInputDialog(parent, 
                    (edit?"Edit":"Tambah")+" " + title + ":", 
                    edit ? "Edit " + title : "Tambah " + title, 
                    JOptionPane.QUESTION_MESSAGE, null, null, val);
                
                if (input == null) return;
                input = input.trim();
                if (input.isEmpty()) return;

                try {
                    if (!edit) {
                        long idNew = DB.exec("INSERT INTO "+table+"("+nameCol+") VALUES (?)", input);
                        DB.audit(Long.valueOf(actorId), "CREATE", table, String.valueOf(idNew), "Tambah "+title);
                        showMessageDialog("Sukses", title + " berhasil ditambahkan.");
                    } else {
                        DB.exec("UPDATE "+table+" SET "+nameCol+"=? WHERE "+idCol+"=?", input, data.get("id"));
                        DB.audit(Long.valueOf(actorId), "UPDATE", table, data.get("id"), "Edit "+title);
                        showMessageDialog("Sukses", title + " berhasil diperbarui.");
                    }
                    refresh();
                } catch (Exception ex) {
                    showErrorDialog("Error", "Gagal menyimpan. Nama mungkin sudah digunakan.");
                }
            }
        }
    }

    class LaporanPanel extends JPanel {
        DefaultTableModel model = new DefaultTableModel(new String[]{"ID","Tanggal","Jatuh Tempo","Anggota","Petugas","Status","Total Item"},0);
        JTable table = new JTable(model);
        private JButton btnRefresh;
        private JTextField from = Utils.input("YYYY-MM-DD");
        private JTextField to = Utils.input("YYYY-MM-DD");
        private JTextField searchField;

        LaporanPanel() {
            setBackground(Utils.BG);
            setLayout(new BorderLayout());
            
            // Header dengan live search
            JPanel header = createPanelHeader("Laporan Peminjaman", 
                "Lihat dan filter data peminjaman buku");
            
            // Filter panel dengan validasi tanggal
            JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            filterPanel.setBackground(Utils.BG);
            filterPanel.setBorder(new EmptyBorder(0, 20, 10, 20));
            
            filterPanel.add(new JLabel("Dari:"));
            
            // Validasi input tanggal - hanya angka dan dash
            from.addKeyListener(new java.awt.event.KeyAdapter() {
                @Override
                public void keyTyped(java.awt.event.KeyEvent e) {
                    char c = e.getKeyChar();
                    if (!((c >= '0' && c <= '9') || c == '-' || c == '\b')) {
                        e.consume();
                    }
                }
            });
            
            filterPanel.add(from);
            filterPanel.add(new JLabel("Sampai:"));
            
            to.addKeyListener(new java.awt.event.KeyAdapter() {
                @Override
                public void keyTyped(java.awt.event.KeyEvent e) {
                    char c = e.getKeyChar();
                    if (!((c >= '0' && c <= '9') || c == '-' || c == '\b')) {
                        e.consume();
                    }
                }
            });
            
            filterPanel.add(to);
            
            JButton filterBtn = createSecondaryButton("Filter");
            JButton clearBtn = createSecondaryButton("Clear");
            
            filterBtn.addActionListener(e -> refresh());
            clearBtn.addActionListener(e -> { 
                from.setText(""); 
                to.setText(""); 
                refresh(); 
            });
            
            filterPanel.add(filterBtn);
            filterPanel.add(clearBtn);
            
            // Table dengan live search
            styleTable();
            JScrollPane scroll = new JScrollPane(table);
            scroll.setBorder(new EmptyBorder(0, 20, 20, 20));
            scroll.getViewport().setBackground(Utils.CARD);
            
            add(header, BorderLayout.NORTH);
            add(filterPanel, BorderLayout.CENTER);
            add(scroll, BorderLayout.SOUTH);
            
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
            searchField.putClientProperty("JTextField.placeholderText", "Cari laporan...");
            
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
            
            JLabel searchIcon = new JLabel("Cari");
            searchIcon.setFont(new Font("Segoe UI", Font.PLAIN, 14));
             searchIcon.setForeground(Utils.TEXT);
            
            searchPanel.add(searchIcon);
            searchPanel.add(searchField);
            
            btnRefresh = new JButton("Refresh");
            btnRefresh.setFont(Utils.FONT);
            btnRefresh.setBackground(Utils.CARD);
            btnRefresh.setForeground(Utils.TEXT);
            btnRefresh.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Utils.BORDER),
                new EmptyBorder(6, 12, 6, 12)
            ));
            btnRefresh.setFocusPainted(false);
            btnRefresh.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnRefresh.addActionListener(e -> refresh());
            
            searchPanel.add(btnRefresh);
            
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
                    
                    // Status column styling
                    if (column == 5) {
                        if ("AKTIF".equals(value)) {
                            setForeground(new Color(66, 133, 244));
                        } else if ("SELESAI".equals(value)) {
                            setForeground(new Color(52, 168, 83));
                        } else if ("BATAL".equals(value)) {
                            setForeground(new Color(234, 67, 53));
                        }
                    }
                    
                    // Date comparison for overdue
                    if (column == 2 && value != null) {
                        try {
                            String dueDate = value.toString();
                            String status = (String) table.getValueAt(row, 5);
                            if ("AKTIF".equals(status)) {
                                java.sql.Date due = java.sql.Date.valueOf(dueDate);
                                java.sql.Date today = new java.sql.Date(System.currentTimeMillis());
                                if (due.before(today)) {
                                    setForeground(new Color(234, 67, 53));
                                }
                            }
                        } catch (Exception e) {
                            // Ignore
                        }
                    }
                    
                    setBorder(noFocusBorder);
                    return c;
                }
            });
            
            // Enable row sorter
            table.setRowSorter(new TableRowSorter<>(model));
        }

        void refresh() {
            try {
                model.setRowCount(0);
                String f = from.getText().trim();
                String t = to.getText().trim();
                List<Map<String,String>> rows;
                
                if (!f.isEmpty() && !t.isEmpty()) {
                    rows = DB.query(
                        "SELECT l.loan_id, l.tanggal_pinjam, l.jatuh_tempo, u.nama_lengkap anggota, p.nama_lengkap petugas, l.status, " +
                        "(SELECT SUM(qty) FROM loan_items li WHERE li.loan_id=l.loan_id) total_item " +
                        "FROM loans l " +
                        "JOIN users u ON l.user_id=u.user_id " +
                        "JOIN users p ON l.petugas_id=p.user_id " +
                        "WHERE l.tanggal_pinjam BETWEEN ? AND ? " +
                        "ORDER BY l.loan_id DESC", f, t
                    );
                } else if (!f.isEmpty()) {
                    rows = DB.query(
                        "SELECT l.loan_id, l.tanggal_pinjam, l.jatuh_tempo, u.nama_lengkap anggota, p.nama_lengkap petugas, l.status, " +
                        "(SELECT SUM(qty) FROM loan_items li WHERE li.loan_id=l.loan_id) total_item " +
                        "FROM loans l " +
                        "JOIN users u ON l.user_id=u.user_id " +
                        "JOIN users p ON l.petugas_id=p.user_id " +
                        "WHERE l.tanggal_pinjam >= ? " +
                        "ORDER BY l.loan_id DESC", f
                    );
                } else if (!t.isEmpty()) {
                    rows = DB.query(
                        "SELECT l.loan_id, l.tanggal_pinjam, l.jatuh_tempo, u.nama_lengkap anggota, p.nama_lengkap petugas, l.status, " +
                        "(SELECT SUM(qty) FROM loan_items li WHERE li.loan_id=l.loan_id) total_item " +
                        "FROM loans l " +
                        "JOIN users u ON l.user_id=u.user_id " +
                        "JOIN users p ON l.petugas_id=p.user_id " +
                        "WHERE l.tanggal_pinjam <= ? " +
                        "ORDER BY l.loan_id DESC", t
                    );
                } else {
                    rows = DB.query(
                        "SELECT l.loan_id, l.tanggal_pinjam, l.jatuh_tempo, u.nama_lengkap anggota, p.nama_lengkap petugas, l.status, " +
                        "(SELECT SUM(qty) FROM loan_items li WHERE li.loan_id=l.loan_id) total_item " +
                        "FROM loans l " +
                        "JOIN users u ON l.user_id=u.user_id " +
                        "JOIN users p ON l.petugas_id=p.user_id " +
                        "ORDER BY l.loan_id DESC"
                    );
                }
                
                for (var r: rows) {
                    model.addRow(new Object[]{
                        r.get("loan_id"), r.get("tanggal_pinjam"), r.get("jatuh_tempo"),
                        r.get("anggota"), r.get("petugas"), r.get("status"), r.get("total_item")
                    });
                }
                
                int total = rows.size();
                int aktif = 0;
                int selesai = 0;
                int batal = 0;
                
                for (var r : rows) {
                    String status = r.get("status");
                    if ("AKTIF".equals(status)) aktif++;
                    else if ("SELESAI".equals(status)) selesai++;
                    else if ("BATAL".equals(status)) batal++;
                }
                
                btnRefresh.setText("🔄 " + total + " laporan (" + aktif + " aktif, " + selesai + " selesai, " + batal + " batal)");
            } catch (Exception ignored) {
                ignored.printStackTrace();
            }
        }
    }

    class SettingsPanel extends JPanel {
        private JTextField libName = Utils.input("nama perpustakaan");
        private JTextField opHours = Utils.input("operasional");
        private JTextField maxDays = Utils.input("maks hari (angka)");
        private JTextField maxBooks = Utils.input("maks buku (angka)");
        private JTextField fine = Utils.input("denda per hari (angka)");
        private JTextField maxBorrowUser = Utils.input("maks pinjam per user (angka)");
        private JButton btnSave;

        SettingsPanel() {
            setBackground(Utils.BG);
            setLayout(new BorderLayout());
            
            // Header
            JPanel header = createPanelHeader("Pengaturan Sistem", 
                "Konfigurasi aturan dan pengaturan perpustakaan");
            
            // Settings form
            JPanel form = new JPanel(new GridLayout(7, 2, 15, 15));
            form.setBackground(Utils.BG);
            form.setBorder(new EmptyBorder(20, 20, 20, 20));
            
            Utils.numericOnly(maxDays);
            Utils.numericOnly(maxBooks);
            Utils.numericOnly(fine);
            Utils.numericOnly(maxBorrowUser);
            
            // Labels with icons
            form.add(createSettingRow(" Nama Perpustakaan", libName));
            form.add(createSettingRow("Jam Operasional", opHours));
            form.add(createSettingRow("Maks Hari Pinjam", maxDays));
            form.add(createSettingRow("Maks Buku per Transaksi", maxBooks));
            form.add(createSettingRow("Maks Pinjam per User", maxBorrowUser));
            form.add(createSettingRow("Denda per Hari (Rp)", fine));
            
            // Buttons
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            buttonPanel.setBackground(Utils.BG);
            buttonPanel.setBorder(new EmptyBorder(0, 20, 20, 20));
            
            btnSave = createPrimaryButton("Simpan Pengaturan");
            JButton export = createSecondaryButton("Export CSV...");
            JButton backup = createSecondaryButton("Backup Database");
            
            btnSave.addActionListener(e -> saveSettings());
            export.addActionListener(e -> exportCSV());
            backup.addActionListener(e -> backupDatabase());
            
            buttonPanel.add(btnSave);
            buttonPanel.add(export);
            buttonPanel.add(backup);
            
            add(header, BorderLayout.NORTH);
            add(form, BorderLayout.CENTER);
            add(buttonPanel, BorderLayout.SOUTH);
            
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
            
            header.add(titlePanel, BorderLayout.WEST);
            return header;
        }
        
        private JPanel createSettingRow(String label, JTextField field) {
            JPanel row = new JPanel(new BorderLayout(10, 5));
            row.setOpaque(false);
            
            JLabel lbl = new JLabel(label);
            lbl.setForeground(Utils.TEXT);
            lbl.setFont(Utils.FONT);
            lbl.setPreferredSize(new Dimension(220, 30));
            
            row.add(lbl, BorderLayout.WEST);
            row.add(field, BorderLayout.CENTER);
            
            return row;
        }

        void refresh() {
            try {
                var s = DB.settings();
                libName.setText(s.getOrDefault("library_name","Nahsaz Library"));
                opHours.setText(s.getOrDefault("operational_hours",""));
                var r = DB.rules();
                maxDays.setText(String.valueOf(r.get("max_days")));
                maxBooks.setText(String.valueOf(r.get("max_books")));
                fine.setText(String.valueOf(r.get("fine_per_day")));
                maxBorrowUser.setText(String.valueOf(r.get("max_borrow_per_user")));
                
                btnSave.setText("Pengaturan Dimuat");
            } catch (Exception e) {
                showErrorDialog("Error", "Gagal memuat pengaturan.");
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
                    showMessageDialog("Peringatan", "Semua nilai harus bernilai positif.");
                    return;
                }
                
                if (!confirmDialog("Konfirmasi", 
                    "Apakah Anda yakin ingin menyimpan pengaturan?\n\n" +
                    "Nama Perpustakaan: " + libName.getText().trim() + "\n" +
                    "Maks Hari: " + d + " hari\n" +
                    "Maks Buku per Transaksi: " + b + " buku\n" +
                    "Maks Pinjam per User: " + mu + " buku\n" +
                    "Denda: Rp " + f + " per hari")) return;
                
                DB.exec("UPDATE settings SET setting_value=? WHERE setting_key='library_name'", libName.getText().trim());
                DB.exec("UPDATE settings SET setting_value=? WHERE setting_key='operational_hours'", opHours.getText().trim());
                DB.exec("UPDATE rules SET max_days=?, max_books=?, fine_per_day=?, max_borrow_per_user=? WHERE rule_id=1", 
                    d, b, f, mu);
                 DB.exec("UPDATE settings SET setting_value=? WHERE setting_key='library_name'", 
                    libName.getText().trim());

                DB.audit(Long.valueOf(AdminPage.this.id()), "UPDATE", "settings", "rules", "Update settings & rules");
                btnSave.setText("Pengaturan Tersimpan");
                showMessageDialog("Sukses", "Pengaturan berhasil disimpan.");
            } catch (NumberFormatException e) {
                showErrorDialog("Error", "Format angka tidak valid.");
            } catch (Exception ex) {
                ex.printStackTrace();
                showErrorDialog("Error", "Gagal menyimpan pengaturan: " + ex.getMessage());
            }
        }
        
        private void exportCSV() {
            String[] tables = {"users","books","loans","loan_items","returns","wishlist","categories","racks","audit_log"};
            String pick = (String) JOptionPane.showInputDialog(this, "Pilih tabel untuk export:", "Export CSV",
                    JOptionPane.PLAIN_MESSAGE, null, tables, tables[0]);
            if (pick == null) return;

            JFileChooser fc = new JFileChooser();
            fc.setSelectedFile(new File(pick + ".csv"));
            if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

            try {
                DB.exportTableToCSV(pick, fc.getSelectedFile());
                DB.audit(Long.valueOf(AdminPage.this.id()), "EXPORT", "csv", pick, "Export CSV");
                showMessageDialog("Sukses", "Export berhasil: " + fc.getSelectedFile().getName());
            } catch (Exception ex) {
                showErrorDialog("Error", "Export gagal: " + ex.getMessage());
            }
        }
        
        private void backupDatabase() {
            if (!confirmDialog("Backup Database", 
                "Apakah Anda yakin ingin membuat backup database?\n\n" +
                "Backup akan mengekspor semua tabel ke file SQL.")) return;
            
            JFileChooser fc = new JFileChooser();
            fc.setSelectedFile(new File("nahsaz_library_backup_" + 
                java.time.LocalDate.now().toString() + ".sql"));
            
            if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    showMessageDialog("Info", "Fitur backup database dalam pengembangan.\n" +
                        "Gunakan Export CSV untuk mengekspor data tabel.");
                } catch (Exception ex) {
                    showErrorDialog("Error", "Backup gagal.");
                }
            }
        }
    }

    class AuditPanel extends JPanel {
        DefaultTableModel model = new DefaultTableModel(new String[]{"Waktu","Actor","Action","Entity","EntityID","Detail"},0);
        JTable table = new JTable(model);
        private JButton btnRefresh;
        private JTextField dateFrom = Utils.input("YYYY-MM-DD");
        private JTextField dateTo = Utils.input("YYYY-MM-DD");
        private JTextField searchField;

        AuditPanel() {
            setBackground(Utils.BG);
            setLayout(new BorderLayout());
            
            // Header
            JPanel header = createPanelHeader("Activity Log", 
                "Riwayat aktivitas sistem");
            
            // Filter tanggal di pojok kanan atas
            JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            filterPanel.setBackground(Utils.BG);
            filterPanel.setBorder(new EmptyBorder(10, 20, 10, 20));
            
            filterPanel.add(new JLabel("Dari:"));
            
            // Validasi input tanggal
            dateFrom.addKeyListener(new java.awt.event.KeyAdapter() {
                @Override
                public void keyTyped(java.awt.event.KeyEvent e) {
                    char c = e.getKeyChar();
                    if (!((c >= '0' && c <= '9') || c == '-' || c == '\b')) {
                        e.consume();
                    }
                }
            });
            
            dateFrom.setPreferredSize(new Dimension(100, 30));
            filterPanel.add(dateFrom);
            
            filterPanel.add(new JLabel("Sampai:"));
            
            dateTo.addKeyListener(new java.awt.event.KeyAdapter() {
                @Override
                public void keyTyped(java.awt.event.KeyEvent e) {
                    char c = e.getKeyChar();
                    if (!((c >= '0' && c <= '9') || c == '-' || c == '\b')) {
                        e.consume();
                    }
                }
            });
            
            dateTo.setPreferredSize(new Dimension(100, 30));
            filterPanel.add(dateTo);
            
            JButton filterBtn = createSecondaryButton("Filter");
            JButton clearBtn = createSecondaryButton("Clear");
            
            filterBtn.addActionListener(e -> refresh());
            clearBtn.addActionListener(e -> { 
                dateFrom.setText(""); 
                dateTo.setText(""); 
                refresh(); 
            });
            
            filterPanel.add(filterBtn);
            filterPanel.add(clearBtn);
            
            // Table
            styleTable();
            JScrollPane scroll = new JScrollPane(table);
            scroll.setBorder(new EmptyBorder(0, 20, 20, 20));
            scroll.getViewport().setBackground(Utils.CARD);
            
            // Action buttons
            JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            actions.setBackground(Utils.BG);
            actions.setBorder(new EmptyBorder(0, 20, 20, 20));
            
            btnRefresh = createSecondaryButton("Refresh");
            JButton clear = createSecondaryButton("Clear Old Logs");
            
            btnRefresh.addActionListener(e -> refresh());
            clear.addActionListener(e -> clearOldLogs());
            
            actions.add(btnRefresh);
            actions.add(clear);
            
            add(header, BorderLayout.NORTH);
            add(filterPanel, BorderLayout.CENTER);
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
            searchField.putClientProperty("JTextField.placeholderText", "Cari log...");
            
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
            
            JLabel searchIcon = new JLabel("Cari");
            searchIcon.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            searchIcon.setForeground(Utils.TEXT);
            
            searchPanel.add(searchIcon);
            searchPanel.add(searchField);
            
            btnRefresh = new JButton("Refresh");
            btnRefresh.setFont(Utils.FONT);
            btnRefresh.setBackground(Utils.CARD);
            btnRefresh.setForeground(Utils.TEXT);
            btnRefresh.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Utils.BORDER),
                new EmptyBorder(6, 12, 6, 12)
            ));
            btnRefresh.setFocusPainted(false);
            btnRefresh.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnRefresh.addActionListener(e -> refresh());
            
            searchPanel.add(btnRefresh);
            
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
                    
                    // Color code actions
                    if (column == 2 && value != null) {
                        String action = value.toString();
                        switch (action) {
                            case "CREATE":
                                setForeground(new Color(52, 168, 83));
                                break;
                            case "UPDATE":
                                setForeground(new Color(66, 133, 244));
                                break;
                            case "DELETE":
                                setForeground(new Color(234, 67, 53));
                                break;
                            case "LOGIN":
                                setForeground(new Color(251, 188, 5));
                                break;
                            case "LOGOUT":
                                setForeground(new Color(154, 160, 166));
                                break;
                        }
                    }
                    
                    setBorder(noFocusBorder);
                    return c;
                }
            });
            
            // Enable row sorter
            table.setRowSorter(new TableRowSorter<>(model));
        }

        void refresh() {
            try {
                model.setRowCount(0);
                String f = dateFrom.getText().trim();
                String t = dateTo.getText().trim();
                List<Map<String,String>> rows;
                
                if (!f.isEmpty() && !t.isEmpty()) {
                    rows = DB.query(
                        "SELECT a.created_at, u.nama_lengkap actor, a.action, a.entity, a.entity_id, a.detail " +
                        "FROM audit_log a LEFT JOIN users u ON a.actor_id=u.user_id " +
                        "WHERE DATE(a.created_at) BETWEEN ? AND ? " +
                        "ORDER BY a.audit_id DESC LIMIT 500", f, t
                    );
                } else if (!f.isEmpty()) {
                    rows = DB.query(
                        "SELECT a.created_at, u.nama_lengkap actor, a.action, a.entity, a.entity_id, a.detail " +
                        "FROM audit_log a LEFT JOIN users u ON a.actor_id=u.user_id " +
                        "WHERE DATE(a.created_at) >= ? " +
                        "ORDER BY a.audit_id DESC LIMIT 500", f
                    );
                } else if (!t.isEmpty()) {
                    rows = DB.query(
                        "SELECT a.created_at, u.nama_lengkap actor, a.action, a.entity, a.entity_id, a.detail " +
                        "FROM audit_log a LEFT JOIN users u ON a.actor_id=u.user_id " +
                        "WHERE DATE(a.created_at) <= ? " +
                        "ORDER BY a.audit_id DESC LIMIT 500", t
                    );
                } else {
                    rows = DB.query(
                        "SELECT a.created_at, u.nama_lengkap actor, a.action, a.entity, a.entity_id, a.detail " +
                        "FROM audit_log a LEFT JOIN users u ON a.actor_id=u.user_id " +
                        "ORDER BY a.audit_id DESC LIMIT 500"
                    );
                }
                
                for (var r: rows) {
                    model.addRow(new Object[]{
                        r.get("created_at"),
                        r.get("actor")==null?"-":r.get("actor"),
                        r.get("action"), r.get("entity"), r.get("entity_id"), r.get("detail")
                    });
                }
                btnRefresh.setText("🔄 " + rows.size() + " log");
            } catch (Exception ignored) {
                ignored.printStackTrace();
            }
        }
        
        private void clearOldLogs() {
            if (!confirmDialog("Clear Logs", 
                "Apakah Anda yakin ingin menghapus log audit yang berumur > 30 hari?\n\n" +
                "Tindakan ini tidak dapat dibatalkan!")) return;
            
            try {
                int deleted = (int) DB.exec("DELETE FROM audit_log WHERE created_at < DATE_SUB(NOW(), INTERVAL 30 DAY)");
                DB.audit(Long.valueOf(AdminPage.this.id()), "DELETE", "audit_log", "batch", "Clear old logs (" + deleted + " records)");
                refresh();
                showMessageDialog("Sukses", "Berhasil menghapus " + deleted + " log lama.");
            } catch (Exception ex) {
                showErrorDialog("Error", "Gagal menghapus log.");
            }
        }
    }
}