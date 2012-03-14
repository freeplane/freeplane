package org.docear.plugin.bibtex;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import net.sf.jabref.BasePanel;
import net.sf.jabref.BibtexDatabase;
import net.sf.jabref.Globals;
import net.sf.jabref.JabRef;
import net.sf.jabref.JabRefFrame;
import net.sf.jabref.Util;
import net.sf.jabref.export.SaveSession;
import net.sf.jabref.external.FileLinksUpgradeWarning;
import net.sf.jabref.imports.CheckForNewEntryTypesAction;
import net.sf.jabref.imports.OpenDatabaseAction;
import net.sf.jabref.imports.ParserResult;
import net.sf.jabref.imports.PostOpenAction;
import net.sf.jabref.label.HandleDuplicateWarnings;

import org.docear.plugin.bibtex.actions.FilePathValidatorAction;
import org.docear.plugin.bibtex.listeners.MapViewListener;
import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.event.DocearEventType;
import org.docear.plugin.core.logger.DocearLogEvent;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.Compat;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.ui.IMapViewChangeListener;
import org.swingplus.JHyperlink;

public class JabrefWrapper extends JabRef implements IMapViewChangeListener {

	private static final int MAX_TRY_OPEN = 5;

	private static ArrayList<PostOpenAction> postOpenActions = new ArrayList<PostOpenAction>();

	static {
		// bibtex files exported by mendeley do not contain leading "/" for
		// absolute paths so we do not know if
		// the file contaions relative paths or absolute paths
		postOpenActions.add(new FilePathValidatorAction());
		// Add the action for checking for new custom entry types loaded from
		// the bib file:
		postOpenActions.add(new CheckForNewEntryTypesAction());
		// Add the action for the new external file handling system in version
		// 2.3:
		postOpenActions.add(new FileLinksUpgradeWarning());
		// Add the action for warning about and handling duplicate BibTeX keys:
		postOpenActions.add(new HandleDuplicateWarnings());
	}

	private static final MapViewListener mapViewListener = new MapViewListener();
	private ParserResult parserResult = null;
	private String encoding = null;
	private File file;
	private HashMap<String, String> meta = null;

	public JabrefWrapper(JFrame frame) {
		this(frame, null);

	}

	/**
	 * @param jFrame
	 * @param file
	 */
	public JabrefWrapper(JFrame frame, File file) {
		// super(frame, new String[]{"true", "-i", "\""+file.toString()+"\""});
		super(frame);
		registerListeners();
		if(file != null ) {
			openIt(file, true);
		}

	}

	public JabRefFrame getJabrefFrame() {

		return this.jrf;
	}

	private void registerListeners() {
		Controller.getCurrentController().getMapViewManager().addMapViewChangeListener(this);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				synchronized (Controller.getCurrentModeController().getMapController()) {
					
					Controller.getCurrentModeController().getMapController().addNodeSelectionListener(mapViewListener);
				}
			}
		});
	}

	public BasePanel getBasePanel() {
		return (BasePanel) getJabrefFrame().getTabbedPane().getSelectedComponent();
	}

	public BibtexDatabase getDatabase() {
		if (getBasePanel() == null) {
			return null;
		}
		return getBasePanel().getDatabase();
	}

	public BasePanel addNewDatabase(ParserResult pr, File file, boolean raisePanel) {
		this.file = file;
		String fileName = file.getPath();
		BibtexDatabase database = pr.getDatabase();
		database.addDatabaseChangeListener(ReferencesController.getJabRefChangeListener());
		this.setMeta(pr.getMetaData());
		this.setEncoding(pr.getEncoding());

		BasePanel bp = new BasePanel(getJabrefFrame(), database, file, meta, pr.getEncoding());

		// file is set to null inside the EventDispatcherThread
		// SwingUtilities.invokeLater(new OpenItSwingHelper(bp, file,
		// raisePanel));

		getJabrefFrame().addTab(bp, file, raisePanel);

		LogUtils.info(Globals.lang("Opened database") + " '" + fileName + "' " + Globals.lang("with") + " "
				+ database.getEntryCount() + " " + Globals.lang("entries") + ".");

		return bp;
	}

	public void replaceDatabase(File file, boolean raisePanel) {
		// getJabrefFrame().getTabbedPane().removeAll();
		// if(getBasePanel() != null) {
		// getBasePanel().runCommand("save");
		// }
		while (getJabrefFrame().getTabbedPane().getTabCount() > 0) {
			getJabrefFrame().closeCurrentTab();
		}
		openIt(file, raisePanel);
		
		DocearController.getController().getDocearEventLogger().appendToLog(this, DocearLogEvent.RM_BIBTEX_FILE_CHANGE, new Object[] {file, this.getDatabase().getEntries().size()});
	}

	public void openIt(File file, boolean raisePanel) {
		if ((file != null) && (file.exists())) {
			if (!isCompatibleToJabref(file)) {
				JHyperlink hyperlink = new JHyperlink("http://www.docear.org/support/user-manual/#docear_and_mendeley ",
						"http://www.docear.org/support/user-manual/#docear_and_mendeley");
				JPanel panel = new JPanel(new BorderLayout());
				panel.add(new JLabel(TextUtils.getText("jabref_mendeley_incompatible_1")), BorderLayout.NORTH);
				panel.add(hyperlink, BorderLayout.CENTER);
				panel.add(new JLabel(TextUtils.getText("jabref_mendeley_incompatible_2")), BorderLayout.SOUTH);

				int option = JOptionPane.showConfirmDialog(UITools.getFrame(), panel,

				TextUtils.getText("jabref_mendeley_incompatible_title"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
				if (option == JOptionPane.YES_OPTION) {
					return;
				}
			}
			File fileToLoad = file;
			LogUtils.info(Globals.lang("Opening References") + ": '" + file.getPath() + "'");

			int tryCounter = 0;
			boolean done = false;
			while (!done && tryCounter++ < MAX_TRY_OPEN) {
				String fileName = file.getPath();
				Globals.prefs.put("workingDirectory", file.getPath());
				// Should this be done _after_ we know it was successfully
				// opened?
				ResourceController resourceController = ResourceController.getResourceController();
				String encoding = resourceController.getProperty("docear_bibtex_encoding", Globals.prefs.get("defaultEncoding"));

				if (Util.hasLockFile(file)) {
					long modTime = Util.getLockFileTimeStamp(file);
					if ((modTime != -1) && (System.currentTimeMillis() - modTime > SaveSession.LOCKFILE_CRITICAL_AGE)) {
						// The lock file is fairly old, so we can offer to
						// "steal" the file:
						int answer = JOptionPane.showConfirmDialog(
								null,
								"<html>" + Globals.lang("Error opening file") + " '" + fileName + "'. "
										+ Globals.lang("File is locked by another JabRef instance.") + "<p>"
										+ Globals.lang("Do you want to override the file lock?"), Globals.lang("File locked"),
								JOptionPane.YES_NO_OPTION);
						if (answer == JOptionPane.YES_OPTION) {
							Util.deleteLockFile(file);
						}
						else
							return;
					}
					else if (!Util.waitForFileLock(file, 10)) {
						JOptionPane.showMessageDialog(null, Globals.lang("Error opening file") + " '" + fileName + "'. "
								+ Globals.lang("File is locked by another JabRef instance."), Globals.lang("Error"),
								JOptionPane.ERROR_MESSAGE);
						return;
					}

				}
				ParserResult pr;
				try {
					String source = resourceController.getProperty("docear_bibtex_source", "Jabref");
					pr = OpenDatabaseAction.loadDataBase(fileToLoad, encoding, source);
				}
				catch (Exception ex) {
					pr = null;
				}
				if ((pr == null) || (pr == ParserResult.INVALID_FORMAT)) {
					LogUtils.warn("ERROR: Could not load file" + file);
					continue;
				}
				else {
					done = true;
					final BasePanel panel = addNewDatabase(pr, file, raisePanel);

					panel.markNonUndoableBaseChanged();

					// After adding the database, go through our list and see if
					// any post open actions need to be done. For instance,
					// checking
					// if we found new entry types that can be imported, or
					// checking
					// if the database contents should be modified due to new
					// features
					// in this version of JabRef:
					final ParserResult prf = pr;
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							performPostOpenActions(panel, prf, true);
						}
					});
				}
			}

		}
		
		DocearController.getController().getDocearEventLogger().appendToLog(this, DocearLogEvent.RM_BIBTEX_FILE_OPEN, new Object[] {file, this.getDatabase().getEntries().size()});
		
	}

	// JabRef does not use character escaping of "{" and "}"
	// unfortunately all other escapings are not unambiguously or might be set
	// in jabref-preferences too
	public boolean isCompatibleToJabref(File f) {
		int escapeCount = 0;
		int allCount = 0;

		ArrayList<Character> allowedCharsBeforeSlash = new ArrayList<Character>();
		allowedCharsBeforeSlash.add('\"');
		allowedCharsBeforeSlash.add('\'');
		allowedCharsBeforeSlash.add('`');
		allowedCharsBeforeSlash.add('^');
		allowedCharsBeforeSlash.add('~');

		Scanner in = null;
		try {
			in = new Scanner(new FileReader(f));
			while (in.hasNextLine()) {
				String line = in.nextLine();

				String normalized = line.trim().toLowerCase();
				if (Compat.isWindowsOS() && normalized.startsWith("file")) {
					if (normalized.contains("backslash$:")) {
						return false;
					}
				}
				if (normalized.startsWith("journal") || normalized.startsWith("title") || normalized.startsWith("booktitle")) {
					int pos = 0;
					int i = 0;

					String s = normalized.substring(normalized.indexOf("=") + 1).trim();
					while (s.charAt(pos) == '{') {
						pos++;
					}
					while ((i = s.indexOf("{", pos)) >= 0) {
						pos = (i + 1);
						if (allowedCharsBeforeSlash.contains(s.charAt(i - 1))) {
							continue;
						}
						allCount++;

					}

					pos = 0;
					i = 0;
					while ((i = s.indexOf("\\{", pos)) >= 0) {
						escapeCount++;
						pos = (i + 1);
					}
				}
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			try {
				in.close();
			}
			catch (Exception e) {
				LogUtils.warn(e);
			}
		}

		// if no escaped and no unescaped char sequence was found in the whole
		// file we assume it to be ok for usage in jabref
		if (allCount / 2 >= escapeCount) {
			return true;
		}
		return false;
	}

	/**
	 * Go through the list of post open actions, and perform those that need to
	 * be performed.
	 * 
	 * @param panel
	 *            The BasePanel where the database is shown.
	 * @param pr
	 *            The result of the bib file parse operation.
	 */
	public static void performPostOpenActions(BasePanel panel, ParserResult pr, boolean mustRaisePanel) {
		for (Iterator<PostOpenAction> iterator = postOpenActions.iterator(); iterator.hasNext();) {
			PostOpenAction action = iterator.next();
			if (action.isActionNecessary(pr)) {
				if (mustRaisePanel)
					panel.frame().getTabbedPane().setSelectedComponent(panel);
				action.performAction(panel, pr);
			}
		}
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public ParserResult getParserResult() {
		return parserResult;
	}

	public void setParserResult(ParserResult parserResult) {
		this.parserResult = parserResult;
	}

	public HashMap<String, String> getMeta() {
		return meta;
	}

	public void setMeta(HashMap<String, String> meta) {
		this.meta = meta;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public void afterViewChange(Component oldView, Component newView) {
	}

	public void afterViewClose(final Component oldView) {
		oldView.removeMouseListener(mapViewListener);
	}

	public void afterViewCreated(final Component mapView) {
		mapView.addMouseListener(mapViewListener);
	}

	public void beforeViewChange(Component oldView, Component newView) {
	}

}
