package com.focusmate.ui;

import com.focusmate.dao.PresetDAO;
import com.focusmate.dao.SessionDAO;
import com.focusmate.dao.TaskDAO;
import com.focusmate.model.Preset;
import com.focusmate.model.Session;
import com.focusmate.model.Task;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class TimerPanel extends JPanel {
    // Blue theme colors
    private static final Color PRIMARY_BLUE = new Color(33, 150, 243);
    private static final Color DARK_BLUE = new Color(25, 118, 210);
    private static final Color LIGHT_BLUE = new Color(144, 202, 249);
    private static final Color BACKGROUND = new Color(240, 248, 255);
    private static final Color CARD_BG = Color.WHITE;
    
    private final JComboBox<Task> taskBox = new JComboBox<>();
    private final JComboBox<Preset> presetBox = new JComboBox<>();
    private final JTextField customMin = new JTextField("25", 4);
    private final JButton startBtn = new JButton("▶️ Start");
    private final JButton stopBtn = new JButton("⏹️ Stop");
    private final JLabel countdownLbl = new JLabel("00:00", SwingConstants.CENTER);

    private final TaskDAO taskDAO;
    private final PresetDAO presetDAO;
    private final SessionDAO sessionDAO;

    private Timer ticking;
    private LocalDateTime startTime;
    private int plannedMin;
    private int remainingSec;

    public TimerPanel(TaskDAO taskDAO, PresetDAO presetDAO, SessionDAO sessionDAO) {
        this.taskDAO = taskDAO;
        this.presetDAO = presetDAO;
        this.sessionDAO = sessionDAO;
        
        setLayout(new BorderLayout(12, 12));
        setBackground(BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Top control panel with modern styling
        JPanel top = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        top.setBackground(CARD_BG);
        top.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LIGHT_BLUE, 2, true),
                BorderFactory.createMatteBorder(2, 2, 5, 2, new Color(200, 200, 200, 100))
            ),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        // Style labels with emojis
        JLabel taskLabel = new JLabel("📌 Task:");
        taskLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        taskLabel.setForeground(PRIMARY_BLUE);
        top.add(taskLabel);
        
        taskBox.setFont(new Font("SansSerif", Font.PLAIN, 12));
        top.add(taskBox);
        
        JLabel presetLabel = new JLabel("⚙️ Preset:");
        presetLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        presetLabel.setForeground(PRIMARY_BLUE);
        top.add(presetLabel);
        
        presetBox.setFont(new Font("SansSerif", Font.PLAIN, 12));
        top.add(presetBox);
        
        JLabel customLabel = new JLabel("⏰ Custom (min):");
        customLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        customLabel.setForeground(PRIMARY_BLUE);
        top.add(customLabel);
        
        customMin.setFont(new Font("SansSerif", Font.PLAIN, 12));
        top.add(customMin);
        
        // Style buttons with hover effects
        styleButton(startBtn, PRIMARY_BLUE, DARK_BLUE);
        styleButton(stopBtn, new Color(244, 67, 54), new Color(211, 47, 47));
        
        top.add(startBtn);
        top.add(stopBtn);
        add(top, BorderLayout.NORTH);

        // Center countdown with modern card
        JPanel centerCard = new JPanel(new BorderLayout());
        centerCard.setBackground(CARD_BG);
        centerCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY_BLUE, 3, true),
                BorderFactory.createMatteBorder(3, 3, 8, 3, new Color(33, 150, 243, 50))
            ),
            BorderFactory.createEmptyBorder(40, 40, 40, 40)
        ));
        
        countdownLbl.setFont(new Font("SansSerif", Font.BOLD, 72));
        countdownLbl.setForeground(PRIMARY_BLUE);
        centerCard.add(countdownLbl, BorderLayout.CENTER);
        
        JLabel timerIcon = new JLabel("⏳", SwingConstants.CENTER);
        timerIcon.setFont(new Font("SansSerif", Font.PLAIN, 48));
        centerCard.add(timerIcon, BorderLayout.NORTH);
        
        add(centerCard, BorderLayout.CENTER);

        loadData();
        startBtn.addActionListener(e -> onStart());
        stopBtn.addActionListener(e -> onStop(true));
    }
    
    private void styleButton(JButton btn, Color normalColor, Color hoverColor) {
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(normalColor);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(normalColor.darker(), 1, true),
            BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        
        // Advanced hover effect with shadow simulation and scale effect
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(hoverColor);
                btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(hoverColor.darker().darker(), 2, true),
                    BorderFactory.createEmptyBorder(9, 19, 9, 19)
                ));
                // Scale effect simulation
                Font currentFont = btn.getFont();
                btn.setFont(currentFont.deriveFont(15f));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(normalColor);
                btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(normalColor.darker(), 1, true),
                    BorderFactory.createEmptyBorder(10, 20, 10, 20)
                ));
                btn.setFont(new Font("SansSerif", Font.BOLD, 14));
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                // Pressed effect - darker color and slight inset
                btn.setBackground(hoverColor.darker());
                btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(hoverColor.darker().darker(), 2, false),
                    BorderFactory.createEmptyBorder(11, 21, 8, 18)
                ));
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                if (btn.contains(evt.getPoint())) {
                    btn.setBackground(hoverColor);
                    btn.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(hoverColor.darker().darker(), 2, true),
                        BorderFactory.createEmptyBorder(9, 19, 9, 19)
                    ));
                }
            }
        });
    }

    private void loadData() {
        try {
            taskBox.removeAllItems();
            for (Task t : taskDAO.listAll()) taskBox.addItem(t);
            presetBox.removeAllItems();
            List<Preset> presets = presetDAO.listAll();
            for (Preset p : presets) presetBox.addItem(p);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "DB load error: " + ex.getMessage());
        }
    }

    private void onStart() {
        if (ticking != null) return;
        try {
            plannedMin = Integer.parseInt(customMin.getText().trim());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid minutes");
            return;
        }
        remainingSec = plannedMin * 60;
        startTime = LocalDateTime.now();
        ticking = new Timer(true);
        ticking.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                SwingUtilities.invokeLater(() -> tick());
            }
        }, 0, 1000);
    }

    private void tick() {
        if (remainingSec <= 0) {
            onStop(false);
            return;
        }
        remainingSec--;
        int m = remainingSec / 60, s = remainingSec % 60;
        countdownLbl.setText(String.format("%02d:%02d", m, s));
        
        // Pulsating effect when time is low (last 60 seconds)
        if (remainingSec <= 60 && remainingSec % 2 == 0) {
            countdownLbl.setForeground(new Color(244, 67, 54)); // Red
            countdownLbl.setFont(new Font("SansSerif", Font.BOLD, 76));
        } else if (remainingSec <= 60) {
            countdownLbl.setForeground(PRIMARY_BLUE);
            countdownLbl.setFont(new Font("SansSerif", Font.BOLD, 72));
        } else {
            countdownLbl.setForeground(PRIMARY_BLUE);
            countdownLbl.setFont(new Font("SansSerif", Font.BOLD, 72));
        }
    }

    private void onStop(boolean manual) {
        if (ticking == null) return;
        ticking.cancel();
        ticking = null;
        LocalDateTime end = LocalDateTime.now();
        int actualMin = (int) Math.max(1, Math.round((java.time.Duration.between(startTime, end).toSeconds()) / 60.0));

        Session s = new Session();
        Task selected = (Task) taskBox.getSelectedItem();
        s.taskId = selected == null ? null : selected.id;
        s.start = startTime;
        s.end = end;
        s.plannedMinutes = plannedMin;
        s.actualMinutes = actualMin;
        s.stoppedManually = manual;

        try {
            sessionDAO.insert(s);
            JOptionPane.showMessageDialog(this, "Session saved (" + (manual ? "manual stop" : "auto complete") + ")");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Save failed: " + ex.getMessage());
        }
        countdownLbl.setText("00:00");
    }
}