package nahlib;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;

public class SimpleChart extends JPanel {
    public enum Type { BAR, LINE, PIE }
    
    private Type type;
    private List<Double> values = new ArrayList<>();
    private List<String> labels = new ArrayList<>();
    private Color color;

    public SimpleChart(Type type, Color color) {
        this.type = type;
        this.color = color;
        setBackground(Utils.CARD);
        setOpaque(true);
    }

    public void setData(List<String> labels, List<Double> values) {
        this.labels = labels;
        this.values = values;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (values == null || values.isEmpty()) return;

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        int padding = 30;

        double max = 0;
        for (double v : values) if (v > max) max = v;
        if (max == 0) max = 1;

        if (type == Type.BAR) {
            int barWidth = (w - 2 * padding) / values.size() - 10;
            for (int i = 0; i < values.size(); i++) {
                int barHeight = (int) ((h - 2 * padding) * (values.get(i) / max));
                int x = padding + i * (barWidth + 10);
                int y = h - padding - barHeight;

                g2.setColor(color);
                g2.fillRect(x, y, barWidth, barHeight);
                
                g2.setColor(Utils.TEXT);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                if (i < labels.size()) {
                    g2.drawString(labels.get(i), x, h - padding + 15);
                }
                g2.drawString(String.valueOf(values.get(i).intValue()), x, y - 5);
            }
        } else if (type == Type.LINE) {
            int stepX = (w - 2 * padding) / (values.size() - 1);
            int[] px = new int[values.size()];
            int[] py = new int[values.size()];

            for (int i = 0; i < values.size(); i++) {
                px[i] = padding + i * stepX;
                py[i] = h - padding - (int) ((h - 2 * padding) * (values.get(i) / max));
                
                g2.setColor(Utils.TEXT);
                if (i < labels.size()) {
                    g2.drawString(labels.get(i), px[i] - 10, h - padding + 15);
                }
            }

            g2.setColor(color);
            g2.setStroke(new BasicStroke(2.5f));
            g2.drawPolyline(px, py, values.size());
            
            for (int i = 0; i < values.size(); i++) {
                g2.fillOval(px[i] - 4, py[i] - 4, 8, 8);
            }
        }
    }
}
