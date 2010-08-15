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

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.frame.ViewController;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.BitmapImagePreview;
import org.freeplane.core.ui.components.OptionalDontShowMeAgainDialog;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.undo.IActor;
import org.freeplane.core.util.FixedHTMLWriter;
import org.freeplane.core.util.HtmlUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.common.link.LinkController;
import org.freeplane.features.common.link.NodeLinks;
import org.freeplane.features.common.map.MapModel;
import org.freeplane.features.common.map.ModeController;
import org.freeplane.features.common.map.NodeModel;
import org.freeplane.features.common.nodestyle.NodeStyleController;
import org.freeplane.features.common.text.TextController;
import org.freeplane.features.common.url.UrlManager;
import org.freeplane.features.mindmapmode.link.MLinkController;
import org.freeplane.features.mindmapmode.map.MMapController;
import org.freeplane.features.mindmapmode.nodestyle.MNodeStyleController;

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

	public MTextController() {
		super();
		createActions();
	}

	/**
	 *
	 */
	private void createActions() {
		ModeController modeController = Controller.getCurrentModeController();
		edit = new EditAction();
		modeController.addAction(edit);
		modeController.addAction(new UseRichFormattingAction());
		modeController.addAction(new UsePlainTextAction());
		modeController.addAction(new JoinNodesAction());
		modeController.addAction(new EditLongAction());
		modeController.addAction(new SetImageByFileChooserAction());
	}

	public void edit(final KeyEvent e, final boolean addNew, final boolean editLong) {
		((EditAction) Controller.getCurrentModeController().getAction("EditAction")).edit(e, addNew, editLong);
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
				LogUtils.severe(e);
			}
			catch (final BadLocationException e) {
				LogUtils.severe(e);
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

	public void joinNodes(final NodeModel selectedNode, final List<NodeModel> selectedNodes) {
		((JoinNodesAction) Controller.getCurrentModeController().getAction("JoinNodesAction")).joinNodes(selectedNode, selectedNodes);
	}

	public void setImageByFileChooser() {
		boolean picturesAmongSelecteds = false;
		final ModeController modeController = Controller.getCurrentModeController();
		for (final NodeModel node : modeController.getMapController().getSelectedNodes()) {
			final URI link = NodeLinks.getLink(node);
			if (link != null) {
				final String linkString = link.toString();
				final String lowerCase = linkString.toLowerCase();
				if (lowerCase.endsWith(".png") || lowerCase.endsWith(".jpg") || lowerCase.endsWith(".jpeg")
				        || lowerCase.endsWith(".gif")) {
					picturesAmongSelecteds = true;
					final String encodedLinkString = HtmlUtils.unicodeToHTMLUnicodeEntity(linkString);
					final String strText = "<html><img src=\"" + encodedLinkString + "\">";
					((MLinkController) LinkController.getController()).setLink(node, (URI) null, false);
					setNodeText(node, strText);
				}
			}
		}
		if (picturesAmongSelecteds) {
			return;
		}
		final Controller controller = modeController.getController();
		final ViewController viewController = controller.getViewController();
		final NodeModel selectedNode = modeController.getMapController().getSelectedNode();
		final MapModel map = selectedNode.getMap();
		final File file = map.getFile();
		final boolean useRelativeUri = ResourceController.getResourceController().getProperty("links").equals(
		    "relative");
		if (file == null && useRelativeUri) {
			JOptionPane.showMessageDialog(viewController.getContentPane(), TextUtils
			    .getText("not_saved_for_image_error"), "Freeplane", JOptionPane.WARNING_MESSAGE);
			return;
		}
		final ExampleFileFilter filter = new ExampleFileFilter();
		filter.addExtension("jpg");
		filter.addExtension("jpeg");
		filter.addExtension("png");
		filter.addExtension("gif");
		filter.setDescription(TextUtils.getText("bitmaps"));
		final UrlManager urlManager = (UrlManager) modeController.getExtension(UrlManager.class);
		final JFileChooser chooser = urlManager.getFileChooser(null);
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
		// bad hack: try to interpret file as http link
		if(! input.exists()){
			uri = LinkController.toRelativeURI(map.getFile(), input);
			if(uri == null || ! "http".equals(uri.getScheme())){
				UITools.errorMessage(TextUtils.formatText("file_not_found", input.toString()));
				return;
			}
		}
		else if (useRelativeUri) {
			uri = LinkController.toRelativeURI(map.getFile(), input);
		}
		String uriString = uri.toString();
		if(uriString.startsWith("http:/")){
			uriString = "http://" + uriString.substring("http:/".length());
		}
		final String strText = "<html><img src=\"" + uriString + "\">";
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
					Controller.getCurrentModeController().getMapController().nodeChanged(node, NodeModel.NODE_TEXT, oldText, newText);
				}
			}

			public String getDescription() {
				return "setNodeText";
			}

			public void undo() {
				if (!oldText.equals(newText)) {
					node.setText(oldText);
					Controller.getCurrentModeController().getMapController().nodeChanged(node, NodeModel.NODE_TEXT, newText, oldText);
				}
			}
		};
		Controller.getCurrentModeController().execute(actor, node.getMap());
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
		final ModeController modeController = Controller.getCurrentModeController();
		final NodeModel lowerNode = ((MMapController) modeController.getMapController()).addNewNode(parent, parent
		    .getChildPosition(node) + 1, node.isLeft());
		final MNodeStyleController nodeStyleController = (MNodeStyleController) NodeStyleController
		    .getController();
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
		final int showResult = OptionalDontShowMeAgainDialog.show(
		    "edit.edit_rich_text", "edit.decision", MTextController.RESOURCES_REMIND_USE_RICH_TEXT_IN_NEW_LONG_NODES,
		    OptionalDontShowMeAgainDialog.BOTH_OK_AND_CANCEL_OPTIONS_ARE_STORED);
		final String useRichTextInNewLongNodes = (showResult == JOptionPane.OK_OPTION) ? "true" : "false";
		return useRichTextInNewLongNodes.equals("true");
	}
}
