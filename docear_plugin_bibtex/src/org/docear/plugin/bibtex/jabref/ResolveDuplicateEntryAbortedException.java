package org.docear.plugin.bibtex.jabref;

import java.io.File;

public class ResolveDuplicateEntryAbortedException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	final private File file;

	public ResolveDuplicateEntryAbortedException(File f) {
		this.file = f;
	}

	public File getFile() {
		return file;
	}
	
}
