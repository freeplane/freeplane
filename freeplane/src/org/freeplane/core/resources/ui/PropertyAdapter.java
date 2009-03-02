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
package org.freeplane.core.resources.ui;

import org.freeplane.core.model.Triple;
import org.freeplane.core.resources.ResourceController;

/**
 * @author Dimitry Polivaev
 * 26.12.2008
 */
// TODO ARCH rladstaetter 28.02.2009 a property is a property. finito. there is conceptually no need for an adapter 
@Deprecated
public class PropertyAdapter extends Triple<String,String,String>{

	@Deprecated
	public PropertyAdapter(final String name) {
		this(name, "OptionPanel." + name, "OptionPanel." + name + ".tooltip");
		if (ResourceController.getResourceController().getText(null, null) == null) {
			setC(null);
		}
	}

	public PropertyAdapter(final String name, final String label, final String description) {
		super(name,label,description);
		assert name != null;
	}

	public String getDescription() {
		return getC();
	}

	public String getLabel() {
		return getB();
	}

	public String getName() {
		return getA();
	}
}
