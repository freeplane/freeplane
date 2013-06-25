package org.freeplane.view.swing.features.progress.mindmapmode;

import java.awt.event.ActionEvent;
import java.util.Collection;
import org.freeplane.core.ui.AMultipleNodeAction;
import org.freeplane.core.ui.EnabledAction;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;

/**
 * @author Stefan Ott
 * 
 * This class is called when the progress icons are increased
 */
@EnabledAction(checkOnNodeChange = true)
class ProgressUpAction extends AMultipleNodeAction {
	private static final long serialVersionUID = 1L;

	public ProgressUpAction() {
		super("IconProgressIconUpAction");
	}

	@Override
	protected void actionPerformed(final ActionEvent e, final NodeModel node) {
		final ProgressUtilities progUtil = new ProgressUtilities();
		if (!progUtil.hasExtendedProgressIcon(node)) {
			ProgressIcons.updateProgressIcons(node, true);
		}
	}

	@Override
	public void setEnabled() {
		boolean enable = false;
		final ProgressUtilities progUtil = new ProgressUtilities();
		final Collection<NodeModel> nodes = Controller.getCurrentModeController().getMapController().getSelectedNodes();
		for (final NodeModel node : nodes) {
			if (node != null && !progUtil.hasExtendedProgressIcon(node)) {
				enable = true;
				break;
			}
		}
		setEnabled(enable);
	}
}
