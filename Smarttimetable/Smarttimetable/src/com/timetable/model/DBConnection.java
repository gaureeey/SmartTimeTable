package com.timetable.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class DBConnection {

    private static final DBConnection INSTANCE = new DBConnection();

    private final String dbHost;
    private final String dbPort;
    private final String dbName;
    private final String dbUser;
    private final String dbPassword;
    private final boolean explicitHostOrPort;
    private final boolean explicitCredentials;

    private DBConnection() {
        Map<String, String> env = System.getenv();
        this.dbHost = env.getOrDefault("TT_DB_HOST", "localhost");
        this.dbPort = env.getOrDefault("TT_DB_PORT", "3306");
        this.dbName = env.getOrDefault("TT_DB_NAME", "timetable_db");

        this.explicitHostOrPort = env.containsKey("TT_DB_HOST") || env.containsKey("TT_DB_PORT");
        this.explicitCredentials = env.containsKey("TT_DB_USER") || env.containsKey("TT_DB_PASS");
        this.dbUser = env.getOrDefault("TT_DB_USER", "root");
        this.dbPassword = env.getOrDefault("TT_DB_PASS", "root");

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException ex) {
            throw new IllegalStateException(
                    "MySQL JDBC driver not found. Add mysql-connector-j to WebContent/WEB-INF/lib.", ex);
        }
    }

    public static DBConnection getInstance() {
        return INSTANCE;
    }

    public Connection getConnection() throws SQLException {
        SQLException lastException = null;

        for (String jdbcUrl : getCandidateJdbcUrls()) {
            try {
                return DriverManager.getConnection(jdbcUrl, dbUser, dbPassword);
            } catch (SQLException primaryException) {
                lastException = primaryException;

                if (shouldRetryWithEmptyPassword(primaryException)) {
                    try {
                        return DriverManager.getConnection(jdbcUrl, dbUser, "");
                    } catch (SQLException fallbackException) {
                        lastException = fallbackException;
                    }
                }
            }
        }

        throw withCredentialHint(lastException == null ? new SQLException("Database connection failed.") : lastException);
    }

    private List<String> getCandidateJdbcUrls() {
        List<String> urls = new ArrayList<>();
        urls.add(buildJdbcUrl(dbPort));

        if (!explicitHostOrPort) {
            if (!"3307".equals(dbPort)) {
                urls.add(buildJdbcUrl("3307"));
            }
            if (!"3306".equals(dbPort)) {
                urls.add(buildJdbcUrl("3306"));
            }
        }

        return urls;
    }

    private String buildJdbcUrl(String port) {
        return "jdbc:mysql://" + dbHost + ":" + port + "/" + dbName
                + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    }

    private boolean shouldRetryWithEmptyPassword(SQLException exception) {
        if (explicitCredentials || !"root".equals(dbUser) || dbPassword.isEmpty()) {
            return false;
        }

        String sqlState = exception.getSQLState();
        String message = exception.getMessage();
        boolean accessDenied = "28000".equals(sqlState)
                || (message != null && message.toLowerCase().contains("access denied"));
        return accessDenied;
    }

    private SQLException withCredentialHint(SQLException cause) {
        String message = cause.getMessage();
        String hint = " Configure TT_DB_USER and TT_DB_PASS environment variables for your MySQL user.";

        if (message == null || message.trim().isEmpty()) {
            return new SQLException("Database connection failed." + hint, cause.getSQLState(), cause.getErrorCode(),
                    cause);
        }

        if (message.contains("TT_DB_USER") || message.contains("TT_DB_PASS")) {
            return cause;
        }

        return new SQLException(message + hint, cause.getSQLState(), cause.getErrorCode(), cause);
    }
}
