package com.github.gquintana.dbscriptrunner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

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
}
