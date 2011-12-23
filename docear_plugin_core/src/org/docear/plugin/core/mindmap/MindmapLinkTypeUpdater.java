package org.docear.plugin.core.mindmap;

import java.io.File;
import java.net.URI;

import org.freeplane.features.link.LinkController;
import org.freeplane.features.link.NodeLinks;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.plugin.workspace.WorkspaceUtils;

public class MindmapLinkTypeUpdater extends AMindmapUpdater {

	public MindmapLinkTypeUpdater(String title) {
		super(title);		
	}

	public boolean updateMindmap(MapModel map) {
		return updateNodesRecursive(map.getRootNode());
	}
	
	private boolean updateMindmap(NodeModel node) {
		NodeLinks links = NodeLinks.getLinkExtension(node);

		if (links == null || links.getHyperLink() == null) {
			return false;
		}

		URI uri = links.getHyperLink();

		File file = WorkspaceUtils.resolveURI(uri, node.getMap());
		if (file != null) {
			links.setHyperLink(LinkController.toLinkTypeDependantURI(node.getMap().getFile(), file));
		}

		return true;
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
		changes = changes | updateMindmap(node);
		return changes;
	}

}
