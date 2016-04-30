package com.github.gquintana.beepbeep;

import com.github.gquintana.beepbeep.pipeline.Consumer;
import com.github.gquintana.beepbeep.pipeline.LineEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class TestConsumer implements Consumer {
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
}
