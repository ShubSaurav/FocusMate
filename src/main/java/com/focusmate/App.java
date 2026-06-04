package com.focusmate;

import com.focusmate.dao.PresetDAO;
import com.focusmate.dao.SessionDAO;
import com.focusmate.dao.TaskDAO;
import com.focusmate.service.Scheduler;
import com.focusmate.ui.AnalyticsPanel;
import com.focusmate.ui.TaskPanel;
import com.focusmate.ui.TimerPanel;

import javax.swing.*;
import java.awt.*;

public class App {
    // Modern blue color scheme
    public static final Color PRIMARY_BLUE = new Color(33, 150, 243);
    public static final Color DARK_BLUE = new Color(25, 118, 210);
    public static final Color LIGHT_BLUE = new Color(144, 202, 249);
    public static final Color BACKGROUND = new Color(240, 248, 255);
    public static final Color CARD_BG = Color.WHITE;
    public static final Color TEXT_PRIMARY = new Color(33, 33, 33);
    public static final Color TEXT_SECONDARY = new Color(117, 117, 117);
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Modern Look and Feel
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            JFrame frame = new JFrame("🎯 FocusMate - Pomodoro Timer");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1000, 650);
            frame.setLocationRelativeTo(null);
            frame.getContentPane().setBackground(BACKGROUND);

            // Create DAO objects
            TaskDAO taskDAO = new TaskDAO();
            PresetDAO presetDAO = new PresetDAO();
            SessionDAO sessionDAO = new SessionDAO();
            Scheduler scheduler = new Scheduler(taskDAO);

            // Create UI Tabs with emojis
            JTabbedPane tabs = new JTabbedPane();
            tabs.setFont(new Font("SansSerif", Font.BOLD, 14));
            tabs.setBackground(CARD_BG);
            tabs.setForeground(TEXT_PRIMARY);
            
            tabs.addTab("⏱️ Timer", new TimerPanel(taskDAO, presetDAO, sessionDAO));
            tabs.addTab("📋 Tasks", new TaskPanel(taskDAO, scheduler));
            tabs.addTab("📊 Analytics", new AnalyticsPanel(taskDAO));

            // Add tabs to the frame
            frame.add(tabs, BorderLayout.CENTER);
            frame.setVisible(true);
        });
    }
}