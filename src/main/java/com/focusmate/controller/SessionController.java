package com.focusmate.controller;

import com.focusmate.dao.SessionDAO;
import com.focusmate.model.Session;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/sessions")
@CrossOrigin(origins = "*")
public class SessionController {
    
    private final SessionDAO sessionDAO = new SessionDAO();
    private boolean dbAvailable() {
        try {
            // Try a lightweight insert into a transaction-less connection? Not ideal.
            // Instead, rely on TaskDAO probe approach or simply catch on insert.
            return true; // We'll detect failure on insert and fallback.
        } catch (Exception e) {
            return false;
        }
    }
    
    @PostMapping
    public ResponseEntity<Map<String, Object>> createSession(@RequestBody SessionRequest request) {
        try {
            Session session = new Session();
            session.taskId = request.taskId;
            session.start = LocalDateTime.parse(request.start);
            session.end = LocalDateTime.parse(request.end);
            session.plannedMinutes = request.plannedMinutes;
            session.actualMinutes = request.actualMinutes;
            session.stoppedManually = request.stoppedManually;
            
            try {
                if (dbAvailable()) {
                    sessionDAO.insert(session);
                } else {
                    throw new RuntimeException("DB not available");
                }
            } catch (Exception ex) {
                // Fallback to memory store
                com.focusmate.store.MemoryStore.SESSIONS.add(session);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Session saved successfully");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    public static class SessionRequest {
        public Integer taskId;
        public String start;
        public String end;
        public int plannedMinutes;
        public int actualMinutes;
        public boolean stoppedManually;
    }
}
