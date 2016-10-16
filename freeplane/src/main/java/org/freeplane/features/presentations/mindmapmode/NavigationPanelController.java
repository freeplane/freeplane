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
	
	private CollectionModel<Slide> slides;
	private final CollectionChangeListener<Slide> slideChangeListener;
	private PresentationStateModel presentationStateModel;
	private Presentation presentationModel;

	public void setPresentationModel(Presentation presentationModel) {
		this.presentationModel = presentationModel;
		if(slides != null)
			slides.removeSelectionChangeListener(slideChangeListener);
		if(presentationModel != null) {
			this.slides = presentationModel.slides;
			updateUi();
			slides.addSelectionChangeListener(slideChangeListener);
		} else
			this.slides = null;
	}

	private void updateUi() {
		btnPrevious.setEnabled(slides.getCurrentElementIndex() > 0);
		tglbtnCurrent.setEnabled(slides.getSize() != 0);
		btnNext.setEnabled(slides.getSize() != 0 && slides.getCurrentElementIndex() < slides.getSize() - 1);
	}

	NavigationPanelController(final PresentationStateModel presentationStateModel){
		this.presentationStateModel = presentationStateModel;
		btnPrevious = createPreviousButton();
		tglbtnCurrent = createCurrentButton();
		btnNext = createNextButton();
		components = new JComponent[]{btnPrevious, tglbtnCurrent, btnNext};
		PresentationStateChangeListener presentationStateListener = new PresentationStateChangeListener() {
			@Override
			public void onPresentationStateChange(PresentationStateChangeEvent presentationStateChangeEvent) {
				tglbtnCurrent.setSelected(presentationStateChangeEvent.presentationState.getRunningPresentation() != null);
				updateUi();
			}
		};
		presentationStateModel.addPresentationStateListener(presentationStateListener);
		slideChangeListener = new CollectionChangeListener<Slide>() {
			
			@Override
			public void onCollectionChange(CollectionChangedEvent<Slide> event) {
				presentationStateModel.changeSlide(null);
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
				final int currentElementIndex = slides.getCurrentElementIndex();
				slides.selectCurrentElement(currentElementIndex + 1);
				presentationStateModel.changeSlide(presentationModel);
			}
		});
		return btnNext;
	}

	private JToggleButton createCurrentButton() {
		final JToggleButton btnCurrent = new JToggleButton("Current");
		btnPrevious.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if(presentationStateModel.getRunningPresentation() == null)
					presentationStateModel.changeSlide(presentationModel);
				else
					presentationStateModel.changeSlide(null);
			}
		});
		return btnCurrent;
	}

	private JButton createPreviousButton() {
		final JButton btnPrevious = new JButton("Previous");
		btnPrevious.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				final int currentElementIndex = slides.getCurrentElementIndex();
				slides.selectCurrentElement(currentElementIndex - 1);
				presentationStateModel.changeSlide(presentationModel);
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