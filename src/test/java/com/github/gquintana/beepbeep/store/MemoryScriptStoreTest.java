package com.github.gquintana.beepbeep.store;

import org.junit.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class MemoryScriptStoreTest {

    private ScriptInfo<Integer> createInfo() {
        ScriptInfo<Integer> info = new ScriptInfo<>();
        info.setFullName("/test/script.sql");
        info.setSize(421);
        info.setSha1("123abc");
        info.setStatus(ScriptStatus.STARTED);
        info.setStartDate(Instant.now());
        return info;
    }

    @Test
    public void testCreate() throws Exception {
        // Given
        ScriptInfo<Integer> info = createInfo();
        MemoryScriptStore store = new MemoryScriptStore();
        // When
        info = store.create(info);
        // Then
        assertThat(info.getId()).isEqualTo(1);
        assertThat(info.getVersion()).isEqualTo(1);
        assertThat(store.getByFullName(info.getFullName())).isNotNull();
    }

    @Test(expected = ScriptStoreException.class)
    public void testCreate_AlreadyExists() throws Exception {
        // Given
        ScriptInfo<Integer> info = createInfo();
        MemoryScriptStore store = new MemoryScriptStore();
        info = store.create(info);
        // When
        info = store.create(info);
    }

    @Test
    public void testUpdate() throws Exception {
        // Given
        ScriptInfo<Integer> info = createInfo();
        MemoryScriptStore store = new MemoryScriptStore();
        info = store.create(info);
        info.setStatus(ScriptStatus.SUCCEEDED);
        info.setEndDate(Instant.now().plus(10, ChronoUnit.SECONDS));
        // When
        info = store.update(info);
        // Then
        assertThat(info.getId()).isEqualTo(1);
        assertThat(info.getVersion()).isEqualTo(2);
        ScriptInfo<Integer> inStore = store.getByFullName(info.getFullName());
        assertThat(inStore.getStatus()).isEqualTo(ScriptStatus.SUCCEEDED);
        assertThat(inStore.getEndDate()).isNotNull();
    }

    @Test(expected = ScriptStoreException.class)
    public void testUpdate_NotFound() throws Exception {
        // Given
        ScriptInfo<Integer> info = createInfo();
        MemoryScriptStore store = new MemoryScriptStore();
        info.setStatus(ScriptStatus.SUCCEEDED);
        info.setEndDate(Instant.now().plus(10, ChronoUnit.SECONDS));
        // When
        info = store.update(info);
    }

    @Test(expected = ScriptStoreException.class)
    public void testUpdate_ConcurrentModification() throws Exception {
        // Given
        ScriptInfo<Integer> info = createInfo();
        MemoryScriptStore store = new MemoryScriptStore();
        info = store.create(info);
        info.setStatus(ScriptStatus.SUCCEEDED);
        info.setEndDate(Instant.now().plus(10, ChronoUnit.SECONDS));
        int version = info.getVersion();
        info = store.update(info);
        info.setVersion(version);
        info.setStatus(ScriptStatus.FAILED);
        info.setEndDate(Instant.now().plus(10, ChronoUnit.SECONDS));
        // When
        info = store.update(info);
    }
}
