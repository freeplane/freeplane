package org.freeplane.view.swing.features.progress.mindmapmode;

import java.awt.event.ActionEvent;
import java.io.File;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AMultipleNodeAction;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.view.swing.features.filepreview.ViewerController;

/**
 * @author Stefan Ott
 * 
 *  This class is called when the 25% step extended progress icon is added
 */
class ExtendedProgress25Action extends AMultipleNodeAction {
	private static final long serialVersionUID = 1L;

	public ExtendedProgress25Action() {
		super("IconProgressExtended25Action");
	}

	/**
	 * Adds a svg-file as an external object to the node.
	 * The handling of the file and the updating of the icons
	 * is done in the ViewerController.
	 */
	@Override
	protected void actionPerformed(final ActionEvent e, final NodeModel node) {
		final File file = new File(ResourceController.getResourceController().getResourceBaseDir()
		        + System.getProperty("file.separator") + "svg" + System.getProperty("file.separator")
		        + "Progress_quarter_00.svg");
		final ViewerController vc = ((ViewerController) Controller.getCurrentController().getModeController()
		    .getExtension(ViewerController.class));
		vc.paste(file, node, node.isLeft());
	}
}
