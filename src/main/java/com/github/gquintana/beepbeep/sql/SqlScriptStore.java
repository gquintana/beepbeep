package com.github.gquintana.beepbeep.sql;

import com.github.gquintana.beepbeep.store.ScriptStatus;
import com.github.gquintana.beepbeep.store.ScriptInfo;
import com.github.gquintana.beepbeep.store.ScriptStore;
import com.github.gquintana.beepbeep.store.ScriptStoreException;

import java.sql.*;
import java.time.Instant;

/**
 * Implementation of {@link ScriptStore} bases on JDBC.
 */
public class SqlScriptStore implements ScriptStore<Integer> {
    private final SqlConnectionProvider connectionProvider;
    private final String table;
    /**
     * Use sequence, not auto increment field
     */
    private final boolean sequence;

    public SqlScriptStore(SqlConnectionProvider connectionProvider, String table, boolean sequence) {
        this.connectionProvider = connectionProvider;
        this.table = table;
        this.sequence = sequence;
    }

    protected String getCreateTableSql() {
        return "CREATE TABLE " + table + " (" +
            "id INT PRIMARY KEY " + (sequence ? "" : "AUTO_INCREMENT") + ", " +
            "version INT NOT NULL DEFAULT 1, " +
            "full_name VARCHAR(512) NOT NULL, " +
            "size_bytes INT NOT NULL, " +
            "sha1 VARCHAR(128) NOT NULL, " +
            "start_date TIMESTAMP NOT NULL, " +
            "end_date TIMESTAMP," +
            "status VARCHAR(16)" +
            ")";
    }

    protected String getCreateUniqueIndexSql() {
        return "CREATE UNIQUE INDEX  " + table + "_full_name_idx ON " + table + "(full_name)";
    }

    protected String getCreateSequenceSql() {
        return "CREATE SEQUENCE " + table + "_seq";
    }

    protected String getSelectByFullNameSql() {
        return "SELECT * FROM " + table + " WHERE full_name=?";
    }

    protected String getUpdateSql() {
        return "UPDATE " + table +
            " SET version = version + 1, full_name = ?, size_bytes = ?, sha1 = ?," +
            " start_date = ?, end_date = ?, status = ?" +
            " WHERE id=? AND version = ?";
    }

    protected String getSelectSequenceSql() {
        return "SELECT nextval('" + table + "_seq')";
    }

    protected String getInsertSql() {
        return "INSERT INTO " + table +
            " (" + (sequence ? "id, " : "") +
            "version, full_name, size_bytes, sha1, start_date, end_date, status)" +
            " VALUES " +
            " (" + (sequence ? "?, " : "") +
            "1, ?, ?, ?, ?, ?, ?)";
    }

    protected boolean doesTableExist(Connection connection) throws SQLException {
        DatabaseMetaData md = connection.getMetaData();
        String t;
        if (md.storesLowerCaseIdentifiers()) {
            t = table.toLowerCase();
        } else if (md.storesUpperCaseIdentifiers()) {
            t = table.toUpperCase();
        } else {
            t = table;
        }
        try (ResultSet resultSet = md.getTables(null, null, t, null)) {
            return resultSet.next();
        }
    }

    public void prepare() {
        try (Connection connection = connectionProvider.getConnection()) {
            if (doesTableExist(connection)) {
                return;
            }
            try (Statement statement = connection.createStatement()) {
                statement.execute(getCreateTableSql());
                statement.execute(getCreateUniqueIndexSql());
                if (sequence) {
                    statement.execute(getCreateSequenceSql());
                }
            }
        } catch (SQLException e) {
            throw new ScriptStoreException("create table " + table + " failed", e);
        }
    }

    @Override
    public ScriptInfo<Integer> getByFullName(String fullName) {
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement statement = connection.prepareStatement(getSelectByFullNameSql())) {
            statement.setString(1, fullName);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return read(resultSet);
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            throw new ScriptStoreException("select from " + table + " failed", e);
        }
    }

    @Override
    public ScriptInfo<Integer> update(ScriptInfo<Integer> info) {
        try (Connection connection = connectionProvider.getConnection();
             PreparedStatement statement = connection.prepareStatement(getUpdateSql())) {
            int index = write(statement, info, 1);
            statement.setInt(index++, info.getId());
            statement.setInt(index++, info.getVersion());
            if (statement.executeUpdate() == 0) {
                throw new ScriptStoreException("Concurrent modification of script " + info.getFullName());
            } else {
                info.setVersion(info.getVersion() + 1);
            }
            commit(connection);
            return info;
        } catch (SQLException e) {
            throw new ScriptStoreException("update " + table + " failed", e);
        }
    }

    /**
     * Use sequence to generate Id
     */
    private int generateId(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            return getId(statement.executeQuery(getSelectSequenceSql()));
        }
    }

    @Override
    public ScriptInfo<Integer> create(ScriptInfo<Integer> info) {
        try (Connection connection = connectionProvider.getConnection()) {
            int index = 1;
            int id = 0;
            if (sequence) {
                id = generateId(connection);
                try (PreparedStatement statement = connection.prepareStatement(getInsertSql())) {
                    statement.setInt(index++, id);
                    write(statement, info, index);
                    statement.executeUpdate();
                }
            } else {
                try (PreparedStatement statement = connection.prepareStatement(getInsertSql(), PreparedStatement.RETURN_GENERATED_KEYS)) {
                    write(statement, info, index);
                    statement.executeUpdate();
                    id = getId(statement.getGeneratedKeys());
                }
            }
            info.setId(id);
            info.setVersion(1);
            commit(connection);
            return info;
        } catch (SQLException e) {
            throw new ScriptStoreException("insert into " + table + " failed", e);
        }
    }

    private int getId(ResultSet resultSet) throws SQLException {
        try {
            resultSet.next();
            return resultSet.getInt(1);
        } finally {
            resultSet.close();
        }
    }

    private static Instant getInstant(ResultSet resultSet, String columnName) throws SQLException {
        Timestamp t = resultSet.getTimestamp(columnName);
        return t == null || resultSet.wasNull() ? null : Instant.ofEpochMilli(t.getTime());
    }

    private static ScriptStatus getStatus(ResultSet resultSet, String columnName) throws SQLException {
        String s = resultSet.getString(columnName);
        return s == null || resultSet.wasNull() ? null : ScriptStatus.valueOf(s);
    }

    /**
     * Load resultset row into ScriptInfo object
     */
    private ScriptInfo<Integer> read(ResultSet resultSet) throws SQLException {
        return new ScriptInfo<>(
            resultSet.getInt("id"),
            resultSet.getInt("version"),
            resultSet.getString("full_name"),
            resultSet.getLong("size_bytes"),
            resultSet.getString("sha1"),
            getInstant(resultSet, "start_date"),
            getInstant(resultSet, "end_date"),
            getStatus(resultSet, "status")
        );
    }

    private int write(PreparedStatement statement, ScriptInfo<Integer> info, int index) throws SQLException {
        statement.setString(index++, info.getFullName());
        statement.setLong(index++, info.getSize());
        statement.setString(index++, info.getSha1());
        setInstant(statement, info.getStartDate(), index++);
        setInstant(statement, info.getEndDate(), index++);
        setStatus(statement, info.getStatus(), index++);
        return index;
    }

    private void setStatus(PreparedStatement statement, ScriptStatus status, int index) throws SQLException {
        if (status == null) {
            statement.setNull(index, Types.VARCHAR);
        } else {
            statement.setString(index, status.name());
        }
    }

    private void setInstant(PreparedStatement statement, Instant instant, int index) throws SQLException {
        if (instant == null) {
            statement.setNull(index, Types.TIMESTAMP);
        } else {
            statement.setTimestamp(index, new Timestamp(instant.toEpochMilli()));
        }
    }

    private void commit(Connection connection) throws SQLException {
        if (!connection.getAutoCommit()) {
            connection.commit();
        }
    }
}
