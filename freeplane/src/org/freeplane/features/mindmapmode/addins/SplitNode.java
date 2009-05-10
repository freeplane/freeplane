/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
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
package org.freeplane.features.mindmapmode.addins;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import java.util.ListIterator;

import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.ActionLocationDescriptor;
import org.freeplane.core.util.FixedHTMLWriter;
import org.freeplane.core.util.LogTool;
import org.freeplane.features.common.nodestyle.NodeStyleController;
import org.freeplane.features.common.text.TextController;
import org.freeplane.features.mindmapmode.MMapController;
import org.freeplane.features.mindmapmode.nodestyle.MNodeStyleController;
import org.freeplane.features.mindmapmode.text.MTextController;

/**
 * @author Dimitry Polivaev
 */
@ActionLocationDescriptor(locations = { "/menu_bar/extras/first/join" })
public class SplitNode extends AFreeplaneAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 *
	 */
	public SplitNode(final Controller controller) {
		super("SplitNode", controller);
	}

	/*
	 * (non-Javadoc)
	 * @see freeplane.extensions.NodeHook#invoke(freeplane.modes.MindMapNode,
	 * java.util.List)
	 */
	public void actionPerformed(final ActionEvent e) {
		final List list = getModeController().getMapController().getSelectedNodes();
		final ListIterator listIterator = list.listIterator();
		while (listIterator.hasNext()) {
			final NodeModel next = (NodeModel) listIterator.next();
			splitNode(next);
		}
	}

	private Element getParentElement(final HTMLDocument doc) {
		final Element htmlRoot = doc.getDefaultRootElement();
		Element parentCandidate = htmlRoot.getElement(htmlRoot.getElementCount() - 1);
		do {
			if (parentCandidate.getElementCount() > 1) {
				return parentCandidate;
			}
			parentCandidate = parentCandidate.getElement(0);
		} while (!(parentCandidate.isLeaf() || parentCandidate.getName().equalsIgnoreCase("p-implied")));
		return null;
	}

	private void splitNode(final NodeModel node) {
		if (node.isRoot()) {
			return;
		}
		final String text = node.toString();
		final String[] parts = splitNode(text);
		if (parts == null || parts.length == 1) {
			return;
		}
		final ModeController c = getModeController();
		int firstPartNumber = 0;
		while (parts[firstPartNumber] == null) {
			firstPartNumber++;
		}
		((MTextController) TextController.getController(c)).setNodeText(node, parts[firstPartNumber]);
		final NodeModel parent = node.getParentNode();
		final int nodePosition = parent.getChildPosition(node) + 1;
		for (int i = parts.length - 1; i > firstPartNumber; i--) {
			final MMapController mapController = (MMapController) c.getMapController();
			final NodeModel lowerNode = mapController.addNewNode(parent, nodePosition, node.isLeft());
			final String part = parts[i];
			if (part == null) {
				continue;
			}
			((MTextController) TextController.getController(c)).setNodeText(lowerNode, part);
			final MNodeStyleController nodeStyleController = (MNodeStyleController) NodeStyleController
			    .getController(c);
			nodeStyleController.copyStyle(node, lowerNode);
			mapController.setFolded(lowerNode, !lowerNode.isFolded());
		}
	}

	private String[] splitNode(final String text) {
		if (text.startsWith("<html>")) {
			String[] parts = null;
			final HTMLEditorKit kit = new HTMLEditorKit();
			final HTMLDocument doc = new HTMLDocument();
			final StringReader buf = new StringReader(text);
			try {
				kit.read(buf, doc, 0);
				final Element parent = getParentElement(doc);
				if (parent == null) {
					return null;
				}
				final int elementCount = parent.getElementCount();
				int notEmptyElementCount = 0;
				parts = new String[elementCount];
				for (int i = 0; i < elementCount; i++) {
					final Element current = parent.getElement(i);
					final int start = current.getStartOffset();
					final int end = current.getEndOffset();
					final String paragraphText = doc.getText(start, end - start).trim();
					if (paragraphText.length() > 0) {
						final StringWriter out = new StringWriter();
						new FixedHTMLWriter(out, doc, start, end - start).write();
						final String string = out.toString();
						if (!string.equals("")) {
							parts[i] = string;
							notEmptyElementCount++;
						}
						else {
							parts[i] = null;
						}
					}
				}
				if (notEmptyElementCount <= 1) {
					return null;
				}
			}
			catch (final IOException e) {
				LogTool.severe(e);
			}
			catch (final BadLocationException e) {
				LogTool.severe(e);
			}
			return parts;
		}
		return text.split("\n");
	}
}
