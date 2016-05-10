package com.github.gquintana.beepbeep.sql;

import com.github.gquintana.beepbeep.BeepBeepException;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DriverSqlConnectionProvider implements SqlConnectionProvider {
    private final Class<? extends java.sql.Driver> driverClass;
    private final String url;
    private final String username;
    private final String password;
    private static final Pattern URL_PATTERN = Pattern.compile("^jdbc:([^:]+):.*");
    private static final Map<String, String> DRIVER_CLASS_BY_TYPE = createDriverClassByType();

    @SuppressWarnings("unchecked")
    public DriverSqlConnectionProvider(String driverClassName, String url, String username, String password) {
        try {
            this.driverClass = (Class<? extends Driver>) Class.forName(driverClassName);
        } catch (ClassNotFoundException e) {
            throw new BeepBeepException("Invalid SQL Driver " + driverClassName, e);
        }
        this.url = url;
        this.username = username;
        this.password = password;

    }

    public Class<? extends Driver> getDriverClass() {
        return driverClass;
    }

    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    private static Map<String, String> createDriverClassByType() {
        Map<String, String> driverClassByType = new HashMap<>();
        driverClassByType.put("h2", "org.h2.Driver");
        driverClassByType.put("hsqldb", "org.hsqldb.jdbc.JDBCDriver");
        driverClassByType.put("mysql", "com.mysql.jdbc.Driver");
        driverClassByType.put("oracle", "oracle.jdbc.driver.OracleDriver");
        driverClassByType.put("postgresql", "org.postgresql.Driver");
        driverClassByType.put("sqlserver", "com.microsoft.sqlserver.jdbc.SQLServerDriver");
        driverClassByType.put("derby", "org.apache.derby.jdbc.ClientDriver");
        driverClassByType.put("sybase", "com.sybase.jdbc.SybDriver");
        return driverClassByType;
    }

    public static DriverSqlConnectionProvider create(String url, String username, String password) {
        Matcher urlMatcher = URL_PATTERN.matcher(url);
        String driverClassName = null;
        if (urlMatcher.matches()) {
            String type = urlMatcher.group(1);
            driverClassName = DRIVER_CLASS_BY_TYPE.get(type);
        }
        if (driverClassName == null) {
            throw new IllegalArgumentException("Unrecognized database type");
        }
        return new DriverSqlConnectionProvider(driverClassName, url, username, password);
    }
}
