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
package org.freeplane.features.mindmapmode.icon;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.IIconInformation;
import org.freeplane.core.model.MindIcon;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.FpStringUtils;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.MultipleNodeAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.features.common.icon.IconController;

class IconAction extends MultipleNodeAction implements IIconInformation {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	final private MindIcon icon;

	public IconAction(final Controller controller, final MindIcon _icon) {
		super("IconAction." + _icon.getName(), controller, getLocalName(_icon), _icon.getIcon());
		putValue(Action.SHORT_DESCRIPTION, _icon.getDescription());
		icon = _icon;
	}

	private static String getLocalName(final MindIcon _icon) {
	    final String name = _icon.getName();
		final String localName = ResourceBundles.getText("icon_"+ name, null);
		if(localName != null){
			return localName;
		}
		return FpStringUtils.formatText("user_icon", name);
    }

	@Override
	public void actionPerformed(final ActionEvent e, final NodeModel node) {
		((MIconController) IconController.getController(getModeController())).addIcon(node, icon, MindIcon.LAST);
	}

	public String getDescription() {
		return icon.getDescription();
	}

	public ImageIcon getIcon() {
		return icon.getIcon();
	}

	public KeyStroke getKeyStroke() {
		final String keystrokeResourceName = icon.getShortcutKey();
		final String keyStrokeDescription = ResourceController.getResourceController().getAdjustableProperty(
		    keystrokeResourceName);
		return UITools.getKeyStroke(keyStrokeDescription);
	}

	public MindIcon getMindIcon() {
		return icon;
	}

	public String getShortcutKey() {
		return icon.getShortcutKey();
	}
}
