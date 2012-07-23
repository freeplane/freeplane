package org.docear.plugin.bibtex.actions;

import java.util.regex.Pattern;

import net.sf.jabref.BasePanel;
import net.sf.jabref.BibtexEntry;
import net.sf.jabref.GUIGlobals;
import net.sf.jabref.imports.ParserResult;
import net.sf.jabref.imports.PostOpenAction;

public class DocearTransformZoteroPathsAction implements PostOpenAction  {

	@Override
	public boolean isActionNecessary(ParserResult pr) {
		// e.g.: ":C:\" instead of JabRef-Style: ":C\:\\"
		Pattern zotero = Pattern.compile(":[a-zA-Z]:\\\\");
		Pattern jabref = Pattern.compile(":[A-Za-z]\\\\:\\\\\\\\");
		
		for (BibtexEntry entry : pr.getDatabase().getEntries()) {
			String fileField = entry.getField(GUIGlobals.FILE_FIELD); 
			if (fileField != null && fileField.trim().length() > 0) {
				if (zotero.matcher(fileField).find()) {
					return true;
				}
				else if (jabref.matcher(fileField).find()) {
					return false;
				}
			}
		}
		
		return false;
	}

	@Override
	public void performAction(BasePanel panel, ParserResult pr) {
		for (BibtexEntry entry : pr.getDatabase().getEntries()) {
			String fileField = entry.getField(GUIGlobals.FILE_FIELD);
			if (fileField != null && fileField.trim().length() > 0) {
				fileField = fileField.replaceAll("\\\\", "\\\\\\\\");
				fileField = fileField.replaceAll(":([a-zA-Z]):", ":$1\\\\:");
				entry.setField(GUIGlobals.FILE_FIELD, fileField);
			}
		}
	}
	
}

