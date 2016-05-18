package com.github.gquintana.beepbeep.config;

import com.github.gquintana.beepbeep.pipeline.PipelineBuilder;
import com.github.gquintana.beepbeep.pipeline.Pipelines;
import com.github.gquintana.beepbeep.util.Strings;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

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
        Map<String, Object> map = yaml.loadAs(inputStream, Map.class);
        String type = convertToString(map.get("type"));
        PipelineBuilder pipelineBuilder = Pipelines.create(type);
        for (Map.Entry<String, Object> keyValue : map.entrySet()) {
            if (keyValue.getKey().equals("type")) {
                // Skip
            } else if (keyValue.getKey().equals("scripts")) {
                applyWithScriptScanner(pipelineBuilder, keyValue.getValue());
            } else if (keyValue.getKey().equals("variables")) {
                applyWithVariables(pipelineBuilder, keyValue.getValue());
            } else {
                applyWith(pipelineBuilder, keyValue.getKey(), keyValue.getValue());
            }
        }
        return pipelineBuilder;
    }

    private void applyWithScriptScanner(PipelineBuilder pipelineBuilder, Object value) {
        if (value instanceof List) {
            List scripts = (List) value;
            PipelineBuilder.CompositeScriptScannerBuilder compositeBuilder = pipelineBuilder.withCompositeScriptScanner();
            for (Object script : scripts) {
                compositeBuilder.files(convertToString(script));
            }
            compositeBuilder.end();
        } else {
            pipelineBuilder.withFilesScriptScanner(convertToString(value));
        }
    }

    private void applyWithVariables(PipelineBuilder pipelineBuilder, Object value) {
        if (value instanceof Map) {
            pipelineBuilder.withVariables((Map) value);
        }
    }

    /**
     * Call withXXX method on PipelineBuilder
     */
    private void applyWith(PipelineBuilder pipelineBuilder, String key, Object value) {
        String setterName = "with" + Character.toUpperCase(key.charAt(0)) + Strings.right(key, 1);
        Class clazz = pipelineBuilder.getClass();
        List<Method> methods = Arrays.stream(clazz.getMethods())
            .filter(m -> isMethod(m, key, setterName))
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

    private static final Set<Class> SUPPORTED_PARAMETER_TYPES = new HashSet<>(asList(String.class, Boolean.class, Boolean.TYPE, Charset.class));

    /**
     * Find withXXX method on PipelineBuilder
     */
    private static boolean isMethod(Method method, String fieldName, String setterName) {
        if (method.getParameterCount() != 1) {
            return false;
        }
        Class parameterClass = method.getParameterTypes()[0];
        if (!SUPPORTED_PARAMETER_TYPES.contains(parameterClass)) {
            return false;
        }
        if (method.getName().equals(setterName)) {
            return true;
        }
        return false;
    }

    /**
     * Convert withXXX method parameter to appropriate type
     */
    private <T> T convert(Object object, Class<T> clazz) {
        Object result;
        if (object == null || clazz.isInstance(object)) {
            result = object;
        } else if (clazz.equals(String.class)) {
            result = (object instanceof String ? (String) object : object.toString());
        } else if (clazz.equals(Charset.class)) {
            result = Charset.forName(convertToString(object));
        } else if (clazz.equals(Boolean.class) || clazz.equals(Boolean.TYPE)) {
            result = Boolean.valueOf(convertToString(object));
        } else {
            throw new ConfigurationException("Don't known how to convert to " + clazz);
        }
        return clazz.isPrimitive() ? (T) result : clazz.cast(result);
    }

    private String convertToString(Object object) {
        return convert(object, String.class).trim();
    }
}
