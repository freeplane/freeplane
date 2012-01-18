package org.docear.plugin.pdfutilities.util;


import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import org.docear.plugin.core.util.Tools;

public class CustomFileListFilter implements FileFilter{
	
	List<String> regexList = new ArrayList<String>();
	
	public CustomFileListFilter(String property){
		List<String> temp = Tools.getStringList(property);
		for(String s : temp){
			if(!s.contains(".")) continue; //$NON-NLS-1$
			s = s.substring(s.indexOf(".") + 1); //$NON-NLS-1$
			String regex = ".*[.]"; //$NON-NLS-1$
			for(char c : s.toCharArray()){
				regex = regex + "[" + Character.toLowerCase(c) + Character.toUpperCase(c) + "]"; //$NON-NLS-1$ //$NON-NLS-2$
			}
			regexList.add(regex);
		}
	}

	public boolean accept(File pathname) {
		for(String regex : this.regexList){
			if(new CustomFileFilter(regex).accept(pathname)){
				return true;
			}			
		}
		return false;
	}

}
