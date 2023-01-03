/*
 * Created on 9 Sep 2022
 *
 * author dimitry
 */
package org.freeplane.api;

public enum ChildNodesAlignment {
	NOT_SET(false), AFTER_PARENT(true), BY_CENTER(false), BEFORE_PARENT(true), AUTO(false);

    public final boolean areChildrenApart;
    private ChildNodesAlignment(boolean areChildrenApart) {
        this.areChildrenApart = areChildrenApart;

    }
}
