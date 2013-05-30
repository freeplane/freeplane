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
package org.freeplane.features.url.mindmapmode;

import java.awt.event.ActionEvent;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.mindmapmode.MMapController;
import org.freeplane.features.mode.Controller;

/**
 * Reverts the map to the saved version. In Xml, the old map is stored as xml
 * and as an undo action, the new map is stored, too. Moreover, the filename of
 * the doAction is set to the appropriate map file's name. The undo action has
 * no file name associated. The action goes like this: close the actual map and
 * open the given Xml/File. If only a Xml string is given, a temporary file name
 * is created, the xml stored into and this map is opened instead of the actual.
 *
 * @author foltin
 */
class RevertAction extends AFreeplaneAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 */
	public RevertAction() {
		super("RevertAction");
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(final ActionEvent ev) {
		Controller controller =Controller.getCurrentController();
		final MMapController mapController = (MMapController) controller.getModeController().getMapController();
		try {
			mapController.restoreCurrentMap();
		}
		catch (final Exception e) {
			LogUtils.severe(e);
		}
	}

}
