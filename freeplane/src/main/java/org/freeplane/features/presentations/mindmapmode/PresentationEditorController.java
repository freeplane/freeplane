package org.freeplane.features.presentations.mindmapmode;

import java.awt.Component;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.freeplane.core.resources.components.OptionPanel;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.textchanger.TranslatedElementFactory;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.presentations.mindmapmode.PresentationStateChangeEvent.EventType;

public class PresentationEditorController {
	private final CollectionBoxController<Presentation> presentationPanelController;
	private final CollectionBoxController<Slide> slidePanelController;
	private final SlideEditorController slideEditorController;
	private final NavigationPanelController navigationPanelController;
	private NamedElementCollection<Presentation> presentations;
	private CollectionChangeListener<Presentation> presentationChangeListener;
	private AFreeplaneAction configureAction;

	public PresentationEditorController(final PresentationState presentationState) {
		presentationPanelController = new CollectionBoxController<>("presentation");
		slidePanelController = new CollectionBoxController<Slide>("slide");
		slideEditorController = new SlideEditorController(presentationState);
		presentationState.addPresentationStateListener(new PresentationStateChangeListener() {
			@Override
			public void onPresentationStateChange(PresentationStateChangeEvent presentationStateChangeEvent) {
				if (presentationStateChangeEvent.eventType == EventType.PLAYING_STATE_CHANGED) {
					if (presentationStateChangeEvent.presentationState.isPresentationRunning()) {
						presentationPanelController.disableEditing();
						slidePanelController.disableEditing();
					}
					else {
						presentationPanelController.enableEditing();
						slidePanelController.enableEditing();
					}
				}
			}
		});
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
					slides.removeCollectionChangeListener(slideChangeListener);
				Presentation presentation = event.collection != null ? event.collection.getCurrentElement() : null;
				if(presentation != null) {
					slides = presentation.slides;
					slides.addCollectionChangeListener(slideChangeListener);
					slideEditorController.setSlide(slides.getCurrentElement());
				} else {
					slides = null;
					slideEditorController.setSlide(null);
				}
				presentationState.changePresentation(event);
				slidePanelController.setCollection(slides);
				navigationPanelController.setPresentation(presentation);
			}
		};
	}

	public void setPresentations(NamedElementCollection<Presentation> newPresentations) {
		if(presentations != null)
			presentations.removeCollectionChangeListener(presentationChangeListener);
		presentations = newPresentations;
		presentationPanelController.setCollection(presentations);
		if (presentations != null) {
			Presentation presentation = presentations.getCurrentElement();
			if (presentation != null) {
				slidePanelController.setCollection(presentation.slides);
				slideEditorController.setSlide(presentation.slides.getCurrentElement());
			}
			presentations.addCollectionChangeListener(presentationChangeListener);
		}
		presentationChangeListener.onCollectionChange(CollectionChangedEvent.EventType.SELECTION_CHANGED.of(presentations));
	}

	public Component createPanel() {
		Box panel = Box.createVerticalBox();
		final JComponent presentationBox = presentationPanelController.createCollectionBox();
		TranslatedElementFactory.createTitledBorder(presentationBox, "slide.presentations");
		panel.add(presentationBox);
		final JComponent slideBox = slidePanelController.createCollectionBox();
		TranslatedElementFactory.createTitledBorder(slideBox, "slide.slides");
		panel.add(slideBox);
		JComponent content = slideEditorController.createSlideContentBox();
		panel.add(content);
		JComponent navigation = navigationPanelController.createNavigationBox();
		panel.add(navigation);
		JButton btnConfigure = new JButton(configureAction);
		btnConfigure.setActionCommand(OptionPanel.OPTION_PANEL_RESOURCE_PREFIX + "Presentation");
		btnConfigure.setAlignmentX(JButton.CENTER_ALIGNMENT);
		panel.add(Box.createVerticalStrut(btnConfigure.getPreferredSize().height / 2));
		panel.add(btnConfigure);
		return panel;
	}

	void registerActions(ModeController modeController) {
		navigationPanelController.registerActions(modeController);
		configureAction = modeController.getAction("PropertyAction");
	}

}
