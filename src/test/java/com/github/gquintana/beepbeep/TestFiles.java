package com.github.gquintana.beepbeep;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;

public class TestFiles {
    public static void writeResource(String sourceResource, File targetFile) throws IOException {
        try (InputStream inputStream = TestFiles.class.getResourceAsStream(sourceResource);
             FileOutputStream outputStream = new FileOutputStream(targetFile)) {
            byte[] buffer = new byte[4096];
            int bufferLen;
            while ((bufferLen = inputStream.read(buffer)) >= 0) {
                outputStream.write(buffer, 0, bufferLen);
            }
        }
    }

    /**
     * Get resource file size
     */
    public static long getResourceSize(String resource) throws IOException{
        URL url = TestFiles.class.getResource(resource);
        try {
            return new File(url.toURI()).length();
        } catch (URISyntaxException e) {
            // Workaround when resource is in a Jar file
            try (InputStream inputStream = TestFiles.class.getResourceAsStream(resource)) {
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

    public static FolderNode folder(String name, Node ... children) {
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
        protected FolderNode(String name, Node ... children) {
            super(name);
            this.children = children;
            for(Node child: children) {
                child.parent = this;
            }
        }

        public File create(File rootFolder) throws IOException {
            File folder = new File(rootFolder, name);
            folder.mkdir();
            for(Node child: children) {
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
}
