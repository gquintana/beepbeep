package com.github.gquintana.beepbeep.pipeline;

import com.github.gquintana.beepbeep.TestConsumer;
import org.junit.Test;

import static com.github.gquintana.beepbeep.pipeline.LineEvent.event;
import static org.assertj.core.api.Assertions.assertThat;

public class MultiLineAggregatorTest {

    @Test
    public void testConsume_EndMarker() throws Exception {
        // Given
        TestConsumer end = new TestConsumer();
        MultilineAggregator processor = new MultilineAggregator(";[ ]*$", end);
        String eol = System.lineSeparator();
        // When
        processor.consume(event(0, "insert into table1(column1, column2)"));
        processor.consume(event(1, "values(1, 'one');"));
        processor.consume(event(2, ""));
        processor.consume(event(3, "insert into table1(column1, column2)"));
        processor.consume(event(4, "values(2, 'two');"));
        // Then
        assertThat(end.events).hasSize(2);
        assertThat(end.events)
                .contains(event(1, "insert into table1(column1, column2)" + eol + "values(1, 'one')" + eol),
                        event(4, eol + "insert into table1(column1, column2)" + eol + "values(2, 'two')" + eol));


    }

    @Test
    public void testConsume_StartMarker() throws Exception {
        // Given
        TestConsumer end = new TestConsumer();
        MultilineAggregator processor = new MultilineAggregator("^[ ]*(GET|POST)", MultilineAggregator.LineMarkerStrategy.START, false, end);
        String eol = System.lineSeparator();
        // When
        processor.consume(event(0, "GET /this/url"));
        processor.consume(event(1, "{\"body\":\"json\"}"));
        processor.consume(event(2, ""));
        processor.consume(event(3, "POST /other/url"));
        processor.consume(event(4, "{\"body\":\"json\"}"));
        processor.consume(null);
        // Then
        assertThat(end.events).hasSize(3);
        assertThat(end.events)
                .contains(event(2, "GET /this/url" + eol + "{\"body\":\"json\"}" + eol + eol),
                        event(4, "POST /other/url" + eol + "{\"body\":\"json\"}" + eol));


    }

}
