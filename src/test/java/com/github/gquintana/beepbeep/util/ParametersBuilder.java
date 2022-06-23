package com.github.gquintana.beepbeep.util;

import org.junit.jupiter.params.provider.Arguments;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class ParametersBuilder {
    private final List<Arguments> parameters = new ArrayList<>();

    public ParametersBuilder add(Object... parameter) {
        parameters.add(Arguments.of(parameter));
        return this;
    }

    public Stream<Arguments> build() {
        return parameters.stream();
    }
}
