package com.github.gquintana.beepbeep.config;

import com.github.gquintana.beepbeep.TestFiles;
import com.github.gquintana.beepbeep.pipeline.PipelineBuilder;
import com.github.gquintana.beepbeep.script.FileScriptScanner;
import com.github.gquintana.beepbeep.script.ScriptScanner;
import com.github.gquintana.beepbeep.sql.SqlScriptStore;
import org.junit.Test;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.Charset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class ConfigurationLoaderTest {
    private static Object getField(Object object, String fieldName) {
        Class clazz = object.getClass();
        Field field = null;
        while (field == null && !clazz.equals(Object.class)) {
            try {
                field = clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                field = null;
            }
            clazz = clazz.getSuperclass();
        }
        if (field == null) {
            fail("Field " + fieldName + " not found");
        }
        if (!field.isAccessible()) {
            field.setAccessible(true);
        }
        try {
            return field.get(object);
        } catch (IllegalAccessException e) {
            fail(e.getMessage());
        }
        return null;
    }

    @Test
    public void testSqlYml() throws Exception {
        // Given
        ConfigurationLoader loader = new ConfigurationLoader();
        // When
        PipelineBuilder pipelineBuilder = null;
        try (InputStream inputStream = TestFiles.getResourceAsStream("config/sql.yml")) {
            pipelineBuilder = loader.load(inputStream);
        }
        ScriptScanner scriptScanner = pipelineBuilder.createScriptScanner();
        // Then
        assertThat(getField(pipelineBuilder, "charset")).isEqualTo(Charset.forName("UTF-8"));
        assertThat(getField(pipelineBuilder, "url")).isEqualTo("jdbc:h2:mem:test");
        assertThat(getField(pipelineBuilder, "username")).isEqualTo("sa");
        assertThat(getField(pipelineBuilder, "password")).isEqualTo(null);
        assertThat(getField(pipelineBuilder, "autoCommit")).isEqualTo(false);
        assertThat(getField(pipelineBuilder, "autoCommit")).isEqualTo(false);
        Object scriptStore = getField(pipelineBuilder, "scriptStore");
        assertThat(scriptStore).isInstanceOf(SqlScriptStore.class);
        assertThat(getField(scriptStore, "table")).isEqualTo("beepbeep");
        assertThat(scriptScanner).isInstanceOf(FileScriptScanner.class);
    }

}
