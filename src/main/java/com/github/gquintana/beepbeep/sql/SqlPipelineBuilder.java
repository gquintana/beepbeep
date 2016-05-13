package com.github.gquintana.beepbeep.sql;

import com.github.gquintana.beepbeep.pipeline.*;

import java.util.regex.Pattern;

public class SqlPipelineBuilder extends PipelineBuilder<SqlPipelineBuilder> {
    private SqlConnectionProvider connectionProvider;
    private boolean autoCommit = true;
    private String endOfLineRegex = ";\\s*$";

    public SqlPipelineBuilder withConnectionProvider(SqlConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
        return self();
    }

    public SqlPipelineBuilder withConnectionProvider(String driverClass, String url, String username, String password) {
        return withConnectionProvider(new DriverSqlConnectionProvider(driverClass, url, username, password));
    }

    public SqlPipelineBuilder withManualCommit() {
        this.autoCommit = false;
        return self();
    }

    public SqlPipelineBuilder withEndOfLineRegex(String regex) {
        this.endOfLineRegex = regex;
        return self();
    }

    public SqlPipelineBuilder withEndOfLineMarker(String marker) {
        this.endOfLineRegex = Pattern.quote(marker) + "\\s*$";
        return self();
    }

    @Override
    public SqlPipelineBuilder withScriptStore(String name) {
        return withScriptStore(new SqlScriptStore(getConnectionProvider(), name, true));
    }

    @Override
    public  Consumer<ScriptStartEvent> createConsumers() {
        Consumer<ScriptEvent> consumer = endConsumer;
        consumer = new SqlLineExecutor(getConnectionProvider(), autoCommit, consumer);
        consumer = notNullNorEmptyFilter(consumer);
        consumer = new MultilineAggregator(endOfLineRegex, MultilineAggregator.LineMarkerStrategy.END, true, consumer);
        consumer = variableReplacer(consumer);
        return scriptReader(consumer);
    }

    protected SqlConnectionProvider getConnectionProvider() {
        return connectionProvider == null ?
            DriverSqlConnectionProvider.create(url, username, password) :
            connectionProvider;
    }
}
