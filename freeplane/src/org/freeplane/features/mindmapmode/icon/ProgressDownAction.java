package org.freeplane.features.mindmapmode.icon;

import java.awt.event.ActionEvent;

import org.freeplane.core.ui.AMultipleNodeAction;
import org.freeplane.features.common.map.NodeModel;

/**
 * @author Stefan Ott
 * 
 * This class is called when the progress icons are decreased
 */
public class ProgressDownAction extends AMultipleNodeAction {
	private static final long serialVersionUID = 1L;

	public ProgressDownAction() {
		super("IconProgressIconDownAction");
	}

	@Override
	protected void actionPerformed(final ActionEvent e, final NodeModel node) {
		ProgressIconUpdater.update(node, false);
	}
}
