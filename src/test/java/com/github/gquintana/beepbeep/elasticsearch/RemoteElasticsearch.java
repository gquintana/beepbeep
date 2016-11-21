package com.github.gquintana.beepbeep.elasticsearch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class RemoteElasticsearch {
    private static final Logger LOGGER = LoggerFactory.getLogger(RemoteElasticsearch.class);
    private final String name;
    private final URL downloadUrl;
    private final File targetDir;
    private final File homeDir;
    private Process process;
    private File pidFile;

    public RemoteElasticsearch() {
        try {
            this.name = "elasticsearch-5.0.0";
            this.downloadUrl = new URL("https://artifacts.elastic.co/downloads/elasticsearch/" + name + ".zip");
            this.targetDir = new File("target");
            this.homeDir = new File(targetDir, name);
        } catch (MalformedURLException e) {
            throw new RemoteElasticsearchException("Invalid download URL", e);
        }
    }

    public void download() throws IOException {
        if (homeDir.exists() && homeDir.isDirectory()) {
            return;
        }
        File downloadFile = new File(targetDir, name + ".zip");
        LOGGER.info("Downloading " + name);
        try (InputStream is = downloadUrl.openStream();
             FileOutputStream os = new FileOutputStream(downloadFile)) {
            copyStream(is, os);
        }
        LOGGER.info("Unzipping " + name);
        try (ZipFile zipFile = new ZipFile(downloadFile)) {
            zipFile.stream().forEach(zipEntry -> writeZipEntry(zipFile, zipEntry));
        }

    }

    private void writeZipEntry(ZipFile zipFile, ZipEntry zipEntry) {
        try {
            File targetFile = new File(targetDir, zipEntry.getName());
            if (zipEntry.isDirectory()) {
                targetFile.mkdirs();
            } else {
                try (FileOutputStream os = new FileOutputStream(targetFile)) {
                    copyStream(zipFile.getInputStream(zipEntry), os);
                }
            }
            targetFile.setLastModified(zipEntry.getTime());
        } catch (IOException e) {
            throw new RemoteElasticsearchException("Failed to unzip "+zipFile.getName(), e);
        }
    }

    private static void copyStream(InputStream is, OutputStream os) throws IOException {
        byte[] buffer = new byte[4096];
        int bufferLen;
        while ((bufferLen = is.read(buffer)) >= 0) os.write(buffer, 0, bufferLen);
    }

    public void start() {
        if (process != null) {
            return;
        }
        try {
            download();
            LOGGER.info("Starting " + name + " in " + homeDir.getAbsolutePath());
            pidFile = new File(homeDir, "elasticsearch.pid");
            String[] cmd = isOsWindows() ?
                new String[]{"cmd", "/c", "\"bin\\elasticsearch.bat\""} :
                new String[]{"bash", "bin/elasticsearch", "-d", "-p", pidFile.getName()};
            this.process = Runtime.getRuntime().exec(cmd, null, homeDir);
            if (waitProcessToBeStarted()) {
                LOGGER.info("Started " + name + " with PID " + getPid());
            } else {
                throw new RemoteElasticsearchException(name + " not started");
            }
        } catch (IOException e) {
            throw new RemoteElasticsearchException("Failed to started", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private boolean waitProcessToBeStarted() throws InterruptedException, IOException {
        Thread.sleep(2000L);
        for (int i = 0; i < 58; i++) {
            if (ping() && (isOsWindows() || getPid() !=null)) {
                return true;
            } else {
                Thread.sleep(1000L);
            }
        }
        return false;
    }

    private boolean ping() throws IOException {
        try {
            URLConnection urlConnection = new URL("http", "localhost", 9200, "/").openConnection();
            InputStream content = (InputStream) urlConnection.getContent();
            content.close();
            return true;
        } catch (ConnectException e) {
            return false;
        }
    }

    private boolean isOsWindows() {
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }

    private Integer getPid() throws IOException {
        if (pidFile == null || !pidFile.exists()) {
            return null;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(pidFile))) {
            String line = reader.readLine();
            if (line == null || line.isEmpty()) {
                return null;
            }
            return Integer.valueOf(line.trim());
        }
    }

    public void stop() {
        if (process != null) {
            LOGGER.info("Stopping " + name);
            process.destroy();
            process = null;
        }
        try {
            Integer pid = getPid();
            if (pid != null) {
                Runtime.getRuntime().exec(new String[]{"kill ", pid.toString()});
            }
        } catch (IOException e) {
        } finally {
            pidFile = null;
        }
    }

    public static void main(String[] args) throws Exception {
        RemoteElasticsearch remote = new RemoteElasticsearch();
        remote.start();
        Thread.sleep(30000L);
        remote.stop();
    }
}
