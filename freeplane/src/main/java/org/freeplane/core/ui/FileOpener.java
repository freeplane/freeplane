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
package org.freeplane.core.ui;

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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.Compat;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.clipboard.MindMapNodesSelection;

public class FileOpener implements DropTargetListener {
	
    private final String fileExtension;
	private Listener listener;

	public static interface Listener {
         public void filesDropped(Collection<URL> urls ) throws Exception;
    }
    

	public FileOpener(String fileExtension, Listener listener) {
		this.listener = listener;
		this.fileExtension = fileExtension;
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
	private boolean isMindMapUrl(final String urlString) {
	    return urlString.substring(urlString.length() - fileExtension.length() - 1).equalsIgnoreCase("." + fileExtension);
    }

	@SuppressWarnings("unchecked")
	public void drop(final DropTargetDropEvent dtde) {
		if (!isDropAcceptable(dtde)) {
			dtde.rejectDrop();
			return;
		}
		dtde.acceptDrop(DnDConstants.ACTION_COPY);
		try {
			ArrayList<URL> droppedUrls = new ArrayList<>();
			final Transferable transferable = dtde.getTransferable();
			if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
				final List<File> list = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
				for (final File file : list) {
					if (file.isDirectory() || ! isMindMapUrl(file.getName()) || ! file.canRead()) {
						continue;
					}
					final URL url = Compat.fileToUrl(file);
					
					droppedUrls.add(url);
				}
			}
			else if (transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
				final String urlStringRepresentation = (String) transferable.getTransferData(DataFlavor.stringFlavor);
				if(urlStringRepresentation.startsWith("file:")){
					final Matcher matcher = filePattern.matcher(urlStringRepresentation);
					while (matcher.find()) {
						final String urlString = matcher.group();
						if(isMindMapUrl(urlString)) {
							try {
								final URI uri = new URI(urlString);
								final URL url = new URL(uri.getScheme(), uri.getHost(), uri.getPath());
								final File file = Compat.urlToFile(url);
								if(! file.exists() || file.isDirectory())
									continue;
								droppedUrls.add(url);
							}
							catch (final Exception e) {
								e.printStackTrace();
								continue;
							}
						}
					}
				}
				else if(urlStringRepresentation.startsWith("http://") && isMindMapUrl(urlStringRepresentation)){
					final URL url = new URL(urlStringRepresentation);
					droppedUrls.add(url);
				}
			}
			listener.filesDropped(droppedUrls);
		}
		catch (final Exception e) {
			UITools.errorMessage(TextUtils.format("dropped_file_error", e.getMessage()));
			dtde.dropComplete(false);
			return;
		}
		dtde.dropComplete(true);
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
