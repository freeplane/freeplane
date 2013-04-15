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

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.Controller;

class PrintPreviewAction extends AbstractPrintAction {
	static final String NAME = "printPreview";
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
// // 	final private Controller controller;

	PrintPreviewAction( final PrintController printController) {
		super("PrintPreviewAction", printController);
//		this.controller = controller;
	}

	public void actionPerformed(final ActionEvent e) {
		if (!getPrintController().acquirePrinterJobAndPageFormat(false)) {
			return;
		}
		final Component mapView = Controller.getCurrentController().getMapViewManager().getMapViewComponent();
		final PreviewDialog previewDialog = new PreviewDialog(getPrintController(), TextUtils
		    .getText("print_preview_title"), mapView);
		previewDialog.pack();
		previewDialog.setLocationRelativeTo(JOptionPane.getFrameForComponent(mapView));
		previewDialog.setVisible(true);
	}
}
