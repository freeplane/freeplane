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
package org.freeplane.features.mindmapmode.file;

import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.File;
import java.util.Iterator;
import java.util.List;

import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.Compat;

class FileOpener implements DropTargetListener {
	/**
	 *
	 */
	final private ModeController modeController;

	/**
	 * @param modeController
	 */
	FileOpener(final ModeController modeController) {
		this.modeController = modeController;
	}

	public void dragEnter(final DropTargetDragEvent dtde) {
		if (!isDragAcceptable(dtde)) {
			dtde.rejectDrag();
			return;
		}
	}

	public void dragExit(final DropTargetEvent e) {
	}

	public void dragOver(final DropTargetDragEvent e) {
	}

	public void dragScroll(final DropTargetDragEvent e) {
	}

	public void drop(final DropTargetDropEvent dtde) {
		if (!isDropAcceptable(dtde)) {
			dtde.rejectDrop();
			return;
		}
		dtde.acceptDrop(DnDConstants.ACTION_COPY);
		try {
			final Object data = dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
			if (data == null) {
				dtde.dropComplete(false);
				return;
			}
			final Iterator iterator = ((List) data).iterator();
			while (iterator.hasNext()) {
				final File file = (File) iterator.next();
				modeController.getMapController().newMap(Compat.fileToUrl(file));
			}
		}
		catch (final Exception e) {
			UITools.errorMessage("Couldn't open dropped file(s). Reason: " + e.getMessage());
			dtde.dropComplete(false);
			return;
		}
		dtde.dropComplete(true);
	}

	public void dropActionChanged(final DropTargetDragEvent e) {
	}

	private boolean isDragAcceptable(final DropTargetDragEvent event) {
		final DataFlavor[] flavors = event.getCurrentDataFlavors();
		for (int i = 0; i < flavors.length; i++) {
			if (flavors[i].isFlavorJavaFileListType()) {
				return true;
			}
		}
		return false;
	}

	private boolean isDropAcceptable(final DropTargetDropEvent event) {
		final DataFlavor[] flavors = event.getCurrentDataFlavors();
		for (int i = 0; i < flavors.length; i++) {
			if (flavors[i].isFlavorJavaFileListType()) {
				return true;
			}
		}
		return false;
	}
}
