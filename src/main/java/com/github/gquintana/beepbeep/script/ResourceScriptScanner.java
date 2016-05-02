package com.github.gquintana.beepbeep.script;

import com.github.gquintana.beepbeep.pipeline.Consumer;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.stream.Collectors;

public class ResourceScriptScanner extends ScriptScanner {

    private final ClassLoader classLoader;
    private final Predicate<String> nameFilter;

    public ResourceScriptScanner(ClassLoader classLoader, Predicate<String> nameFilter, Consumer<Script> scriptConsumer) {
        super(scriptConsumer);
        this.classLoader = classLoader;
        this.nameFilter = nameFilter;
    }

    private List<Path> getClassPath() {
        final Set<Path> bootClassPath =
            Arrays.stream(System.getProperty("sun.boot.class.path")
                .split(File.pathSeparator))
                .map(Paths::get)
                .collect(Collectors.toSet());
        List<Path> classPath = new ArrayList<>();
        classPath.addAll(
            Arrays.stream(System.getProperty("java.class.path")
                .split(File.pathSeparator))
                .map(Paths::get)
                .filter(f -> !bootClassPath.contains(f))
                .collect(Collectors.toList()));
        if (classLoader instanceof URLClassLoader) {
            URLClassLoader urlClassLoader = (URLClassLoader) classLoader;
            for (URL url : urlClassLoader.getURLs()) {
                Path path;
                try {
                    path = Paths.get(url.toURI());
                } catch (URISyntaxException e) {
                    path = Paths.get(url.getPath());
                }
                if (!bootClassPath.contains(path) && !classPath.contains(path)) {
                    classPath.add(path);
                }
            }
        }
        return classPath;
    }

    public void scan() throws IOException {
        for (Path path: getClassPath()) {
            String pathString = path.toString();
            File pathFile = path.toFile();
            if (pathFile.isFile() && pathString.endsWith(".jar")) {
                scanJar(path);
            } else if (pathFile.isDirectory()) {
                try {
                    scanFolder(path);
                } catch (URISyntaxException e) {
                    throw new IOException("Failed scanning folder " + pathString, e);
                }
            }
        }
    }

    private void scanJar(Path jarPath) throws IOException {
        try (JarInputStream jarStream = new JarInputStream(Files.newInputStream(jarPath))) {
            JarEntry jarEntry;
            while ((jarEntry = jarStream.getNextJarEntry()) != null) {
                String jarEntryName = jarEntry.getName();
                scanResource(jarEntryName);
            }
        }
    }

    private void scanFolder(Path folderPath) throws URISyntaxException, IOException {
        Files.walkFileTree(folderPath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                String filePath = fixFileSeparator(folderPath.relativize(file).toString());
                scanResource(filePath);
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
