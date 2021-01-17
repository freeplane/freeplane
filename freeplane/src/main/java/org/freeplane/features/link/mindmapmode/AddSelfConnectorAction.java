package org.freeplane.features.link.mindmapmode;

import java.awt.event.ActionEvent;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.EnabledAction;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.link.NodeLinks;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.styles.MapStyleModel;

@EnabledAction(checkOnNodeChange = true)
public class AddSelfConnectorAction extends AFreeplaneAction {
    private static final long serialVersionUID = 1L;

    public AddSelfConnectorAction() {
        super("AddSelfConnectorAction");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        MLinkController linkController = (MLinkController) LinkController.getController();
        NodeModel node = Controller.getCurrentController().getSelection().getSelected();
        linkController.addConnector(node, node);
    }

    @Override
    protected void setEnabled() {
        NodeModel node = Controller.getCurrentController().getSelection().getSelected();
        boolean canAddSelfConnector = (
                ! MapStyleModel.isStyleNode(node) || MapStyleModel.isUserStyleNode(node)
                ) && ! NodeLinks.getSelfConnector(node).isPresent();
        super.setEnabled(canAddSelfConnector);
    }
    

}
