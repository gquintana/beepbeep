package com.github.gquintana.beepbeep.script;

import com.github.gquintana.beepbeep.TestConsumer;
import com.github.gquintana.beepbeep.pipeline.ScriptStartEvent;
import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class ResourceScriptScannerGlobTest {
    @Test
    public void testFileGlob() throws IOException {
        checkResourceGlobScan("*.sql", 1);
        checkResourceGlobScan("**/*.sql", 5);
        checkResourceGlobScan("com/github/gquintana/**/*.sql", 4);
        checkResourceGlobScan("com/github/gquintana/beepbeep/script/*.sql", 3);
    }

    private void checkResourceGlobScan(String fileGlob, int fileNb) throws IOException {
        TestConsumer<ScriptStartEvent> end = new TestConsumer<>();
        ResourceScriptScanner.resourceGlob(getClass().getClassLoader(), fileGlob, end).scan();
        assertThat(end.events).hasSize(fileNb);
    }

}
