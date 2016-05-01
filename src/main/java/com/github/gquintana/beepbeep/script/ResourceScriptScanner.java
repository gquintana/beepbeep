package com.github.gquintana.beepbeep.script;

import com.github.gquintana.beepbeep.pipeline.Consumer;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class ResourceScriptScanner extends ScriptScanner {

    private final ClassLoader classLoader;
    private final Predicate<String> nameFilter;

    public ResourceScriptScanner(ClassLoader classLoader, Predicate<String> nameFilter, Consumer<Script> scriptConsumer) {
        super(scriptConsumer);
        this.classLoader = classLoader;
        this.nameFilter = nameFilter;
    }

    public void scan() throws IOException {
        if (classLoader instanceof URLClassLoader) {
            URLClassLoader urlClassLoader = (URLClassLoader) classLoader;
            for (URL url : urlClassLoader.getURLs()) {
                if (url.getFile().endsWith(".jar")) {
                    scanJar(url);
                } else if (url.getFile().endsWith("/")) {
                    try {
                        scanFolder(url);
                    } catch (URISyntaxException e) {
                        throw new IOException("Failed scanning folder " + url.toString(), e);
                    }
                }
            }
        }
    }

    private void scanJar(URL jarUrl) throws IOException {
        try (JarInputStream jarStream = new JarInputStream(jarUrl.openStream())) {
            JarEntry jarEntry;
            while ((jarEntry = jarStream.getNextJarEntry()) != null) {
                String jarEntryName = jarEntry.getName();
                scanResource(jarEntryName);
            }
        }
    }

    private void scanFolder(URL folderUrl) throws URISyntaxException, IOException {
        Path folderPath = Paths.get(folderUrl.toURI());
        Files.walkFileTree(folderPath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Path filePath = folderPath.relativize(file);
                scanResource(filePath.toString());
                return super.visitFile(file, attrs);
            }
        });
    }

    private void scanResource(String name) {
        if (nameFilter.test(name)) {
            produce(ResourceScript.create(classLoader, name));
        }
    }
}
