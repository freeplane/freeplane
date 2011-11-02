package org.docear.plugin.bibtex;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
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

public class JabrefWrapper extends JabRef  {
	
	private static ArrayList<PostOpenAction> postOpenActions =
            new ArrayList<PostOpenAction>();

    static {
        // Add the action for checking for new custom entry types loaded from
        // the bib file:
        postOpenActions.add(new CheckForNewEntryTypesAction());
        // Add the action for the new external file handling system in version 2.3:
        postOpenActions.add(new FileLinksUpgradeWarning());
        // Add the action for warning about and handling duplicate BibTeX keys:
        postOpenActions.add(new HandleDuplicateWarnings());
    }

	private BasePanel basePanel = null;
	private BibtexDatabase database = null;
	private ParserResult parserResult = null;
	private String encoding = null;
	private File file;
	private HashMap<String, String> meta = null;
	
	protected JabrefWrapper(String[] arg0) {
		super(arg0);		

	}
	
	public JabrefWrapper(JFrame frame) {
		super(frame);

	}
	
	/**
	 * @param jFrame
	 * @param file
	 */
	public JabrefWrapper(JFrame frame, File file) {
		//super(frame, new String[]{"true", "-i", "\""+file.toString()+"\""});
		super(frame);		
		openIt(file, true);

	}

	public JabRefFrame getJabrefFrame(){
		
		return this.jrf;
	}
	
	public BasePanel addNewDatabase(ParserResult pr, File file, boolean raisePanel) {
		this.file = file;
		String fileName = file.getPath();		
		this.setParserResult(pr);
		this.setDatabase(pr.getDatabase());
		this.setMeta(pr.getMetaData());
		this.setEncoding(pr.getEncoding());
		
		BasePanel bp = new BasePanel(getJabrefFrame(), database, file, meta, pr.getEncoding());
		this.basePanel = bp;
	
		// file is set to null inside the EventDispatcherThread
		//SwingUtilities.invokeLater(new OpenItSwingHelper(bp, file, raisePanel));
		
		getJabrefFrame().addTab(bp, file, raisePanel);		
		
		System.out.println(Globals.lang("Opened database") + " '" + fileName +
		"' " + Globals.lang("with") + " " +
		database.getEntryCount() + " " + Globals.lang("entries") + ".");
		
		return bp;
	}
	
	public BasePanel addNewDatabase(BibtexDatabase database, boolean raisePanel) {		
		this.setDatabase(database);
		
		BasePanel bp = new BasePanel(getJabrefFrame(), database, file, meta, encoding);
		this.basePanel = bp;
	
		// file is set to null inside the EventDispatcherThread
		//SwingUtilities.invokeLater(new OpenItSwingHelper(bp, file, raisePanel));
		
		getJabrefFrame().addTab(bp, file, raisePanel);		
		
		System.out.println(Globals.lang("updated database") + Globals.lang("with") + " " +
		database.getEntryCount() + " " + Globals.lang("entries") + ".");
		
		return bp;
	}
	
	public BasePanel updateDatabase(BibtexDatabase database) {
		//FIXME: basePanel is new --> not existent --> java.lang.IllegalArgumentException: component not found in tabbed pane
//		getJabrefFrame().getTabbedPane().setSelectedComponent(basePanel);
//		this.setDatabase(database);
//		BasePanel basePanel = new BasePanel(getJabrefFrame(), database, this.getFile(), this.getMeta(), this.getEncoding());
//		this.basePanel = basePanel;
		
		
		this.getJabrefFrame().closeCurrentTab();
		addNewDatabase(database, true);
		
		return basePanel;		
	}
		
	public void openIt(File file, boolean raisePanel) {
        if ((file != null) && (file.exists())) {
            File fileToLoad = file;
            System.out.println(Globals.lang("Opening References") + ": '" + file.getPath() + "'");

            boolean done = false;
            while (!done) {
                String fileName = file.getPath();
                Globals.prefs.put("workingDirectory", file.getPath());
                // Should this be done _after_ we know it was successfully opened?
                String encoding = Globals.prefs.get("defaultEncoding");

                if (Util.hasLockFile(file)) {
                    long modTime = Util.getLockFileTimeStamp(file);
                    if ((modTime != -1) && (System.currentTimeMillis() - modTime
                            > SaveSession.LOCKFILE_CRITICAL_AGE)) {
                        // The lock file is fairly old, so we can offer to "steal" the file:
                        int answer = JOptionPane.showConfirmDialog(null, "<html>"+Globals.lang("Error opening file")
                            +" '"+fileName+"'. "+Globals.lang("File is locked by another JabRef instance.")
                            +"<p>"+Globals.lang("Do you want to override the file lock?"),
                            Globals.lang("File locked"), JOptionPane.YES_NO_OPTION);
                        if (answer == JOptionPane.YES_OPTION) {
                            Util.deleteLockFile(file);
                        }
                        else return;
                    }
                    else if (!Util.waitForFileLock(file, 10)) {
                        JOptionPane.showMessageDialog(null, Globals.lang("Error opening file")
                            +" '"+fileName+"'. "+Globals.lang("File is locked by another JabRef instance."),
                            Globals.lang("Error"), JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                }
                ParserResult pr;
                try {
                    pr = OpenDatabaseAction.loadDatabase(fileToLoad, encoding);
                } catch (Exception ex) {
                    pr = null;
                }
                if ((pr == null) || (pr == ParserResult.INVALID_FORMAT)) {
                    System.out.println("ERROR: Could not load file"+file);
                    continue;
                } else done = true;

                final BasePanel panel = addNewDatabase(pr, file, raisePanel);
                
                panel.markNonUndoableBaseChanged();

                // After adding the database, go through our list and see if
                // any post open actions need to be done. For instance, checking
                // if we found new entry types that can be imported, or checking
                // if the database contents should be modified due to new features
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
	
    
    /**
     * Go through the list of post open actions, and perform those that need
     * to be performed.
     * @param panel The BasePanel where the database is shown.
     * @param pr The result of the bib file parse operation.
     */
    public static void performPostOpenActions(BasePanel panel, ParserResult pr,
                                              boolean mustRaisePanel) {
        for (Iterator<PostOpenAction> iterator = postOpenActions.iterator(); iterator.hasNext();) {
            PostOpenAction action = iterator.next();
            if (action.isActionNecessary(pr)) {
                if (mustRaisePanel)
                    panel.frame().getTabbedPane().setSelectedComponent(panel);
                action.performAction(panel, pr);
            }
        }
    }


	public BibtexDatabase getDatabase() {
		return database;
	}

	public void setDatabase(BibtexDatabase database) {
		this.database = database;
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

	public BasePanel getBasePanel() {
		return basePanel;
	}

	public void setBasePanel(BasePanel basePanel) {
		this.basePanel = basePanel;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}
	
}
