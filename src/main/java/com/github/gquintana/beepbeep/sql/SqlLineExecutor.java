package com.github.gquintana.beepbeep.sql;

import com.github.gquintana.beepbeep.LineException;
import com.github.gquintana.beepbeep.pipeline.Consumer;
import com.github.gquintana.beepbeep.pipeline.LineEvent;
import com.github.gquintana.beepbeep.pipeline.LineExecutor;
import com.github.gquintana.beepbeep.pipeline.ScriptEvent;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SqlLineExecutor extends LineExecutor {
    private final SqlConnectionProvider connectionProvider;
    private final boolean autoCommit;
    private Connection connection;


    public SqlLineExecutor(SqlConnectionProvider connectionProvider, boolean autoCommit, Consumer<ScriptEvent> consumer) {
        super(consumer);
        this.connectionProvider = connectionProvider;
        this.autoCommit = autoCommit;
    }

    public SqlLineExecutor(SqlConnectionProvider connectionProvider, Consumer<ScriptEvent> consumer) {
        this(connectionProvider, true, consumer);
    }

    @Override
    protected void executeEnd(boolean success) {
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
            throw new SqlException("Failed to close connection", e);
        }
    }

    @Override
    protected void executeStart() {
        if (connection == null) {
            try {
                connection = connectionProvider.getConnection();
                connection.setAutoCommit(autoCommit);
            } catch (SQLException e) {
                throw new SqlException("Failed to open connection", e);
            }
        }
    }

    @Override
    protected void executeLine(LineEvent lineEvent) {
        String line = lineEvent.getLine();
        executeStart();
        try (Statement statement = connection.createStatement()) {
            boolean resultSetResult = statement.execute(line);
            if (resultSetResult) {
                try (ResultSet resultSet = statement.getResultSet()) {
                    produce(lineEvent, resultSet);
                }
            } else {
                int updateCount = statement.getUpdateCount();
                if (updateCount >= 0) {
                    produce(lineEvent, updateCount + " updates");
                }
            }
        } catch (SQLException e) {
            throw new LineException("SQL error ", e, lineEvent);
        }
    }

    private void produce(LineEvent lineEvent, ResultSet resultSet) throws SQLException {
        int columnCount = resultSet.getMetaData().getColumnCount();
        int rowIndex = 0;
        while (resultSet.next()) {
            StringBuilder lineBuilder = new StringBuilder(Integer.toString(rowIndex));
            for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
                lineBuilder.append(";").append(resultSet.getObject(columnIndex));
            }
            produce(lineEvent, lineBuilder.toString());
            rowIndex++;
        }
    }
}
