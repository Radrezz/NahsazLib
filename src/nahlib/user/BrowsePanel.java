package nahlib.user;

import nahlib.Lang;
import nahlib.DB;
import nahlib.Utils;
import nahlib.CustomIcon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class BrowsePanel extends JPanel {
    private final UserPage userPage;
    private JPanel gridPanel;
    private JTextField search = Utils.input(Lang.get("user.browse.placeholder"));
    private JButton btnRefresh;
    private JLabel summaryLabel;
    private javax.swing.Timer searchTimer;

    public BrowsePanel(UserPage userPage) {
        this.userPage = userPage;
        setBackground(Utils.BG);
        setLayout(new BorderLayout());
        
        // Header
        JPanel header = createHeader();
        
        // Grid Container - 3 columns to match the requested design
        gridPanel = new JPanel(new GridLayout(0, 3, 25, 30)); 
        gridPanel.setBackground(Utils.BG);
        gridPanel.setBorder(new EmptyBorder(0, 20, 30, 20));

        JScrollPane scroll = new JScrollPane(gridPanel);
        scroll.getViewport().setBackground(Utils.BG);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(24);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        // Action panel
        JPanel actionPanel = createFooter();
        
        add(header, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(actionPanel, BorderLayout.SOUTH);
        
        refresh();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Utils.BG);
        header.setBorder(new EmptyBorder(15, 20, 15, 20)); // Reduced vertical padding
        
        JLabel title = new JLabel(Lang.get("user.browse.title"));
        title.setForeground(Utils.TEXT);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22)); // Slightly smaller title
        
        JLabel subtitle = new JLabel(Lang.get("user.browse.subtitle"));
        subtitle.setForeground(Utils.MUTED);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.add(title, BorderLayout.NORTH);
        titlePanel.add(subtitle, BorderLayout.SOUTH);
        
        // Search Box
        JPanel searchBox = new JPanel(new BorderLayout(10, 0));
        searchBox.setOpaque(false);
        
        JLabel lblSearch = new JLabel(Lang.get("btn.search"));
        lblSearch.setForeground(Utils.MUTED);
        lblSearch.setFont(new Font("Segoe UI", Font.BOLD, 13));
        
        search.setPreferredSize(new Dimension(280, 36)); // More compact search
        
        searchTimer = new javax.swing.Timer(500, e -> refresh());
        searchTimer.setRepeats(false);
        search.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { searchTimer.restart(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { searchTimer.restart(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { searchTimer.restart(); }
        });

        searchBox.add(lblSearch, BorderLayout.WEST);
        searchBox.add(search, BorderLayout.CENTER);
        
        header.add(titlePanel, BorderLayout.WEST);
        header.add(searchBox, BorderLayout.EAST);
        return header;
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Utils.BG);
        footer.setBorder(new EmptyBorder(8, 20, 15, 20)); // Reduced padding
        
        summaryLabel = new JLabel();
        summaryLabel.setForeground(Utils.MUTED);
        summaryLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        btnRefresh = userPage.createSecondaryButton(Lang.get("btn.refresh"));
        btnRefresh.setPreferredSize(new Dimension(100, 32));
        btnRefresh.addActionListener(e -> refresh());
        
        footer.add(summaryLabel, BorderLayout.WEST);
        footer.add(btnRefresh, BorderLayout.EAST);
        return footer;
    }

    public void refresh() {
        try {
            gridPanel.removeAll();
            String q = search.getText().trim();
            String sql =
                    "SELECT b.*, c.name kategori, r.code rak " +
                    "FROM books b " +
                    "LEFT JOIN categories c ON b.category_id=c.category_id " +
                    "LEFT JOIN racks r ON b.rack_id=r.rack_id ";
            
            List<Map<String,String>> rows;
            if (q.isEmpty()) {
                rows = DB.query(sql + " ORDER BY b.judul LIMIT 12"); // Limit to 2 rows to avoid initial scroll
            } else {
                rows = DB.query(sql + " WHERE b.judul LIKE ? OR b.code LIKE ? OR b.penulis LIKE ? OR b.penerbit LIKE ? ORDER BY b.judul LIMIT 24", 
                    "%"+q+"%", "%"+q+"%", "%"+q+"%", "%"+q+"%");
            }
            
            for (Map<String,String> r: rows) {
                gridPanel.add(createBookCard(r));
            }
            
            // Add filler cards to keep layout consistent if few items
            int count = rows.size();
            summaryLabel.setText(String.format(Lang.get("user.label.total_books"), count));
            
            gridPanel.revalidate();
            gridPanel.repaint();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private JPanel createBookCard(Map<String,String> data) {
        JPanel card = new JPanel(new BorderLayout(0, 15));
        card.setBackground(Utils.CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Utils.BORDER),
            new EmptyBorder(15, 15, 15, 15)
        ));
        
        // Thumbnail - 4:6 Ratio (200x300) for a more prominent look
        JLabel coverLabel = new JLabel();
        coverLabel.setHorizontalAlignment(SwingConstants.CENTER);
        coverLabel.setPreferredSize(new Dimension(200, 300));
        coverLabel.setOpaque(true);
        coverLabel.setBackground(Utils.CARD2);
        
        String coverPath = data.get("cover");
        if (coverPath != null && !coverPath.equals("null") && !coverPath.isEmpty()) {
            ImageIcon icon = Utils.getCover(coverPath, 200, 300);
            if (icon != null) coverLabel.setIcon(icon);
        } else {
            coverLabel.setText("No Image");
            coverLabel.setForeground(Utils.MUTED);
        }
        
        // Info
        JPanel info = new JPanel(new GridLayout(3, 1, 0, 5));
        info.setOpaque(false);
        
        JLabel title = new JLabel("<html><b>" + data.get("judul") + "</b></html>");
        title.setForeground(Utils.TEXT);
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        
        JLabel author = new JLabel(data.get("penulis"));
        author.setForeground(Utils.MUTED);
        author.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        
        JLabel stock = new JLabel(Lang.get("books.table.available") + ": " + data.get("stok_tersedia"));
        stock.setForeground(new Color(52, 168, 83));
        stock.setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        info.add(title);
        info.add(author);
        info.add(stock);
        
        // Actions
        JPanel btns = new JPanel(new GridLayout(1, 2, 10, 0));
        btns.setOpaque(false);
        
        JButton det = userPage.createSecondaryButton(Lang.get("btn.view_detail"));
        det.setFont(new Font("Segoe UI", Font.BOLD, 12));
        det.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Utils.BORDER), new EmptyBorder(8, 8, 8, 8)));
        det.addActionListener(e -> new nahlib.DetailPage(userPage, Lang.get("user.browse.title"), data));
        
        JButton wish = userPage.createPrimaryButton("+ " + Lang.get("nav.wishlist"));
        wish.setFont(new Font("Segoe UI", Font.BOLD, 12));
        wish.setBorder(new EmptyBorder(8, 8, 8, 8));
        wish.addActionListener(e -> addToWishlist(data));
        
        btns.add(det);
        btns.add(wish);
        
        card.add(coverLabel, BorderLayout.NORTH);
        card.add(info, BorderLayout.CENTER);
        card.add(btns, BorderLayout.SOUTH);
        
        return card;
    }

    void addToWishlist(Map<String, String> data) {
        String bookIdStr = data.get("book_id");
        String judul = data.get("judul");
        long bookId = Long.parseLong(bookIdStr);
        
        try {
            List<Map<String,String>> existing = DB.query("SELECT COUNT(*) as c FROM wishlist WHERE user_id=? AND book_id=?", userPage.id(), bookId);
            if (Integer.parseInt(existing.get(0).get("c")) > 0) {
                userPage.showMessageDialog("Info", String.format(Lang.get("user.msg.already_wishlist"), judul));
                return;
            }
            
            if (!userPage.confirmDialog("Wishlist", String.format(Lang.get("user.msg.confirm_wishlist"), judul))) return;
            
            DB.exec("INSERT INTO wishlist(user_id,book_id) VALUES (?,?)", userPage.id(), bookId);
            DB.audit(userPage.id(), "CREATE", "wishlist", String.valueOf(bookId), "Tambah wishlist: " + judul);
            userPage.showMessageDialog(Lang.get("msg.success"), String.format(Lang.get("user.msg.success_wishlist"), judul));
        } catch (Exception ex) {
            ex.printStackTrace();
            userPage.showErrorDialog("Error", "Gagal menambahkan ke wishlist.");
        }
    }
}
