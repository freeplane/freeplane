/*
 * Created on 4 Apr 2024
 *
 * author dimitry
 */
package org.freeplane.features.icon.mindmapmode;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.RootPaneContainer;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.UITools;

public class EditTagCategoriesAction extends AFreeplaneAction {

    private static final long serialVersionUID = 1L;
    private final TagCategories tagCategories;

    public EditTagCategoriesAction(TagCategories tagCategories) {
        super("EditTagCategoriesAction");
        this.tagCategories = tagCategories;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final RootPaneContainer frame = (RootPaneContainer) UITools.getCurrentRootComponent();
        TagCategoryEditor tagCategoryEditor = new TagCategoryEditor(frame, tagCategories);
        tagCategoryEditor.show();
    }

}
