package com.focusmate.controller;

import com.focusmate.dao.UserDAO;
import com.focusmate.model.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    private static final String USER_SESSION_KEY = "FOCUSMATE_USER_ID";
    private final UserDAO userDAO = new UserDAO();

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request, HttpSession session) {
        try {
            if (request.email == null || request.email.isBlank() || request.password == null || request.password.isBlank() || request.name == null || request.name.isBlank()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Name, email and password are required."));
            }

            if (userDAO.findByEmail(request.email) != null) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("error", "Email is already registered."));
            }

            User user = new User();
            user.email = request.email.trim().toLowerCase();
            user.name = request.name.trim();
            user.passwordHash = com.focusmate.util.PasswordUtil.hash(request.password);
            userDAO.insert(user);
            session.setAttribute(USER_SESSION_KEY, user.id);
            return ResponseEntity.status(HttpStatus.CREATED).body(new UserResponse(user));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Unable to register user."));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpSession session) {
        try {
            if (request.email == null || request.email.isBlank() || request.password == null || request.password.isBlank()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Email and password are required."));
            }
            User user = userDAO.authenticate(request.email, request.password);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid email or password."));
            }
            session.setAttribute(USER_SESSION_KEY, user.id);
            return ResponseEntity.ok(new UserResponse(user));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Login failed."));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> currentUser(HttpSession session) {
        Integer userId = (Integer) session.getAttribute(USER_SESSION_KEY);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not logged in"));
        }
        try {
            User user = userDAO.findById(userId);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid session."));
            }
            return ResponseEntity.ok(new UserResponse(user));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Unable to load user."));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(Map.of("success", true));
    }

    public static class LoginRequest {
        public String email;
        public String password;
    }

    public static class RegisterRequest {
        public String name;
        public String email;
        public String password;
    }

    public static class UserResponse {
        public Integer id;
        public String email;
        public String name;

        public UserResponse(User user) {
            this.id = user.id;
            this.email = user.email;
            this.name = user.name;
        }
    }
}
