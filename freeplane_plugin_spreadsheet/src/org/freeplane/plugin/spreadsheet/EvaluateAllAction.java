package org.freeplane.plugin.spreadsheet;

import java.awt.event.ActionEvent;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.ActionLocationDescriptor;
import org.freeplane.features.common.map.MapModel;
import org.freeplane.plugin.script.FormulaUtils;
import org.freeplane.view.swing.map.MapView;

@ActionLocationDescriptor(locations = { Activator.MENU_BAR_LOCATION })
public class EvaluateAllAction extends AFreeplaneAction {
	private static final long serialVersionUID = 1L;

	public EvaluateAllAction() {
		super(SpreadSheetUtils.getSpreadSheetKey("EvaluateAllAction"));
	}

	public void actionPerformed(final ActionEvent e) {
		final MapModel map = Controller.getCurrentController().getMap();
		FormulaUtils.clearCache(map);
		MapView mapView = (MapView)Controller.getCurrentController().getMapViewManager().getMapViewComponent();
		mapView.getRoot().updateAll();
	}
}
