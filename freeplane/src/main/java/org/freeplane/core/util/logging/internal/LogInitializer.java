package org.freeplane.core.util.logging.internal;

import java.io.File;
import java.io.PrintStream;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;

import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.logging.LogHandlers;

public class LogInitializer {
	private static boolean loggerCreated = false;
	private static final int MAX_LOG_SIZE = 1 * 1024 * 1024;

	public static void createLogger() {
		if (loggerCreated) {
			return;
		}
		loggerCreated = true;
		replaceOutputStream();
		replaceConsoleHandler();
		addFileHandler();
	}

	private static void replaceOutputStream() {
		LoggingOutputStream los;
		los = new LoggingOutputStream(StdFormatter.STDOUT, System.out, MAX_LOG_SIZE);
		System.setOut(new PrintStream(los, true));
		los = new LoggingOutputStream(StdFormatter.STDERR, System.err, MAX_LOG_SIZE);
		System.setErr(new PrintStream(los, true));
	}

	private static void addFileHandler() {
		try {
			final String logDirectoryPath = LogUtils.getLogDirectory();
			final File logDirectory = new File(logDirectoryPath);
			logDirectory.mkdirs();
			if(logDirectory.isDirectory()){
				final String pathPattern = logDirectoryPath + File.separatorChar + "log";
				LogFileHandler fileHandler = new LogFileHandler(pathPattern, 1400000, 5, false);
				fileHandler.setFormatter(new StdFormatter());
				LogHandlers.addHandler(fileHandler);
				if(System.getProperty("java.util.logging.config.file", null) == null){
					fileHandler.setLevel(Level.INFO);
				}
			}
		}
		catch (final Exception e) {
			LogUtils.warn("Error creating logging File Handler", e);
		}
	}

	private static void replaceConsoleHandler() {
		final Handler[] handlers = getRootLogger().getHandlers();
		boolean consoleHandlerRemoved = false;
		for (int i = 0; i < handlers.length; i++) {
			final Handler handler = handlers[i];
			if (handler instanceof ConsoleHandler) {
				getRootLogger().removeHandler(handler);
				consoleHandlerRemoved = true;
			}
		}
		if(consoleHandlerRemoved) {
			final StreamHandler stdConsoleHandler = new StreamHandler(System.out, new StdFormatter()) {
				{
					setLevel(Level.INFO);
				}
				@Override
				public void publish(LogRecord record) {
					super.publish(record);
					flush();
				}
			};

			getRootLogger().addHandler(stdConsoleHandler);
		}

	}

	private static Logger getRootLogger() {
		return Logger.getAnonymousLogger().getParent();
	}
}