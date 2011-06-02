package org.freeplane.features.mindmapmode.icon;

import java.awt.event.ActionEvent;
import java.io.File;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AMultipleNodeAction;
import org.freeplane.features.common.map.NodeModel;
import org.freeplane.view.swing.addins.filepreview.ViewerController;

/**
 * @author Stefan Ott
 * 
 * This class is called when the 10% step extended progress icon is added
 */
public class ExtendedProgress10Action extends AMultipleNodeAction {
	private static final long serialVersionUID = 1L;

	public ExtendedProgress10Action() {
		super("IconProgressExtended10Action");
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
		        + "Progress_tenth_00.svg");
		final ViewerController vc = ((ViewerController) Controller.getCurrentController().getModeController()
		    .getExtension(ViewerController.class));
		vc.paste(file, node, node.isLeft());
	}
}
