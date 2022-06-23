package com.github.gquintana.beepbeep.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class StringsTest {

    @Test
    public void testLeft()  {
        assertThat(Strings.left("Bonjour le monde", 7)).isEqualTo("Bonjour");
        assertThat(Strings.left("Bonjour le monde", 20)).isEqualTo("Bonjour le monde");
        assertThat(Strings.left("Bonjour le monde", 0)).isEqualTo("");
    }

    @Test
    public void testRight()  {
        assertThat(Strings.right("Bonjour le monde", 11)).isEqualTo("monde");
        assertThat(Strings.right("Bonjour le monde", 0)).isEqualTo("Bonjour le monde");
        assertThat(Strings.right("Bonjour le monde", 20)).isEqualTo("");

    }

    @Test
    public void testBytesToHex()  {
        assertThat(Strings.bytesToHex(new byte[]{18, 58, (byte) 188})).isEqualTo("123abc");
    }

    @Test
    public void testUpperCaseFirstLetter()  {
        assertThat(Strings.upperCaseFirstChar("abc")).isEqualTo("Abc");
        assertThat(Strings.upperCaseFirstChar("")).isEqualTo("");
    }

    @Test
    public void testToCamelCase()  {
        assertThat(Strings.toCamelCase("abc_def_ghi")).isEqualTo("AbcDefGhi");
        assertThat(Strings.toCamelCase("abc.def-ghi")).isEqualTo("AbcDefGhi");
        assertThat(Strings.toCamelCase("abc..def.ghi")).isEqualTo("AbcDefGhi");
        assertThat(Strings.toCamelCase("")).isEqualTo("");
    }

    @Test
    public void testReplaceAll() {
        assertThat("abc\\def\\ghi".replace('\\', '/')).isEqualTo("abc/def/ghi");
    }

    @Test
    public void testTrimToNull() {
        assertThat(Strings.trimToNull("\tfoo \n")).isEqualTo("foo");
        assertThat(Strings.trimToNull("\t \n")).isNull();
        assertThat(Strings.trimToNull(null)).isNull();
    }

}
