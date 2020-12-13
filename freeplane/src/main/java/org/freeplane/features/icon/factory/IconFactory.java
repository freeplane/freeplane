/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2009 Tamas Eppel
 *
 *  This file author is Tamas Eppel
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
package org.freeplane.features.icon.factory;

import java.awt.GraphicsEnvironment;
import java.net.URL;

import javax.swing.Icon;

import org.freeplane.api.LengthUnit;
import org.freeplane.api.Quantity;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.features.icon.UIIcon;

/**
 *
 * Factory for swing icons used in the GUI.
 *
 * @author Tamas Eppel
 *
 */
public interface IconFactory {

	Quantity<LengthUnit> DEFAULT_UI_ICON_HEIGTH = ResourceController.getResourceController()
		    .getLengthQuantityProperty("toolbar_icon_height");
  
	IconFactory FACTORY = ! GraphicsEnvironment.isHeadless() ? GraphicIconFactory.FACTORY : HeadlessIconFactory.FACTORY;
	
	static IconFactory getInstance() {
		return FACTORY;
	}
	
	boolean canScaleIcon(Icon icon);
	Icon getScaledIcon(Icon icon, Quantity<LengthUnit> quantity);
	Icon getIcon(URL imageURL);
	Icon getIcon(UIIcon icon);
	Icon getIcon(UIIcon uiIcon, Quantity<LengthUnit> iconHeight);
	Icon getIcon(URL url, Quantity<LengthUnit> defaultUiIconHeight);
}