package com.github.gquintana.beepbeep.sql;

import com.github.gquintana.beepbeep.pipeline.Pipeline;
import com.github.gquintana.beepbeep.script.ScriptScanner;
import com.github.gquintana.beepbeep.store.ScriptStore;

public class SqlPipeline extends Pipeline {
    protected final SqlConnectionProvider connectionProvider;

    public SqlPipeline(ScriptStore scriptStore, ScriptScanner scriptScanner, SqlConnectionProvider connectionProvider) {
        super(scriptStore, scriptScanner);
        this.connectionProvider = connectionProvider;
    }

    @Override
    public void close() {
        try {
            connectionProvider.close();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
