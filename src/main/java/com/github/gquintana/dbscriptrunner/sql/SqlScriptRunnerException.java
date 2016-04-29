package com.github.gquintana.dbscriptrunner.sql;

import com.github.gquintana.dbscriptrunner.DbScriptRunnerException;

import java.sql.SQLException;

public class SqlScriptRunnerException extends DbScriptRunnerException {
    public SqlScriptRunnerException(String message, SQLException cause) {
        super(message, cause);
    }
}
