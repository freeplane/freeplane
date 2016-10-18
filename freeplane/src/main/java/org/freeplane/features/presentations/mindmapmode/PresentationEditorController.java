package org.freeplane.features.presentations.mindmapmode;

import java.awt.Component;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;

public class PresentationEditorController {
	private final CollectionBoxController<Presentation> presentationPanelController;
	private final CollectionBoxController<Slide> slidePanelController;
	private final SlideEditorController slideEditorController;
	private final NavigationPanelController navigationPanelController;
	private NamedElementCollection<Presentation> presentations;
	private CollectionChangeListener<Presentation> presentationChangeListener;

	public PresentationEditorController(final PresentationState presentationState) {
		presentationPanelController = new CollectionBoxController<>("New presentation");
		slidePanelController = new CollectionBoxController<Slide>("New slide");
		slideEditorController = new SlideEditorController();
		navigationPanelController = new NavigationPanelController(presentationState);
		final CollectionChangeListener<Slide> slideChangeListener = new CollectionChangeListener<Slide>() {
			
			private Slide slide;

			@Override
			public void onCollectionChange(CollectionChangedEvent<Slide> event) {
				slide = event.collection.getCurrentElement();
				slideEditorController.setSlide(slide);
			}
		};
		presentationChangeListener = new CollectionChangeListener<Presentation>() {
			
			private NamedElementCollection<Slide> slides;

			@Override
			public void onCollectionChange(CollectionChangedEvent<Presentation> event) {
				if(slides != null)
					slides.removeSelectionChangeListener(slideChangeListener);
				Presentation presentation = event.collection.getCurrentElement();
				if(presentation != null) {
					slides = presentation.slides;
					slides.addSelectionChangeListener(slideChangeListener);
					slideEditorController.setSlide(slides.getCurrentElement());
				} else {
					slides = null;
					slideEditorController.setSlide(null);
				}
				presentationState.changePresentation(presentation);
				slidePanelController.setCollection(slides);
				navigationPanelController.setPresentation(presentation);
			}
		};
	}

	public void setPresentations(NamedElementCollection<Presentation> newPresentations) {
		if(presentations != null)
			presentations.removeSelectionChangeListener(presentationChangeListener);
		presentations = newPresentations;
		presentationPanelController.setCollection(presentations);
		if(presentations != null)
			presentations.addSelectionChangeListener(presentationChangeListener);
	}

	public Component createPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.add(presentationPanelController.createCollectionBox("Presentations"));
		panel.add(slidePanelController.createCollectionBox("Slides"));
		JComponent content = slideEditorController.createSlideContentBox();
		panel.add(content);
		Box navigation = navigationPanelController.createNavigationBox();
		panel.add(navigation);
		return panel;
	}

}
