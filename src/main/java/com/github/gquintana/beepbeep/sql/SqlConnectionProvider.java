package com.github.gquintana.beepbeep.sql;

import java.sql.Connection;
import java.sql.SQLException;

public interface SqlConnectionProvider extends AutoCloseable {
    Connection getConnection() throws SQLException;
}
