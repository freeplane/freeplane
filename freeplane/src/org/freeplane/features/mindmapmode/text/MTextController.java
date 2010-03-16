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
package org.freeplane.features.mindmapmode.text;

import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.util.List;
import java.util.ListIterator;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.frame.ViewController;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.MapModel;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.BitmapImagePreview;
import org.freeplane.core.ui.components.OptionalDontShowMeAgainDialog;
import org.freeplane.core.undo.IActor;
import org.freeplane.core.url.UrlManager;
import org.freeplane.core.util.FixedHTMLWriter;
import org.freeplane.core.util.HtmlTools;
import org.freeplane.core.util.LogTool;
import org.freeplane.features.common.link.LinkController;
import org.freeplane.features.common.link.NodeLinks;
import org.freeplane.features.common.nodestyle.NodeStyleController;
import org.freeplane.features.common.text.TextController;
import org.freeplane.features.mindmapmode.MMapController;
import org.freeplane.features.mindmapmode.MModeController;
import org.freeplane.features.mindmapmode.file.MFileManager;
import org.freeplane.features.mindmapmode.link.MLinkController;
import org.freeplane.features.mindmapmode.nodestyle.MNodeStyleController;
import org.freeplane.view.swing.addins.filepreview.ImagePreview;

import com.lightdev.app.shtm.SHTMLPanel;
import com.lightdev.app.shtm.TextResources;

/**
 * @author Dimitry Polivaev
 */
public class MTextController extends TextController {
	static private EditAction edit;
	public static final String RESOURCES_REMIND_USE_RICH_TEXT_IN_NEW_LONG_NODES = "remind_use_rich_text_in_new_long_nodes";

	static public SHTMLPanel createSHTMLPanel() {
		SHTMLPanel.setResources(new TextResources() {
			public String getString(String pKey) {
				pKey = "simplyhtml." + pKey;
				String resourceString = ResourceController.getResourceController().getText(pKey, null);
				if (resourceString == null) {
					resourceString = ResourceController.getResourceController().getProperty(pKey);
				}
				return resourceString;
			}
		});
		return SHTMLPanel.createSHTMLPanel();
	}

	public MTextController(final MModeController modeController) {
		super(modeController);
		createActions(modeController);
	}

	/**
	 *
	 */
	private void createActions(final ModeController modeController) {
		final Controller controller = modeController.getController();
		edit = new EditAction(controller);
		modeController.addAction(edit);
		modeController.addAction(new UseRichFormattingAction(controller));
		modeController.addAction(new UsePlainTextAction(controller));
		modeController.addAction(new JoinNodesAction(controller));
		modeController.addAction(new EditLongAction(controller));
		modeController.addAction(new SetImageByFileChooserAction(controller));
	}

	public void edit(final KeyEvent e, final boolean addNew, final boolean editLong) {
		((EditAction) getModeController().getAction("EditAction")).edit(e, addNew, editLong);
	}

	public void edit(final NodeModel node, final NodeModel prevSelected, final KeyEvent firstEvent,
	                 final boolean isNewNode, final boolean parentFolded, final boolean editLong) {
		edit.edit(node, prevSelected, firstEvent, isNewNode, parentFolded, editLong);
	}

	private String[] getContent(final String text, final int pos) {
		if (pos <= 0) {
			return null;
		}
		final String[] strings = new String[2];
		if (text.startsWith("<html>")) {
			final HTMLEditorKit kit = new HTMLEditorKit();
			final HTMLDocument doc = new HTMLDocument();
			final StringReader buf = new StringReader(text);
			try {
				kit.read(buf, doc, 0);
				final char[] firstText = doc.getText(0, pos).toCharArray();
				int firstStart = 0;
				int firstLen = pos;
				while ((firstStart < firstLen) && (firstText[firstStart] <= ' ')) {
					firstStart++;
				}
				while ((firstStart < firstLen) && (firstText[firstLen - 1] <= ' ')) {
					firstLen--;
				}
				int secondStart = 0;
				int secondLen = doc.getLength() - pos;
				final char[] secondText = doc.getText(pos, secondLen).toCharArray();
				while ((secondStart < secondLen) && (secondText[secondStart] <= ' ')) {
					secondStart++;
				}
				while ((secondStart < secondLen) && (secondText[secondLen - 1] <= ' ')) {
					secondLen--;
				}
				if (firstStart == firstLen || secondStart == secondLen) {
					return null;
				}
				StringWriter out = new StringWriter();
				new FixedHTMLWriter(out, doc, firstStart, firstLen - firstStart).write();
				strings[0] = out.toString();
				out = new StringWriter();
				new FixedHTMLWriter(out, doc, pos + secondStart, secondLen - secondStart).write();
				strings[1] = out.toString();
				return strings;
			}
			catch (final IOException e) {
				LogTool.severe(e);
			}
			catch (final BadLocationException e) {
				LogTool.severe(e);
			}
		}
		else {
			if (pos >= text.length()) {
				return null;
			}
			strings[0] = text.substring(0, pos);
			strings[1] = text.substring(pos);
		}
		return strings;
	}

	public void joinNodes(final NodeModel selectedNode, final List selectedNodes) {
		((JoinNodesAction) getModeController().getAction("JoinNodesAction")).joinNodes(selectedNode, selectedNodes);
	}

	public void setImageByFileChooser() {
		boolean picturesAmongSelecteds = false;
		ModeController modeController = getModeController();
		for (final NodeModel node : modeController.getMapController().getSelectedNodes()) {
			final URI link = NodeLinks.getLink(node);
			if (link != null) {
				final String linkString = link.toString();
				final String lowerCase = linkString.toLowerCase();
				if (lowerCase.endsWith(".png") || lowerCase.endsWith(".jpg") || lowerCase.endsWith(".jpeg")
				        || lowerCase.endsWith(".gif")) {
					picturesAmongSelecteds = true;
					final String encodedLinkString = HtmlTools.unicodeToHTMLUnicodeEntity(linkString);
					final String strText = "<html><img src=\"" + encodedLinkString + "\">";
					((MLinkController) LinkController.getController(modeController)).setLink(node, (URI) null, false);
					setNodeText(node, strText);
				}
			}
		}
		if (picturesAmongSelecteds) {
			return;
		}
		
		Controller controller = modeController.getController();
		ViewController viewController = controller.getViewController();
		NodeModel selectedNode = modeController.getMapController().getSelectedNode();
		MapModel map = selectedNode.getMap();
		File file = map.getFile();
		boolean useRelativeUri = ResourceController.getResourceController().getProperty("links").equals("relative");
		if (file == null && useRelativeUri) {
			JOptionPane.showMessageDialog(viewController.getContentPane(), ResourceBundles
				.getText("not_saved_for_image_error"), "Freeplane", JOptionPane.WARNING_MESSAGE);
			return;
		}
		final ExampleFileFilter filter = new ExampleFileFilter();
		filter.addExtension("jpg");
		filter.addExtension("jpeg");
		filter.addExtension("png");
		filter.addExtension("gif");
		filter.setDescription(ResourceBundles.getText("bitmaps"));
		UrlManager urlManager = (UrlManager) modeController.getExtension(UrlManager.class);
		JFileChooser chooser = urlManager.getFileChooser(null);
		chooser.setFileFilter(filter);
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setAccessory(new BitmapImagePreview(chooser));
		final int returnVal = chooser.showOpenDialog(viewController.getContentPane());
		if (returnVal != JFileChooser.APPROVE_OPTION) {
			return;
		}
		final File input = chooser.getSelectedFile();
		URI uri = input.toURI();
		if (uri == null) {
			return;
		}
		if (useRelativeUri) {
			uri = LinkController.toRelativeURI(map.getFile(), input);
		}
		final String strText = "<html><img src=\"" + uri.toString() + "\">";
		setNodeText(selectedNode, strText);
	}

	public void setNodeText(final NodeModel node, final String newText) {
		final String oldText = node.toString();
		if (oldText.equals(newText)) {
			return;
		}
		final IActor actor = new IActor() {
			public void act() {
				if (!oldText.equals(newText)) {
					node.setText(newText);
					getModeController().getMapController().nodeChanged(node, NodeModel.NODE_TEXT, oldText, newText);
				}
			}

			public String getDescription() {
				return "setNodeText";
			}

			public void undo() {
				if (!oldText.equals(newText)) {
					node.setText(oldText);
					getModeController().getMapController().nodeChanged(node, NodeModel.NODE_TEXT, newText, oldText);
				}
			}
		};
		getModeController().execute(actor, node.getMap());
	}

	public void splitNode(final NodeModel node, final int caretPosition, final String newText) {
		if (node.isRoot()) {
			return;
		}
		final String futureText = newText != null ? newText : node.toString();
		final String[] strings = getContent(futureText, caretPosition);
		if (strings == null) {
			return;
		}
		final String newUpperContent = strings[0];
		final String newLowerContent = strings[1];
		setNodeText(node, newUpperContent);
		final NodeModel parent = node.getParentNode();
		final ModeController modeController = getModeController();
		final NodeModel lowerNode = ((MMapController) modeController.getMapController()).addNewNode(parent, parent
		    .getChildPosition(node) + 1, node.isLeft());
		final MNodeStyleController nodeStyleController = (MNodeStyleController) NodeStyleController
		    .getController(modeController);
		nodeStyleController.copyStyle(node, lowerNode);
		setNodeText(lowerNode, newLowerContent);
	}

	/**
	 *
	 */
	public void stopEditing() {
		edit.stopEditing();
	}

	public boolean useRichTextInNewLongNodes() {
		final int showResult = OptionalDontShowMeAgainDialog.show(getModeController().getController(),
		    "edit.edit_rich_text", "edit.decision", MTextController.RESOURCES_REMIND_USE_RICH_TEXT_IN_NEW_LONG_NODES,
		    OptionalDontShowMeAgainDialog.BOTH_OK_AND_CANCEL_OPTIONS_ARE_STORED);
		final String useRichTextInNewLongNodes = (showResult == JOptionPane.OK_OPTION) ? "true" : "false";
		return useRichTextInNewLongNodes.equals("true");
	}
}
