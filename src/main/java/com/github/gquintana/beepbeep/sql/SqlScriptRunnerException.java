package com.github.gquintana.beepbeep.sql;

import com.github.gquintana.beepbeep.BeepBeepException;

import java.sql.SQLException;

public class SqlScriptRunnerException extends BeepBeepException {
    public SqlScriptRunnerException(String message, SQLException cause) {
        super(message, cause);
    }
}
