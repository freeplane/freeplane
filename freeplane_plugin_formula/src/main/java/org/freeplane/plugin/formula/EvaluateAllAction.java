package org.freeplane.plugin.formula;

import java.awt.event.ActionEvent;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.features.attribute.NodeAttributeTableModel;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.plugin.script.FormulaUtils;
import org.freeplane.view.swing.map.MapView;

class EvaluateAllAction extends AFreeplaneAction {
	private static final long serialVersionUID = 1L;

	public EvaluateAllAction() {
		super(FormulaPluginUtils.getFormulaKey("EvaluateAllAction"));
	}

	public void actionPerformed(final ActionEvent e) {
		final MapModel map = Controller.getCurrentController().getMap();
		org.freeplane.plugin.script.FormulaUtils.clearCache(map);
		evaluateAll(map.getRootNode());
	}

	private void evaluateAll(NodeModel node) {
		evaluateObject(node, node.getUserObject());
		node.getExtension(NodeAttributeTableModel.class).getAttributes().stream().forEach(a -> evaluateObject(node, a));
		node.getChildren().stream().forEach(this::evaluateAll);
	}

	private void evaluateObject(NodeModel node, Object userObject) {
		try {
			if (FormulaUtils.containsFormula(userObject)){
				FormulaUtils.evalIfScript(node, (String) userObject);
			}
		} catch (Exception e) {
		}
	}
}
