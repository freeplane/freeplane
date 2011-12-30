package org.docear.plugin.bibtex.listeners;

import org.docear.plugin.bibtex.ReferenceUpdater;
import org.docear.plugin.core.listeners.ISplmmMapsConvertListener;
import org.docear.plugin.core.listeners.SplmmMapsConvertEvent;
import org.docear.plugin.core.mindmap.MindmapUpdateController;
import org.freeplane.core.util.TextUtils;

public class SplmmMapsConvertListener implements ISplmmMapsConvertListener {

	public void mapsConvert(SplmmMapsConvertEvent event) {	
		MindmapUpdateController mindmapUpdateController = (MindmapUpdateController) event.getObject();
		mindmapUpdateController.addMindmapUpdater(new ReferenceUpdater(TextUtils.getText("update_references_open_mindmaps")));
	}

}
