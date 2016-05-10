package com.github.gquintana.beepbeep.store;

import com.github.gquintana.beepbeep.pipeline.*;

import java.time.Instant;

public class ScriptStoreUpdater<ID> extends Transformer<ScriptEvent, ScriptEvent> {
    private final ScriptStore<ID> store;

    public ScriptStoreUpdater(ScriptStore<ID> store, Consumer<ScriptEvent> consumer) {
        super(consumer);
        this.store = store;
    }

    @Override
    protected ScriptEvent transform(ScriptEvent event) {
        String fullName = event.getScript().getFullName();
        if (event instanceof ScriptStartEvent) {
            ScriptInfo<ID> info = store.getByFullName(fullName);
            boolean create = info == null;
            if (create) {
                info = new ScriptInfo<>();
                info.setFullName(fullName);
                info.setSize(event.getScript().getSize());
            }
            info.setStartDate(Instant.now());
            info.setEndDate(null);
            info.setStatus(ScriptStatus.STARTED);
            info.setSize(event.getScript().getSize());
            info.setSha1(event.getScript().getSha1Hex());
            if (create) {
                info = store.create(info);
            } else {
                info = store.update(info);
            }
        } else if (event instanceof ScriptEndEvent) {
            ScriptEndEvent endEvent = (ScriptEndEvent) event;
            ScriptInfo<ID> info = store.getByFullName(fullName);
            if (info == null) {
                throw new ScriptStoreException("Script end not in store " + event.getScript());
            }
            info.setEndDate(Instant.now());
            if (endEvent.isSuccess()) {
                info.setStatus(ScriptStatus.SUCCEEDED);
            } else {
                info.setStatus(ScriptStatus.FAILED);
            }
            info = store.update(info);
        }
        return event;
    }
}