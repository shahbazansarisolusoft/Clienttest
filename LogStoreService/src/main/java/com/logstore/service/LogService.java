package com.logstore.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

@Service
public class LogService {
	private final BlockingQueue<String> logQueue = new LinkedBlockingQueue<>(10000); // Handles high throughput
	private static final String LOG_DIRECTORY = "logs/";
//	private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
	private static final long MAX_FILE_SIZE = 200 * 1024; //200kb

	public LogService(S3Uploader s3Uploader) {
		// Ensure logs directory exists
		new File(LOG_DIRECTORY).mkdirs();

		// Start worker thread
		new Thread(() -> processLogs(s3Uploader)).start();
	}

	public void addLog(String logJson) {
		logQueue.offer(logJson); // Add log to queue
	}

	private void processLogs(S3Uploader s3Uploader) {
		File logFile = new File(LOG_DIRECTORY + "logs_" + System.currentTimeMillis() + ".log");
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true));
			while (true) {
				String log = logQueue.poll(1, TimeUnit.SECONDS);
				if (log != null) {
					writer.write(log);
					writer.newLine();
					writer.flush();
				}
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
}