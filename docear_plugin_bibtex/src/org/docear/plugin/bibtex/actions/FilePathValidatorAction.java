package org.docear.plugin.bibtex.actions;

import java.io.File;

import net.sf.jabref.BasePanel;
import net.sf.jabref.BibtexDatabase;
import net.sf.jabref.BibtexEntry;
import net.sf.jabref.imports.ParserResult;
import net.sf.jabref.imports.PostOpenAction;

import org.docear.plugin.bibtex.JabRefAttributes;
import org.freeplane.core.util.LogUtils;

public class FilePathValidatorAction implements PostOpenAction {

	@Override
	public boolean isActionNecessary(ParserResult pr) {
		return true;
	}

	@Override
	public void performAction(BasePanel panel, ParserResult pr) {
		// JabRefAttributes attributes =
		// ReferencesController.getController().getJabRefAttributes();
		BibtexDatabase database = pr.getDatabase();

		boolean changes = false;
		for (BibtexEntry entry : database.getEntries()) {
			changes = correctEntryIfNeeded(entry) || changes;
		}

		if (changes) {
			LogUtils.warn("BibTeX database provided by " + pr.getFile().getPath()
					+ " seems to be created by mendeley. Therefore some filepaths needed to be corrected.");
		}
	}

	public boolean correctEntryIfNeeded(BibtexEntry entry) {
		boolean changes = false;
		String jabrefFiles = entry.getField("file");
		if (jabrefFiles != null) {
			// path linked in jabref
			for (String filePath : JabRefAttributes.extractPaths(jabrefFiles)) {
				File file = new File(filePath);
				if (!filePath.startsWith("/") && !file.exists()) {
					File fileCorrected = new File("/" + filePath);
					if (fileCorrected.exists()) {
						changes = true;
						jabrefFiles = jabrefFiles.replace(file.getPath(), fileCorrected.getPath());
					}
				}
			}
			if (changes) {
				entry.setField("file", jabrefFiles);
			}
		}
		

		return changes;
	}

}
