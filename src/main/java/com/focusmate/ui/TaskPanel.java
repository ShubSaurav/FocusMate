package com.focusmate.ui;

import com.focusmate.dao.TaskDAO;
import com.focusmate.model.Task;
import com.focusmate.service.Scheduler;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class TaskPanel extends JPanel {
    // Blue theme colors
    private static final Color PRIMARY_BLUE = new Color(33, 150, 243);
    private static final Color DARK_BLUE = new Color(25, 118, 210);
    private static final Color LIGHT_BLUE = new Color(144, 202, 249);
    private static final Color BACKGROUND = new Color(240, 248, 255);
    private static final Color CARD_BG = Color.WHITE;
    
    private final TaskDAO dao;
    private final Scheduler scheduler;
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID", "Title", "Priority", "Due", "Target (min)", "Status"}, 0);
    private final JTable table = new JTable(model);
    private final JTextField title = new JTextField(18);
    private final JSpinner priority = new JSpinner(new SpinnerNumberModel(3, 1, 5, 1));
    private final JTextField due = new JTextField(10);
    private final JSpinner target = new JSpinner(new SpinnerNumberModel(60, 0, 10000, 15));
    private final JComboBox<String> status = new JComboBox<>(new String[]{"PENDING", "IN_PROGRESS", "DONE"});

    public TaskPanel(TaskDAO dao, Scheduler scheduler) {
        this.dao = dao;
        this.scheduler = scheduler;
        
        setLayout(new BorderLayout(10, 10));
        setBackground(BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Style table
        table.setFont(new Font("SansSerif", Font.PLAIN, 12));
        table.setRowHeight(30);
        table.setGridColor(LIGHT_BLUE);
        table.setSelectionBackground(LIGHT_BLUE);
        table.setSelectionForeground(DARK_BLUE);
        table.setShowVerticalLines(true);
        table.setShowHorizontalLines(true);
        table.setIntercellSpacing(new Dimension(1, 1));
        
        // Add hover effect to table rows
        table.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            private int lastRow = -1;
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                int row = table.rowAtPoint(evt.getPoint());
                if (row != lastRow) {
                    lastRow = row;
                    table.repaint();
                }
            }
        });
        
        // Custom cell renderer for hover effect
        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            public java.awt.Component getTableCellRendererComponent(javax.swing.JTable table, 
                    Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                java.awt.Component c = super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);
                
                if (!isSelected) {
                    java.awt.Point p = table.getMousePosition();
                    if (p != null && table.rowAtPoint(p) == row) {
                        c.setBackground(new Color(227, 242, 253)); // Light blue hover
                        c.setForeground(DARK_BLUE);
                    } else if (row % 2 == 0) {
                        c.setBackground(Color.WHITE);
                        c.setForeground(Color.BLACK);
                    } else {
                        c.setBackground(new Color(250, 250, 250)); // Alternate row
                        c.setForeground(Color.BLACK);
                    }
                } else {
                    c.setBackground(LIGHT_BLUE);
                    c.setForeground(DARK_BLUE);
                }
                
                setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                return c;
            }
        });
        
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("SansSerif", Font.BOLD, 13));
        header.setBackground(PRIMARY_BLUE);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(header.getWidth(), 35));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, DARK_BLUE));
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(LIGHT_BLUE, 2));
        add(scrollPane, BorderLayout.CENTER);

        // Form panel with modern styling
        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        form.setBackground(CARD_BG);
        form.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LIGHT_BLUE, 2, true),
                BorderFactory.createMatteBorder(2, 2, 5, 2, new Color(200, 200, 200, 100))
            ),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        // Add form fields with emoji labels
        addLabeledField(form, "✏️ Title:", title);
        addLabeledField(form, "⭐ Priority:", priority);
        addLabeledField(form, "📅 Due (yyyy-mm-dd):", due);
        addLabeledField(form, "🎯 Target:", target);
        addLabeledField(form, "📊 Status:", status);

        // Style buttons
        JButton addBtn = new JButton("➕ Add Task");
        JButton refreshBtn = new JButton("🔄 Refresh");
        JButton arrangeBtn = new JButton("🤖 Auto-Arrange");
        
        styleButton(addBtn, PRIMARY_BLUE, DARK_BLUE);
        styleButton(refreshBtn, new Color(76, 175, 80), new Color(56, 142, 60));
        styleButton(arrangeBtn, new Color(156, 39, 176), new Color(123, 31, 162));
        
        form.add(addBtn); 
        form.add(refreshBtn); 
        form.add(arrangeBtn);
        add(form, BorderLayout.NORTH);

        addBtn.addActionListener(e -> save());
        refreshBtn.addActionListener(e -> load(false));
        arrangeBtn.addActionListener(e -> load(true));

        load(false);
    }
    
    private void addLabeledField(JPanel panel, String labelText, JComponent field) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("SansSerif", Font.BOLD, 13));
        label.setForeground(PRIMARY_BLUE);
        panel.add(label);
        
        if (field instanceof JTextField) {
            field.setFont(new Font("SansSerif", Font.PLAIN, 12));
        }
        panel.add(field);
    }
    
    private void styleButton(JButton btn, Color normalColor, Color hoverColor) {
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(normalColor);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(normalColor.darker(), 1, true),
            BorderFactory.createEmptyBorder(8, 16, 8, 16)
        ));
        
        // Advanced hover effect with shadow and scale
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(hoverColor);
                btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(hoverColor.darker().darker(), 2, true),
                    BorderFactory.createEmptyBorder(7, 15, 7, 15)
                ));
                Font currentFont = btn.getFont();
                btn.setFont(currentFont.deriveFont(14f));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(normalColor);
                btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(normalColor.darker(), 1, true),
                    BorderFactory.createEmptyBorder(8, 16, 8, 16)
                ));
                btn.setFont(new Font("SansSerif", Font.BOLD, 13));
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                btn.setBackground(hoverColor.darker());
                btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(hoverColor.darker().darker(), 2, false),
                    BorderFactory.createEmptyBorder(9, 17, 6, 14)
                ));
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                if (btn.contains(evt.getPoint())) {
                    btn.setBackground(hoverColor);
                    btn.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(hoverColor.darker().darker(), 2, true),
                        BorderFactory.createEmptyBorder(7, 15, 7, 15)
                    ));
                }
            }
        });
    }

    private void save() {
        try {
            Task t = new Task();
            t.title = title.getText().trim();
            if (t.title.isEmpty()) throw new IllegalArgumentException("Title required");
            t.priority = (Integer) priority.getValue();
            t.dueDate = due.getText().trim().isEmpty() ? null : LocalDate.parse(due.getText().trim());
            t.targetMinutes = (Integer) target.getValue();
            t.status = (String) status.getSelectedItem();
            dao.insert(t);
            title.setText(""); due.setText("");
            load(false);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Save error: " + ex.getMessage());
        }
    }

    private void load(boolean smartOrder) {
        model.setRowCount(0);
        try {
            List<Task> tasks = smartOrder ? scheduler.sorted() : dao.listAll();
            for (Task t : tasks) {
                model.addRow(new Object[]{
                        t.id, t.title, t.priority,
                        t.dueDate == null ? "" : t.dueDate.toString(),
                        t.targetMinutes, t.status
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Load error: " + ex.getMessage());
        }
    }
}