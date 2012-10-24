package org.docear.plugin.bibtex.jabref;

import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.swing.JOptionPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.ws.rs.core.MultivaluedMap;

import net.sf.jabref.BasePanel;
import net.sf.jabref.BibtexEntry;
import net.sf.jabref.BibtexEntryType;
import net.sf.jabref.EntryTypeDialog;
import net.sf.jabref.FocusRequester;
import net.sf.jabref.Globals;
import net.sf.jabref.JabRefFrame;
import net.sf.jabref.KeyCollisionException;
import net.sf.jabref.Util;
import net.sf.jabref.export.DocearReferenceUpdateController;
import net.sf.jabref.external.DroppedFileHandler;
import net.sf.jabref.gui.MainTable;
import net.sf.jabref.imports.ImportMenuItem;
import net.sf.jabref.labelPattern.LabelPatternUtil;
import net.sf.jabref.undo.UndoableInsertEntry;
import net.sf.jabref.util.XMPUtil;

import org.docear.plugin.bibtex.Reference;
import org.docear.plugin.bibtex.ReferenceUpdater;
import org.docear.plugin.bibtex.ReferencesController;
import org.docear.plugin.bibtex.dialogs.PdfMetadataListDialog;
import org.docear.plugin.bibtex.dialogs.PdfTitleQuestionDialog;
import org.docear.plugin.core.mindmap.MindmapUpdateController;
import org.docear.plugin.pdfutilities.map.AnnotationController;
import org.docear.plugin.services.communications.CommunicationsController;
import org.docear.plugin.services.communications.features.DocearServiceResponse;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.Controller;

import spl.Tools;
import spl.gui.ImportDialog;

import com.sun.jersey.core.util.StringKeyStringValueIgnoreCaseMultivaluedMap;

public abstract class JabRefCommons {
	
	static class MetadataRequestTask implements Callable<MetadataCallableResult> {
		private MetadataCallableResult result;
		private Runnable task;

		private MetadataRequestTask(Runnable task, MetadataCallableResult result) {
			this.task = task;
			this.result = result;
		}

		public static Callable<MetadataCallableResult> create(Runnable task, MetadataCallableResult result) {
			return new MetadataRequestTask(task, result);
		}

		public MetadataCallableResult call() throws Exception {
			if (task == null) {
				return this.result;
			}
			task.run();
			return this.result;
		}

	}

	public static class MetadataCallableResult {
		private String result;
		private String errorText;

		public static MetadataCallableResult newInstance() {
			return new MetadataCallableResult();
		}

		public void setResult(String string) {
			if (string == null || string.trim().length() <= 0) {
				this.result = null;
			} else {
				this.result = string;
			}
		}

		public String getResult() {
			return this.result;
		}

		public void setError(String text) {
			this.errorText = text;
		}

		public boolean hasError() {
			return this.errorText != null;
		}

		public String getError() {
			return errorText;
		}

		public String toString() {
			return getResult();
		}

	}

	private static void updateEntryInDatabase(File file, BibtexEntry selected, BibtexEntry oldEntry) {
		if (selected == null) {
			return;
		}
		BibtexEntryType type = selected.getType();
		if (type != null) {
			oldEntry.setType(type);
		}

		addMissingFields(oldEntry, selected);
//		insertFields(oldEntry.getRequiredFields(), oldEntry, selected);
//		insertFields(oldEntry.getGeneralFields(), oldEntry, selected);
//		insertFields(oldEntry.getOptionalFields(), oldEntry, selected);

		JabrefWrapper wrapper = ReferencesController.getController().getJabrefWrapper();
		if(file != null) {
			new JabRefAttributes().removePdfFromBibtexEntry(file, oldEntry);
			DroppedFileHandler dfh = new DroppedFileHandler(wrapper.getJabrefFrame(), wrapper.getBasePanel());
			// DOCEAR - change file path to relative to bib-library path?
			dfh.linkPdfToEntry(file.getPath(), oldEntry);
		}
		else {
			runCurrentMapUpdate();
		}
		showInReferenceManager(oldEntry);

	}
	
	private static void runCurrentMapUpdate() {
		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				try {
					if (DocearReferenceUpdateController.isLocked()) {
						return;
					}
					DocearReferenceUpdateController.lock();
					
					MapModel currentMap = Controller.getCurrentController().getMap();
					if (currentMap == null) {
						return;
					}

					MindmapUpdateController mindmapUpdateController = new MindmapUpdateController(false);
					mindmapUpdateController.addMindmapUpdater(new ReferenceUpdater(TextUtils.getText("update_references_current_mindmap")));
					mindmapUpdateController.updateCurrentMindmap(true);
				}
				finally {
					DocearReferenceUpdateController.unlock();
				}
			}

		});
	}

	private static void addMissingFields(BibtexEntry oldEntry, BibtexEntry newData) {
		DocearReferenceUpdateController.lock();
		Collection<String> fields = newData.getAllFields();
		for (String field : fields) {
			if (oldEntry.getField(field) == null) {
				oldEntry.setField(field, newData.getField(field));
			}
		}
		DocearReferenceUpdateController.unlock();
	}
	
	

	
	
	public static List<String> addOrUpdateRefenceEntry(String[] fileNames, int dropRow, JabRefFrame jabRefFrame, BasePanel basePanel, MainTable entryTable, boolean chooseFirst) {
		List<String> unhandledFileNames = new ArrayList<String>();
		if (fileNames != null && fileNames.length > 0) {			
			for (String fileName : fileNames) {

				// create document hash and try to extract the title for
				// each file that is of type pdf
				if (fileName.toLowerCase().endsWith(".pdf")) {
					URI fileUri = new File(fileName).toURI();
					
					ImportDialog importDialog = new ImportDialog(dropRow, fileName, (chooseFirst ? (dropRow < 0) : null));
					Tools.centerRelativeToWindow(importDialog, UITools.getFrame());
					
					String hash = AnnotationController.getDocumentHash(fileUri);
					if(hash == null) {
						importDialog.getRadioButtonMrDlib().setEnabled(false);
						importDialog.getRadioButtonUpdateEmptyFields().setEnabled(false);
						importDialog.getRadioButtonMrDlib().setSelected(false);
						importDialog.getRadioButtonNoMeta().setSelected(true);
					}
					
					List<BibtexEntry> xmpEntriesInFile = readXmpEntries(fileName);
					if ((xmpEntriesInFile == null) || (xmpEntriesInFile.size() == 0)) {
						importDialog.getRadioButtonXmp().setEnabled(false);
					}
					
					if(chooseFirst) {
						importDialog.showDialog();						
					}
					else if (dropRow == -1 && hash != null) { // dropped on a new area
						// create new entry (with metadata? empty entry?
						try {
							showMetadataDialog(fileUri);
						} catch (Exception e) {
							LogUtils.warn("Exception in org.docear.plugin.bibtex.jabref.JabrefChangeEventListener.processEvent(0): " + e.getMessage());
						}
						continue;						
					} 
					
					// dropped on an existing entry
					if(!chooseFirst) {
						importDialog.showDialog();						
					}
					if (importDialog.getResult() == JOptionPane.OK_OPTION) {
						// xmp metadata was selected
						if (importDialog.getRadioButtonXmp().isSelected()) {
							ImportMenuItem importer = new ImportMenuItem(jabRefFrame, false);
							importer.automatedImport(new String[] { fileName });
						}
						// docear services was selected
						else if (importDialog.getRadioButtonMrDlib().isSelected()) {
							try {
								showMetadataDialog(fileUri);
							} catch (Exception e) {
								LogUtils.warn("Exception in org.docear.plugin.bibtex.jabref.JabrefChangeEventListener.processEvent(1): "
										+ e.getMessage());
							}
						} else {
							
							if (importDialog.getRadioButtonNoMeta().isSelected()) {
								BibtexEntry newEntry = JabRefCommons.createNewEntry(jabRefFrame, basePanel);
								if (newEntry != null) {
									DroppedFileHandler dfh = new DroppedFileHandler(jabRefFrame, basePanel);
									dfh.linkPdfToEntry(fileName, newEntry);
								}
							}
							// update was selected
							else if (importDialog.getRadioButtonUpdateEmptyFields().isSelected()) {
								try {
									showMetadataUpdateDialog(fileUri, entryTable.getEntryAt(dropRow));
								} catch (Exception e) {
									LogUtils.warn("Exception in org.docear.plugin.bibtex.jabref.JabrefChangeEventListener.processEvent(2): "
											+ e.getMessage());
								}
							}
							// attach file only was selected
							else if (importDialog.getRadioButtononlyAttachPDF().isSelected()) {
								DroppedFileHandler dfh = new DroppedFileHandler(jabRefFrame, basePanel);
								dfh.linkPdfToEntry(fileName, entryTable.getEntryAt(dropRow));
							}
						}
					}
				} else {
					// add filename to fallback list
					unhandledFileNames.add(fileName);
				}
			}	
		}
		return unhandledFileNames;
	}
	
	private static List<BibtexEntry> readXmpEntries(String fileName) {
		List<BibtexEntry> xmpEntriesInFile = null;
		try {
			xmpEntriesInFile = XMPUtil.readXMP(fileName);
		} catch (Exception e) {
			LogUtils.info("Exception in org.docear.plugin.bibtex.jabref.JabrefChangeEventListener.readXmpEntries(): " + e.getMessage());
		}
		return xmpEntriesInFile;
	}
	
	public static void showMetadataDialog(URI uri) throws InterruptedException, ExecutionException, IOException {
		String userName = CommunicationsController.getController().getUserName();
		if (userName == null) {
			return;
		}

		final String hash = AnnotationController.getDocumentHash(uri);
		if (hash == null) {
			return;
		}

		// ask for title dialog
		String title = searchForTitle(AnnotationController.getDocumentTitle(uri), uri);
		if (title == null) {
			return;
		}

		File file = new File(uri);
		final MultivaluedMap<String, String> params = new StringKeyStringValueIgnoreCaseMultivaluedMap();
		params.add("username", userName);
		if (title != null) {
			params.add("title", title);
		}

		
		PdfMetadataListDialog metadata = new PdfMetadataListDialog();
		metadata.runServiceRequest(hash, params);
		int response = JOptionPane.showConfirmDialog(UITools.getFrame(), metadata, TextUtils.getText("docear.metadata.import.title"),
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if(metadata.wasSuccessful()) {
			if (response == JOptionPane.OK_OPTION) {
				Util.setAutomaticFields(metadata.getEntries(), true, true, false);
				BibtexEntry selected = metadata.getSelectedEntry();
				selected.setField("dcr_hash", hash);
				addOrUpdateEntryToDatabase(file, selected);
				if (metadata.hasRemoteBib()) {
					commit(selected.getField("dcr_bibtex_id"), hash, userName);
				}
			} else {
				if (metadata.hasRemoteBib()) {
					rejectAll(hash, userName);
				}
			}
		}
	}
	
	public static void showMetadataUpdateDialog(URI uri, BibtexEntry oldEntry) throws InterruptedException, ExecutionException, IOException {
		String userName = CommunicationsController.getController().getUserName();
		if (userName == null) {
			return;
		}

		final String hash = AnnotationController.getDocumentHash(uri);
		if (hash == null) {
			return;
		}

		// ask for title dialog
		String title = searchForTitle(AnnotationController.getDocumentTitle(uri), uri);
		if (title == null) {
			return;
		}

		File file = new File(uri);
		final MultivaluedMap<String, String> params = new StringKeyStringValueIgnoreCaseMultivaluedMap();
		params.add("username", userName);
		if (title != null) {
			params.add("title", title);
		}
		
		PdfMetadataListDialog metadata = new PdfMetadataListDialog();
		metadata.runServiceRequest(hash, params);
		int response = JOptionPane.showConfirmDialog(UITools.getFrame(), metadata, TextUtils.getText("docear.metadata.import.title"),
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if(metadata.wasSuccessful()) {
			if (response == JOptionPane.OK_OPTION) {
				BibtexEntry selected = metadata.getSelectedEntry();
				selected.setField("dcr_hash", hash);
				updateEntryInDatabase(file, selected, oldEntry);
				if (metadata.hasRemoteBib()) {
					commit(selected.getField("dcr_bibtex_id"), hash, userName);
				}
			} else {
				if (metadata.hasRemoteBib()) {
					rejectAll(hash, userName);
				}
			}
		}
			
	}

	public static String searchForTitle(String title, URI fileUri) {
		PdfTitleQuestionDialog titleDialog = new PdfTitleQuestionDialog(title == null ? "" : title, fileUri);
		int searchGo = JOptionPane.showConfirmDialog(UITools.getFrame(), titleDialog, TextUtils.getText("docear.metadata.title.title"),
				JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (searchGo == JOptionPane.YES_OPTION) {
			return titleDialog.getTitle();
		}
		return null;
	}

	private static void commit(final String bibtexID, final String hash, final String userName) {
		final MetadataCallableResult result = MetadataCallableResult.newInstance();
		Runnable task = new Runnable() {
			public void run() {
				try {
					MultivaluedMap<String, String> params = new StringKeyStringValueIgnoreCaseMultivaluedMap();
					params.add("username", userName);
					params.add("commit", "true");
					params.add("id", bibtexID);
					DocearServiceResponse serviceResponse = CommunicationsController.getController().put("/internal/documents/" + hash + "/metadata", params);
					if (serviceResponse.getStatus() != DocearServiceResponse.Status.OK) {
						LogUtils.info("org.docear.plugin.bibtex.actions.ImportMetadateForNodeLink.commit().TASK: " + serviceResponse.getContentAsString());
					}
				} catch (Throwable e) {
					// JOptionPane.showMessageDialog(UITools.getFrame(),
					// e.getLocalizedMessage(),
					// TextUtils.getText("docear.metadata.import.error"),
					// JOptionPane.ERROR_MESSAGE);
					result.setError(e.getLocalizedMessage());
				}
			}
		};
		try {
			executeTask(MetadataRequestTask.create(task, result));
		} catch (Exception e) {
			LogUtils.info("org.docear.plugin.bibtex.actions.ImportMetadateForNodeLink.commit(): " + e.getLocalizedMessage());
			LogUtils.warn(e);
		}
	}

	private static void rejectAll(final String hash, final String userName) {
		final MetadataCallableResult result = MetadataCallableResult.newInstance();
		Runnable task = new Runnable() {
			public void run() {
				try {
					MultivaluedMap<String, String> params = new StringKeyStringValueIgnoreCaseMultivaluedMap();
					params.add("username", userName);
					params.add("commit", "false");
					DocearServiceResponse serviceResponse = CommunicationsController.getController().put("/internal/documents/" + hash + "/metadata", params);
					if (serviceResponse.getStatus() != DocearServiceResponse.Status.OK) {
						LogUtils.info("org.docear.plugin.bibtex.actions.ImportMetadateForNodeLink.rejectAll().TASK: " + serviceResponse.getContentAsString());
					}
				} catch (Throwable e) {
					// JOptionPane.showMessageDialog(UITools.getFrame(),
					// e.getLocalizedMessage(),
					// TextUtils.getText("docear.metadata.import.error"),
					// JOptionPane.ERROR_MESSAGE);
					result.setError(e.getLocalizedMessage());
				}
			}
		};
		try {
			executeTask(MetadataRequestTask.create(task, result));
		} catch (Exception e) {
			LogUtils.info("org.docear.plugin.bibtex.actions.ImportMetadateForNodeLink.rejectAll(): " + e.getLocalizedMessage());
			LogUtils.warn(e);
		}

	}

	public static MetadataCallableResult requestBibTeX(final String hash, final MultivaluedMap<String, String> params) throws InterruptedException, ExecutionException, IOException {
		final MetadataCallableResult result = MetadataCallableResult.newInstance();
		Runnable task = new Runnable() {
			public void run() {
				try {
					StringBuilder sb = new StringBuilder();
					DocearServiceResponse serviceResponse = CommunicationsController.getController().get("/internal/documents/" + hash + "/metadata", params);
					if (serviceResponse.getStatus() == DocearServiceResponse.Status.FAILURE) {
						// JOptionPane.showMessageDialog(UITools.getFrame(),
						// serviceResponse.getContentAsString(),
						// TextUtils.getText("docear.metadata.import.error"),
						// JOptionPane.ERROR_MESSAGE);
						result.setError(serviceResponse.getContentAsString());
						return;
					}
					if (serviceResponse.getStatus() == DocearServiceResponse.Status.NO_CONTENT) {
						// JOptionPane.showMessageDialog(UITools.getFrame(),
						// TextUtils.getText("docear.metadata.import.infotext"),
						// TextUtils.getText("docear.metadata.import.info"),
						// JOptionPane.INFORMATION_MESSAGE);
						result.setError(TextUtils.getText("docear.metadata.import.infotext"));
						return;
					}

					InputStream is = serviceResponse.getContent();// this.getClass().getResourceAsStream("/bibtex-test.bib");
					Reader reader = new InputStreamReader(is);
					int c = -1;
					while ((c = reader.read()) > -1) {
						sb.append((char) c);
					}
					is.close();
					result.setResult(sb.toString());
				} catch (Throwable e) {
					JOptionPane.showMessageDialog(UITools.getFrame(), e.getLocalizedMessage(), TextUtils.getText("docear.metadata.import.error"),
							JOptionPane.ERROR_MESSAGE);
				}
			}
		};
		executeTask(MetadataRequestTask.create(task, result));
		return result;
	}

	private static MetadataCallableResult executeTask(Callable<MetadataCallableResult> task) throws InterruptedException, ExecutionException {
		ExecutorService executor = Executors.newSingleThreadExecutor();
		MetadataCallableResult taskResult = null;
		try {
			Future<MetadataCallableResult> future = executor.submit(task);
			taskResult = future.get(5, TimeUnit.SECONDS);
			future.cancel(true);
		} catch (TimeoutException tex) {
		}
		executor.shutdown();
		return taskResult;
	}

	private static void addOrUpdateEntryToDatabase(File file, BibtexEntry selected) {
		if (selected == null) {
			return;
		}
		JabrefWrapper wrapper = ReferencesController.getController().getJabrefWrapper();
		BibtexEntry oldEntry = null;
		if(file != null) {
			for(BibtexEntry entry : wrapper.getDatabase().getEntries()) {
				Reference ref = new Reference(entry);
				if(ref.containsFile(file)) {
					oldEntry = entry;
					break;
				}
			}
		}
		if(oldEntry == null) {
			selected.setId(Util.createNeutralId());
			wrapper.getBasePanel().getDatabase().insertEntry(selected);
			showInReferenceManager(selected);
			DroppedFileHandler dfh = new DroppedFileHandler(wrapper.getJabrefFrame(), wrapper.getBasePanel());
			
			if(file != null) {
				// DOCEAR - change file path to relative to bib-library path?
				dfh.linkPdfToEntry(file.getPath(), selected);
				LabelPatternUtil.makeLabel(Globals.prefs.getKeyPattern(), wrapper.getDatabase(), selected);
			}
		}
		else {
			JabRefCommons.updateEntryInDatabase(null, selected, oldEntry);
			showInReferenceManager(oldEntry);
		}
		
		
	}
	
	public static void showInReferenceManager(String bibtexKey) {
		if (bibtexKey != null && bibtexKey.length()>0) {
			
			BibtexEntry referenceEntry = ReferencesController.getController().getJabrefWrapper().getDatabase().getEntryByKey(bibtexKey);
			showInReferenceManager(referenceEntry);
		}
	}
	
	public static void showInReferenceManager(BibtexEntry referenceEntry) {
		if(referenceEntry == null) {
			return;
		}
		MainTable table = ReferencesController.getController().getJabrefWrapper().getBasePanel().getMainTable();
		
		List<BibtexEntry> list = table.getTableRows();
		int viewHeight = table.getPane().getHeight()-table.getTableHeader().getHeight();
		Rectangle viewRect = new Rectangle(0,((JViewport)table.getParent()).getViewPosition().y, 4, viewHeight);
		int pos = 0;
		Rectangle rowArea = new Rectangle(); 
		for(BibtexEntry row : list) {
			if(row.equals(referenceEntry)) {
				rowArea.setBounds(0, (table.getRowHeight()*pos), 2, table.getRowHeight());					
				table.clearSelection();
				table.addRowSelectionInterval(pos,pos);
				if(isRowOutsideViewArea(viewRect, rowArea)) {
					((JViewport)table.getParent()).setViewPosition(rowArea.getLocation());
				}
				break;
			}
			pos++;
		}
	}
	
	private static boolean isRowOutsideViewArea(final Rectangle viewArea, final Rectangle row) {
		if(viewArea.contains(row)) {
			return false;
		}
		return true;
	}

	public static void addNewRefenceEntry(String[] fileNames, JabRefFrame jabRefFrame, BasePanel basePanel) {
		addOrUpdateRefenceEntry(fileNames, -1, jabRefFrame, basePanel, null, true);
		
	}

	public static BibtexEntry createNewEntry(JabRefFrame frame, BasePanel panel) {		
	    // Find out what type is wanted.
	    EntryTypeDialog etd = new EntryTypeDialog(frame);
	    // We want to center the dialog, to make it look nicer.
	    Util.placeDialog(etd, UITools.getFrame());
	    etd.setVisible(true);
	    BibtexEntryType type = etd.getChoice();
	
	    if (type != null) { // Only if the dialog was not cancelled.
	        String id = Util.createNeutralId();
	        final BibtexEntry be = new BibtexEntry(id, type);
	        try {
	            panel.database().insertEntry(be);
	
	            // Set owner/timestamp if options are enabled:
	            ArrayList<BibtexEntry> list = new ArrayList<BibtexEntry>();
	            list.add(be);
	            Util.setAutomaticFields(list, true, true, false);
	
	            // Create an UndoableInsertEntry object.
	            panel.undoManager.addEdit(new UndoableInsertEntry(panel.database(), be, panel));
	            panel.output(Globals.lang("Added new")+" '"+type.getName().toLowerCase()+"' "
	                   +Globals.lang("entry")+".");
	
	            // We are going to select the new entry. Before that, make sure that we are in
	            // show-entry mode. If we aren't already in that mode, enter the WILL_SHOW_EDITOR
	            // mode which makes sure the selection will trigger display of the entry editor
	            // and adjustment of the splitter.
	            if (panel.getMode() != BasePanel.SHOWING_EDITOR) {
	            	panel.setMode(BasePanel.WILL_SHOW_EDITOR);
	            }
	
	            panel.showEntry(be);
	            panel.markBaseChanged(); // The database just changed.
	            new FocusRequester(panel.getEntryEditor(be));
	            return be;
	        } catch (KeyCollisionException ex) {
	            LogUtils.warn("Exception in org.docear.plugin.bibtex.jabref.JabrefWrapper.createNewEntry(): "+ex.getMessage());
	        }
	    }
	    return null;
	}

//	private static void insertFields(String[] fields, BibtexEntry entry, BibtexEntry newData) {
//		for (String field : fields) {
//			if (entry.getField(field) == null) {
//				entry.setField(field, newData.getField(field));
//			}
//		}
//	}
}
