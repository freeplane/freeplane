package org.docear.plugin.bibtex.listeners;

import java.net.URI;

import net.sf.jabref.BibtexEntry;

import org.docear.plugin.bibtex.JabRefAttributes;
import org.docear.plugin.bibtex.ReferencesController;
import org.docear.plugin.core.event.DocearEvent;
import org.docear.plugin.core.event.DocearEventType;
import org.docear.plugin.core.event.IDocearEventListener;
import org.freeplane.features.map.NodeModel;

public class PdfAddedListener implements IDocearEventListener {

	@Override
	public void handleEvent(DocearEvent event) {
		
		if (event.getType() != DocearEventType.MINDMAP_ADD_PDF_TO_NODE) {
			return;
		}
		
		NodeModel node = (NodeModel) event.getSource();
		URI uri = (URI) event.getEventObject();
		
		JabRefAttributes jabRefAttributes = ReferencesController.getController().getJabRefAttributes();
		BibtexEntry entry = jabRefAttributes.findBibtexEntryForPDF(uri);
		
		if (entry != null) {
			jabRefAttributes.setReferenceToNode(entry, node);
		}
	}

}
