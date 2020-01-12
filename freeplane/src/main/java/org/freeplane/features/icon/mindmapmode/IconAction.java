/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is modified by Dimitry Polivaev in 2008.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.freeplane.features.icon.mindmapmode;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.Icon;

import org.freeplane.core.ui.AMultipleNodeAction;
import org.freeplane.core.ui.menubuilders.generic.UserRoleConstraint;
import org.freeplane.core.ui.svgicons.FixedSizeUIIcon;
import org.freeplane.features.icon.IconController;
import org.freeplane.features.icon.IconDescription;
import org.freeplane.features.icon.MindIcon;
import org.freeplane.features.icon.factory.IconFactory;
import org.freeplane.features.icon.factory.IconStoreFactory;
import org.freeplane.features.map.NodeModel;

public class IconAction extends AMultipleNodeAction implements IconDescription {

    private static final long serialVersionUID = 1L;
    final private MindIcon mindIcon;

    public IconAction( final MindIcon mindIcon) {
        super("IconAction." + mindIcon.getName(), mindIcon.getTranslatedDescription(), null);
        this.mindIcon = mindIcon;
        setIcon(FixedSizeUIIcon.withHeigth(//
                mindIcon.getUrl(), IconFactory.DEFAULT_UI_ICON_HEIGTH.toBaseUnitsRounded(), mindIcon.hasStandardSize()));
        putValue(Action.SHORT_DESCRIPTION, getTranslatedDescription());
        addConstraint(UserRoleConstraint.EDITOR);
    }

    private MindIcon replaceByUserDefinedIcon() {
        return IconStoreFactory.ICON_STORE.getMindIcon(mindIcon.getName());
    }

    @Override
    public void actionPerformed(final ActionEvent e, final NodeModel node) {
        ((MIconController) IconController.getController()).addIconByUserAction(node, this);
    }
    
    public String getDescriptionTranslationKey() {
        return mindIcon.getDescriptionTranslationKey();
    }

    @Override
    public String getTextKey() {
        return getDescriptionTranslationKey();
    }

    public String getTranslatedDescription() {
        return mindIcon.getTranslatedDescription();
    }

    public Icon getIcon() {
        return IconFactory.getInstance().getIcon(replaceByUserDefinedIcon());
    }
    
    public Icon getActionIcon() {
        return (Icon) getValue(Action.SMALL_ICON);
    }
    
    public MindIcon getMindIcon() {
        return replaceByUserDefinedIcon();
    }

    public String getShortcutKey() {
        return mindIcon.getShortcutKey();
    }
}
