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
package org.freeplane.features.common.clipboard;

import java.awt.Color;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.io.MapWriter.Mode;
import org.freeplane.core.modecontroller.IMapSelection;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.util.LogTool;
import org.freeplane.features.common.link.NodeLinks;
import org.freeplane.features.common.nodestyle.NodeStyleModel;

/**
 * @author Dimitry Polivaev
 */
public class ClipboardController implements IExtension {
	public static final String NODESEPARATOR = "<nodeseparator>";

	public static ClipboardController getController(final ModeController modeController) {
		return (ClipboardController) modeController.getExtension(ClipboardController.class);
	}

	public static void install(final ModeController modeController, final ClipboardController clipboardController) {
		modeController.addExtension(ClipboardController.class, clipboardController);
	}

	final private Clipboard clipboard;
	final private ModeController modeController;
	final private Clipboard selection;

	public ClipboardController(final ModeController modeController) {
		super();
		this.modeController = modeController;
		final Toolkit toolkit = Toolkit.getDefaultToolkit();
		selection = toolkit.getSystemSelection();
		clipboard = toolkit.getSystemClipboard();
		createActions();
	}

	private void collectColors(final NodeModel node, final HashSet colors) {
		final Color color = NodeStyleModel.getColor(node);
		if (color != null) {
			colors.add(color);
		}
		for (final ListIterator e = getModeController().getMapController().childrenUnfolded(node); e.hasNext();) {
			collectColors((NodeModel) e.next(), colors);
		}
	}

	public Transferable copy(final Collection<NodeModel> selectedNodes, final boolean copyInvisible) {
		try {
			final String forNodesFlavor = createForNodesFlavor(selectedNodes, copyInvisible);
			final String plainText = getAsPlainText(selectedNodes);
			return new MindMapNodesSelection(forNodesFlavor, plainText, getAsRTF(selectedNodes),
			    getAsHTML(selectedNodes), null, null);
		}
		catch (final UnsupportedFlavorException ex) {
			LogTool.severe(ex);
		}
		catch (final IOException ex) {
			LogTool.severe(ex);
		}
		return null;
	}

	public Transferable copy(final IMapSelection selection) {
		return copy(selection.getSortedSelection(true), false);
	}

	public Transferable copy(final NodeModel node, final boolean saveInvisible) {
		final StringWriter stringWriter = new StringWriter();
		try {
			getModeController().getMapController().getMapWriter().writeNodeAsXml(stringWriter, node, Mode.CLIPBOARD,
			    saveInvisible, true);
		}
		catch (final IOException e) {
			LogTool.severe(e);
		}
		return new MindMapNodesSelection(stringWriter.toString(), null, null, null, null, null);
	}

	public Transferable copySingle(final List<NodeModel> source) {
		final Collection target = new Vector(source.size());
		final ListIterator<NodeModel> iterator = source.listIterator(source.size());
		while (iterator.hasPrevious()) {
			final NodeModel node = iterator.previous();
			target.add(shallowCopy(node));
		}
		return copy(target, false);
	}

	/**
	 *
	 */
	private void createActions() {
		final Controller controller = modeController.getController();
		modeController.addAction(new CopyAction(controller));
		modeController.addAction(new CopySingleAction(controller));
		modeController.addAction(new CopyIDAction(controller));
	}

	public String createForNodesFlavor(final Collection<NodeModel> selectedNodes, final boolean copyInvisible)
	        throws UnsupportedFlavorException, IOException {
		String forNodesFlavor = "";
		boolean firstLoop = true;
		for (final Iterator it = selectedNodes.iterator(); it.hasNext();) {
			final NodeModel tmpNode = (NodeModel) it.next();
			if (firstLoop) {
				firstLoop = false;
			}
			else {
				forNodesFlavor += "<nodeseparator>";
			}
			forNodesFlavor += copy(tmpNode, copyInvisible).getTransferData(MindMapNodesSelection.mindMapNodesFlavor);
		}
		return forNodesFlavor;
	}

	public String getAsHTML(final Collection<NodeModel> selectedNodes) {
		try {
			final StringWriter stringWriter = new StringWriter();
			final BufferedWriter fileout = new BufferedWriter(stringWriter);
			writeHTML(selectedNodes, fileout);
			fileout.close();
			return stringWriter.toString();
		}
		catch (final Exception e) {
			LogTool.severe(e);
			return null;
		}
	}

	public String getAsPlainText(final Collection<NodeModel> selectedNodes) {
		try {
			final StringWriter stringWriter = new StringWriter();
			final BufferedWriter fileout = new BufferedWriter(stringWriter);
			for (final Iterator it = selectedNodes.iterator(); it.hasNext();) {
				writeTXT(((NodeModel) it.next()), fileout,/* depth= */0);
			}
			fileout.close();
			return stringWriter.toString();
		}
		catch (final Exception e) {
			LogTool.severe(e);
			return null;
		}
	}

	public String getAsRTF(final Collection<NodeModel> selectedNodes) {
		try {
			final StringWriter stringWriter = new StringWriter();
			final BufferedWriter fileout = new BufferedWriter(stringWriter);
			writeRTF(selectedNodes, fileout);
			fileout.close();
			return stringWriter.toString();
		}
		catch (final Exception e) {
			LogTool.severe(e);
			return null;
		}
	}

	/**
	 */
	public Transferable getClipboardContents() {
		return clipboard.getContents(this);
	}

	public ModeController getModeController() {
		return modeController;
	}

	private String rtfEscapeUnicodeAndSpecialCharacters(final String text) {
		final int len = text.length();
		final StringBuilder result = new StringBuilder(len);
		int intValue;
		char myChar;
		for (int i = 0; i < len; ++i) {
			myChar = text.charAt(i);
			intValue = text.charAt(i);
			if (intValue > 128) {
				result.append("\\u").append(intValue).append("?");
			}
			else {
				switch (myChar) {
					case '\\':
						result.append("\\\\");
						break;
					case '{':
						result.append("\\{");
						break;
					case '}':
						result.append("\\}");
						break;
					case '\n':
						result.append(" \\line ");
						break;
					default:
						result.append(myChar);
				}
			}
		}
		return result.toString();
	}

	public void saveHTML(final NodeModel rootNodeOfBranch, final File file) throws IOException {
		final BufferedWriter fileout = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
		final MindMapHTMLWriter htmlWriter = new MindMapHTMLWriter(getModeController().getMapController(), fileout);
		htmlWriter.writeHTML(rootNodeOfBranch);
	}

	public boolean saveTXT(final NodeModel rootNodeOfBranch, final File file) {
		try {
			final BufferedWriter fileout = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
			writeTXT(rootNodeOfBranch, fileout,/* depth= */
			0);
			fileout.close();
			return true;
		}
		catch (final Exception e) {
			LogTool.severe("Error in MindMapMapModel.saveTXT(): ", e);
			return false;
		}
	}

	/**
	 */
	public void setClipboardContents(final Transferable t) {
		clipboard.setContents(t, null);
		if (selection != null) {
			selection.setContents(t, null);
		}
	}

	public NodeModel shallowCopy(final NodeModel source) {
		try {
			final StringWriter writer = new StringWriter();
			modeController.getMapController().getMapWriter()
			    .writeNodeAsXml(writer, source, Mode.CLIPBOARD, true, false);
			final String result = writer.toString();
			final NodeModel copy = modeController.getMapController().getMapReader().createNodeTreeFromXml(
			    source.getMap(), new StringReader(result), Mode.CLIPBOARD);
			copy.setFolded(false);
			return copy;
		}
		catch (final Exception e) {
			LogTool.severe(e);
			return null;
		}
	}

	private void writeChildrenRTF(final NodeModel mindMapNodeModel, final Writer fileout, final int depth,
	                              final HashMap colorTable) throws IOException {
		for (final ListIterator e = getModeController().getMapController().childrenUnfolded(mindMapNodeModel); e
		    .hasNext();) {
			final NodeModel child = (NodeModel) e.next();
			if (child.isVisible()) {
				writeRTF(child, fileout, depth + 1, colorTable);
			}
			else {
				writeChildrenRTF(child, fileout, depth, colorTable);
			}
		}
	}

	private void writeChildrenText(final NodeModel mindMapNodeModel, final Writer fileout, final int depth)
	        throws IOException {
		for (final ListIterator e = getModeController().getMapController().childrenUnfolded(mindMapNodeModel); e
		    .hasNext();) {
			final NodeModel child = (NodeModel) e.next();
			if (child.isVisible()) {
				writeTXT(child, fileout, depth + 1);
			}
			else {
				writeChildrenText(child, fileout, depth);
			}
		}
	}

	public void writeHTML(final Collection<NodeModel> selectedNodes, final Writer fileout) throws IOException {
		final MindMapHTMLWriter htmlWriter = new MindMapHTMLWriter(getModeController().getMapController(), fileout);
		htmlWriter.writeHTML(selectedNodes);
	}

	public boolean writeRTF(final Collection<NodeModel> selectedNodes, final BufferedWriter fileout) {
		try {
			final HashSet colors = new HashSet();
			for (final Iterator it = selectedNodes.iterator(); it.hasNext();) {
				collectColors((NodeModel) it.next(), colors);
			}
			String colorTableString = "{\\colortbl;\\red0\\green0\\blue255;";
			final HashMap colorTable = new HashMap();
			int colorPosition = 2;
			for (final Iterator it = colors.iterator(); it.hasNext(); ++colorPosition) {
				final Color color = (Color) it.next();
				colorTableString += "\\red" + color.getRed() + "\\green" + color.getGreen() + "\\blue"
				        + color.getBlue() + ";";
				colorTable.put(color, new Integer(colorPosition));
			}
			colorTableString += "}";
			fileout.write("{\\rtf1\\ansi\\ansicpg1252\\deff0\\deflang1033{\\fonttbl{\\f0\\fswiss\\fcharset0 Arial;}"
			        + colorTableString + "}" + "\\viewkind4\\uc1\\pard\\f0\\fs20{}");
			for (final Iterator it = selectedNodes.iterator(); it.hasNext();) {
				writeRTF(((NodeModel) it.next()), fileout,/* depth= */0, colorTable);
			}
			fileout.write("}");
			return true;
		}
		catch (final Exception e) {
			LogTool.severe(e);
			return false;
		}
	}

	public void writeRTF(final NodeModel mindMapNodeModel, final Writer fileout, final int depth,
	                     final HashMap colorTable) throws IOException {
		String pre = "{" + "\\li" + depth * 350;
		String level;
		if (depth <= 8) {
			level = "\\outlinelevel" + depth;
		}
		else {
			level = "";
		}
		String fontsize = "";
		if (NodeStyleModel.getColor(mindMapNodeModel) != null) {
			pre += "\\cf" + ((Integer) colorTable.get(NodeStyleModel.getColor(mindMapNodeModel))).intValue();
		}
		final NodeStyleModel font = NodeStyleModel.getModel(mindMapNodeModel);
		if (font != null) {
			if (Boolean.TRUE.equals(font.isItalic())) {
				pre += "\\i ";
			}
			if (Boolean.TRUE.equals(font.isBold())) {
				pre += "\\b ";
			}
			if (font.getFontSize() != null) {
				fontsize = "\\fs" + Math.round(1.5 * font.getFontSize());
				pre += fontsize;
			}
		}
		pre += "{}";
		fileout.write("\\li" + depth * 350 + level + "{}");
		if (mindMapNodeModel.toString().matches(" *")) {
			fileout.write("o");
		}
		else {
			final String text = rtfEscapeUnicodeAndSpecialCharacters(mindMapNodeModel.getPlainTextContent());
			if (NodeLinks.getValidLink(mindMapNodeModel) != null) {
				final String link = rtfEscapeUnicodeAndSpecialCharacters(NodeLinks.getLinkAsString(mindMapNodeModel));
				if (link.equals(mindMapNodeModel.toString())) {
					fileout.write(pre + "<{\\ul\\cf1 " + link + "}>" + "}");
				}
				else {
					fileout.write("{" + fontsize + pre + text + "} ");
					fileout.write("<{\\ul\\cf1 " + link + "}}>");
				}
			}
			else {
				fileout.write(pre + text + "}");
			}
		}
		fileout.write("\\par");
		fileout.write("\n");
		writeChildrenRTF(mindMapNodeModel, fileout, depth, colorTable);
	}

	public void writeTXT(final NodeModel mindMapNodeModel, final Writer fileout, final int depth) throws IOException {
		final String plainTextContent = mindMapNodeModel.getPlainTextContent();
		for (int i = 0; i < depth; ++i) {
			fileout.write("    ");
		}
		if (plainTextContent.matches(" *")) {
			fileout.write("o");
		}
		else {
			if (NodeLinks.getValidLink(mindMapNodeModel) != null) {
				final String link = NodeLinks.getLinkAsString(mindMapNodeModel);
				if (!link.equals(plainTextContent)) {
					fileout.write(plainTextContent + " ");
				}
				fileout.write("<" + link + ">");
			}
			else {
				fileout.write(plainTextContent);
			}
		}
		fileout.write("\n");
		writeChildrenText(mindMapNodeModel, fileout, depth);
	}
}
