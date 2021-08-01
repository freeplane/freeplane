package org.freeplane.features.styles.mindmapmode;

import java.awt.event.ActionEvent;

import javax.swing.Action;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.Controller;

class ManageAssociatedMindMapsAction extends AFreeplaneAction{
    private static final long serialVersionUID = 1L;
    public ManageAssociatedMindMapsAction() {
        super("ManageAssociatedMindMapsAction");
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        MapModel map = Controller.getCurrentController().getMap();
        String title = getValue(Action.NAME).toString();
        new ManageAssociatedMindMapsDialog(title, map).show();
    }

}
