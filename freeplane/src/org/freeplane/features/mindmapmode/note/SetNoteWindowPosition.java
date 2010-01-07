package org.freeplane.features.mindmapmode.note;

import java.awt.event.ActionEvent;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.frame.ViewController;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.SelectableAction;

/** Select Note Window at the position action */
@SelectableAction(checkOnPopup=true)
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
		resourceController.setProperty("note_location", position);
		final ViewController viewController = getModeController().getController().getViewController();
		
		String useSplitPaneProp = resourceController.getProperty(MNoteController.RESOURCES_USE_SPLIT_PANE);
		boolean useSplitPane = useSplitPaneProp == null ? false : Boolean.parseBoolean(useSplitPaneProp);
		
		viewController.changeNoteWindowLocation(useSplitPane);
	}
	@Override
	public void setSelected() {
		final ResourceController resourceController = ResourceController.getResourceController();
		final boolean isSelected = resourceController.getProperty("note_location").equals(position);
		setSelected(isSelected);
	}

}
