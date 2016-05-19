package com.github.gquintana.beepbeep.store;

import com.github.gquintana.beepbeep.BeepBeepException;
import com.github.gquintana.beepbeep.pipeline.Consumer;
import com.github.gquintana.beepbeep.pipeline.Filter;
import com.github.gquintana.beepbeep.pipeline.ScriptStartEvent;
import com.github.gquintana.beepbeep.script.Script;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.TemporalAmount;
import java.util.EnumSet;

/**
 * Skip script execution when marked SUCCEEDED in ScriptStore,
 * detect script modifications.
 */
public class ScriptStoreFilter extends Filter<ScriptStartEvent> {
    private final ScriptStore store;
    /**
     * Re run script when it is modified
     */
    private boolean reRunChanged;
    /**
     * Re run script when it previously failed
     */
    private boolean reRunFailed = true;
    /**
     * Re run script when it's stuck in started state after given timeout
     */
    private TemporalAmount reRunStartedTimeout = Duration.ofMinutes(1);

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
        boolean changed = !(script.getSize() == info.getSize() && script.getSha1Hex().equals(info.getSha1()));
        if (changed) {
            if (!reRunChanged) {
                throw new ScriptStoreException("Script " + info.getFullName() + " changed. Fix script store to run it again");
            }
            return EnumSet.of(ScriptStatus.SUCCEEDED, ScriptStatus.FAILED).contains(info.getStatus())
                || (info.getStatus() == ScriptStatus.STARTED && isStartedTimeoutExpired(info, now));
        } else {
            return (info.getStatus() == ScriptStatus.FAILED && reRunFailed)
                || (info.getStatus() == ScriptStatus.STARTED && isStartedTimeoutExpired(info, now));
        }
    }

    private boolean isStartedTimeoutExpired(ScriptInfo info, Instant now) {
        boolean timeoutExpired = reRunStartedTimeout != null && info.getStartDate().plus(reRunStartedTimeout).isBefore(now);
        if (!timeoutExpired) {
            throw new ScriptStoreException("Script " + info.getFullName() + " already started");
        }
        return timeoutExpired;
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
