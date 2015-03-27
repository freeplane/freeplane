/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2014 Dimitry
 *
 *  This file author is Dimitry
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
package org.freeplane.features.map;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.features.icon.IStateIconProvider;
import org.freeplane.features.icon.IconController;
import org.freeplane.features.icon.UIIcon;
import org.freeplane.features.icon.factory.IconStoreFactory;

/**
 * @author Dimitry Polivaev
 * 26.04.2014
 */
public class CloneStateIconSupplier {
	public static final String CLONEROOT_ICON_FILENAME = ResourceController.getResourceController().getProperty("cloneroot_icon");
	public static final String CLONE_ICON_FILENAME = ResourceController.getResourceController().getProperty("clone_icon");
	public static UIIcon CLONE_ICON = IconStoreFactory.create().getUIIcon(CLONE_ICON_FILENAME);
	public static UIIcon CLONEROOT_ICON= IconStoreFactory.create().getUIIcon(CLONEROOT_ICON_FILENAME);
	public void registerStateIconProvider() {
	    IconController.getController().addStateIconProvider(new IStateIconProvider() {

			public UIIcon getStateIcon(NodeModel node) {
				if (node.clones().size() <= 1) {
					return null;
				}
				final NodeModel parentNode = node.getParentNode();
				if(parentNode != null && parentNode.clones().size() == node.clones().size())
	                return CLONE_ICON;
				else
					return CLONEROOT_ICON;
			}
		});
    }

}
