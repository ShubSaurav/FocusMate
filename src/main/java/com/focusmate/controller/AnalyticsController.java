package com.focusmate.controller;

import com.focusmate.model.Session;
import com.focusmate.model.Task;
import com.focusmate.store.MemoryStore;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "*")
public class AnalyticsController {

    @GetMapping("/summary")
    public Map<String, Object> summary() {
        // Basic summary based on in-memory sessions and tasks
        List<Session> sessions = MemoryStore.SESSIONS;
        Map<Integer, Task> tasks = MemoryStore.TASKS;

        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        long todaySessions = sessions.stream()
                .filter(s -> s.start != null && s.start.toLocalDate().equals(today))
                .count();

        int totalMinutes = sessions.stream().mapToInt(s -> s.actualMinutes).sum();
        long totalTasks = tasks.size();
        long doneTasks = tasks.values().stream().filter(t -> "DONE".equalsIgnoreCase(t.status)).count();
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
    public Map<String, Object> taskAnalytics(@PathVariable Integer id) {
        Task t = MemoryStore.TASKS.get(id);
        int target = t != null ? t.targetMinutes : 0;
        int actual = MemoryStore.SESSIONS.stream()
                .filter(s -> s.taskId != null && s.taskId.equals(id))
                .mapToInt(s -> s.actualMinutes)
                .sum();

        Map<String, Object> result = new HashMap<>();
        result.put("target", target);
        result.put("actual", actual);
        return result;
    }

    private int computeStreak(List<Session> sessions) {
        if (sessions.isEmpty()) return 0;
        // Simple heuristic: days with any session, count consecutive days up to today
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
