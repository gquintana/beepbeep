package com.github.gquintana.dbscriptrunner.script;

import java.io.FileNotFoundException;
import java.io.InputStream;

public abstract class ResourceScript extends Script {
    protected final String fullName;

    protected ResourceScript(String fullName) {
        this.fullName = fullName;
    }

    @Override
    public String getName() {
        int slashPos = fullName.lastIndexOf('/');
        if (slashPos < 0) {
            return fullName;
        }
        if (slashPos + 1 < fullName.length()) {
            return fullName.substring(slashPos + 1, fullName.length());
        }
        return "";
    }

    @Override
    public String getFullName() {
        return fullName;
    }

    @Override
    public InputStream getStream() throws FileNotFoundException {
        InputStream inputStream = doGetStream();
        if (inputStream == null) {
            throw new FileNotFoundException("Resource " + fullName + " not found");
        }
        return inputStream;
    }

    protected abstract InputStream doGetStream();

    public static ResourceScript create(Class clazz, String resource) {
        return new FromClass(clazz, resource);
    }

    public static ResourceScript create(ClassLoader classLoader, String resource) {
        return new FromClassLoader(classLoader, resource);
    }

    private static class FromClass extends ResourceScript {
        private final Class clazz;

        public FromClass(Class clazz, String fullName) {
            super(fullName);
            this.clazz = clazz;
        }

        @Override
        protected InputStream doGetStream() {
            return clazz.getResourceAsStream(fullName);
        }
    }

    private static class FromClassLoader extends ResourceScript {
        private final ClassLoader classLoader;

        public FromClassLoader(ClassLoader classLoader, String fullName) {
            super(fullName);
            this.classLoader = classLoader;
        }

        @Override
        protected InputStream doGetStream() {
            return classLoader.getResourceAsStream(fullName);
        }
    }

}
