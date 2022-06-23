package com.github.gquintana.beepbeep.pipeline;

import com.github.gquintana.beepbeep.TestConsumer;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class VariableReplacerTest {

    @Test
    public void testConsume() {
        // Given
        TestConsumer<ScriptEvent> end = new TestConsumer<>();
        Map<String, Object> variables = new HashMap<>();
        variables.put("column2.1", "One");
        VariableReplacer processor = new VariableReplacer(variables, end);
        String eol = System.lineSeparator();
        // When
        processor.consume(event(0, "insert into table1(column1, column2) values(1, '${column2.1}');"));
        processor.consume(event(1, ""));
        processor.consume(event(2, "insert into table1(column1, column2) values(2, '${column2.2}');"));
        processor.consume(null);
        // Then
        assertThat(end.events).hasSize(4);
        assertThat(end.events)
                .contains(event(0, "insert into table1(column1, column2) values(1, 'One');"),
                        event(1, ""),
                        event(2, "insert into table1(column1, column2) values(2, '${column2.2}');"),
                        null);
    }

    private LineEvent event(int lineNb, String line) {
        return new LineEvent(null, lineNb, line);
    }

    @Test
    public void testConsumeRecursiveReplace() {
        // Given
        TestConsumer<ScriptEvent> end = new TestConsumer<>();
        Map<String, Object> variables = new HashMap<>();
        variables.put("column2.default", "X");
        variables.put("column2.1", "<${column2.default}>");
        variables.put("column2.2", "<${column2.default}>");
        VariableReplacer processor = new VariableReplacer(variables, end);
        String eol = System.lineSeparator();
        // When
        processor.consume(event(0, "insert into table1(column1, column2) values(1, '${column2.1}');"));
        processor.consume(event(1, ""));
        processor.consume(event(2, "insert into table1(column1, column2) values(2, '${column2.2}');"));
        processor.consume(null);
        // Then
        assertThat(end.events).hasSize(4);
        assertThat(end.events)
                .contains(event(0, "insert into table1(column1, column2) values(1, '<X>');"),
                        event(1, ""),
                        event(2, "insert into table1(column1, column2) values(2, '<X>');"),
                        null);
    }

}
