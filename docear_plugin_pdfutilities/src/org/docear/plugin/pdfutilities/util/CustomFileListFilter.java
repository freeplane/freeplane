package org.docear.plugin.pdfutilities.util;


import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

public class CustomFileListFilter implements FileFilter{
	
	List<String> regexList = new ArrayList<String>();
	
	public CustomFileListFilter(String property){
		List<String> temp = Tools.getStringList(property);
		for(String s : temp){
			if(!s.contains(".")) continue;
			s = s.substring(s.indexOf(".") + 1);
			String regex = ".*[.]";
			for(char c : s.toCharArray()){
				regex = regex + "[" + Character.toLowerCase(c) + Character.toUpperCase(c) + "]";
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
