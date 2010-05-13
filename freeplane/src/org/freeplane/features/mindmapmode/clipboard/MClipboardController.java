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
package org.freeplane.features.mindmapmode.clipboard;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.ElementIterator;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import org.apache.commons.lang.StringUtils;
import org.freeplane.core.controller.Controller;
import org.freeplane.core.io.MapReader;
import org.freeplane.core.io.MapReader.NodeTreeCreator;
import org.freeplane.core.io.MapWriter.Hint;
import org.freeplane.core.io.MapWriter.Mode;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.MapModel;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.FixedHTMLWriter;
import org.freeplane.core.util.HtmlTools;
import org.freeplane.core.util.LogTool;
import org.freeplane.features.common.attribute.AttributeController;
import org.freeplane.features.common.clipboard.ClipboardController;
import org.freeplane.features.common.clipboard.MindMapNodesSelection;
import org.freeplane.features.common.link.LinkController;
import org.freeplane.features.common.text.TextController;
import org.freeplane.features.mindmapmode.MMapController;
import org.freeplane.features.mindmapmode.MModeController;
import org.freeplane.features.mindmapmode.link.MLinkController;
import org.freeplane.features.mindmapmode.text.MTextController;
import org.freeplane.n3.nanoxml.XMLException;

/**
 * @author Dimitry Polivaev
 */
public class MClipboardController extends ClipboardController {
	private class DirectHtmlFlavorHandler implements IDataFlavorHandler {
		private String textFromClipboard;

		public DirectHtmlFlavorHandler(final String textFromClipboard) {
			this.textFromClipboard = textFromClipboard;
		}

		void paste(final NodeModel target) {
			textFromClipboard = cleanHtml(textFromClipboard);
			final NodeModel node = getModeController().getMapController().newNode(textFromClipboard,
			    getController().getMap());
			final String text = textFromClipboard;
			final Matcher m = HREF_PATTERN.matcher(text);
			if (m.matches()) {
				final String body = m.group(2);
				if (!body.matches(".*<\\s*a.*")) {
					final String href = m.group(1);
					final boolean useRelativeUri = ResourceController.getResourceController().getProperty("links")
					    .equals("relative");
					((MLinkController) LinkController.getController(getModeController())).setLink(node, href,
					    useRelativeUri);
				}
			}
			((MMapController) getModeController().getMapController()).insertNode(node, target);
		}

		public void paste(final NodeModel target, final boolean asSibling, final boolean isLeft) {
			paste(target);
		}
	}

	private class FileListFlavorHandler implements IDataFlavorHandler {
		final List<File> fileList;

		public FileListFlavorHandler(final List<File> fileList) {
			super();
			this.fileList = fileList;
		}

		public void paste(final NodeModel target, final boolean asSibling, final boolean isLeft) {
			for (final File file : fileList) {
				final MMapController mapController = (MMapController) getModeController().getMapController();
				final NodeModel node = mapController.newNode(file.getName(), target.getMap());
				final URI uri;
				if (ResourceController.getResourceController().getProperty("links").equals("relative")) {
					uri = LinkController.toRelativeURI(node.getMap().getFile(), file);
				}
				else {
					uri = file.getAbsoluteFile().toURI();
				}
				((MLinkController) LinkController.getController(getModeController())).setLink(node, uri, false);
				mapController.insertNode(node, target, asSibling, isLeft, isLeft);
			}
		}
	}

	interface IDataFlavorHandler {
		void paste(NodeModel target, boolean asSibling, boolean isLeft);
	}

	private class MindMapNodesFlavorHandler implements IDataFlavorHandler {
		private final String textFromClipboard;

		public MindMapNodesFlavorHandler(final String textFromClipboard) {
			this.textFromClipboard = textFromClipboard;
		}

		public void paste(final NodeModel target, final boolean asSibling, final boolean isLeft) {
			if (textFromClipboard != null) {
				paste(textFromClipboard, target, asSibling, isLeft);
			}
		}

		private void paste(final String text, final NodeModel target, final boolean asSibling, final boolean isLeft) {
			final String[] textLines = text.split(ClipboardController.NODESEPARATOR);
			final MMapController mapController = (MMapController) getModeController().getMapController();
			final MapReader mapReader = mapController.getMapReader();
			final NodeTreeCreator nodeTreeCreator = mapReader.nodeTreeCreator(target.getMap());
			mapReader.setHint(Hint.MODE, Mode.CLIPBOARD);
			for (int i = 0; i < textLines.length; ++i) {
				try {
					final NodeModel newModel = nodeTreeCreator.create(new StringReader(textLines[i]));
					final boolean wasLeft = newModel.isLeft();
					mapController.insertNode(newModel, target, asSibling, isLeft, wasLeft != isLeft);
				}
				catch (final XMLException e) {
					LogTool.severe("error on paste", e);
				}
			}
			nodeTreeCreator.finish(target);
		}
	}

	private static class PasteHtmlWriter extends FixedHTMLWriter {
		private final Element element;

		public PasteHtmlWriter(final Writer writer, final Element element, final HTMLDocument doc, final int pos,
		                       final int len) {
			super(writer, doc, pos, len);
			this.element = getStandAloneElement(element);
		}

		@Override
		protected ElementIterator getElementIterator() {
			return new ElementIterator(element);
		}

		private Element getStandAloneElement(final Element element) {
			final String name = element.getName();
			if (name.equals("ul") || name.equals("ol") || name.equals("table") || name.equals("html")) {
				return element;
			}
			return getStandAloneElement(element.getParentElement());
		}

		@Override
		public void write() throws IOException, BadLocationException {
			if (element.getName().equals("html")) {
				super.write();
				return;
			}
			write("<html>");
			super.write();
			write("</html>");
		}
	}

	private class StringFlavorHandler implements IDataFlavorHandler {
		private final String textFromClipboard;

		public StringFlavorHandler(final String textFromClipboard) {
			this.textFromClipboard = textFromClipboard;
		}

		public void paste(final NodeModel target, final boolean asSibling, final boolean isLeft) {
			final TextFragment[] textFragments = split(textFromClipboard);
			pasteStringWithoutRedisplay(textFragments, target, asSibling, isLeft);
		}

		private TextFragment[] split(final String textFromClipboard) {
			final LinkedList<TextFragment> textFragments = new LinkedList<TextFragment>();
			final String[] textLines = textFromClipboard.split("\n");
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
				final String visibleText = text.trim();
				//				if (visibleText.matches("^http://(www\\.)?[^ ]*$")) {
				//					visibleText = visibleText.replaceAll("^http://(www\\.)?", "").replaceAll("(/|\\.[^\\./\\?]*)$", "")
				//					    .replaceAll("((\\.[^\\./]*\\?)|\\?)[^/]*$", " ? ...").replaceAll("_|%20", " ");
				//					final String[] textParts = visibleText.split("/");
				//					visibleText = "";
				//					for (int textPartIdx = 0; textPartIdx < textParts.length; textPartIdx++) {
				//						if (textPartIdx > 0) {
				//							visibleText += " > ";
				//						}
				//						visibleText += textPartIdx == 0 ? textParts[textPartIdx] : PasteAction
				//						    .firstLetterCapitalized(textParts[textPartIdx].replaceAll("^~*", ""));
				//					}
				//				}
				final MLinkController linkController = (MLinkController) LinkController
				    .getController(getModeController());
				final String link = linkController.findLink(text);
				if (!visibleText.equals("")) {
					textFragments.add(new TextFragment(visibleText, link, depth));
				}
			}
			return textFragments.toArray(new TextFragment[textFragments.size()]);
		}
	}

	private class StructuredHtmlFlavorHandler implements IDataFlavorHandler {
		private final String textFromClipboard;

		public StructuredHtmlFlavorHandler(final String textFromClipboard) {
			this.textFromClipboard = textFromClipboard;
		}

		private String addFragment(final HTMLDocument doc, final Element element, final int depth, final int start,
		                           final int end, final LinkedList<TextFragment> htmlFragments)
		        throws BadLocationException, IOException {
			final String paragraphText = doc.getText(start, end - start).trim();
			if (paragraphText.length() > 0) {
				final StringWriter out = new StringWriter();
				new PasteHtmlWriter(out, element, doc, start, end - start).write();
				final String string = out.toString();
				if (!string.equals("")) {
					final MLinkController linkController = (MLinkController) LinkController
					    .getController(getModeController());
					final String link = linkController.findLink(string);
					final TextFragment htmlFragment = new TextFragment(string, link, depth);
					htmlFragments.add(htmlFragment);
				}
			}
			return paragraphText;
		}

		private Element getParentElement(final HTMLDocument doc) {
			final Element htmlRoot = doc.getDefaultRootElement();
			final Element bodyElement = htmlRoot.getElement(htmlRoot.getElementCount() - 1);
			Element parentCandidate = bodyElement;
			do {
				if (parentCandidate.getElementCount() > 1) {
					return parentCandidate;
				}
				parentCandidate = parentCandidate.getElement(0);
			} while (!(parentCandidate.isLeaf() || parentCandidate.getName().equalsIgnoreCase("p-implied")));
			return bodyElement;
		}

		private boolean isSeparateElement(final Element current) {
			return !current.isLeaf();
		}

		public void paste(final NodeModel target, final boolean asSibling, final boolean isLeft) {
			pasteHtmlWithoutRedisplay(textFromClipboard, target, asSibling, isLeft);
		}

		private void pasteHtmlWithoutRedisplay(final Object t, final NodeModel parent, final boolean asSibling,
		                                       final boolean isLeft) {
			String textFromClipboard = (String) t;
			textFromClipboard = cleanHtml(textFromClipboard);
			final TextFragment[] htmlFragments = split(textFromClipboard);
			pasteStringWithoutRedisplay(htmlFragments, parent, asSibling, isLeft);
		}

		private void split(final HTMLDocument doc, final Element parent, final LinkedList<TextFragment> htmlFragments,
		                   int depth) throws BadLocationException, IOException {
			final int elementCount = parent.getElementCount();
			int headerDepth = 0;
			boolean headerFound = false;
			int start = -1;
			int end = -1;
			Element last = null;
			for (int i = 0; i < elementCount; i++) {
				final Element current = parent.getElement(i);
				final String name = current.getName();
				final Matcher matcher = HEADER_REGEX.matcher(name);
				if (matcher.matches()) {
					try {
						if (!headerFound) {
							depth--;
						}
						final int newHeaderDepth = Integer.parseInt(matcher.group(1));
						depth += newHeaderDepth - headerDepth;
						headerDepth = newHeaderDepth;
						headerFound = true;
					}
					catch (final NumberFormatException e) {
						LogTool.severe(e);
					}
				}
				else {
					if (headerFound) {
						headerFound = false;
						depth++;
					}
				}
				final boolean separateElement = isSeparateElement(current);
				if (separateElement && current.getElementCount() != 0) {
					start = -1;
					last = null;
					split(doc, current, htmlFragments, depth + 1);
					continue;
				}
				if (separateElement && start != -1) {
					addFragment(doc, last, depth, start, end, htmlFragments);
				}
				if (start == -1 || separateElement) {
					start = current.getStartOffset();
					last = current;
				}
				end = current.getEndOffset();
				if (separateElement) {
					addFragment(doc, current, depth, start, end, htmlFragments);
				}
			}
			if (start != -1) {
				addFragment(doc, last, depth, start, end, htmlFragments);
			}
		}

		private TextFragment[] split(final String text) {
			final LinkedList<TextFragment> htmlFragments = new LinkedList<TextFragment>();
			final HTMLEditorKit kit = new HTMLEditorKit();
			final HTMLDocument doc = new HTMLDocument();
			final StringReader buf = new StringReader(text);
			try {
				kit.read(buf, doc, 0);
				final Element parent = getParentElement(doc);
				split(doc, parent, htmlFragments, 0);
			}
			catch (final IOException e) {
				LogTool.severe(e);
			}
			catch (final BadLocationException e) {
				LogTool.severe(e);
			}
			return htmlFragments.toArray(new TextFragment[htmlFragments.size()]);
		}
	}

	private static class TextFragment {
		int depth;
		String link;
		String text;

		public TextFragment(final String text, final String link, final int depth) {
			super();
			this.text = text;
			this.depth = depth;
			this.link = link;
		}
	}

	private static final Pattern HEADER_REGEX = Pattern.compile("h(\\d)", Pattern.CASE_INSENSITIVE);
	private static final Pattern HREF_PATTERN = Pattern
	    .compile("<html>\\s*<body>\\s*<a\\s+href=\"([^>]+)\">(.*)</a>\\s*</body>\\s*</html>");
	private static final String RESOURCE_UNFOLD_ON_PASTE = "unfold_on_paste";
	public static final String RESOURCES_CUT_NODES_WITHOUT_QUESTION = "cut_nodes_without_question";
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static String firstLetterCapitalized(final String text) {
		if (text == null || text.length() == 0) {
			return text;
		}
		return text.substring(0, 1).toUpperCase() + text.substring(1, text.length());
	}

	private List newNodes;

	/**
	 * @param modeController
	 */
	public MClipboardController(final MModeController modeController) {
		super(modeController);
		createActions(modeController);
	}

	private String cleanHtml(String in) {
		in = in.replaceFirst("(?i)(?s)<head>.*</head>", "").replaceFirst("(?i)(?s)^.*<html[^>]*>", "<html>")
		    .replaceFirst("(?i)(?s)<body [^>]*>", "<body>").replaceAll("(?i)(?s)<script.*?>.*?</script>", "")
		    .replaceAll("(?i)(?s)</?tbody.*?>", "").replaceAll("(?i)(?s)<!--.*?-->", "").replaceAll(
		        "(?i)(?s)</?o[^>]*>", "");
		if (StringUtils.equals(ResourceController.getResourceController().getProperty(
		    "cut_out_pictures_when_pasting_html"), "true")) {
			in = in.replaceAll("(?i)(?s)<img[^>]*>", "");
		}
		in = HtmlTools.unescapeHTMLUnicodeEntity(in);
		return in;
	}

	/**
	 * @param modeController
	 */
	private void createActions(final ModeController modeController) {
		final Controller controller = modeController.getController();
		modeController.addAction(new CutAction(controller));
		modeController.addAction(new PasteAction(controller));
		modeController.addAction(new SelectedPasteAction(controller));
	}

	Transferable cut(final List<NodeModel> collection) {
		getModeController().getMapController().sortNodesByDepth(collection);
		final Transferable totalCopy = ((ClipboardController) getModeController().getExtension(
		    ClipboardController.class)).copy(collection, true);
		for (final Iterator i = collection.iterator(); i.hasNext();) {
			final NodeModel node = (NodeModel) i.next();
			if (node.getParentNode() != null) {
				((MMapController) getModeController().getMapController()).deleteNode(node);
			}
		}
		setClipboardContents(totalCopy);
		return totalCopy;
	}

	private Controller getController() {
		return getModeController().getController();
	}

	/**
	 * @param t 
	 */
	private IDataFlavorHandler getFlavorHandler(final Transferable t) {
		if (t.isDataFlavorSupported(MindMapNodesSelection.mindMapNodesFlavor)) {
			try {
				final String textFromClipboard = t.getTransferData(MindMapNodesSelection.mindMapNodesFlavor).toString();
				return new MindMapNodesFlavorHandler(textFromClipboard);
			}
			catch (final UnsupportedFlavorException e) {
			}
			catch (final IOException e) {
			}
		}
		if (t.isDataFlavorSupported(MindMapNodesSelection.fileListFlavor)) {
			try {
				final List<File> fileList = (List<File>) t.getTransferData(MindMapNodesSelection.fileListFlavor);
				return new FileListFlavorHandler(fileList);
			}
			catch (final UnsupportedFlavorException e) {
			}
			catch (final IOException e) {
			}
		}
		final ResourceController resourceController = ResourceController.getResourceController();
		if (t.isDataFlavorSupported(MindMapNodesSelection.htmlFlavor)) {
			try {
				final String textFromClipboard = t.getTransferData(MindMapNodesSelection.htmlFlavor).toString();
				if (textFromClipboard.charAt(0) != 65533) {
					if (t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
						final MTextController textController = (MTextController) TextController
						    .getController(getModeController());
						final boolean richText = textController.useRichTextInNewLongNodes();
						if (richText) {
							final boolean structuredHtmlImport = resourceController
							    .getBooleanProperty("structured_html_import");
							final IDataFlavorHandler htmlFlavorHandler;
							if (structuredHtmlImport) {
								htmlFlavorHandler = new StructuredHtmlFlavorHandler(textFromClipboard);
							}
							else {
								htmlFlavorHandler = new DirectHtmlFlavorHandler(textFromClipboard);
							}
							return htmlFlavorHandler;
						}
					}
				}
			}
			catch (final UnsupportedFlavorException e) {
			}
			catch (final IOException e) {
			}
		}
		if (t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
			try {
				final String plainTextFromClipboard = t.getTransferData(DataFlavor.stringFlavor).toString();
				return new StringFlavorHandler(plainTextFromClipboard);
			}
			catch (final UnsupportedFlavorException e) {
			}
			catch (final IOException e) {
			}
		}
		return null;
	}

	Collection<IDataFlavorHandler> getFlavorHandlers() {
		final Transferable t = getClipboardContents();
		final Collection<IDataFlavorHandler> handlerList = new LinkedList<IDataFlavorHandler>();
		if (t.isDataFlavorSupported(MindMapNodesSelection.mindMapNodesFlavor)) {
			try {
				final String textFromClipboard = t.getTransferData(MindMapNodesSelection.mindMapNodesFlavor).toString();
				handlerList.add(new MindMapNodesFlavorHandler(textFromClipboard));
			}
			catch (final UnsupportedFlavorException e) {
			}
			catch (final IOException e) {
			}
		}
		if (t.isDataFlavorSupported(MindMapNodesSelection.htmlFlavor)) {
			try {
				final String textFromClipboard = t.getTransferData(MindMapNodesSelection.htmlFlavor).toString();
				if (textFromClipboard.charAt(0) != 65533) {
					if (t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
						handlerList.add(new StructuredHtmlFlavorHandler(textFromClipboard));
						handlerList.add(new DirectHtmlFlavorHandler(textFromClipboard));
					}
				}
			}
			catch (final UnsupportedFlavorException e) {
			}
			catch (final IOException e) {
			}
		}
		if (t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
			try {
				final String plainTextFromClipboard = t.getTransferData(DataFlavor.stringFlavor).toString();
				handlerList.add(new StringFlavorHandler(plainTextFromClipboard));
			}
			catch (final UnsupportedFlavorException e) {
			}
			catch (final IOException e) {
			}
		}
		if (t.isDataFlavorSupported(MindMapNodesSelection.fileListFlavor)) {
			try {
				final List<File> fileList = (List<File>) t.getTransferData(MindMapNodesSelection.fileListFlavor);
				handlerList.add(new FileListFlavorHandler(fileList));
			}
			catch (final UnsupportedFlavorException e) {
			}
			catch (final IOException e) {
			}
		}
		return handlerList;
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
		/*
		 * DataFlavor[] fl = t.getTransferDataFlavors(); for (int i = 0; i <
		 * fl.length; i++) { System.out.println(fl[i]); }
		 */
		final IDataFlavorHandler handler = getFlavorHandler(t);
		paste(handler, target, asSibling, isLeft);
	}

	void paste(final IDataFlavorHandler handler, final NodeModel target, final boolean asSibling, final boolean isLeft) {
		if (handler == null) {
			return;
		}
		final MMapController mapController = (MMapController) getModeController().getMapController();
		if (asSibling && !mapController.isWriteable(target.getParentNode()) || !asSibling
		        && !mapController.isWriteable(target)) {
			final String message = ResourceBundles.getText("node_is_write_protected");
			UITools.errorMessage(message);
			return;
		}
		try {
			getController().getViewController().setWaitingCursor(true);
			if (newNodes == null) {
				newNodes = new LinkedList();
			}
			newNodes.clear();
			handler.paste(target, asSibling, isLeft);
			final ModeController modeController = getModeController();
			if (!asSibling && modeController.getMapController().isFolded(target)
			        && ResourceController.getResourceController().getBooleanProperty(RESOURCE_UNFOLD_ON_PASTE)) {
				modeController.getMapController().setFolded(target, false);
			}
			for (final ListIterator e = newNodes.listIterator(); e.hasNext();) {
				final NodeModel child = (NodeModel) e.next();
				AttributeController.getController(getModeController()).performRegistrySubtreeAttributes(child);
			}
		}
		finally {
			getController().getViewController().setWaitingCursor(false);
		}
	}

	private void pasteStringWithoutRedisplay(final TextFragment[] textFragments, NodeModel parent,
	                                              final boolean asSibling, final boolean isLeft) {
		final MapModel map = parent.getMap();
		int insertionIndex;
		if (asSibling) {
			NodeModel target = parent;
			parent = parent.getParentNode();
			insertionIndex = parent.getChildPosition(target);
		}
		else{
			insertionIndex = parent.getChildCount();
		}
		final ArrayList<NodeModel> parentNodes = new ArrayList<NodeModel>();
		final ArrayList<Integer> parentNodesDepths = new ArrayList<Integer>();
		parentNodes.add(parent);
		parentNodesDepths.add(new Integer(-1));
		final MMapController mapController = (MMapController) getModeController().getMapController();
		final boolean useRelativeUri = ResourceController.getResourceController().getProperty("links").equals(
		    "relative");
		for (int i = 0; i < textFragments.length; ++i) {
			final TextFragment textFragment = textFragments[i];
			final String text = textFragment.text;
			final NodeModel node = mapController.newNode(text, map);
			if (textFragment.link != null) {
				((MLinkController) LinkController.getController(getModeController())).setLink(node, textFragment.link,
				    useRelativeUri);
			}
			for (int j = parentNodes.size() - 1; j >= 0; --j) {
				if (textFragment.depth > ((Integer) parentNodesDepths.get(j)).intValue()) {
					for (int k = j + 1; k < parentNodes.size(); ++k) {
						final NodeModel n = (NodeModel) parentNodes.get(k);
						if (n.getParentNode() == null) {
							mapController.insertNode(n, parent, insertionIndex++);
						}
						parentNodes.remove(k);
						parentNodesDepths.remove(k);
					}
					final NodeModel target = (NodeModel) parentNodes.get(j);
					node.setLeft(isLeft);
					if (target != parent) {
						target.setFolded(true);
						target.insert(node, target.getChildCount());
					}
					parentNodes.add(node);
					parentNodesDepths.add(new Integer(textFragment.depth));
					break;
				}
			}
		}
		{
			for (int k = 0; k < parentNodes.size(); ++k) {
				final NodeModel n = (NodeModel) parentNodes.get(k);
				if (map.getRootNode() != n && n.getParentNode() == null) {
					mapController.insertNode(n, parent, insertionIndex++);
				}
			}
		}
	}
}
