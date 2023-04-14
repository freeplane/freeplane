/*
 * Created on 9 Sep 2022
 *
 * author dimitry
 */
package org.freeplane.api;

public enum ChildNodesAlignment {
	NOT_SET(false, false),
	FIRST_CHILD_BY_PARENT(false, false), BY_CENTER(false, false), LAST_CHILD_BY_PARENT(false, false),
	AFTER_PARENT(true, false), BEFORE_PARENT(true, false),
	TOP_OR_LEFT(true, true), BOTTOM_OR_RIGHT(true, true), CENTER(true, true),
	AUTO(false, false);

    private final boolean isStacked;

    private final boolean areChildrenAlignedWithParent;

    private ChildNodesAlignment(boolean isStacked, boolean areChildrenAlignedWithParent) {
        this.isStacked = isStacked;
        this.areChildrenAlignedWithParent = areChildrenAlignedWithParent;
    }

    public boolean isStacked() {
        return isStacked;
    }

    public boolean areChildrenAlignedWithParent() {
        return areChildrenAlignedWithParent;
    }

    public boolean isChildStackedBeforeParent(boolean isChildTopOrLeft) {
        return this == BEFORE_PARENT
                || areChildrenAlignedWithParent() && isChildTopOrLeft;
    }

    public boolean isChildStackedAfterParent(boolean isChildTopOrLeft) {
        return this == AFTER_PARENT
                || areChildrenAlignedWithParent() && ! isChildTopOrLeft;

    }

}
