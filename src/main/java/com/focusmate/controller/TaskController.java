package com.focusmate.controller;

import com.focusmate.dao.TaskDAO;
import com.focusmate.model.Task;
import com.focusmate.service.Scheduler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import jakarta.servlet.http.HttpSession;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "*")
public class TaskController {

    private static final String SESSION_USER_KEY = "FOCUSMATE_USER_ID";
    private final TaskDAO taskDAO = new TaskDAO();
    private final Scheduler scheduler = new Scheduler(taskDAO);

    private boolean dbAvailable() {
        try {
            taskDAO.listAll(0);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private int getUserId(HttpSession session) {
        Integer userId = (Integer) session.getAttribute(SESSION_USER_KEY);
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login required");
        }
        return userId;
    }

    @GetMapping
    public ResponseEntity<List<Task>> getAllTasks(HttpSession session) throws Exception {
        int userId = getUserId(session);
        if (!dbAvailable()) {
            return ResponseEntity.ok(com.focusmate.store.MemoryStore.TASKS.values().stream()
                    .filter(t -> t.userId != null && t.userId.equals(userId)).toList());
        }
        return ResponseEntity.ok(taskDAO.listAll(userId));
    }

    @GetMapping("/scheduled")
    public ResponseEntity<List<Task>> getScheduledTasks(HttpSession session) throws Exception {
        int userId = getUserId(session);
        List<Task> allTasks;
        if (!dbAvailable()) {
            allTasks = com.focusmate.store.MemoryStore.TASKS.values().stream()
                    .filter(t -> t.userId != null && t.userId.equals(userId)).toList();
        } else {
            allTasks = scheduler.sorted(userId);
        }
        List<Task> scheduled = allTasks.stream()
                .filter(t -> "PENDING".equals(t.status))
                .toList();
        return ResponseEntity.ok(scheduled);
    }

    @GetMapping("/completed")
    public ResponseEntity<List<Task>> getCompletedTasks(HttpSession session) throws Exception {
        int userId = getUserId(session);
        List<Task> completed;
        if (!dbAvailable()) {
            completed = com.focusmate.store.MemoryStore.TASKS.values().stream()
                    .filter(t -> t.userId != null && t.userId.equals(userId) && "DONE".equals(t.status))
                    .toList();
        } else {
            completed = taskDAO.listAll(userId).stream()
                    .filter(t -> "DONE".equals(t.status))
                    .toList();
        }
        return ResponseEntity.ok(completed);
    }

    @GetMapping("/pending")
    public ResponseEntity<List<Task>> getPendingTasks(HttpSession session) throws Exception {
        int userId = getUserId(session);
        List<Task> pending;
        if (!dbAvailable()) {
            pending = com.focusmate.store.MemoryStore.TASKS.values().stream()
                    .filter(t -> t.userId != null && t.userId.equals(userId) && !"DONE".equals(t.status))
                    .toList();
        } else {
            pending = taskDAO.listAll(userId).stream()
                    .filter(t -> !"DONE".equals(t.status))
                    .toList();
        }
        return ResponseEntity.ok(pending);
    }

    @PostMapping
    public ResponseEntity<?> createTask(@RequestBody TaskRequest request, HttpSession session) throws Exception {
        int userId = getUserId(session);
        if (request.title == null || request.title.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Title is required"));
        }
        Task task = new Task();
        task.title = request.title.trim();
        task.priority = request.priority;
        task.dueDate = request.dueDate != null && !request.dueDate.isBlank() ? LocalDate.parse(request.dueDate) : null;
        task.targetMinutes = request.targetMinutes;
        task.status = request.status != null ? request.status : "PENDING";
        task.userId = userId;
        if (dbAvailable()) {
            taskDAO.insert(task);
        } else {
            task.id = com.focusmate.store.MemoryStore.TASK_SEQ.getAndIncrement();
            com.focusmate.store.MemoryStore.TASKS.put(task.id, task);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(task);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Integer id, @RequestBody StatusUpdate update, HttpSession session) throws Exception {
        int userId = getUserId(session);
        if (update.status == null || update.status.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Status is required"));
        }
        Task task;
        if (dbAvailable()) {
            task = taskDAO.findById(id, userId);
        } else {
            task = com.focusmate.store.MemoryStore.TASKS.get(id);
            if (task != null && !task.userId.equals(userId)) task = null;
        }
        if (task == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Task not found"));
        }
        task.status = update.status.trim();
        if (dbAvailable()) {
            taskDAO.update(task);
        } else {
            com.focusmate.store.MemoryStore.TASKS.put(task.id, task);
        }
        return ResponseEntity.ok(Map.of("success", true));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTask(@PathVariable Integer id, HttpSession session) throws Exception {
        int userId = getUserId(session);
        if (dbAvailable()) {
            boolean removed = taskDAO.delete(id, userId);
            if (!removed) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Task not found"));
            }
            return ResponseEntity.ok(Map.of("success", true));
        } else {
            Task removed = com.focusmate.store.MemoryStore.TASKS.remove(id);
            if (removed == null || removed.userId == null || !removed.userId.equals(userId)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Task not found"));
            }
            return ResponseEntity.ok(Map.of("success", true, "message", "Task deleted"));
        }
    }

    public static class TaskRequest {
        public String title;
        public int priority;
        public String dueDate;
        public int targetMinutes;
        public String status;
    }

    public static class StatusUpdate {
        public String status;
    }
}
