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
import org.freeplane.core.ui.menubuilders.generic.UserRole;
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
					presentationState.showNextSlide();
			}
			else if (presentationState.canShowCurrentSlide())
				presentationState.showPresentation();
		}

		@Override
		public void afterMapChange(UserRole userRole, boolean isMapSelected) {
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
		public void afterMapChange(UserRole userRole, boolean isMapSelected) {
		}
	}
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
				presentationState.showNextSlide();
		}
		
		@Override
		public void afterMapChange(UserRole userRole, boolean isMapSelected) {
		}
	}
	
	@SuppressWarnings("serial")
	@SelectableAction
	@EnabledAction
	static private class StartPresentationAction extends AFreeplaneAction {
		private PresentationState presentationState;

		public StartPresentationAction(PresentationState presentationState) {
			super("StartPresentationAction");
			this.presentationState = presentationState;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (! presentationState.isPresentationRunning() && presentationState.canShowCurrentSlide())
				presentationState.showPresentation();
		}

		@Override
		public void afterMapChange(UserRole userRole, boolean isMapSelected) {
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
				presentationState.showPreviousSlide();
		}

		@Override
		public void afterMapChange(UserRole userRole, boolean isMapSelected) {
		}
	}


	@SuppressWarnings("serial")
	@EnabledAction
	static private class ShowFirstSlideAction extends AFreeplaneAction {
		private PresentationState presentationState;

		public ShowFirstSlideAction(PresentationState presentationState) {
			super("ShowFirstSlideAction");
			this.presentationState = presentationState;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
				presentationState.showFirstSlide();
		}

		@Override
		public void afterMapChange(UserRole userRole, boolean isMapSelected) {
		}
	}
	
	@SuppressWarnings("serial")
	@EnabledAction
	static private class ShowFirstSlideAndStartPresentationAction extends AFreeplaneAction {
		private PresentationState presentationState;

		public ShowFirstSlideAndStartPresentationAction(PresentationState presentationState) {
			super("ShowFirstSlideAndStartPresentationAction");
			this.presentationState = presentationState;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			presentationState.showFirstSlide();
			if(! presentationState.isPresentationRunning() && presentationState.canShowCurrentSlide())
				presentationState.showPresentation();
		}

		@Override
		public void afterMapChange(UserRole userRole, boolean isMapSelected) {
		}
	}
	
	@SuppressWarnings("serial")
	@EnabledAction
	static private class ShowLastSlideAction extends AFreeplaneAction {
		private PresentationState presentationState;

		public ShowLastSlideAction(PresentationState presentationState) {
			super("ShowLastSlideAction");
			this.presentationState = presentationState;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
				presentationState.showLastSlide();
		}

		@Override
		public void afterMapChange(UserRole userRole, boolean isMapSelected) {
		}
	}
	
	private final Action[] actions;
	
	private final PresentationState presentationState;
	private final AFreeplaneAction showNextSlideAction;
	private final AFreeplaneAction showPreviousSlideAction;
	private final AFreeplaneAction startPresentationOrShowNextSlideAction;

	private final AFreeplaneAction showFirstSlideAction;
	private final AFreeplaneAction showLastSlideAction;
	private final AFreeplaneAction showFirstSlideAndStartPresentationAction;

	private final AFreeplaneAction startPresentationAction;
	private final StopPresentationAction stopPresentationAction;

	NavigationPanelController(final PresentationState presentationState){
		this.presentationState = presentationState;
		startPresentationOrShowNextSlideAction = new StartPresentationOrShowNextSlideAction(presentationState);
		stopPresentationAction = new StopPresentationAction(presentationState);
		showPreviousSlideAction = new ShowPreviousSlideAction(presentationState);
		startPresentationAction = new StartPresentationAction(presentationState);
		showNextSlideAction = new ShowNextSlideAction(presentationState);
		showFirstSlideAction = new ShowFirstSlideAction(presentationState);
		showLastSlideAction = new ShowLastSlideAction(presentationState);
		showFirstSlideAndStartPresentationAction = new ShowFirstSlideAndStartPresentationAction(presentationState);
		actions = new Action[]{startPresentationOrShowNextSlideAction, stopPresentationAction, startPresentationAction, showNextSlideAction, showNextSlideAction,
				showFirstSlideAction, showLastSlideAction, showFirstSlideAndStartPresentationAction};
		PresentationStateChangeListener presentationStateListener = new PresentationStateChangeListener() {
			@Override
			public void onPresentationStateChange(PresentationStateChangeEvent presentationStateChangeEvent) {
				updateUi();
			}
		};
		presentationState.addPresentationStateListener(presentationStateListener);
		disableUi();
	}

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
		boolean canShowFirstSlide = presentationState.canShowFirstSlide();
		boolean canShowLastSlide = presentationState.canShowLastSlide();
		showFirstSlideAndStartPresentationAction.setEnabled(! isPresentationRunning || canShowFirstSlide);
		showFirstSlideAction.setEnabled(canShowFirstSlide);
		showLastSlideAction.setEnabled(canShowLastSlide);
	}

	private void disableUi() {
		for(Action a : actions)
			a.setEnabled(false);
	}

	JComponent createNavigationBox() {
		JButton btnPresent = TranslatedElementFactory.createButtonWithIcon(startPresentationAction, "StartPresentationAction.icon", "slide.present");
		JButton btnStop = TranslatedElementFactory.createButtonWithIcon(stopPresentationAction, "StopPresentationAction.icon", "slide.stop");
		JButton btnFirst = TranslatedElementFactory.createButtonWithIcon(showFirstSlideAction, "ShowFirstSlideAction.icon", "slide.first");
		JButton btnPrevious = TranslatedElementFactory.createButtonWithIcon(showPreviousSlideAction, "ShowPreviousSlideAction.icon", "slide.previous");
		JButton btnNext = TranslatedElementFactory.createButtonWithIcon(showNextSlideAction, "ShowNextSlideAction.icon", "slide.next");
		JButton btnLast = TranslatedElementFactory.createButtonWithIcon(showLastSlideAction, "ShowLastSlideAction.icon", "slide.last");
		JPanel slideButtons = new JPanel(new GridLayout(2, 4));
		Box slideBox = Box.createHorizontalBox();
		TranslatedElementFactory.createTitledBorder(slideBox, "slide.presentation");
		slideButtons.add(new JPanel());
		slideButtons.add(btnPresent);
		slideButtons.add(btnStop);
		slideButtons.add(new JPanel());
		slideButtons.add(btnFirst);
		slideButtons.add(btnPrevious);
		slideButtons.add(btnNext);
		slideButtons.add(btnLast);
		slideButtons.setAlignmentX(Box.CENTER_ALIGNMENT);
		slideButtons.setMaximumSize(slideButtons.getPreferredSize());
		slideBox.add(Box.createHorizontalGlue());
		slideBox.add(slideButtons);
		slideBox.add(Box.createHorizontalGlue());
		return slideBox;
	}

	void registerActions(ModeController modeController) {
		modeController.addAction(startPresentationOrShowNextSlideAction);
		modeController.addAction(showFirstSlideAndStartPresentationAction);
		modeController.addAction(startPresentationAction);
		modeController.addAction(stopPresentationAction);
		modeController.addAction(showFirstSlideAction);
		modeController.addAction(showNextSlideAction);
		modeController.addAction(showPreviousSlideAction);
		modeController.addAction(showLastSlideAction);
	}
	
}

