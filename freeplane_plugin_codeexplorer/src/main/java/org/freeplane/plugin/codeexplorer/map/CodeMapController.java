package org.freeplane.plugin.codeexplorer.map;

import java.awt.Color;
import java.awt.EventQueue;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import javax.swing.UIManager;

import org.freeplane.api.HorizontalTextAlignment;
import org.freeplane.api.LengthUnit;
import org.freeplane.api.Quantity;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.attribute.AttributeRegistry;
import org.freeplane.features.attribute.AttributeTableLayoutModel;
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
import org.freeplane.plugin.codeexplorer.map.ShowDependingNodesAction.DependencyDirection;
import org.freeplane.plugin.codeexplorer.task.CodeExplorer;
import org.freeplane.plugin.codeexplorer.task.CodeExplorerConfiguration;
import org.freeplane.plugin.codeexplorer.task.DependencyJudge;
import org.freeplane.plugin.codeexplorer.task.DirectoryMatcher;
import org.freeplane.view.swing.map.MapView;
import org.freeplane.view.swing.map.NodeView;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;

public class CodeMapController extends MapController implements CodeExplorer{
    private final AtomicReference<CodeMap> createdMap;

    public CodeMapController(ModeController modeController) {
        super(modeController);
        createdMap = new AtomicReference<>();
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
    public CodeMap newMap() {
	    final CodeMap codeMap = new CodeMap(getModeController().getMapController().duplicator());
	    fireMapCreated(codeMap);
	    Color background = UIManager.getColor("Panel.background");
	    Color foreground = UIManager.getColor("Panel.foreground");
	    MapStyleModel mapStyleModel = MapStyleModel.getExtension(codeMap);
	    NodeModel defaultStyleNode = mapStyleModel.getDefaultStyleNode();
	    NodeStyleModel nodeStyle = NodeStyleModel.createNodeStyleModel(defaultStyleNode);
	    nodeStyle.setFontSize(10);
	    nodeStyle.setHorizontalTextAlignment(HorizontalTextAlignment.LEFT);
        if(background != null && foreground != null) {
	        mapStyleModel.setBackgroundColor(background.darker());
	        nodeStyle.setColor(foreground);
	    }
	    else {
	        mapStyleModel.setBackgroundColor(Color.WHITE);
	        nodeStyle.setColor(Color.BLACK);
	    }
	    NodeSizeModel nodeSizeModel = NodeSizeModel.createNodeSizeModel(defaultStyleNode);
        nodeSizeModel.setMinNodeWidth(Quantity.fromString("6", LengthUnit.cm));
        nodeSizeModel.setMaxNodeWidth(Quantity.fromString("30", LengthUnit.cm));
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
        CodeMap oldMap = (CodeMap) selection.getMap();
        CodeMap loadingHintMap = newMap();
	    EmptyNodeModel emptyRoot = new EmptyNodeModel(loadingHintMap, "Analyzing"
	            + (codeExplorerConfiguration!= null ? " " + codeExplorerConfiguration.countLocations():"")
	            + " locations ...");
	    loadingHintMap.setRoot(emptyRoot);
	    mapView.setMap(loadingHintMap);
	    selection.selectAsTheOnlyOneSelected(emptyRoot);
	    Controller.getCurrentController().getViewController().setWaitingCursor(true);
        CodeMap projectMap = newMap();
        this.createdMap.set(projectMap);
        projectMap.getExtension(AttributeRegistry.class).setAttributeViewType(AttributeTableLayoutModel.HIDE_ALL);
	    new Thread(() -> {

	        CodeMap nextMap = oldMap;
	        ProjectRootNode projectRoot = null;
	        try {
	            if(codeExplorerConfiguration != null) {
	                JavaClasses importedClasses = codeExplorerConfiguration.importClasses();
	                if(projectMap.equals(createdMap.get())) {
	                    projectRoot = ProjectRootNode.asMapRoot(codeExplorerConfiguration.getProjectName(),
	                            projectMap, importedClasses, codeExplorerConfiguration.createDirectoryMatcher());

	                    projectMap.setJudge(codeExplorerConfiguration.getDependencyJudge());
	                }
	            }
	            else {
	                ClassFileImporter classFileImporter = new ClassFileImporter();
                    JavaClasses importedClasses  = classFileImporter.importPackages("org.freeplane");
                    if(projectMap.equals(createdMap.get())) {
                        projectRoot = ProjectRootNode.asMapRoot("demo", projectMap, importedClasses, DirectoryMatcher.ALLOW_ALL);
                    }
                }
                LogUtils.info("Code map prepared");
                if(projectRoot != null && projectMap.equals(createdMap.get())) {
                    projectRoot.setFolded(false);
                    nextMap = projectMap;
                }
            } catch (Exception e) {
                LogUtils.warn("Loading classes failed", e);
                UITools.errorMessage("Loading classes failed, " + e.getMessage());
            }
            CodeMap viewedMap = nextMap;
            EventQueue.invokeLater(() -> {
                createdMap.compareAndSet(projectMap, null);
                mapView.setMap(viewedMap);
                selection.selectAsTheOnlyOneSelected(viewedMap.getRootNode());
                unfoldedNodeIDs.stream()
                .map(id -> getExistingAncestorOrSelfNode(viewedMap, id))
                .filter(x -> x != null)
                .forEach(node -> node.setFolded(false));

                NodeModel[] newSelection = orderedSelectionIds.stream()
                        .map(id -> getExistingAncestorOrSelfNode(viewedMap, id))
                        .filter(x -> x != null)
                        .distinct()
                        .toArray(NodeModel[]::new);
                if(newSelection.length > 0)
                    selection.replaceSelection(newSelection);
                FilterController.getCurrentFilterController().mapRootNodeChanged(viewedMap);
                EventQueue.invokeLater(() -> Controller.getCurrentController().getViewController().setWaitingCursor(false));
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

    @Override
    public void cancelAnalysis() {
        createdMap.set(null);
    }
}
