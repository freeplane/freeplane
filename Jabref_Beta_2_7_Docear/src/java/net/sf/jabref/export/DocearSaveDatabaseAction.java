package net.sf.jabref.export;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import net.sf.jabref.BasePanel;
import net.sf.jabref.BibtexEntry;
import net.sf.jabref.Globals;
import net.sf.jabref.labelPattern.LabelPatternUtil;

public class DocearSaveDatabaseAction extends SaveDatabaseAction {

	public final static String JABREF_DATABASE_SAVE_SUCCESS = "__JABREF_DATABASE_SAVE_SUCCESS__";
	public final static String JABREF_DATABASE_SAVE_FAILED = "__JABREF_DATABASE_SAVE_FAILED__";

	private List<ActionListener> actionListeners = new ArrayList<ActionListener>();

	public DocearSaveDatabaseAction(BasePanel panel) {
		super(panel);
	}
	
	public void callListeners(Map<String, BibtexEntry> entryNodeTuples, boolean success) {
		ActionEvent event;
		for (Entry<String, BibtexEntry> tuple : entryNodeTuples.entrySet()) {
			//addNodeID to entry for AddNewReferenceAction (deleted before saving the database
			tuple.getValue().setField("docear_add_to_node", tuple.getKey());
			event = new ActionEvent(tuple.getValue(), 0, success ? JABREF_DATABASE_SAVE_SUCCESS : JABREF_DATABASE_SAVE_FAILED);			
			for (ActionListener listener : actionListeners) {
				listener.actionPerformed(event);
			}
			//delete nodeID before continuing --> same state as saved database and bibtex key would not be added again on next run
			tuple.getValue().setField("docear_add_to_node", null);
		}
	}

	public void run() {		
		DocearReferenceUpdateController.lock();
		TreeMap<String, BibtexEntry> entryNodeTuples = new TreeMap<String, BibtexEntry>();
		
		String nodeId = null;
		for (BibtexEntry entry : this.panel.getDatabase().getEntries()) {
			nodeId = entry.getField("docear_add_to_node");
			if (nodeId != null) {						
				if (entry.getCiteKey() == null) {
					LabelPatternUtil.makeLabel(Globals.prefs.getKeyPattern(), this.panel.getDatabase(), entry);
				}
				entryNodeTuples.put(nodeId, entry);				
			}
			entry.setField("docear_add_to_node", null);
		}		
		
		//save Database
		super.run();
		
		if (entryNodeTuples.size()>0) {
			callListeners(entryNodeTuples, isSuccess());
		}		
		DocearReferenceUpdateController.unlock();
	}

	public void addActionListener(ActionListener actionListener) {
		this.actionListeners.add(actionListener);
	}

}
