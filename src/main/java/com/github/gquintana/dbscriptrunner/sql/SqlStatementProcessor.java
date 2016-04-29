package com.github.gquintana.dbscriptrunner.sql;

import com.github.gquintana.dbscriptrunner.pipeline.Consumer;
import com.github.gquintana.dbscriptrunner.pipeline.LineEvent;
import com.github.gquintana.dbscriptrunner.pipeline.Processor;
import com.github.gquintana.dbscriptrunner.pipeline.ScriptEvent;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SqlStatementProcessor extends Processor {
    private final SqlConnectionProvider connectionProvider;
    private final boolean autoCommit;
    private Connection connection;


    public SqlStatementProcessor(SqlConnectionProvider connectionProvider, boolean autoCommit, Consumer consumer) {
        super(consumer);
        this.connectionProvider = connectionProvider;
        this.autoCommit = autoCommit;
    }

    public SqlStatementProcessor(SqlConnectionProvider connectionProvider, Consumer consumer) {
        this(connectionProvider, true, consumer);
    }

    @Override
    public void consume(Object event) {
        if (event instanceof LineEvent) {
            executeLine((LineEvent) event);
        } else if (event instanceof ScriptEvent) {
            ScriptEvent scriptEvent = (ScriptEvent) event;
            switch (scriptEvent.getType()) {
                case START:
                    openConnection();
                    break;
                case END_SUCCESS:
                    closeConnection(true);
                    break;
                case END_FAILED:
                    closeConnection(false);
            }
            produce(event);
        } else {
            produce(event);
        }
    }

    private void closeConnection(boolean success) {
        try {
            if (!autoCommit) {
                if (success) {
                    connection.commit();
                } else {
                    connection.rollback();
                }
            }
            if (!connection.isClosed()) {
                connection.close();
            }
            connection = null;
        } catch (SQLException e) {
            throw new SqlScriptRunnerException("Failed to close connection", e);
        }
    }

    private void openConnection() {
        if (connection == null) {
            try {
                connection = connectionProvider.getConnection();
                connection.setAutoCommit(autoCommit);
            } catch (SQLException e) {
                throw new SqlScriptRunnerException("Failed to open connection", e);
            }
        }
    }

    private void executeLine(LineEvent lineEvent) {
        String line = lineEvent.getLine();
        openConnection();
        try (Statement statement = connection.createStatement()) {
            boolean resultSetResult = statement.execute(line);
            if (resultSetResult) {
                try (ResultSet resultSet = statement.getResultSet()) {
                    produce(resultSet);
                }
            } else {
                int updateCount = statement.getUpdateCount();
                if (updateCount >= 0) {
                    produce(updateCount + " updates");
                }
            }
        } catch (SQLException e) {
            produce(e.getMessage());
            throw new SqlScriptRunnerException("Failed to execute " + lineEvent, e);
        }
    }

    private void produce(ResultSet resultSet) throws SQLException {
        int columnCount = resultSet.getMetaData().getColumnCount();
        int rowIndex = 0;
        while (resultSet.next()) {
            StringBuilder lineBuilder = new StringBuilder(Integer.toString(rowIndex));
            for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
                lineBuilder.append(";").append(resultSet.getObject(columnIndex));
            }
            produce(lineBuilder.toString());
            rowIndex++;
        }
    }
}
