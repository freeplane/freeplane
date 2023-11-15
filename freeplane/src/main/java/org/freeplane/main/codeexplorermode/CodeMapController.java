package org.freeplane.main.codeexplorermode;

import org.freeplane.features.map.MapController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;

import com.tngtech.archunit.core.domain.JavaPackage;

class CodeMapController extends MapController {
	CodeMapController(CodeModeController modeController) {
		super(modeController);
	}

	public CodeModeController getCodeModeController() {
		return (CodeModeController) Controller.getCurrentModeController();
	}

	MapModel newMap(final JavaPackage rootPackage) {
		final CodeMapModel codeMapModel = new CodeMapModel(getModeController().getMapController().duplicator(), rootPackage);
		fireMapCreated(codeMapModel);
		createMapView(codeMapModel);
		return codeMapModel;
	}

	@Override
    protected void fireFoldingChanged(final NodeModel node) {/**/}
}
