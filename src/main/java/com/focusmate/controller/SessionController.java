package com.focusmate.controller;

import com.focusmate.dao.SessionDAO;
import com.focusmate.model.Session;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/sessions")
@CrossOrigin(origins = "*")
public class SessionController {

    private static final String SESSION_USER_KEY = "FOCUSMATE_USER_ID";
    private final SessionDAO sessionDAO = new SessionDAO();

    private int getUserId(HttpSession session) {
        Integer userId = (Integer) session.getAttribute(SESSION_USER_KEY);
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login required");
        }
        return userId;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createSession(@RequestBody SessionRequest request, HttpSession session) {
        int userId = getUserId(session);
        try {
            Session record = new Session();
            record.taskId = request.taskId;
            record.userId = userId;
            record.start = LocalDateTime.parse(request.start);
            record.end = LocalDateTime.parse(request.end);
            record.plannedMinutes = request.plannedMinutes;
            record.actualMinutes = request.actualMinutes;
            record.stoppedManually = request.stoppedManually;
            
            try {
                sessionDAO.insert(record);
            } catch (Exception ex) {
                com.focusmate.store.MemoryStore.SESSIONS.add(record);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Session saved successfully");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (ResponseStatusException ex) {
            throw ex;
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
