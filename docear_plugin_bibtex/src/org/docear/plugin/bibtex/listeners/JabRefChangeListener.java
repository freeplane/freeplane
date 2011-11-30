package org.docear.plugin.bibtex.listeners;

import net.sf.jabref.BibtexEntry;
import net.sf.jabref.DatabaseChangeEvent;
import net.sf.jabref.DatabaseChangeListener;
import net.sf.jabref.export.DocearReferenceUpdateController;

import org.docear.plugin.bibtex.JabRefAttributes;
import org.docear.plugin.bibtex.ReferencesController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.jdesktop.swingworker.SwingWorker;

public class JabRefChangeListener implements DatabaseChangeListener {


	public void databaseChanged(DatabaseChangeEvent e) {
		if (DocearReferenceUpdateController.isLocked()) {		
			return;
		}		
		DocearReferenceUpdateController.lock();

		BibtexEntry entry = e.getEntry();
		
		NodeModel root;
		
		try {
			root = Controller.getCurrentModeController().getMapController().getRootNode();
		}
		catch(NullPointerException ex) {
			//no database open
			return;
		}

		if (e.getType() == DatabaseChangeEvent.REMOVED_ENTRY) {
			System.out.println("debug removed: " + e.getEntry().getCiteKey());
			deleteNodeAttributes(root, entry);
		}
		else if (e.getType() == DatabaseChangeEvent.CHANGED_ENTRY) {
			updateNodeAttributes(root, entry);
		}
		else if (e.getType() == DatabaseChangeEvent.CHANGING_ENTRY) {
			System.out.println("debug changing: " + e.getEntry().getCiteKey());
		}
		else if (e.getType() == DatabaseChangeEvent.ADDED_ENTRY) {
			System.out.println("debug added: " + e.getEntry().getCiteKey());
		}

		DocearReferenceUpdateController.unlock();
	}

	private void updateNodeAttributes(final NodeModel node, final BibtexEntry entry) {
		SwingWorker<Void, Void> thread = new SwingWorker<Void, Void>() {
			
			protected Void doInBackground() throws Exception {
				if (entry.getCiteKey().length() <= 0) {
					return null;
				}
				JabRefAttributes jabRefAttributes = ReferencesController.getController().getJabRefAttributes();

				if (jabRefAttributes.isReferencing(entry, node)) {
					jabRefAttributes.updateReferenceToNode(entry, node);
				}

				for (NodeModel child : node.getChildren()) {
					updateNodeAttributes(child, entry);
				}

				return null;
			}
		};

		thread.run();
	}

	public void deleteNodeAttributes(final NodeModel node, final BibtexEntry entry) {
		SwingWorker<Void, Void> thread = new SwingWorker<Void, Void>() {
			
			protected Void doInBackground() throws Exception {
				JabRefAttributes jabRefAttributes = ReferencesController.getController().getJabRefAttributes();

				if (jabRefAttributes.isReferencing(entry, node)) {
					jabRefAttributes.removeReferenceFromNode(node);
				}

				for (NodeModel child : node.getChildren()) {
					updateNodeAttributes(child, entry);
				}

				return null;
			}
		};

		thread.run();

	}

}
