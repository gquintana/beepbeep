package com.github.gquintana.beepbeep.pipeline;

import com.github.gquintana.beepbeep.TestConsumer;
import com.github.gquintana.beepbeep.TestFiles;
import com.github.gquintana.beepbeep.script.ResourceScript;
import org.junit.Test;

import java.nio.charset.Charset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ScriptReaderProducerTest {

    @Test
    public void testRead() {
        // Given
        ResourceScript script = ResourceScript.create(TestFiles.class, "script/script_data.sql");
        TestConsumer end = new TestConsumer();
        ScriptReaderProducer scriptReader = new ScriptReaderProducer(end, Charset.forName("UTF-8"));
        // When
        scriptReader.consume(script);
        // Then
        assertThat(end.events).hasSize(4);
        List<String> lines = end.lines();
        assertThat(lines).hasSize(2);
        assertThat(lines).contains("INSERT INTO person(login, email) VALUE ('jdoe', 'john.doe@unknown.com');",
        "INSERT INTO person(login, email) VALUE ('sconnor', 'sarah.connor@cyberdine.com');");
        List<ScriptEvent> scriptEvents = end.events(ScriptEvent.class);
        assertThat(scriptEvents.get(0).getType()).isEqualTo(ScriptEvent.Type.START);
        assertThat(scriptEvents.get(1).getType()).isEqualTo(ScriptEvent.Type.END_SUCCESS);
    }

}