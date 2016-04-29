package com.github.gquintana.dbscriptrunner.sql;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;

public class SingleSqlConnectionProvider implements SqlConnectionProvider, AutoCloseable {
    private final SqlConnectionProvider connectionProvider;
    private Connection connection;
    private Connection wrappedConnection;

    public SingleSqlConnectionProvider(SqlConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    @Override
    public Connection getConnection() throws SQLException {
        if (wrappedConnection == null) {
            connection = connectionProvider.getConnection();
            wrappedConnection = wrapConnection(connection);
        }
        return wrappedConnection;
    }

    @Override
    public void close() throws Exception {
        if (connection != null) {
            connection.close();
            connection = null;
            wrappedConnection = null;
        }
    }

    private Connection wrapConnection(final Connection connection) {
        return (Connection) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{Connection.class}, new ConnectionInvocationHandler(connection));
    }

    private static class ConnectionInvocationHandler implements InvocationHandler {
        private final Connection connection;

        public ConnectionInvocationHandler(Connection connection) {
            this.connection = connection;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getName().equals("close")) {
                return null;
            } else if (method.getName().equals("isWrapperFor") && args.length == 1 && Connection.class.equals(args[0])) {
                return true;
            } else if (method.getName().equals("unwrap") && args.length == 1 && Connection.class.equals(args[0])) {
                if (connection.isWrapperFor(Connection.class)) {
                    return connection.unwrap(Connection.class);
                } else {
                    return connection;
                }
            } else {
                return method.invoke(connection, args);
            }
        }
    }
}
