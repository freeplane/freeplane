package org.freeplane.plugin.codeexplorer.map;

import java.awt.Color;
import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.UIManager;

import org.freeplane.api.LengthUnit;
import org.freeplane.api.Quantity;
import org.freeplane.features.filter.FilterController;
import org.freeplane.features.map.IMapSelection;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.nodestyle.NodeSizeModel;
import org.freeplane.features.nodestyle.NodeStyleModel;
import org.freeplane.features.styles.MapStyleModel;
import org.freeplane.features.ui.IMapViewManager;
import org.freeplane.plugin.codeexplorer.dependencies.DependencyJudge;
import org.freeplane.plugin.codeexplorer.map.ShowDependingNodesAction.DependencyDirection;
import org.freeplane.plugin.codeexplorer.task.CodeExplorer;
import org.freeplane.plugin.codeexplorer.task.CodeExplorerConfiguration;
import org.freeplane.view.swing.map.MapView;
import org.freeplane.view.swing.map.NodeView;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaPackage;
import com.tngtech.archunit.core.importer.ClassFileImporter;

public class CodeMapController extends MapController implements CodeExplorer{
    public CodeMapController(ModeController modeController) {
        super(modeController);
        modeController.addAction(new ShowSelectedClassesWithExternalDependenciesAction());
        for(CodeNodeSelection selection: CodeNodeSelection.values()) {
            for(DependencyDirection direction: ShowDependingNodesAction.DependencyDirection.values()) {
                for(ShowDependingNodesAction.Depth maximumDepth : ShowDependingNodesAction.Depth.values()) {
                    modeController.addAction(new ShowDependingNodesAction(direction, selection, maximumDepth));
                }
            }
        }
        modeController.addAction(new CopyQualifiedName());
        modeController.addAction(new ShowAllClassesAction());
        modeController.addAction(new SelectCyclesAction());
        modeController.addAction(new FilterCyclesAction());
    }

	@Override
    public MapModel newMap() {
	    final CodeMap codeMap = new CodeMap(getModeController().getMapController().duplicator());
	    fireMapCreated(codeMap);
	    Color background = UIManager.getColor("Panel.background");
	    Color foreground = UIManager.getColor("Panel.foreground");
	    MapStyleModel mapStyleModel = MapStyleModel.getExtension(codeMap);
	    NodeModel defaultStyleNode = mapStyleModel.getDefaultStyleNode();
	    NodeStyleModel nodeStyle = NodeStyleModel.createNodeStyleModel(defaultStyleNode);
	    nodeStyle.setFontSize(10);
        if(background != null && foreground != null) {
	        mapStyleModel.setBackgroundColor(background.darker());
	        nodeStyle.setColor(foreground);
	    }
	    else {
	        mapStyleModel.setBackgroundColor(Color.WHITE);
	        nodeStyle.setColor(Color.BLACK);
	    }
	    NodeSizeModel.createNodeSizeModel(defaultStyleNode).setMaxNodeWidth(Quantity.fromString("30", LengthUnit.cm));
	    createMapView(codeMap);
	    return codeMap;
	}

	@Override
	protected void fireFoldingChanged(final NodeModel node) {/**/}

	@Override
    public void explore(CodeExplorerConfiguration codeExplorerConfiguration) {
	    Controller currentController = Controller.getCurrentController();
        IMapSelection selection = currentController.getSelection();
	    List<String> orderedSelectionIds = selection.getOrderedSelectionIds();
	    IMapViewManager mapViewManager = currentController.getMapViewManager();
	    MapView mapView = (MapView) mapViewManager.getMapViewComponent();
	    List<String> unfoldedNodeIDs = CodeNodeStream.nodeViews(mapView)
	            .filter(nv -> ! nv.isFolded())
	            .map(NodeView::getNode)
	            .map(NodeModel::getID)
	            .collect(Collectors.toList());
        CodeMap map = (CodeMap) selection.getMap();
	    EmptyNodeModel emptyRoot = new EmptyNodeModel(map, "Loading...");
	    map.setRoot(emptyRoot);
	    setJudge(codeExplorerConfiguration.getDependencyJudge());
	    mapView.mapRootNodeChanged();
	    mapViewManager.updateMapViewName();
	    new Thread(() -> {

	        NodeModel newRoot;
	        if(codeExplorerConfiguration != null) {
	            JavaPackage rootPackage = codeExplorerConfiguration.importPackages();
	            newRoot = new ProjectRootNode(map, rootPackage, codeExplorerConfiguration);
	        }
	        else {
	            ClassFileImporter classFileImporter = new ClassFileImporter();
	            JavaClasses importedClasses  = classFileImporter.importPackages("org.freeplane");
	            JavaPackage rootPackage = importedClasses.getPackage("org.freeplane");
	            newRoot = new ProjectRootNode(map, rootPackage, new CodeExplorerConfiguration("demo", new ArrayList<>(), ""));
	        }

	        EventQueue.invokeLater(() -> {
	            newRoot.setFolded(false);
	            map.setRoot(newRoot);
	            mapView.mapRootNodeChanged();
	            mapViewManager.updateMapViewName();
	            FilterController.getCurrentFilterController().mapRootNodeChanged(map);

	            unfoldedNodeIDs.stream()
                .map(id -> getExistingAncestorOrSelfNode(map, id))
                .filter(x -> x != null)
                .forEach(node -> node.setFolded(false));

	            NodeModel[] newSelection = orderedSelectionIds.stream()
	                    .map(id -> getExistingAncestorOrSelfNode(map, id))
	                    .filter(x -> x != null)
	                    .distinct()
	                    .toArray(NodeModel[]::new);
	            if(newSelection.length > 0)
	                selection.replaceSelection(newSelection);
	        });
	    }, "Load explored packages").start();

	}

    private CodeNode getExistingAncestorOrSelfNode(MapModel map, String id) {
        NodeModel node = map.getNodeForID(id);
        if(node != null)
            return (CodeNode) node;
        int lastDelimiterPosition = id.lastIndexOf('.');
        if(lastDelimiterPosition > 0)
            return getExistingAncestorOrSelfNode(map, id.substring(0, lastDelimiterPosition));
        return null;
    }

    @Override
    public void setJudge(DependencyJudge judge) {
        Controller currentController = Controller.getCurrentController();
        IMapSelection selection = currentController.getSelection();
        CodeMap map = (CodeMap) selection.getMap();
        map.setJudge(judge);
        IMapViewManager mapViewManager = currentController.getMapViewManager();
        MapView mapView = (MapView) mapViewManager.getMapViewComponent();
        mapView.repaint();

    }

}
