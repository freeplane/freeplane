package org.freeplane.features.icon.mindmapmode;

import java.awt.event.ActionEvent;
import java.util.List;

import org.freeplane.core.ui.AMultipleNodeAction;
import org.freeplane.core.ui.EnabledAction;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;

/**
 * @author Stefan Ott
 * 
 * This class is called when the progress icons are decreased
 */
@EnabledAction(checkOnNodeChange = true)
public class ProgressDownAction extends AMultipleNodeAction {
	private static final long serialVersionUID = 1L;

	public ProgressDownAction() {
		super("IconProgressIconDownAction");
	}

	@Override
	protected void actionPerformed(final ActionEvent e, final NodeModel node) {
		final ProgressUtilities progUtil = new ProgressUtilities();
		if (!progUtil.hasExtendedProgressIcon(node)) {
			ProgressIcons.updateProgressIcons(node, false);
		}
	}

	@Override
	public void setEnabled() {
		boolean enable = false;
		final ProgressUtilities progUtil = new ProgressUtilities();
		final List<NodeModel> nodes = Controller.getCurrentModeController().getMapController().getSelectedNodes();
		for (final NodeModel node : nodes) {
			if (node != null && !progUtil.hasExtendedProgressIcon(node)) {
				enable = true;
				break;
			}
		}
		setEnabled(enable);
	}
}
