package com.github.gquintana.beepbeep.sql;

import com.github.gquintana.beepbeep.util.BaseInvocationHandler;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;

public class SingleSqlConnectionProvider implements SqlConnectionProvider {
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
        return new ConnectionInvocationHandler(connection).newProxy(Connection.class);
    }

    private static class ConnectionInvocationHandler extends BaseInvocationHandler<Connection> {
        public ConnectionInvocationHandler(Connection delegate) {
            super(delegate);
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getName().equals("close")) {
                return null;
            } else if (method.getName().equals("isWrapperFor") && args.length == 1 && Connection.class.equals(args[0])) {
                return true;
            } else if (method.getName().equals("unwrap") && args.length == 1 && Connection.class.equals(args[0])) {
                if (delegate.isWrapperFor(Connection.class)) {
                    return delegate.unwrap(Connection.class);
                } else {
                    return delegate;
                }
            } else {
                return delegate(method,args);
            }
        }
    }
}
