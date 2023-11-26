package org.freeplane.core.ui.commandtonode;

import java.awt.event.ActionEvent;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.AccelerateableAction;

public class NewNodeLinkedToMenuItemOnNextClickAction extends AFreeplaneAction {
    public NewNodeLinkedToMenuItemOnNextClickAction() {
        super("NewNodeLinkedToMenuItemOnNextClickAction");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        AccelerateableAction.newNodeLinkedToMenuItemOnNextClick();
    }
}
