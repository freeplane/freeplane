package org.docear.plugin.bibtex.listeners;


import org.docear.plugin.bibtex.ReferenceUpdater;
import org.docear.plugin.bibtex.SplmmReferenceUpdater;
import org.docear.plugin.core.mindmap.MindmapUpdateController;
import org.docear.plugin.pdfutilities.features.ISplmmMapsConvertListener;
import org.docear.plugin.pdfutilities.features.SplmmMapsConvertEvent;
import org.freeplane.core.util.TextUtils;

public class SplmmMapsConvertListener implements ISplmmMapsConvertListener {

	public void mapsConvert(SplmmMapsConvertEvent event) {	
		MindmapUpdateController mindmapUpdateController = (MindmapUpdateController) event.getObject();
		mindmapUpdateController.addMindmapUpdater(new SplmmReferenceUpdater(TextUtils.getText("update_splmm_references_current_map")));
		mindmapUpdateController.addMindmapUpdater(new ReferenceUpdater(TextUtils.getText("update_references_current_mindmap")));
	}

}
