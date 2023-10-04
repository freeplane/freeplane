package org.freeplane.features.filter.hidden;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.freeplane.core.enumeration.NodeEnumerationAction;
import org.freeplane.core.ui.SelectableAction;
import org.freeplane.features.map.NodeModel;

@SelectableAction(checkOnNodeChange=true)
public class HideNodeAction extends NodeEnumerationAction<NodeVisibility>{
	private static final long serialVersionUID = 1L;

	public HideNodeAction() {
		super(NodeVisibility.HIDDEN);
	}

	@Override
	protected List<NodeModel> getNodes() {
		return getNodesStream().collect(Collectors.toList());
	}

    private Stream<NodeModel> getNodesStream() {
        return super.getNodes().stream().filter(this::isNotRoot);
    }

	private boolean isNotRoot(NodeModel node) {
		return ! node.isRoot();
	}

    @Override
    public void setSelected() {
        final boolean selected = getNodesStream().limit(1).anyMatch(node -> node.getExtension(getValueClass()) == getValue());
        setSelected(selected);
    }
}
