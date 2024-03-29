package com.github.gquintana.beepbeep.config;

import com.github.gquintana.beepbeep.pipeline.PipelineBuilder;
import com.github.gquintana.beepbeep.pipeline.Pipelines;
import com.github.gquintana.beepbeep.util.Converters;
import com.github.gquintana.beepbeep.util.Strings;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.time.Duration;
import java.time.temporal.TemporalAmount;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.github.gquintana.beepbeep.util.Maps.flatten;
import static java.util.Arrays.asList;

public class ConfigurationLoader {
    private final Yaml yaml = new Yaml();

    public PipelineBuilder loadFile(Path configurationFile) {
        try (FileInputStream inputStream = new FileInputStream(configurationFile.toFile())) {
            return load(inputStream);
        } catch (IOException e) {
            throw new ConfigurationException("Failed to read configuration " + configurationFile);
        }
    }

    public PipelineBuilder loadResource(String resource) {
        try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource)) {
            if (inputStream == null) {
                throw new FileNotFoundException("Resource not found");
            }
            return load(inputStream);
        } catch (IOException e) {
            throw new ConfigurationException("Failed to read configuration " + resource, e);
        }
    }

    public PipelineBuilder load(InputStream inputStream) {
        @SuppressWarnings("unchecked")
        Map<String, Object> map = yaml.loadAs(inputStream, Map.class);
        String type = Converters.convertToString(map.get("type"));
        PipelineBuilder pipelineBuilder = Pipelines.create(type);
        for (Map.Entry<String, Object> keyValue : map.entrySet()) {
            String key = Strings.toCamelCase(keyValue.getKey());
            switch (key) {
                case "Type":
                    // Skip
                    break;
                case "Scripts":
                    applyWithScriptScanner(pipelineBuilder, keyValue.getValue());
                    break;
                case "Variables":
                    applyWithVariables(pipelineBuilder, flatten(keyValue.getValue(), null));
                    break;
                case "ScriptStore":
                    applyWithScriptStore(pipelineBuilder, key, keyValue.getValue());
                    break;
                default:
                    applyWith(pipelineBuilder, keyValue.getKey(), keyValue.getValue());
                    break;
            }
        }
        return pipelineBuilder;
    }

    /**
     * Inject script score configuration in PipelineBuilder
     */
    private void applyWithScriptStore(PipelineBuilder pipelineBuilder, String key, Object value) {
        if (value instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> valueMap = (Map<String, Object>) value;
            Object storeName = valueMap.remove("name");
            if (storeName != null) {
                applyWith(pipelineBuilder, key, storeName);
            }
            for (Map.Entry<String, Object> mapEntry : valueMap.entrySet()) {
                applyWith(pipelineBuilder, key + "." + mapEntry.getKey(), mapEntry.getValue());
            }
        } else {
            applyWith(pipelineBuilder, key, value);
        }
    }

    /**
     * Inject script scanners configuration in PipelineBuilder
     */
    private void applyWithScriptScanner(PipelineBuilder pipelineBuilder, Object value) {
        if (value instanceof List) {
            List scripts = (List) value;
            PipelineBuilder.CompositeScriptScannerBuilder compositeBuilder = pipelineBuilder.withCompositeScriptScanner();
            for (Object script : scripts) {
                compositeBuilder.schemes(Converters.convertToString(script));
            }
            compositeBuilder.end();
        } else {
            pipelineBuilder.withFilesScriptScanner(Converters.convertToString(value));
        }
    }

    /**
     * Inject variables in PipelineBuilder
     */
    @SuppressWarnings("unchecked")
    private void applyWithVariables(PipelineBuilder pipelineBuilder, Object value) {
        if (value instanceof Map) {
            pipelineBuilder.withVariables((Map) value);
        }
    }

    /**
     * Call withXXX method on PipelineBuilder
     */
    private void applyWith(PipelineBuilder pipelineBuilder, String key, Object value) {
        String setterName = "with" + Strings.toCamelCase(key);
        Class clazz = pipelineBuilder.getClass();
        List<Method> methods = Arrays.stream(clazz.getMethods())
            .filter(m -> isMethod(m, setterName))
            .collect(Collectors.toList());
        if (methods.isEmpty()) {
            throw new ConfigurationException("Invalid configuration " + key);
        }
        Method method = methods.get(0);
        value = convert(value, method.getParameterTypes()[0]);
        try {
            method.invoke(pipelineBuilder, value);
        } catch (ReflectiveOperationException e) {
            throw new ConfigurationException("Failed to invoke " + method.getName(), e);
        }
    }


    private static final Set<Class> SUPPORTED_PARAMETER_TYPES = new HashSet<>(asList(String.class, Boolean.class, Boolean.TYPE, Charset.class, TemporalAmount.class, Duration.class));

    /**
     * Find withXXX method on PipelineBuilder
     */
    private static boolean isMethod(Method method, String setterName) {
        if (method.getParameterCount() != 1) {
            return false;
        }
        Class parameterClass = method.getParameterTypes()[0];
        if (!SUPPORTED_PARAMETER_TYPES.contains(parameterClass)) {
            return false;
        }
        return method.getName().equals(setterName);
    }

    /**
     * Convert withXXX method parameter to appropriate type
     */
    private <T> T convert(Object object, Class<T> clazz) {
        return Converters.convert(object, clazz);
    }
}
