/*
 * Created on 28 Jan 2023
 *
 * author dimitry
 */
package org.freeplane.features.layout.mindmapmode;

import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
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

class LayoutSelectorPanelFactory {
    private static final ChildNodesLayout[] LAYOUTS =
            Arrays.asList(ChildNodesLayout.values()).stream().skip(1).toArray(ChildNodesLayout[]::new);

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
            ComponentBefore componentBefore;
            if(layout.layoutOrientation() == LayoutOrientation.TOP_TO_BOTTOM) {
                if(layout.childrenSides() == ChildrenSides.TOP_OR_LEFT)
                    componentBefore = ComponentBefore.SEPARATOR;
                else
                    componentBefore = ComponentBefore.NOTHING;
            }
            else if(layout.layoutOrientation() == LayoutOrientation.LEFT_TO_RIGHT) {
                if(layout.childNodesAlignment() == ChildNodesAlignment.BEFORE_PARENT)
                    componentBefore = ComponentBefore.SEPARATOR;
                else if(layout.childNodesAlignment() == ChildNodesAlignment.AUTO) {
                    if(layout.childrenSides() == ChildrenSides.TOP_OR_LEFT)
                        componentBefore = ComponentBefore.SEPARATOR;
                    else
                        componentBefore = ComponentBefore.NOTHING;
                }
                else
                    componentBefore = ComponentBefore.NOTHING;
            }
            else if(layout.childNodesAlignment() == ChildNodesAlignment.AFTER_PARENT
                    || layout.childNodesAlignment() == ChildNodesAlignment.AUTO)
                componentBefore = ComponentBefore.SEPARATOR;
            else
                componentBefore = ComponentBefore.NOTHING;
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