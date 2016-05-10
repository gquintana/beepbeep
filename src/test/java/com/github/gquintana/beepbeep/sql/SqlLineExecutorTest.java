package com.github.gquintana.beepbeep.sql;

import com.github.gquintana.beepbeep.TestConsumer;
import com.github.gquintana.beepbeep.pipeline.LineEvent;
import com.github.gquintana.beepbeep.pipeline.RegexReplacerProcessor;
import com.github.gquintana.beepbeep.pipeline.ResultEvent;
import com.github.gquintana.beepbeep.pipeline.ScriptEvent;
import org.h2.Driver;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class SqlLineExecutorTest {

    @Test
    public void testConsume() throws Exception {
        // Given
        TestConsumer<ScriptEvent> end = new TestConsumer<>();
        SqlConnectionProvider connectionProvider = new DriverSqlConnectionProvider(
                Driver.class.getName(), "jdbc:h2:mem:test", "sa", "");
        try(SingleSqlConnectionProvider connectionProvider2 = new SingleSqlConnectionProvider(connectionProvider)) {
            RegexReplacerProcessor processor = new RegexReplacerProcessor(";\\s*$", "", new SqlLineExecutor(connectionProvider2, end));
            // When
            processor.consume(event(0, "create table person(login varchar(64), email varchar(256), constraint person_pk primary key (login));"));
            processor.consume(event(1, "insert into person(login, email) values('jdoe', 'john.doe@unknown.com');"));
            processor.consume(event(2, "insert into person(login, email) values('sconnor', 'sarah.connor@cyberdine.com');"));
            processor.consume(event(3, "select login,email from person order by login asc;"));
            // Then
            assertThat(end.events).hasSize(5);
            List<String> results = end.eventStream(ResultEvent.class).map(r -> r.getResult()).collect(Collectors.toList());
            assertThat(results).contains(
                    "0 updates",
                    "1 updates",
                    "1 updates",
                    "0;jdoe;john.doe@unknown.com",
                    "1;sconnor;sarah.connor@cyberdine.com");
        }
    }

    private LineEvent event(int lineNb, String line) {
        return new LineEvent(null, lineNb, line);
    }
}
