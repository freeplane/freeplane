package spl.listener;

import spl.JabRefDraggedFilesEvent;
import spl.JabRefEvent;
import spl.JabRefEventListener;
import spl.PdfImporter;

public class PdfImportListener implements JabRefEventListener {

	@Override
	public void processEvent(JabRefEvent event) {
		if (event instanceof JabRefDraggedFilesEvent) {
			final JabRefDraggedFilesEvent evt = (JabRefDraggedFilesEvent) event;
			new Thread(new Runnable() {
				public void run() {
					// Done by MrDlib
					final String[] newfileNames = new PdfImporter(evt.getJabRefFrame(), evt.getBasePanel(), evt.getEntryTable(), evt.getDropRow()).importPdfFiles(evt.getFileNames());
					if (newfileNames.length > 0) {
						evt.getHandler().loadOrImportFiles(newfileNames, evt.getDropRow());												
					}
					// loadOrImportFiles(fileNames, dropRow);
					// Done by MrDlib
				}
			}).start();
			event.consume();
		}

	}

}
