package com.project.artconnect;

import java.sql.Connection;

import com.project.artconnect.config.DatabaseConfig;
import com.project.artconnect.util.ConnectionManager;
import com.project.artconnect.util.ServiceProvider;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main entry point for the ArtConnect application.
 * Performs database connection diagnostics before starting the UI.
 */
public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // Perform database diagnostics before loading UI
        System.out.println("\n╔════════════════════════════════════════════════════════╗");
        System.out.println("║         ArtConnect Pro - Application Starting           ║");
        System.out.println("╚════════════════════════════════════════════════════════╝\n");

        // Check database connection
        performDatabaseCheck();

        System.out.println("\n✅ Loading application UI...\n");

        // Load main UI
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/project/artconnect/ui/MainView.fxml"));
        Scene scene = new Scene(loader.load(), 1200, 800);
        stage.setTitle("ArtConnect Pro - Local Art Community Platform");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Performs database connection diagnostics to identify issues
     */
    private void performDatabaseCheck() {
        System.out.println("📋 DATABASE DIAGNOSTICS:");
        System.out.println("─".repeat(56));

        // Display current configuration
        System.out.println("Configuration Summary:");
        DatabaseConfig.printConfiguration();
        System.out.println();

        // Test connection
        System.out.println("Testing database connection...");
        try {
            Connection conn = ConnectionManager.getConnection();
            if (conn != null) {
                System.out.println("✅ Database connection SUCCESS");

                // Verify we can query data
                try {
                    int count = ServiceProvider.getArtistService().getAllArtists().size();
                    System.out.println("✅ Data query SUCCESS - Retrieved " + count + " artists from database");

                    if (count == 0) {
                        System.out.println("⚠️  WARNING: No artists found in database!");
                        System.out.println("   → Execute: mysql -u root -p Art_connect < SQL/Insert.sql");
                    }
                } catch (Exception e) {
                    System.out.println("❌ Data query FAILED: " + e.getMessage());
                }

                conn.close();
            } else {
                System.out.println("❌ Database connection FAILED - returned null");
            }
        } catch (Exception e) {
            System.out.println("❌ Database connection FAILED");
            System.out.println("   Error: " + e.getMessage());
            System.out.println();
            System.out.println("TROUBLESHOOTING:");
            System.out.println("  1. Verify all parameters in DatabaseConfig.java");
            System.out.println("  2. Check that MySQL is running");
            System.out.println("  3. Verify database exists: mysql -u root -p Art_connect");
            System.out.println("  4. Load sample data: mysql -u root -p Art_connect < SQL/Insert.sql");
        }

        System.out.println("─".repeat(56));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
