package org.freeplane.plugin.configurationservice;

import java.awt.event.ActionEvent;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.view.swing.map.MapView;

class StartConfigurationSessionAction extends AFreeplaneAction {
	private static final String ACTION_NAME = "StartConfigurationSessionAction";
	private static final long serialVersionUID = 1L;
	private ConfigurationEngine engine;

	public StartConfigurationSessionAction(ConfigurationEngine engine) {
		super(ACTION_NAME);
		this.engine = engine;
	}

	public void actionPerformed(final ActionEvent e) {
		
		ConfigurationSession session = engine.newSession();
		
		final MapModel map = Controller.getCurrentController().getMap();
		org.freeplane.plugin.script.FormulaUtils.clearCache(map);
		MapView mapView = (MapView)Controller.getCurrentController().getMapViewManager().getMapViewComponent();
		mapView.getRoot().updateAll();
	}
}
