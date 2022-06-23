package com.github.gquintana.beepbeep.script;

import com.github.gquintana.beepbeep.TestConsumer;
import com.github.gquintana.beepbeep.TestFiles;
import com.github.gquintana.beepbeep.pipeline.ScriptStartEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.gquintana.beepbeep.TestFiles.*;
import static org.assertj.core.api.Assertions.assertThat;

public class FileScriptScannerGlobTest {
    @TempDir
    Path tempDir;

    @Test
    public void testFileGlobRegex() {
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
        Path rootFolder = createFiles(tempDir);
        // When Then
        checkFileFlobScan(rootFolder.toAbsolutePath() + "/*.sql", 1);
        checkFileFlobScan(rootFolder.toAbsolutePath() + "/**/*.sql", 3);
        checkFileFlobScan(rootFolder.toAbsolutePath() + "/**", 4);
        checkFileFlobScan(rootFolder.toAbsolutePath() + "/sub/*.sql", 1);
        checkFileFlobScan(rootFolder.toAbsolutePath() + "/sub/**/*.sql", 2);
        checkFileFlobScan(rootFolder.toAbsolutePath().toString(), 4);
        checkFileFlobScan(rootFolder.toAbsolutePath() + "/baz.sql", 1);
        checkFileFlobScan(rootFolder.toAbsolutePath() + "/**/*.xml", 0);
        checkFileFlobScan(rootFolder.toAbsolutePath() + "/**/ba*.sql", 2);
        checkFileFlobScan(rootFolder.toAbsolutePath() + "/**/ba*", 3);
    }

    @Test
    public void testFileGlob_Relative() throws IOException {
        // Given
        Path targetFolder = Paths.get("target");
        Path rootFolder = createFiles(targetFolder);
        TestConsumer<ScriptStartEvent> end = new TestConsumer<>();
        // When
        FileScriptScanner.fileGlob(adaptFileSeparator("target/root/**/*.sql"), end).scan();
        // Then
        assertThat(end.events).hasSize(3);
        List<String> fullNames = getFullNames(end);
        assertThat(fullNames).contains("target/root/sub/foo.sql", "target/root/sub/inner/bar.sql");
        TestFiles.delete(rootFolder);
    }

    private static List<String> getFullNames(TestConsumer<ScriptStartEvent> end) {
        return end.eventStream(ScriptStartEvent.class)
            .map(e -> e.getScript().getFullName())
            .map(ScriptScanner::fixFileSeparator)
            .collect(Collectors.toList());
    }

    private Path createFiles(Path folder) throws IOException {
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
