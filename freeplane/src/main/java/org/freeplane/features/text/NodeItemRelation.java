package org.freeplane.features.text;

import org.freeplane.features.filter.condition.ICondition;

public interface NodeItemRelation extends ICondition{
	String getNodeItem();
    @Override
    default boolean checksParent() {
        return TextController.FILTER_PARENT_TEXT.equals(getNodeItem());
    }
}
