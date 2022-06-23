package com.github.gquintana.beepbeep.script;

import com.github.gquintana.beepbeep.TestConsumer;
import com.github.gquintana.beepbeep.TestFiles;
import com.github.gquintana.beepbeep.pipeline.ScriptStartEvent;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
public class SingleScriptScannerTest {

    @Test
    public void testScan() {
        // Given
        TestConsumer<ScriptStartEvent> end = new TestConsumer<>();
        SingleScriptScanner scanner = ScriptScanners.resource(TestFiles.class, "script/script_create.sql", end);
        // When
        scanner.scan();
        // Then
        assertThat(end.events).hasSize(1);
        assertThat(end.scriptStream(Script.class).findFirst().get().getName()).isEqualTo("script_create.sql");

    }
}
