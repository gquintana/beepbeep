package com.github.gquintana.beepbeep.script;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
public class ScriptConfigurationTest {

    @Test
    public void testParse() throws Exception {
        // Given
        ScriptConfiguration configuration = new ScriptConfiguration();
        // When
        configuration.parse("-- Hello");
        configuration.parse("-- beepbeep foo=bar");
        configuration.parse("select * from person");
        configuration.parse("-- beepbeep baz=true timeUnit=seconds");
        // Then
        assertThat(configuration.getValue("foo", String.class).get()).isEqualTo("bar");
        assertThat(configuration.getValue("baz", Boolean.class).get()).isTrue();
        assertThat(configuration.getValue("timeUnit", TimeUnit.class).get()).isEqualTo(TimeUnit.SECONDS);
    }
}
