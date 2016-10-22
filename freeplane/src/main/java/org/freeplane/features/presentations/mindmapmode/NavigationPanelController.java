package org.freeplane.features.presentations.mindmapmode;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JToggleButton;

import org.freeplane.core.ui.textchanger.TranslatedElementFactory;

class NavigationPanelController {
	
	private final JButton btnPrevious;
	private final JToggleButton tglbtnCurrent;
	private final JButton btnNext;
	private final JToggleButton tglbtnHighlightNodes;
	private final JComponent[] components;
	
	private NamedElementCollection<Slide> slides;
	private final CollectionChangeListener<Slide> slideChangeListener;
	private PresentationState presentationState;

	public void setPresentation(Presentation presentation) {
		if(slides != null)
			slides.removeCollectionChangeListener(slideChangeListener);
		if(presentation != null) {
			this.slides = presentation.slides;
			updateUi();
			slides.addCollectionChangeListener(slideChangeListener);
		} else
			this.slides = null;
	}

	private void updateUi() {
		tglbtnCurrent.setSelected(presentationState.isPresentationRunning());
		tglbtnHighlightNodes.setSelected(presentationState.highlightsNodes());
		btnPrevious.setEnabled(presentationState.canShowPreviousSlide());
		final boolean canShowCurrentSlide = presentationState.canShowCurrentSlide();
		tglbtnCurrent.setEnabled(canShowCurrentSlide);
		tglbtnHighlightNodes.setEnabled(canShowCurrentSlide);
		btnNext.setEnabled(presentationState.canShowNextSlide());
	}

	NavigationPanelController(final PresentationState presentationState){
		this.presentationState = presentationState;
		btnPrevious = createPreviousButton();
		tglbtnCurrent = createCurrentButton();
		btnNext = createNextButton();
		tglbtnHighlightNodes = createHighlightSlideNodesButton();
		components = new JComponent[]{btnPrevious, tglbtnCurrent, btnNext, tglbtnHighlightNodes};
		PresentationStateChangeListener presentationStateListener = new PresentationStateChangeListener() {
			@Override
			public void onPresentationStateChange(PresentationStateChangeEvent presentationStateChangeEvent) {
				updateUi();
			}
		};
		presentationState.addPresentationStateListener(presentationStateListener);
		slideChangeListener = new CollectionChangeListener<Slide>() {
			
			@Override
			public void onCollectionChange(CollectionChangedEvent<Slide> event) {
				presentationState.stopPresentation();
				updateUi();
			}
		};
		disableUi();
	}

	private void disableUi() {
		for(JComponent c : components)
			c.setEnabled(false);
	}

	private JButton createNextButton() {
		final JButton btnNext = TranslatedElementFactory.createButton("slide.next");
		btnNext.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				presentationState.showNextSlide();
			}
		});
		return btnNext;
	}

	private JToggleButton createCurrentButton() {
		final JToggleButton btnCurrent = TranslatedElementFactory.createToggleButton("slide.current");
		btnCurrent.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (presentationState.isPresentationRunning())
					presentationState.stopPresentation();
				else
					presentationState.showSlide();
			}
		});
		return btnCurrent;
	}
	private JToggleButton createHighlightSlideNodesButton() {
		final JToggleButton btnHighlight = TranslatedElementFactory.createToggleButton("slide.highlight");
		btnHighlight.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				presentationState.setHighlightsNodes(! presentationState.highlightsNodes());
			}
		});
		return btnHighlight;
	}
	
	private JButton createPreviousButton() {
		final JButton btnPrevious = TranslatedElementFactory.createButton("slide.previous");
		btnPrevious.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				presentationState.showPreviousSlide();
			}
		});
		return btnPrevious;
	}
	
	Box createNavigationBox() {
		Box navigation = Box.createVerticalBox();
		TranslatedElementFactory.createTitledBorder(navigation, "slide.show");
		Box slideButtons = Box.createHorizontalBox();
		navigation.add(slideButtons);
		slideButtons.add(Box.createHorizontalGlue());
		slideButtons.add(btnPrevious);
		slideButtons.add(tglbtnCurrent);
		slideButtons.add(btnNext);
		slideButtons.add(Box.createHorizontalGlue());
		slideButtons.setAlignmentX(Box.CENTER_ALIGNMENT);
		tglbtnHighlightNodes.setAlignmentX(Box.CENTER_ALIGNMENT);
		navigation.add(tglbtnHighlightNodes);
		return navigation;
	}
	
}