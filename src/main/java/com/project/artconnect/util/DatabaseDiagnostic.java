package com.project.artconnect.util;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.project.artconnect.config.DatabaseConfig;

/**
 * Database Configuration Diagnostic Tool
 * 
 * Use this class to verify your database connection configuration.
 * It performs a comprehensive check and reports any issues.
 * 
 * Usage:
 * java -cp target/classes com.project.artconnect.util.DatabaseDiagnostic
 * 
 * Or from Java code:
 * DatabaseDiagnostic.performFullDiagnostic();
 */
public class DatabaseDiagnostic {

    /**
     * Performs comprehensive database connection diagnostic
     */
    public static void performFullDiagnostic() {
        System.out.println("╔════════════════════════════════════════════════════════╗");
        System.out.println("║     DATABASE CONFIGURATION DIAGNOSTIC                  ║");
        System.out.println("║              ArtConnect Project                        ║");
        System.out.println("╚════════════════════════════════════════════════════════╝\n");

        // Step 1: Print configuration
        printConfiguration();
        System.out.println();

        // Step 2: Test JDBC driver
        testJdbcDriver();
        System.out.println();

        // Step 3: Test connection
        testConnection();
        System.out.println();

        // Step 4: Check database
        checkDatabase();
        System.out.println();

        // Step 5: Check tables
        checkTables();
        System.out.println();

        // Final summary
        printSummary();
    }

    /**
     * Step 1: Print current configuration
     */
    private static void printConfiguration() {
        System.out.println("STEP 1: Current Configuration");
        System.out.println("─".repeat(56));
        DatabaseConfig.printConfiguration();
    }

    /**
     * Step 2: Verify JDBC driver is available
     */
    private static void testJdbcDriver() {
        System.out.println("STEP 2: JDBC Driver Check");
        System.out.println("─".repeat(56));

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("✅ MySQL JDBC Driver found: com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("❌ MySQL JDBC Driver NOT found!");
            System.out.println("   Error: " + e.getMessage());
            System.out.println("   Solution: Verify mysql-connector-j is in pom.xml");
        }
    }

    /**
     * Step 3: Test actual connection to database
     */
    private static void testConnection() {
        System.out.println("STEP 3: Database Connection Test");
        System.out.println("─".repeat(56));

        try {
            Connection conn = ConnectionManager.getConnection();
            if (conn != null) {
                System.out.println("✅ Connection SUCCESSFUL");
                System.out.println("   URL: " + conn.getMetaData().getURL());
                System.out.println("   User: " + conn.getMetaData().getUserName());
                System.out.println("   Driver: " + conn.getMetaData().getDriverName());
                System.out.println("   Driver Version: " + conn.getMetaData().getDriverVersion());

                conn.close();
            } else {
                System.out.println("❌ Connection FAILED - returned null");
            }
        } catch (SQLException e) {
            System.out.println("❌ Connection FAILED");
            System.out.println("   Error Code: " + e.getErrorCode());
            System.out.println("   SQL State: " + e.getSQLState());
            System.out.println("   Message: " + e.getMessage());
            System.out.println();
            System.out.println("   Troubleshooting:");
            troubleshootError(e);
        }
    }

    /**
     * Step 4: Check if database exists and is accessible
     */
    private static void checkDatabase() {
        System.out.println("STEP 4: Database Verification");
        System.out.println("─".repeat(56));

        try {
            Connection conn = ConnectionManager.getConnection();
            if (conn != null) {
                DatabaseMetaData metaData = conn.getMetaData();

                // List all databases
                ResultSet databases = metaData.getCatalogs();
                boolean found = false;

                System.out.println("Available Databases:");
                while (databases.next()) {
                    String dbName = databases.getString(1);
                    if ("Art_connect".equals(dbName)) {
                        System.out.println("   ✅ " + dbName + " (TARGET DATABASE)");
                        found = true;
                    } else if (!dbName.startsWith("mysql") && !dbName.equals("information_schema")
                            && !dbName.equals("performance_schema")) {
                        System.out.println("   • " + dbName);
                    }
                }
                databases.close();

                if (!found) {
                    System.out.println("\n   ❌ ERROR: Art_connect database NOT found!");
                    System.out.println("   Solution: Execute SQL/Art-connect_bdd.sql");
                }

                conn.close();
            }
        } catch (SQLException e) {
            System.out.println("❌ Could not check databases");
            System.out.println("   Error: " + e.getMessage());
        }
    }

    /**
     * Step 5: Check if required tables exist
     */
    private static void checkTables() {
        System.out.println("STEP 5: Database Tables Check");
        System.out.println("─".repeat(56));

        String[] requiredTables = { "artists", "artworks", "exhibitions", "galleries", "workshops" };

        try {
            Connection conn = ConnectionManager.getConnection();
            if (conn != null) {
                DatabaseMetaData metaData = conn.getMetaData();

                // Get tables for Art_connect database
                ResultSet tables = metaData.getTables("Art_connect", null, "%", new String[] { "TABLE" });

                java.util.Set<String> foundTables = new java.util.HashSet<>();
                while (tables.next()) {
                    foundTables.add(tables.getString(3));
                }
                tables.close();

                System.out.println("Required Tables:");
                boolean allFound = true;
                for (String table : requiredTables) {
                    if (foundTables.contains(table)) {
                        System.out.println("   ✅ " + table);
                    } else {
                        System.out.println("   ❌ " + table + " (MISSING)");
                        allFound = false;
                    }
                }

                if (!allFound) {
                    System.out.println("\n   ⚠️  Some tables are missing!");
                    System.out.println("   Solution: Execute SQL/Art-connect_bdd.sql again");
                }

                conn.close();
            }
        } catch (SQLException e) {
            System.out.println("❌ Could not check tables");
            System.out.println("   Error: " + e.getMessage());
        }
    }

    /**
     * Print final diagnostic summary
     */
    private static void printSummary() {
        System.out.println("FINAL SUMMARY");
        System.out.println("─".repeat(56));

        boolean configOk = checkConfiguration();

        if (configOk) {
            System.out.println("✅ DATABASE CONFIGURATION IS CORRECT!");
            System.out.println("\nYou can now:");
            System.out.println("  1. Compile: mvn clean compile");
            System.out.println("  2. Run: mvn javafx:run");
        } else {
            System.out.println("❌ DATABASE CONFIGURATION HAS ISSUES");
            System.out.println("\nPlease verify:");
            System.out.println("  1. MySQL is running");
            System.out.println("  2. Credentials in DatabaseConfig.java are correct");
            System.out.println("  3. Database and tables exist");
            System.out.println("  4. MySQL JDBC driver is in Maven dependencies");
        }
        System.out.println("════════════════════════════════════════════════════════");
    }

    /**
     * Check overall configuration
     */
    private static boolean checkConfiguration() {
        try {
            Connection conn = ConnectionManager.getConnection();
            if (conn != null) {
                conn.close();
                return true;
            }
        } catch (SQLException e) {
            return false;
        }
        return false;
    }

    /**
     * Troubleshoot specific SQL error
     */
    private static void troubleshootError(SQLException e) {
        String errorMessage = e.getMessage();

        if (errorMessage.contains("Connection refused") || errorMessage.contains("Communication link failure")) {
            System.out.println("   • MySQL server is not running or unreachable");
            System.out.println("   • On Windows: Check Services → MySQL");
            System.out.println("   • On Mac: brew services start mysql");
            System.out.println("   • On Linux: sudo systemctl start mysq");
        } else if (errorMessage.contains("Access denied")) {
            System.out.println("   • Wrong username or password");
            System.out.println("   • Update DB_USER or DB_PASSWORD in DatabaseConfig.java");
            System.out.println("   • To reset MySQL root password: Use MySQL Installer or mysql_secure_installation");
        } else if (errorMessage.contains("Unknown database")) {
            System.out.println("   • Database 'Art_connect' does not exist");
            System.out.println("   • Execute: mysql -u root -p < SQL/Art-connect_bdd.sql");
        } else if (errorMessage.contains("No suitable driver")) {
            System.out.println("   • MySQL JDBC driver is not in classpath");
            System.out.println("   • Check pom.xml for mysql-connector-j dependency");
        } else {
            System.out.println("   • Other error: Check MySQL logs");
        }
    }

    /**
     * Main entry point for command line usage
     */
    public static void main(String[] args) {
        performFullDiagnostic();
    }
}
