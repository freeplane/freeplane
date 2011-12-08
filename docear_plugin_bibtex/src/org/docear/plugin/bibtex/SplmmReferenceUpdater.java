package org.docear.plugin.bibtex;

import org.docear.plugin.core.mindmap.AMindmapUpdater;
import org.freeplane.features.map.NodeModel;

public class SplmmReferenceUpdater extends AMindmapUpdater {

	public SplmmReferenceUpdater(String title) {
		super(title);		
	}

	@Override
	public boolean updateNode(NodeModel node) {
		boolean changes = ReferencesController.getController().getSplmmAttributes().translate(node);
		return changes;
	}

}
