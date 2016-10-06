package org.freeplane.features.presentations.mindmapmode;

import java.awt.Component;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.freeplane.features.presentations.CollectionChangeListener;
import org.freeplane.features.presentations.CollectionChangedEvent;
import org.freeplane.features.presentations.CollectionModel;
import org.freeplane.features.presentations.PresentationModel;
import org.freeplane.features.presentations.PresentationStateModel;
import org.freeplane.features.presentations.SlideModel;

public class PresentationEditorController {
	private final CollectionBoxController<PresentationModel> presentationPanelController;
	private final CollectionBoxController<SlideModel> slidePanelController;
	private final SlideEditorController slideEditorController;
	private final NavigationPanelController navigationPanelController;
	private CollectionModel<PresentationModel> presentations;
	private CollectionChangeListener<PresentationModel> presentationChangeListener;

	public PresentationEditorController(PresentationStateModel presentationStateModel) {
		presentationPanelController = new CollectionBoxController<>("New presentation");
		slidePanelController = new CollectionBoxController<SlideModel>("New slide");
		slideEditorController = new SlideEditorController();
		navigationPanelController = new NavigationPanelController(presentationStateModel);
		final CollectionChangeListener<SlideModel> slideChangeListener = new CollectionChangeListener<SlideModel>() {
			
			private SlideModel slide;

			@Override
			public void onCollectionChange(CollectionChangedEvent<SlideModel> event) {
				slide = event.collection.getCurrentElement();
				slideEditorController.setSlide(slide);
			}
		};
		presentationChangeListener = new CollectionChangeListener<PresentationModel>() {
			
			private CollectionModel<SlideModel> slides;

			@Override
			public void onCollectionChange(CollectionChangedEvent<PresentationModel> event) {
				if(slides != null)
					slides.removeSelectionChangeListener(slideChangeListener);
				PresentationModel presentationModel = event.collection.getCurrentElement();
				if(presentationModel != null) {
					slides = presentationModel.slides;
					slides.addSelectionChangeListener(slideChangeListener);
					slideEditorController.setSlide(slides.getCurrentElement());
				} else {
					slides = null;
					slideEditorController.setSlide(null);
				}
				slidePanelController.setCollection(slides);
				navigationPanelController.setPresentationModel(presentationModel);
			}
		};
	}

	public void setPresentations(CollectionModel<PresentationModel> newPresentations) {
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
