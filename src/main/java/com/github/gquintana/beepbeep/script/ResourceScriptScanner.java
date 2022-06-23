package com.github.gquintana.beepbeep.script;

import com.github.gquintana.beepbeep.pipeline.Consumer;
import com.github.gquintana.beepbeep.pipeline.ScriptStartEvent;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ResourceScriptScanner extends ScriptScanner {

    private final ClassLoader classLoader;
    private final Predicate<String> nameFilter;

    public ResourceScriptScanner(ClassLoader classLoader, Predicate<String> nameFilter, Consumer<ScriptStartEvent> scriptConsumer) {
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
        List<Path> classPath = new ArrayList<>(Arrays.stream(System.getProperty("java.class.path")
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

    @Override
    public void scan() throws IOException {
        // Same resource can be present multiple times in class path
        Set<String> resources = new HashSet<>();
        for (Path path: getClassPath()) {
            String pathString = path.toString();
            List<String> resourcesList = new ArrayList<>();
            BasicFileAttributes pathAttr = Files.readAttributes(path, BasicFileAttributes.class);
            if (pathAttr.isRegularFile() && pathString.endsWith(".jar")) {
                scanJar(path, resources, resourcesList);
            } else if (pathAttr.isDirectory()) {
                scanFolder(path, resources, resourcesList);
            }
            Collections.sort(resourcesList);
            for(String resource:resourcesList) {
                produce(ResourceScript.create(classLoader, resource));
            }
        }
    }

    private void scanJar(Path jarPath, Set<String> knownResources, List<String> resources) throws IOException {
        try (JarInputStream jarStream = new JarInputStream(Files.newInputStream(jarPath))) {
            JarEntry jarEntry;
            while ((jarEntry = jarStream.getNextJarEntry()) != null) {
                String jarEntryName = jarEntry.getName();
                scanResource(jarEntryName, knownResources, resources);
            }
        }
    }

    private void scanFolder(Path folderPath, Set<String> knownResources, List<String> resources) throws IOException {
        Files.walkFileTree(folderPath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                String filePath = fixFileSeparator(folderPath.relativize(file).toString());
                scanResource(filePath, knownResources, resources);
                return super.visitFile(file, attrs);
            }
        });
    }

    private void scanResource(String name, Set<String> knownResources, List<String> resources) {
        if (nameFilter.test(name) && !knownResources.contains(name)) {
            knownResources.add(name);
            resources.add(name);
        }
    }

    public static ResourceScriptScanner resourceGlob(ClassLoader classLoader, String resourceGlob, Consumer<ScriptStartEvent> scriptConsumer) {
        RegexNamePredicate fileFilter = new RegexNamePredicate(fileGlobToRegex(resourceGlob));
        return new ResourceScriptScanner(classLoader,
            fileFilter,
            scriptConsumer);
    }

    private static final class RegexNamePredicate implements Predicate<String> {
        private final Pattern pattern;

        public RegexNamePredicate(String regex) {
            this.pattern = Pattern.compile(regex);
        }

        @Override
        public boolean test(String name) {
            return pattern.matcher(name).matches();
        }
    }

}
