package org.freeplane.api;

public enum ChildrenSides {
	NOT_SET, AUTO, TOP_OR_LEFT, BOTTOM_OR_RIGHT, BOTH_SIDES, DIAGONAL_ASCENDING, DIAGONAL_DESCENDING;

    private static final String PLACE_ = "PLACE_";
    private static final String PLACE_AT_THE = PLACE_ + "AT_THE_";
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
                return PLACE_AT_THE + LEFT;
            if(this == BOTTOM_OR_RIGHT)
                return PLACE_AT_THE + RIGHT;
        }
        else if(layoutOrientation == LayoutOrientation.LEFT_TO_RIGHT) {
            if(this == TOP_OR_LEFT)
                return PLACE_AT_THE + TOP;
            if(this == BOTTOM_OR_RIGHT)
                return PLACE_AT_THE + BOTTOM;
        }
        if(this == DIAGONAL_ASCENDING || this == DIAGONAL_DESCENDING)
            return PLACE_ + name();
        else
            return PLACE_AT_THE + name();
    }
}
