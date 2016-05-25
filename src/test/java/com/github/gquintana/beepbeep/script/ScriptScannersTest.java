package com.github.gquintana.beepbeep.script;

import com.github.gquintana.beepbeep.TestConsumer;
import com.github.gquintana.beepbeep.TestFiles;
import com.github.gquintana.beepbeep.config.ConfigurationException;
import com.github.gquintana.beepbeep.pipeline.ScriptStartEvent;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class ScriptScannersTest {

    private String readAsString(Script script) throws IOException {
        try (InputStream inputStream = script.getStream();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            TestFiles.copy(inputStream, outputStream);
            return new String(outputStream.toByteArray(), "UTF-8");
        }
    }

    @Test
    public void testResource_Class() throws Exception {
        // Given
        TestConsumer<ScriptStartEvent> output = new TestConsumer<>();
        // When
        String resourceFullName = TestFiles.getResourceFullName("sql/clean/drop.sql");
        SingleScriptScanner scanner = ScriptScanners.resource(TestFiles.class, "sql/clean/drop.sql", output);
        scanner.scan();
        // Then
        assertThat(output.events).hasSize(1);
        ScriptStartEvent event = output.events(ScriptStartEvent.class).get(0);
        assertThat(event.getScript().getFullName()).isEqualTo(resourceFullName);
        assertThat(readAsString(event.getScript()).trim()).isEqualTo("DROP TABLE person;");
    }

    @Test
    public void testResource_ClassLoader() throws Exception {
        // Given
        TestConsumer<ScriptStartEvent> output = new TestConsumer<>();
        // When
        String resourceFullName = TestFiles.getResourceFullName("sql/clean/drop.sql");
        SingleScriptScanner scanner = ScriptScanners.resource(Thread.currentThread().getContextClassLoader(), resourceFullName, output);
        scanner.scan();
        // Then
        assertThat(output.events).hasSize(1);
        ScriptStartEvent event = output.events(ScriptStartEvent.class).get(0);
        assertThat(event.getScript().getFullName()).isEqualTo(resourceFullName);
        assertThat(readAsString(event.getScript()).trim()).isEqualTo("DROP TABLE person;");
    }


    @Test
    public void testScheme_Raw() throws Exception {
        // Given
        TestConsumer<ScriptStartEvent> output = new TestConsumer<>();
        // When
        String resourceFullName = TestFiles.adaptFileSeparator("target/test-classes/" + TestFiles.getResourceFullName("sql/clean/drop.sql"));
        ScriptScanner scanner = ScriptScanners.schemes(resourceFullName, output);
        scanner.scan();
        // Then
        assertThat(output.events).hasSize(1);
        ScriptStartEvent event = output.events(ScriptStartEvent.class).get(0);
        assertThat(event.getScript().getFullName()).isEqualTo(resourceFullName);
    }

    @Test
    public void testScheme_File() throws Exception {
        // Given
        TestConsumer<ScriptStartEvent> output = new TestConsumer<>();
        // When
        String resourceFullName = "target/test-classes/" + TestFiles.getResourceFullName("sql/clean/drop.sql");
        ScriptScanner scanner = ScriptScanners.schemes("file:" + resourceFullName, output);
        scanner.scan();
        // Then
        assertThat(output.events).hasSize(1);
        ScriptStartEvent event = output.events(ScriptStartEvent.class).get(0);
        assertThat(event.getScript().getFullName()).isEqualTo(TestFiles.adaptFileSeparator(resourceFullName));
    }

    @Test
    public void testScheme_Class() throws Exception {
        // Given
        TestConsumer<ScriptStartEvent> output = new TestConsumer<>();
        // When
        String resourceFullName = TestFiles.getResourceFullName("sql/clean/drop.sql");
        ScriptScanner scanner = ScriptScanners.schemes("classpath:" + resourceFullName, output);
        scanner.scan();
        // Then
        assertThat(output.events).hasSize(1);
        ScriptStartEvent event = output.events(ScriptStartEvent.class).get(0);
        assertThat(event.getScript().getFullName()).isEqualTo(resourceFullName);
    }

    @Test
    public void testSchemes_Raw() throws Exception {
        // Given
        TestConsumer<ScriptStartEvent> output = new TestConsumer<>();
        // When
        String resourceFullName = TestFiles.adaptFileSeparator("target/test-classes/" + TestFiles.getResourceFullName("sql/init/*.sql"));
        ScriptScanner scanner = ScriptScanners.schemes(resourceFullName, output);
        scanner.scan();
        // Then
        assertThat(output.events).hasSize(2);
    }

    @Test
    public void testSchemes_Class() throws Exception {
        // Given
        TestConsumer<ScriptStartEvent> output = new TestConsumer<>();
        // When
        String resourceFullName = TestFiles.getResourceFullName("sql/init/*.sql");
        ScriptScanner scanner = ScriptScanners.schemes("classpath:" + resourceFullName, output);
        scanner.scan();
        // Then
        assertThat(output.events).hasSize(2);
    }

    @Test
    public void testSchemes_File() throws Exception {
        // Given
        TestConsumer<ScriptStartEvent> output = new TestConsumer<>();
        // When
        String resourceFullName = "target/test-classes/" + TestFiles.getResourceFullName("sql/init/*.sql");
        ScriptScanner scanner = ScriptScanners.schemes("file:" + resourceFullName, output);
        scanner.scan();
        // Then
        assertThat(output.events).hasSize(2);
    }

    @Test
    public void testScheme_Invalidscheme() throws Exception {
        // Given
        TestConsumer<ScriptStartEvent> output = new TestConsumer<>();
        // When
        try {
            ScriptScanner scanner = ScriptScanners.schemes("ftp://server/test.sql", output);
            fail("Exception expected");
        } catch (ConfigurationException e) {
        }
        // Then
    }
}
