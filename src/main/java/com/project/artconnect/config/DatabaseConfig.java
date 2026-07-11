package com.project.artconnect.config;

/**
 * ============================================================================
 * DATABASE CONFIGURATION - ARTCONNECT PROJECT
 * ============================================================================
 * 
 * This file contains all database connection parameters for the ArtConnect
 * application. Modify the values below to match your MySQL database setup.
 * 
 * ⚠️  IMPORTANT: These are sensitive credentials. In production, consider:
 *     - Using environment variables instead of hardcoding
 *     - Using a properties file that's not committed to version control
 *     - Using a secrets management service
 * 
 * ============================================================================
 */
public class DatabaseConfig {

    // ========================================================================
    // 📝 SECTION 1: DATABASE CONNECTION PARAMETERS - MODIFY THESE VALUES
    // ========================================================================
    
    /**
     * Database Host Configuration
     * 
     * Typical values:
     *   - "localhost"     → Database on your local machine
     *   - "127.0.0.1"     → Same as localhost (IP address)
     *   - "192.168.1.10"  → Database on another machine in your network
     *   - "db.example.com" → Remote database (cloud, server)
     */
    private static final String DB_HOST = "localhost";
    
    /**
     * Database Port Configuration
     * 
     * Default MySQL port: 3306
     * Change if your MySQL is configured on a different port
     */
    private static final int DB_PORT = 3306;
    
    /**
     * Database Name Configuration
     * 
     * This is the name of the database that was created.
     * Must match the database created in SQL/Art-connect_bdd.sql
     * 
     * Created with: CREATE DATABASE Art_connect;
     */
    private static final String DB_NAME = "Art_connect";
    
    // ========================================================================
    // 🔐 SECTION 2: AUTHENTICATION CREDENTIALS - MODIFY THESE VALUES
    // ========================================================================
    
    /**
     * MySQL Username
     * 
     * Default: "root"
     * Change to your MySQL username if different
     * 
     * ⚠️  SECURITY NOTE: In production, use a dedicated non-root user
     *     with minimal required permissions.
     */
    private static final String DB_USER = "root";
    
    /**
     * MySQL Password
     * 
     * ⚠️  IMPORTANT: This is YOUR MySQL password set during installation
     * MUST be changed from the default!
     * 
     * Default MySQL installation: Usually empty or "password"
     * 
     * Examples:
     *   - ""                        → No password (not recommended)
     *   - "YOUR_MYSQL_PASSWORD"         → Your actual password
     *   - "my$ecure#Pass123"       → Complex password recommended
     * 
     * To find your password:
     *   1. Check your MySQL installation notes
     *   2. If forgotten, you may need to reset it
     * 
     * How to set/change MySQL password:
     *   mysql -u root
     *   ALTER USER 'root'@'localhost' IDENTIFIED BY 'new_password';
     *   FLUSH PRIVILEGES;
     */
    private static final String DB_PASSWORD = "YOUR_MYSQL_PASSWORD";
    
    // ========================================================================
    // 🔗 SECTION 3: JDBC CONNECTION STRING (AUTO-GENERATED)
    // ========================================================================
    // Do NOT modify these - they're built from the parameters above
    
    /**
     * Complete JDBC URL
     * 
     * Format: jdbc:mysql://host:port/database?useSSL=false&serverTimezone=UTC
     * 
     * Parameters explained:
     *   - jdbc:mysql://  → JDBC driver for MySQL
     *   - host:port      → Database location
     *   - database       → Database name
     *   - useSSL=false   → Disable SSL (for local development)
     *   - serverTimezone=UTC → Timezone setting (important for Java 8+)
     */
    public static final String URL = 
        "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME 
        + "?useSSL=false&serverTimezone=UTC";
    
    /**
     * MySQL Username
     */
    public static final String USER = DB_USER;
    
    /**
     * MySQL Password
     */
    public static final String PASSWORD = DB_PASSWORD;
    
    // ========================================================================
    // ✅ QUICK REFERENCE
    // ========================================================================
    
    /**
     * To configure the database connection:
     * 
     * STEP 1: Change these values if needed
     *   - DB_HOST (line ~24)       → Usually "localhost"
     *   - DB_PORT (line ~31)       → Usually 3306
     *   - DB_NAME (line ~38)       → "Art_connect"
     *   - DB_USER (line ~49)       → "root" or your MySQL username
     *   - DB_PASSWORD (line ~63)   → Your MySQL password ⚠️
     * 
     * STEP 2: Save this file
     * 
     * STEP 3: Verify connection
     *   - Use MySQL Workbench or mysql CLI to test
     *   - mysql -u root -p Art_connect
     * 
     * STEP 4: Compile and run
     *   - mvn clean compile
     *   - mvn javafx:run
     * 
     * Current Configuration Summary:
     *   Host:     " + DB_HOST + "
     *   Port:     " + DB_PORT + "
     *   Database: " + DB_NAME + "
     *   User:     " + DB_USER + "
     *   Password: [SET - " + (DB_PASSWORD.isEmpty() ? "EMPTY" : "****") + "]
     *   
     *   JDBC URL: " + URL + "
     */
    
    // ========================================================================
    // Debug method - Displays current configuration (password masked)
    // ========================================================================
    
    /**
     * Prints the current database configuration for verification.
     * Password is masked for security.
     */
    public static void printConfiguration() {
        System.out.println("╔════════════════════════════════════════════════════════╗");
        System.out.println("║      DATABASE CONFIGURATION (ArtConnect)              ║");
        System.out.println("╚════════════════════════════════════════════════════════╝");
        System.out.println("Host:        " + DB_HOST);
        System.out.println("Port:        " + DB_PORT);
        System.out.println("Database:    " + DB_NAME);
        System.out.println("User:        " + DB_USER);
        System.out.println("Password:    " + (DB_PASSWORD.isEmpty() ? "[EMPTY]" : "[SET]"));
        System.out.println("─".repeat(56));
        System.out.println("JDBC URL: " + URL);
        System.out.println("════════════════════════════════════════════════════════");
    }
}
