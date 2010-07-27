package org.freeplane.features.common.map;

import org.freeplane.core.util.TextUtils;
import org.freeplane.features.common.filter.condition.ASelectableCondition;
import org.freeplane.features.common.filter.condition.ISelectableCondition;
import org.freeplane.n3.nanoxml.XMLElement;

public class EvenLevelCondition extends ASelectableCondition {
	public static final String NAME = "node_even_level_condition";
	@Override
	protected String getName() {
		return NAME;
	}

	public boolean checkNode(NodeModel node) {
		final int nodeLevel = node.getNodeLevel(true);
		return 0 == (nodeLevel & 1);
	}

	public static ISelectableCondition load(XMLElement element) {
	    return new EvenLevelCondition();
    }

	@Override
    protected String createDesctiption() {
	    return TextUtils.getText(NodeLevelConditionController.FILTER_EVEN_LEVEL);
    }
}
