package com.focusmate.controller;

import com.focusmate.dao.SessionDAO;
import com.focusmate.dao.TaskDAO;
import com.focusmate.model.Session;
import com.focusmate.model.Task;
import com.focusmate.store.MemoryStore;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "*")
public class AnalyticsController {

    private static final String SESSION_USER_KEY = "FOCUSMATE_USER_ID";
    private final TaskDAO taskDAO = new TaskDAO();
    private final SessionDAO sessionDAO = new SessionDAO();

    private int getUserId(HttpSession session) {
        Integer userId = (Integer) session.getAttribute(SESSION_USER_KEY);
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login required");
        }
        return userId;
    }

    @GetMapping("/summary")
    public Map<String, Object> summary(HttpSession session) {
        int userId = getUserId(session);
        List<Session> sessions;
        List<Task> tasks;
        try {
            sessions = sessionDAO.listAll(userId);
            tasks = taskDAO.listAll(userId);
        } catch (Exception ex) {
            sessions = MemoryStore.SESSIONS.stream()
                    .filter(s -> s.userId != null && s.userId.equals(userId))
                    .toList();
            tasks = new ArrayList<>(MemoryStore.TASKS.values()).stream()
                    .filter(t -> t.userId != null && t.userId.equals(userId))
                    .toList();
        }

        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        long todaySessions = sessions.stream()
                .filter(s -> s.start != null && s.start.toLocalDate().equals(today))
                .count();

        int totalMinutes = sessions.stream().mapToInt(s -> s.actualMinutes).sum();
        long totalTasks = tasks.size();
        long doneTasks = tasks.stream().filter(t -> "DONE".equalsIgnoreCase(t.status)).count();
        int completionRate = totalTasks == 0 ? 0 : (int) Math.round((doneTasks * 100.0) / totalTasks);
        int avgSession = sessions.isEmpty() ? 0 : (int) Math.round(totalMinutes / (double) sessions.size());

        int streak = computeStreak(sessions);

        Map<String, Object> result = new HashMap<>();
        result.put("todaySessions", todaySessions);
        result.put("totalMinutes", totalMinutes);
        result.put("completionRate", completionRate);
        result.put("avgSession", avgSession);
        result.put("streak", streak);
        return result;
    }

    @GetMapping("/task/{id}")
    public Map<String, Object> taskAnalytics(@PathVariable Integer id, HttpSession session) {
        int userId = getUserId(session);
        Task task;
        int actual;
        try {
            task = taskDAO.findById(id, userId);
            actual = sessionDAO.listAll(userId).stream()
                    .filter(s -> s.taskId != null && s.taskId.equals(id))
                    .mapToInt(s -> s.actualMinutes)
                    .sum();
        } catch (Exception ex) {
            task = MemoryStore.TASKS.get(id);
            if (task != null && !task.userId.equals(userId)) {
                task = null;
            }
            actual = MemoryStore.SESSIONS.stream()
                    .filter(s -> s.userId != null && s.userId.equals(userId))
                    .filter(s -> s.taskId != null && s.taskId.equals(id))
                    .mapToInt(s -> s.actualMinutes)
                    .sum();
        }

        int target = task != null ? task.targetMinutes : 0;
        Map<String, Object> result = new HashMap<>();
        result.put("target", target);
        result.put("actual", actual);
        return result;
    }

    @GetMapping("/activity")
    public List<Map<String, Object>> activity(HttpSession session) {
        int userId = getUserId(session);
        List<Session> sessions;
        try {
            sessions = sessionDAO.listAll(userId);
        } catch (Exception ex) {
            sessions = MemoryStore.SESSIONS.stream()
                    .filter(s -> s.userId != null && s.userId.equals(userId))
                    .toList();
        }
        java.util.Map<String, Integer> daily = new java.util.HashMap<>();
        for (Session s : sessions) {
            if (s.start == null) continue;
            String dateKey = s.start.toLocalDate().toString();
            daily.put(dateKey, daily.getOrDefault(dateKey, 0) + s.actualMinutes);
        }
        java.util.List<Map<String, Object>> list = new java.util.ArrayList<>();
        for (var entry : daily.entrySet()) {
            java.util.Map<String, Object> item = new java.util.HashMap<>();
            item.put("date", entry.getKey());
            item.put("minutes", entry.getValue());
            list.add(item);
        }
        return list;
    }

    private int computeStreak(List<Session> sessions) {
        if (sessions.isEmpty()) return 0;
        LocalDate today = LocalDate.now();
        int streak = 0;
        LocalDate day = today;
        while (true) {
            LocalDate d = day;
            boolean hasSession = sessions.stream()
                    .anyMatch(s -> s.start != null && s.start.toLocalDate().equals(d));
            if (hasSession) {
                streak++;
                day = day.minusDays(1);
            } else {
                break;
            }
        }
        return streak;
    }
}
