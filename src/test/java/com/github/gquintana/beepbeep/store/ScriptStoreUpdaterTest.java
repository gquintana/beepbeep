package com.github.gquintana.beepbeep.store;

import com.github.gquintana.beepbeep.BeepBeepException;
import com.github.gquintana.beepbeep.TestConsumer;
import com.github.gquintana.beepbeep.TestFiles;
import com.github.gquintana.beepbeep.pipeline.ScriptEndEvent;
import com.github.gquintana.beepbeep.pipeline.ScriptEvent;
import com.github.gquintana.beepbeep.pipeline.ScriptStartEvent;
import com.github.gquintana.beepbeep.script.ResourceScript;
import com.github.gquintana.beepbeep.script.Script;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

public class ScriptStoreUpdaterTest {
    private TestConsumer<ScriptEvent> output;
    private MemoryScriptStore store;
    private ScriptStoreUpdater<Integer> updater;
    private Script script;

    @Before
    public void setUp() {
        output = new TestConsumer<>();
        store = new MemoryScriptStore();
        updater = new ScriptStoreUpdater<>(store, output);
        script = ResourceScript.create(TestFiles.class, "sql/init/01_create.sql");
    }

    private void storeScript(Instant start, Instant end, ScriptStatus status) {
        store.create(new ScriptInfo<>(null, null, script.getFullName(), script.getSize(), script.getSha1Hex(),
            start, end, status));
    }


    @Test
    public void testTransform_StartCreate() throws Exception {
        // Given
        // When
        updater.consume(new ScriptStartEvent(script));
        // Then
        assertThat(output.events).hasSize(1);
        ScriptInfo<Integer> info = store.getByFullName(script.getFullName());
        assertThat(info).isNotNull();
        assertThat(info.getFullName()).isEqualTo(script.getFullName());
        assertThat(info.getVersion()).isEqualTo(1);
        assertThat(info.getStatus()).isEqualTo(ScriptStatus.STARTED);
        assertThat(info.getStartDate()).isNotNull();
        assertThat(info.getEndDate()).isNull();
    }

    @Test
    public void testTransform_StartUpdate() throws Exception {
        // Given
        storeScript(Instant.now().minusSeconds(300L), null, ScriptStatus.STARTED);
        // When
        updater.consume(new ScriptStartEvent(script));
        // Then
        assertThat(output.events).hasSize(1);
        ScriptInfo<Integer> info = store.getByFullName(script.getFullName());
        assertThat(info).isNotNull();
        assertThat(info.getFullName()).isEqualTo(script.getFullName());
        assertThat(info.getVersion()).isEqualTo(2);
        assertThat(info.getStatus()).isEqualTo(ScriptStatus.STARTED);
        assertThat(info.getStartDate()).isNotNull();
        assertThat(info.getStartDate().isAfter(Instant.now().minusSeconds(5))).isTrue();
        assertThat(info.getEndDate()).isNull();

    }

    @Test
    public void testTransform_EndSuccessUpdate() throws Exception {
        // Given
        Instant start = Instant.now();
        storeScript(start.minusSeconds(10L), null, ScriptStatus.STARTED);
        // When
        updater.consume(new ScriptEndEvent(script, 12, start));
        // Then
        assertThat(output.events).hasSize(1);
        ScriptInfo<Integer> info = store.getByFullName(script.getFullName());
        assertThat(info).isNotNull();
        assertThat(info.getFullName()).isEqualTo(script.getFullName());
        assertThat(info.getVersion()).isEqualTo(2);
        assertThat(info.getStatus()).isEqualTo(ScriptStatus.SUCCEEDED);
        assertThat(info.getStartDate()).isNotNull();
        assertThat(info.getEndDate()).isNotNull();

    }

    @Test
    public void testTransform_EndFailUpdate() throws Exception {
        // Given
        Instant start = Instant.now().minusSeconds(10L);
        storeScript(start, null, ScriptStatus.STARTED);
        // When
        updater.consume(new ScriptEndEvent(script, 12, new IllegalStateException(), start));
        // Then
        assertThat(output.events).hasSize(1);
        ScriptInfo<Integer> info = store.getByFullName(script.getFullName());
        assertThat(info).isNotNull();
        assertThat(info.getFullName()).isEqualTo(script.getFullName());
        assertThat(info.getVersion()).isEqualTo(2);
        assertThat(info.getStatus()).isEqualTo(ScriptStatus.FAILED);
        assertThat(info.getStartDate()).isNotNull();
        assertThat(info.getEndDate()).isNotNull();

    }
    @Test
    public void testTransform_EndWithoutStart() throws Exception {
        // Given
        // When
        try {
            updater.consume(new ScriptEndEvent(script, 12, new IllegalStateException(), Instant.now().minusMillis(10L)));
            fail("Exception expected");
        } catch (BeepBeepException e) {

        }
        // Then
        assertThat(output.events).isEmpty();
        ScriptInfo<Integer> info = store.getByFullName(script.getFullName());
        assertThat(info).isNull();

    }
}
