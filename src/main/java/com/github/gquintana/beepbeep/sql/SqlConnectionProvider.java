package com.github.gquintana.beepbeep.sql;

import java.sql.Connection;
import java.sql.SQLException;

public interface SqlConnectionProvider {
    Connection getConnection() throws SQLException;
}
