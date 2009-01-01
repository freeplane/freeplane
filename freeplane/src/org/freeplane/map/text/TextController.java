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
package org.freeplane.map.text;

import org.freeplane.core.io.ReadManager;
import org.freeplane.core.map.MapController;
import org.freeplane.core.mode.ModeController;

/**
 * @author Dimitry Polivaev
 */
public class TextController {
	final private ModeController modeController;

	public TextController(final ModeController modeController) {
		super();
		this.modeController = modeController;
		createActions(modeController);
		final MapController mapController = modeController.getMapController();
		final ReadManager readManager = mapController.getReadManager();
		final NodeTextBuilder textBuilder = new NodeTextBuilder();
		textBuilder.registerBy(readManager);
	}

	/**
	 * @param modeController
	 */
	private void createActions(final ModeController modeController) {
		final FindAction find = new FindAction();
		modeController.addAction("find", find);
		modeController.addAction("findNext", new FindNextAction(modeController, find));
	}

	public ModeController getModeController() {
		return modeController;
	}
}
