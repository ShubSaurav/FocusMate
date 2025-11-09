package com.focusmate.ui;

import com.focusmate.dao.TaskDAO;
import com.focusmate.db.DB;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class AnalyticsPanel extends JPanel {
    // Blue theme colors
    private static final Color PRIMARY_BLUE = new Color(33, 150, 243);
    private static final Color DARK_BLUE = new Color(25, 118, 210);
    private static final Color LIGHT_BLUE = new Color(144, 202, 249);
    private static final Color BACKGROUND = new Color(240, 248, 255);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color SUCCESS_GREEN = new Color(76, 175, 80);
    
    private final TaskDAO taskDAO;
    private final JComboBox<Item> taskBox = new JComboBox<>();
    private final ChartPanel chart = new ChartPanel();

    public AnalyticsPanel(TaskDAO taskDAO) {
        this.taskDAO = taskDAO;
        
        setLayout(new BorderLayout(10, 10));
        setBackground(BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Top control panel
        JPanel top = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        top.setBackground(CARD_BG);
        top.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LIGHT_BLUE, 2, true),
                BorderFactory.createMatteBorder(2, 2, 5, 2, new Color(200, 200, 200, 100))
            ),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel taskLabel = new JLabel("📊 Select Task:");
        taskLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        taskLabel.setForeground(PRIMARY_BLUE);
        top.add(taskLabel);
        
        taskBox.setFont(new Font("SansSerif", Font.PLAIN, 12));
        top.add(taskBox);
        
        JButton refresh = new JButton("📈 Show Graph");
        styleButton(refresh, PRIMARY_BLUE, DARK_BLUE);
        top.add(refresh);
        
        add(top, BorderLayout.NORTH);
        
        // Chart panel with border
        chart.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LIGHT_BLUE, 2, true),
                BorderFactory.createMatteBorder(2, 2, 5, 2, new Color(200, 200, 200, 100))
            ),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        add(chart, BorderLayout.CENTER);

        refresh.addActionListener(e -> reloadChart());
        loadTasks();
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
        
        // Advanced hover effect
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

    private void loadTasks() {
        try {
            taskBox.removeAllItems();
            for (var t : taskDAO.listAll()) {
                taskBox.addItem(new Item(t.id, t.title, t.targetMinutes));
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Task load failed: " + ex.getMessage());
        }
    }

    private void reloadChart() {
        Item it = (Item) taskBox.getSelectedItem();
        if (it == null) return;
        int actual = 0;
        try (Connection c = DB.get();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT COALESCE(SUM(actual_minutes),0) FROM sessions WHERE task_id=?")) {
            ps.setInt(1, it.id);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                actual = rs.getInt(1);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Query failed: " + ex.getMessage());
        }
        Map<String, Integer> data = new LinkedHashMap<>();
        data.put("Target", it.target);
        data.put("Actual", actual);
        chart.setData(data);
    }

    private static class Item {
        int id; String title; int target;
        Item(int id, String title, int target) { this.id = id; this.title = title; this.target = target; }
        public String toString() { return title; }
    }

    private static class ChartPanel extends JPanel {
        private Map<String, Integer> data = Map.of("Target", 0, "Actual", 0);
        
        public ChartPanel() {
            setBackground(Color.WHITE);
        }
        
        public void setData(Map<String, Integer> d) { 
            this.data = d; 
            repaint(); 
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            int w = getWidth(), h = getHeight();
            int padding = 60;
            int max = 1;
            for (int v : data.values()) max = Math.max(max, v);

            int barWidth = (w - 2 * padding) / data.size();
            int i = 0;
            
            // Modern gradient bars
            for (var e : data.entrySet()) {
                int val = e.getValue();
                int barHeight = (int) ((h - 2 * padding) * (val / (double) max));
                int bx = padding + i * barWidth + 20;
                int by = h - padding - barHeight;
                int bw = barWidth - 40;
                
                // Choose color based on bar type
                Color barColor = i == 0 ? PRIMARY_BLUE : SUCCESS_GREEN;
                Color darkColor = i == 0 ? DARK_BLUE : new Color(56, 142, 60);
                
                // Draw gradient bar
                GradientPaint gradient = new GradientPaint(
                    bx, by, barColor, 
                    bx, by + barHeight, darkColor
                );
                g2.setPaint(gradient);
                g2.fillRoundRect(bx, by, bw, barHeight, 10, 10);
                
                // Draw border
                g2.setColor(darkColor);
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(bx, by, bw, barHeight, 10, 10);
                
                // Draw label with emoji
                g2.setFont(new Font("SansSerif", Font.BOLD, 14));
                g2.setColor(new Color(33, 33, 33));
                String emoji = i == 0 ? "🎯" : "✅";
                String label = emoji + " " + e.getKey() + ": " + val + " min";
                int labelWidth = g2.getFontMetrics().stringWidth(label);
                g2.drawString(label, bx + (bw - labelWidth) / 2, h - padding + 30);
                
                i++;
            }
            
            // Draw title
            g2.setFont(new Font("SansSerif", Font.BOLD, 16));
            g2.setColor(PRIMARY_BLUE);
            g2.drawString("📊 Progress Overview", 10, 25);
        }
    }
}