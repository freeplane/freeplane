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
package org.freeplane.features.print;

import java.awt.event.ActionEvent;
import java.awt.print.Printable;

import org.freeplane.core.util.LogUtils;
import org.freeplane.features.mode.Controller;

class PrintAction extends AbstractPrintAction {
	static final String NAME = "print";
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
// // 	final private Controller controller;
	final private boolean isDlg;

	PrintAction( final PrintController printController, final boolean isDlg) {
		this("PrintAction", printController, isDlg);
	}

	public PrintAction(final String key, final PrintController printController,
	                   final boolean isDlg) {
		super(key, printController);
//		this.controller = controller;
		this.isDlg = isDlg;
	}

	public void actionPerformed(final ActionEvent e) {
		final PrintController printController = getPrintController();
		try {
			printController.print((Printable) Controller.getCurrentController().getMapViewManager().getMapViewComponent(), isDlg);
		}
		catch (final Exception ex) {
			LogUtils.severe(ex);
		}
	}
}
