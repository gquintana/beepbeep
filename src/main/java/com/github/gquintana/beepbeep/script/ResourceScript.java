package com.github.gquintana.beepbeep.script;

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
            return fullName.substring(slashPos + 1);
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
        private final String localName;
        public FromClass(Class clazz, String localName) {
            super(getResourceFullName(clazz, localName));
            this.clazz = clazz;
            this.localName = localName;
        }

        @Override
        protected InputStream doGetStream() {
            return clazz.getResourceAsStream(localName);
        }
    }

    public static String getResourceFullName(Class clazz, String localName) {
        if (localName.startsWith("/")) {
            return localName.substring(1);
        } else {
            return clazz.getPackage().getName().replaceAll("\\.", "/") + "/" + localName;
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
