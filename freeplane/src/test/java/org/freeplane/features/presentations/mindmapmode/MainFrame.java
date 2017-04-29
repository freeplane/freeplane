package org.freeplane.features.presentations.mindmapmode;

import javax.swing.JFrame;

public class MainFrame {
	public static void main(String... argv){
		final JFrame frame = new JFrame("Freeplane presentation editor");
		final PresentationState presentationStateModel = new PresentationState();
		final PresentationEditorController presentationEditorController = new PresentationEditorController(presentationStateModel);
		frame.getContentPane().add(presentationEditorController.createPanel(null));
		final NamedElementFactory<Slide> slideFactory = new NamedElementFactory<Slide>(){

			@Override
			public Slide create(String name) {
				return new Slide(name);
			}

			@Override
			public Slide create(Slide prototype, String newName) {
				return prototype.saveAs(newName);
			}
			
		};
		final NamedElementFactory<Presentation> presentationFactory = new NamedElementFactory<Presentation>() {

			@Override
			public Presentation create(String name) {
				return new Presentation(name, slideFactory);
			}

			@Override
			public Presentation create(Presentation prototype, String newName) {
				return prototype.saveAs(newName);
			}
		};
		presentationEditorController.setPresentations(new NamedElementCollection<>(presentationFactory));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.show();
	}
}
