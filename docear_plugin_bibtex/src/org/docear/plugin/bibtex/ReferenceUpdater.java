package org.docear.plugin.bibtex;

import net.sf.jabref.BibtexEntry;
import net.sf.jabref.export.DocearReferenceUpdateController;

import org.docear.plugin.core.mindmap.AMindmapUpdater;
import org.freeplane.features.map.NodeModel;

public class ReferenceUpdater extends AMindmapUpdater {

	public ReferenceUpdater(String title) {
		super(title);		
	}

	@Override
	public void updateNode(NodeModel node) {
		if (DocearReferenceUpdateController.isLocked()) {
			return;
		}		
		DocearReferenceUpdateController.lock();
		
		JabRefAttributes jabrefAttributes = ReferencesController.getController().getJabRefAttributes();
		String bibtexKey = jabrefAttributes.getBibtexKey(node);
		
		if (bibtexKey != null) {
			BibtexEntry entry = ReferencesController.getController().getJabrefWrapper().getDatabase().getEntryByKey(bibtexKey);
			
			if (entry != null) {
				jabrefAttributes.setReferenceToNode(entry, node);
			}
		}
		
		DocearReferenceUpdateController.unlock();
	}

}
