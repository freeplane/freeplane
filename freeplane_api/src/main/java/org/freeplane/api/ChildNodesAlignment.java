/*
 * Created on 9 Sep 2022
 *
 * author dimitry
 */
package org.freeplane.api;

public enum ChildNodesAlignment {
	NOT_SET(false), AFTER_PARENT(true), FIRST_CHILD_BY_PARENT(false), BY_CENTER(false), LAST_CHILD_BY_PARENT(false), BEFORE_PARENT(true),
	TOP_OR_LEFT(true), BOTTOM_OR_RIGHT(true), CENTER(true), AUTO(false);

    private final boolean isStacked;

    private ChildNodesAlignment(boolean isStacked) {
        this.isStacked = isStacked;
    }

    public boolean isStacked() {
        return isStacked;
    }
}
