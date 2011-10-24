package org.docear.plugin.bibtex;

import java.io.File;
import java.util.HashMap;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import net.sf.jabref.BasePanel;
import net.sf.jabref.BibtexDatabase;
import net.sf.jabref.Globals;
import net.sf.jabref.JabRef;
import net.sf.jabref.JabRefFrame;
import net.sf.jabref.Util;
import net.sf.jabref.export.SaveSession;
import net.sf.jabref.imports.OpenDatabaseAction;
import net.sf.jabref.imports.ParserResult;

public class JabrefWrapper extends JabRef  {
	
	private BibtexDatabase database = null;

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
		String fileName = file.getPath();
		BibtexDatabase db = pr.getDatabase();
		this.setDatabase(db);
		HashMap<String, String> meta = pr.getMetaData();
		
		BasePanel bp = new BasePanel(getJabrefFrame(), db, file, meta, pr.getEncoding());
	
		// file is set to null inside the EventDispatcherThread
		//SwingUtilities.invokeLater(new OpenItSwingHelper(bp, file, raisePanel));
		
		getJabrefFrame().addTab(bp, file, raisePanel);
		
		System.out.println(Globals.lang("Opened database") + " '" + fileName +
		"' " + Globals.lang("with") + " " +
		db.getEntryCount() + " " + Globals.lang("entries") + ".");
		
		return bp;
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
                String errorMessage = null;
                try {
                    pr = OpenDatabaseAction.loadDatabase(fileToLoad, encoding);
                } catch (Exception ex) {
                    //ex.printStackTrace();
                    errorMessage = ex.getMessage();
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
//                final ParserResult prf = pr;
//                SwingUtilities.invokeLater(new Runnable() {
//                    public void run() {
//                        performPostOpenActions(panel, prf, true);
//                    }
//                });
            }
            
            
            

            
        }
    }

	public BibtexDatabase getDatabase() {
		return database;
	}

	public void setDatabase(BibtexDatabase database) {
		this.database = database;
	}
	
}
