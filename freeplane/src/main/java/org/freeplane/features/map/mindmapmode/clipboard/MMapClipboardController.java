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
package org.freeplane.features.map.mindmapmode.clipboard;

import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.ElementIterator;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.ExampleFileFilter;
import org.freeplane.core.ui.components.OptionalDontShowMeAgainDialog;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.FileUtils;
import org.freeplane.core.util.FixedHTMLWriter;
import org.freeplane.core.util.HtmlUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.attribute.Attribute;
import org.freeplane.features.attribute.AttributeController;
import org.freeplane.features.attribute.NodeAttributeTableModel;
import org.freeplane.features.attribute.mindmapmode.MAttributeController;
import org.freeplane.features.clipboard.ClipboardAccessor;
import org.freeplane.features.clipboard.mindmapmode.MClipboardController;
import org.freeplane.features.format.ScannerController;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.link.NodeLinks;
import org.freeplane.features.link.mindmapmode.MLinkController;
import org.freeplane.features.map.CloneEncryptedNodeException;
import org.freeplane.features.map.FreeNode;
import org.freeplane.features.map.IMapSelection;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.MapReader;
import org.freeplane.features.map.MapReader.NodeTreeCreator;
import org.freeplane.features.map.MapWriter.Hint;
import org.freeplane.features.map.MapWriter.Mode;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.clipboard.MapClipboardController;
import org.freeplane.features.map.clipboard.MindMapNodesSelection;
import org.freeplane.features.map.mindmapmode.MMapController;
import org.freeplane.features.map.mindmapmode.SummaryGroupEdgeListAdder;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.text.TextController;
import org.freeplane.features.text.mindmapmode.MTextController;
import org.freeplane.features.url.UrlManager;
import org.freeplane.n3.nanoxml.XMLException;
import org.freeplane.view.swing.features.filepreview.ImageAdder;
import org.freeplane.view.swing.features.filepreview.ViewerController;
import org.freeplane.view.swing.features.filepreview.ViewerController.PasteMode;

/**
 * @author Dimitry Polivaev
 */
public class MMapClipboardController extends MapClipboardController implements MClipboardController{
	public static final String RESOURCES_REMIND_USE_RICH_TEXT_IN_NEW_NODES = "remind_use_rich_text_in_new_nodes";
	private class DirectHtmlFlavorHandler implements IDataFlavorHandler {
		private final String textFromClipboard;

		public DirectHtmlFlavorHandler(final String textFromClipboard) {
			this.textFromClipboard = textFromClipboard;
		}

		void paste(final NodeModel target) {
			final String text = cleanHtml(textFromClipboard);
			final NodeModel node = Controller.getCurrentModeController().getMapController().newNode(text,
					Controller.getCurrentController().getMap());
			((MMapController) Controller.getCurrentModeController().getMapController()).insertNode(node, target);
		}

		@Override
		public void paste(Transferable t, final NodeModel target, final boolean asSibling, final boolean isLeft, int dropAction) {
			paste(target);
		}
	}

	private class FileListFlavorHandler implements IDataFlavorHandler {
		final List<File> fileList;

		public FileListFlavorHandler(final List<File> fileList) {
			super();
			this.fileList = fileList;
		}

		@Override
		public void paste(Transferable t, final NodeModel target, final boolean asSibling, final boolean isLeft, int dropAction) {
			boolean copyFile = dropAction == DnDConstants.ACTION_COPY;
	        final File mapFile = target.getMap().getFile();
			if ((copyFile || LinkController.getLinkType() == LinkController.LINK_RELATIVE_TO_MINDMAP) && mapFile == null) {
	        	JOptionPane.showMessageDialog(Controller.getCurrentController().getViewController().getCurrentRootComponent(),
	        	    TextUtils.getText("map_not_saved"), "Freeplane", JOptionPane.WARNING_MESSAGE);
	        	return;
	        }
			ViewerController viewerController = (Controller.getCurrentModeController().getExtension(ViewerController.class));
			boolean pasteImagesFromFiles = ResourceController.getResourceController().getBooleanProperty("pasteImagesFromFiles");
			for (final File sourceFile : fileList) {
				final File file;
				if(copyFile){
					try {
						file = new TargetFileCreator().createTargetFile(mapFile, sourceFile.getName());
						file.getParentFile().mkdirs();
						FileUtils.copyFile(sourceFile, file);
					} catch (IOException e) {
						LogUtils.warn(e);
						continue;
					}
				}
				else
					file = sourceFile;
				if(! pasteImagesFromFiles || dropAction == DnDConstants.ACTION_LINK || !viewerController.paste(file, target, PasteMode.valueOf(asSibling), isLeft)) {
					final MMapController mapController = (MMapController) Controller.getCurrentModeController().getMapController();
					final NodeModel node = mapController.newNode(file.getName(), target.getMap());
					((MLinkController) LinkController.getController()).setLinkTypeDependantLink(node, file);
					mapController.insertNode(node, target, asSibling, isLeft, isLeft);
				}
			}
		}
	}

	interface IDataFlavorHandler {
		void paste(Transferable t, NodeModel target, boolean asSibling, boolean isLeft, int dropAction);
	}

	private class MindMapNodesFlavorHandler implements IDataFlavorHandler {
		private final String textFromClipboard;

		public MindMapNodesFlavorHandler(final String textFromClipboard) {
			this.textFromClipboard = textFromClipboard;
		}

		@Override
		public void paste(Transferable t, final NodeModel target, final boolean asSibling, final boolean isLeft, int dropAction) {
			if (textFromClipboard != null) {
				paste(textFromClipboard, target, asSibling, isLeft);
			}
		}

		private void paste(final String text, final NodeModel target, final boolean asSibling, final boolean isLeft) {
			final String[] textLines = text.split(MapClipboardController.NODESEPARATOR);
			final MMapController mapController = (MMapController) Controller.getCurrentModeController().getMapController();
			final MapReader mapReader = mapController.getMapReader();
			synchronized(mapReader) {
				final NodeTreeCreator nodeTreeCreator = mapReader.nodeTreeCreator(target.getMap());
				nodeTreeCreator.setHint(Hint.MODE, Mode.CLIPBOARD);
				for (int i = 0; i < textLines.length; ++i) {
					try {
						final NodeModel newModel = nodeTreeCreator.create(new StringReader(textLines[i]));
						newModel.removeExtension(FreeNode.class);
						final boolean wasLeft = newModel.isLeft();
						mapController.insertNode(newModel, target, asSibling, isLeft, wasLeft != isLeft);
					}
					catch (final XMLException e) {
						LogUtils.severe("error on paste", e);
					}
				}
				nodeTreeCreator.finish(target);
			}
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

	private static final Pattern ATTRIBUTE_REGEX = Pattern.compile("\\s*\\+\t(\\S[^\t]*)(?:\t(.*?))\\s*");
	private class StringFlavorHandler implements IDataFlavorHandler {
		private final String textFromClipboard;

		public StringFlavorHandler(final String textFromClipboard) {
			this.textFromClipboard = textFromClipboard;
		}

		@Override
		public void paste(Transferable t, final NodeModel target, final boolean asSibling, final boolean isLeft, int dropAction) {
			final TextFragment[] textFragments = split(textFromClipboard);
			pasteStringWithoutRedisplay(textFragments, target, asSibling, isLeft);
		}

		private TextFragment[] split(final String textFromClipboard) {
			final LinkedList<TextFragment> textFragments = new LinkedList<TextFragment>();
			final String[] textLines = textFromClipboard.split("\n");
			for (int i = 0; i < textLines.length; ++i) {
				String text = textLines[i];
				final Matcher matcher = ATTRIBUTE_REGEX.matcher(text);
				if(matcher.matches()) {
					textFragments.add(new TextFragment(matcher.group(1), matcher.group(2), TextFragment.ATTRIBUTE_DEPTH));
				}
				else {
					text = text.replaceAll("\t", "        ");
					if (text.matches(" *")) {
						continue;
					}
					int depth = 0;
					while (depth < text.length() && text.charAt(depth) == ' ') {
						++depth;
					}
					final String visibleText = text.trim();
					final String link = LinkController.findLink(text);
					if (!visibleText.equals("")) {
						textFragments.add(new TextFragment(visibleText, link, depth));
					}
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

		private void addFragment(final HTMLDocument doc, final Element element, final int depth, final int start,
		                           final int end, final LinkedList<TextFragment> htmlFragments)
		        throws BadLocationException, IOException {
			final String paragraphText = doc.getText(start, end - start).trim();
			if (paragraphText.length() > 0 || element.getName().equals("img")) {
				final StringWriter out = new StringWriter();
				new PasteHtmlWriter(out, element, doc, start, end - start).write();
				final String string = out.toString();
				if (!string.equals("")) {
					final String link = LinkController.findLink(string);
					final TextFragment htmlFragment = new TextFragment(string, link, depth);
					htmlFragments.add(htmlFragment);
				}
			}
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

		@Override
		public void paste(Transferable t, final NodeModel target, final boolean asSibling, final boolean isLeft, int dropAction) {
			pasteHtmlWithoutRedisplay(textFromClipboard, target, asSibling, isLeft);
		}

		private void pasteHtmlWithoutRedisplay(final Object t, final NodeModel parent, final boolean asSibling,
		                                       final boolean isLeft) {
			final String textFromClipboard = (String) t;
			final String cleanedTextFromClipboard = cleanHtml(textFromClipboard);
			final TextFragment[] htmlFragments = split(cleanedTextFromClipboard);
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
						LogUtils.severe(e);
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
				LogUtils.severe(e);
			}
			catch (final BadLocationException e) {
				LogUtils.severe(e);
			}
			return htmlFragments.toArray(new TextFragment[htmlFragments.size()]);
		}
	}

	private static class TextFragment {
		final static int ATTRIBUTE_DEPTH = -2;
		String text;
		String link;
		int depth;

		public TextFragment(final String text, final String link, final int depth) {
			super();
			this.text = text;
			this.link = link;
			this.depth = depth;
		}

		boolean isAttribute() {
			return depth == ATTRIBUTE_DEPTH;
		}

		boolean isNode() {
			return ! isAttribute();
		}

		@Override
		public String toString() {
			return "TextFragment [" + text + (link != null ? " [" + link  +  "]": "")  + "," + depth + "]";
		}


	}

	private class ImageFlavorHandler implements IDataFlavorHandler {

		final private Image image;

		public ImageFlavorHandler(Image img) {
			super();
			image = img;
		}

        @Override
		public void paste(Transferable t, NodeModel target, boolean asSibling, boolean isLeft, int dropAction) {
			final ModeController modeController = Controller.getCurrentModeController();
			final MMapController mapController = (MMapController) modeController.getMapController();
            File mindmapFile = target.getMap().getFile();
            if(mindmapFile == null) {
            	UITools.errorMessage(TextUtils.getRawText("map_not_saved"));
            	return;
            }
            //file that we'll save to disk.
            File imageFile;
            try {
            	imageFile = new TargetFileCreator().createTargetFile(mindmapFile, ImageAdder.IMAGE_FORMAT);
    			imageFile.getParentFile().mkdirs();
            	String imgfilepath=imageFile.getAbsolutePath();
            	File tempFile = imageFile = new File(imgfilepath);
            	final JFileChooser fileChooser = new JFileChooser(imageFile);
            	final ExampleFileFilter filter = new ExampleFileFilter();
            	filter.addExtension(ImageAdder.IMAGE_FORMAT);
            	fileChooser.setAcceptAllFileFilterUsed(false);
            	fileChooser.setFileFilter(filter);
            	fileChooser.setSelectedFile(imageFile);
            	int returnVal = fileChooser.showSaveDialog(UITools.getCurrentRootComponent());
            	if (returnVal != JFileChooser.APPROVE_OPTION) {
            		tempFile.delete();
            		return;
            	}
            	imageFile = fileChooser.getSelectedFile();
            	if(tempFile.exists() && ! imageFile.getAbsoluteFile().equals(tempFile)){
            		tempFile.delete();
            	}
            	if(imageFile.isDirectory())
            		return;
            	if(! FileUtils.getExtension(imageFile.getName()).equals(ImageAdder.IMAGE_FORMAT))
            		imageFile = new File(imageFile.getPath() + '.' + ImageAdder.IMAGE_FORMAT);
            	final NodeModel node = mapController.newNode(imageFile.getName(), target.getMap());
            	mapController.insertNode(node, target, asSibling, isLeft, isLeft);
            	new ImageAdder(image, mapController, mindmapFile, imageFile).attachImageToNode(node);
            }
            catch (IOException e) {
            	e.printStackTrace();
            }
        }

    }
	private static final Pattern HEADER_REGEX = Pattern.compile("h(\\d)", Pattern.CASE_INSENSITIVE);
	private static final String RESOURCE_UNFOLD_ON_PASTE = "unfold_on_paste";
	public static final String RESOURCES_CUT_NODES_WITHOUT_QUESTION = "cut_nodes_without_question";

	public static String firstLetterCapitalized(final String text) {
		if (text == null || text.length() == 0) {
			return text;
		}
		return text.substring(0, 1).toUpperCase() + text.substring(1, text.length());
	}

	private List<NodeModel> newNodes;

	/**
	 * @param modeController
	 */
	public MMapClipboardController() {
		super();
		createActions();
	}

	private String cleanHtml(String content) {
		content = content.replaceFirst("(?i)(?s)<head>.*</head>", "").replaceFirst("(?i)(?s)^.*<html[^>]*>", "<html>")
		    .replaceFirst("(?i)(?s)<body [^>]*>", "<body>").replaceAll("(?i)(?s)<script.*?>.*?</script>", "")
		    .replaceAll("(?i)(?s)</?tbody.*?>", "").replaceAll("(?i)(?s)<!--.*?-->", "").replaceAll(
		        "(?i)(?s)</?o[^>]*>", "");
		if (ResourceController.getResourceController().getBooleanProperty("cut_out_pictures_when_pasting_html")) {
			String contentWithoutImages = content.replaceAll("(?i)(?s)<img[^>]*>", "");
			final boolean contentContainsOnlyImages = HtmlUtils.htmlToPlain(contentWithoutImages).trim().isEmpty();
			if(! contentContainsOnlyImages) {
				content = contentWithoutImages;
			}
		}
		content = HtmlUtils.unescapeHTMLUnicodeEntity(content);
		return content;
	}

	/**
	 * @param modeController
	 */
	private void createActions() {
		final ModeController modeController = Controller.getCurrentModeController();
		modeController.addAction(new SelectedPasteAction());
		modeController.addAction(new CloneAction());
		modeController.addAction(new MoveAction());
	}

	@Override
    public Transferable copy(IMapSelection selection) {
	    final List<NodeModel> collection = selection.getSortedSelection(true);
		final MindMapNodesSelection transferable = copy(new SummaryGroupEdgeListAdder(collection).addSummaryEdgeNodes(), false);
		transferable.setNodeObjects(collection, false);
		return transferable;
    }



	@Override
	public Transferable copySingle(Collection<NodeModel> source) {
		final MindMapNodesSelection transferable = (MindMapNodesSelection) super.copySingle(source);
		transferable.setNodeObjects(new ArrayList<NodeModel>(source), true);
		return transferable;
	}

	Transferable cut(final List<NodeModel> collection) {
		Controller.getCurrentModeController().getMapController().sortNodesByDepth(collection);
		final MindMapNodesSelection transferable = copy(new SummaryGroupEdgeListAdder(collection).addSummaryEdgeNodes(), true);
		((MMapController) Controller.getCurrentModeController().getMapController()).deleteNodes(collection);
		setClipboardContents(transferable);
		return transferable;
	}

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
		final ResourceController resourceController = ResourceController.getResourceController();
		DataFlavor supportedHtmlFlavor = getSupportedHtmlFlavor(t);
		if (supportedHtmlFlavor != null) {
			try {
				final String textFromClipboard = t.getTransferData(supportedHtmlFlavor).toString();
				if (textFromClipboard.charAt(0) != 65533) {
					if (t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
						final MTextController textController = (MTextController) TextController
						    .getController();
						final boolean richText = textController.useRichTextInEditor(RESOURCES_REMIND_USE_RICH_TEXT_IN_NEW_NODES);
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
		if (t.isDataFlavorSupported(MindMapNodesSelection.fileListFlavor)) {
			try {
				final List<File> fileList = castToFileList(t.getTransferData(MindMapNodesSelection.fileListFlavor));
				if (!shouldIgnoreFileListFlavor(fileList))
					return new FileListFlavorHandler(fileList);
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
		if (t.isDataFlavorSupported(DataFlavor.imageFlavor)) {
			try {
				Image image = (Image) t.getTransferData(DataFlavor.imageFlavor);
				return new ImageFlavorHandler(image);
			}
			catch (final UnsupportedFlavorException e) {
			}
			catch (final IOException e) {
			}
		}
		return null;
	}

	private boolean shouldIgnoreFileListFlavor(final List<File> fileList) {
		if(fileList == null || fileList.isEmpty())
			return true;
		final File file = fileList.get(0);
		if(file.isDirectory())
			return false;
	    final String name = file.getName();
		return name.endsWith(".URL") || name.endsWith(".url");
    }

	@SuppressWarnings("unchecked")
    private List<File> castToFileList(Object transferData) {
	    return (List<File>) transferData;
    }

	Collection<IDataFlavorHandler> getFlavorHandlers() {
		final Transferable t = getClipboardContents();
		final Collection<IDataFlavorHandler> handlerList = new LinkedList<IDataFlavorHandler>();
		if (t == null) {
			return handlerList;
		}
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
		DataFlavor supportedHtmlFlavor = getSupportedHtmlFlavor(t);
		if (supportedHtmlFlavor != null) {
			try {
				final String textFromClipboard = t.getTransferData(supportedHtmlFlavor).toString();
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
				final List<File> fileList = castToFileList(t.getTransferData(MindMapNodesSelection.fileListFlavor));
				handlerList.add(new FileListFlavorHandler(fileList));
			}
			catch (final UnsupportedFlavorException e) {
			}
			catch (final IOException e) {
			}
		}
		if (t.isDataFlavorSupported(DataFlavor.imageFlavor)) {
			try {
				Image image = (Image) t.getTransferData(DataFlavor.imageFlavor);
				handlerList.add(new ImageFlavorHandler(image));
			}
			catch (final UnsupportedFlavorException e) {
			}
			catch (final IOException e) {
			}
		}
		return handlerList;
	}
	private DataFlavor getSupportedHtmlFlavor(Transferable t) {
		for (DataFlavor dataFlavor : t.getTransferDataFlavors())
			if(dataFlavor.getPrimaryType().equals(MindMapNodesSelection.htmlFlavor.getPrimaryType())
			&& dataFlavor.getSubType().equals(MindMapNodesSelection.htmlFlavor.getSubType())
			&& dataFlavor.getRepresentationClass().equals(MindMapNodesSelection.htmlFlavor.getRepresentationClass())
			)
				return dataFlavor;
		return null;
	}

	public void paste(final Transferable t, final NodeModel target, final boolean asSibling, final boolean isLeft) {
		paste(t, target, asSibling, isLeft, DnDConstants.ACTION_NONE);
	}

	public void paste(final Transferable t, final NodeModel target, final boolean asSibling, final boolean isLeft, int dropAction) {
		if (t == null) {
			return;
		}
//
//		DataFlavor[] fl = t.getTransferDataFlavors();
//		for (int i = 0; i < fl.length; i++) {
//			System.out.println(fl[i]);
//		}

		final IDataFlavorHandler handler = getFlavorHandler(t);
		paste(t, handler, target, asSibling, isLeft, dropAction);
	}

	void paste(final Transferable t, final IDataFlavorHandler handler, final NodeModel target, final boolean asSibling, final boolean isLeft) {
		paste(t, handler, target, asSibling, isLeft, DnDConstants.ACTION_NONE);
    }

	void paste(final Transferable t, final IDataFlavorHandler handler, final NodeModel target, final boolean asSibling, final boolean isLeft, int dropAction) {
		if (handler == null) {
			return;
		}
		final MMapController mapController = (MMapController) Controller.getCurrentModeController().getMapController();
		if (asSibling && !mapController.isWriteable(target.getParentNode()) || !asSibling
		        && !mapController.isWriteable(target)) {
			final String message = TextUtils.getText("node_is_write_protected");
			UITools.errorMessage(message);
			return;
		}
		try {
			Controller.getCurrentController().getViewController().setWaitingCursor(true);
			if (newNodes == null) {
				newNodes = new LinkedList<NodeModel>();
			}
			newNodes.clear();
			handler.paste(t, target, asSibling, isLeft, dropAction);
			final ModeController modeController = Controller.getCurrentModeController();
			if (!asSibling && modeController.getMapController().isFolded(target)
			        && ResourceController.getResourceController().getBooleanProperty(RESOURCE_UNFOLD_ON_PASTE)) {
				modeController.getMapController().unfoldAndScroll(target);
			}
			for (final NodeModel child : newNodes) {
				AttributeController.getController().performRegistrySubtreeAttributes(child);
			}
		}
		finally {
			Controller.getCurrentController().getViewController().setWaitingCursor(false);
		}
	}

	private void pasteStringWithoutRedisplay(final TextFragment[] textFragments, NodeModel parent,
	                                              final boolean asSibling, final boolean isLeft) {
		int insertionIndex;
		if (asSibling) {
			NodeModel target = parent;
			parent = parent.getParentNode();
			final NodeModel childNode = target;
			insertionIndex = parent.getIndex(childNode);
		}
		else{
			insertionIndex = parent.getChildCount();
		}
		final ArrayList<NodeModel> parentNodes = new ArrayList<NodeModel>();
		final ArrayList<Integer> parentNodesDepths = new ArrayList<Integer>();
		parentNodes.add(parent);
		parentNodesDepths.add(new Integer(-1));
		for (int i = 0; i < textFragments.length; ++i) {
			final TextFragment textFragment = textFragments[i];
			if(textFragment.isNode()) {
				insertionIndex = addNode(parent, isLeft, insertionIndex, parentNodes, parentNodesDepths,
					textFragment);
			}
			else if(textFragment.isAttribute()) {
				NodeModel node = parentNodes.get(parentNodes.size() - 1);
				addAttribute(node, textFragment, parent==node);
			}
		}
		insertNewNodes(parent, insertionIndex, parentNodes);
	}

	private void addAttribute(NodeModel node, final TextFragment textFragment, boolean toExistingNode) {
		final String name = textFragment.text;
		final Object value = ScannerController.getController().parse(textFragment.link);
		final Attribute atribute = new Attribute(name, value);
		if(toExistingNode) {
			MAttributeController.getController().addAttribute(node, atribute);
		}
		else {
			NodeAttributeTableModel attributes = node.getExtension(NodeAttributeTableModel.class);
			if(attributes == null) {
				attributes = new NodeAttributeTableModel();
				node.addExtension(attributes);
			}
			attributes.addRowNoUndo(node, atribute);
		}
	}

	private int addNode(NodeModel parent, final boolean isLeft, int insertionIndex,
						final ArrayList<NodeModel> parentNodes, final ArrayList<Integer> parentNodesDepths,
						final TextFragment textFragment) {
		final MapModel map = parent.getMap();
		final NodeModel node = createNode(map, textFragment);
		return insertNode(parent, isLeft, insertionIndex, parentNodes, parentNodesDepths, textFragment, node);
	}

	private int insertNode(NodeModel parent, final boolean isLeft, int insertionIndex,
						   final ArrayList<NodeModel> parentNodes, final ArrayList<Integer> parentNodesDepths,
						   final TextFragment textFragment, final NodeModel node) {
		final MMapController mapController = (MMapController) Controller.getCurrentModeController().getMapController();
		for (int parentNodeIndex = parentNodes.size() - 1; parentNodeIndex >= 0; --parentNodeIndex) {
			if (textFragment.depth > parentNodesDepths.get(parentNodeIndex).intValue()) {
				int completedParentNodeIndex = parentNodeIndex + 1;
				while (completedParentNodeIndex < parentNodes.size()) {
					final NodeModel n = parentNodes.get(completedParentNodeIndex);
					if (n.getParentNode() == null) {
						mapController.insertNode(n, parent, insertionIndex++);
					}
					parentNodes.remove(completedParentNodeIndex);
					parentNodesDepths.remove(completedParentNodeIndex);
				}
				final NodeModel target = parentNodes.get(parentNodeIndex);
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
		return insertionIndex;
	}

	private NodeModel createNode(final MapModel map, final TextFragment textFragment) {
		String text = textFragment.text;
		final String link = textFragment.link;
		URI uri = null;
		if (link != null) {
			try {
				URI linkUri = new URI(link);
				uri = linkUri;

				File absoluteFile = UrlManager.getController().getAbsoluteFile(map, uri);
				if(absoluteFile != null) {
					//if ("file".equals(linkUri.getScheme())) {
					final File mapFile = map.getFile();
					uri  = LinkController.toLinkTypeDependantURI(mapFile, absoluteFile);
					if(link.equals(text)){
						text =  uri.toString();
					}
				}

			}
			catch (Exception e) {
			}
		}
		final MMapController mapController = (MMapController) Controller.getCurrentModeController().getMapController();
		final NodeModel node = mapController.newNode(text, map);
		if(uri != null){
			NodeLinks.createLinkExtension(node).setHyperLink(uri);
		}
		return node;
	}

	private void insertNewNodes(NodeModel parent, int insertionIndex,
								final ArrayList<NodeModel> parentNodes) {
		final MMapController mapController = (MMapController) Controller.getCurrentModeController().getMapController();
		final MapModel map = parent.getMap();
		for (int k = 0; k < parentNodes.size(); ++k) {
			final NodeModel node = parentNodes.get(k);
			if (map.getRootNode() != node && node.getParentNode() == null) {
				mapController.insertNode(node, parent, insertionIndex++);
			}
		}
	}

	private enum Operation{CLONE, MOVE};

	public void addClone(final Transferable transferable, final NodeModel target) {
		processTransferable(transferable, target, Operation.CLONE);
	}

	public void move(final Transferable transferable, final NodeModel target) {
		processTransferable(transferable, target, Operation.MOVE);
	}

	@SuppressWarnings("unchecked")
	private void processTransferable(final Transferable transferable, final NodeModel target, Operation operation) {
		try {
			final Collection<NodeModel> clonedNodes;
			final boolean asSingleNodes;
			if (operation == Operation.CLONE && transferable.isDataFlavorSupported(MindMapNodesSelection.mindMapNodeSingleObjectsFlavor)){
				clonedNodes = (Collection<NodeModel>) transferable.getTransferData(MindMapNodesSelection.mindMapNodeSingleObjectsFlavor);
				asSingleNodes = true;
			}
			else if(transferable.isDataFlavorSupported(MindMapNodesSelection.mindMapNodeObjectsFlavor)){
				clonedNodes = (Collection<NodeModel>) transferable.getTransferData(MindMapNodesSelection.mindMapNodeObjectsFlavor);
				asSingleNodes = false;
			}
			else
				return;

			final List<NodeModel> movedNodes = new ArrayList<NodeModel>(clonedNodes.size());
			final MMapController mapController = (MMapController) Controller.getCurrentModeController().getMapController();
			for(NodeModel clonedNode:clonedNodes){
				if(clonedNode.getParentNode() == null || ! clonedNode.getMap().equals(target.getMap()))
					return;
				if (!clonedNode.isRoot() && ! clonedNode.subtreeContainsCloneOf(target)) {
					switch(operation){
					case CLONE:
						try {
							final NodeModel clone = asSingleNodes ? clonedNode.cloneContent() : clonedNode.cloneTree();
							mapController.addNewNode(clone, target, target.getChildCount(), target.isNewChildLeft());
						} catch (CloneEncryptedNodeException e) {
							UITools.errorMessage(TextUtils.getText("can_not_clone_encrypted_node"));
						}
						break;
					case MOVE:
						movedNodes.add(clonedNode);
						break;
					}
				}
			}
			switch(operation){
			case MOVE:
				mapController.moveNodesAsChildren(movedNodes, target, target.isNewChildLeft(), true);
					break;
			default:
				break;
			}
		}
		catch (Exception e) {
	        LogUtils.severe(e);
        }
    }

	public Transferable getClipboardContents() {
		return ClipboardAccessor.getController().getClipboardContents();
	}

	@Override
	public boolean canCut() {
		return true;
	}

	@Override
	public void cut() {
		final Controller controller = Controller.getCurrentController();
		final NodeModel root = controller.getMap().getRootNode();
		if (controller.getSelection().isSelected(root)) {
			UITools.errorMessage(TextUtils.getText("cannot_delete_root"));
			return;
		}
		final int showResult = OptionalDontShowMeAgainDialog.show("really_cut_node", "confirmation",
		    MMapClipboardController.RESOURCES_CUT_NODES_WITHOUT_QUESTION,
		    OptionalDontShowMeAgainDialog.ONLY_OK_SELECTION_IS_STORED);
		if (showResult != JOptionPane.OK_OPTION) {
			return;
		}
		cut(controller.getSelection().getSortedSelection(true));
		controller.getMapViewManager().obtainFocusForSelected();

	}

	@Override
	public boolean canPaste(Transferable t) {
		return true;
	}

	@Override
	public void paste(Transferable t) {
		final NodeModel parent = Controller.getCurrentController().getSelection().getSelected();
		if(parent != null)
			paste(t, parent, false, parent.isNewChildLeft());
	}
}
