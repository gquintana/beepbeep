package com.github.gquintana.beepbeep.util;

import java.util.ArrayList;
import java.util.List;

public class ParametersBuilder {
    private final List<Object[]> parameters = new ArrayList<>();

    public ParametersBuilder add(Object... parameter) {
        parameters.add(parameter);
        return this;
    }

    public List<Object[]> build() {
        return parameters;
    }
}
