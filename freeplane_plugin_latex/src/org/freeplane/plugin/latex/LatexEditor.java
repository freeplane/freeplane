package org.freeplane.plugin.latex;

import java.awt.Dimension;

import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;

/**
 * @author Stefan Ott
 * 
 * This class has only one static method to show the editor for Latex-fomulas
 */
public class LatexEditor {
	/**
	 * This method shows the Latex editor and sets the equation to be rendered from Latex
	 * @param oldEquation TODO
	 * 
	 * @param nodeHook: reference to the node
	 * @param latexExtension: the latexExtension
	 */
	public static String editLatex(String oldEquation) {
		final JEditorPane textArea = new JEditorPane();
		final JScrollPane editorScrollPane = new JScrollPane(textArea);
		editorScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		editorScrollPane.setPreferredSize(new Dimension(700, 200));
		final JOptionPane editPane = new JOptionPane(editorScrollPane, JOptionPane.PLAIN_MESSAGE,
		    JOptionPane.OK_CANCEL_OPTION) {
			private static final long serialVersionUID = 1L;

			@Override
			public void selectInitialValue() {
				textArea.requestFocusInWindow(); // (not pPanel)
			}
		};
		final JDialog edit = editPane.createDialog(null, LatexViewer.editorTitle);
		edit.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		// set content and rendering for textArea
		textArea.setContentType("text/groovy"); /* text/groovy is from JSyntaxPane */
		textArea.setText(oldEquation);
		editorScrollPane.requestFocusInWindow();
		edit.setVisible(true);
		if (editPane.getValue().equals(JOptionPane.OK_OPTION)) {
			final String eq = textArea.getText();
			return eq;
		}
		return null;
	}
}
