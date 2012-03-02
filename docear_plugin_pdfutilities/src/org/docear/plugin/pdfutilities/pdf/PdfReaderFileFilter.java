package org.docear.plugin.pdfutilities.pdf;

import java.io.File;
import java.io.FileFilter;

import org.freeplane.core.util.Compat;

public class PdfReaderFileFilter implements FileFilter {

	public boolean accept(File file) {
		if(Compat.isMacOsX()){
			return true;
		}
		
		if(file == null || !file.exists() || !file.isFile()){
			return false;
		}
		
		if(!Compat.isWindowsOS()){
			return true;
		}
		
		if(isAdobe(file)){
			return true;
		}
		
		if(isPdfXChange(file)){
			return true;
		}
		
		if(isFoxit(file)){
			return true;
		}
		
		return false;
	}
	
	public boolean isAdobe(File file){
		return file.getName().matches("[Aa][Cc][Rr][Oo].*[.][Ee][Xx][Ee]"); //$NON-NLS-1$
	}
	
	public boolean isFoxit(File file){
		return file.getName().matches("[Ff][Oo][Xx].*[.][Ee][Xx][Ee]"); //$NON-NLS-1$
	}
	
	public boolean isPdfXChange(File file){
		return file.getName().matches("[Pp][Dd][Ff][Xx][Cc].*[.][Ee][Xx][Ee]"); //$NON-NLS-1$
	}

}
