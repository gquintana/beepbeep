package com.github.gquintana.beepbeep.sql;

import com.github.gquintana.beepbeep.BeepBeepException;

import java.sql.SQLException;

/**
 * SQL Runtime exception
 */
public class SqlException extends BeepBeepException {
    public SqlException(String message, SQLException cause) {
        super(message, cause);
    }
}
