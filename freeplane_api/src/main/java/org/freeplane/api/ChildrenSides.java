package org.freeplane.api;

public enum ChildrenSides {
	NOT_SET, AUTO, TOP_OR_LEFT, BOTTOM_OR_RIGHT, BOTH_SIDES;

    public static ChildrenSides ofTopOrLeft(boolean isTopOrLeft) {
        return isTopOrLeft ? ChildrenSides.TOP_OR_LEFT : ChildrenSides.BOTTOM_OR_RIGHT;
    }

    public boolean matches(boolean topOrLeft) {
        return this == BOTH_SIDES || this == ofTopOrLeft(topOrLeft);
    }
}
