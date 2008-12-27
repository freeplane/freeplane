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
package org.freeplane.controller;

import java.util.HashMap;
import javax.swing.Action;

/**
 * @author Dimitry Polivaev
 * 10.12.2008
 */
public class ActionController {
	final private HashMap<Object, Action> actions;

	public ActionController() {
		actions = new HashMap<Object, Action>();
	}

	public void addAction(final Object key, final Action value) {
		assert key != null;
		assert value != null;
		final Action oldAction = actions.put(key, value);
		assert oldAction == null;
	}

	public Action getAction(final String key) {
		return actions.get(key);
	}

	public Action removeAction(final String key) {
		return actions.remove(key);
	}
}
