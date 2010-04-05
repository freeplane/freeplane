package org.freeplane.uispec4j.framework;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;


public class ErrorCheckHandler extends Handler {
	private boolean errorsFound = false;

	public void close() throws SecurityException {
	}

	public void flush() {
	}

	public void publish(LogRecord record) {
		final Level level = record.getLevel();
		if(level == Level.OFF) {
			return;
		}
		if(level.intValue() >= Level.SEVERE.intValue())
		{
			errorsFound = true;
		}
	}				
	public boolean checkErrors(){
		boolean errors = errorsFound;
		errorsFound = false;
		return errors;
	}

};


