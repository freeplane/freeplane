package org.freeplane.features.mindmapmode.file;

import java.io.File;
import java.io.IOException;

@SuppressWarnings("serial")
class SkipException extends IOException {

	public SkipException(File file) {
	    super("skip " + file.toString());
    }
}
