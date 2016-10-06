package org.freeplane.features.presentations.mindmapmode;

import javax.swing.JFrame;

import org.freeplane.features.presentations.CollectionModel;
import org.freeplane.features.presentations.PresentationModel;
import org.freeplane.features.presentations.PresentationStateModel;

public class MainFrame {
	public static void main(String... argv){
		final JFrame frame = new JFrame("Freeplane presentation editor");
		final PresentationStateModel presentationStateModel = new PresentationStateModel();
		final PresentationEditorController presentationEditorController = new PresentationEditorController(presentationStateModel);
		frame.getContentPane().add(presentationEditorController.createPanel());
		presentationEditorController.setPresentations(new CollectionModel<>(PresentationModel.class));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.show();
	}
}
