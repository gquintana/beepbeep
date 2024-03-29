package com.github.gquintana.beepbeep.pipeline;

import com.github.gquintana.beepbeep.TestConsumer;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RegexReplacerTest {

    @Test
    public void testTransform() {
        // Given
        TestConsumer<ScriptEvent> end = new TestConsumer<>();
        RegexReplacer processor = new RegexReplacer(";\\s*$", "", end);
        String eol = System.lineSeparator();
        // When
        processor.consume(event(0, "insert into table1(column1, column2) values(1, 'One');"));
        processor.consume(event(1, "insert into table1(column1, column2)" + eol + " values(2, 'Two');"));
        processor.consume(event(2, "insert into table1(column1, column2)" + eol + " values(3, 'Three')"));
        processor.consume(null);
        // Then
        assertThat(end.events).hasSize(4);
        assertThat(end.events)
                .contains(event(0, "insert into table1(column1, column2) values(1, 'One')"),
                        event(1, "insert into table1(column1, column2)" + eol + " values(2, 'Two')"),
                        event(2, "insert into table1(column1, column2)" + eol + " values(3, 'Three')"),
                        null);

    }

    private LineEvent event(int lineNb, String line) {
        return new LineEvent(null, lineNb, line);
    }
}
