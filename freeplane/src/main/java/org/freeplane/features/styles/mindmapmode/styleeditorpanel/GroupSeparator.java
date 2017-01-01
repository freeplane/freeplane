/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2016 jberry
 *
 *  This file author is jberry
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
package org.freeplane.features.styles.mindmapmode.styleeditorpanel;

import java.util.List;

import org.freeplane.core.resources.components.IPropertyControl;
import org.freeplane.core.resources.components.SeparatorProperty;
import org.freeplane.features.map.NodeModel;

/**
 * @author Joe Berry
 * Dec 13, 2016
 */
public class GroupSeparator implements ControlGroup {
	final String labelKey;

	public GroupSeparator(String labelKey) {
		this.labelKey = labelKey;
	}

	@Override
	public void setStyle(NodeModel node) {
		// intentionally left blank
	}

	@Override
	public void addControlGroup(List<IPropertyControl> controls) {
		controls.add(new SeparatorProperty(labelKey));
	}
}