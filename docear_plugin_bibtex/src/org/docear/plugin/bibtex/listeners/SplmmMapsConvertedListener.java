package org.docear.plugin.bibtex.listeners;

import java.util.List;

import org.docear.plugin.bibtex.ReferenceUpdater;
import org.docear.plugin.bibtex.SplmmReferenceUpdater;
import org.docear.plugin.core.listeners.ISplmmMapsConvertedListener;
import org.docear.plugin.core.listeners.SplmmMapsConvertedEvent;
import org.docear.plugin.core.mindmap.MindmapUpdateController;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.MapModel;

public class SplmmMapsConvertedListener implements ISplmmMapsConvertedListener {

	@SuppressWarnings("unchecked")
	@Override
	public void mapsConverted(SplmmMapsConvertedEvent event) {
		List<MapModel> maps = (List<MapModel>) event.getObject();

		if (maps != null) {
			MindmapUpdateController mindmapUpdateController = new MindmapUpdateController();
			mindmapUpdateController.addMindmapUpdater(new SplmmReferenceUpdater(TextUtils.getText("update_splmm_references_all_mindmaps")));
			mindmapUpdateController.addMindmapUpdater(new ReferenceUpdater(TextUtils.getText("update_references_open_mindmaps")));
			mindmapUpdateController.updateMindmapsInList(maps);
		}
	}

}
