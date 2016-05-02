package com.github.gquintana.beepbeep;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Target;
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
}
