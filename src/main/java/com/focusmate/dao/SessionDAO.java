package com.focusmate.dao;

import com.focusmate.db.DB;
import com.focusmate.model.Session;

import java.sql.*;

public class SessionDAO {
    public void insert(Session s) throws Exception {
        try (Connection c = DB.get();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO sessions(task_id, start_time, end_time, planned_minutes, actual_minutes, stopped_manually) VALUES (?,?,?,?,?,?)")) {
            if (s.taskId == null)
                ps.setNull(1, Types.INTEGER);
            else
                ps.setInt(1, s.taskId);
            ps.setTimestamp(2, Timestamp.valueOf(s.start));
            ps.setTimestamp(3, Timestamp.valueOf(s.end));
            ps.setInt(4, s.plannedMinutes);
            ps.setInt(5, s.actualMinutes);
            ps.setBoolean(6, s.stoppedManually);
            ps.executeUpdate();
        }
    }
}