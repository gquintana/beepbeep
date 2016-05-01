package com.github.gquintana.beepbeep.util;

import org.junit.Test;

import static org.junit.Assert.*;

public class StringsTest {

    @Test
    public void testLeft() throws Exception {
        assertEquals("Bonjour", Strings.left("Bonjour le monde", 7));
        assertEquals("Bonjour le monde", Strings.left("Bonjour le monde", 20));
        assertEquals("", Strings.left("Bonjour le monde", 0));
    }

    @Test
    public void testRight() throws Exception {
        assertEquals("monde", Strings.right("Bonjour le monde", 11));
        assertEquals("Bonjour le monde", Strings.right("Bonjour le monde", 0));
        assertEquals("", Strings.right("Bonjour le monde", 20));

    }
}
