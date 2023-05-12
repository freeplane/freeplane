/*
 * Created on 12 May 2023
 *
 * author dimitry
 */
package org.freeplane.features.layout.mindmapmode;

import java.awt.event.ActionEvent;
import java.util.Collection;

import org.freeplane.api.ChildNodesLayout;
import org.freeplane.core.resources.components.ButtonSelectorPanel;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.features.layout.LayoutController;
import org.freeplane.features.map.IMapSelection;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;

public class SelectNodeChildrenLayoutAction extends AFreeplaneAction {

    private static final long serialVersionUID = 1L;

    public SelectNodeChildrenLayoutAction() {
        super("SelectNodeChildrenLayoutAction");
    }

    @Override
    public String getTextKey() {
        return "OptionPanel.children_nodes_layouts";
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        MLayoutController layoutController = (MLayoutController) Controller.getCurrentModeController().getExtension(LayoutController.class);
        NodeModel selected = Controller.getCurrentController().getSelection().getSelected();
        ChildNodesLayout childNodesLayout = layoutController.getChildNodesLayout(selected);
        ButtonSelectorPanel layoutSelectorPanel = LayoutSelectorPanelFactory.createLayoutSelectorPanel();
        layoutSelectorPanel.setValue(childNodesLayout.name());
        layoutSelectorPanel.showButtonDialog(Controller.getCurrentController().getMapViewManager().getSelectedComponent(),
                () -> applySelectedLayout(layoutSelectorPanel));

    }

    private void applySelectedLayout(ButtonSelectorPanel layoutSelectorPanel) {
        final IMapSelection selection = Controller.getCurrentController().getSelection();
        final Collection<NodeModel> nodes = selection.getSelection();
        final MLayoutController styleController = (MLayoutController) Controller
                .getCurrentModeController().getExtension(LayoutController.class);
        String selectedValue = layoutSelectorPanel.getValue();
        ChildNodesLayout layout = selectedValue != null ? ChildNodesLayout.valueOf(selectedValue) : null;
               for (final NodeModel node : nodes)
           styleController.setChildNodesLayout(node, layout);
    }

}
