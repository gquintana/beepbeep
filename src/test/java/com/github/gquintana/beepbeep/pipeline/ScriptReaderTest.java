package com.github.gquintana.beepbeep.pipeline;

import com.github.gquintana.beepbeep.TestConsumer;
import com.github.gquintana.beepbeep.TestFiles;
import com.github.gquintana.beepbeep.script.ResourceScript;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ScriptReaderTest {

    @Test
    public void testRead_Success() {
        // Given
        ResourceScript script = ResourceScript.create(TestFiles.class, "sql/init/02_data.sql");
        TestConsumer<ScriptEvent> end = new TestConsumer<>();
        ScriptReader scriptReader = new ScriptReader(end, StandardCharsets.UTF_8);
        // When
        scriptReader.consume(new ScriptStartEvent(script));
        // Then
        assertThat(end.events).hasSize(6);
        List<String> lines = end.lines();
        assertThat(lines).hasSize(4);
        assertThat(lines).contains("INSERT INTO person(login, email) VALUES ('jdoe', 'john.doe@unknown.com');",
        "INSERT INTO person(login, email) VALUES ('sconnor', 'sarah.connor@cyberdine.com');");
        List<ScriptEvent> scriptEvents = end.events(ScriptEvent.class);
        assertThat(scriptEvents.get(0).getType()).isEqualTo(ScriptStartEvent.TYPE);
        assertThat(scriptEvents.get(1).getType()).isEqualTo(LineEvent.TYPE);
        assertThat(scriptEvents.get(2).getType()).isEqualTo(LineEvent.TYPE);
        assertThat(scriptEvents.get(5).getType()).isEqualTo(ScriptEndEvent.SUCCESS_TYPE);
    }

    @Test
    public void testRead_Fail() {
        // Given
        ResourceScript script = ResourceScript.create(TestFiles.class, "sql/init/02_data.sql");
        TestConsumer<ScriptEvent> end = failAtEventNum(3);
        ScriptReader scriptReader = new ScriptReader(end, StandardCharsets.UTF_8);
        // When
        assertThatThrownBy(() ->
            scriptReader.consume(new ScriptStartEvent(script)))
            .isInstanceOf(IllegalArgumentException.class);

        // Then
        assertThat(end.events).hasSize(3);
        List<String> lines = end.lines();
        assertThat(lines).hasSize(1);
        List<ScriptEvent> scriptEvents = end.events(ScriptEvent.class);
        assertThat(scriptEvents.get(0).getType()).isEqualTo(ScriptStartEvent.TYPE);
        assertThat(scriptEvents.get(1).getType()).isEqualTo(LineEvent.TYPE);
        assertThat(scriptEvents.get(2).getType()).isEqualTo(ScriptEndEvent.FAIL_TYPE);
    }

    @Test
    public void testRead_FailContinue() {
        // Given
        ResourceScript script = ResourceScript.create(TestFiles.class, "sql/init/02_data.sql");
        TestConsumer<ScriptEvent> end = failAtEventNum(3);
        ScriptReader scriptReader = new ScriptReader(end, StandardCharsets.UTF_8, true);
        // When
        scriptReader.consume(new ScriptStartEvent(script));
        // Then
        assertThat(end.events).hasSize(6);
        List<ScriptEvent> scriptEvents = end.events(ScriptEvent.class);
        assertThat(scriptEvents.get(0).getType()).isEqualTo(ScriptStartEvent.TYPE);
        assertThat(scriptEvents.get(5).getType()).isEqualTo(ScriptEndEvent.FAIL_TYPE);
    }

    public static TestConsumer<ScriptEvent> failAtEventNum(int ... eventNums) {
        return new TestConsumer<ScriptEvent>() {
            private int number = 0;
            @Override
            public void consume(ScriptEvent event) {
                number++;
                if (Arrays.binarySearch(eventNums, number) >= 0) {
                    throw new IllegalArgumentException("Fail "+number);
                }
                super.consume(event);
            }
        };
    }
}
