package com.github.gquintana.beepbeep.store;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
    public void testCreate() {
        // Given
        ScriptInfo<Integer> info = createInfo();
        MemoryScriptStore store = new MemoryScriptStore();
        // When
        info = store.create(info);
        // Then
        assertThat(info.getId()).isEqualTo(1);
        assertThat(info.getVersion()).isEqualTo("1");
        assertThat(store.getByFullName(info.getFullName())).isNotNull();
    }

    @Test

    public void testCreate_AlreadyExists() {
        // Given
        ScriptInfo<Integer> info = createInfo();
        MemoryScriptStore store = new MemoryScriptStore();
        ScriptInfo<Integer> info2 = store.create(info);
        // When
        assertThatThrownBy(() -> store.create(info2));
    }

    @Test
    public void testUpdate() {
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
        assertThat(info.getVersionAsInt()).isEqualTo(2);
        ScriptInfo<Integer> inStore = store.getByFullName(info.getFullName());
        assertThat(inStore.getStatus()).isEqualTo(ScriptStatus.SUCCEEDED);
        assertThat(inStore.getEndDate()).isNotNull();
    }

    @Test
    public void testUpdate_NotFound() {
        // Given
        ScriptInfo<Integer> info = createInfo();
        MemoryScriptStore store = new MemoryScriptStore();
        info.setStatus(ScriptStatus.SUCCEEDED);
        info.setEndDate(Instant.now().plus(10, ChronoUnit.SECONDS));
        // When
        assertThatThrownBy(() -> store.update(info)).isInstanceOf(ScriptStoreException.class);
    }

    @Test
    public void testUpdate_ConcurrentModification() {
        // Given
        ScriptInfo<Integer> info = createInfo();
        MemoryScriptStore store = new MemoryScriptStore();
        info = store.create(info);
        info.setStatus(ScriptStatus.SUCCEEDED);
        info.setEndDate(Instant.now().plus(10, ChronoUnit.SECONDS));
        int version = info.getVersionAsInt();
        info = store.update(info);
        info.setVersion(version);
        info.setStatus(ScriptStatus.FAILED);
        info.setEndDate(Instant.now().plus(10, ChronoUnit.SECONDS));
        ScriptInfo<Integer> info2 = info;
        // When
        assertThatThrownBy(() -> store.update(info2)).isInstanceOf(ScriptStoreException.class);
    }
}
