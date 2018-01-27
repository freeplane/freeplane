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
package org.freeplane.features.clipboard;

import java.awt.Color;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
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
import java.util.Vector;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.link.NodeLinks;
import org.freeplane.features.map.IMapSelection;
import org.freeplane.features.map.MapWriter.Mode;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.nodestyle.NodeStyleModel;
import org.freeplane.features.text.TextController;

/**
 * @author Dimitry Polivaev
 */
public class ClipboardController implements IExtension {
	public static final String NODESEPARATOR = "<nodeseparator>";

	public static ClipboardController getController() {
		return (ClipboardController) Controller.getCurrentModeController().getExtension(ClipboardController.class);
	}

	public static void install( final ClipboardController clipboardController) {
		Controller.getCurrentModeController().addExtension(ClipboardController.class, clipboardController);
	}

	final private Clipboard clipboard;
// 	final private ModeController modeController;
	final private Clipboard selection;

	public ClipboardController() {
		super();
//		this.modeController = modeController;
		final Toolkit toolkit = Toolkit.getDefaultToolkit();
		
		if (!GraphicsEnvironment.isHeadless()) {
			selection = toolkit.getSystemSelection();
			clipboard = toolkit.getSystemClipboard();
		} else {
			selection = null;
			clipboard = null;
		}
		createActions();
	}

	private void collectColors(final NodeModel node, final HashSet<Color> colors) {
		final Color color = NodeStyleModel.getColor(node);
		if (color != null) {
			colors.add(color);
		}
		for (final NodeModel child : Controller.getCurrentModeController().getMapController().childrenUnfolded(node)) {
			collectColors(child, colors);
		}
	}

	public MindMapNodesSelection copy(final Collection<NodeModel> selectedNodes, final boolean copyInvisible) {
		try {
			final String forNodesFlavor = createForNodesFlavor(selectedNodes, copyInvisible);
			final String plainText = getAsPlainText(selectedNodes);
			return new MindMapNodesSelection(forNodesFlavor, plainText, getAsRTF(selectedNodes),
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
		return copy(selection.getSortedSelection(true), false);
	}

	public Transferable copy(final NodeModel node, final boolean saveInvisible) {
		final StringWriter stringWriter = new StringWriter();
		try {
			Controller.getCurrentModeController().getMapController().getMapWriter().writeNodeAsXml(stringWriter, node, Mode.CLIPBOARD,
			    saveInvisible, true, false);
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
		return copy(target, false);
	}

	/**
	 *
	 */
	private void createActions() {
		final Controller controller = Controller.getCurrentController();
		ModeController modeController = controller.getModeController();
		modeController.addAction(new CopyAction());
		modeController.addAction(new CopySingleAction());
		if(!controller.getViewController().isApplet())
			modeController.addAction(new CopyIDAction());
		modeController.addAction(new CopyNodeURIAction());
	}

	public String createForNodesFlavor(final Collection<NodeModel> selectedNodes, final boolean copyInvisible)
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
			LogUtils.severe(e);
			return null;
		}
	}

	public String getAsPlainText(final Collection<NodeModel> selectedNodes) {
		try {
			final StringWriter stringWriter = new StringWriter();
			final BufferedWriter fileout = new BufferedWriter(stringWriter);
			for (final Iterator<NodeModel> it = selectedNodes.iterator(); it.hasNext();) {
				writeTXT(it.next(), fileout,/* depth= */0);
			}
			fileout.close();
			return stringWriter.toString();
		}
		catch (final Exception e) {
			LogUtils.severe(e);
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
			LogUtils.severe(e);
			return null;
		}
	}

	/**
	 */
	public Transferable getClipboardContents() {
		if (clipboard != null) {
			return clipboard.getContents(this);
		}
		return null;
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
		final MindMapHTMLWriter htmlWriter = new MindMapHTMLWriter(Controller.getCurrentModeController().getMapController(), fileout);
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
			LogUtils.severe("Error in MindMapMapModel.saveTXT(): ", e);
			return false;
		}
	}

	/**
	 */
	public void setClipboardContents(final Transferable t) {
		if (clipboard != null) {
			clipboard.setContents(t, null);
		}
		if (selection != null) {
			selection.setContents(t, null);
		}
	}

    /** copies a string to the system clipboard. */
    public void setClipboardContents(final String string) {
        setClipboardContents(new StringSelection(string));
    }
    
    /** copies a string to the system clipboard with text/html mimetype. */
    public void setClipboardContentsToHtml(final String html) {
    	setClipboardContents(new HtmlSelection(html));
    }

	public NodeModel duplicate(final NodeModel source, boolean withChildren) {
		try {
			final StringWriter writer = new StringWriter();
			ModeController modeController = Controller.getCurrentModeController();
			modeController.getMapController().getMapWriter()
			    .writeNodeAsXml(writer, source, Mode.CLIPBOARD, true, withChildren, false);
			final String result = writer.toString();
			final NodeModel copy = modeController.getMapController().getMapReader().createNodeTreeFromXml(
			    source.getMap(), new StringReader(result), Mode.CLIPBOARD);
			copy.setFolded(false);
			return copy;
		}
		catch (final Exception e) {
			LogUtils.severe(e);
			return null;
		}
	}

	private void writeChildrenRTF(final NodeModel node, final Writer fileout, final int depth,
	                              final HashMap<Color, Integer> colorTable) throws IOException {
		for (final NodeModel child : Controller.getCurrentModeController().getMapController().childrenUnfolded(node)) {
			if (child.hasVisibleContent()) {
				writeRTF(child, fileout, depth + 1, colorTable);
			}
			else {
				writeChildrenRTF(child, fileout, depth, colorTable);
			}
		}
	}

	private void writeChildrenText(final NodeModel node, final Writer fileout, final int depth, String indentation)
	        throws IOException {
		for (final NodeModel child : Controller.getCurrentModeController().getMapController().childrenUnfolded(node)) {
			if (child.hasVisibleContent()) {
				writeTXT(child, fileout, depth + 1, indentation);
			}
			else {
				writeChildrenText(child, fileout, depth, indentation);
			}
		}
	}

	public void writeHTML(final Collection<NodeModel> selectedNodes, final Writer fileout) throws IOException {
		final MindMapHTMLWriter htmlWriter = new MindMapHTMLWriter(Controller.getCurrentModeController().getMapController(), fileout);
		htmlWriter.writeHTML(selectedNodes);
	}

	public boolean writeRTF(final Collection<NodeModel> selectedNodes, final BufferedWriter fileout) {
		try {
			final HashSet<Color> colors = new HashSet<Color>();
			for (final Iterator<NodeModel> it = selectedNodes.iterator(); it.hasNext();) {
				collectColors(it.next(), colors);
			}
			String colorTableString = "{\\colortbl;\\red0\\green0\\blue255;";
			final HashMap<Color, Integer> colorTable = new HashMap<Color, Integer>();
			int colorPosition = 2;
			for (final Color color : colors) {
				colorTableString += "\\red" + color.getRed() + "\\green" + color.getGreen() + "\\blue"
				        + color.getBlue() + ";";
				colorTable.put(color, new Integer(colorPosition));
			}
			colorTableString += "}";
			fileout.write("{\\rtf1\\ansi\\ansicpg1252\\deff0\\deflang1033{\\fonttbl{\\f0\\fswiss\\fcharset0 Arial;}"
			        + colorTableString + "}" + "\\viewkind4\\uc1\\pard\\f0\\fs20{}");
			for (final Iterator<NodeModel> it = selectedNodes.iterator(); it.hasNext();) {
				writeRTF(it.next(), fileout,/* depth= */0, colorTable);
			}
			fileout.write("}");
			return true;
		}
		catch (final Exception e) {
			LogUtils.severe(e);
			return false;
		}
	}

	public void writeRTF(final NodeModel mindMapNodeModel, final Writer fileout, final int depth,
	                     final HashMap<Color, Integer> colorTable) throws IOException {
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
			pre += "\\cf" + colorTable.get(NodeStyleModel.getColor(mindMapNodeModel)).intValue();
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
		final String nodeText = TextController.getController().getPlainTransformedText(mindMapNodeModel);
		if (nodeText.matches(" *")) {
			fileout.write("o");
		}
		else {
			final String text = rtfEscapeUnicodeAndSpecialCharacters(nodeText);
			if (NodeLinks.getValidLink(mindMapNodeModel) != null) {
				final String link = rtfEscapeUnicodeAndSpecialCharacters(NodeLinks.getLinkAsString(mindMapNodeModel));
				if (link.equals(nodeText)) {
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
		boolean indentationUsesTabsInTextOutput = ResourceController.getResourceController().getBooleanProperty("indentationUsesTabsInTextOutput");
		String indentation = indentationUsesTabsInTextOutput ? "\t" : "    ";
		writeTXT(mindMapNodeModel, fileout, depth, indentation);
	}

	private void writeTXT(final NodeModel mindMapNodeModel, final Writer fileout, final int depth, String indentation) throws IOException {
		String plainTextContent = TextController.getController().getPlainTransformedText(mindMapNodeModel).replace('\n', ' ');
		for (int i = 0; i < depth; ++i) {
			fileout.write(indentation);
		}
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
		fileout.write("\n");
		writeChildrenText(mindMapNodeModel, fileout, depth, indentation);
	}
}
