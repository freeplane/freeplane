package org.docear.plugin.bibtex.jabref;

import java.io.File;
import java.net.URL;

public class ResolveDuplicateEntryAbortedException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	final private URL url;
	final private File file;

	public ResolveDuplicateEntryAbortedException(URL url) {
		this.url = url;
		this.file = null;
	}
	
	public ResolveDuplicateEntryAbortedException(File file) {
		this.file = file;
		this.url = null;
	}

	public URL getUrl() {
		return this.url;
	}

	public File getFile() {
		return file;
	}
	
}
