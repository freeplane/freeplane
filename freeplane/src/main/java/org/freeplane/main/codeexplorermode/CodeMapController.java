package org.freeplane.main.codeexplorermode;

import org.freeplane.features.map.MapController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.codeexplorermode.CodeModeController;

import com.tngtech.archunit.core.domain.JavaPackage;

public class CodeMapController extends MapController {
	public CodeMapController(CodeModeController modeController) {
		super(modeController);
	}

	public CodeModeController getCodeModeController() {
		return (CodeModeController) Controller.getCurrentModeController();
	}

	public MapModel newMap(final JavaPackage rootPackage) {
		final CodeMapModel codeMapModel = new CodeMapModel(getModeController().getMapController().duplicator(), rootPackage);
		fireMapCreated(codeMapModel);
		createMapView(codeMapModel);
		return codeMapModel;
	}

	@Override
    protected void fireFoldingChanged(final NodeModel node) {/**/}
}
