package spl;

import net.sf.jabref.BasePanel;
import net.sf.jabref.JabRefFrame;
import net.sf.jabref.groups.EntryTableTransferHandler;
import net.sf.jabref.gui.MainTable;

public class JabRefDraggedFilesEvent extends JabRefEvent {	
	private final String[] fileNames;
	private final BasePanel basePanel;
	private final MainTable entryTable;
	private final int dropRow;
	private String[] returnFileNames;
	private final EntryTableTransferHandler handler;
	
	public JabRefDraggedFilesEvent(JabRefFrame frame, BasePanel panel, MainTable entryTable, int dropRow, String[] fileNames, EntryTableTransferHandler transferHandler) {
		super(frame);
		this.basePanel = panel;
		this.entryTable = entryTable;
		this.dropRow = dropRow;
		this.fileNames = fileNames;
		this.handler = transferHandler;
	}

	public String[] getFileNames() {
		return fileNames;
	}

	public BasePanel getBasePanel() {
		return basePanel;
	}

	public MainTable getEntryTable() {
		return entryTable;
	}

	public int getDropRow() {
		return dropRow;
	}

	public String[] returnedFileNames() {
		return returnFileNames;
	}
	
	public void setReturnFileNames(String[] fileNames) {
		returnFileNames = fileNames;
	}

	public EntryTableTransferHandler getHandler() {
		return handler;
	}

}
