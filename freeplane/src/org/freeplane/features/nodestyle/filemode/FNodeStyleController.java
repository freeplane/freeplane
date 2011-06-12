/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Dimitry Polivaev
 *
 *  This file author is Dimitry Polivaev
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
package org.freeplane.features.nodestyle.filemode;

import java.awt.Color;

import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.IPropertyHandler;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.nodestyle.NodeStyleController;

/**
 * @author Dimitry Polivaev
 * 02.01.2009
 */
class FNodeStyleController extends NodeStyleController {
	public FNodeStyleController(final ModeController modeController) {
		super(modeController);
		addColorGetter(IPropertyHandler.DEFAULT - 1, new IPropertyHandler<Color, NodeModel>() {
			public Color getProperty(final NodeModel node, final Color currentValue) {
				return node.isLeaf() ? Color.BLACK : Color.GRAY;
			}
		});
	}
}
