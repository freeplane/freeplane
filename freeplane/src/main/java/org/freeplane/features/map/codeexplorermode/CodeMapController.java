package org.freeplane.features.map.codeexplorermode;

import java.io.File;

import org.freeplane.features.map.MapController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.codeexplorermode.CodeModeController;

public class CodeMapController extends MapController {
	public CodeMapController(CodeModeController modeController) {
		super(modeController);
	}

	public CodeModeController getCodeModeController() {
		return (CodeModeController) Controller.getCurrentModeController();
	}

	public MapModel newMap(final File[] roots) {
		final CodeMapModel codeMapModel = new CodeMapModel(getModeController().getMapController().duplicator(), roots);
		fireMapCreated(codeMapModel);
		createMapView(codeMapModel);
		return codeMapModel;
	}

	@Override
	public NodeModel newNode(final Object userObject, final MapModel map) {
		return new CodeNodeModel((File) userObject, map);
	}

	public void newMap(File file) {
		newMap(new File[]{file});
	}
}
