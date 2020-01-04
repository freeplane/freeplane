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

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.LengthUnits;
import org.freeplane.core.util.Quantity;
import org.freeplane.features.icon.UIIcon;

/**
 *
 * Factory for swing icons used in the GUI.
 *
 * @author Tamas Eppel
 *
 */
public interface IconFactory {

	String USE_SVG_ICONS = "use_svg_icons";

	Quantity<LengthUnits> DEFAULT_UI_ICON_HEIGHT = ResourceController.getResourceController()
		    .getLengthQuantityProperty("toolbar_icon_height");
  
	IconFactory FACTORY = ! GraphicsEnvironment.isHeadless() ? GraphicIconFactory.FACTORY : HeadlessIconFactory.FACTORY;
	//IconFactory FACTORY = FakeIconFactory.FACTORY;
	
	static IconFactory getInstance() {
		return FACTORY;
	}
	
	public static String[] getAlternativePaths(final String resourcePath) {
		final String pngSuffix = ".png";
		if (isSvgIconsEnabled() && resourcePath.endsWith(pngSuffix)) {
			final String svgPath = resourcePath.substring(0, resourcePath.length() - pngSuffix.length()) + ".svg";
			return new String[] { svgPath, resourcePath };
		}
		else
			return new String[] { resourcePath };
	}

	static public boolean isSvgIconsEnabled() {
		return ResourceController.getResourceController().getBooleanProperty(IconFactory.USE_SVG_ICONS);
	}
	
	boolean canScaleIcon(Icon icon);
	Icon getScaledIcon(Icon icon, Quantity<LengthUnits> quantity);
	Icon getIcon(URL imageURL);
	Icon getIcon(UIIcon icon);
	Icon getIcon(UIIcon uiIcon, Quantity<LengthUnits> iconHeight);
	Icon getIcon(URL url, Quantity<LengthUnits> defaultUiIconHeight);
}