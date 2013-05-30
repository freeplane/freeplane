package org.freeplane.features.nodestyle.mindmapmode;

import java.awt.event.ActionEvent;

import org.freeplane.core.ui.AMultipleNodeAction;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.nodestyle.NodeStyleController;

class NodeWidthAction extends AMultipleNodeAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int minNodeWidth;
	private int maxNodeWidth;
	private MNodeStyleController nsc;

	public NodeWidthAction() {
		super("NodeWidthAction");
	}

	public void actionPerformed(final ActionEvent e) {
		final NodeModel selected = Controller.getCurrentController().getSelection().getSelected();
		nsc = (MNodeStyleController) NodeStyleController.getController();
		minNodeWidth = nsc.getMinWidth(selected);
		maxNodeWidth = nsc.getMaxWidth(selected);
		final NodeSizeDialog nodeSizeDialog = new NodeSizeDialog();
		nodeSizeDialog.setTitle(TextUtils.getText("NodeWidthAction.text"));
		if(nodeSizeDialog.showDialog(minNodeWidth, maxNodeWidth)){
			minNodeWidth = nodeSizeDialog.getMinWidth();
			maxNodeWidth = nodeSizeDialog.getMaxNodeWidth();
			super.actionPerformed(e);
		}
		nsc = null;
	}

	@Override
    protected void actionPerformed(ActionEvent e, NodeModel node) {
		nsc.setMaxNodeWidth(node, maxNodeWidth);
		nsc.setMinNodeWidth(node, minNodeWidth);
    }
}
