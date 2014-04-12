package org.freeplane.view.swing.features.progress.mindmapmode;

import java.awt.event.ActionEvent;
import java.net.URI;
import java.net.URISyntaxException;
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
		final ViewerController vc = ((ViewerController) Controller.getCurrentController().getModeController()
		    .getExtension(ViewerController.class));
		try {
	        URI uri = new URI(ResourceController.FREEPLANE_RESOURCE_URL_PROTOCOL, null, "/images/svg/Progress_tenth_00.svg", null);
			vc.paste(uri, node);
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
	}
}
