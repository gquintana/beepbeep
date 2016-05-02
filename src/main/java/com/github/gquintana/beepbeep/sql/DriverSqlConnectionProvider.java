package com.github.gquintana.beepbeep.sql;

import com.github.gquintana.beepbeep.BeepBeepException;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DriverSqlConnectionProvider implements SqlConnectionProvider {
    private final Class<? extends java.sql.Driver> driverClass;
    private final String url;
    private final String username;
    private final String password;

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

    @Override
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }
}
