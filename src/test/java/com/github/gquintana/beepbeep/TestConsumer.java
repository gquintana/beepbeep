package com.github.gquintana.beepbeep;

import com.github.gquintana.beepbeep.pipeline.Consumer;
import com.github.gquintana.beepbeep.pipeline.LineEvent;

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
