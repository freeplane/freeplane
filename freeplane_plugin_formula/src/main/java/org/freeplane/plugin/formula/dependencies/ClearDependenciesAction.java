package org.freeplane.plugin.formula.dependencies;

import java.awt.event.ActionEvent;

import org.freeplane.core.extension.Configurable;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.features.mode.Controller;
import org.freeplane.plugin.formula.FormulaPluginUtils;

class ClearDependenciesAction extends AFreeplaneAction {
	private static final long serialVersionUID = 1L;

	public ClearDependenciesAction() {
		super(FormulaPluginUtils.getFormulaKey("ClearDependenciesAction"));
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		Configurable mapViewConfiguration = Controller.getCurrentController().getMapViewManager().getMapViewConfiguration();
		FormulaDependencyTracer.clear(mapViewConfiguration);
	}
}
