package com.placementpro.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class DBConnectionPool {
    private static final int MAX_CONNECTIONS = 5;
    private static BlockingQueue<Connection> pool = new ArrayBlockingQueue<>(MAX_CONNECTIONS);
    private static Properties config = new Properties();

    static {
        try (FileInputStream fis = new FileInputStream("config.properties")) {
            config.load(fis);
            // Pre-fill the pool
            for (int i = 0; i < MAX_CONNECTIONS; i++) {
                pool.add(createNewConnection());
            }
            System.out.println("Database connection pool initialized with " + MAX_CONNECTIONS + " connections.");
        } catch (IOException | SQLException e) {
            System.err.println("Failed to initialize database connection pool: " + e.getMessage());
        }
    }

    private static Connection createNewConnection() throws SQLException {
        return DriverManager.getConnection(
                config.getProperty("db.url"),
                config.getProperty("db.user"),
                config.getProperty("db.password")
        );
    }

    public static Connection getConnection() {
        try {
            Connection conn = pool.take(); // Blocks if pool is empty
            // Check if connection is closed or invalid
            if (conn.isClosed() || !conn.isValid(2)) {
                conn = createNewConnection();
            }
            return conn;
        } catch (InterruptedException | SQLException e) {
            System.err.println("Error getting connection from pool: " + e.getMessage());
            return null;
        }
    }

    public static void releaseConnection(Connection conn) {
        if (conn != null) {
            try {
                if (!conn.isClosed()) {
                    pool.offer(conn); // Returns connection to the pool
                }
            } catch (SQLException e) {
                System.err.println("Error releasing connection: " + e.getMessage());
            }
        }
    }

    // Main method to test connection as required by P1 Gate Condition
    public static void main(String[] args) {
        System.out.println("Testing DBConnectionPool...");
        Connection conn = DBConnectionPool.getConnection();
        if (conn != null) {
            System.out.println("SUCCESS: Connection retrieved successfully from pool.");
            DBConnectionPool.releaseConnection(conn);
            System.out.println("Connection released back to pool.");
        } else {
            System.err.println("FAILURE: Could not get a connection.");
        }
    }
}
