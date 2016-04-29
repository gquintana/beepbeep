package com.github.gquintana.dbscriptrunner;

import com.github.gquintana.dbscriptrunner.pipeline.Consumer;
import com.github.gquintana.dbscriptrunner.pipeline.LineEvent;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class TestConsumer implements Consumer {
    public final List<Object> events = new ArrayList<>();

    @Override
    public void consume(Object event) {
        events.add(event);
    }

    public List<String> lines() {
        return events.stream().filter(l -> l instanceof LineEvent).map(l -> ((LineEvent) l).getLine()).collect(toList());
    }
}
