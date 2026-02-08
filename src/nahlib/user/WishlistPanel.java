package nahlib.user;

import nahlib.Lang;
import nahlib.DB;
import nahlib.Utils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class WishlistPanel extends JPanel {
    private final UserPage userPage;
    private JPanel gridPanel;
    private JLabel summaryLabel;

    public WishlistPanel(UserPage userPage) {
        this.userPage = userPage;
        setBackground(Utils.BG);
        setLayout(new BorderLayout());
        
        // Header
        JPanel header = createHeader();
        
        // Grid Container - 3 columns to match the requested design
        gridPanel = new JPanel(new GridLayout(0, 3, 25, 30));
        gridPanel.setBackground(Utils.BG);
        gridPanel.setBorder(new EmptyBorder(0, 20, 20, 20));

        JScrollPane scroll = new JScrollPane(gridPanel);
        scroll.getViewport().setBackground(Utils.BG);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(24);
        
        // Footer
        JPanel footer = createFooter();
        
        add(header, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);
        
        refresh();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Utils.BG);
        header.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JLabel title = new JLabel(Lang.get("user.wishlist.title"));
        title.setForeground(Utils.TEXT);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        
        JLabel subtitle = new JLabel(Lang.get("user.wishlist.subtitle"));
        subtitle.setForeground(Utils.MUTED);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.add(title, BorderLayout.NORTH);
        titlePanel.add(subtitle, BorderLayout.SOUTH);
        
        header.add(titlePanel, BorderLayout.WEST);
        return header;
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Utils.BG);
        footer.setBorder(new EmptyBorder(10, 20, 20, 20));
        
        summaryLabel = new JLabel();
        summaryLabel.setForeground(Utils.MUTED);
        summaryLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        JButton btnRefresh = userPage.createSecondaryButton(Lang.get("btn.refresh"));
        btnRefresh.setPreferredSize(new Dimension(100, 32));
        btnRefresh.addActionListener(e -> refresh());
        
        footer.add(summaryLabel, BorderLayout.WEST);
        footer.add(btnRefresh, BorderLayout.EAST);
        return footer;
    }

    public void refresh() {
        try {
            gridPanel.removeAll();
            List<Map<String,String>> rows = DB.query(
                    "SELECT b.*, c.name kategori, r.code rak " +
                    "FROM wishlist w " +
                    "JOIN books b ON w.book_id=b.book_id " +
                    "LEFT JOIN categories c ON b.category_id=c.category_id " +
                    "LEFT JOIN racks r ON b.rack_id=r.rack_id " +
                    "WHERE w.user_id=? ORDER BY b.judul", 
                    userPage.id()
            );
            
            for (Map<String,String> r: rows) {
                gridPanel.add(createBookCard(r));
            }
            
            summaryLabel.setText(String.format(Lang.get("user.label.total_books"), rows.size()));
            gridPanel.revalidate();
            gridPanel.repaint();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private JPanel createBookCard(Map<String, String> data) {
        JPanel card = new JPanel(new BorderLayout(0, 15));
        card.setBackground(Utils.CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Utils.BORDER),
            new EmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel coverLabel = new JLabel();
        coverLabel.setHorizontalAlignment(SwingConstants.CENTER);
        coverLabel.setPreferredSize(new Dimension(200, 300));
        
        String coverPath = data.get("cover");
        if (coverPath != null && !coverPath.equals("null") && !coverPath.isEmpty()) {
            ImageIcon icon = Utils.getCover(coverPath, 200, 300);
            if (icon != null) coverLabel.setIcon(icon);
        } else {
            coverLabel.setOpaque(true);
            coverLabel.setBackground(Utils.CARD2);
            coverLabel.setText("No Image");
            coverLabel.setForeground(Utils.MUTED);
        }
        
        JPanel info = new JPanel(new GridLayout(3, 1, 0, 5));
        info.setOpaque(false);
        
        JLabel title = new JLabel("<html><b>" + data.get("judul") + "</b></html>");
        title.setForeground(Utils.TEXT);
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        
        JLabel author = new JLabel(data.get("penulis") == null ? "-" : data.get("penulis"));
        author.setForeground(Utils.MUTED);
        author.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        
        JLabel stock = new JLabel(Lang.get("books.table.available") + ": " + data.get("stok_tersedia"));
        stock.setForeground(new Color(52, 168, 83));
        stock.setFont(new Font("Segoe UI", Font.BOLD, 12));

        info.add(title);
        info.add(author);
        info.add(stock);
        
        JPanel btns = new JPanel(new GridLayout(1, 2, 10, 0));
        btns.setOpaque(false);
        
        JButton det = userPage.createSecondaryButton(Lang.get("btn.view_detail"));
        det.setFont(new Font("Segoe UI", Font.BOLD, 12));
        det.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Utils.BORDER), new EmptyBorder(8, 8, 8, 8)));
        det.addActionListener(e -> new nahlib.DetailPage(userPage, Lang.get("user.browse.title"), data));
        
        JButton remove = userPage.createPrimaryButton(Lang.get("btn.delete"));
        remove.setBackground(new Color(0xEA4335));
        remove.setFont(new Font("Segoe UI", Font.BOLD, 12));
        remove.setBorder(new EmptyBorder(8, 8, 8, 8));
        remove.addActionListener(e -> removeWish(data));
        
        btns.add(det);
        btns.add(remove);
        
        card.add(coverLabel, BorderLayout.NORTH);
        card.add(info, BorderLayout.CENTER);
        card.add(btns, BorderLayout.SOUTH);
        
        return card;
    }

    void removeWish(Map<String, String> data) {
        String bookIdStr = data.get("book_id");
        String judul = data.get("judul");
        long bookId = Long.parseLong(bookIdStr);
        
        if (!userPage.confirmDialog("Hapus Wishlist", String.format(Lang.get("user.msg.confirm_remove_wishlist"), judul))) return;
        
        try {
            DB.exec("DELETE FROM wishlist WHERE user_id=? AND book_id=?", userPage.id(), bookId);
            DB.audit(userPage.id(), "DELETE", "wishlist", String.valueOf(bookId), "Hapus wishlist: " + judul);
            userPage.showMessageDialog(Lang.get("msg.success"), String.format(Lang.get("user.msg.success_remove_wishlist"), judul));
            refresh();
        } catch (Exception ex) {
            ex.printStackTrace();
            userPage.showErrorDialog("Error", "Gagal menghapus wishlist.");
        }
    }
}
