package org.freeplane.features.presentations.mindmapmode;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JToggleButton;
import javax.swing.border.TitledBorder;

import org.freeplane.features.presentations.CollectionChangeListener;
import org.freeplane.features.presentations.CollectionChangedEvent;
import org.freeplane.features.presentations.CollectionModel;
import org.freeplane.features.presentations.PresentationModel;
import org.freeplane.features.presentations.PresentationStateChangeEvent;
import org.freeplane.features.presentations.PresentationStateChangeListener;
import org.freeplane.features.presentations.PresentationStateModel;
import org.freeplane.features.presentations.SlideModel;

class NavigationPanelController {
	
	private final JButton btnPrevious;
	private final JToggleButton tglbtnCurrent;
	private final JButton btnNext;
	private final JComponent[] components;
	
	private CollectionModel<SlideModel> slides;
	private final CollectionChangeListener<SlideModel> slideChangeListener;
	private PresentationStateModel presentationStateModel;
	private PresentationModel presentationModel;

	public void setPresentationModel(PresentationModel presentationModel) {
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
				tglbtnCurrent.setSelected(presentationStateChangeEvent.presentationModel.getRunningPresentation() != null);
				updateUi();
			}
		};
		presentationStateModel.addPresentationStateListener(presentationStateListener);
		slideChangeListener = new CollectionChangeListener<SlideModel>() {
			
			@Override
			public void onCollectionChange(CollectionChangedEvent<SlideModel> event) {
				presentationStateModel.setRunningPresentation(null);
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
				presentationStateModel.setRunningPresentation(presentationModel);
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
					presentationStateModel.setRunningPresentation(presentationModel);
				else
					presentationStateModel.setRunningPresentation(null);
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
				presentationStateModel.setRunningPresentation(presentationModel);
			}
		});
		return btnPrevious;
	}
	
	Box createNavigationBox() {
		Box navigation = Box.createHorizontalBox();
		navigation.setBorder(new TitledBorder(null, "Show", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		navigation.add(btnPrevious);
		navigation.add(tglbtnCurrent);
		navigation.add(btnNext);
		return navigation;
	}
	
}