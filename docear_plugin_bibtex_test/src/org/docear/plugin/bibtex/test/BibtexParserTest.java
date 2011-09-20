package org.docear.plugin.bibtex.test;

import java.io.File;
import java.io.IOException;

import net.sf.jabref.BibtexDatabase;
import net.sf.jabref.BibtexEntry;
import net.sf.jabref.imports.OpenDatabaseAction;
import net.sf.jabref.imports.ParserResult;

import org.junit.Test;

public class BibtexParserTest {
	
	@Test
	public void testPathParser() {
		File file = new File("C:\\Users\\Anwender\\Dropbox\\Bitex Test\\Jabref Linux\\test.bib");
		try {
			ParserResult parserResult = OpenDatabaseAction.loadDatabase(file, "ISO8859_1");
			BibtexDatabase db = parserResult.getDatabase();
			for(BibtexEntry entry : db.getEntries()){
				String files = entry.getField("file");
				if(files != null && files.length() > 0){
					String[] paths = files.split("(?<!\\\\);"); // taken from splmm, could not test it
	                for(String path : paths){
	                	path = extractPath(path);
	                }
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static String extractPath(String path) {
		String[] array = path.split("(^:|(?<=[^\\\\]):)");
		return "";
	}

}
