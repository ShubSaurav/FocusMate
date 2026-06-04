package com.focusmate.dao;

import com.focusmate.db.DB;
import com.focusmate.model.Preset;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PresetDAO {
    public List<Preset> listAll() throws Exception {
        List<Preset> presets = new ArrayList<>();
        try (Connection c = DB.get();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM presets ORDER BY id ASC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Preset p = new Preset();
                p.id = rs.getInt("id");
                p.name = rs.getString("name");
                p.focusMin = rs.getInt("focus_min");
                p.shortBreakMin = rs.getInt("short_break_min");
                p.longBreakMin = rs.getInt("long_break_min");
                p.cyclesBeforeLong = rs.getInt("cycles_before_long");
                presets.add(p);
            }
        }
        return presets;
    }
}