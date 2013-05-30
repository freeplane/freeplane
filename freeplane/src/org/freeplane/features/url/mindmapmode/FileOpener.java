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

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.Compat;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.clipboard.MindMapNodesSelection;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.mode.mindmapmode.MModeController;

public class FileOpener implements DropTargetListener {
	/**
	 *
	 */
// 	final private ModeController modeController;

	/**
	 * @param modeController
	 */
	public FileOpener() {
//		this.modeController = modeController;
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

	static final private Pattern filePattern = Pattern.compile("file://[^\\s" + File.pathSeparatorChar + "]+");

	@SuppressWarnings("unchecked")
	public void drop(final DropTargetDropEvent dtde) {
		if (!isDropAcceptable(dtde)) {
			dtde.rejectDrop();
			return;
		}
		dtde.acceptDrop(DnDConstants.ACTION_COPY);
		try {
			final Transferable transferable = dtde.getTransferable();
			Controller.getCurrentController().selectMode(MModeController.MODENAME);
			ModeController modeController = Controller.getCurrentModeController();
			if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
				final List<File> list = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
				for (final File file : list) {
					String fileName = file.getName();
					if (file.isDirectory() || !isMindMapUrl(fileName)) {
						continue;
					}
					modeController.getMapController().newMap(Compat.fileToUrl(file));
				}
			}
			if (transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
				final String urls = (String) transferable.getTransferData(DataFlavor.stringFlavor);
				if(urls.startsWith("file:")){
					final Matcher matcher = filePattern.matcher(urls);
					while (matcher.find()) {
						final String urlString = matcher.group();
						if (!isMindMapUrl(urlString)) {
							continue;
						}
						try {
							final URI uri = new URI(urlString);
							final URL url = new URL(uri.getScheme(), uri.getHost(), uri.getPath());
							final File file = Compat.urlToFile(url);
							if(! file.exists() || file.isDirectory())
								continue;
							modeController.getMapController().newMap(url);
						}
						catch (final Exception e) {
							e.printStackTrace();
							continue;
						}
					}
				}
				else if(urls.startsWith("http://") && isMindMapUrl(urls)){
					final URL url = new URL(urls);
					modeController.getMapController().newMap(url);
				}
			}
		}
		catch (final Exception e) {
			UITools.errorMessage(TextUtils.format("dropped_file_error", e.getMessage()));
			dtde.dropComplete(false);
			return;
		}
		dtde.dropComplete(true);
	}

	private boolean isMindMapUrl(final String urlString) {
	    return urlString.substring(urlString.length() - 3).equalsIgnoreCase(".mm");
    }

	public void dropActionChanged(final DropTargetDragEvent e) {
	}

	private boolean isDragAcceptable(final DropTargetDragEvent event) {
		return event.isDataFlavorSupported(DataFlavor.javaFileListFlavor)
		        || !event.isDataFlavorSupported(MindMapNodesSelection.mindMapNodesFlavor)
		        && event.isDataFlavorSupported(DataFlavor.stringFlavor);
	}

	private boolean isDropAcceptable(final DropTargetDropEvent event) {
		return event.isDataFlavorSupported(DataFlavor.javaFileListFlavor)
		        || event.isDataFlavorSupported(DataFlavor.stringFlavor);
	}
}
