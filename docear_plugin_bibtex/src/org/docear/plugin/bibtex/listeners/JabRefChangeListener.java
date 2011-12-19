package org.docear.plugin.bibtex.listeners;

import javax.swing.SwingUtilities;

import net.sf.jabref.DatabaseChangeEvent;
import net.sf.jabref.DatabaseChangeListener;
import net.sf.jabref.export.DocearReferenceUpdateController;

import org.docear.plugin.bibtex.ReferenceUpdater;
import org.docear.plugin.core.mindmap.MindmapUpdateController;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.Controller;

public class JabRefChangeListener implements DatabaseChangeListener {	

	public void databaseChanged(DatabaseChangeEvent e) {
		if (DocearReferenceUpdateController.isLocked()) {
			return;
		}
		
		if (e.getEntry() == null || e.getEntry().getCiteKey() == null) {
			return;
		}
		
		SwingUtilities.invokeLater(new Runnable() {
			

			public void run() {	
				DocearReferenceUpdateController.lock();
				MapModel currentMap = Controller.getCurrentController().getMap();
				if (currentMap == null) {
					return;
				}

				MindmapUpdateController mindmapUpdateController = new MindmapUpdateController();
				mindmapUpdateController.addMindmapUpdater(new ReferenceUpdater(TextUtils
						.getText("update_references_open_mindmaps")));
				mindmapUpdateController.updateCurrentMindmap(true);

				DocearReferenceUpdateController.unlock();
			}
		});
	}

	
}
