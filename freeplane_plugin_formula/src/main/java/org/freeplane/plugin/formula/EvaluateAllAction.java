package org.freeplane.plugin.formula;

import java.awt.event.ActionEvent;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.plugin.script.FormulaUtils;

class EvaluateAllAction extends AFreeplaneAction {
	private static final long serialVersionUID = 1L;

	public EvaluateAllAction() {
		super(FormulaPluginUtils.getFormulaKey("EvaluateAllAction"));
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		final MapModel map = Controller.getCurrentController().getMap();
		FormulaUtils.evaluateAllFormulas(map);
	}

}
