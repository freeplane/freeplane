package org.freeplane.features.filter.hidden;

import java.util.List;
import java.util.stream.Collectors;

import org.freeplane.core.enumeration.NodeEnumerationAction;
import org.freeplane.core.ui.EnabledAction;
import org.freeplane.core.ui.SelectableAction;
import org.freeplane.features.map.NodeModel;

@EnabledAction(checkOnPopup=true)
@SelectableAction(checkOnPopup=true)
public class HideNodeAction extends NodeEnumerationAction<NodeVisibility>{
	private static final long serialVersionUID = 1L;

	public HideNodeAction() {
		super(NodeVisibility.HIDDEN);
	}

	@Override
	protected List<NodeModel> getNodes() {
		return super.getNodes().stream().filter(this::isNotRoot).collect(Collectors.toList());
	}

	private boolean isNotRoot(NodeModel node) {
		return ! node.isRoot();
	}

}
