package com.github.gquintana.beepbeep.sql;

import com.github.gquintana.beepbeep.pipeline.Consumer;
import com.github.gquintana.beepbeep.pipeline.MultilineAggregator;
import com.github.gquintana.beepbeep.pipeline.PipelineBuilder;

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
        return withScriptStore(new SqlScriptStore(connectionProvider, name, true));
    }

    public Consumer build() {
        Consumer consumer = endConsumer;
        if (connectionProvider == null) {
            connectionProvider = DriverSqlConnectionProvider.create(url, username, password);
        }
        consumer = new SqlLineExecutor(connectionProvider, autoCommit, consumer);
        consumer = notNullNorEmptyFilter(consumer);
        consumer = new MultilineAggregator(endOfLineRegex, MultilineAggregator.LineMarkerStrategy.END, true, consumer);
        consumer = variableReplacer(consumer);
        consumer = scriptReader(consumer);
        return consumer;
    }
}
