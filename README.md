Log Store Service - Compressed Version  

# Overview  
This Spring Boot application logs events into a local `logs/` folder and automatically uploads them to Amazon S3 when the file size exceeds 200 KB (configurable). It uses BlockingQueue for efficient log handling.  

# Project Structure  
- `logs/` → Stores generated log files.  
- `src/` → Contains the application source code.  

# Running the Project  
1. Clone the Repository and build the project using Maven/Gradle.  
2. Configure AWS credentials in `application.properties`.  
3. Run the Application via an IDE or using `java -jar target/logstore-service.jar`.  

# Logging Mechanism  
- Logs are buffered using BlockingQueue for high throughput.  
- Files are uploaded to S3 when they exceed 200 KB or after 60 seconds.  

# Testing  
- Run `log_flood.bat` to generate logs rapidly.
- If the file exceeds MAX_FILE_SIZE, it is uploaded to S3, and a new file is created.
Every 60 seconds, the system checks the file:
 - Uploads if modified and not empty.
 - Skips upload if unchanged.
Handling Server Restarts
On restart, the service reuses the latest log file under MAX_FILE_SIZE instead of creating a new one.
If no valid file exists, a new file is created.
- Monitor the `logs/` folder and check S3 uploads.  

# Troubleshooting  
- Check console logs for errors.  
- Ensure AWS credentials & permissions are correctly set.  

This service efficiently stores logs in S3, ensuring secure and scalable log management!
