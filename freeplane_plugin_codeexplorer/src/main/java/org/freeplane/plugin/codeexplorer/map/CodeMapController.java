package org.freeplane.plugin.codeexplorer.map;

import java.awt.Color;
import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import javax.swing.UIManager;

import org.freeplane.api.HorizontalTextAlignment;
import org.freeplane.api.LengthUnit;
import org.freeplane.api.Quantity;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.undo.IActor;
import org.freeplane.core.undo.IUndoHandler;
import org.freeplane.core.undo.UndoHandler;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.filter.FilterController;
import org.freeplane.features.map.IMapSelection;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeDeletionEvent;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.nodestyle.NodeSizeModel;
import org.freeplane.features.nodestyle.NodeStyleModel;
import org.freeplane.features.styles.MapStyleModel;
import org.freeplane.features.ui.IMapViewManager;
import org.freeplane.plugin.codeexplorer.map.ShowDependingNodesAction.DependencyDirection;
import org.freeplane.plugin.codeexplorer.task.AnnotationMatcher;
import org.freeplane.plugin.codeexplorer.task.CodeExplorer;
import org.freeplane.plugin.codeexplorer.task.CodeExplorerConfiguration;
import org.freeplane.plugin.codeexplorer.task.DependencyJudge;
import org.freeplane.view.swing.map.MapView;
import org.freeplane.view.swing.map.NodeView;

import com.tngtech.archunit.core.domain.JavaClasses;

public class CodeMapController extends MapController implements CodeExplorer{
    private final ExecutorService classImportService;
    private static class LoadedMap implements IExtension {
        static boolean containsProjectMap(MapModel map, CodeMap projectMap) {
            LoadedMap loadedMap = map.getExtension(LoadedMap.class);
            return loadedMap != null && loadedMap.projectMap == projectMap;
        }
        CodeMap projectMap;
        LoadedMap(CodeMap projectMap) {
            this.projectMap = projectMap;
        }
    }

    public CodeMapController(ModeController modeController, ExecutorService classImportService) {
        super(modeController);
        this.classImportService = classImportService;

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
        modeController.addAction(new NewCodeMapAction());
        modeController.addAction(new PurgeAction());
    }

    public CodeMap newCodeMap(boolean canBeSaved) {
	    final CodeMap codeMap = new CodeMap(getModeController().getMapController().duplicator());
	    codeMap.setCanBeSaved(canBeSaved);
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
        CodeMap oldMap = (CodeMap) selection.getMap();
        if (!currentController.getMapViewManager().saveModifiedIfNotCancelled(oldMap))
            return;
	    List<String> orderedSelectionIds = selection.getOrderedSelectionIds();
	    IMapViewManager mapViewManager = currentController.getMapViewManager();
	    MapView mapView = (MapView) mapViewManager.getMapViewComponent();
	    List<String> unfoldedNodeIDs = CodeNodeStream.nodeViews(mapView)
	            .filter(nv -> ! nv.isFolded())
	            .map(NodeView::getNode)
	            .map(NodeModel::getID)
	            .collect(Collectors.toList());
        CodeMap loadingHintMap = newCodeMap(false);
	    EmptyNodeModel emptyRoot = new EmptyNodeModel(loadingHintMap, "Analyzing"
	            + (codeExplorerConfiguration!= null ? " " + codeExplorerConfiguration.countLocations():"")
	            + " locations ...");
	    loadingHintMap.setRoot(emptyRoot);
	    mapViewManager.setMap(mapView, loadingHintMap);
	    Controller.getCurrentController().getMapViewManager().setMapTitles();
	    Controller.getCurrentController().getViewController().setWaitingCursor(true);
        CodeMap projectMap = newCodeMap(codeExplorerConfiguration.canBeSaved());
        projectMap.setConfiguration(codeExplorerConfiguration);
        loadingHintMap.addExtension(new LoadedMap(projectMap));
        classImportService.execute(() -> {

            CodeMap nextMap = oldMap;
            ProjectRootNode projectRoot = null;
            try {
                JavaClasses importedClasses = codeExplorerConfiguration.importClasses();
                if(LoadedMap.containsProjectMap(loadingHintMap, projectMap)) {
                    projectRoot = ProjectRootNode.asMapRoot(codeExplorerConfiguration.getProjectName(),
                            projectMap, importedClasses, codeExplorerConfiguration.createLocationMatcher());

                    projectMap.setJudge(codeExplorerConfiguration.getDependencyJudge());
                    CodeMapPersistenceManager.getCodeMapPersistenceManager(getModeController()).restoreUserContent(projectMap);
                }
                LogUtils.info("Code map prepared");
                if(projectRoot != null && LoadedMap.containsProjectMap(loadingHintMap, projectMap)) {
                    projectRoot.setFolded(false);
                    nextMap = projectMap;
                }
            } catch (Exception e) {
                LogUtils.warn("Loading classes failed", e);
                UITools.errorMessage("Loading classes failed, " + e.getMessage());
            }
            CodeMap viewedMap = nextMap;
            EventQueue.invokeLater(() -> {
                loadingHintMap.removeExtension(LoadedMap.class);
                if(! viewedMap.containsExtension(IUndoHandler.class))
                    viewedMap.addExtension(IUndoHandler.class, new UndoHandler(viewedMap));
                mapViewManager.setMap(mapView, viewedMap);
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
                if(codeExplorerConfiguration != null)
                    projectMap.updateAnnotations(codeExplorerConfiguration.getAnnotationMatcher());
                FilterController.getCurrentFilterController().mapRootNodeChanged(viewedMap);
                Controller.getCurrentController().getMapViewManager().setMapTitles();
                EventQueue.invokeLater(() -> Controller.getCurrentController().getViewController().setWaitingCursor(false));
            });
	    });

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
    public void setProjectConfiguration(DependencyJudge judge, AnnotationMatcher annotationMatcher) {
        Controller currentController = Controller.getCurrentController();
        IMapSelection selection = currentController.getSelection();
        CodeMap map = (CodeMap) selection.getMap();
        map.setJudge(judge);
        map.updateAnnotations(annotationMatcher);
        IMapViewManager mapViewManager = currentController.getMapViewManager();
        MapView mapView = (MapView) mapViewManager.getMapViewComponent();
        mapView.repaint();

    }

    @Override
    public void cancelAnalysis() {
        Controller.getCurrentController().getMap().removeExtension(LoadedMap.class);
    }

    @Override
    public void mapSaved(MapModel mapModel, boolean saved) {
        mapModel.setSaved(saved);
    }

    void purge(final NodeModel node) {
        if(node instanceof DeletedContentNode)
            deleteNode(node);
        else
            new ArrayList<>(node.getChildren()).forEach(this::purge);
    }

    private void deleteNode(final NodeModel node) {
        final NodeModel parentNode = node.getParentNode();
        if(parentNode == null)
            return;
        final int index = parentNode.getIndex(node);
        final IActor actor = new IActor() {
            @Override
            public void act() {
                deleteWithoutUndo(parentNode, index);
            }

            @Override
            public String getDescription() {
                return "delete";
            }

            @Override
            public void undo() {
                insertNodeIntoWithoutUndo(node, parentNode, index);
            }
        };
        Controller.getCurrentModeController().execute(actor, parentNode.getMap());
    }

    private void deleteWithoutUndo(final NodeModel parent, final int index) {
        final NodeModel child = parent.getChildAt(index);
        final NodeDeletionEvent nodeDeletionEvent = new NodeDeletionEvent(parent, child, index);
        firePreNodeDelete(nodeDeletionEvent);
        final MapModel map = parent.getMap();
        mapSaved(map, false);
        parent.remove(index);
        fireNodeDeleted(nodeDeletionEvent);
    }


}
