package org.freeplane.features.map;

import org.freeplane.core.util.TextUtils;
import org.freeplane.features.filter.condition.ASelectableCondition;
import org.freeplane.n3.nanoxml.XMLElement;

public class RootCondition extends ASelectableCondition {
	public static final String NAME = "node_root_condition";
	@Override
	protected String getName() {
		return NAME;
	}

	public boolean checkNode(NodeModel node) {
		return node.isRoot();
	}

	public static ASelectableCondition load(XMLElement element) {
	    return new RootCondition();
    }

	@Override
    protected String createDescription() {
	    return TextUtils.getText(NodeLevelConditionController.FILTER_ROOT);
    }
}
