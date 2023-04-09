/*
 * Created on 4 Nov 2022
 *
 * author dimitry
 */
package org.freeplane.api;

import java.util.Optional;
import java.util.stream.Stream;

public enum ChildNodesLayout {
    NOT_SET(LayoutOrientation.NOT_SET, ChildrenSides.NOT_SET, ChildNodesAlignment.NOT_SET),

    TOPTOBOTTOM_LEFT_TOP(LayoutOrientation.TOP_TO_BOTTOM, ChildrenSides.TOP_OR_LEFT, ChildNodesAlignment.BEFORE_PARENT),
    TOPTOBOTTOM_BOTHSIDES_TOP(LayoutOrientation.TOP_TO_BOTTOM, ChildrenSides.BOTH_SIDES, ChildNodesAlignment.BEFORE_PARENT),
    TOPTOBOTTOM_RIGHT_TOP(LayoutOrientation.TOP_TO_BOTTOM, ChildrenSides.BOTTOM_OR_RIGHT, ChildNodesAlignment.BEFORE_PARENT),

    LEFTTORIGHT_TOP_LEFT(LayoutOrientation.LEFT_TO_RIGHT, ChildrenSides.TOP_OR_LEFT, ChildNodesAlignment.BEFORE_PARENT),
    LEFTTORIGHT_TOP_LAST(LayoutOrientation.LEFT_TO_RIGHT, ChildrenSides.TOP_OR_LEFT, ChildNodesAlignment.LAST_CHILD_BY_PARENT),
    LEFTTORIGHT_TOP_CENTERED(LayoutOrientation.LEFT_TO_RIGHT, ChildrenSides.TOP_OR_LEFT, ChildNodesAlignment.BY_CENTER),
    LEFTTORIGHT_TOP_FIRST(LayoutOrientation.LEFT_TO_RIGHT, ChildrenSides.TOP_OR_LEFT, ChildNodesAlignment.FIRST_CHILD_BY_PARENT),
    LEFTTORIGHT_TOP_RIGHT(LayoutOrientation.LEFT_TO_RIGHT, ChildrenSides.TOP_OR_LEFT, ChildNodesAlignment.AFTER_PARENT),

    TOPTOBOTTOM_LEFT_LAST(LayoutOrientation.TOP_TO_BOTTOM, ChildrenSides.TOP_OR_LEFT, ChildNodesAlignment.LAST_CHILD_BY_PARENT),
    TOPTOBOTTOM_BOTHSIDES_LAST(LayoutOrientation.TOP_TO_BOTTOM, ChildrenSides.BOTH_SIDES, ChildNodesAlignment.LAST_CHILD_BY_PARENT),
    TOPTOBOTTOM_RIGHT_LAST(LayoutOrientation.TOP_TO_BOTTOM, ChildrenSides.BOTTOM_OR_RIGHT, ChildNodesAlignment.LAST_CHILD_BY_PARENT),

    TOPTOBOTTOM_LEFT_CENTERED(LayoutOrientation.TOP_TO_BOTTOM, ChildrenSides.TOP_OR_LEFT, ChildNodesAlignment.BY_CENTER),
    TOPTOBOTTOM_BOTHSIDES_CENTERED(LayoutOrientation.TOP_TO_BOTTOM, ChildrenSides.BOTH_SIDES, ChildNodesAlignment.BY_CENTER),
    TOPTOBOTTOM_RIGHT_CENTERED(LayoutOrientation.TOP_TO_BOTTOM, ChildrenSides.BOTTOM_OR_RIGHT, ChildNodesAlignment.BY_CENTER),

    LEFTTORIGHT_BOTHSIDES_LEFT(LayoutOrientation.LEFT_TO_RIGHT, ChildrenSides.BOTH_SIDES, ChildNodesAlignment.BEFORE_PARENT),
    LEFTTORIGHT_BOTHSIDES_LAST(LayoutOrientation.LEFT_TO_RIGHT, ChildrenSides.BOTH_SIDES, ChildNodesAlignment.LAST_CHILD_BY_PARENT),
    LEFTTORIGHT_BOTHSIDES_CENTERED(LayoutOrientation.LEFT_TO_RIGHT, ChildrenSides.BOTH_SIDES, ChildNodesAlignment.BY_CENTER),
    LEFTTORIGHT_BOTHSIDES_FIRST(LayoutOrientation.LEFT_TO_RIGHT, ChildrenSides.BOTH_SIDES, ChildNodesAlignment.FIRST_CHILD_BY_PARENT),
    LEFTTORIGHT_BOTHSIDES_RIGHT(LayoutOrientation.LEFT_TO_RIGHT, ChildrenSides.BOTH_SIDES, ChildNodesAlignment.AFTER_PARENT),

    TOPTOBOTTOM_LEFT_FIRST(LayoutOrientation.TOP_TO_BOTTOM, ChildrenSides.TOP_OR_LEFT, ChildNodesAlignment.FIRST_CHILD_BY_PARENT),
    TOPTOBOTTOM_BOTHSIDES_FIRST(LayoutOrientation.TOP_TO_BOTTOM, ChildrenSides.BOTH_SIDES, ChildNodesAlignment.FIRST_CHILD_BY_PARENT),
    TOPTOBOTTOM_RIGHT_FIRST(LayoutOrientation.TOP_TO_BOTTOM, ChildrenSides.BOTTOM_OR_RIGHT, ChildNodesAlignment.FIRST_CHILD_BY_PARENT),

    TOPTOBOTTOM_LEFT_BOTTOM(LayoutOrientation.TOP_TO_BOTTOM, ChildrenSides.TOP_OR_LEFT, ChildNodesAlignment.AFTER_PARENT),
    TOPTOBOTTOM_BOTHSIDES_BOTTOM(LayoutOrientation.TOP_TO_BOTTOM, ChildrenSides.BOTH_SIDES, ChildNodesAlignment.AFTER_PARENT),
    TOPTOBOTTOM_RIGHT_BOTTOM(LayoutOrientation.TOP_TO_BOTTOM, ChildrenSides.BOTTOM_OR_RIGHT, ChildNodesAlignment.AFTER_PARENT),

    LEFTTORIGHT_BOTTOM_LEFT(LayoutOrientation.LEFT_TO_RIGHT, ChildrenSides.BOTTOM_OR_RIGHT, ChildNodesAlignment.BEFORE_PARENT),
    LEFTTORIGHT_BOTTOM_LAST(LayoutOrientation.LEFT_TO_RIGHT, ChildrenSides.BOTTOM_OR_RIGHT, ChildNodesAlignment.LAST_CHILD_BY_PARENT),
    LEFTTORIGHT_BOTTOM_CENTERED(LayoutOrientation.LEFT_TO_RIGHT, ChildrenSides.BOTTOM_OR_RIGHT, ChildNodesAlignment.BY_CENTER),
    LEFTTORIGHT_BOTTOM_FIRST(LayoutOrientation.LEFT_TO_RIGHT, ChildrenSides.BOTTOM_OR_RIGHT, ChildNodesAlignment.FIRST_CHILD_BY_PARENT),
    LEFTTORIGHT_BOTTOM_RIGHT(LayoutOrientation.LEFT_TO_RIGHT, ChildrenSides.BOTTOM_OR_RIGHT, ChildNodesAlignment.AFTER_PARENT),

    TOPTOBOTTOM_TOP_RIGHT(LayoutOrientation.TOP_TO_BOTTOM, ChildrenSides.TOP_OR_LEFT, ChildNodesAlignment.BOTTOM_OR_RIGHT),
    TOPTOBOTTOM_TOP_CENTER(LayoutOrientation.TOP_TO_BOTTOM, ChildrenSides.TOP_OR_LEFT, ChildNodesAlignment.CENTER),
    TOPTOBOTTOM_TOP_LEFT(LayoutOrientation.TOP_TO_BOTTOM, ChildrenSides.TOP_OR_LEFT, ChildNodesAlignment.TOP_OR_LEFT),

    LEFTTORIGHT_LEFT_BOTTOM(LayoutOrientation.LEFT_TO_RIGHT, ChildrenSides.TOP_OR_LEFT, ChildNodesAlignment.BOTTOM_OR_RIGHT),
    LEFTTORIGHT_BOTHSIDES_BOTTOM(LayoutOrientation.LEFT_TO_RIGHT, ChildrenSides.BOTH_SIDES, ChildNodesAlignment.BOTTOM_OR_RIGHT),
    LEFTTORIGHT_RIGHT_BOTTOM(LayoutOrientation.LEFT_TO_RIGHT, ChildrenSides.BOTTOM_OR_RIGHT, ChildNodesAlignment.BOTTOM_OR_RIGHT),

    TOPTOBOTTOM_BOTHSIDES_RIGHT(LayoutOrientation.TOP_TO_BOTTOM, ChildrenSides.BOTH_SIDES, ChildNodesAlignment.BOTTOM_OR_RIGHT),
    TOPTOBOTTOM_BOTHSIDES_CENTER(LayoutOrientation.TOP_TO_BOTTOM, ChildrenSides.BOTH_SIDES, ChildNodesAlignment.CENTER),
    TOPTOBOTTOM_BOTHSIDES_LEFT(LayoutOrientation.TOP_TO_BOTTOM, ChildrenSides.BOTH_SIDES, ChildNodesAlignment.TOP_OR_LEFT),

    LEFTTORIGHT_LEFT_CENTER(LayoutOrientation.LEFT_TO_RIGHT, ChildrenSides.TOP_OR_LEFT, ChildNodesAlignment.CENTER),
    LEFTTORIGHT_BOTHSIDES_CENTER(LayoutOrientation.LEFT_TO_RIGHT, ChildrenSides.BOTH_SIDES, ChildNodesAlignment.CENTER),
    LEFTTORIGHT_RIGHT_CENTER(LayoutOrientation.LEFT_TO_RIGHT, ChildrenSides.BOTTOM_OR_RIGHT, ChildNodesAlignment.CENTER),

    TOPTOBOTTOM_BOTTOM_RIGHT(LayoutOrientation.TOP_TO_BOTTOM, ChildrenSides.BOTTOM_OR_RIGHT, ChildNodesAlignment.BOTTOM_OR_RIGHT),
    TOPTOBOTTOM_BOTTOM_CENTER(LayoutOrientation.TOP_TO_BOTTOM, ChildrenSides.BOTTOM_OR_RIGHT, ChildNodesAlignment.CENTER),
    TOPTOBOTTOM_BOTTOM_LEFT(LayoutOrientation.TOP_TO_BOTTOM, ChildrenSides.BOTTOM_OR_RIGHT, ChildNodesAlignment.TOP_OR_LEFT),

    LEFTTORIGHT_LEFT_TOP(LayoutOrientation.LEFT_TO_RIGHT, ChildrenSides.TOP_OR_LEFT, ChildNodesAlignment.TOP_OR_LEFT),
    LEFTTORIGHT_BOTHSIDES_TOP(LayoutOrientation.LEFT_TO_RIGHT, ChildrenSides.BOTH_SIDES, ChildNodesAlignment.TOP_OR_LEFT),
    LEFTTORIGHT_RIGHT_TOP(LayoutOrientation.LEFT_TO_RIGHT, ChildrenSides.BOTTOM_OR_RIGHT, ChildNodesAlignment.TOP_OR_LEFT),

    TOPTOBOTTOM_LEFT_AUTO(LayoutOrientation.TOP_TO_BOTTOM, ChildrenSides.TOP_OR_LEFT, ChildNodesAlignment.AUTO),
    TOPTOBOTTOM_BOTHSIDES_AUTO(LayoutOrientation.TOP_TO_BOTTOM, ChildrenSides.BOTH_SIDES, ChildNodesAlignment.AUTO),
    TOPTOBOTTOM_RIGHT_AUTO(LayoutOrientation.TOP_TO_BOTTOM, ChildrenSides.BOTTOM_OR_RIGHT, ChildNodesAlignment.AUTO),

    LEFTTORIGHT_TOP_AUTO(LayoutOrientation.LEFT_TO_RIGHT, ChildrenSides.TOP_OR_LEFT, ChildNodesAlignment.AUTO),
    LEFTTORIGHT_BOTHSIDES_AUTO(LayoutOrientation.LEFT_TO_RIGHT, ChildrenSides.BOTH_SIDES, ChildNodesAlignment.AUTO),
    LEFTTORIGHT_BOTTOM_AUTO(LayoutOrientation.LEFT_TO_RIGHT, ChildrenSides.BOTTOM_OR_RIGHT, ChildNodesAlignment.AUTO),

    AUTO_AFTERPARENT(LayoutOrientation.AUTO, ChildrenSides.AUTO, ChildNodesAlignment.AFTER_PARENT),
    AUTO_FIRST(LayoutOrientation.AUTO, ChildrenSides.AUTO, ChildNodesAlignment.FIRST_CHILD_BY_PARENT),
    AUTO_CENTERED(LayoutOrientation.AUTO, ChildrenSides.AUTO, ChildNodesAlignment.BY_CENTER),
    AUTO_LAST(LayoutOrientation.AUTO, ChildrenSides.AUTO, ChildNodesAlignment.LAST_CHILD_BY_PARENT),
    AUTO_BEFOREPARENT(LayoutOrientation.AUTO, ChildrenSides.AUTO, ChildNodesAlignment.BEFORE_PARENT),

    AUTO(LayoutOrientation.AUTO, ChildrenSides.AUTO, ChildNodesAlignment.AUTO),
    ;

    static public Optional<ChildNodesLayout> using(ChildrenSides childrenSides, ChildNodesAlignment childNodesAlignment, LayoutOrientation layoutOrientation) {
        return Stream.of(values()).filter(x ->
        x.childrenSides == childrenSides
        && x.childNodesAlignment == childNodesAlignment
        && x.layoutOrientation == layoutOrientation).findAny();
    }

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
