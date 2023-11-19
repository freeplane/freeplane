package org.freeplane.main.codeexplorermode;

import java.awt.EventQueue;

import org.freeplane.features.map.MapController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.ui.IMapViewManager;
import org.freeplane.main.codeexplorermode.ShowDependingNodesAction.DependencyDirection;
import org.freeplane.view.swing.map.MapView;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaPackage;
import com.tngtech.archunit.core.importer.ClassFileImporter;

class CodeMapController extends MapController {
    CodeMapController(CodeModeController modeController) {
        super(modeController);
        modeController.addAction(new FilterSelectedCodeNodesAction());
        for(CodeNodeSelection selection: CodeNodeSelection.values()) {
            for(DependencyDirection direction: ShowDependingNodesAction.DependencyDirection.values()) {
                modeController.addAction(new ShowDependingNodesAction(direction, selection));
            }
            for(FilterCyclesAction.Action action: FilterCyclesAction.Action.values()) {
                modeController.addAction(new FilterCyclesAction(selection, action));
            }
        }
    }

	public CodeModeController getCodeModeController() {
		return (CodeModeController) Controller.getCurrentModeController();
	}

	@Override
    public MapModel newMap() {
	    final CodeMapModel codeMapModel = new CodeMapModel(getModeController().getMapController().duplicator());
	    fireMapCreated(codeMapModel);
	    createMapView(codeMapModel);
	    return codeMapModel;
	}

	@Override
	protected void fireFoldingChanged(final NodeModel node) {/**/}

	void explore(CodeExplorerConfiguration codeExplorerConfiguration) {
	    CodeMapModel map = (CodeMapModel) Controller.getCurrentController().getSelection().getMap();
	    EmptyNodeModel emptyRoot = new EmptyNodeModel(map, "Loading...");
	    map.setRoot(emptyRoot);

	    IMapViewManager mapViewManager = Controller.getCurrentController().getMapViewManager();
	    MapView mapView = (MapView) mapViewManager.getMapViewComponent();
	    mapView.mapRootNodeChanged();
	    mapViewManager.updateMapViewName();
	    new Thread(() -> {

	        PackageNodeModel newRoot;
	        if(codeExplorerConfiguration != null) {
	            newRoot = codeExplorerConfiguration.importPackages(map);
	        }
	        else {
	            ClassFileImporter classFileImporter = new ClassFileImporter();
	            JavaClasses importedClasses  = classFileImporter.importPackages("org.freeplane");
	            JavaPackage rootPackage = importedClasses.getPackage("org.freeplane");
	            newRoot = new PackageNodeModel(rootPackage, map, "demo");
	        }

	        EventQueue.invokeLater(() -> {
	            newRoot.setFolded(false);
	            map.setRoot(newRoot);
	            mapView.mapRootNodeChanged();
	            mapViewManager.updateMapViewName();
	        });
	    }, "Load explored packages").start();

	}

}
