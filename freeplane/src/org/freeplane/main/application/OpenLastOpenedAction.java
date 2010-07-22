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
package org.freeplane.main.application;

import java.awt.event.ActionEvent;

import org.freeplane.core.ui.AFreeplaneAction;

class OpenLastOpenedAction extends AFreeplaneAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	final private LastOpenedList list;

	public OpenLastOpenedAction(final int i, final LastOpenedList list) {
		super("OpenLastOpenedAction_" + i, null, null);
		this.list = list;
	}

	public void actionPerformed(final ActionEvent e) {
		final String restoreable = e.getActionCommand();
		list.safeOpen(restoreable);
	}

	@Override
	public void afterMapChange(final Object newMap) {
	}
}
