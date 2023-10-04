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
package org.freeplane.features.text.mindmapmode;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.RootPaneContainer;
import javax.swing.text.JTextComponent;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.StyleSheet;

import org.dpolivaev.mnemonicsetter.MnemonicSetter;
import org.freeplane.api.HorizontalTextAlignment;
import org.freeplane.api.TextWritingDirection;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.LabelAndMnemonicSetter;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.ui.components.html.CssRuleBuilder;
import org.freeplane.core.ui.components.html.StyleSheetConfigurer;
import org.freeplane.core.util.HtmlProcessor;
import org.freeplane.core.util.HtmlUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.nodestyle.NodeCss;
import org.freeplane.features.note.mindmapmode.MNoteController;
import org.freeplane.features.spellchecker.mindmapmode.SpellCheckerController;

import com.lightdev.app.shtm.SHTMLEditorPane;
import com.lightdev.app.shtm.SHTMLPanel;

/**
 * @author Daniel Polansky
 */
public class EditNodeWYSIWYG extends EditNodeBase {
	private static class HTMLDialog extends EditDialog {
		private SHTMLPanel htmlEditorPanel;
		private JButton splitButton;
		private StyleSheet ownStyleSheet = StyleSheetConfigurer.createDefaultStyleSheet();

		HTMLDialog(final EditNodeBase base, final String title, String purpose, final RootPaneContainer frame) throws Exception {
			super(base, title, frame);
			JDialog dialog = getDialog();
            dialog.setModal(ResourceController.getResourceController().getBooleanProperty("enforceModalEditorDialogs"));
			createEditorPanel(purpose);
			getContentPane().add(htmlEditorPanel, BorderLayout.CENTER);
			UITools.addEscapeActionToDialog(dialog, new CancelAction());
			final JButton okButton = new JButton();
			final JButton cancelButton = new JButton();
			splitButton = new JButton();
			LabelAndMnemonicSetter.setLabelAndMnemonic(okButton, TextUtils.getRawText("ok"));
			LabelAndMnemonicSetter.setLabelAndMnemonic(cancelButton, TextUtils.getRawText("cancel"));
			LabelAndMnemonicSetter.setLabelAndMnemonic(splitButton, TextUtils.getRawText("split"));
			final SubmitAction submitAction = new SubmitAction();
			okButton.addActionListener(submitAction);
			cancelButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					cancel();
				}
			});
			final SplitAction splitAction = new SplitAction();
			splitButton.addActionListener(splitAction);
			UITools.addKeyActionToDialog(dialog, submitAction, "alt ENTER", "submit");
			final KeyStroke splitNodeHotKey = ResourceController.getResourceController().getAcceleratorManager().getAccelerator("SplitNode");
			if(splitNodeHotKey != null)
				UITools.addKeyActionToDialog(dialog, splitAction, splitNodeHotKey.toString(), "split");
			final JPanel buttonPane = new JPanel();
			buttonPane.add(okButton);
			buttonPane.add(cancelButton);
			buttonPane.add(splitButton);
			buttonPane.setMaximumSize(new Dimension(1000, 20));
			MnemonicSetter.INSTANCE.setComponentMnemonics(buttonPane, htmlEditorPanel.getMenuBar());

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
			super.cancel();
			final StyleSheet styleSheet = htmlEditorPanel.getDocument().getStyleSheet();
			StyleSheetConfigurer.resetStyles(styleSheet, 1);
			getBase().getEditControl().cancel();
		}

		private SHTMLPanel createEditorPanel(String purpose) throws Exception {
			if (htmlEditorPanel == null) {
				htmlEditorPanel = MTextController.getController().createSHTMLPanel(purpose);
				final SHTMLEditorPane editorPane = (SHTMLEditorPane) htmlEditorPanel.getEditorPane();
				final SpellCheckerController spellCheckerController = SpellCheckerController.getController();
				spellCheckerController.enableAutoSpell(editorPane, true);
				spellCheckerController.addSpellCheckerMenu(editorPane.getPopup());
				spellCheckerController.enableShortKey(editorPane, true);
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
			if (getDialog().isFocused()) {
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
			super.split();
			final StyleSheet styleSheet = htmlEditorPanel.getDocument().getStyleSheet();
			StyleSheetConfigurer.resetStyles(styleSheet, 1);
			getBase().getEditControl().split(HtmlUtils.unescapeHTMLUnicodeEntity(htmlEditorPanel.getDocumentText()),
			    htmlEditorPanel.getCaretPosition());
		}

		/*
		 * (non-Javadoc)
		 * @see freeplane.view.mindmapview.EditNodeBase.Dialog#close()
		 */
		@Override
		protected void submit() {
			super.submit();
			StyleSheetConfigurer.resetStyles(htmlEditorPanel.getDocument().getStyleSheet(), 1);
			if (htmlEditorPanel.needsSaving()) {
				getBase().getEditControl().ok(HtmlUtils.unescapeHTMLUnicodeEntity(htmlEditorPanel.getDocumentText()));
			}
			else {
				getBase().getEditControl().cancel();
			}
		}

		public void setSplitEnabled(boolean enableSplit) {
			splitButton.setEnabled(enableSplit);
	        splitButton.setVisible(enableSplit);
        }

		public void updateStyleSheet(String rule, StyleSheet customStyleSheet) {
			final StyleSheet styleSheet = htmlEditorPanel.getDocument().getStyleSheet();
			StyleSheetConfigurer.resetStyles(styleSheet, 1);
			ownStyleSheet.removeStyle("body");
			ownStyleSheet.removeStyle("p");
			ownStyleSheet.addRule(rule);
			styleSheet.addStyleSheet(ownStyleSheet);
			styleSheet.addStyleSheet(customStyleSheet);
		}
	}
	private static final Dimension PREFERRED_CONTENT_SIZE = new Dimension(600, 400);


	private String title;

	private Font font;

	private StyleSheet customStyleSheet = NodeCss.EMPTY.getStyleSheet();

	private Color textColor = Color.BLACK;
	private Dimension preferredContentSize = PREFERRED_CONTENT_SIZE;

	private int horizontalAlignment = HorizontalTextAlignment.DEFAULT.swingConstant;
	private ComponentOrientation componentOrientation = TextWritingDirection.DEFAULT.componentOrientation;

	public String getTitle() {
    	return title;
    }

	public void setTitle(String purpose) {
    	this.title = purpose;
    }

	public Font getFont() {
    	return font;
    }

	public void setFont(Font font) {
    	this.font = font;
    }

	public Color getTextColor() {
    	return textColor;
    }

	public void setTextColor(Color textColor) {
    	this.textColor = textColor;
    }

	public StyleSheet getCustomStyleSheet() {
		return customStyleSheet;
	}

	public void setCustomStyleSheet(StyleSheet customStyleSheet) {
		this.customStyleSheet = customStyleSheet;
	}

	public Dimension getPreferredContentSize() {
    	return preferredContentSize;
    }

	public void setPreferredContentSize(Dimension preferredSize) {
    	this.preferredContentSize = preferredSize;
    }

	public EditNodeWYSIWYG(final NodeModel node, final String text, final IEditControl editControl, boolean enableSplit) {
		super(node, text, false, editControl);
	}

	@Override
	public void show(final RootPaneContainer frame) {
		try {
			HTMLDialog htmlEditorWindow = createHtmlEditor(frame);
			htmlEditorWindow.setBase(this);
			final String titleText;
			titleText = TextUtils.getText(title);
			JDialog dialog = htmlEditorWindow.getDialog();
            dialog.setTitle(titleText);
            getEditControl().getEditType().installHolder(node, dialog);
			htmlEditorWindow.setSplitEnabled(getEditControl().canSplit());
			final SHTMLPanel htmlEditorPanel = (htmlEditorWindow).getHtmlEditorPanel();
			final StringBuilder ruleBuilder = new StringBuilder(100);
			ruleBuilder.append("body {");
			ruleBuilder.append(new CssRuleBuilder()
					.withCSSFont(font, UITools.FONT_SCALE_FACTOR)
					.withColor(textColor)
					.withBackground(getBackground())
					.withAlignment(horizontalAlignment));
			ruleBuilder.append("}\n");
			if(getEditControl().getEditType() != EditedComponent.NOTE
					|| ResourceController.getResourceController().getBooleanProperty(
			        MNoteController.RESOURCES_USE_MARGIN_TOP_ZERO_FOR_NOTES))
				ruleBuilder.append("p {margin-top:0;}\n");
			final HTMLDocument document = htmlEditorPanel.getDocument();
			final JEditorPane editorPane = htmlEditorPanel.getEditorPane();
			editorPane.setComponentOrientation(componentOrientation);
			if(textColor != null){
				editorPane.setForeground(textColor);
				editorPane.setCaretColor(textColor);
			}
			htmlEditorWindow.updateStyleSheet(ruleBuilder.toString(), customStyleSheet);
			final URL url = node.getMap().getURL();
			if (url != null) {
				document.setBase(url);
			}
			else {
				document.setBase(new URL("file: "));
			}
			htmlEditorPanel.setContentPanePreferredSize(preferredContentSize);
			restoreDialogSize(dialog);
			dialog.pack();
			dialog.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                   saveDialogSize(dialog);
                }

            });
			if (ResourceController.getResourceController().getBooleanProperty("el__position_window_below_node")) {
				UITools.setDialogLocationUnder(dialog, node);
			}
			else {
				UITools.setDialogLocationRelativeTo(dialog, node);
			}
			String content = getText();
			if (!HtmlUtils.isHtml(content)) {
				content = HtmlUtils.plainToHTML(content);
			}
			HtmlProcessor.configureUnknownTags(htmlEditorPanel.getDocument());
			htmlEditorPanel.setCurrentDocumentContent(content);
			final KeyEvent firstKeyEvent = MTextController.getController().getEventQueue().getFirstEvent();
			final JTextComponent currentPane = htmlEditorPanel.getEditorPane();
			if (currentPane == htmlEditorPanel.getMostRecentFocusOwner()) {
				redispatchKeyEvents(currentPane, firstKeyEvent);
				if (firstKeyEvent == null) {
					editorPane.setCaretPosition(htmlEditorPanel.getDocument().getLength());
				}
			}
			else{
				final EventBuffer keyEventDispatcher = MTextController.getController().getEventQueue();
				keyEventDispatcher.deactivate();
			}
			htmlEditorPanel.getMostRecentFocusOwner().requestFocus();
			htmlEditorWindow.show();
		}
		catch (final Exception ex) {
			LogUtils.severe("Loading of WYSIWYG HTML editor failed. Use the other editors instead.", ex);
		}
	}

	public HTMLDialog createHtmlEditor(final RootPaneContainer frame) throws Exception {
			HTMLDialog htmlEditorWindow = new HTMLDialog(this, "", "", frame);
		   	ResourceController.getResourceController().addPropertyChangeListener(
	    			new FreeplaneToSHTMLPropertyChangeAdapter("simplyhtml.window.", htmlEditorWindow.getHtmlEditorPanel()));
		   	return htmlEditorWindow;
    }

	public void setTextAlignment(int horizontalAlignment) {
		this.horizontalAlignment = horizontalAlignment;
	}

	public void setComponentOrientation(ComponentOrientation componentOrientation) {
		this.componentOrientation = componentOrientation;
	}
}
