/*
 * Created on 15 Apr 2023
 *
 * author dimitry
 */
package org.freeplane.view.swing.map;

import java.awt.Component;
import java.awt.Container;
import java.awt.Rectangle;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.freeplane.view.swing.map.MapView.SelectionDirection;

class ComponentWithDistances {
    private final Component toComponent;
    private final int minDistance;
    private final int maxDistance;
    private final boolean isContainedInAngle;
    private final int translationX;
    private final int translationY;
    private final Rectangle from;
    private final SelectionDirection direction;

    ComponentWithDistances(Rectangle from, Component to, MapView.SelectionDirection direction) {
        this(from, to, direction, 0, 0);
    }

    private ComponentWithDistances(Rectangle from, Component to, MapView.SelectionDirection direction, int translationX, int translationY) {
        this.toComponent = to;
        this.from = from;
        this.direction = direction;
        this.translationX = translationX;
        this.translationY = translationY;
         Rectangle toRectangle = new Rectangle(
                translationX,
                translationY,
                toComponent.getWidth(),
                toComponent.getHeight()
                );
       if(toComponent instanceof NodeView) {
           int spaceAround = ((NodeView)toComponent).getSpaceAround();
           toRectangle.x += spaceAround;
           toRectangle.y += spaceAround;
           toRectangle.width -= 2*spaceAround;
           toRectangle.height -= spaceAround;
           this.isContainedInAngle = direction.isRectangleOverlappingOrInAngleRange(from, toRectangle);
       }
       else
           this.isContainedInAngle = direction.isRectangleInAngleRange(from, toRectangle);
       if(isContainedInAngle && ! from.intersects(toRectangle)) {
        this.minDistance = direction.minDistance(from, toRectangle);
        this.maxDistance = direction.maxDistance(from, toRectangle);
       }
       else {
           this.minDistance = 0;
           this.maxDistance = 0;
       }
    }


    public List<ComponentWithDistances> components(){
        return (toComponent instanceof Container) ?
                Arrays.stream(((Container)toComponent).getComponents())
                .map(c -> new ComponentWithDistances(from, c, direction, c.getX() + translationX, c.getY() + translationY))
                .filter(ComponentWithDistances::isContainedInAngle)
                .collect(Collectors.toList())
                : Collections.emptyList();
    }

    public Component getComponent() {
        return toComponent;
    }

    public int getMinDistance() {
        return minDistance;
    }

    public int getMaxDistance() {
        return maxDistance;
    }

    @Override
    public String toString() {
        return "ComponentWithDistances [from=" + from + ", direction=" + direction
                + ", translationX=" + translationX
                + ", translationY=" + translationY + ", minDistance=" + minDistance
                + ", maxDistance=" + maxDistance + ", isContainedInAngle=" + isContainedInAngle + ", to=" + toComponent + "]";
    }

    public boolean isContainedInAngle() {
        return isContainedInAngle;
    }


}
