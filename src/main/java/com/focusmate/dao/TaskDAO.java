package com.focusmate.dao;

import com.focusmate.db.DB;
import com.focusmate.model.Task;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TaskDAO {
    public void insert(Task t) throws Exception {
        try (Connection c = DB.get();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO tasks(title,priority,due_date,target_minutes,status,user_id) VALUES (?,?,?,?,?,?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, t.title);
            ps.setInt(2, t.priority);
            if (t.dueDate == null)
                ps.setNull(3, Types.DATE);
            else
                ps.setDate(3, Date.valueOf(t.dueDate));
            ps.setInt(4, t.targetMinutes);
            ps.setString(5, t.status == null ? "PENDING" : t.status);
            if (t.userId == null) {
                ps.setNull(6, Types.INTEGER);
            } else {
                ps.setInt(6, t.userId);
            }
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) t.id = rs.getInt(1);
            }
        }
    }

    public void update(Task t) throws Exception {
        try (Connection c = DB.get();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE tasks SET title=?, priority=?, due_date=?, target_minutes=?, status=? WHERE id=? AND user_id=?")) {
            ps.setString(1, t.title);
            ps.setInt(2, t.priority);
            if (t.dueDate == null)
                ps.setNull(3, Types.DATE);
            else
                ps.setDate(3, Date.valueOf(t.dueDate));
            ps.setInt(4, t.targetMinutes);
            ps.setString(5, t.status);
            ps.setInt(6, t.id);
            ps.setInt(7, t.userId != null ? t.userId : 0);
            ps.executeUpdate();
        }
    }

    public List<Task> listAll(int userId) throws Exception {
        List<Task> list = new ArrayList<>();
        try (Connection c = DB.get();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM tasks WHERE user_id = ? ORDER BY created_at DESC")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public List<Task> listAll() throws Exception {
        List<Task> list = new ArrayList<>();
        try (Connection c = DB.get();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM tasks ORDER BY created_at DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }


    public Task findById(int id, int userId) throws Exception {
        try (Connection c = DB.get();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM tasks WHERE id = ? AND user_id = ?")) {
            ps.setInt(1, id);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    public boolean delete(int id, int userId) throws Exception {
        try (Connection c = DB.get();
             PreparedStatement ps = c.prepareStatement("DELETE FROM tasks WHERE id = ? AND user_id = ?")) {
            ps.setInt(1, id);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    public int getActualMinutes(int taskId, int userId) throws Exception {
        try (Connection c = DB.get();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT COALESCE(SUM(actual_minutes),0) FROM sessions WHERE task_id=? AND user_id=?")) {
            ps.setInt(1, taskId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private Task map(ResultSet rs) throws Exception {
        Task t = new Task();
        t.id = rs.getInt("id");
        t.title = rs.getString("title");
        t.priority = rs.getInt("priority");
        Date date = rs.getDate("due_date");
        t.dueDate = (date != null) ? date.toLocalDate() : null;
        t.targetMinutes = rs.getInt("target_minutes");
        t.status = rs.getString("status");
        t.userId = rs.getInt("user_id");
        if (rs.wasNull()) t.userId = null;
        return t;
    }
}