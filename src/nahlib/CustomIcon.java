package nahlib;

import javax.swing.*;
import java.awt.*;

public class CustomIcon implements Icon {
    public enum Type {
        DASHBOARD, USERS, BOOKS, REPORTS, SETTINGS, LOGOUT, AUDIT, HOME, SEARCH, STAFF, GLOBE, BACK
    }

    private final Type type;
    private final int size;
    private final Color color;

    public CustomIcon(Type type, int size, Color color) {
        this.type = type;
        this.size = size;
        this.color = color;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        
        // Enhanced rendering hints for smoother icons
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        
        g2.translate(x, y);
        g2.setColor(color);
        g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        switch (type) {
            case DASHBOARD:
                g2.drawRoundRect(2, 2, 7, 7, 2, 2);
                g2.drawRoundRect(11, 2, 7, 7, 2, 2);
                g2.drawRoundRect(2, 11, 7, 7, 2, 2);
                g2.drawRoundRect(11, 11, 7, 7, 2, 2);
                break;
                
            case USERS:
                // Improved member icon with smoother curves
                g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawOval(6, 2, 8, 8);
                g2.drawArc(2, 11, 16, 10, 0, 180);
                break;
                
            case STAFF:
                // Improved staff icon with ID badge
                g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                // Head
                g2.drawOval(6, 2, 8, 8);
                // Body
                g2.drawArc(2, 11, 16, 10, 0, 180);
                // ID Badge
                g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawRoundRect(7, 12, 6, 4, 2, 2);
                g2.drawLine(8, 14, 12, 14);
                break;
                
            case BOOKS:
                // Horizontal book stack (wider)
                g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                // Book 1
                g2.drawRoundRect(2, 4, 6, 12, 2, 2);
                g2.drawLine(2, 8, 8, 8);
                // Book 2
                g2.drawRoundRect(7, 4, 6, 12, 2, 2);
                g2.drawLine(7, 8, 13, 8);
                // Book 3
                g2.drawRoundRect(12, 4, 6, 12, 2, 2);
                g2.drawLine(12, 8, 18, 8);
                break;
                
            case REPORTS:
                g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawRoundRect(3, 2, 14, 16, 2, 2);
                g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(6, 6, 14, 6);
                g2.drawLine(6, 10, 14, 10);
                g2.drawLine(6, 14, 14, 14);
                break;
                
            case SETTINGS:
                // Improved gear icon
                g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                // Center circle
                g2.drawOval(7, 7, 6, 6);
                // Gear teeth
                g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                for(int i=0; i<8; i++) {
                    double angle = Math.toRadians(i * 45);
                    int x1 = (int)(10 + Math.cos(angle) * 5);
                    int y1 = (int)(10 + Math.sin(angle) * 5);
                    int x2 = (int)(10 + Math.cos(angle) * 9);
                    int y2 = (int)(10 + Math.sin(angle) * 9);
                    g2.drawLine(x1, y1, x2, y2);
                }
                break;
                
            case AUDIT:
                // Improved activity log icon (clipboard with checkmark)
                g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                // Clipboard
                g2.drawRoundRect(4, 2, 12, 16, 2, 2);
                // Clip
                g2.drawRoundRect(7, 1, 6, 3, 2, 2);
                // Checkmark
                g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(7, 10, 9, 13);
                g2.drawLine(9, 13, 13, 8);
                break;
                
            case LOGOUT:
                g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawArc(2, 2, 16, 16, 45, 270);
                g2.drawLine(10, 10, 18, 10);
                g2.drawLine(15, 7, 18, 10);
                g2.drawLine(15, 13, 18, 10);
                break;
                
            case HOME:
                g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int[] px = {2, 10, 18, 18, 14, 14, 6, 6, 2};
                int[] py = {10, 2, 10, 18, 18, 12, 12, 18, 18};
                g2.drawPolygon(px, py, 9);
                break;
                
            case SEARCH:
                g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawOval(2, 2, 12, 12);
                g2.drawLine(12, 12, 18, 18);
                break;
                
            case GLOBE:
                g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawOval(2, 2, 16, 16);
                g2.drawOval(6, 2, 8, 16);
                g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(2, 10, 18, 10);
                g2.drawLine(4, 5, 16, 5);
                g2.drawLine(4, 15, 16, 15);
                break;
                
            case BACK:
                g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(16, 10, 4, 10);
                g2.drawLine(10, 4, 4, 10);
                g2.drawLine(10, 16, 4, 10);
                break;
        }

        g2.dispose();
    }

    @Override
    public int getIconWidth() {
        return size;
    }

    @Override
    public int getIconHeight() {
        return size;
    }
}
