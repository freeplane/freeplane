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
import javax.swing.Icon;
import javax.swing.KeyStroke;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AMultipleNodeAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.features.common.icon.IIconInformation;
import org.freeplane.features.common.icon.IconController;
import org.freeplane.features.common.icon.MindIcon;
import org.freeplane.features.common.icon.factory.ImageIconFactory;
import org.freeplane.features.common.map.NodeModel;

class IconAction extends AMultipleNodeAction implements IIconInformation {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	final private MindIcon icon;

	public IconAction( final MindIcon _icon) {
		super("IconAction." + _icon.getName(), _icon.getDescription(), ImageIconFactory.getInstance()
		    .getImageIcon(_icon));
		putValue(Action.SHORT_DESCRIPTION, _icon.getDescription());
		icon = _icon;
	}

	@Override
	public void actionPerformed(final ActionEvent e, final NodeModel node) {
		((MIconController) IconController.getController()).addIcon(node, icon);
	}

	public String getDescription() {
		return icon.getDescription();
	}

	public Icon getIcon() {
		return ImageIconFactory.getInstance().getImageIcon(icon);
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
