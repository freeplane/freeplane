package net.sf.jabref.export;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

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

	public void run() {
		BibtexEntry entryForMindmapNode = null;
		for (BibtexEntry entry : this.panel.getDatabase().getEntries()) {
			String nodeId = entry.getField("docear_add_to_node");
			if (nodeId != null) {
				entryForMindmapNode = entry;				
				if (entry.getCiteKey() == null) {
					LabelPatternUtil.makeLabel(Globals.prefs.getKeyPattern(), this.panel.getDatabase(), entry);
				}
			}
		}		

		super.run();
		
		if (entryForMindmapNode != null) {
			ActionEvent event;
			if (this.isSuccess()) {				
				event = new ActionEvent(entryForMindmapNode, 0, JABREF_DATABASE_SAVE_SUCCESS);
				System.out.println("entry for mindmap node: "+entryForMindmapNode.getField("docear_add_to_node"));				
			}
			else if (!this.isCancelled()) {
				event = new ActionEvent(entryForMindmapNode, 0, JABREF_DATABASE_SAVE_FAILED);
			}
			else {
				return;
			}
			for (ActionListener listener : actionListeners) {
				listener.actionPerformed(event);
			}
			
			entryForMindmapNode.setField("docear_add_to_node", null);
		}
		
		
	}

	public void addActionListener(ActionListener actionListener) {
		this.actionListeners.add(actionListener);
	}

}
