package org.docear.plugin.bibtex.jabref;

import java.util.List;

import net.sf.jabref.BasePanel;
import net.sf.jabref.JabRefFrame;
import net.sf.jabref.gui.MainTable;
import spl.JabRefDraggedFilesEvent;
import spl.JabRefEvent;
import spl.JabRefEventListener;

public class JabrefChangeEventListener implements JabRefEventListener {

	public void processEvent(JabRefEvent event) {
		// if a file was dropped onto the jabref entry table
		if (event instanceof JabRefDraggedFilesEvent) {
			JabRefDraggedFilesEvent evt = (JabRefDraggedFilesEvent) event;

			// check if supposed to be dropped file list is not empty
			String[] fileNames = evt.getFileNames();			
			int dropRow = evt.getDropRow();
			JabRefFrame jabRefFrame = evt.getJabRefFrame();
			BasePanel basePanel = evt.getBasePanel();
			MainTable entryTable = evt.getEntryTable();
			List<String> unhandledFileNames = JabRefCommons.addOrUpdateRefenceEntry(fileNames, dropRow, jabRefFrame, basePanel, entryTable, false);
			
			if (unhandledFileNames != null && unhandledFileNames.size() > 0 && evt.getHandler() != null) {
				// apply default handling to all left over files
				evt.getHandler().loadOrImportFiles(unhandledFileNames.toArray(new String[0]), dropRow);
			}
			// prevent this event from further processing
			event.consume();
		}
	}
}
