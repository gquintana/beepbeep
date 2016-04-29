package com.github.gquintana.dbscriptrunner.sql;

import java.sql.Connection;
import java.sql.SQLException;

public interface SqlConnectionProvider {
    Connection getConnection() throws SQLException;
}
