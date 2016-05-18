package com.github.gquintana.beepbeep.config;

import com.github.gquintana.beepbeep.TestFiles;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class YamlTest {

    @Test
    public void testLoad() throws IOException {
        // Given
        Yaml yaml = new Yaml();
        // When
        try(InputStream inputStream = TestFiles.getResourceAsStream("config/sql.yml")) {
            Object object = yaml.load(inputStream);
            System.out.println(object);
        }
    }
}
