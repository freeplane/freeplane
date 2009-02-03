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
package org.freeplane.features.controller.print;

import java.awt.event.ActionEvent;
import java.awt.print.Printable;

import javax.swing.ImageIcon;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.MenuBuilder;

class PrintAction extends AbstractPrintAction {
	final private Controller controller;
	final private boolean isDlg;

	PrintAction(final Controller controller, final PrintController printController, final boolean isDlg) {
		super(printController, null, new ImageIcon(ResourceController.getResourceController().getResource(
		    "/images/fileprint.png")));
		this.controller = controller;
		MenuBuilder.setLabelAndMnemonic(this, isDlg ? ResourceController.getText("print_dialog") : ResourceController.getText("print"));
		this.isDlg = isDlg;
	}

	public void actionPerformed(final ActionEvent e) {
		final PrintController printController = getPrintController();
		if (!printController.acquirePrinterJobAndPageFormat()) {
			return;
		}
		printController.getPrinterJob().setPrintable((Printable) controller.getViewController().getMapView(),
		    printController.getPageFormat());
		if (!isDlg || printController.getPrinterJob().printDialog()) {
			try {
				printController.getPrinterJob().print();
			}
			catch (final Exception ex) {
				org.freeplane.core.util.Tools.logException(ex);
			}
		}
	}
}
