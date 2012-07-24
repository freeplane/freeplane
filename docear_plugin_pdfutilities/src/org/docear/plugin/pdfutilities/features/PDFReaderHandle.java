package org.docear.plugin.pdfutilities.features;

import java.io.File;

public class PDFReaderHandle {

	private final String name;
	private final String execFile;
	
	/**
	 * @param Name
	 * @param file
	 */
	public PDFReaderHandle(String name, String execFile) {
		this.name = name;
		this.execFile = execFile;
	}	

	/**
	 * @return
	 */
	public String getName() {
		return this.name;
	}
	
	public String toString() {
		return this.getName();
	}

	public String getExecFile() {
		return execFile;
	}
}
