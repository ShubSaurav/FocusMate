package com.focusmate.dao;

import com.focusmate.db.DB;
import com.focusmate.model.Session;

import java.sql.*;

public class SessionDAO {
    public void insert(Session s) throws Exception {
        try (Connection c = DB.get();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO sessions(task_id, user_id, start_time, end_time, planned_minutes, actual_minutes, stopped_manually) VALUES (?,?,?,?,?,?,?)")) {
            if (s.taskId == null)
                ps.setNull(1, Types.INTEGER);
            else
                ps.setInt(1, s.taskId);
            if (s.userId == null)
                ps.setNull(2, Types.INTEGER);
            else
                ps.setInt(2, s.userId);
            ps.setTimestamp(3, Timestamp.valueOf(s.start));
            ps.setTimestamp(4, Timestamp.valueOf(s.end));
            ps.setInt(5, s.plannedMinutes);
            ps.setInt(6, s.actualMinutes);
            ps.setBoolean(7, s.stoppedManually);
            ps.executeUpdate();
        }
    }

    public java.util.List<Session> listAll(int userId) throws Exception {
        java.util.List<Session> sessions = new java.util.ArrayList<>();
        try (Connection c = DB.get();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM sessions WHERE user_id = ? ORDER BY start_time DESC")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Session s = map(rs);
                    sessions.add(s);
                }
            }
        }
        return sessions;
    }

    private Session map(ResultSet rs) throws Exception {
        Session s = new Session();
        s.id = rs.getInt("id");
        s.taskId = rs.getInt("task_id");
        if (rs.wasNull()) s.taskId = null;
        s.userId = rs.getInt("user_id");
        if (rs.wasNull()) s.userId = null;
        s.start = rs.getTimestamp("start_time").toLocalDateTime();
        s.end = rs.getTimestamp("end_time").toLocalDateTime();
        s.plannedMinutes = rs.getInt("planned_minutes");
        s.actualMinutes = rs.getInt("actual_minutes");
        s.stoppedManually = rs.getBoolean("stopped_manually");
        return s;
    }
}
