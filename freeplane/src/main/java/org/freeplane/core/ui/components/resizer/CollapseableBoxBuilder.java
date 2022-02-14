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
package org.freeplane.core.ui.components.resizer;

import java.awt.Component;

import javax.swing.Box;

import org.freeplane.core.ui.components.resizer.JResizer.Direction;

/**
 * @author Dimitry Polivaev
 * 01.02.2014
 */
public class CollapseableBoxBuilder {
	private String propertyNameBase;
	public CollapseableBoxBuilder(String propertyNameBase){
		this.propertyNameBase = propertyNameBase;

	}
	public Box createBox(final Component component, final Direction direction) {
		return new OneTouchCollapseResizer(direction, component, propertyNameBase).getParentBox();
    }
}
