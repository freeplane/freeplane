/*
 * Created on 23 May 2023
 *
 * author dimitry
 */
package org.freeplane.features.layout.mindmapmode;

import javax.swing.Icon;
import javax.swing.JToggleButton;

import org.freeplane.api.ChildNodesLayout;
import org.freeplane.core.resources.components.ButtonPanelProperty;
import org.freeplane.core.ui.components.MultipleImageIcon;
import org.freeplane.core.ui.svgicons.FixSizeIconWrapper;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.layout.LayoutController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.view.swing.map.MapViewController;
import org.freeplane.view.swing.map.NodeView;

public class ChildNodesLayoutButtonPanelProperty extends ButtonPanelProperty {
    static final String CHILD_NODES_LAYOUTS = "children_nodes_layouts";
    public ChildNodesLayoutButtonPanelProperty() {
        super(CHILD_NODES_LAYOUTS,  LayoutSelectorPanelFactory.createLayoutSelectorPanel());
        Icon icon = startButton.getIcon();
        int iconWidth = icon.getIconWidth() * 2 + LayoutSelectorPanelFactory.RIGHT_ARROW_ICON.getIconWidth();
        int iconHeight = icon.getIconHeight();
        startButton.setIcon(new FixSizeIconWrapper(iconWidth, iconHeight));

    }

    public void setValue(ChildNodesLayout value, ChildNodesLayout viewValue) {
        if(value == viewValue || viewValue == null) {
            super.setValue(value.name());
            return;
        }
        buttons.setValue(value.name());
        MultipleImageIcon multipleImageIcon = new MultipleImageIcon();
        JToggleButton selectedButton = buttons.getSelectedButton();
        JToggleButton viewButton = buttons.getButton(viewValue.name());
        multipleImageIcon.addIcon(selectedButton.getIcon());
        multipleImageIcon.addIcon(LayoutSelectorPanelFactory.RIGHT_ARROW_ICON);
        multipleImageIcon.addIcon(viewButton.getIcon());
        startButton.setIcon(multipleImageIcon);
        final String labelKey = getLabel();
        final String buttonName = TextUtils.getRawText(labelKey);
        startButton.setToolTipText(
                buttonName + ": " +
                selectedButton.getToolTipText() + ": " + viewButton.getToolTipText());
    }

    public void setStyleOnExternalChange(NodeModel node) {
        if(node == null) {
            setEnabled(false);
        }
        else {
            setEnabled(true);
            ChildNodesLayout displayedValue = displayedValue(node);
            NodeView nodeView = ((MapViewController)Controller.getCurrentController().getMapViewManager()).getMapView().getNodeView(node);
            if(nodeView == null)
                setValue(displayedValue, displayedValue);
            else
                setValue(displayedValue, nodeView.recalculateChildNodesLayout());
        }
    }

    private ChildNodesLayout displayedValue(NodeModel node) {
        final LayoutController styleController = LayoutController.getController();
        final ChildNodesLayout displayedValue = styleController.getChildNodesLayout(node);
        return displayedValue;
    }


}