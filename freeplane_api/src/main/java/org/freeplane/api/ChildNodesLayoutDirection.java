/*
 * Created on 9 Sep 2022
 *
 * author dimitry
 */
package org.freeplane.api;

public enum ChildNodesLayoutDirection {
	UNDEFINED(false), LEFT_TO_RIGHT(true), TO_LEFT(true), TO_RIGHT(true),
	TOP_TO_BOTTOM(false), TO_TOP(false), TO_BOTTOM(false), AS_PARENT(false);
    private final boolean isHorizontal;

    private ChildNodesLayoutDirection(boolean isHorizontal) {
        this.isHorizontal = isHorizontal;
    }

    public boolean isHorizontal() {
        return isHorizontal;
    }
}
