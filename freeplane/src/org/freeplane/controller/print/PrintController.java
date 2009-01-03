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
package org.freeplane.controller.print;

import java.awt.print.PageFormat;
import java.awt.print.PrinterJob;

import javax.swing.Action;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.util.Tools;

/**
 * @author Dimitry Polivaev
 */
public class PrintController implements IExtension {
	public static PrintController getController() {
		return (PrintController) Controller.getController().getExtension(PrintController.class);
	}

	public static void install() {
		Controller.getController().addExtension(PrintController.class, new PrintController());
	}

	final private Action page;
	private PageFormat pageFormat = null;
	final private Action print;
	final private Action printDirect;
	private PrinterJob printerJob = null;
	private boolean printingAllowed;
	final private Action printPreview;

	public PrintController() {
		super();
		print = new PrintAction(this, true);
		printDirect = new PrintAction(this, false);
		printPreview = new PrintPreviewAction(this);
		page = new PageAction(this);
		final Controller controller = Controller.getController();
		controller.addAction("print", print);
		controller.addAction("printDirect", printDirect);
		controller.addAction("printPreview", printPreview);
		controller.addAction("page", page);
		printingAllowed = true;
	}

	boolean acquirePrinterJobAndPageFormat() {
		if (printerJob == null) {
			try {
				printerJob = PrinterJob.getPrinterJob();
			}
			catch (final SecurityException ex) {
				print.setEnabled(false);
				printDirect.setEnabled(false);
				printPreview.setEnabled(false);
				page.setEnabled(false);
				printingAllowed = false;
				return false;
			}
		}
		if (pageFormat == null) {
			pageFormat = printerJob.defaultPage();
			if (Tools.safeEquals(
			    Controller.getResourceController().getProperty("page_orientation"), "landscape")) {
				pageFormat.setOrientation(PageFormat.LANDSCAPE);
			}
			else if (Tools.safeEquals(Controller.getResourceController().getProperty(
			    "page_orientation"), "portrait")) {
				pageFormat.setOrientation(PageFormat.PORTRAIT);
			}
			else if (Tools.safeEquals(Controller.getResourceController().getProperty(
			    "page_orientation"), "reverse_landscape")) {
				pageFormat.setOrientation(PageFormat.REVERSE_LANDSCAPE);
			}
		}
		return true;
	}

	public PageFormat getPageFormat() {
		return pageFormat;
	}

	PrinterJob getPrinterJob() {
		return printerJob;
	}

	public boolean isEnabled() {
		return printingAllowed;
	}

	void setPageFormat(final PageFormat pageFormat) {
		this.pageFormat = pageFormat;
	}

	void setPrinterJob(final PrinterJob printerJob) {
		this.printerJob = printerJob;
	}
}
