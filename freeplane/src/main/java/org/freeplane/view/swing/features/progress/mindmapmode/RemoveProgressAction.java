package org.freeplane.view.swing.features.progress.mindmapmode;

import java.awt.event.ActionEvent;
import java.util.Collection;
import org.freeplane.core.ui.AMultipleNodeAction;
import org.freeplane.core.ui.EnabledAction;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.view.swing.features.filepreview.ViewerController;

/**
 * @author Stefan Ott
 * 
 * This class is called when the progress icons are removed
 */
@EnabledAction(checkOnNodeChange = true)
class RemoveProgressAction extends AMultipleNodeAction {
	private static final long serialVersionUID = 1L;

	public RemoveProgressAction() {
		super("IconProgressRemoveAction");
	}

	/**
	 *Removes the progresss icons and the extended progress icons
	 */
	@Override
	protected void actionPerformed(final ActionEvent e, final NodeModel node) {
		final ProgressUtilities progUtil = new ProgressUtilities();
		//remove progress icon if present
		if (progUtil.hasProgressIcons(node)) {
			ProgressIcons.removeProgressIcons(node);
		}
		//remove extended progress icon
		if (progUtil.hasExtendedProgressIcon(node)) {
			final ViewerController vc = ((ViewerController) Controller.getCurrentController().getModeController()
			    .getExtension(ViewerController.class));
			vc.undoableDeactivateHook(node);
		}
	}

	@Override
	public void setEnabled() {
		final ProgressUtilities progUtil = new ProgressUtilities();
		boolean enable = false;
		final Collection<NodeModel> nodes = Controller.getCurrentModeController().getMapController().getSelectedNodes();
		for (final NodeModel node : nodes) {
			if (node != null && (progUtil.hasProgressIcons(node) || progUtil.hasExtendedProgressIcon(node))) {
				enable = true;
				break;
			}
		}
		setEnabled(enable);
	}
}
