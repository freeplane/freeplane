package org.docear.plugin.pdfutilities.pdf;

import java.io.File;
import java.io.FileFilter;
import java.util.regex.Pattern;

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
		return isAdobe(file.getName());
	}
	
	public boolean isFoxit(File file){
		return isFoxit(file.getName());
	}
	
	public boolean isPdfXChange(File file){
		return isPdfXChange(file.getName());
	}
	
	public boolean isAdobe(String readerCommand) {
		Pattern regex = Pattern.compile("acro.*.exe");
		return regex.matcher(readerCommand.trim().toLowerCase()).find();
	}
	
	public boolean isFoxit(String readerCommand) {
		Pattern regex = Pattern.compile("foxit.*.exe");
		return regex.matcher(readerCommand.trim().toLowerCase()).find();
	}
	
	public boolean isPdfXChange(String readerCommand){		
		Pattern regex = Pattern.compile("pdfxcv.*.exe");		
		return regex.matcher(readerCommand.trim().toLowerCase()).find();
	}

}
