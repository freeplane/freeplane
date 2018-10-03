package org.freeplane.plugin.formula;

import java.awt.event.ActionEvent;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.view.swing.map.MapView;

class EvaluateAllAction extends AFreeplaneAction {
	private static final long serialVersionUID = 1L;

	public EvaluateAllAction() {
		super(FormulaPluginUtils.getFormulaKey("EvaluateAllAction"));
	}

	public void actionPerformed(final ActionEvent e) {
		final MapModel map = Controller.getCurrentController().getMap();
		org.freeplane.plugin.script.FormulaUtils.clearCache(map);
		MapView mapView = (MapView)Controller.getCurrentController().getMapViewManager().getMapViewComponent();
		mapView.getRoot().updateAll();
	}
}
