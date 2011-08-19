package org.docear.plugin.pdfutilities.util;

import java.io.File;
import java.io.FileFilter;

public class DirectoryFileFilter implements FileFilter {

    public boolean accept(File file) {
        if(file.isDirectory()){
            /*ArrayList<String> subfolders = SplmmPreferences.getStringList(SplmmPreferences.SUBFOLDERS, SciPloreUtils.DEFAULT_SUBFOLDERS);
            for(String subfolder : subfolders){
                if(file.getName().equalsIgnoreCase(subfolder)){
                    return false;
                }
            }*/
            return true;
        }
        else{
            return false;
        }
    }

}
