package com.github.gquintana.beepbeep.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

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

    @Test
    public void testBytesToHex() throws Exception {
        assertEquals("123abc", Strings.bytesToHex(new byte[]{18, 58, (byte) 188}));
    }

    @Test
    public void testUpperCaseFirstLetter() throws Exception {
        assertEquals("Abc", Strings.upperCaseFirstChar("abc"));
        assertEquals("", Strings.upperCaseFirstChar(""));
    }

    @Test
    public void testToCamelCase() throws Exception {
        assertEquals("AbcDefGhi", Strings.toCamelCase("abc_def_ghi"));
        assertEquals("AbcDefGhi", Strings.toCamelCase("abc.def-ghi"));
        assertEquals("AbcDefGhi", Strings.toCamelCase("abc..def.ghi"));
        assertEquals("", Strings.toCamelCase(""));
    }

    @Test
    public void testReplaceAll() {
        assertEquals("abc/def/ghi", "abc\\def\\ghi".replace('\\', '/'));
    }
}
