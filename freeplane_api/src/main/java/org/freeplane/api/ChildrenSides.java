package org.freeplane.api;

public enum ChildrenSides {
	NOT_SET, AUTO, TOP_OR_LEFT, BOTTOM_OR_RIGHT, BOTH_SIDES, ASC, DESC;

    private static final String AT_THE_ = "AT_THE_";
    private static final String BOTTOM = "BOTTOM";
       private static final String TOP = "TOP";
    private static final String RIGHT = "RIGHT";
    private static final String LEFT = "LEFT";

    public static ChildrenSides ofTopOrLeft(boolean isTopOrLeft) {
        return isTopOrLeft ? ChildrenSides.TOP_OR_LEFT : ChildrenSides.BOTTOM_OR_RIGHT;
    }

    public boolean matches(boolean topOrLeft) {
        return this == BOTH_SIDES || this == ofTopOrLeft(topOrLeft);
    }

    public String labelKey(LayoutOrientation layoutOrientation) {
        if(layoutOrientation == LayoutOrientation.TOP_TO_BOTTOM) {
            if(this == TOP_OR_LEFT)
                return AT_THE_ + LEFT;
            if(this == BOTTOM_OR_RIGHT)
                return AT_THE_ + RIGHT;
        }
        else if(layoutOrientation == LayoutOrientation.LEFT_TO_RIGHT) {
            if(this == TOP_OR_LEFT)
                return AT_THE_ + TOP;
            if(this == BOTTOM_OR_RIGHT)
                return AT_THE_ + BOTTOM;
        }
        return AT_THE_ + name();
    }
}
