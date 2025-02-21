package com.logstore.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.*;

import org.springframework.stereotype.Service;

@Service
public class LogService {
    private final BlockingQueue<String> logQueue = new LinkedBlockingQueue<>(10000); // Handles high throughput
    private static final String LOG_DIRECTORY = "logs/";
    private static final long MAX_FILE_SIZE = 100 * 1024; // 100 KB
    private static final long CHECK_INTERVAL = 60; // Check every 60 seconds

    private volatile File logFile;
    private volatile long lastModifiedTime = 0;

    public LogService(S3Uploader s3Uploader) {
        // Ensure logs directory exists
        new File(LOG_DIRECTORY).mkdirs();

        // Initialize log file: use existing file if available
        logFile = getLatestLogFile();

        // Start log processing thread
        new Thread(() -> processLogs(s3Uploader)).start();

        // Start scheduled task to check file modifications every 60 seconds
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> checkAndUpload(s3Uploader), 0, CHECK_INTERVAL, TimeUnit.SECONDS);
    }

    public void addLog(String logJson) {
        logQueue.offer(logJson); // Add log to queue
    }

    private void processLogs(S3Uploader s3Uploader) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true));
            while (true) {
                String log = logQueue.poll(1, TimeUnit.SECONDS);
                if (log != null) {
                    writer.write(log);
                    writer.newLine();
                    writer.flush();
                }

                // If file exceeds max size, upload and create a new file
                if (logFile.length() > MAX_FILE_SIZE) {
                    writer.close();
                    s3Uploader.uploadToS3(logFile);
                    logFile = new File(LOG_DIRECTORY + "logs_" + System.currentTimeMillis() + ".log");
                    writer = new BufferedWriter(new FileWriter(logFile, true));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkAndUpload(S3Uploader s3Uploader) {
        try {
            if (logFile.exists() && logFile.length() > 0 && logFile.length() < MAX_FILE_SIZE) { // Ensure file is NOT empty
                BasicFileAttributes attrs = Files.readAttributes(logFile.toPath(), BasicFileAttributes.class);
                long currentModifiedTime = attrs.lastModifiedTime().toMillis();

                // Upload only if file was modified since the last check
                if (currentModifiedTime > lastModifiedTime) {
                    lastModifiedTime = currentModifiedTime;
                    s3Uploader.uploadToS3(logFile);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the latest log file in the directory (if it exists and is under MAX_FILE_SIZE).
     */
    private File getLatestLogFile() {
        File logDir = new File(LOG_DIRECTORY);
        File[] logFiles = logDir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.startsWith("logs_") && name.endsWith(".log");
            }
        });

        if (logFiles != null && logFiles.length > 0) {
            // Sort files by last modified time (latest first)
            Arrays.sort(logFiles, Comparator.comparingLong(File::lastModified).reversed());

            // Pick the most recent file if it is under MAX_FILE_SIZE
            for (File file : logFiles) {
                if (file.length() < MAX_FILE_SIZE) {
                    return file;
                }
            }
        }

        // No suitable file found, create a new one
        return new File(LOG_DIRECTORY + "logs_" + System.currentTimeMillis() + ".log");
    }
}
