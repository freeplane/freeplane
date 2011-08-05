package org.docear.plugin.pdfutilities.util;

import java.io.File;
import java.io.FileFilter;

public class ExeFileFilter implements FileFilter {

	public boolean accept(File file) {
		if(file == null || !file.exists() || !file.isFile()){
			return false;
		}
		
		if(isExe(file)){
			return true;
		}		
		
		
		return false;
	}
	
	public boolean isExe(File file){
		return file.getName().matches(".*[.][Ee][Xx][Ee]");
	}

}
