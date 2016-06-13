package com.github.gquintana.beepbeep.util;

import org.junit.Test;

import java.nio.charset.Charset;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class ConvertersTest {

    @Test
    public void testConvertBoolean() throws Exception {
        assertThat(Converters.convert("true", Boolean.class)).isTrue();
        assertThat(Converters.convert("false", Boolean.TYPE)).isFalse();
        assertThat(Converters.convert("", Boolean.class)).isNull();
    }

    @Test
    public void testConvertEnum() throws Exception {
        assertThat(Converters.convert("seconds", TimeUnit.class)).isEqualTo(TimeUnit.SECONDS);
        assertThat(Converters.convert("", TimeUnit.class)).isNull();
    }

    @Test
    public void testConvertDuration() throws Exception {
        assertThat(Converters.convert("2s", Duration.class)).isEqualTo(Duration.ofSeconds(2L));
        assertThat(Converters.convert("1m53s", Duration.class)).isEqualTo(Duration.ofSeconds(60+53));
        assertThat(Converters.convert("", Duration.class)).isNull();
    }

    @Test
    public void testConvertInteger() throws Exception {
        assertThat(Converters.convert("123", Integer.class)).isEqualTo(123);
        assertThat(Converters.convert("456", Integer.TYPE)).isEqualTo(456);
        assertThat(Converters.convert("", Integer.class)).isNull();
    }

    @Test
    public void testConvertLong() throws Exception {
        assertThat(Converters.convert("123", Long.class)).isEqualTo(123L);
        assertThat(Converters.convert("456", Long.TYPE)).isEqualTo(456L);
        assertThat(Converters.convert("", Long.class)).isNull();
    }

    @Test
    public void testConvertCharset() throws Exception {
        assertThat(Converters.convert("utf-8", Charset.class)).isEqualTo(Charset.forName("UTF-8"));
        assertThat(Converters.convert("iso-8859-1", Charset.class)).isEqualTo(Charset.forName("ISO-8859-1"));
        assertThat(Converters.convert("", Charset.class)).isNull();
    }
}
