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
package org.freeplane.features.mindmapmode.text;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.text.JTextComponent;
import javax.swing.text.html.HTMLDocument;

import org.freeplane.core.frame.ViewController;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.ColorUtils;
import org.freeplane.core.util.HtmlTools;
import org.freeplane.core.util.LogTool;
import org.freeplane.features.mindmapmode.ortho.SpellCheckerController;

import com.lightdev.app.shtm.SHTMLEditorPane;
import com.lightdev.app.shtm.SHTMLPanel;

/**
 * @author Daniel Polansky
 */
class EditNodeWYSIWYG extends EditNodeBase {
	private static class HTMLDialog extends EditDialog {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private SHTMLPanel htmlEditorPanel;

		HTMLDialog(final EditNodeBase base, final Frame frame) throws Exception {
			super(base, frame);
			createEditorPanel();
			getContentPane().add(htmlEditorPanel, BorderLayout.CENTER);
			UITools.addEscapeActionToDialog(this, new CancelAction());
			final JButton okButton = new JButton();
			final JButton cancelButton = new JButton();
			final JButton splitButton = new JButton();
			MenuBuilder.setLabelAndMnemonic(okButton, base.getText("ok"));
			MenuBuilder.setLabelAndMnemonic(cancelButton, base.getText("cancel"));
			MenuBuilder.setLabelAndMnemonic(splitButton, base.getText("split"));
			okButton.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					submit();
				}
			});
			cancelButton.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					cancel();
				}
			});
			splitButton.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					split();
				}
			});
			UITools.addKeyActionToDialog(this, new SubmitAction(), "alt ENTER", "submit");
			final JPanel buttonPane = new JPanel();
			buttonPane.add(okButton);
			buttonPane.add(cancelButton);
			buttonPane.add(splitButton);
			buttonPane.setMaximumSize(new Dimension(1000, 20));
			if (ResourceController.getResourceController().getBooleanProperty("el__buttons_above")) {
				getContentPane().add(buttonPane, BorderLayout.NORTH);
			}
			else {
				getContentPane().add(buttonPane, BorderLayout.SOUTH);
			}
		}

		/*
		 * (non-Javadoc)
		 * @see freeplane.view.mindmapview.EditNodeBase.Dialog#close()
		 */
		@Override
		protected void cancel() {
			htmlEditorPanel.getDocument().getStyleSheet().removeStyle("body");
			getBase().getEditControl().cancel();
			super.cancel();
		}

		private SHTMLPanel createEditorPanel() throws Exception {
			if (htmlEditorPanel == null) {
				htmlEditorPanel = SHTMLPanel.createSHTMLPanel();
				final SHTMLEditorPane editorPane = (SHTMLEditorPane) htmlEditorPanel.getEditorPane();
				final SpellCheckerController spellCheckerController = SpellCheckerController.getController(getBase()
				    .getModeController());
				spellCheckerController.enableAutoSpell(editorPane);
				spellCheckerController.addSpellCheckerMenu(editorPane.getPopup());
				spellCheckerController.enableShortKey(editorPane);
			}
			return htmlEditorPanel;
		}

		/**
		 * @return Returns the htmlEditorPanel.
		 */
		public SHTMLPanel getHtmlEditorPanel() {
			return htmlEditorPanel;
		}

		@Override
		public Component getMostRecentFocusOwner() {
			if (isFocused()) {
				return getFocusOwner();
			}
			else {
				return htmlEditorPanel.getMostRecentFocusOwner();
			}
		}

		@Override
		protected boolean isChanged() {
			return htmlEditorPanel.needsSaving();
		}

		/*
		 * (non-Javadoc)
		 * @see freeplane.view.mindmapview.EditNodeBase.Dialog#split()
		 */
		@Override
		protected void split() {
			htmlEditorPanel.getDocument().getStyleSheet().removeStyle("body");
			getBase().getEditControl().split(HtmlTools.unescapeHTMLUnicodeEntity(htmlEditorPanel.getDocumentText()),
			    htmlEditorPanel.getCaretPosition());
			super.split();
		}

		/*
		 * (non-Javadoc)
		 * @see freeplane.view.mindmapview.EditNodeBase.Dialog#close()
		 */
		@Override
		protected void submit() {
			htmlEditorPanel.getDocument().getStyleSheet().removeStyle("body");
			if (htmlEditorPanel.needsSaving()) {
				getBase().getEditControl().ok(HtmlTools.unescapeHTMLUnicodeEntity(htmlEditorPanel.getDocumentText()));
			}
			else {
				getBase().getEditControl().cancel();
			}
			super.submit();
		}
	}

	private static HTMLDialog htmlEditorWindow;
	final private KeyEvent firstEvent;

	public EditNodeWYSIWYG(final NodeModel node, final String text, final KeyEvent firstEvent,
	                       final ModeController modeController, final IEditControl editControl) {
		super(node, text, modeController, editControl);
		this.firstEvent = firstEvent;
	}

	public void show(final Frame frame) {
		try {
			if (EditNodeWYSIWYG.htmlEditorWindow == null) {
				EditNodeWYSIWYG.htmlEditorWindow = new HTMLDialog(this, frame);
			}
			EditNodeWYSIWYG.htmlEditorWindow.setBase(this);
			final SHTMLPanel htmlEditorPanel = (EditNodeWYSIWYG.htmlEditorWindow).getHtmlEditorPanel();
			String rule = "BODY {";
			final ViewController viewController = getModeController().getController().getViewController();
			final Font font = viewController.getFont(node);
			final Color nodeTextBackground = viewController.getBackgroundColor(node);
			rule += "font-family: " + font.getFamily() + ";";
			rule += "font-size: " + font.getSize() + "pt;";
			if (font.isItalic()) {
				rule += "font-style: italic; ";
			}
			if (font.isBold()) {
				rule += "font-weight: bold; ";
			}
			final Color nodeTextColor = viewController.getTextColor(node);
			rule += "color: " + ColorUtils.colorToString(nodeTextColor) + ";";
			rule += "}\n";
			rule += "p {";
			rule += "margin-top:0;";
			rule += "}\n";
			final HTMLDocument document = htmlEditorPanel.getDocument();
			final JEditorPane editorPane = htmlEditorPanel.getEditorPane();
			editorPane.setForeground(nodeTextColor);
			editorPane.setBackground(nodeTextBackground);
			editorPane.setCaretColor(nodeTextColor);
			document.getStyleSheet().addRule(rule);
			document.setBase(node.getMap().getURL());
			int preferredHeight = (int) (viewController.getComponent(node).getHeight() * 1.2);
			preferredHeight = Math.max(preferredHeight, Integer.parseInt(ResourceController.getResourceController()
			    .getProperty("el__min_default_window_height")));
			preferredHeight = Math.min(preferredHeight, Integer.parseInt(ResourceController.getResourceController()
			    .getProperty("el__max_default_window_height")));
			int preferredWidth = (int) (viewController.getComponent(node).getWidth() * 1.2);
			preferredWidth = Math.max(preferredWidth, Integer.parseInt(ResourceController.getResourceController()
			    .getProperty("el__min_default_window_width")));
			preferredWidth = Math.min(preferredWidth, Integer.parseInt(ResourceController.getResourceController()
			    .getProperty("el__max_default_window_width")));
			htmlEditorPanel.setContentPanePreferredSize(new Dimension(preferredWidth, preferredHeight));
			EditNodeWYSIWYG.htmlEditorWindow.pack();
			if(ResourceController.getResourceController().getBooleanProperty("el__position_window_below_node")){
				UITools.setDialogLocationUnder(EditNodeWYSIWYG.htmlEditorWindow, getController(), node);
			}
			else{
				UITools.setDialogLocationRelativeTo(EditNodeWYSIWYG.htmlEditorWindow, getController(), node);
			}
			String content = node.toString();
			if (!HtmlTools.isHtmlNode(content)) {
				content = HtmlTools.plainToHTML(content);
			}
			htmlEditorPanel.setCurrentDocumentContent(content);
			if (firstEvent instanceof KeyEvent) {
				final KeyEvent firstKeyEvent = firstEvent;
				final JTextComponent currentPane = htmlEditorPanel.getEditorPane();
				if (currentPane == htmlEditorPanel.getMostRecentFocusOwner()) {
					redispatchKeyEvents(currentPane, firstKeyEvent);
				}
			}
			else {
				editorPane.setCaretPosition(htmlEditorPanel.getDocument().getLength());
			}
			htmlEditorPanel.getMostRecentFocusOwner().requestFocus();
			EditNodeWYSIWYG.htmlEditorWindow.show();
		}
		catch (final Exception ex) {
			LogTool.severe(ex);
			System.err.println("Loading of WYSIWYG HTML editor failed. Use the other editors instead.");
		}
	}
}
