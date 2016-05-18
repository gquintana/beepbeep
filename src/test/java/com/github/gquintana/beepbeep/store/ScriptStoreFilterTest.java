package com.github.gquintana.beepbeep.store;

import com.github.gquintana.beepbeep.TestConsumer;
import com.github.gquintana.beepbeep.TestFiles;
import com.github.gquintana.beepbeep.pipeline.ScriptStartEvent;
import com.github.gquintana.beepbeep.script.ResourceScript;
import com.github.gquintana.beepbeep.script.Script;
import org.junit.Before;
import org.junit.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class ScriptStoreFilterTest {

    private TestConsumer<ScriptStartEvent> output;
    private MemoryScriptStore store;
    private ScriptStoreFilter filter;
    private Script script;

    @Before
    public void setUp() {
        output = new TestConsumer<>();
        store = new MemoryScriptStore();
        filter = new ScriptStoreFilter(store, output);
        script = ResourceScript.create(TestFiles.class, "sql/init/01_create.sql");
    }

    private void storeScript(Instant start, Instant end, ScriptStatus status) {
        store.create(new ScriptInfo<>(null, null, script.getFullName(), script.getSize(), script.getSha1Hex(),
            start, end, status));
    }

    private void storeScriptChanged(Instant start, Instant end, ScriptStatus status) {
        store.create(new ScriptInfo<>(null, null, script.getFullName(), script.getSize() + 12, script.getSha1Hex(),
            start, end, status));
    }

    @Test
    public void testFilter_RunFirst() {
        // Given
        // When
        filter.consume(new ScriptStartEvent(script));
        // Then
        assertThat(output.events).hasSize(1);
    }

    @Test
    public void testFilter_DontReRunSameWhenSucceeded() {
        // Given
        Instant now = Instant.now();
        storeScript(now, now.plusSeconds(10), ScriptStatus.SUCCEEDED);
        // When
        filter.consume(new ScriptStartEvent(script));
        // Then
        assertThat(output.events).isEmpty();
    }

    @Test
    public void testFilter_DontReRunSameWhenFailed() {
        // Given
        Instant now = Instant.now();
        storeScript(now, now.plusSeconds(10), ScriptStatus.FAILED);
        // When
        filter.consume(new ScriptStartEvent(script));
        // Then
        assertThat(output.events).isEmpty();
    }


    @Test
    public void testFilter_ReRunSameWhenFailedAndFlag() {
        // Given
        Instant now = Instant.now();
        storeScript(now, now.plusSeconds(10), ScriptStatus.FAILED);
        filter.setReRunFailed(true);
        // When
        filter.consume(new ScriptStartEvent(script));
        // Then
        assertThat(output.events).hasSize(1);
    }

    @Test
    public void testFilter_DontReRunSameWhenStarted() {
        // Given
        Instant now = Instant.now();
        storeScript(now, null, ScriptStatus.STARTED);
        filter.setReRunStartedTimeout(Duration.ofMinutes(1));
        // When
        filter.consume(new ScriptStartEvent(script));
        // Then
        assertThat(output.events).isEmpty();
    }

    @Test
    public void testFilter_ReRunSameWhenStartedButTimeout() {
        // Given
        Instant now = Instant.now().minus(30, ChronoUnit.MINUTES);
        storeScript(now, null, ScriptStatus.STARTED);
        filter.setReRunStartedTimeout(Duration.ofMinutes(1));
        // When
        filter.consume(new ScriptStartEvent(script));
        // Then
        assertThat(output.events).hasSize(1);
    }

    @Test
    public void testFilter_DontReRunChangedWhenSucceeded() {
        // Given
        Instant now = Instant.now();
        storeScriptChanged(now, now.plusSeconds(10), ScriptStatus.SUCCEEDED);
        // When
        filter.consume(new ScriptStartEvent(script));
        // Then
        assertThat(output.events).isEmpty();
    }

    @Test
    public void testFilter_ReRunChangedSucceededAndFlag() {
        // Given
        Instant now = Instant.now();
        storeScriptChanged(now, now.plusSeconds(10), ScriptStatus.SUCCEEDED);
        filter.setReRunChanged(true);
        // When
        filter.consume(new ScriptStartEvent(script));
        // Then
        assertThat(output.events).hasSize(1);
    }

    @Test
    public void testFilter_DontReRunChangedWhenFailed() {
        // Given
        Instant now = Instant.now();
        storeScriptChanged(now, now.plusSeconds(10), ScriptStatus.FAILED);
        // When
        filter.consume(new ScriptStartEvent(script));
        // Then
        assertThat(output.events).isEmpty();
    }


    @Test
    public void testFilter_ReRunChangedWhenFailedAndFlag() {
        // Given
        Instant now = Instant.now();
        storeScriptChanged(now, now.plusSeconds(10), ScriptStatus.FAILED);
        filter.setReRunFailed(true);
        // When
        filter.consume(new ScriptStartEvent(script));
        // Then
        assertThat(output.events).hasSize(1);
    }

    @Test
    public void testFilter_DontReRunChangedWhenStarted() {
        // Given
        Instant now = Instant.now();
        storeScriptChanged(now, null, ScriptStatus.STARTED);
        filter.setReRunStartedTimeout(Duration.ofMinutes(1));
        // When
        filter.consume(new ScriptStartEvent(script));
        // Then
        assertThat(output.events).isEmpty();
    }

    @Test
    public void testFilter_ReRunChangedWhenStartedButTimeout() {
        // Given
        Instant now = Instant.now().minus(30, ChronoUnit.MINUTES);
        storeScriptChanged(now, null, ScriptStatus.STARTED);
        filter.setReRunStartedTimeout(Duration.ofMinutes(1));
        // When
        filter.consume(new ScriptStartEvent(script));
        // Then
        assertThat(output.events).hasSize(1);
    }


}
