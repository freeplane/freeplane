package org.freeplane.features.mindmapmode.note;

import java.awt.event.ActionEvent;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.frame.ViewController;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;

/** Select Note Window at the position action */
class SetNoteWindowPosition extends AFreeplaneAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final String position;

	public SetNoteWindowPosition(final Controller controller, final String position) {
		super("SetNoteWindowPosition." + position, controller);
		this.position = position;
	};

	public void actionPerformed(final ActionEvent e) {
		final ResourceController resourceController = ResourceController.getResourceController();
		resourceController.setProperty("location", position);
		final ViewController viewController = getModeController().getController().getViewController();
		
		String useSplitPaneProp = resourceController.getProperty(MNoteController.RESOURCES_USE_SPLIT_PANE);
		boolean useSplitPane = useSplitPaneProp == null ? false : Boolean.parseBoolean(useSplitPaneProp);
		
		viewController.changeNoteWindowLocation(useSplitPane);
	}
}
