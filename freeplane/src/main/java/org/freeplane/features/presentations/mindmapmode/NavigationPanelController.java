package org.freeplane.features.presentations.mindmapmode;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.EnabledAction;
import org.freeplane.core.ui.SelectableAction;
import org.freeplane.core.ui.textchanger.TranslatedElementFactory;
import org.freeplane.features.mode.ModeController;

class NavigationPanelController {
	
	@SuppressWarnings("serial")
	@EnabledAction
	static private class StartPresentationOrShowNextSlideAction extends AFreeplaneAction {
		private final PresentationState presentationState;

		public StartPresentationOrShowNextSlideAction(PresentationState presentationState) {
			super("StartPresentationOrShowNextSlideAction");
			this.presentationState = presentationState;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if(presentationState.isPresentationRunning()) { 
				if (presentationState.canShowNextSlide())
					presentationState.showNextSlide();
			}
			else if (presentationState.canShowCurrentSlide())
				presentationState.showPresentation();
		}

		@Override
		public void afterMapChange(final Object newMap) {
		}
	}

	@SuppressWarnings("serial")
	@EnabledAction
	static private class StopPresentationAction extends AFreeplaneAction {
		private PresentationState presentationState;

		public StopPresentationAction(PresentationState presentationState) {
			super("StopPresentationAction");
			this.presentationState = presentationState;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (presentationState.isPresentationRunning())
				presentationState.stopPresentation();
		}

		@Override
		public void afterMapChange(final Object newMap) {
		}
	}
	@SuppressWarnings("serial")
	@EnabledAction
	static private class ShowNextSlideAction extends AFreeplaneAction {
		private final PresentationState presentationState;

		public ShowNextSlideAction(PresentationState presentationState) {
			super(null, null, null);
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
	static private class StartPresentationAction extends AFreeplaneAction {
		private PresentationState presentationState;

		public StartPresentationAction(PresentationState presentationState) {
			super(null, null, null);
			this.presentationState = presentationState;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (! presentationState.isPresentationRunning() && presentationState.canShowCurrentSlide())
				presentationState.showPresentation();
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
	private final AFreeplaneAction showPreviousSlideAction;

	private final StartPresentationOrShowNextSlideAction startPresentationOrShowNextSlideAction;

	private final AFreeplaneAction startPresentationAction;
	private final StopPresentationAction stopPresentationAction;

	public void setPresentation(Presentation presentation) {
		updateUi();
	}

	private void updateUi() {
		boolean isPresentationRunning = presentationState.isPresentationRunning();
		boolean canShowCurrentSlide = presentationState.canShowCurrentSlide();
		boolean canShowPreviousSlide = presentationState.canShowPreviousSlide();
		boolean canShowNextSlide = presentationState.canShowNextSlide();
		startPresentationOrShowNextSlideAction.setEnabled(! isPresentationRunning && canShowCurrentSlide || canShowNextSlide);
		startPresentationAction.setEnabled(canShowCurrentSlide && ! isPresentationRunning);
		stopPresentationAction.setEnabled(isPresentationRunning);
		showPreviousSlideAction.setEnabled(canShowPreviousSlide);
		showNextSlideAction.setEnabled(canShowNextSlide);
	}

	NavigationPanelController(final PresentationState presentationState){
		this.presentationState = presentationState;
		startPresentationOrShowNextSlideAction = new StartPresentationOrShowNextSlideAction(presentationState);
		stopPresentationAction = new StopPresentationAction(presentationState);
		showPreviousSlideAction = new ShowPreviousSlideAction(presentationState);
		startPresentationAction = new StartPresentationAction(presentationState);
		showNextSlideAction = new ShowNextSlideAction(presentationState);
		actions = new Action[]{startPresentationOrShowNextSlideAction, stopPresentationAction, startPresentationAction, showNextSlideAction, showNextSlideAction};
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

	JComponent createNavigationBox() {
		JButton btnPresent = TranslatedElementFactory.createButton(startPresentationAction, "slide.present");
		JButton btnStop = TranslatedElementFactory.createButton(stopPresentationAction, "slide.stop");
		JButton btnPrevious = TranslatedElementFactory.createButton(showPreviousSlideAction, "slide.previous");
		JButton btnNext = TranslatedElementFactory.createButton(showNextSlideAction, "slide.next");
		JPanel slideButtons = new JPanel(new GridLayout(2, 2));
		Box slideBox = Box.createHorizontalBox();
		TranslatedElementFactory.createTitledBorder(slideBox, "slide.presentation");
		slideButtons.add(btnPresent);
		slideButtons.add(btnStop);
		slideButtons.add(btnPrevious);
		slideButtons.add(btnNext);
		slideButtons.setAlignmentX(Box.CENTER_ALIGNMENT);
		slideButtons.setMaximumSize(slideButtons.getPreferredSize());
		slideBox.add(Box.createHorizontalGlue());
		slideBox.add(slideButtons);
		slideBox.add(Box.createHorizontalGlue());
		return slideBox;
	}

	void registerActions(ModeController modeController) {
		modeController.addAction(startPresentationOrShowNextSlideAction);
		modeController.addAction(stopPresentationAction);
		modeController.addAction(showPreviousSlideAction);
	}
	
}

