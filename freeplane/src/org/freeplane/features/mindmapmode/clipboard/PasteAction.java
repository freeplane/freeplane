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
package org.freeplane.features.mindmapmode.clipboard;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.freeplane.core.controller.Controller;
import org.freeplane.core.enums.ResourceControllerProperties;
import org.freeplane.core.extension.ControllerUtil;
import org.freeplane.core.io.MapReader;
import org.freeplane.core.io.MapReader.NodeTreeCreator;
import org.freeplane.core.model.MapModel;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.undo.IUndoableActor;
import org.freeplane.core.util.HtmlTools;
import org.freeplane.core.util.LogTool;
import org.freeplane.features.common.attribute.AttributeController;
import org.freeplane.features.common.clipboard.ClipboardController;
import org.freeplane.features.common.clipboard.MindMapNodesSelection;
import org.freeplane.features.common.link.LinkController;
import org.freeplane.features.mindmapmode.MMapController;
import org.freeplane.features.mindmapmode.link.MLinkController;

class PasteAction extends AFreeplaneAction {
	private interface DataFlavorHandler {
		DataFlavor getDataFlavor();

		void paste(Object TransferData, NodeModel target, boolean asSibling, boolean isLeft, Transferable t)
		        throws UnsupportedFlavorException, IOException;
	}

	private class DirectHtmlFlavorHandler implements DataFlavorHandler {
		public DataFlavor getDataFlavor() {
			return MindMapNodesSelection.htmlFlavor;
		}

		public void paste(final Object transferData, final NodeModel target, final boolean asSibling,
		                  final boolean isLeft, final Transferable t) throws UnsupportedFlavorException, IOException {
			String textFromClipboard = (String) transferData;
			if (textFromClipboard.charAt(0) == 65533) {
				throw new UnsupportedFlavorException(MindMapNodesSelection.htmlFlavor);
			}
			getController().getViewController().setWaitingCursor(true);
			textFromClipboard = textFromClipboard.replaceFirst("(?i)(?s)<head>.*</head>", "").replaceFirst(
			    "(?i)(?s)^.*<html[^>]*>", "<html>").replaceFirst("(?i)(?s)<body [^>]*>", "<body>").replaceAll(
			    "(?i)(?s)<script.*?>.*?</script>", "").replaceAll("(?i)(?s)</?tbody.*?>", "").replaceAll(
			    "(?i)(?s)<!--.*?-->", "").replaceAll("(?i)(?s)</?o[^>]*>", "");
			if (StringUtils.equals(ResourceController.getResourceController().getProperty("cut_out_pictures_when_pasting_html"),
			    "true")) {
				textFromClipboard = textFromClipboard.replaceAll("(?i)(?s)<img[^>]*>", "");
			}
			textFromClipboard = HtmlTools.unescapeHTMLUnicodeEntity(textFromClipboard);
			final NodeModel node = getModeController().getMapController().newNode(textFromClipboard,
			    getController().getMap());
			final Matcher m = PasteAction.HREF_PATTERN.matcher(textFromClipboard);
			if (m.matches()) {
				final String body = m.group(2);
				if (!body.matches(".*<\\s*a.*")) {
					final String href = m.group(1);
					((MLinkController) LinkController.getController(node.getModeController())).setLink(node, href);
				}
			}
			PasteAction.this.paste(node, target, target.getChildCount());
			getController().getViewController().setWaitingCursor(false);
		}
	}

	private class FileListFlavorHandler implements DataFlavorHandler {
		public DataFlavor getDataFlavor() {
			return MindMapNodesSelection.fileListFlavor;
		}

		public void paste(final Object TransferData, final NodeModel target, final boolean asSibling,
		                  final boolean isLeft, final Transferable t) {
			final List fileList = (List) TransferData;
			for (final ListIterator it = fileList.listIterator(); it.hasNext();) {
				final File file = (File) it.next();
				final NodeModel node = getModeController().getMapController().newNode(file.getName(), target.getMap());
				node.setLeft(isLeft);
				((MLinkController) LinkController.getController(node.getModeController())).setLink(node, file
				    .getAbsolutePath());
				PasteAction.this.paste(node, target, asSibling, isLeft, false);
			}
		}
	}

	private class MindMapNodesFlavorHandler implements DataFlavorHandler {
		public DataFlavor getDataFlavor() {
			return MindMapNodesSelection.mindMapNodesFlavor;
		}

		public void paste(final Object TransferData, final NodeModel target, final boolean asSibling,
		                  final boolean isLeft, final Transferable t) {
			final String textFromClipboard = (String) TransferData;
			if (textFromClipboard != null) {
				final String[] textLines = textFromClipboard.split(ResourceControllerProperties.NODESEPARATOR);
				if (textLines.length > 1) {
					getController().getViewController().setWaitingCursor(true);
				}
				final MapReader mapController = getModeController().getMapController().getMapReader();
				final NodeTreeCreator nodeTreeCreator = mapController.nodeTreeCreator(target.getMap());
				for (int i = 0; i < textLines.length; ++i) {
					final NodeModel newModel = nodeTreeCreator.create(new StringReader(textLines[i]));
					newModel.setLeft(isLeft);
					PasteAction.this.paste(newModel, target, target.getChildCount());
				}
				nodeTreeCreator.finish(target);
			}
		}
	}

	private class StringFlavorHandler implements DataFlavorHandler {
		public DataFlavor getDataFlavor() {
			return DataFlavor.stringFlavor;
		}

		public void paste(final Object TransferData, final NodeModel target, final boolean asSibling,
		                  final boolean isLeft, final Transferable t) throws UnsupportedFlavorException, IOException {
			pasteStringWithoutRedisplay(t, target, asSibling, isLeft);
		}
	}

	private static final Pattern HREF_PATTERN = Pattern
	    .compile("<html>\\s*<body>\\s*<a\\s+href=\"([^>]+)\">(.*)</a>\\s*</body>\\s*</html>");
	static private Pattern mailPattern;
	static final Pattern nonLinkCharacter = Pattern.compile("[ \n()'\",;]");

	public static String firstLetterCapitalized(final String text) {
		if (text == null || text.length() == 0) {
			return text;
		}
		return text.substring(0, 1).toUpperCase() + text.substring(1, text.length());
	}

	private List newNodes;

	public PasteAction(final Controller controller) {
		super(controller, "paste", "/images/editpaste.png");
	}

	public void actionPerformed(final ActionEvent e) {
//		final MClipboardController clipboardController = (MClipboardController) ControllerUtil
//	    .getController(getModeController(),ClipboardController.class);
		final MClipboardController clipboardController = (MClipboardController)getModeController().getExtension(ClipboardController.class);

		clipboardController.paste(clipboardController.getClipboardContents(), getController().getSelection()
		    .getSelected());
	}

	/**
	 */
	private DataFlavorHandler[] getFlavorHandlers() {
		final DataFlavorHandler[] dataFlavorHandlerList = new DataFlavorHandler[] { new FileListFlavorHandler(),
		        new MindMapNodesFlavorHandler(), new DirectHtmlFlavorHandler(), new StringFlavorHandler() };
		return dataFlavorHandlerList;
	}

	public void paste(final NodeModel node, final NodeModel parent) {
		paste(node, parent, parent.getChildCount());
	}

	private void paste(final NodeModel node, final NodeModel target, final boolean asSibling, final boolean isLeft,
	                   final boolean changeSide) {
		NodeModel parent;
		if (asSibling) {
			parent = target.getParentNode();
		}
		else {
			parent = target;
		}
		if (changeSide) {
			node.setParent(parent);
			node.setLeft(isLeft);
		}
		if (asSibling) {
			paste(node, parent, parent.getChildPosition(target));
		}
		else {
			paste(node, parent, parent.getChildCount());
		}
	}

	public void paste(final NodeModel node, final NodeModel parentNode, final int index) {
		final IUndoableActor actor = new IUndoableActor() {
			public void act() {
				(getModeController().getMapController()).insertNodeIntoWithoutUndo(node, parentNode, index);
			}

			public String getDescription() {
				return "paste";
			}

			public void undo() {
				((MMapController) getModeController().getMapController()).deleteWithoutUndo(node);
			}
		};
		getModeController().execute(actor);
	}

	/**
	 * @param t
	 *            the content
	 * @param target
	 *            where to add the content
	 * @param asSibling
	 *            if true, the content is added beside the target, otherwise as
	 *            new children
	 * @param isLeft
	 *            if something is pasted as a sibling to root, it must be
	 *            decided on which side of root
	 * @return true, if successfully executed.
	 */
	public void paste(final Transferable t, final NodeModel target, final boolean asSibling, final boolean isLeft) {
		if (t == null) {
			return;
		}
		try {
			/*
			 * DataFlavor[] fl = t.getTransferDataFlavors(); for (int i = 0; i <
			 * fl.length; i++) { System.out.println(fl[i]); }
			 */
			if (newNodes == null) {
				newNodes = new LinkedList();
			}
			newNodes.clear();
			final DataFlavorHandler[] dataFlavorHandlerList = getFlavorHandlers();
			for (int i = 0; i < dataFlavorHandlerList.length; i++) {
				final DataFlavorHandler handler = dataFlavorHandlerList[i];
				final DataFlavor flavor = handler.getDataFlavor();
				if (t.isDataFlavorSupported(flavor)) {
					try {
						handler.paste(t.getTransferData(flavor), target, asSibling, isLeft, t);
						break;
					}
					catch (final UnsupportedFlavorException e) {
					}
				}
			}
			for (final ListIterator e = newNodes.listIterator(); e.hasNext();) {
				final NodeModel child = (NodeModel) e.next();
				AttributeController.getController(getModeController()).performRegistrySubtreeAttributes(child);
			}
		}
		catch (final IOException e) {
			LogTool.logException(e);
		}
		finally {
			getController().getViewController().setWaitingCursor(false);
		}
	}

	/**
	 * Paste String (as opposed to other flavours) Split the text into lines;
	 * determine the new tree structure by the number of leading spaces in
	 * lines. In case that trimmed line starts with protocol (http:, https:,
	 * ftp:), create a link with the same content. If there was only one line to
	 * be pasted, return the pasted node, null otherwise.
	 *
	 * @param isLeft
	 */
	private NodeModel pasteStringWithoutRedisplay(final Transferable t, NodeModel parent, final boolean asSibling,
	                                              final boolean isLeft) throws UnsupportedFlavorException, IOException {
		final String textFromClipboard = (String) t.getTransferData(DataFlavor.stringFlavor);
		if (mailPattern == null) {
			mailPattern = Pattern.compile("([^@ <>\\*']+@[^@ <>\\*']+)");
		}
		final String[] textLines = textFromClipboard.split("\n");
		if (textLines.length > 1) {
			getController().getViewController().setWaitingCursor(true);
		}
		final MapModel map = parent.getMap();
		if (asSibling) {
			parent = new NodeModel(map);
		}
		final ArrayList parentNodes = new ArrayList();
		final ArrayList parentNodesDepths = new ArrayList();
		parentNodes.add(parent);
		parentNodesDepths.add(new Integer(-1));
		final String[] linkPrefixes = { "http://", "ftp://", "https://" };
		NodeModel pastedNode = null;
		for (int i = 0; i < textLines.length; ++i) {
			String text = textLines[i];
			text = text.replaceAll("\t", "        ");
			if (text.matches(" *")) {
				continue;
			}
			int depth = 0;
			while (depth < text.length() && text.charAt(depth) == ' ') {
				++depth;
			}
			String visibleText = text.trim();
			if (visibleText.matches("^http://(www\\.)?[^ ]*$")) {
				visibleText = visibleText.replaceAll("^http://(www\\.)?", "").replaceAll("(/|\\.[^\\./\\?]*)$", "")
				    .replaceAll("((\\.[^\\./]*\\?)|\\?)[^/]*$", " ? ...").replaceAll("_|%20", " ");
				final String[] textParts = visibleText.split("/");
				visibleText = "";
				for (int textPartIdx = 0; textPartIdx < textParts.length; textPartIdx++) {
					if (textPartIdx > 0) {
						visibleText += " > ";
					}
					visibleText += textPartIdx == 0 ? textParts[textPartIdx] : PasteAction
					    .firstLetterCapitalized(textParts[textPartIdx].replaceAll("^~*", ""));
				}
			}
			final NodeModel node = getModeController().getMapController().newNode(visibleText, map);
			if (textLines.length == 1) {
				pastedNode = node;
			}
			final Matcher mailMatcher = mailPattern.matcher(visibleText);
			if (mailMatcher.find()) {
				((MLinkController) LinkController.getController(node.getModeController())).setLink(node,
				    ("mailto:" + mailMatcher.group()));
			}
			for (int j = 0; j < linkPrefixes.length; j++) {
				final int linkStart = text.indexOf(linkPrefixes[j]);
				if (linkStart != -1) {
					int linkEnd = linkStart;
					while (linkEnd < text.length()
					        && !PasteAction.nonLinkCharacter.matcher(text.substring(linkEnd, linkEnd + 1)).matches()) {
						linkEnd++;
					}
					((MLinkController) LinkController.getController(node.getModeController())).setLink(node, text
					    .substring(linkStart, linkEnd));
				}
			}
			for (int j = parentNodes.size() - 1; j >= 0; --j) {
				if (depth > ((Integer) parentNodesDepths.get(j)).intValue()) {
					for (int k = j + 1; k < parentNodes.size(); ++k) {
						final NodeModel n = (NodeModel) parentNodes.get(k);
						if (n.getParentNode() == null) {
							paste(n, parent, parent.getChildCount());
						}
						parentNodes.remove(k);
						parentNodesDepths.remove(k);
					}
					final NodeModel target = (NodeModel) parentNodes.get(j);
					node.setLeft(isLeft);
					if (target != parent) {
						target.insert(node, target.getChildCount());
					}
					parentNodes.add(node);
					parentNodesDepths.add(new Integer(depth));
					break;
				}
			}
		}
		for (int k = 0; k < parentNodes.size(); ++k) {
			final NodeModel n = (NodeModel) parentNodes.get(k);
			if (map.getRootNode() != n && n.getParentNode() == null) {
				paste(n, parent, parent.getChildCount());
			}
		}
		return pastedNode;
	}
}
