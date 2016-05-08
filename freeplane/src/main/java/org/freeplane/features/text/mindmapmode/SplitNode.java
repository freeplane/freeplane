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
package org.freeplane.features.text.mindmapmode;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collection;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.util.FixedHTMLWriter;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.mindmapmode.MMapController;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.nodestyle.NodeStyleController;
import org.freeplane.features.nodestyle.mindmapmode.MNodeStyleController;
import org.freeplane.features.text.TextController;

/**
 * @author Dimitry Polivaev
 */
public class SplitNode extends AFreeplaneAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 *
	 */
	public SplitNode() {
		super("SplitNode");
	}

	/*
	 * (non-Javadoc)
	 * @see freeplane.extensions.NodeHook#invoke(freeplane.modes.MindMapNode,
	 * java.util.List)
	 */
	public void actionPerformed(final ActionEvent e) {
		final Collection<NodeModel> list = Controller.getCurrentModeController().getMapController().getSelectedNodes();
		for (NodeModel next : list) {
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
		final String text = node.getText();
		final String[] parts = splitNode(text);
		if (parts == null || parts.length == 1) {
			return;
		}
		final ModeController c = Controller.getCurrentModeController();
		int firstPartNumber = 0;
		while (parts[firstPartNumber] == null) {
			firstPartNumber++;
		}
		((MTextController) TextController.getController()).setNodeText(node, parts[firstPartNumber]);
		final NodeModel parent = node.getParentNode();
		final int nodePosition = parent.getIndex(node) + 1;
		for (int i = parts.length - 1; i > firstPartNumber; i--) {
			final MMapController mapController = (MMapController) c.getMapController();
			final String part = parts[i];
			if (part == null) {
				continue;
			}
			final NodeModel lowerNode = mapController.addNewNode(parent, nodePosition, node.isLeft());
			((MTextController) TextController.getController()).setNodeText(lowerNode, part);
			final MNodeStyleController nodeStyleController = (MNodeStyleController) NodeStyleController
			    .getController();
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
				int start = parent.getStartOffset();
				for (int i = 0; i < elementCount; i++) {
					final Element current = parent.getElement(i);
					if(current.isLeaf())
						continue;
					final int end = current.getEndOffset();
					final String paragraphText = doc.getText(start, end - start).trim();
					if (paragraphText.length() > 0) {
						final StringWriter out = new StringWriter();
						new FixedHTMLWriter(out, doc, start, end - start).write();
						final String string = out.toString();
						if (!string.equals("")) {
							parts[i] = string;
							notEmptyElementCount++;
							start = end;
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
				LogUtils.severe(e);
			}
			catch (final BadLocationException e) {
				LogUtils.severe(e);
			}
			return parts;
		}
		return text.split("\n");
	}
}
