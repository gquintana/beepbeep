package com.github.gquintana.beepbeep.sql;

import com.github.gquintana.beepbeep.pipeline.Consumer;
import com.github.gquintana.beepbeep.pipeline.MultilineAggregator;
import com.github.gquintana.beepbeep.pipeline.PipelineBuilder;
import com.github.gquintana.beepbeep.pipeline.ScriptReaderProducer;

import java.util.regex.Pattern;

public class SqlPipelineBuilder extends PipelineBuilder<SqlPipelineBuilder> {
    private SqlConnectionProvider connectionProvider;
    private boolean autoCommit = true;
    private String endOfLineRegex = ";\\s*$";

    public SqlPipelineBuilder withConnectionProvider(String driverClass, String url, String username, String password) {
        this.connectionProvider = new DriverSqlConnectionProvider(driverClass, url, username, password);
        return this;
    }

    public SqlPipelineBuilder withManualCommit() {
        this.autoCommit = false;
        return this;
    }

    public SqlPipelineBuilder withEndOfLineRegex(String regex) {
        this.endOfLineRegex = regex;
        return this;
    }

    public SqlPipelineBuilder withEndOfLineMarker(String marker) {
        this.endOfLineRegex = Pattern.quote(marker) + "\\s*$";
        return this;
    }

    public Consumer build() {
        Consumer consumer = endConsumer;
        consumer = new SqlStatementProcessor(connectionProvider, autoCommit, consumer);
        consumer = notNullNorEmptyFilter(consumer);
        consumer = new MultilineAggregator(endOfLineRegex, MultilineAggregator.LineMarkerStrategy.END, true, consumer);
        consumer = variableReplacer(consumer);
        consumer = scriptReader(consumer);
        return consumer;
    }
}
