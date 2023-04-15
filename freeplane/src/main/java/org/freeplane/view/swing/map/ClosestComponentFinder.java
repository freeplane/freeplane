package org.freeplane.view.swing.map;

import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import javax.swing.JComponent;

import org.freeplane.core.ui.components.UITools;

class ClosestComponentFinder {
    public static Optional<Component> findClosestComponent(Component reference, JComponent ancestor, MapView.SelectionDirection direction) {
        Point point = new Point(0, 0);
        UITools.convertPointToAncestor(reference, point, ancestor);
        Rectangle from = new Rectangle(point.x, point.y, reference.getWidth(), reference.getHeight());
        ComponentWithDistances ancestorWithDistances = new ComponentWithDistances(from, ancestor, direction);

        Stream<ComponentWithDistances> filteredComponents = filterCloseComponents(from,
                Collections.singletonList(ancestorWithDistances));

        ArrayList<ComponentWithDistances> log = new ArrayList<ComponentWithDistances>();
        Optional<Component> result = filteredComponents
                .peek(log::add)
                .filter(c -> {
                    Component component = c.getComponent();
                    return component != reference
                        && component.isVisible()
                        && component.getWidth() > 0
                        && component.getHeight() > 0;
                })
                .min((c1, c2) ->
                Integer.compare(c1.getMinDistance(), c2.getMinDistance())
                )
                .map(ComponentWithDistances::getComponent);
        return result;
    }


    private static Stream<ComponentWithDistances> filterCloseComponents(Rectangle rectangle,
            List<ComponentWithDistances> componentDistances) {
        Stream<ComponentWithDistances> filteredComponents = componentDistances.stream()
                .filter(cwd -> hasNoOtherComponentWithSmallerMaxDistance(cwd, componentDistances))
                .flatMap(cwd -> (cwd.getComponent() instanceof NodeView)
                        ? filterCloseComponents(rectangle, cwd.components())
                        : Stream.of(cwd));

        return filteredComponents;
    }


    private static boolean hasNoOtherComponentWithSmallerMaxDistance(ComponentWithDistances componentWithDistances, List<ComponentWithDistances> components) {
        int componentMinDistance = componentWithDistances.getMinDistance();

        return components.stream()
                .noneMatch(otherCwd -> {
                    if (otherCwd == componentWithDistances) {
                        return false;
                    }

                    int otherMaxDistance = otherCwd.getMaxDistance();
                    boolean result = otherMaxDistance > 0 && otherMaxDistance < componentMinDistance;
                    return result;
                });
    }
}
