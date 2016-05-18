package com.github.gquintana.beepbeep.script;

import com.github.gquintana.beepbeep.TestConsumer;
import com.github.gquintana.beepbeep.TestFiles;
import com.github.gquintana.beepbeep.pipeline.ScriptStartEvent;
import org.junit.Test;

import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;

public class CompositeScriptScannerTest {

    @Test
    public void testScan() throws Exception {
        // Given
        TestConsumer<ScriptStartEvent> end = new TestConsumer<>();
        final String folder = TestFiles.getResourceFullName("sql/init/");
        Predicate<String> resourceFilter = (String r) -> r.endsWith(".sql") && r.startsWith(folder);
        CompositeScriptScanner.Builder composite = ScriptScanners.composite()
            .resources(getClass().getClassLoader(), resourceFilter);
        CompositeScriptScanner scanner = composite
            .resource(getClass(), "sql/script_not_found.sql")
            .build(end);
        // When
        scanner.scan();
        // Then
        assertThat(end.events).hasSize(3);
    }
}
