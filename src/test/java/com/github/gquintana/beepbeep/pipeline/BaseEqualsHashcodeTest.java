package com.github.gquintana.beepbeep.pipeline;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class BaseEqualsHashcodeTest<T> {

    @ParameterizedTest
    @MethodSource("getParameters")
    public void testEquals(T actual, T expected, boolean equals) {
        if (equals) {
            assertThat(actual).isEqualTo(expected);
        } else {
            assertThat(actual).isNotEqualTo(expected);
        }
    }

    @ParameterizedTest
    @MethodSource("getParameters")
    public void testHashcode(T actual, T expected, boolean equals) {
        if (equals) {
            assertThat(actual.hashCode()).isEqualTo(expected.hashCode());
        }
    }

}
