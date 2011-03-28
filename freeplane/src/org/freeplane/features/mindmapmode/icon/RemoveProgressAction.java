package org.freeplane.features.mindmapmode.icon;

import java.awt.event.ActionEvent;
import java.util.List;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.ui.AMultipleNodeAction;
import org.freeplane.core.ui.EnabledAction;
import org.freeplane.features.common.map.NodeModel;
import org.freeplane.view.swing.addins.filepreview.ViewerController;

/**
 * @author Stefan Ott
 * 
 * This class is called when the progress icons are removed
 */
@EnabledAction(checkOnNodeChange = true)
public class RemoveProgressAction extends AMultipleNodeAction {
	private static final long serialVersionUID = 1L;

	public RemoveProgressAction() {
		super("IconProgressRemoveAction");
	}

	/**
	 *Removes the progresss icons and the extended progress icons
	 */
	@Override
	protected void actionPerformed(final ActionEvent e, final NodeModel node) {
		//remove progress icon if present
		if (node.hasProgressIcons()) {
			ProgressIcons.removeProgressIcons(node);
		}
		//remove extended progress icon
		if (node.hasExtendedProgressIcon()) {
			final ViewerController vc = ((ViewerController) Controller.getCurrentController().getModeController()
			    .getExtension(ViewerController.class));
			vc.undoableDeactivateHook(node);
		}
	}

	@Override
	public void setEnabled() {
		boolean enable = false;
		final List<NodeModel> nodes = Controller.getCurrentModeController().getMapController().getSelectedNodes();
		for (final NodeModel node : nodes) {
			if (node != null && (node.hasProgressIcons() || node.hasExtendedProgressIcon())) {
				enable = true;
				break;
			}
		}
		setEnabled(enable);
	}
}
