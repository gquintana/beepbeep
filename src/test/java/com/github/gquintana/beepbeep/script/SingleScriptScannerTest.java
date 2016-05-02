package com.github.gquintana.beepbeep.script;

import com.github.gquintana.beepbeep.TestConsumer;
import com.github.gquintana.beepbeep.TestFiles;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
public class SingleScriptScannerTest {

    @Test
    public void testScan() throws Exception {
        // Given
        TestConsumer end = new TestConsumer();
        SingleScriptScanner scanner = ScriptScanners.resource(TestFiles.class, "script/script_create.sql", end);
        // When
        scanner.scan();
        // Then
        assertThat(end.events).hasSize(1);
        assertThat(end.events(Script.class).get(0).getName()).isEqualTo("script_create.sql");

    }
}
