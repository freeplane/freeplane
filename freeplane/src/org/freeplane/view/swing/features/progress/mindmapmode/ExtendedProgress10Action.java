package org.freeplane.view.swing.features.progress.mindmapmode;

import java.awt.event.ActionEvent;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AMultipleNodeAction;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.view.swing.features.filepreview.ViewerController;

/**
 * @author Stefan Ott
 * 
 * This class is called when the 10% step extended progress icon is added
 */
class ExtendedProgress10Action extends AMultipleNodeAction {
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
		//final File file = new File(ResourceController.getResourceController().getResourceBaseDir()
		//       + System.getProperty("file.separator") + "svg" + System.getProperty("file.separator")
		//      + "Progress_tenth_00.svg");
		URL url= ResourceController.getResourceController().getResource("/images/svg/Progress_tenth_00.svg");
		final ViewerController vc = ((ViewerController) Controller.getCurrentController().getModeController()
		    .getExtension(ViewerController.class));
		try {
			vc.paste(url.toURI(), node);
		} catch (URISyntaxException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}
