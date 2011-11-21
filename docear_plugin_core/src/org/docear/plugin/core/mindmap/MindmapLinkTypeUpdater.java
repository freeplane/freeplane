package org.docear.plugin.core.mindmap;

import java.net.URI;

import org.freeplane.features.link.LinkController;
import org.freeplane.features.link.NodeLinks;
import org.freeplane.features.map.NodeModel;
import org.freeplane.plugin.workspace.WorkspaceUtils;

public class MindmapLinkTypeUpdater extends AMindmapUpdater {

	public MindmapLinkTypeUpdater(String title) {
		super(title);		
	}

	@Override
	public boolean updateNode(NodeModel node) {
		NodeLinks links = NodeLinks.getLinkExtension(node);
		
		if (links == null || links.getHyperLink() == null) {
			return false;
		}
		
		URI uri = links.getHyperLink();
		links.setHyperLink(LinkController.toLinkTypeDependantURI(node.getMap().getFile(), WorkspaceUtils.resolveURI(uri)));
				
		return true;
	}

}
