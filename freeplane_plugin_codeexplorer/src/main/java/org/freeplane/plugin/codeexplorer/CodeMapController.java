package org.freeplane.plugin.codeexplorer;

import java.awt.Color;
import java.awt.EventQueue;

import javax.swing.UIManager;

import org.freeplane.features.filter.FilterController;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.nodestyle.NodeStyleModel;
import org.freeplane.features.styles.MapStyleModel;
import org.freeplane.features.ui.IMapViewManager;
import org.freeplane.plugin.codeexplorer.ShowDependingNodesAction.DependencyDirection;
import org.freeplane.plugin.codeexplorer.configurator.CodeExplorer;
import org.freeplane.plugin.codeexplorer.configurator.CodeExplorerConfiguration;
import org.freeplane.view.swing.map.MapView;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaPackage;
import com.tngtech.archunit.core.importer.ClassFileImporter;

class CodeMapController extends MapController implements CodeExplorer{
    CodeMapController(CodeModeController modeController) {
        super(modeController);
        modeController.addAction(new ShowSelectedClassesWithExternalDependenciesAction());
        for(CodeNodeSelection selection: CodeNodeSelection.values()) {
            for(DependencyDirection direction: ShowDependingNodesAction.DependencyDirection.values()) {
                for(ShowDependingNodesAction.Depth maximumDepth : ShowDependingNodesAction.Depth.values()) {
                    modeController.addAction(new ShowDependingNodesAction(direction, selection, maximumDepth));
                }
            }
        }
        modeController.addAction(new ShowAllClassesAction());
        modeController.addAction(new SelectCyclesAction());
        modeController.addAction(new FilterCyclesAction());
    }

	public CodeModeController getCodeModeController() {
		return (CodeModeController) Controller.getCurrentModeController();
	}

	@Override
    public MapModel newMap() {
	    final CodeMapModel codeMapModel = new CodeMapModel(getModeController().getMapController().duplicator());
	    fireMapCreated(codeMapModel);
	    Color background = UIManager.getColor("Panel.background");
	    Color foreground = UIManager.getColor("Panel.foreground");
	    MapStyleModel mapStyleModel = MapStyleModel.getExtension(codeMapModel);
	    NodeModel defaultStyleNode = mapStyleModel.getDefaultStyleNode();
	    if(background != null && foreground != null) {
	        mapStyleModel.setBackgroundColor(background.darker());
	        NodeStyleModel.createNodeStyleModel(defaultStyleNode).setColor(foreground);
	    }
	    else {
	        mapStyleModel.setBackgroundColor(Color.WHITE);
	        NodeStyleModel.createNodeStyleModel(defaultStyleNode).setColor(Color.BLACK);
	    }
	    createMapView(codeMapModel);
	    return codeMapModel;
	}

	@Override
	protected void fireFoldingChanged(final NodeModel node) {/**/}

	@Override
    public void explore(CodeExplorerConfiguration codeExplorerConfiguration) {
	    CodeMapModel map = (CodeMapModel) Controller.getCurrentController().getSelection().getMap();
	    EmptyNodeModel emptyRoot = new EmptyNodeModel(map, "Loading...");
	    map.setRoot(emptyRoot);

	    IMapViewManager mapViewManager = Controller.getCurrentController().getMapViewManager();
	    MapView mapView = (MapView) mapViewManager.getMapViewComponent();
	    mapView.mapRootNodeChanged();
	    mapViewManager.updateMapViewName();
	    new Thread(() -> {

	        NodeModel newRoot;
	        if(codeExplorerConfiguration != null) {
	            JavaPackage rootPackage = codeExplorerConfiguration.importPackages();
	            newRoot = new PackageNodeModel(rootPackage, map, codeExplorerConfiguration.getProjectName(), 0);
	        }
	        else {
	            ClassFileImporter classFileImporter = new ClassFileImporter();
	            JavaClasses importedClasses  = classFileImporter.importPackages("org.freeplane");
	            JavaPackage rootPackage = importedClasses.getPackage("org.freeplane");
	            newRoot = new PackageNodeModel(rootPackage, map, "demo", 0);
	        }

	        EventQueue.invokeLater(() -> {
	            newRoot.setFolded(false);
	            map.setRoot(newRoot);
	            mapView.mapRootNodeChanged();
	            mapViewManager.updateMapViewName();
	            FilterController.getCurrentFilterController().mapRootNodeChanged(map);
	        });
	    }, "Load explored packages").start();

	}

}
