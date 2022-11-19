/*
 * Created on 4 Nov 2022
 *
 * author dimitry
 */
package org.freeplane.api;


public enum ChildNodesLayout {
    NOT_SET(LayoutOrientation.NOT_SET, ChildrenSides.NOT_SET, ParentNodeAlignment.NOT_SET),
    AUTO(LayoutOrientation.AUTO, ChildrenSides.AUTO, ParentNodeAlignment.AUTO),
    AUTO_BYFIRSTCHILD(LayoutOrientation.AUTO, ChildrenSides.AUTO, ParentNodeAlignment.BEFORE_FIRST_CHILD),
    AUTO_CENTERED(LayoutOrientation.AUTO, ChildrenSides.AUTO, ParentNodeAlignment.BY_CENTER),
    AUTO_BYLASTCHILD(LayoutOrientation.AUTO, ChildrenSides.AUTO, ParentNodeAlignment.AFTER_LAST_CHILD),
    TOPTOBOTTOM_BOTHSIDES_AUTO(LayoutOrientation.TOP_TO_BOTTOM, ChildrenSides.BOTH_SIDES, ParentNodeAlignment.AUTO),
    TOPTOBOTTOM_LEFT_AUTO(LayoutOrientation.TOP_TO_BOTTOM, ChildrenSides.TOP_OR_LEFT, ParentNodeAlignment.AUTO),
    TOPTOBOTTOM_RIGHT_AUTO(LayoutOrientation.TOP_TO_BOTTOM, ChildrenSides.BOTTOM_OR_RIGHT, ParentNodeAlignment.AUTO),
    LEFTTORIGHT_TOP_AUTO(LayoutOrientation.LEFT_TO_RIGHT, ChildrenSides.TOP_OR_LEFT, ParentNodeAlignment.AUTO),
    LEFTTORIGHT_BOTTOM_AUTO(LayoutOrientation.LEFT_TO_RIGHT, ChildrenSides.BOTTOM_OR_RIGHT, ParentNodeAlignment.AUTO),
    LEFTTORIGHT_BOTHSIDES_AUTO(LayoutOrientation.LEFT_TO_RIGHT, ChildrenSides.BOTH_SIDES, ParentNodeAlignment.AUTO),
    TOPTOBOTTOM_BOTHSIDES_TOP(LayoutOrientation.TOP_TO_BOTTOM, ChildrenSides.BOTH_SIDES, ParentNodeAlignment.BEFORE_FIRST_CHILD),
    TOPTOBOTTOM_BOTHSIDES_CENTERED(LayoutOrientation.TOP_TO_BOTTOM, ChildrenSides.BOTH_SIDES, ParentNodeAlignment.BY_CENTER),
    TOPTOBOTTOM_BOTHSIDES_BOTTOM(LayoutOrientation.TOP_TO_BOTTOM, ChildrenSides.BOTH_SIDES, ParentNodeAlignment.AFTER_LAST_CHILD),
    TOPTOBOTTOM_LEFT_TOP(LayoutOrientation.TOP_TO_BOTTOM, ChildrenSides.TOP_OR_LEFT, ParentNodeAlignment.BEFORE_FIRST_CHILD),
    TOPTOBOTTOM_LEFT_CENTERED(LayoutOrientation.TOP_TO_BOTTOM, ChildrenSides.TOP_OR_LEFT, ParentNodeAlignment.BY_CENTER),
    TOPTOBOTTOM_LEFT_BOTTOM(LayoutOrientation.TOP_TO_BOTTOM, ChildrenSides.TOP_OR_LEFT, ParentNodeAlignment.AFTER_LAST_CHILD),
    TOPTOBOTTOM_RIGHT_TOP(LayoutOrientation.TOP_TO_BOTTOM, ChildrenSides.BOTTOM_OR_RIGHT, ParentNodeAlignment.BEFORE_FIRST_CHILD),
    TOPTOBOTTOM_RIGHT_CENTERED(LayoutOrientation.TOP_TO_BOTTOM, ChildrenSides.BOTTOM_OR_RIGHT, ParentNodeAlignment.BY_CENTER),
    TOPTOBOTTOM_RIGHT_BOTTOM(LayoutOrientation.TOP_TO_BOTTOM, ChildrenSides.BOTTOM_OR_RIGHT, ParentNodeAlignment.AFTER_LAST_CHILD),
    LEFTTORIGHT_BOTHSIDES_LEFT(LayoutOrientation.LEFT_TO_RIGHT, ChildrenSides.BOTH_SIDES, ParentNodeAlignment.BEFORE_FIRST_CHILD),
    LEFTTORIGHT_BOTHSIDES_CENTERED(LayoutOrientation.LEFT_TO_RIGHT, ChildrenSides.BOTH_SIDES, ParentNodeAlignment.BY_CENTER),
    LEFTTORIGHT_BOTHSIDES_RIGHT(LayoutOrientation.LEFT_TO_RIGHT, ChildrenSides.BOTH_SIDES, ParentNodeAlignment.AFTER_LAST_CHILD),
    LEFTTORIGHT_TOP_LEFT(LayoutOrientation.LEFT_TO_RIGHT, ChildrenSides.TOP_OR_LEFT, ParentNodeAlignment.BEFORE_FIRST_CHILD),
    LEFTTORIGHT_TOP_CENTERED(LayoutOrientation.LEFT_TO_RIGHT, ChildrenSides.TOP_OR_LEFT, ParentNodeAlignment.BY_CENTER),
    LEFTTORIGHT_TOP_RIGHT(LayoutOrientation.LEFT_TO_RIGHT, ChildrenSides.TOP_OR_LEFT, ParentNodeAlignment.AFTER_LAST_CHILD),
    LEFTTORIGHT_BOTTOM_LEFT(LayoutOrientation.LEFT_TO_RIGHT, ChildrenSides.BOTTOM_OR_RIGHT, ParentNodeAlignment.BEFORE_FIRST_CHILD),
    LEFTTORIGHT_BOTTOM_CENTERED(LayoutOrientation.LEFT_TO_RIGHT, ChildrenSides.BOTTOM_OR_RIGHT, ParentNodeAlignment.BY_CENTER),
    LEFTTORIGHT_BOTTOM_RIGHT(LayoutOrientation.LEFT_TO_RIGHT, ChildrenSides.BOTTOM_OR_RIGHT, ParentNodeAlignment.AFTER_LAST_CHILD),
    ;
    
    private final ChildrenSides childrenSides;
    private final ParentNodeAlignment parentNodeAlignment;
    private final LayoutOrientation layoutOrientation;

    private ChildNodesLayout(LayoutOrientation layoutOrientation, ChildrenSides childrenSides, ParentNodeAlignment parentNodeAlignment) {
        this.layoutOrientation = layoutOrientation;
        this.childrenSides = childrenSides;
        this.parentNodeAlignment = parentNodeAlignment;
    }

    public ChildrenSides childrenSides() {
        return childrenSides;
    }

    public ParentNodeAlignment parentNodeAlignment() {
        return parentNodeAlignment;
    }

    public LayoutOrientation layoutOrientation() {
        return layoutOrientation;
    }
}
