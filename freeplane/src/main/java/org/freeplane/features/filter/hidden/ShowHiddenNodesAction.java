package org.freeplane.features.filter.hidden;

import java.util.Collections;
import java.util.List;

import org.freeplane.core.enumeration.NodeEnumerationAction;
import org.freeplane.core.ui.SelectableAction;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;

@SelectableAction(checkOnPopup=true)
public class ShowHiddenNodesAction extends NodeEnumerationAction<NodeVisibilityConfiguration>{
	private static final long serialVersionUID = 1L;

	public ShowHiddenNodesAction() {
		super(NodeVisibilityConfiguration.SHOW_HIDDEN_NODES);
	}

	@Override
	protected List<NodeModel> getNodes() {
		final MapModel map = Controller.getCurrentController().getMap();
		if(map == null)
			return Collections.emptyList();
		else {
			final NodeModel rootNode = map.getRootNode();
			return Collections.singletonList(rootNode);
		}
	}
}
