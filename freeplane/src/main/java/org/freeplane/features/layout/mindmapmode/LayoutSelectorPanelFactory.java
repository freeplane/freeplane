/*
 * Created on 28 Jan 2023
 *
 * author dimitry
 */
package org.freeplane.features.layout.mindmapmode;

import static org.freeplane.api.ChildNodesLayout.AUTO;
import static org.freeplane.api.ChildNodesLayout.AUTO_AFTERPARENT;
import static org.freeplane.api.ChildNodesLayout.TOPTOBOTTOM_BOTHSIDES_RIGHT;
import static org.freeplane.api.ChildNodesLayout.TOPTOBOTTOM_BOTTOM_RIGHT;
import static org.freeplane.api.ChildNodesLayout.TOPTOBOTTOM_LEFT_AUTO;
import static org.freeplane.api.ChildNodesLayout.TOPTOBOTTOM_LEFT_BOTTOM;
import static org.freeplane.api.ChildNodesLayout.TOPTOBOTTOM_LEFT_CENTERED;
import static org.freeplane.api.ChildNodesLayout.TOPTOBOTTOM_LEFT_FIRST;
import static org.freeplane.api.ChildNodesLayout.TOPTOBOTTOM_LEFT_LAST;
import static org.freeplane.api.ChildNodesLayout.TOPTOBOTTOM_LEFT_TOP;
import static org.freeplane.api.ChildNodesLayout.TOPTOBOTTOM_TOP_RIGHT;

import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Vector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.Icon;

import org.freeplane.api.ChildNodesAlignment;
import org.freeplane.api.ChildNodesLayout;
import org.freeplane.api.ChildrenSides;
import org.freeplane.api.LayoutOrientation;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.resources.components.ButtonSelectorPanel;
import org.freeplane.core.resources.components.ButtonSelectorPanel.ButtonIcon;
import org.freeplane.core.resources.components.ButtonSelectorPanel.ComponentBefore;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.icon.factory.IconFactory;

public class LayoutSelectorPanelFactory {
    private static final ChildNodesLayout[] LAYOUTS =
            Arrays.asList(ChildNodesLayout.values()).stream().skip(1).toArray(ChildNodesLayout[]::new);

    private static final EnumSet<ChildNodesLayout> LINE_BREAK_LAYOUTS =
            EnumSet.of(
                    TOPTOBOTTOM_TOP_RIGHT,
                    TOPTOBOTTOM_BOTHSIDES_RIGHT,
                    TOPTOBOTTOM_BOTTOM_RIGHT,
                    TOPTOBOTTOM_LEFT_TOP,
                    TOPTOBOTTOM_LEFT_LAST,
                    TOPTOBOTTOM_LEFT_CENTERED,
                    TOPTOBOTTOM_LEFT_FIRST,
                    TOPTOBOTTOM_LEFT_BOTTOM,
                    TOPTOBOTTOM_LEFT_AUTO,
                    AUTO_AFTERPARENT,
                    AUTO
                    );

    public final static Icon RIGHT_ARROW_ICON = IconFactory.getInstance()
            .getIcon(ResourceController.getResourceController().getIconResource("/images/layouts/right_arrow.svg?useAccentColor=true"),
                    IconFactory.DEFAULT_UI_ICON_HEIGTH.zoomBy(2));

    static public ButtonSelectorPanel createLayoutSelectorPanel() {
        final Vector<ButtonIcon> icons = new Vector<>(LayoutSelectorPanelFactory.LAYOUTS.length);
        ResourceController resourceController = ResourceController.getResourceController();
        for (int i = 0; i < LayoutSelectorPanelFactory.LAYOUTS.length; i++) {
            ChildNodesLayout layout = LayoutSelectorPanelFactory.LAYOUTS[i];
            String name = layout.name().toLowerCase(Locale.ENGLISH);
            URL url = resourceController.getIconResource("/images/layouts/" + name + ".svg?useAccentColor=true");
            ComponentBefore componentBefore = LINE_BREAK_LAYOUTS.contains(layout)
                    ? ComponentBefore.SEPARATOR
                    : ComponentBefore.NOTHING;
            icons.add(new ButtonIcon(
                    IconFactory.getInstance().getIcon(url, IconFactory.DEFAULT_UI_ICON_HEIGTH.zoomBy(2)),
                    description(layout), componentBefore));
        }
        Collection<String> alignmentNames = Stream.of(LayoutSelectorPanelFactory.LAYOUTS).map(Enum::name).collect(Collectors.toList());
        ButtonSelectorPanel buttons = new ButtonSelectorPanel(alignmentNames, icons);
        return buttons;
    }

    static private String description(ChildNodesLayout layout) {
        if(layout == ChildNodesLayout.AUTO)
            return TextUtils.getRawText(layout.name());
        String childNodesAlignmentText = TextUtils.getRawText(layout.childNodesAlignment().name());
        if(layout.layoutOrientation() == LayoutOrientation.AUTO
                && layout.childrenSides() == ChildrenSides.AUTO)
            return childNodesAlignmentText;
        String childrenSidesText = TextUtils.getRawText(layout.childrenSides().labelKey(layout.layoutOrientation()));
        String layoutOrientationText = TextUtils.getRawText(layout.layoutOrientation().name());
        if(layout.childNodesAlignment() == ChildNodesAlignment.AUTO)
            return layoutOrientationText + ", " + childrenSidesText;
        return layoutOrientationText + ", " + childrenSidesText  + ", " + childNodesAlignmentText;
    }
}