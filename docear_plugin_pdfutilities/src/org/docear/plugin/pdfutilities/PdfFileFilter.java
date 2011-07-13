package org.docear.plugin.pdfutilities;

import java.io.File;

public class PdfFileFilter {
	
	public boolean accept(File file) {
        if(file == null) return false;
        
        String path = file.getPath();

        return accept(path);
    }

    public boolean accept(String path) {
        if(path == null || path.isEmpty()) return false;
        
        if(path.matches(".*[.][pP][dD][fF]")){
            return true;
        }        
        else{
            return false;
        }
    }

    

}
