package org.docear.plugin.bibtex.listeners;

import net.sf.jabref.BibtexEntry;
import net.sf.jabref.DatabaseChangeEvent;
import net.sf.jabref.DatabaseChangeListener;

import org.docear.plugin.bibtex.JabRefAttributes;
import org.docear.plugin.bibtex.ReferencesController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;

public class JabRefChangeListener implements DatabaseChangeListener {
	
	private static boolean locked = false;

	@Override
	public void databaseChanged(DatabaseChangeEvent e) {
		if (locked) {
			return;
		}
		
		locked = true;
		
		BibtexEntry entry = e.getEntry();
		NodeModel root = Controller.getCurrentModeController().getMapController().getRootNode();
		
		if (e.getType() == DatabaseChangeEvent.REMOVED_ENTRY) {
			System.out.println("debug removed: "+e.getEntry().getCiteKey());
			deleteNodeAttributes(root, entry);
		}
		else if (e.getType() == DatabaseChangeEvent.CHANGED_ENTRY) {
			System.out.println("debug changed: "+e.getEntry().getCiteKey());
			updateNodeAttributes(root, entry);
		}
		else if (e.getType() == DatabaseChangeEvent.CHANGING_ENTRY) {
			System.out.println("debug changing: "+e.getEntry().getCiteKey());			
		}
		else if (e.getType() == DatabaseChangeEvent.ADDED_ENTRY) {
			System.out.println("debug added: "+e.getEntry().getCiteKey());
		}
		
		locked = false;
	}
	
	public void updateNodeAttributes(NodeModel node, BibtexEntry entry) {
		JabRefAttributes jabRefAttributes = ReferencesController.getController().getJabRefAttributes();
		
		if (jabRefAttributes.isReferencing(entry, node)) {
			jabRefAttributes.setReferenceToNode(entry, node);
		}
		
		for (NodeModel child : node.getChildren()) {
			updateNodeAttributes(child, entry);
		}
	}
	
	public void deleteNodeAttributes(NodeModel node, BibtexEntry entry) {
		JabRefAttributes jabRefAttributes = ReferencesController.getController().getJabRefAttributes();
		
		if (jabRefAttributes.isReferencing(entry, node)) {
			jabRefAttributes.removeReferenceFromNode(entry, node);
		}
		
		for (NodeModel child : node.getChildren()) {
			updateNodeAttributes(child, entry);
		}
		
	}
	

}
