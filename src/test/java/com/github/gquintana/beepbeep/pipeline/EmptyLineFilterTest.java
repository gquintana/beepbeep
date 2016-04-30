package com.github.gquintana.beepbeep.pipeline;

import com.github.gquintana.beepbeep.TestConsumer;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class EmptyLineFilterTest {

    @Test
    public void testConsume() throws Exception {
        // Given
        TestConsumer end = new TestConsumer();
        EmptyFilter filter = new EmptyFilter(end);
        // When
        filter.consume("Not empty");
        filter.consume("");
        filter.consume("Not empty");
        filter.consume(null);
        // Then
        assertThat(end.events).hasSize(2);
        assertThat(end.events).contains("Not empty", "Not empty");
    }
}
