package org.freeplane.features.map;

import org.freeplane.core.util.TextUtils;
import org.freeplane.features.filter.condition.ASelectableCondition;
import org.freeplane.n3.nanoxml.XMLElement;

public class LeafCondition extends ASelectableCondition {
	public static final String NAME = "node_leaf_condition";
	@Override
	protected String getName() {
		return NAME;
	}

	public boolean checkNode(NodeModel node) {
		return node.isLeaf();
	}

	public static ASelectableCondition load(XMLElement element) {
	    return new LeafCondition();
    }

	@Override
    protected String createDescription() {
	    return TextUtils.getText(NodeLevelConditionController.FILTER_LEAF);
    }
}
