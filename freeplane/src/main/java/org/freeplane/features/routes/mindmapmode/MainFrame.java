package org.freeplane.features.routes.mindmapmode;

import javax.swing.JFrame;

import org.freeplane.features.presentations.mindmapmode.PresentationEditorPanel;

public class MainFrame {
	public static void main(String... argv){
		final JFrame frame = new JFrame("Freeplane presentation editor");
		frame.getContentPane().add(new PresentationEditorPanel());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.show();
	}
}
