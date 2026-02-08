package nahlib;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Map;

public class DetailPage extends JFrame {
    private final Map<String, String> data;
    private final String title;
    private final JFrame parentFrame;

    public DetailPage(JFrame parentFrame, String title, Map<String, String> data) {
        this.parentFrame = parentFrame;
        this.title = title;
        this.data = data;

        setUndecorated(true);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel root = Utils.createRootPanel(new BorderLayout());
        
        // Header
        JPanel header = createHeader();
        root.add(header, BorderLayout.NORTH);

        // Content
        JPanel contentScrollWrapper = new JPanel(new BorderLayout());
        contentScrollWrapper.setOpaque(false);
        contentScrollWrapper.setBorder(new EmptyBorder(20, 60, 20, 60));

        JPanel content = createContentPanel();
        contentScrollWrapper.add(content, BorderLayout.CENTER);

        JScrollPane scroll = new JScrollPane(contentScrollWrapper);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        root.add(scroll, BorderLayout.CENTER);

        setContentPane(root);
        setVisible(true);
        if (parentFrame != null) parentFrame.setVisible(false);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, Utils.BORDER),
            new EmptyBorder(25, 40, 25, 40)
        ));

        // Left side: Back Button + Title
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        left.setOpaque(false);

        JButton backBtn = new JButton(new CustomIcon(CustomIcon.Type.BACK, 20, Color.WHITE)); 
        backBtn.setBackground(Utils.CARD);
        backBtn.setForeground(Utils.TEXT);
        backBtn.setFocusPainted(false);
        backBtn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Utils.BORDER),
            new EmptyBorder(10, 15, 10, 15)
        ));
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.setText(Lang.get("btn.close"));
        backBtn.setFont(Utils.FONT_B);
        backBtn.addActionListener(e -> {
            dispose();
            if (parentFrame != null) parentFrame.setVisible(true);
        });

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));

        left.add(backBtn);
        left.add(titleLabel);

        header.add(left, BorderLayout.WEST);

        return header;
    }

    private JPanel createContentPanel() {
        JPanel centerWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        centerWrapper.setOpaque(false);
        
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        p.setPreferredSize(new Dimension(1000, 1000));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;

        // Smart Bento Logic: Track occupied cells
        int maxRowsEstimate = data.size() + 5;
        boolean[][] occupied = new boolean[maxRowsEstimate][2];
        int currentRow = 0;
        int currentCol = 0;

        // Anchor to top
        gbc.anchor = GridBagConstraints.NORTH;
        
        // Check for Cover first
        if (data.containsKey("cover")) {
            String path = data.get("cover");
            if (path != null && !path.isEmpty() && !path.equals("null")) {
                JPanel coverBox = new JPanel();
                coverBox.setLayout(new BoxLayout(coverBox, BoxLayout.Y_AXIS)); 
                coverBox.setOpaque(false);
                coverBox.setBorder(new EmptyBorder(0, 0, 0, 20)); // Spacing between image and text

                String[] covers = path.split("\\|");
                
                for (String cPath : covers) {
                    if (cPath.startsWith("placeholder_")) {
                         // custom icon logic - IGNORE
                    } else {
                        // Use a more compact size that fits comfortably in ~4 rows (approx 400px height)
                        ImageIcon icon = Utils.getCover(cPath, 180, 270); 
                         if (icon != null) {
                            JLabel imgLabel = new JLabel(icon);
                            imgLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                            imgLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
                            imgLabel.addMouseListener(new java.awt.event.MouseAdapter() {
                                public void mouseClicked(java.awt.event.MouseEvent e) { showZoomedImage(cPath, data.get("judul")); }
                            });
                            
                            // Add some spacing if multiple images (though rare for current use case)
                            if (coverBox.getComponentCount() > 0) coverBox.add(Box.createVerticalStrut(10));
                            coverBox.add(imgLabel);
                        }
                    }
                }
                
                // Only add if we actually added images
                if (coverBox.getComponentCount() > 0) {
                    gbc.gridx = 0;
                    gbc.gridy = 0;
                    gbc.gridwidth = 1; 
                    gbc.gridheight = 4; // Spans ~4 data rows
                    gbc.fill = GridBagConstraints.VERTICAL; // Fill vert to align top
                    p.add(coverBox, gbc);

                    // Mark cells as occupied
                    occupied[0][0] = true;
                    occupied[1][0] = true;
                    occupied[2][0] = true;
                    occupied[3][0] = true; 
                    
                    // Reset fill for other components
                    gbc.fill = GridBagConstraints.BOTH;
                }
            }
        }

        for (Map.Entry<String, String> entry : data.entrySet()) {
            String rawKey = entry.getKey().toLowerCase();
            if (rawKey.contains("id") || rawKey.equals("cover") || rawKey.equals("coverbinary")) continue;
            
            String key = entry.getKey().replace("_", " ").toUpperCase();
            String value = entry.getValue();
            if (value == null || value.equals("null")) value = "-";
            
            boolean isLong = value.length() > 40 || key.contains("DETAIL") || key.contains("DAFTAR") || key.contains("LIST");

            // Find next available slot
            // Find next available slot
            while (currentRow < maxRowsEstimate) {
                if (currentCol == 0) {
                    if (occupied[currentRow][0]) {
                        // Left blocked, try right
                        currentCol = 1; 
                    } else {
                        // Left free
                        if (isLong && occupied[currentRow][1]) {
                            // Long item needs both cols, but right is blocked. Next row.
                            currentRow++;
                            currentCol = 0;
                        } else {
                            // Can place here (either short or long spanning both)
                            break;
                        }
                    }
                } else {
                    // We are at col 1
                    if (occupied[currentRow][1]) {
                        // Right blocked. Next row.
                        currentRow++;
                        currentCol = 0;
                    } else {
                        // Right free. Place here (width forced to 1)
                        break;
                    }
                }
            }

            if (currentRow >= maxRowsEstimate) break;

            JPanel box = new JPanel(new BorderLayout(0, 5));
            box.setBackground(Utils.CARD);
            box.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Utils.BORDER),
                new EmptyBorder(10, 15, 10, 15)
            ));
            
            JLabel lbl = new JLabel(key);
            lbl.setForeground(Utils.MUTED);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
            
            JLabel val = new JLabel("<html><body style='width: " + (isLong ? "600px" : "250px") + "'>" + value + "</body></html>");
            val.setForeground(Color.WHITE);
            val.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            
            box.add(lbl, BorderLayout.NORTH);
            box.add(val, BorderLayout.CENTER);

            gbc.gridx = currentCol;
            gbc.gridy = currentRow;
            gbc.gridwidth = isLong ? 2 : 1;
            gbc.gridheight = 1;
            
            // Safety check: if we are placing a wide item, ensure it doesn't overlap
            if (isLong && occupied[currentRow][0]) {
                 // Optimization: if we are blocked by image on the left, we can't place a wide item.
                 // We might need to force it to column 1 (but width 1) or wait until image ends.
                 // Simple fix: Force width 1 if on the right side of image
                 gbc.gridwidth = 1;
                 val.setText("<html><body style='width: 250px'>" + value + "</body></html>");
            }

            p.add(box, gbc);
            
            occupied[currentRow][currentCol] = true;
            if (gbc.gridwidth == 2) {
                occupied[currentRow][1] = true;
                currentRow++;
                currentCol = 0;
            } else {
                currentCol++;
                if (currentCol >= 2) { currentCol = 0; currentRow++; }
            }
        }

        // Add bottom filler to push content up
        GridBagConstraints fillerGbc = new GridBagConstraints();
        fillerGbc.gridx = 0;
        fillerGbc.gridy = currentRow + 1;
        fillerGbc.gridwidth = 2;
        fillerGbc.weighty = 1.0; 
        fillerGbc.fill = GridBagConstraints.VERTICAL;
        p.add(Box.createVerticalGlue(), fillerGbc);

        centerWrapper.add(p);
        return centerWrapper;
    }
    private void showZoomedImage(String path, String judul) {
        JDialog zoom = new JDialog(this, true);
        zoom.setTitle(judul);
        zoom.setUndecorated(true);
        zoom.setBackground(new Color(0, 0, 0, 180));
        
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Utils.BG);
        p.setBorder(BorderFactory.createLineBorder(Utils.BORDER, 2));
        
        ImageIcon icon = Utils.getCover(path, 400, 600);
        JLabel img = new JLabel(icon);
        img.setHorizontalAlignment(SwingConstants.CENTER);
        
        JButton close = new JButton("Close");
        close.setBackground(Utils.CARD2);
        close.setForeground(Utils.TEXT);
        close.addActionListener(e -> zoom.dispose());
        
        p.add(img, BorderLayout.CENTER);
        p.add(close, BorderLayout.SOUTH);
        
        zoom.add(p);
        zoom.pack();
        zoom.setLocationRelativeTo(this);
        zoom.setVisible(true);
    }
}
