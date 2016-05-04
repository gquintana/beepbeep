package com.github.gquintana.beepbeep.sql;

import com.github.gquintana.beepbeep.LineException;
import com.github.gquintana.beepbeep.pipeline.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SqlLineExecutor extends LineExecutor {
    private final SqlConnectionProvider connectionProvider;
    private final boolean autoCommit;
    private Connection connection;


    public SqlLineExecutor(SqlConnectionProvider connectionProvider, boolean autoCommit, Consumer consumer) {
        super(consumer);
        this.connectionProvider = connectionProvider;
        this.autoCommit = autoCommit;
    }

    public SqlLineExecutor(SqlConnectionProvider connectionProvider, Consumer consumer) {
        this(connectionProvider, true, consumer);
    }

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

    protected void executeLine(LineEvent lineEvent) {
        String line = lineEvent.getLine();
        executeStart();
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
            throw new LineException("SQL error ", e, lineEvent);
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
