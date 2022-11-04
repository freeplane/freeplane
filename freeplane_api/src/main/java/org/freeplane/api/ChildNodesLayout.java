/*
 * Created on 4 Nov 2022
 *
 * author dimitry
 */
package org.freeplane.api;

public enum ChildNodesLayout {
    NOT_SET(false, ChildrenSides.NOT_SET, ChildNodesAlignment.UNDEFINED),
    AUTO(false, ChildrenSides.AUTO, ChildNodesAlignment.AS_PARENT),
    TOPTOBOTTOM_BOTHSIDES_TOP(false, ChildrenSides.BOTH_SIDES, ChildNodesAlignment.BY_FIRST_NODE),
    TOPTOBOTTOM_BOTHSIDES_CENTERED(false, ChildrenSides.BOTH_SIDES, ChildNodesAlignment.BY_CENTER),
    TOPTOBOTTOM_BOTHSIDES_BOTTOM(false, ChildrenSides.BOTH_SIDES, ChildNodesAlignment.BY_LAST_NODE),
    TOPTOBOTTOM_BOTHSIDES_BYOWNSIDE(false, ChildrenSides.BOTH_SIDES, ChildNodesAlignment.BY_OWN_SIDE),
    TOPTOBOTTOM_LEFT_TOP(false, ChildrenSides.TOP_OR_LEFT, ChildNodesAlignment.BY_FIRST_NODE),
    TOPTOBOTTOM_LEFT_CENTERED(false, ChildrenSides.TOP_OR_LEFT, ChildNodesAlignment.BY_CENTER),
    TOPTOBOTTOM_LEFT_BOTTOM(false, ChildrenSides.TOP_OR_LEFT, ChildNodesAlignment.BY_LAST_NODE),
    TOPTOBOTTOM_LEFT_BYOWNSIDE(false, ChildrenSides.TOP_OR_LEFT, ChildNodesAlignment.BY_OWN_SIDE),
    TOPTOBOTTOM_RIGHT_TOP(false, ChildrenSides.BOTTOM_OR_RIGHT, ChildNodesAlignment.BY_FIRST_NODE),
    TOPTOBOTTOM_RIGHT_CENTERED(false, ChildrenSides.BOTTOM_OR_RIGHT, ChildNodesAlignment.BY_CENTER),
    TOPTOBOTTOM_RIGHT_BOTTOM(false, ChildrenSides.BOTTOM_OR_RIGHT, ChildNodesAlignment.BY_LAST_NODE),
    TOPTOBOTTOM_RIGHT_BYOWNSIDE(false, ChildrenSides.BOTTOM_OR_RIGHT, ChildNodesAlignment.BY_OWN_SIDE),
    LEFTTORIGHT_BOTHSIDES_LEFT(true, ChildrenSides.BOTH_SIDES, ChildNodesAlignment.BY_FIRST_NODE),
    LEFTTORIGHT_BOTHSIDES_CENTERED(true, ChildrenSides.BOTH_SIDES, ChildNodesAlignment.BY_CENTER),
    LEFTTORIGHT_BOTHSIDES_RIGHT(true, ChildrenSides.BOTH_SIDES, ChildNodesAlignment.BY_LAST_NODE),
    LEFTTORIGHT_BOTHSIDES_BYOWNSIDE(true, ChildrenSides.BOTH_SIDES, ChildNodesAlignment.BY_OWN_SIDE),
    LEFTTORIGHT_TOP_LEFT(true, ChildrenSides.TOP_OR_LEFT, ChildNodesAlignment.BY_FIRST_NODE),
    LEFTTORIGHT_TOP_CENTERED(true, ChildrenSides.TOP_OR_LEFT, ChildNodesAlignment.BY_CENTER),
    LEFTTORIGHT_TOP_RIGHT(true, ChildrenSides.TOP_OR_LEFT, ChildNodesAlignment.BY_LAST_NODE),
    LEFTTORIGHT_TOP_BYOWNSIDE(true, ChildrenSides.TOP_OR_LEFT, ChildNodesAlignment.BY_OWN_SIDE),
    LEFTTORIGHT_BOTTOM_LEFT(true, ChildrenSides.BOTTOM_OR_RIGHT, ChildNodesAlignment.BY_FIRST_NODE),
    LEFTTORIGHT_BOTTOM_CENTERED(true, ChildrenSides.BOTTOM_OR_RIGHT, ChildNodesAlignment.BY_CENTER),
    LEFTTORIGHT_BOTTOM_RIGHT(true, ChildrenSides.BOTTOM_OR_RIGHT, ChildNodesAlignment.BY_LAST_NODE),
    LEFTTORIGHT_BOTTOM_BYOWNSIDE(true, ChildrenSides.BOTTOM_OR_RIGHT, ChildNodesAlignment.BY_OWN_SIDE),
    ;
    
    private final boolean usesHorizontalLayout;
    private final ChildrenSides childrenSides;
    private final ChildNodesAlignment childNodesAlignment;

    private ChildNodesLayout(boolean usesHorizontalLayout, ChildrenSides childrenSides, ChildNodesAlignment childNodesAlignment) {
        this.usesHorizontalLayout = usesHorizontalLayout;
        this.childrenSides = childrenSides;
        this.childNodesAlignment = childNodesAlignment;
    }

    public boolean isUsesHorizontalLayout() {
        return usesHorizontalLayout;
    }

    public ChildrenSides childrenSides() {
        return childrenSides;
    }

    public ChildNodesAlignment childNodesAlignment() {
        return childNodesAlignment;
    }
    
    
}
