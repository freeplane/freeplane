/*
 * Created on 4 Apr 2024
 *
 * author dimitry
 */
package org.freeplane.features.icon.mindmapmode;

import java.awt.event.ActionEvent;

import javax.swing.RootPaneContainer;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.features.icon.IconController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.Controller;

public class EditTagCategoriesAction extends AFreeplaneAction {

    private static final long serialVersionUID = 1L;

    public EditTagCategoriesAction() {
        super("EditTagCategoriesAction");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final MapModel map = Controller.getCurrentController().getMap();
        final RootPaneContainer frame = (RootPaneContainer) UITools.getCurrentRootComponent();
        TagCategoryEditor tagCategoryEditor = new TagCategoryEditor(frame, (MIconController) IconController.getController(), map);
        tagCategoryEditor.show();
    }

}
