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
package org.freeplane.features.print;

import java.awt.Component;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Locale;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.Compat;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.view.swing.map.MapView;

/**
 * @author Dimitry Polivaev
 */
public class PrintController implements IExtension {
	public static PrintController getController() {
		Controller controller = Controller.getCurrentController();
		return controller.getExtension(PrintController.class);
	}

	public static void install() {
		Controller controller = Controller.getCurrentController();
		controller.addExtension(PrintController.class, new PrintController());
	}

// // 	final private Controller controller;
	final private PageAction pageAction;
	private PageFormat pageFormat = null;
	final private PrintAction printAction;
	final private PrintDirectAction printDirectAction;
	private PrinterJob printerJob = null;
	private boolean printingAllowed;
	final private PrintPreviewAction printPreviewAction;

	public PrintController() {
		super();
		Controller controller = Controller.getCurrentController();
//		this.controller = controller;
		printAction = new PrintAction(this, true);
		printDirectAction = new PrintDirectAction(this);
		printPreviewAction = new PrintPreviewAction(this);
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

	PageFormat getPageFormat() {
		if (pageFormat == null) {
			pageFormat = printerJob.defaultPage();
			String pageSettings = ResourceController.getResourceController().getProperty("pageSettings", null);
			if(pageSettings == null){
				return pageFormat;
			}
			ParsePosition pos = new ParsePosition(0);
			NumberFormat parser = DecimalFormat.getInstance(Locale.US);
			Number pageFormatWidth = parser.parse(pageSettings, pos);
			if(pos.getErrorIndex() != -1 || pageFormatWidth == null)
				return pageFormat;
			pos.setIndex(pos.getIndex()+1);
			Number pageFormatHeight = parser.parse(pageSettings, pos);
			if(pos.getErrorIndex() != -1 || pageFormatHeight == null)
				return pageFormat;
			pos.setIndex(pos.getIndex()+1);
			Number pageFormatImageableX = parser.parse(pageSettings, pos);
			if(pos.getErrorIndex() != -1 || pageFormatImageableX == null)
				return pageFormat;
			pos.setIndex(pos.getIndex()+1);
			Number pageFormatImageableY = parser.parse(pageSettings, pos);
			if(pos.getErrorIndex() != -1|| pageFormatImageableY == null)
				return pageFormat;
			pos.setIndex(pos.getIndex()+1);
			Number pageFormatImageableWidth = parser.parse(pageSettings, pos);
			if(pos.getErrorIndex() != -1 || pageFormatImageableWidth == null)
				return pageFormat;
			pos.setIndex(pos.getIndex()+1);
			Number pageFormatImageableHeight = parser.parse(pageSettings, pos);
			if(pos.getErrorIndex() != -1 || pageFormatImageableHeight == null)
				return pageFormat;
			pos.setIndex(pos.getIndex()+1);
			Number pageFormatOrientation = parser.parse(pageSettings, pos);
			if(pos.getErrorIndex() != -1 || pageFormatOrientation == null)
				return pageFormat;
			Paper paper = (Paper) pageFormat.getPaper().clone();
			paper.setSize(pageFormatWidth.doubleValue(), pageFormatHeight.doubleValue());
			paper.setImageableArea(pageFormatImageableX.doubleValue(), pageFormatImageableY.doubleValue(), pageFormatImageableWidth.doubleValue(), pageFormatImageableHeight.doubleValue());
			pageFormat.setOrientation(pageFormatOrientation.intValue());
			pageFormat.setPaper(paper);
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
		Paper paper = pageFormat.getPaper();
		StringBuilder sb = new StringBuilder();
		NumberFormat format = DecimalFormat.getInstance(Locale.US);
		double paperWidth = paper.getWidth(); sb.append(format.format(paperWidth)).append(' ');
		double paperHeight = paper.getHeight(); sb.append(format.format(paperHeight)).append(' ');
		double imageableX = paper.getImageableX(); sb.append(format.format(imageableX)).append(' ');
		double imageableY = paper.getImageableY(); sb.append(format.format(imageableY)).append(' ');
		double imageableWidth = paper.getImageableWidth(); sb.append(format.format(imageableWidth)).append(' ');
		double imageableHeight = paper.getImageableHeight(); sb.append(format.format(imageableHeight)).append(' ');
		int pageFormatOrientation = pageFormat.getOrientation(); sb.append(format.format(pageFormatOrientation)).append(' ');
		ResourceController.getResourceController().setProperty("pageSettings", sb.toString());
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
			if(mapView instanceof MapView)
				((MapView)mapView).preparePrinting();
			try{
				final PrinterJob printerJob = getPrinterJob();
				if (mapView instanceof Component){
					final String name = ((Component)mapView).getName();
					if(name != null)
						printerJob.setJobName(name);
				}
				printerJob.print();
			}
			catch(PrinterException ex){
				LogUtils.warn(ex);
			}
			finally{
				if(mapView instanceof MapView)
					((MapView)mapView).endPrinting();
			}
		}
	}
}
