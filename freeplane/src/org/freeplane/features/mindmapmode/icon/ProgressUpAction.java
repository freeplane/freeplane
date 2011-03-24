package org.freeplane.features.mindmapmode.icon;

import java.awt.event.ActionEvent;

import org.freeplane.core.ui.AMultipleNodeAction;
import org.freeplane.features.common.map.NodeModel;

/**
 * @author Stefan Ott
 * 
 * This class is called when the progress icons are increased
 */
public class ProgressUpAction extends AMultipleNodeAction {
	private static final long serialVersionUID = 1L;

	public ProgressUpAction() {
		super("IconProgressIconUpAction");
	}

	@Override
	protected void actionPerformed(final ActionEvent e, final NodeModel node) {
		ProgressIconUpdater.update(node, true);
	}
}
