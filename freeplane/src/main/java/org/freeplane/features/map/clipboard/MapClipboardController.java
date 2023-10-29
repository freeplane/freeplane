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
package org.freeplane.features.map.clipboard;

import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.Compat;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.clipboard.ClipboardAccessor;
import org.freeplane.features.clipboard.ClipboardController;
import org.freeplane.features.map.IMapSelection;
import org.freeplane.features.map.INodeDuplicator;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.MapWriter.Mode;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.styles.IStyle;

/**
 * @author Dimitry Polivaev
 */
public class MapClipboardController implements IExtension, ClipboardController, INodeDuplicator {
	public static enum CopiedNodeSet {ALL_NODES, FILTERED_NODES}

	public static final String NODESEPARATOR = "<nodeseparator>";

	public static MapClipboardController getController() {
		return Controller.getCurrentModeController().getExtension(MapClipboardController.class);
	}

	public static void install( final MapClipboardController clipboardController) {
		Controller.getCurrentModeController().addExtension(MapClipboardController.class, clipboardController);
	}

	private final ModeController modeController;
	public MapClipboardController(ModeController modeController) {
		super();
        this.modeController = modeController;
		createActions();
	}

	public void setClipboardContents(Transferable transferable) {
	    ClipboardAccessor.getInstance().setClipboardContents(transferable);
	}

	public MindMapNodesSelection copy(final Collection<NodeModel> selectedNodes) {
		CopiedNodeSet copiedNodeSet = ResourceController.getResourceController().getBooleanProperty("filtersCopiedNodes") ? MapClipboardController.CopiedNodeSet.FILTERED_NODES : MapClipboardController.CopiedNodeSet.ALL_NODES;
		CopiedNodeSet copiedTextNodeSet = ResourceController.getResourceController().getBooleanProperty("filtersCopiedText") ? MapClipboardController.CopiedNodeSet.FILTERED_NODES : MapClipboardController.CopiedNodeSet.ALL_NODES;
		return copy(selectedNodes, copiedNodeSet, copiedTextNodeSet);
	}

	public MindMapNodesSelection copy(final Collection<NodeModel> selectedNodes,
			CopiedNodeSet copiedNodeSet, CopiedNodeSet copiedTextNodeSet) {
		try {
			final String forNodesFlavor = createForNodesFlavor(selectedNodes, copiedNodeSet);
			final String plainText = MindMapPlainTextWriter.INSTANCE.getAsPlainText(selectedNodes, copiedTextNodeSet);
			return new MindMapNodesSelection(forNodesFlavor, plainText,
			    getAsHTML(selectedNodes));
		}
		catch (final UnsupportedFlavorException ex) {
			LogUtils.severe(ex);
		}
		catch (final IOException ex) {
			LogUtils.severe(ex);
		}
		return null;
	}

	public Transferable copy(final IMapSelection selection) {
		return copy(selection.getSortedSelection(true));
	}

	public Transferable copy(final NodeModel node, CopiedNodeSet copiedNodeSet) {
		final StringWriter stringWriter = new StringWriter();
		try {
			Controller.getCurrentModeController().getMapController().getMapWriter().writeNodeAsXml(stringWriter, node, Mode.CLIPBOARD,
			    copiedNodeSet, true, false);
		}
		catch (final IOException e) {
			LogUtils.severe(e);
		}
		return new MindMapNodesSelection(stringWriter.toString());
	}

	public Transferable copySingle(final Collection<NodeModel> source) {
		final int size = source.size();
		final Vector<NodeModel> target = new Vector<NodeModel>(size);
		target.setSize(size);
		int i = 0;
		for (NodeModel node : source) {
			target.set(i, new SingleCopySource(node));
			i++;
		}
		return copy(target);
	}

	/**
	 *
	 */
	private void createActions() {
		final Controller controller = Controller.getCurrentController();
		ModeController modeController = controller.getModeController();
		modeController.addAction(new CopySingleAction());
		if(!Compat.isApplet())
			modeController.addAction(new CopyIDAction());
		modeController.addAction(new CopyNodeURIAction());
	}

	public String createForNodesFlavor(final Collection<NodeModel> selectedNodes , CopiedNodeSet copiedNodeSet)
	        throws UnsupportedFlavorException, IOException {
		String forNodesFlavor = "";
		boolean firstLoop = true;
		for (final NodeModel tmpNode : selectedNodes) {
			if (firstLoop) {
				firstLoop = false;
			}
			else {
				forNodesFlavor += "<nodeseparator>";
			}
			forNodesFlavor += copy(tmpNode, copiedNodeSet).getTransferData(MindMapNodesSelection.mindMapNodesFlavor);
		}
		return forNodesFlavor;
	}

	public String getAsHTML(final Collection<NodeModel> selectedNodes) {
		try {
			final StringWriter stringWriter = new StringWriter();
			try (BufferedWriter fileout = new BufferedWriter(stringWriter)) {
			    writeHTML(selectedNodes, fileout);
			}
			return stringWriter.toString();
		}
		catch (final Exception e) {
			LogUtils.severe(e);
			return null;
		}
	}
	public void saveHTML(final NodeModel rootNodeOfBranch, final File file) throws IOException {
		saveHTML(Collections.singletonList(rootNodeOfBranch), file);
	}

	public void saveHTML(final List<NodeModel> branchRootNodes, final File file) throws IOException {
		final BufferedWriter fileout = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), //
			StandardCharsets.UTF_8));
		MapClipboardController.CopiedNodeSet copiedNodeSet = ResourceController.getResourceController().getBooleanProperty("filtersCopiedText") ? MapClipboardController.CopiedNodeSet.FILTERED_NODES : MapClipboardController.CopiedNodeSet.ALL_NODES;
		final MindMapHTMLWriter htmlWriter = new MindMapHTMLWriter(Controller.getCurrentModeController().getMapController(), copiedNodeSet, fileout);
		htmlWriter.configureCharset("UTF-8");
		htmlWriter.writeHTML(branchRootNodes);
	}

	@Override
	public NodeModel duplicate(final NodeModel source, final MapModel targetMap,  boolean withChildren) {
		try {
			final StringWriter writer = new StringWriter();
			Mode copyMode = source.getUserObject() instanceof IStyle ? Mode.STYLE : Mode.CLIPBOARD;
			MapClipboardController.CopiedNodeSet copiedNodeSet = copyMode == Mode.CLIPBOARD &&
					ResourceController.getResourceController().getBooleanProperty("filtersCopiedNodes") ? MapClipboardController.CopiedNodeSet.FILTERED_NODES : MapClipboardController.CopiedNodeSet.ALL_NODES;
			modeController.getMapController().getMapWriter()
			    .writeNodeAsXml(writer, source, copyMode, copiedNodeSet, withChildren, false);
			final String result = writer.toString();
            final NodeModel copy = modeController.getMapController().getMapReader().createNodeTreeFromXml(
                    targetMap, new StringReader(result), copyMode);
			copy.setFolded(false);
			return copy;
		}
		catch (final Exception e) {
			LogUtils.severe(e);
			return null;
		}
	}


	public void writeHTML(final Collection<NodeModel> selectedNodes, final Writer fileout) throws IOException {
		MapClipboardController.CopiedNodeSet copiedNodeSet = ResourceController.getResourceController().getBooleanProperty("filtersCopiedText") ? MapClipboardController.CopiedNodeSet.FILTERED_NODES : MapClipboardController.CopiedNodeSet.ALL_NODES;
		final MindMapHTMLWriter htmlWriter = new MindMapHTMLWriter(Controller.getCurrentModeController().getMapController(), copiedNodeSet, fileout);
		htmlWriter.writeHTML(selectedNodes);
	}

	@Override
	public boolean canCopy() {
		return true;
	}

	@Override
	public void copy() {
		final Controller controller = Controller.getCurrentController();
		final IMapSelection selection = controller.getSelection();
		if (selection != null) {
			final Transferable copy = copy(selection);
			if (copy != null) {
				ClipboardAccessor.getInstance().setClipboardContents(copy);
			}
		}
	}

	@Override
	public int getPriority() {
		return Integer.MIN_VALUE;
	}
}
