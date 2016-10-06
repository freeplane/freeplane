package org.freeplane.features.presentations.mindmapmode;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.freeplane.features.presentations.CollectionModel;
import org.freeplane.features.presentations.PresentationModel;
import org.freeplane.features.presentations.PresentationStateModel;
import org.freeplane.features.presentations.CollectionChangeListener;
import org.freeplane.features.presentations.CollectionChangedEvent;
import org.freeplane.features.presentations.SlideModel;

@SuppressWarnings("serial")
public class PresentationEditorPanel extends JPanel {

	/**
	 * Create the panel.
	 */
	public PresentationEditorPanel() {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		final CollectionBoxController<PresentationModel> presentationPanelController = new CollectionBoxController<>("New presentation");
		add(presentationPanelController.createCollectionBox("Presentations"));
		final CollectionModel<PresentationModel> presentations = new CollectionModel<>(PresentationModel.class);
		presentationPanelController.setCollection(presentations);
		final CollectionBoxController<SlideModel> slidePanelController = new CollectionBoxController<SlideModel>("New slide");
		final SlideEditorController slideEditorController = new SlideEditorController();
		final PresentationStateModel presentationStateModel = new PresentationStateModel();
		final NavigationPanelController navigationPanelController = new NavigationPanelController(presentationStateModel);
		final CollectionChangeListener<PresentationModel> presentationChangeListener = new CollectionChangeListener<PresentationModel>() {
			final CollectionChangeListener<SlideModel> slideChangeListener = new CollectionChangeListener<SlideModel>() {
				
				private SlideModel slide;

				@Override
				public void onCollectionChange(CollectionChangedEvent<SlideModel> event) {
					slide = event.collection.getCurrentElement();
					slideEditorController.setSlide(slide);
				}
			};
			
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
		presentations.addSelectionChangeListener(presentationChangeListener);
		
		add(slidePanelController.createCollectionBox("Slides"));
		JComponent content = slideEditorController.createSlideContentBox();
		add(content);
		Box navigation = navigationPanelController.createNavigationBox();
		add(navigation);
	}

}
