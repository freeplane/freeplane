package org.freeplane.features.presentations.mindmapmode;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JToggleButton;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.EnabledAction;
import org.freeplane.core.ui.SelectableAction;
import org.freeplane.core.ui.textchanger.TranslatedElementFactory;
import org.freeplane.features.mode.ModeController;

class NavigationPanelController {
	
	@SuppressWarnings("serial")
	@EnabledAction
	static private class ShowNextSlideAction extends AFreeplaneAction {
		private final PresentationState presentationState;

		public ShowNextSlideAction(PresentationState presentationState) {
			super("ShowNextSlideAction");
			this.presentationState = presentationState;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (presentationState.canShowNextSlide())
				presentationState.showNextSlide();
		}

		@Override
		public void afterMapChange(final Object newMap) {
		}
	}
	@SuppressWarnings("serial")
	@SelectableAction
	@EnabledAction
	static private class ShowCurrentSlideAction extends AFreeplaneAction {
		private PresentationState presentationState;

		public ShowCurrentSlideAction(PresentationState presentationState) {
			super("ShowCurrentSlideAction");
			this.presentationState = presentationState;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (presentationState.isPresentationRunning())
				presentationState.stopPresentation();
			else if (presentationState.canShowCurrentSlide())
				presentationState.showSlide();
			setSelected(presentationState.isPresentationRunning());
		}

		@Override
		public void afterMapChange(final Object newMap) {
		}
	}


	@SuppressWarnings("serial")
	@EnabledAction
	static private class ShowPreviousSlideAction extends AFreeplaneAction {
		private PresentationState presentationState;

		public ShowPreviousSlideAction(PresentationState presentationState) {
			super("ShowPreviousSlideAction");
			this.presentationState = presentationState;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (presentationState.canShowPreviousSlide())
				presentationState.showPreviousSlide();
		}

		@Override
		public void afterMapChange(final Object newMap) {
		}
	}

	
	private final Action[] actions;
	
	private final PresentationState presentationState;
	private final AFreeplaneAction showNextSlideAction;
	private final AFreeplaneAction showCurrentSlideAction;
	private final ShowPreviousSlideAction showPreviousSlideAction;

	public void setPresentation(Presentation presentation) {
		updateUi();
	}

	private void updateUi() {
		showCurrentSlideAction.setSelected(presentationState.isPresentationRunning());
		showPreviousSlideAction.setEnabled(presentationState.canShowPreviousSlide());
		showNextSlideAction.setEnabled(presentationState.canShowNextSlide());
		final boolean canShowCurrentSlide = presentationState.canShowCurrentSlide();
		showCurrentSlideAction.setEnabled(canShowCurrentSlide);
	}

	NavigationPanelController(final PresentationState presentationState){
		this.presentationState = presentationState;
		showPreviousSlideAction = new ShowPreviousSlideAction(presentationState);
		showCurrentSlideAction = new ShowCurrentSlideAction(presentationState);
		showNextSlideAction = new ShowNextSlideAction(presentationState);
		actions = new Action[]{showCurrentSlideAction, showNextSlideAction};
		PresentationStateChangeListener presentationStateListener = new PresentationStateChangeListener() {
			@Override
			public void onPresentationStateChange(PresentationStateChangeEvent presentationStateChangeEvent) {
				updateUi();
			}
		};
		presentationState.addPresentationStateListener(presentationStateListener);
		disableUi();
	}

	private void disableUi() {
		for(Action a : actions)
			a.setEnabled(false);
	}

	Box createNavigationBox() {
		JButton btnPrevious = TranslatedElementFactory.createButton(showPreviousSlideAction, "slide.previous");
		JToggleButton tglbtnCurrent = TranslatedElementFactory.createToggleButton(showCurrentSlideAction, "slide.current");
		JButton btnNext = TranslatedElementFactory.createButton(showNextSlideAction, "slide.next");
		Box slideButtons = Box.createHorizontalBox();
		TranslatedElementFactory.createTitledBorder(slideButtons, "slide.show");
		slideButtons.add(Box.createHorizontalGlue());
		slideButtons.add(btnPrevious);
		slideButtons.add(tglbtnCurrent);
		slideButtons.add(btnNext);
		slideButtons.add(Box.createHorizontalGlue());
		slideButtons.setAlignmentX(Box.CENTER_ALIGNMENT);
		return slideButtons;
	}

	void registerActions(ModeController modeController) {
		modeController.addAction(showCurrentSlideAction);
		modeController.addAction(showNextSlideAction);
		modeController.addAction(showPreviousSlideAction);
	}
	
}

