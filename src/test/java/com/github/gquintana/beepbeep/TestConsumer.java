package com.github.gquintana.beepbeep;

import com.github.gquintana.beepbeep.pipeline.Consumer;
import com.github.gquintana.beepbeep.pipeline.LineEvent;
import com.github.gquintana.beepbeep.pipeline.ScriptEndEvent;
import com.github.gquintana.beepbeep.pipeline.ScriptEvent;
import com.github.gquintana.beepbeep.pipeline.ScriptStartEvent;
import com.github.gquintana.beepbeep.script.Script;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.fail;

public class TestConsumer<E extends ScriptEvent> implements Consumer<E> {
    private final Logger logger = LoggerFactory.getLogger(TestConsumer.class);
    public final List<E> events = new ArrayList<>();

    @Override
    public void consume(E event) {
        events.add(event);
    }


    public <T extends ScriptEvent> List<T> events(Class<T> clazz) {
        return eventStream(clazz).collect(toList());
    }

    public <T extends ScriptEvent> Stream<T> eventStream(Class<T> clazz) {
        return events.stream().filter(clazz::isInstance).map(clazz::cast);
    }

    public <T extends Script> Stream<T> scriptStream(Class<T> clazz) {
        return events.stream()
            .filter(ScriptStartEvent.class::isInstance)
            .map(ScriptEvent::getScript)
            .filter(clazz::isInstance).map(clazz::cast);
    }

    public List<String> lines() {
        return eventStream(LineEvent.class).map(LineEvent::getLine).collect(toList());
    }

    public void assertNoScriptEndFailed() {
        Optional<ScriptEndEvent> optEndFailEvent = eventStream(ScriptEndEvent.class)
            .filter(e -> e.getType().equals(ScriptEndEvent.FAIL_TYPE))
            .findAny();
        if (optEndFailEvent.isPresent()) {
            ScriptEndEvent event = optEndFailEvent.get();
            logger.warn("Script event failed" + event.getScript().getName(), event.getException());
            fail(event.getException().getMessage());
        }
    }

    public void clear() {
        events.clear();
    }
}
