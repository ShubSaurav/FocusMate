package com.focusmate.controller;

import com.focusmate.model.Task;
import com.focusmate.dao.TaskDAO;
import com.focusmate.service.Scheduler;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "*")
public class TaskController {
    
    private final TaskDAO taskDAO = new TaskDAO();
    private final Scheduler scheduler = new Scheduler(taskDAO);
    private boolean dbAvailable() {
        try { taskDAO.listAll(); return true; } catch (Exception e) { return false; }
    }
    
    @GetMapping
    public ResponseEntity<List<Task>> getAllTasks() throws Exception {
        if (!dbAvailable()) {
            return ResponseEntity.ok(com.focusmate.store.MemoryStore.TASKS.values().stream().toList());
        }
        return ResponseEntity.ok(taskDAO.listAll());
    }
    
    @GetMapping("/scheduled")
    public ResponseEntity<List<Task>> getScheduledTasks() throws Exception {
        List<Task> allTasks;
        if (!dbAvailable()) {
            allTasks = com.focusmate.store.MemoryStore.TASKS.values().stream().toList();
        } else {
            allTasks = scheduler.sorted();
        }
        // Filter to only show PENDING tasks (exclude IN_PROGRESS and DONE)
        List<Task> scheduled = allTasks.stream()
            .filter(t -> "PENDING".equals(t.status))
            .toList();
        return ResponseEntity.ok(scheduled);
    }
    
    @GetMapping("/completed")
    public ResponseEntity<List<Task>> getCompletedTasks() throws Exception {
        List<Task> completed = (!dbAvailable() ? com.focusmate.store.MemoryStore.TASKS.values().stream().toList() : taskDAO.listAll()).stream()
            .filter(t -> "DONE".equals(t.status))
            .toList();
        return ResponseEntity.ok(completed);
    }
    
    @GetMapping("/pending")
    public ResponseEntity<List<Task>> getPendingTasks() throws Exception {
        List<Task> pending = (!dbAvailable() ? com.focusmate.store.MemoryStore.TASKS.values().stream().toList() : taskDAO.listAll()).stream()
            .filter(t -> !"DONE".equals(t.status))
            .toList();
        return ResponseEntity.ok(pending);
    }
    
    @PostMapping
    public ResponseEntity<?> createTask(@RequestBody TaskRequest request) throws Exception {
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
        if (dbAvailable()) {
            taskDAO.insert(task);
        } else {
            task.id = com.focusmate.store.MemoryStore.TASK_SEQ.getAndIncrement();
            com.focusmate.store.MemoryStore.TASKS.put(task.id, task);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(task);
    }
    
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Integer id, @RequestBody StatusUpdate update) throws Exception {
        if (update.status == null || update.status.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Status is required"));
        }
        Task task;
        if (dbAvailable()) {
            task = taskDAO.listAll().stream().filter(t -> t.id.equals(id)).findFirst().orElse(null);
        } else {
            task = com.focusmate.store.MemoryStore.TASKS.get(id);
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
    public ResponseEntity<?> deleteTask(@PathVariable Integer id) throws Exception {
        if (dbAvailable()) {
            // Note: TaskDAO doesn't have delete method, so we just mark as deleted or skip for now
            // For memory store, we can remove directly
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                .body(Map.of("error", "Delete not supported with database"));
        } else {
            Task removed = com.focusmate.store.MemoryStore.TASKS.remove(id);
            if (removed == null) {
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
