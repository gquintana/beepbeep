package com.github.gquintana.beepbeep.pipeline;

import com.github.gquintana.beepbeep.TestConsumer;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class EmptyLineFilterTest {

    @Test
    public void testConsume() throws Exception {
        // Given
        TestConsumer end = new TestConsumer();
        LineFilter filter = LineFilter.notNulNotEmptyFilter(end);
        // When
        filter.consume(new LineEvent(null, 0, "Not empty"));
        filter.consume(new LineEvent(null, 1, ""));
        filter.consume(new LineEvent(null, 2, "Not empty"));
        filter.consume(new LineEvent(null, 3, null));
        filter.consume(new ScriptEndEvent(null, 3));
        // Then
        assertThat(end.events).hasSize(3);
        assertThat(end.lines()).hasSize(2);
        assertThat(end.lines()).contains("Not empty", "Not empty");
    }
}
