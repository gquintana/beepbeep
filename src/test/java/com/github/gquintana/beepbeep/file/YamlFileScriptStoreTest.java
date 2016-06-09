package com.github.gquintana.beepbeep.file;

import com.github.gquintana.beepbeep.TestFiles;
import com.github.gquintana.beepbeep.store.ScriptInfo;
import com.github.gquintana.beepbeep.store.ScriptStatus;
import com.github.gquintana.beepbeep.store.ScriptStoreException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

public class YamlFileScriptStoreTest {
    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void testSave() throws IOException {
        // Given
        List<ScriptInfo<Integer>> infos = Arrays.asList(
            new ScriptInfo<>(1, 1, "path/script1.sql", 12, "ABC", Instant.now(), Instant.now().plus(10, ChronoUnit.SECONDS), ScriptStatus.SUCCEEDED),
            new ScriptInfo<>(2, 2, "path/script2.sql", 23, "012", Instant.now().plus(10, ChronoUnit.SECONDS), null, ScriptStatus.STARTED),
            new ScriptInfo<>(3, 1, "path/script1.sql", 34, "1B2", Instant.now().plus(20, ChronoUnit.SECONDS), Instant.now().plus(25, ChronoUnit.SECONDS), ScriptStatus.FAILED)
        );
        File storeFile = temporaryFolder.newFile("store.yml");
        YamlFileScriptStore store = new YamlFileScriptStore(storeFile.toPath());
        // When
        store.save(infos);
        // Then
        List<ScriptInfo<Integer>> infos1 = store.load().collect(Collectors.toList());
        assertThat(infos1).hasSize(3);
    }

    @Test
    public void testLoad() throws IOException {
        // Given
        File file = temporaryFolder.newFile("store.yml");
        TestFiles.writeResource("store/store.yml", file);
        YamlFileScriptStore store = new YamlFileScriptStore(file.toPath());
        // When
        List<ScriptInfo<Integer>> infos = store.load().collect(Collectors.toList());
        // Then
        assertThat(infos).hasSize(3);
        assertThat(infos.get(2).getId()).isEqualTo(3);
        assertThat(infos.get(2).getFullName()).isEqualTo("other/script1.sql");
        assertThat(infos.get(2).getVersion()).isEqualTo(1);
        assertThat(infos.get(2).getSize()).isEqualTo(34);
        assertThat(infos.get(2).getSha1()).isEqualTo("1B2");
        assertThat(infos.get(2).getStatus()).isEqualTo(ScriptStatus.FAILED);
    }

    @Test
    public void testGetByFullName() throws IOException {
        // Given
        File file = temporaryFolder.newFile("store.yml");
        TestFiles.writeResource("store/store.yml", file);
        YamlFileScriptStore store = new YamlFileScriptStore(file.toPath());
        // When
        ScriptInfo<Integer> info1 = store.getByFullName("other/script1.sql");
        ScriptInfo<Integer> info2 = store.getByFullName("other/unknown.sql");
        // Then
        assertThat(info1.getId()).isEqualTo(3);
        assertThat(info2).isNull();
    }

    @Test
    public void testGetByFullName_NoFile() throws IOException {
        // Given
        File file = new File(temporaryFolder.getRoot(), "unknown.yml");
        YamlFileScriptStore store = new YamlFileScriptStore(file.toPath());
        // When
        ScriptInfo<Integer> info1 = null;
        info1 = store.getByFullName("other/script1.sql");
        ScriptInfo<Integer> info2 = store.getByFullName("other/unknown.sql");
        // Then
        assertThat(info1).isNull();
        assertThat(info2).isNull();
    }

    @Test
    public void testCreate() throws IOException {
        // Given
        File file = temporaryFolder.newFile("store.yml");
        TestFiles.writeResource("store/store.yml", file);
        YamlFileScriptStore store = new YamlFileScriptStore(file.toPath());
        ScriptInfo<Integer> info = new ScriptInfo<>(0, 1, "path/script3.sql", 28, "12A", Instant.now(), Instant.now().plus(10, ChronoUnit.SECONDS), ScriptStatus.SUCCEEDED);
        // When
        store.create(info);
        // Then
        assertThat(store.load().count()).isEqualTo(4);
    }

    @Test
    public void testUpdate() throws IOException {
        // Given
        File file = temporaryFolder.newFile("store.yml");
        TestFiles.writeResource("store/store.yml", file);
        YamlFileScriptStore store = new YamlFileScriptStore(file.toPath());
        ScriptInfo<Integer> info = succeededInfo2();
        // When
        store.update(info);
        // Then
        ScriptInfo<Integer> info2 = store.getByFullName("path/script2.sql");
        assertThat(info2.getVersion()).isEqualTo(3);
        assertThat(info2.getEndDate()).isNotNull();
        assertThat(info2.getStatus()).isEqualTo(ScriptStatus.SUCCEEDED);
    }

    @Test
    public void testUpdate_NotFound() throws IOException {
        // Given
        File file = temporaryFolder.newFile("store.yml");
        TestFiles.writeResource("store/store.yml", file);
        YamlFileScriptStore store = new YamlFileScriptStore(file.toPath());
        ScriptInfo<Integer> info = new ScriptInfo<>(4, 2, "path/script4.sql", 23, "012", Instant.now().plus(10, ChronoUnit.SECONDS), null, ScriptStatus.STARTED);
        info.setEndDate(Instant.now().plus(12, ChronoUnit.SECONDS));
        info.setStatus(ScriptStatus.SUCCEEDED);
        // When
        try {
            store.update(info);
            fail("Exception expected");
        } catch (ScriptStoreException e) {
        }
        // Then
    }

    @Test
    public void testUpdate_Concurrent() throws IOException {
        // Given
        File file = temporaryFolder.newFile("store.yml");
        TestFiles.writeResource("store/store.yml", file);
        YamlFileScriptStore store = new YamlFileScriptStore(file.toPath());
        store.update(succeededInfo2());
        // When
        try {
            store.update(succeededInfo2());
            fail("Exception expected");
        } catch (ScriptStoreException e) {
        }
        // Then
    }

    private ScriptInfo<Integer> succeededInfo2() {
        ScriptInfo<Integer> info = new ScriptInfo<>(2, 2, "path/script2.sql", 23, "012", Instant.now().plus(10, ChronoUnit.SECONDS), null, ScriptStatus.STARTED);
        info.setEndDate(Instant.now().plus(12, ChronoUnit.SECONDS));
        info.setStatus(ScriptStatus.SUCCEEDED);
        return info;
    }
}
