package org.docear.plugin.bibtex.listeners;

import java.net.URI;

import javax.swing.SwingUtilities;

import net.sf.jabref.BibtexEntry;
import net.sf.jabref.export.DocearReferenceUpdateController;

import org.docear.plugin.bibtex.JabRefAttributes;
import org.docear.plugin.bibtex.ReferenceUpdater;
import org.docear.plugin.bibtex.ReferencesController;
import org.docear.plugin.core.event.DocearEvent;
import org.docear.plugin.core.event.DocearEventType;
import org.docear.plugin.core.event.IDocearEventListener;
import org.docear.plugin.core.mindmap.MindmapUpdateController;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;

public class PdfAddedListener implements IDocearEventListener {

	public void handleEvent(DocearEvent event) {
		if (DocearReferenceUpdateController.isLocked()) {
			return;
		}		
		if (event.getType() != DocearEventType.MINDMAP_ADD_PDF_TO_NODE) {
			return;
		}
		
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				// ReferencesController.getController().getJabrefWrapper().getBasePanel().undoManager.undoableEditHappened(e)

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
		
//		NodeModel node = (NodeModel) event.getSource();
//		URI uri = (URI) event.getEventObject();
//		
//		JabRefAttributes jabRefAttributes = ReferencesController.getController().getJabRefAttributes();
//		BibtexEntry entry = jabRefAttributes.findBibtexEntryForPDF(uri,node);
//		
//		if (entry != null) {
//			jabRefAttributes.setReferenceToNode(entry, node);
//		}
	}

}
