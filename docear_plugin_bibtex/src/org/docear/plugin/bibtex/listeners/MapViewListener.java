package org.docear.plugin.bibtex.listeners;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.TreeMap;

import net.sf.jabref.BibtexDatabase;
import net.sf.jabref.BibtexEntry;
import net.sf.jabref.Globals;
import net.sf.jabref.export.DocearReferenceUpdateController;
import net.sf.jabref.labelPattern.LabelPatternUtil;

import org.docear.plugin.bibtex.ReferencesController;
import org.freeplane.features.map.INodeSelectionListener;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;

public class MapViewListener implements MouseListener, INodeSelectionListener {
	
	private void handleEvent() {
		ReferencesController referencesController = ReferencesController.getController();
		
		if (referencesController.getInChange() != null) {
			referencesController.setInChange(null);
			ReferencesController.getController().getJabrefWrapper().getBasePanel().runCommand("save");
		}
		
		if (referencesController.getInAdd() != null) {
			addToNodes(referencesController.getInAdd());
			referencesController.setInAdd(null);			
			ReferencesController.getController().getJabrefWrapper().getBasePanel().runCommand("save");
		}
	}

	private void addToNodes(MapModel mapModel) {
		DocearReferenceUpdateController.lock();
		
		BibtexDatabase database = ReferencesController.getController().getJabrefWrapper().getDatabase();
		
		TreeMap<String, BibtexEntry> entryNodeTuples = new TreeMap<String, BibtexEntry>();
				
		for (BibtexEntry entry : database.getEntries()) {
			String nodeId = entry.getField("docear_add_to_node");
			if (nodeId != null) {						
				if (entry.getCiteKey() == null) {
					LabelPatternUtil.makeLabel(Globals.prefs.getKeyPattern(), database, entry);
				}
				entryNodeTuples.put(nodeId, entry);				
			}
			NodeModel node = mapModel.getNodeForID(nodeId);			
			if (node != null) {
				ReferencesController.getController().getJabRefAttributes().setReferenceToNode(entry, node);
			}
			entry.setField("docear_add_to_node", null);
		}		
				
		DocearReferenceUpdateController.unlock();
	}
	
	public void mouseClicked(MouseEvent e) {
		handleEvent();
	}

	public void mousePressed(MouseEvent e) {
	}


	public void mouseReleased(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}


	public void mouseExited(MouseEvent e) {
	}


	public void onDeselect(NodeModel node) {
	}


	public void onSelect(NodeModel node) {
		handleEvent();
	}

}
