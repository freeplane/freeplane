package org.freeplane.features.clipboard;

import java.util.SortedSet;
import java.util.TreeSet;

import org.freeplane.core.extension.IExtension;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;

public class ClipboardControllers implements IExtension{
	public static void install( final ClipboardControllers clipboardController) {
		Controller.getCurrentModeController().addExtension(ClipboardControllers.class, clipboardController);
		Controller.getCurrentModeController().addExtension(ClipboardAccessor.class, new ClipboardAccessor());
	}
	public static ClipboardControllers getController() {
		return Controller.getCurrentModeController().getExtension(ClipboardControllers.class);
	}

	private final SortedSet<ClipboardController> controllers;


	public ClipboardControllers() {
		super();
		controllers = new TreeSet<>(this::prioritySort);
		createActions();
	}

	protected int prioritySort(ClipboardController x, ClipboardController y) {
		return -Integer.compare(x.getPriority(), y.getPriority());
	}


	private void createActions() {
		final Controller controller = Controller.getCurrentController();
		ModeController modeController = controller.getModeController();
		modeController.addAction(new CopyAction());
	}
	public boolean add(ClipboardController e) {
		return controllers.add(e);
	}

	public void copy() {
		controllers.stream().filter(ClipboardController::canCopy)
			.findFirst().ifPresent(ClipboardController::copy);
	}

}
