package com.github.gquintana.beepbeep.script;

import com.github.gquintana.beepbeep.TestConsumer;
import com.github.gquintana.beepbeep.pipeline.ScriptStartEvent;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

import static com.github.gquintana.beepbeep.TestFiles.file;
import static com.github.gquintana.beepbeep.TestFiles.folder;
import static org.assertj.core.api.Assertions.assertThat;

public class FileScriptScannerGlobTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void testFileGlobRegex() throws IOException {
        assertThat(FileScriptScanner.fileGlobToRegex("*.sql")).isEqualTo("^[^/]*\\Q.sql\\E$");
        assertThat(FileScriptScanner.fileGlobToRegex("**/*.sql")).isEqualTo("^(?:.*/)?[^/]*\\Q.sql\\E$");
        assertThat(FileScriptScanner.fileGlobToRegex("**")).isEqualTo("^.*$");
        assertThat(FileScriptScanner.fileGlobToRegex("foo/*.sql")).isEqualTo("^\\Qfoo\\E/[^/]*\\Q.sql\\E$");
        assertThat(FileScriptScanner.fileGlobToRegex("foo/**/*.sql")).isEqualTo("^\\Qfoo\\E/(?:.*/)?[^/]*\\Q.sql\\E$");
        assertThat(FileScriptScanner.fileGlobToRegex("foo/**")).isEqualTo("^\\Qfoo\\E/.*$");
    }

    @Test
    public void testFileGlob() throws IOException {
        // Given
        File rootFolder =
            folder("root",
                folder("sub",
                    file("foo.sql"),
                    folder("inner",
                        file("bar.sql"),
                        file("bar.text"))),
                file("baz.sql")
            ).create(temporaryFolder.getRoot());
        // When Then
        checkFileFlobScan(rootFolder.getAbsolutePath() + File.separator + "*.sql", 1);
        checkFileFlobScan(rootFolder.getAbsolutePath() + File.separator + "**" + File.separator + "*.sql", 3);
        checkFileFlobScan(rootFolder.getAbsolutePath() + File.separator + "**", 4);
        checkFileFlobScan(rootFolder.getAbsolutePath() + File.separator + "sub" + File.separator + "*.sql", 1);
        checkFileFlobScan(rootFolder.getAbsolutePath() + File.separator + "sub" + File.separator + "**" + File.separator + "*.sql", 2);
        checkFileFlobScan(rootFolder.getAbsolutePath(), 4);
        checkFileFlobScan(rootFolder.getAbsolutePath()+ File.separator + "baz.sql", 1);
        checkFileFlobScan(rootFolder.getAbsolutePath() + File.separator + "**" + File.separator + "*.xml", 0);
        checkFileFlobScan(rootFolder.getAbsolutePath() + File.separator + "**" + File.separator + "ba*.sql", 2);
        checkFileFlobScan(rootFolder.getAbsolutePath() + File.separator + "**" + File.separator + "ba*", 3);
    }

    private void checkFileFlobScan(String fileGlob, int fileNb) throws IOException {
        TestConsumer<ScriptStartEvent> end = new TestConsumer<>();
        FileScriptScanner.fileGlob(fileGlob, end).scan();
        assertThat(end.events).hasSize(fileNb);
    }
}
