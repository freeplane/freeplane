package org.freeplane.features.presentations.mindmapmode;

import javax.swing.JFrame;

public class MainFrame {
	public static void main(String... argv){
		final JFrame frame = new JFrame("Freeplane presentation editor");
		final PresentationState presentationStateModel = new PresentationState();
		final PresentationEditorController presentationEditorController = new PresentationEditorController(presentationStateModel);
		frame.getContentPane().add(presentationEditorController.createPanel());
		presentationEditorController.setPresentations(new CollectionModel<>(Presentation.class));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.show();
	}
}
