package com.github.gquintana.beepbeep.sql;

import com.github.gquintana.beepbeep.TestConsumer;
import com.github.gquintana.beepbeep.pipeline.Processor;
import com.github.gquintana.beepbeep.pipeline.RegexReplacerProcessor;
import org.h2.Driver;
import org.junit.Test;

import static com.github.gquintana.beepbeep.pipeline.LineEvent.event;
import static org.assertj.core.api.Assertions.assertThat;

public class SqlLineExecutorTest {

    @Test
    public void testConsume() throws Exception {
        // Given
        TestConsumer end = new TestConsumer();
        SqlConnectionProvider connectionProvider = new DriverSqlConnectionProvider(
                Driver.class.getName(), "jdbc:h2:mem:test", "sa", "");
        try(SingleSqlConnectionProvider connectionProvider2 = new SingleSqlConnectionProvider(connectionProvider)) {
            Processor processor = new RegexReplacerProcessor(";\\s*$", "", new SqlLineExecutor(connectionProvider2, end));
            String eol = System.lineSeparator();
            // When
            processor.consume(event(0, "create table person(login varchar(64), email varchar(256), constraint person_pk primary key (login));"));
            processor.consume(event(1, "insert into person(login, email) values('jdoe', 'john.doe@unknown.com');"));
            processor.consume(event(2, "insert into person(login, email) values('sconnor', 'sarah.connor@cyberdine.com');"));
            processor.consume(event(3, "select login,email from person order by login asc;"));
            // Then
            assertThat(end.events).hasSize(5);
            assertThat(end.events).contains(
                    "0 updates",
                    "1 updates",
                    "1 updates",
                    "0;jdoe;john.doe@unknown.com",
                    "1;sconnor;sarah.connor@cyberdine.com");
        }
    }
}
