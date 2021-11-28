package org.freeplane.features.nodestyle.mindmapmode;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.IOException;
import java.util.Scanner;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.StyleSheet;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.FocusRequestor;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.ui.components.html.ScaledEditorKit;
import org.freeplane.core.ui.components.html.StyleSheetConfigurer;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.Controller;

class CssEditor {
	private static final int PREFERRED_SCROLL_PANE_HEIGHT = (int )(UITools.FONT_SCALE_FACTOR * 200);
	private static final Dimension PREFERRED_SCROLL_PANE_SIZE = new Dimension(2 * PREFERRED_SCROLL_PANE_HEIGHT, 
			PREFERRED_SCROLL_PANE_HEIGHT);
	private String newCss;
	private static final String previewHtml = loadPreview();
	private static String loadPreview() {
		try(Scanner s = new Scanner(ResourceController.getResourceController().getResourceStream("/preview/cssPreview.html"))){
			s.useDelimiter("\\A");
			return s.hasNext() ? s.next() : "";
		} catch (IOException e) {
			LogUtils.severe(e);
			return "";
		}
	}

	
	private final JTextArea editor;
	private final Box box;
	private ScaledEditorKit kit;
	private JEditorPane preview;
	private StyleSheet cssSheet;
	private StyleSheet documentSheet;
	
	CssEditor(){
		editor = new JTextArea();
		FocusRequestor.requestFocus(editor);
		kit = ScaledEditorKit.create();

		JScrollPane editorScrollPane = new JScrollPane(editor, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		TitledBorder editorBorder = BorderFactory.createTitledBorder(editorScrollPane.getBorder());
		editorBorder.setTitle(TextUtils.getText("EditNodeCss"));
		editorScrollPane.setBorder(editorBorder);
		editorScrollPane.setPreferredSize(PREFERRED_SCROLL_PANE_SIZE);
		
		preview = new JEditorPane();
		preview.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.FALSE);
		preview.setOpaque(true);
		preview.setEditable(false);
		preview.setEditorKitForContentType("text/html", kit);
		preview.setContentType("text/html");
		HTMLDocument document = (HTMLDocument) preview.getDocument();
		documentSheet = document.getStyleSheet();
		StyleSheet defaultSheet = StyleSheetConfigurer.createDefaultStyleSheet();
		defaultSheet.addRule("body {font-size: 10pt;} p {margin-top:0;}");
		documentSheet.addStyleSheet(defaultSheet);
	    JScrollPane previewScrollPane = new JScrollPane(preview, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		previewScrollPane.setPreferredSize(PREFERRED_SCROLL_PANE_SIZE);
		TitledBorder previewBorder = BorderFactory.createTitledBorder(previewScrollPane.getBorder());
		previewBorder.setTitle(TextUtils.getText("simplyhtml.previewLabel"));
		previewScrollPane.setBorder(previewBorder);
		box = Box.createHorizontalBox();
		box.add(editorScrollPane);
		JButton previewButton = new JButton("->");
		previewButton.addActionListener(x -> updateDocument());
		box.add(previewButton);
		box.add(previewScrollPane);

	}

	private void updateDocument() {
		if(cssSheet != null)
			documentSheet.removeStyleSheet(cssSheet);
		cssSheet = new StyleSheet();
		cssSheet.addRule(editor.getText());
		documentSheet.addStyleSheet(cssSheet);
		Rectangle visibleRect = preview.getVisibleRect();
		preview.setText(previewHtml);
		SwingUtilities.invokeLater(() -> preview.scrollRectToVisible(visibleRect));
	}

	int editCss(String css) {
		editor.setText(css);
		updateDocument();
		editor.setCaretPosition(0);
		preview.setCaretPosition(0);
		
		int result = UITools.showConfirmDialog(Controller.getCurrentController().getSelection().getSelected(), 
				box, 
				TextUtils.getText("EditNodeCss"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		newCss = editor.getText();
		if (result == JOptionPane.OK_OPTION && ! css.equals(newCss)) {
			return JOptionPane.OK_OPTION;
		}
		else
			return JOptionPane.CANCEL_OPTION;
	}

	public String getNewCss() {
		return newCss;
	}
	
	
}