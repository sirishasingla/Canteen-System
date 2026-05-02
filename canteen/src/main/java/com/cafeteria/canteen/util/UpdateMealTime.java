package com.cafeteria.canteen.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class UpdateMealTime {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/canteen_db";
        String user = "postgres";
        String password = "admin";
        
        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {
            
            // Update lunch time to 3-5 PM
            String sql = "UPDATE meal SET start_time = '15:00:00', end_time = '17:00:00' WHERE type = 'LUNCH'";
            int rowsAffected = stmt.executeUpdate(sql);
            
            System.out.println("✅ Successfully updated " + rowsAffected + " row(s)");
            System.out.println("✅ Lunch time is now 3:00 PM - 5:00 PM");
            
        } catch (Exception e) {
            System.err.println("❌ Error updating database: " + e.getMessage());
            e.printStackTrace();
        }
    }
}