package org.freeplane.features.note.mindmapmode;

import java.awt.event.ActionEvent;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.SelectableAction;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.ui.ViewController;

/** Select Note Window at the position action */
@SelectableAction(checkOnPopup = true)
public class SetNoteWindowPosition extends AFreeplaneAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final String position;

	public SetNoteWindowPosition( final String position) {
		super("SetNoteWindowPosition." + position);
		this.position = position;
	};

	public void actionPerformed(final ActionEvent e) {
		final ResourceController resourceController = ResourceController.getResourceController();
		resourceController.setProperty("note_location", position);
		final ViewController viewController = Controller.getCurrentModeController().getController().getViewController();
		viewController.changeNoteWindowLocation();
	}

	@Override
	public void setSelected() {
		final ResourceController resourceController = ResourceController.getResourceController();
		final boolean isSelected = resourceController.getProperty("note_location").equals(position);
		setSelected(isSelected);
	}
}
