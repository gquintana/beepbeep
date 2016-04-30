package com.github.gquintana.beepbeep.sql;

import com.github.gquintana.beepbeep.DbScriptRunnerException;

import java.sql.SQLException;

public class SqlScriptRunnerException extends DbScriptRunnerException {
    public SqlScriptRunnerException(String message, SQLException cause) {
        super(message, cause);
    }
}
