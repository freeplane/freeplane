package org.freeplane.api;

public enum Side {
	DEFAULT, TOP_OR_LEFT, BOTTOM_OR_RIGHT;
    @Deprecated public static final Side LEFT = TOP_OR_LEFT;
    @Deprecated public static final Side RIGHT = BOTTOM_OR_RIGHT;
}
