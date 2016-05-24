package com.github.gquintana.beepbeep;

import com.github.gquintana.beepbeep.script.ResourceScript;
import com.github.gquintana.beepbeep.util.Strings;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.EnumSet;
import java.util.regex.Pattern;

public class TestFiles {
    public static void copy(InputStream inputStream, OutputStream outputStream) throws IOException {
        byte[] buffer = new byte[4096];
        int bufferLen;
        while ((bufferLen = inputStream.read(buffer)) >= 0) {
            outputStream.write(buffer, 0, bufferLen);
        }
    }

    public static void writeResource(String sourceResource, File targetFile) throws IOException {
        try (InputStream inputStream = getResourceAsStream(sourceResource);
             FileOutputStream outputStream = new FileOutputStream(targetFile)) {
            copy(inputStream, outputStream);
        }
    }

    public static InputStream getResourceAsStream(String sourceResource) throws FileNotFoundException {
        InputStream stream = TestFiles.class.getResourceAsStream(sourceResource);
        if (stream == null) {
            throw new FileNotFoundException("Resource " + sourceResource + " not found");
        }
        return stream;
    }

    /**
     * Get resource file size
     */
    public static long getResourceSize(String resource) throws IOException {
        URL url = getResourceAsUrl(resource);
        try {
            return new File(url.toURI()).length();
        } catch (URISyntaxException e) {
            // Workaround when resource is in a Jar file
            try (InputStream inputStream = getResourceAsStream(resource)) {
                byte[] buffer = new byte[4096];
                long size = 0;
                int bufferLen;
                while ((bufferLen = inputStream.read(buffer)) >= 0) {
                    size += bufferLen;
                }
                return size;
            }
        }
    }

    public static URL getResourceAsUrl(String resource) throws FileNotFoundException {
        URL url = TestFiles.class.getResource(resource);
        if (url == null) {
            throw new FileNotFoundException("Resource " + resource + " not found");
        }
        return url;
    }

    public static String getResourceFullName(String resource) {
        return ResourceScript.getResourceFullName(TestFiles.class, resource);
    }

    public static FolderNode folder(String name, Node... children) {
        return new FolderNode(name, children);
    }

    public static FileNode file(String name) {
        return new FileNode(name);
    }

    public static abstract class Node {
        protected final String name;
        protected FolderNode parent;

        protected Node(String name) {
            this.name = name;
        }

        public abstract File create(File rootFolder) throws IOException;
    }

    public static class FolderNode extends Node {
        private final Node[] children;

        protected FolderNode(String name, Node... children) {
            super(name);
            this.children = children;
            for (Node child : children) {
                child.parent = this;
            }
        }

        public File create(File rootFolder) throws IOException {
            File folder = new File(rootFolder, name);
            folder.mkdir();
            for (Node child : children) {
                child.create(folder);
            }
            return folder;
        }
    }

    public static class FileNode extends Node {
        protected FileNode(String name) {
            super(name);
        }

        public File create(File rootFolder) throws IOException {
            File file = new File(rootFolder, name);
            file.createNewFile();
            return file;
        }
    }

    /**
     * Recursively delete a directory
     */
    public static void delete(Path path) throws IOException {
        Files.walkFileTree(path, Collections.emptySet(), 5, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                file.toFile().delete();
                return super.visitFile(file, attrs);
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                dir.toFile().delete();
                return super.postVisitDirectory(dir, exc);
            }
        });
    }

    /**
     * Convert Unix file separator to Unix/Windows file separator
     */
    public static String adaptFileSeparator(String path) {
        return File.separatorChar == '/' ? path : path.replace('/', File.separatorChar);
    }
}
