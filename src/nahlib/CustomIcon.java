package nahlib;

import javax.swing.*;
import java.awt.*;

public class CustomIcon implements Icon {
    public enum Type {
        DASHBOARD, USERS, BOOKS, REPORTS, SETTINGS, LOGOUT, AUDIT, HOME, SEARCH, STAFF, GLOBE
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
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.translate(x, y);
        g2.setColor(color);
        g2.setStroke(new BasicStroke(1.5f));

        switch (type) {
            case DASHBOARD:
                g2.drawRect(2, 2, 7, 7);
                g2.drawRect(11, 2, 7, 7);
                g2.drawRect(2, 11, 7, 7);
                g2.drawRect(11, 11, 7, 7);
                break;
            case USERS:
                // Simple member icon
                g2.drawOval(6, 2, 8, 8);
                g2.drawArc(2, 12, 16, 8, 0, 180);
                break;
            case STAFF:
                // Staff icon (person with a tie/badge effect)
                g2.drawOval(6, 2, 8, 8);
                g2.drawArc(2, 12, 16, 8, 0, 180);
                // Draw a small tie
                g2.drawLine(10, 12, 10, 15);
                g2.drawLine(10, 15, 8, 17);
                g2.drawLine(10, 15, 12, 17);
                break;
            case BOOKS:
                g2.drawRect(4, 2, 12, 16);
                g2.drawLine(4, 6, 16, 6);
                g2.drawLine(4, 10, 16, 10);
                break;
            case REPORTS:
                g2.drawRect(3, 2, 14, 16);
                g2.drawLine(6, 6, 14, 6);
                g2.drawLine(6, 10, 14, 10);
                g2.drawLine(6, 14, 14, 14);
                break;
            case SETTINGS:
                g2.drawOval(6, 6, 8, 8);
                for(int i=0; i<8; i++) {
                    double angle = Math.toRadians(i * 45);
                    int x1 = (int)(10 + Math.cos(angle) * 6);
                    int y1 = (int)(10 + Math.sin(angle) * 6);
                    int x2 = (int)(10 + Math.cos(angle) * 9);
                    int y2 = (int)(10 + Math.sin(angle) * 9);
                    g2.drawLine(x1, y1, x2, y2);
                }
                break;
            case AUDIT:
                g2.drawOval(2, 2, 16, 16);
                g2.drawLine(10, 5, 10, 10);
                g2.drawLine(10, 10, 14, 10);
                break;
            case LOGOUT:
                g2.drawArc(2, 2, 16, 16, 45, 270);
                g2.drawLine(10, 10, 18, 10);
                g2.drawLine(15, 7, 18, 10);
                g2.drawLine(15, 13, 18, 10);
                break;
            case HOME:
                int[] px = {2, 10, 18, 18, 14, 14, 6, 6, 2};
                int[] py = {10, 2, 10, 18, 18, 12, 12, 18, 18};
                g2.drawPolygon(px, py, 9);
                break;
            case SEARCH:
                g2.drawOval(2, 2, 12, 12);
                g2.drawLine(13, 13, 18, 18);
                break;
            case GLOBE:
                g2.drawOval(2, 2, 16, 16);
                g2.drawOval(6, 2, 8, 16);
                g2.drawLine(2, 10, 18, 10);
                g2.drawLine(4, 5, 16, 5);
                g2.drawLine(4, 15, 16, 15);
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
