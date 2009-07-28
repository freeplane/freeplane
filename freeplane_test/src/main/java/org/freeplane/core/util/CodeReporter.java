package org.freeplane.core.util;

public class CodeReporter {
	
	String baseDir;

	public String getBaseDir() {
    	return baseDir;
    }

	public void setBaseDir(String baseDir) {
    	this.baseDir = baseDir;
    }

	public CodeReporter(String baseDir) {
	    super();
	    this.baseDir = baseDir;
    }
	
}
