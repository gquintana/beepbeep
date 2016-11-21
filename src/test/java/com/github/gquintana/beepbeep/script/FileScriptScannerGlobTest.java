package com.github.gquintana.beepbeep.script;

import com.github.gquintana.beepbeep.TestConsumer;
import com.github.gquintana.beepbeep.TestFiles;
import com.github.gquintana.beepbeep.pipeline.ScriptStartEvent;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.gquintana.beepbeep.TestFiles.*;
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
    public void testFileGlob_Absolute() throws IOException {
        // Given
        File rootFolder = createFiles(temporaryFolder.getRoot());
        // When Then
        checkFileFlobScan(rootFolder.getAbsolutePath() + "/*.sql", 1);
        checkFileFlobScan(rootFolder.getAbsolutePath() + "/**/*.sql", 3);
        checkFileFlobScan(rootFolder.getAbsolutePath() + "/**", 4);
        checkFileFlobScan(rootFolder.getAbsolutePath() + "/sub/*.sql", 1);
        checkFileFlobScan(rootFolder.getAbsolutePath() + "/sub/**/*.sql", 2);
        checkFileFlobScan(rootFolder.getAbsolutePath(), 4);
        checkFileFlobScan(rootFolder.getAbsolutePath() + "/baz.sql", 1);
        checkFileFlobScan(rootFolder.getAbsolutePath() + "/**/*.xml", 0);
        checkFileFlobScan(rootFolder.getAbsolutePath() + "/**/ba*.sql", 2);
        checkFileFlobScan(rootFolder.getAbsolutePath() + "/**/ba*", 3);
    }

    @Test
    public void testFileGlob_Relative() throws IOException {
        // Given
        File targetFolder = new File("target");
        File rootFolder = createFiles(targetFolder);
        TestConsumer<ScriptStartEvent> end = new TestConsumer<>();
        // When
        FileScriptScanner.fileGlob(adaptFileSeparator("target/root/**/*.sql"), end).scan();
        // Then
        assertThat(end.events).hasSize(3);
        List<String> fullNames = getFullNames(end);
        assertThat(fullNames).contains("target/root/sub/foo.sql", "target/root/sub/inner/bar.sql");
        TestFiles.delete(rootFolder.toPath());
    }

    private static List<String> getFullNames(TestConsumer<ScriptStartEvent> end) {
        return end.eventStream(ScriptStartEvent.class)
            .map(e -> e.getScript().getFullName())
            .map(ScriptScanner::fixFileSeparator)
            .collect(Collectors.toList());
    }

    private File createFiles(File folder) throws IOException {
        return folder("root",
            folder("sub",
                file("foo.sql"),
                folder("inner",
                    file("bar.sql"),
                    file("bar.text"))),
            file("baz.sql")
        ).create(folder);
    }

    private void checkFileFlobScan(String fileGlob, int fileNb) throws IOException {
        TestConsumer<ScriptStartEvent> end = new TestConsumer<>();
        FileScriptScanner.fileGlob(adaptFileSeparator(fileGlob), end).scan();
        assertThat(end.events).hasSize(fileNb);
    }
}
