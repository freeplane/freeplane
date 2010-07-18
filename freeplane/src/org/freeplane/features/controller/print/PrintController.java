/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is created by Dimitry Polivaev in 2008.
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

import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.util.Compat;

/**
 * @author Dimitry Polivaev
 */
public class PrintController implements IExtension {
	public static PrintController getController(final Controller controller) {
		return (PrintController) controller.getExtension(PrintController.class);
	}

	public static void install(final Controller controller) {
		controller.addExtension(PrintController.class, new PrintController(controller));
	}

// // 	final private Controller controller;
	final private PageAction pageAction;
	private PageFormat pageFormat = null;
	final private PrintAction printAction;
	final private PrintDirectAction printDirectAction;
	private PrinterJob printerJob = null;
	private boolean printingAllowed;
	final private PrintPreviewAction printPreviewAction;

	public PrintController(final Controller controller) {
		super();
//		this.controller = controller;
		printAction = new PrintAction(controller, this, true);
		printDirectAction = new PrintDirectAction(controller, this);
		printPreviewAction = new PrintPreviewAction(controller, this);
		pageAction = new PageAction(this);
		controller.addAction(printAction);
		controller.addAction(printDirectAction);
		controller.addAction(printPreviewAction);
		controller.addAction(pageAction);
		printingAllowed = true;
	}

	boolean acquirePrinterJobAndPageFormat(boolean showDlg) {
		if (printerJob == null || showDlg && Compat.isWindowsOS()) {
			try {
				printerJob = PrinterJob.getPrinterJob();
			}
			catch (final SecurityException ex) {
				printAction.setEnabled(false);
				printDirectAction.setEnabled(false);
				printPreviewAction.setEnabled(false);
				pageAction.setEnabled(false);
				printingAllowed = false;
				return false;
			}
		}
		return true;
	}

	public Controller getController() {
		return Controller.getCurrentController();
	}

	PageFormat getPageFormat() {
		if (pageFormat == null) {
			pageFormat = printerJob.defaultPage();
		}
		return pageFormat;
	}

	private PrinterJob getPrinterJob() {
		return printerJob;
	}

	public boolean isEnabled() {
		return printingAllowed;
	}

	public void pageDialog() {
		this.pageFormat = getPrinterJob().pageDialog(getPageFormat());	    
    }

	public boolean printDialog() {
	    return getPrinterJob().printDialog();
	}

	public void print(Printable mapView, boolean showDlg) throws PrinterException {
		if (!acquirePrinterJobAndPageFormat(showDlg)) {
			return;
		}
		getPrinterJob().setPrintable(mapView, getPageFormat());
		if(! showDlg || printDialog()){
			getPrinterJob().print();
		}
	}
}
