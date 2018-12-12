package org.freeplane.features.clipboard.mindmapmode;

import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;

import org.freeplane.features.clipboard.ClipboardController;
import org.freeplane.features.clipboard.ClipboardControllers;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;

public class MClipboardControllers extends ClipboardControllers{

	private final SortedSet<MClipboardController> controllers;

	public MClipboardControllers(){
		controllers = new TreeSet<>(this::prioritySort);
		createActions();
	}
	private void createActions() {
		final ModeController modeController = Controller.getCurrentModeController();
		modeController.addAction(new CutAction());
		modeController.addAction(new PasteAction());
	}

	public boolean add(MClipboardController e) {
		return controllers.add(e);
	}

	@Override
	public void copy() {
		final Optional<MClipboardController> controller = controllers.stream()
				.filter(ClipboardController::canCopy).findFirst();
		if(controller.isPresent()) {
			controller.ifPresent(ClipboardController::copy);
		}
		else {
			super.copy();
		}
	}

	public void cut() {
		controllers.stream().filter(MClipboardController::canCut)
			.findFirst().ifPresent(MClipboardController::cut);
	}

	public void paste() {
		controllers.stream().filter(MClipboardController::canPaste)
			.findFirst().ifPresent(MClipboardController::paste);
	}

}
