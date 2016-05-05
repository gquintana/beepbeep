package com.github.gquintana.beepbeep;

import com.github.gquintana.beepbeep.pipeline.Consumer;
import com.github.gquintana.beepbeep.pipeline.LineEvent;
import com.github.gquintana.beepbeep.pipeline.ScriptEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.fail;

public class TestConsumer implements Consumer {
    private final Logger logger = LoggerFactory.getLogger(TestConsumer.class);
    public final List<Object> events = new ArrayList<>();

    @Override
    public void consume(Object event) {
        events.add(event);
    }


    public <T> List<T> events(Class<T> clazz) {
        return eventStream(clazz).collect(toList());
    }

    public <T> Stream<T> eventStream(Class<T> clazz) {
        return events.stream().filter(clazz::isInstance).map(clazz::cast);
    }

    public List<String> lines() {
        return eventStream(LineEvent.class).map(LineEvent::getLine).collect(toList());
    }
    public void assertNoScriptEndFailed() {
        Optional<ScriptEvent> optEndFailEvent = eventStream(ScriptEvent.class)
            .filter(e -> e.getType() == ScriptEvent.Type.END_FAILED)
            .findAny();
        if (optEndFailEvent.isPresent()) {
            ScriptEvent event = optEndFailEvent.get();
            logger.warn("Script event failed"+event.getScript().getName(), event.getException());
            fail(event.getException().getMessage());
        }
    }
}
