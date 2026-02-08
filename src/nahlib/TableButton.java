package nahlib;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class TableButton extends AbstractCellEditor implements TableCellRenderer, TableCellEditor {
    private final JPanel panel;
    private final List<JButton> buttons = new ArrayList<>();
    private final List<Consumer<Integer>> actions = new ArrayList<>();
    private int currentRow;

    public TableButton() {
        this.panel = new JPanel(new GridBagLayout());
        this.panel.setOpaque(false);
    }

    public TableButton(String text, Consumer<Integer> action) {
        this();
        addButton(text, Utils.ACCENT, action);
    }

    public void addButton(String text, Color bg, Consumer<Integer> action) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btn.addActionListener(e -> {
            stopCellEditing();
            action.accept(currentRow);
        });
        
        buttons.add(btn);
        actions.add(action);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = buttons.size() - 1;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 3, 0, 3);
        panel.add(btn, gbc);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        panel.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
        return panel;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        this.currentRow = row;
        panel.setBackground(table.getSelectionBackground());
        return panel;
    }

    @Override
    public Object getCellEditorValue() {
        return null;
    }

    public void install(JTable table, int column) {
        TableColumn col = table.getColumnModel().getColumn(column);
        col.setCellRenderer(this);
        col.setCellEditor(this);
        col.setPreferredWidth(180);
        col.setMinWidth(150);
    }
}
