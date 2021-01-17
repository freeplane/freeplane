/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
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
package org.freeplane.core.resources.components;

import com.jgoodies.forms.builder.DefaultFormBuilder;

public class NextColumnProperty implements IPropertyControl {
	private int n;

	public NextColumnProperty() {
		this(1);
	}

	public NextColumnProperty(int n) {
	    this.n=n;
    }

	public String getTooltip() {
		return null;
	}

	public String getName() {
		return null;
	}

	public void appendToForm(final DefaultFormBuilder builder) {
		for(int i = 0; i < n; i++){
			builder.append("");
		}
	}

	public void setEnabled(final boolean pEnabled) {
	}
}
