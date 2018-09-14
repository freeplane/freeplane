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
package org.freeplane.features.export.mindmapmode;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.Controller;

import java.awt.event.ActionEvent;
import java.util.Collections;

/**
 * @author foltin To change the template for this generated type comment go to
 *         Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
class ExportAction extends AFreeplaneAction {
	private ExportDialog exp = null;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ExportAction() {
		super("ExportAction");
	}

	public void actionPerformed(final ActionEvent e) {
		if(exp == null){
			final ExportController exportController = ExportController.getContoller();
			exp = exportController.createMapExportDialog();
		}
		final MapModel map = Controller.getCurrentController().getMap();
		if (map == null) {
			return;
		}
		exp.export(UITools.getCurrentRootComponent(), Collections.singletonList(map.getRootNode()));
	}

}
