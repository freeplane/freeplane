package org.docear.plugin.bibtex;

import org.docear.plugin.core.mindmap.AMindmapUpdater;
import org.freeplane.features.map.MapModel;

public class SplmmReferenceUpdater extends AMindmapUpdater {

	public SplmmReferenceUpdater(String title) {
		super(title);		
	}

//	@Override
//	public boolean updateMindmap(Ma) {
//		boolean changes = ReferencesController.getController().getSplmmAttributes().translate(node);
//		return changes;
//	}



	@Override
	public boolean updateMindmap(MapModel map) {
		return false;
	}}
