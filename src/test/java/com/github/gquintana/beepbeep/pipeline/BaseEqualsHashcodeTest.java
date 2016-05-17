package com.github.gquintana.beepbeep.pipeline;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public abstract class BaseEqualsHashcodeTest<T> {
    private final T actual;
    private final T expected;
    private final boolean equals;

    protected BaseEqualsHashcodeTest(T actual, T expected, boolean equals) {
        this.actual = actual;
        this.expected = expected;
        this.equals = equals;
    }

    @Test
    public void testEquals() {
        if (equals) {
            assertEquals(expected, actual);
        } else {
            assertNotEquals(expected, actual);
        }
    }

    @Test
    public void testHashcode() {
        if (equals) {
            assertEquals(expected.hashCode(), actual.hashCode());
        }
    }

}
