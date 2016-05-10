package com.github.gquintana.beepbeep.store;

import com.github.gquintana.beepbeep.pipeline.Consumer;
import com.github.gquintana.beepbeep.pipeline.Filter;
import com.github.gquintana.beepbeep.pipeline.ScriptStartEvent;
import com.github.gquintana.beepbeep.script.Script;

import java.time.Instant;
import java.time.temporal.TemporalAmount;

public class ScriptStoreFilter extends Filter<ScriptStartEvent> {
    private final ScriptStore store;
    /**
     * Re run script when it is modified
     */
    private boolean reRunChanged;
    /**
     * Re run script when its previous run failed
     */
    private boolean reRunFailed;
    /**
     * Re run script when it's stuck in started state after given timeout
     */
    private TemporalAmount reRunStartedTimeout;

    public ScriptStoreFilter(ScriptStore store, Consumer<ScriptStartEvent> consumer) {
        super(consumer);
        this.store = store;
    }

    @Override
    protected boolean filter(ScriptStartEvent event) {
        Script script = event.getScript();
        ScriptInfo info = store.getByFullName(script.getFullName());
        if (info == null || info.getStatus() == null) {
            return true;
        }
        Instant now = Instant.now();
        boolean changed = ! (script.getSize() == info.getSize() && script.getSha1Hex().equals(info.getSha1()));
        switch (info.getStatus()) {
            case SUCCEEDED:
                return changed && reRunChanged;
            case FAILED:
                return reRunFailed || (changed && reRunChanged);
            case STARTED:
                return reRunStartedTimeout != null && info.getStartDate().plus(reRunStartedTimeout).isBefore(now);
        }
        return false;
    }

    public ScriptStore getStore() {
        return store;
    }

    public boolean isReRunChanged() {
        return reRunChanged;
    }

    public void setReRunChanged(boolean reRunChanged) {
        this.reRunChanged = reRunChanged;
    }

    public boolean isReRunFailed() {
        return reRunFailed;
    }

    public void setReRunFailed(boolean reRunFailed) {
        this.reRunFailed = reRunFailed;
    }

    public TemporalAmount getReRunStartedTimeout() {
        return reRunStartedTimeout;
    }

    public void setReRunStartedTimeout(TemporalAmount reRunStartedTimeout) {
        this.reRunStartedTimeout = reRunStartedTimeout;
    }
}
