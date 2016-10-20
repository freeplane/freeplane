package org.freeplane.features.presentations.mindmapmode;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JToggleButton;
import javax.swing.border.TitledBorder;

class NavigationPanelController {
	
	private final JButton btnPrevious;
	private final JToggleButton tglbtnCurrent;
	private final JButton btnNext;
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
		btnPrevious.setEnabled(presentationState.canShowPreviousSlide());
		tglbtnCurrent.setEnabled(slides.getSize() != 0);
		btnNext.setEnabled(presentationState.canShowNextSlide());
	}

	NavigationPanelController(final PresentationState presentationState){
		this.presentationState = presentationState;
		btnPrevious = createPreviousButton();
		tglbtnCurrent = createCurrentButton();
		btnNext = createNextButton();
		components = new JComponent[]{btnPrevious, tglbtnCurrent, btnNext};
		PresentationStateChangeListener presentationStateListener = new PresentationStateChangeListener() {
			@Override
			public void onPresentationStateChange(PresentationStateChangeEvent presentationStateChangeEvent) {
				tglbtnCurrent.setSelected(presentationStateChangeEvent.presentationState.isPresentationRunning());
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
		final JButton btnNext = new JButton("Next");
		btnNext.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				presentationState.showNextSlide();
			}
		});
		return btnNext;
	}

	private JToggleButton createCurrentButton() {
		final JToggleButton btnCurrent = new JToggleButton("Current");
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

	private JButton createPreviousButton() {
		final JButton btnPrevious = new JButton("Previous");
		btnPrevious.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				presentationState.showPreviousSlide();
			}
		});
		return btnPrevious;
	}
	
	Box createNavigationBox() {
		Box navigation = Box.createHorizontalBox();
		navigation.setBorder(new TitledBorder(null, "Show", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		navigation.add(Box.createHorizontalGlue());
		navigation.add(btnPrevious);
		navigation.add(tglbtnCurrent);
		navigation.add(btnNext);
		navigation.add(Box.createHorizontalGlue());
		return navigation;
	}
	
}