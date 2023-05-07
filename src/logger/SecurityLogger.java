package logger;

import utils.Constants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.logging.*;

/**
 * SecurityLogger
 *
 * @author Jose L. Navío Mendoza
 */

public class SecurityLogger {

    static {
        try {
            LogManager.getLogManager().readConfiguration(SecurityLogger.class.getResourceAsStream("/logger/logging.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(SecurityLogger.class.getName());

    public SecurityLogger(String fileName) {
        try {

            String logFilePath = Constants.LOGS_DIRECTORY + File.separator + fileName;

            // Create a file handler

            Handler fileHandler = new CustomFileHandler(logFilePath);
            fileHandler.setFormatter(new Formatter() {
                @Override
                public String format(LogRecord record) {
                    String timestamp = new Date(record.getMillis()).toString();
                    String level = record.getLevel().toString();
                    String message = formatMessage(record);
                    return timestamp + " " + level + " --> " + message + "\n";
                }
            });

            // Remove existing handlers to avoid duplicate logging
            logger.setUseParentHandlers(false);
            for (Handler handler : logger.getHandlers()) {
                logger.removeHandler(handler);
            }

            // Add the file handler to the logger
            logger.addHandler(fileHandler);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void log(Level level, String message) {
        logger.log(level, message);
    }

    public void severe(String message) {
        log(Level.SEVERE, message);
    }

    public void warning(String message) {
        log(Level.WARNING, message);
    }

    public void info(String message) {
        log(Level.INFO, message);
    }

    public void config(String message) {
        log(Level.CONFIG, message);
    }

    public void fine(String message) {
        log(Level.FINE, message);
    }

    public void finer(String message) {
        log(Level.FINER, message);
    }

    public void finest(String message) {
        log(Level.FINEST, message);
    }

    // Method to close the log file
    public void closeLogFile() {
        Handler[] handlers = logger.getHandlers();
        for (Handler handler : handlers) {
            handler.close();
        }
    }
}
//Custom handler class so the log file is not clear each time you instantiate the file handler
class CustomFileHandler extends StreamHandler {
    public CustomFileHandler(String fileName) throws IOException {
        // Create a FileOutputStream in append mode
        FileOutputStream fos = new FileOutputStream(fileName, true);
        // Use the FileOutputStream as the output stream for the handler
        setOutputStream(fos);
    }
}