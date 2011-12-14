package org.docear.plugin.bibtex;

import org.docear.plugin.core.mindmap.AMindmapUpdater;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;

public class SplmmReferenceUpdater extends AMindmapUpdater {

	public SplmmReferenceUpdater(String title) {
		super(title);		
	}

	public boolean updateMindmap(MapModel map) {
		return updateNodesRecursive(map.getRootNode());		
	}

	/**
	 * @param node
	 * @return
	 */
	private boolean updateNodesRecursive(NodeModel node) {
		boolean changes = false;
		for(NodeModel child : node.getChildren()) {
			changes = changes | updateNodesRecursive(child);
		}
		changes = changes | ReferencesController.getController().getSplmmAttributes().translate(node);
		return changes;
	}
	
	
}
