/*
 * Created on 4 Apr 2024
 *
 * author dimitry
 */
package org.freeplane.features.icon.mindmapmode;

import java.awt.event.ActionEvent;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.features.mode.Controller;

public class EditTagsAction extends AFreeplaneAction {

    private static final long serialVersionUID = 1L;
    private final MIconController iconController;

    public EditTagsAction(MIconController iconController) {
        super("EditTagsAction");
        this.iconController = iconController;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        iconController.editTags(Controller.getCurrentController().getSelection().getSelected());
    }

}
