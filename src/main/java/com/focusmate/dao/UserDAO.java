package com.focusmate.dao;

import com.focusmate.db.DB;
import com.focusmate.model.User;
import com.focusmate.util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserDAO {

    public User findByEmail(String email) throws Exception {
        try (Connection c = DB.get();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM users WHERE email = ?")) {
            ps.setString(1, email.trim().toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    public User findById(int id) throws Exception {
        try (Connection c = DB.get();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM users WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    public User insert(User user) throws Exception {
        try (Connection c = DB.get();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO users(email, name, password_hash) VALUES (?,?,?)",
                     java.sql.Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.email.trim().toLowerCase());
            ps.setString(2, user.name.trim());
            ps.setString(3, user.passwordHash);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) user.id = rs.getInt(1);
            }
        }
        return user;
    }

    public User authenticate(String email, String password) throws Exception {
        User user = findByEmail(email);
        if (user == null) return null;
        String hashed = PasswordUtil.hash(password);
        if (user.passwordHash != null && user.passwordHash.equals(hashed)) {
            return user;
        }
        return null;
    }

    private User map(ResultSet rs) throws Exception {
        User user = new User();
        user.id = rs.getInt("id");
        user.email = rs.getString("email");
        user.name = rs.getString("name");
        user.passwordHash = rs.getString("password_hash");
        return user;
    }
}
