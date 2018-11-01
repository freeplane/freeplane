package org.freeplane.core.util.logging.internal;

import java.io.IOException;
import java.util.logging.FileHandler;

class LogFileHandler extends FileHandler {
	 LogFileHandler(String pattern, int limit, int count, boolean append)
			throws IOException, SecurityException {
		super(pattern, limit, count, append);
	}
}