package com.focusmate.db;

import java.sql.*;

public class DumpTable {
    public static void main(String[] args) throws Exception {
        String table = args.length > 0 ? args[0] : "users";
        try (Connection conn = DB.get();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM " + table + " LIMIT 100")) {
            ResultSetMetaData md = rs.getMetaData();
            int cols = md.getColumnCount();
            System.out.println("\nRows from table: " + table);
            while (rs.next()) {
                StringBuilder row = new StringBuilder();
                for (int i = 1; i <= cols; i++) {
                    row.append(md.getColumnLabel(i)).append("=")
                       .append(rs.getObject(i))
                       .append(i == cols ? "" : ", ");
                }
                System.out.println(row);
            }
        }
    }
}
