/*
 * Created on 4 Nov 2022
 *
 * author dimitry
 */
package org.freeplane.api;

public enum ChildNodesLayout {
    NOT_SET(LayoutOrientation.NOT_SET, ChildrenSides.NOT_SET, ChildNodesAlignment.UNDEFINED),
    AUTO(LayoutOrientation.AS_PARENT, ChildrenSides.AUTO, ChildNodesAlignment.AS_PARENT),
    TOPTOBOTTOM_BOTHSIDES_TOP(LayoutOrientation.TOP_TO_BOTTOM, ChildrenSides.BOTH_SIDES, ChildNodesAlignment.BY_FIRST_NODE),
    TOPTOBOTTOM_BOTHSIDES_CENTERED(LayoutOrientation.TOP_TO_BOTTOM, ChildrenSides.BOTH_SIDES, ChildNodesAlignment.BY_CENTER),
    TOPTOBOTTOM_BOTHSIDES_BOTTOM(LayoutOrientation.TOP_TO_BOTTOM, ChildrenSides.BOTH_SIDES, ChildNodesAlignment.BY_LAST_NODE),
    TOPTOBOTTOM_BOTHSIDES_BYOWNSIDE(LayoutOrientation.TOP_TO_BOTTOM, ChildrenSides.BOTH_SIDES, ChildNodesAlignment.BY_OWN_SIDE),
    TOPTOBOTTOM_LEFT_TOP(LayoutOrientation.TOP_TO_BOTTOM, ChildrenSides.TOP_OR_LEFT, ChildNodesAlignment.BY_FIRST_NODE),
    TOPTOBOTTOM_LEFT_CENTERED(LayoutOrientation.TOP_TO_BOTTOM, ChildrenSides.TOP_OR_LEFT, ChildNodesAlignment.BY_CENTER),
    TOPTOBOTTOM_LEFT_BOTTOM(LayoutOrientation.TOP_TO_BOTTOM, ChildrenSides.TOP_OR_LEFT, ChildNodesAlignment.BY_LAST_NODE),
    TOPTOBOTTOM_LEFT_BYOWNSIDE(LayoutOrientation.TOP_TO_BOTTOM, ChildrenSides.TOP_OR_LEFT, ChildNodesAlignment.BY_OWN_SIDE),
    TOPTOBOTTOM_RIGHT_TOP(LayoutOrientation.TOP_TO_BOTTOM, ChildrenSides.BOTTOM_OR_RIGHT, ChildNodesAlignment.BY_FIRST_NODE),
    TOPTOBOTTOM_RIGHT_CENTERED(LayoutOrientation.TOP_TO_BOTTOM, ChildrenSides.BOTTOM_OR_RIGHT, ChildNodesAlignment.BY_CENTER),
    TOPTOBOTTOM_RIGHT_BOTTOM(LayoutOrientation.TOP_TO_BOTTOM, ChildrenSides.BOTTOM_OR_RIGHT, ChildNodesAlignment.BY_LAST_NODE),
    TOPTOBOTTOM_RIGHT_BYOWNSIDE(LayoutOrientation.TOP_TO_BOTTOM, ChildrenSides.BOTTOM_OR_RIGHT, ChildNodesAlignment.BY_OWN_SIDE),
    LEFTTORIGHT_BOTHSIDES_LEFT(LayoutOrientation.LEFT_TO_RIGHT, ChildrenSides.BOTH_SIDES, ChildNodesAlignment.BY_FIRST_NODE),
    LEFTTORIGHT_BOTHSIDES_CENTERED(LayoutOrientation.LEFT_TO_RIGHT, ChildrenSides.BOTH_SIDES, ChildNodesAlignment.BY_CENTER),
    LEFTTORIGHT_BOTHSIDES_RIGHT(LayoutOrientation.LEFT_TO_RIGHT, ChildrenSides.BOTH_SIDES, ChildNodesAlignment.BY_LAST_NODE),
    LEFTTORIGHT_BOTHSIDES_BYOWNSIDE(LayoutOrientation.LEFT_TO_RIGHT, ChildrenSides.BOTH_SIDES, ChildNodesAlignment.BY_OWN_SIDE),
    LEFTTORIGHT_TOP_LEFT(LayoutOrientation.LEFT_TO_RIGHT, ChildrenSides.TOP_OR_LEFT, ChildNodesAlignment.BY_FIRST_NODE),
    LEFTTORIGHT_TOP_CENTERED(LayoutOrientation.LEFT_TO_RIGHT, ChildrenSides.TOP_OR_LEFT, ChildNodesAlignment.BY_CENTER),
    LEFTTORIGHT_TOP_RIGHT(LayoutOrientation.LEFT_TO_RIGHT, ChildrenSides.TOP_OR_LEFT, ChildNodesAlignment.BY_LAST_NODE),
    LEFTTORIGHT_TOP_BYOWNSIDE(LayoutOrientation.LEFT_TO_RIGHT, ChildrenSides.TOP_OR_LEFT, ChildNodesAlignment.BY_OWN_SIDE),
    LEFTTORIGHT_BOTTOM_LEFT(LayoutOrientation.LEFT_TO_RIGHT, ChildrenSides.BOTTOM_OR_RIGHT, ChildNodesAlignment.BY_FIRST_NODE),
    LEFTTORIGHT_BOTTOM_CENTERED(LayoutOrientation.LEFT_TO_RIGHT, ChildrenSides.BOTTOM_OR_RIGHT, ChildNodesAlignment.BY_CENTER),
    LEFTTORIGHT_BOTTOM_RIGHT(LayoutOrientation.LEFT_TO_RIGHT, ChildrenSides.BOTTOM_OR_RIGHT, ChildNodesAlignment.BY_LAST_NODE),
    LEFTTORIGHT_BOTTOM_BYOWNSIDE(LayoutOrientation.LEFT_TO_RIGHT, ChildrenSides.BOTTOM_OR_RIGHT, ChildNodesAlignment.BY_OWN_SIDE),
    ;
    
    private final ChildrenSides childrenSides;
    private final ChildNodesAlignment childNodesAlignment;
    private final LayoutOrientation layoutOrientation;

    private ChildNodesLayout(LayoutOrientation layoutOrientation, ChildrenSides childrenSides, ChildNodesAlignment childNodesAlignment) {
        this.layoutOrientation = layoutOrientation;
        this.childrenSides = childrenSides;
        this.childNodesAlignment = childNodesAlignment;
    }

    public ChildrenSides childrenSides() {
        return childrenSides;
    }

    public ChildNodesAlignment childNodesAlignment() {
        return childNodesAlignment;
    }

    public LayoutOrientation layoutOrientation() {
        return layoutOrientation;
    }
}
