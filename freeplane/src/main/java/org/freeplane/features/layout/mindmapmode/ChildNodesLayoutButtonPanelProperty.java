/*
 * Created on 23 May 2023
 *
 * author dimitry
 */
package org.freeplane.features.layout.mindmapmode;

import javax.swing.JToggleButton;

import org.freeplane.api.ChildNodesLayout;
import org.freeplane.core.resources.components.ButtonPanelProperty;
import org.freeplane.core.ui.components.MultipleImageIcon;

public class ChildNodesLayoutButtonPanelProperty extends ButtonPanelProperty {
    public ChildNodesLayoutButtonPanelProperty(String name) {
        super(name,  LayoutSelectorPanelFactory.createLayoutSelectorPanel());
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
        startButton.setToolTipText(selectedButton.getToolTipText() + ": " + viewButton.getToolTipText());
    }
}