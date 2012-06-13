package org.docear.plugin.services.recommendations.workspace;

import org.docear.plugin.services.recommendations.actions.ShowRecommendationsAction;
import org.freeplane.core.util.TextUtils;
import org.freeplane.n3.nanoxml.XMLElement;
import org.freeplane.plugin.workspace.model.AWorkspaceNodeCreator;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;

public class ShowRecommendationsCreator extends AWorkspaceNodeCreator {
	
	public static final String NODE_TYPE = ShowRecommendationsAction.TYPE;
	
	@Override
	public AWorkspaceTreeNode getNode(XMLElement data) {
		String type = data.getAttribute("type", NODE_TYPE);
		ShowRecommendationsNode node = new ShowRecommendationsNode(type);	
		node.setName(TextUtils.getText("recommendations.workspace.node"));
		return node;
	}

}
