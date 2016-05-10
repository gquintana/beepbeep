package com.github.gquintana.beepbeep.script;

import com.github.gquintana.beepbeep.TestConsumer;
import com.github.gquintana.beepbeep.pipeline.ScriptStartEvent;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CompositeScriptScannerTest {

    @Test
    public void testScan() throws Exception {
        // Given
        TestConsumer<ScriptStartEvent> end = new TestConsumer<>();
        final String folder = getClass().getPackage().getName().replaceAll("\\.", "/");
        CompositeScriptScanner scanner = ScriptScanners.composite(end)
            .resources(getClass().getClassLoader(),
                (String r) -> r.endsWith(".sql") && r.startsWith(folder))
            .resource(getClass().getClassLoader(), folder+"/"+"script_not_found.sql");
        // When
        scanner.scan();
        // Then
        assertThat(end.events).hasSize(4);
    }
}
