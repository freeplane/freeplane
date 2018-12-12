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
package org.freeplane.features.map.clipboard;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.ByteArrayInputStream;
import java.util.Collection;

import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.NodeModel;

public class MindMapNodesSelection implements Transferable, ClipboardOwner {
	public static DataFlavor dropActionFlavor = null;
	public static DataFlavor fileListFlavor = null;
	public static DataFlavor htmlFlavor = null;
	public static DataFlavor mindMapNodesFlavor = null;
	public static DataFlavor mindMapNodeObjectsFlavor = null;
	public static DataFlavor mindMapNodeSingleObjectsFlavor = null;
	public static DataFlavor rtfFlavor = null;
	static {
		try {
			MindMapNodesSelection.mindMapNodesFlavor = new DataFlavor("text/freeplane-nodes; class=java.lang.String");
			MindMapNodesSelection.mindMapNodeObjectsFlavor = new DataFlavor("application/freeplane-nodes; class=java.util.Collection");
			MindMapNodesSelection.mindMapNodeSingleObjectsFlavor = new DataFlavor("application/freeplane-single-nodes; class=java.util.Collection");
			MindMapNodesSelection.rtfFlavor = new DataFlavor("text/rtf; class=java.io.InputStream");
			MindMapNodesSelection.htmlFlavor = new DataFlavor("text/html; class=java.lang.String");
			MindMapNodesSelection.fileListFlavor = new DataFlavor("application/x-java-file-list; class=java.util.List");
			MindMapNodesSelection.dropActionFlavor = new DataFlavor("text/drop-action; class=java.lang.String");
		}
		catch (final Exception e) {
			LogUtils.severe(e);
		}
	}
	final private String htmlContent;
	final private String nodesContent;
	final private String rtfContent;
	final private String stringContent;
	private String dropActionContent;
	private Collection<NodeModel> nodes;
	private boolean selectionContainsSingleNodes;

	public MindMapNodesSelection(final String nodesContent, final String stringContent, final String rtfContent,
	                             final String htmlContent) {
		this.nodesContent = nodesContent;
		this.rtfContent = rtfContent;
		this.stringContent = stringContent;
		this.htmlContent = htmlContent;
		this.dropActionContent = null;
	}

	public MindMapNodesSelection(final String nodesContent) {
	    this(nodesContent, null, null, null);
    }

	public Object getTransferData(final DataFlavor flavor) throws UnsupportedFlavorException {
		if (flavor.equals(DataFlavor.stringFlavor)) {
			return stringContent;
		}
		if (flavor.equals(MindMapNodesSelection.mindMapNodesFlavor)) {
			return nodesContent;
		}
		if (flavor.equals(MindMapNodesSelection.dropActionFlavor)) {
			return dropActionContent;
		}
		if (flavor.equals(MindMapNodesSelection.rtfFlavor)) {
			final byte[] byteArray = rtfContent.getBytes();
			return new ByteArrayInputStream(byteArray);
		}
		if (flavor.equals(MindMapNodesSelection.htmlFlavor) && htmlContent != null) {
			return htmlContent;
		}
		if (containsObjectsFor(flavor)) {
			return nodes;
		}
		throw new UnsupportedFlavorException(flavor);
	}

	boolean containsObjectsFor(final DataFlavor flavor) {
		return nodes != null && (flavor.equals(MindMapNodesSelection.mindMapNodeObjectsFlavor) && ! selectionContainsSingleNodes 
				|| flavor.equals(MindMapNodesSelection.mindMapNodeSingleObjectsFlavor) && selectionContainsSingleNodes);
	}

	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[] { DataFlavor.stringFlavor, MindMapNodesSelection.mindMapNodesFlavor,
		        MindMapNodesSelection.rtfFlavor, MindMapNodesSelection.htmlFlavor,
		        MindMapNodesSelection.dropActionFlavor, 
		        MindMapNodesSelection.mindMapNodeObjectsFlavor , MindMapNodesSelection.mindMapNodeSingleObjectsFlavor };
	}

	public boolean isDataFlavorSupported(final DataFlavor flavor) {
		if (flavor.equals(DataFlavor.stringFlavor) && stringContent != null) {
			return true;
		}
		if (flavor.equals(MindMapNodesSelection.mindMapNodesFlavor) && nodesContent != null) {
			return true;
		}
		if (flavor.equals(MindMapNodesSelection.rtfFlavor) && rtfContent != null) {
			return true;
		}
		if (flavor.equals(MindMapNodesSelection.dropActionFlavor) && dropActionContent != null) {
			return true;
		}
		if (flavor.equals(MindMapNodesSelection.htmlFlavor) && htmlContent != null) {
			return true;
		}
		if (containsObjectsFor(flavor)) {
			return true;
		}
		return false;
	}

	public void lostOwnership(final Clipboard clipboard, final Transferable contents) {
	}

	public void setDropAction(final String dropActionContent) {
		this.dropActionContent = dropActionContent;
	}

	public void setNodeObjects(Collection<NodeModel> collection, boolean selectionContainsSingleNodes) {
	    nodes = collection;
	    this.selectionContainsSingleNodes = selectionContainsSingleNodes;
    }
}
