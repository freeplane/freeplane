/*
 * Created on 9 Sep 2022
 *
 * author dimitry
 */
package org.freeplane.api;

public enum ChildNodesAlignment {
	NOT_SET(false), AFTER_PARENT(true), FIRST_CHILD_BY_PARENT(false), BY_CENTER(false), FLOW(false), LAST_CHILD_BY_PARENT(false), BEFORE_PARENT(true), AUTO(false), STACKED_AUTO(true);

    private final boolean isStacked;

    private ChildNodesAlignment(boolean isStacked) {
        this.isStacked = isStacked;
    }

    public boolean isStacked() {
        return isStacked;
    }
}
