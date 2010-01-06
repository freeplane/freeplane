/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2009 Dimitry
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
package org.freeplane.main.mindmapmode.stylemode;

import java.awt.Window;

import org.freeplane.core.controller.Controller;
import org.freeplane.features.mindmapmode.MModeController;

/**
 * @author Dimitry Polivaev
 * 18.09.2009
 */
class SModeController extends MModeController{

	public SModeController(Controller controller) {
	    super(controller);
		Window dialog = ((DialogController)controller.getViewController()).getDialog();
		final ControlToolbar controlToolbar = new ControlToolbar(controller, "styledialog", dialog);
		controller.addAction(controlToolbar.getOkAction());
		controller.addAction(controlToolbar.getCancelAction());
    }
}
