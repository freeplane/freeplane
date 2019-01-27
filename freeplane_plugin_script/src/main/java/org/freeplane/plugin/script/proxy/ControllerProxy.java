/**
 *
 */
package org.freeplane.plugin.script.proxy;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.swing.Icon;
import javax.swing.filechooser.FileFilter;

import org.freeplane.api.Map;
import org.freeplane.api.Node;
import org.freeplane.api.NodeCondition;
import org.freeplane.api.Script;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.IEditHandler.FirstAction;
import org.freeplane.core.undo.IUndoHandler;
import org.freeplane.core.util.FreeplaneVersion;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.export.mindmapmode.ExportController;
import org.freeplane.features.export.mindmapmode.IExportEngine;
import org.freeplane.features.filter.condition.ICondition;
import org.freeplane.features.icon.factory.MindIconFactory;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.mindmapmode.MMapModel;
import org.freeplane.features.mapio.mindmapmode.MMapIO;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.features.text.TextController;
import org.freeplane.features.text.mindmapmode.MTextController;
import org.freeplane.features.ui.IMapViewManager;
import org.freeplane.features.ui.ViewController;
import org.freeplane.plugin.script.ScriptContext;

import groovy.lang.Closure;

class ControllerProxy implements Proxy.Controller {
	private final ScriptContext scriptContext;

	public ControllerProxy(final ScriptContext scriptContext) {
		this.scriptContext = scriptContext;
	}

	@Override
	public void centerOnNode(final Node center) {
		final NodeModel nodeModel = ((NodeProxy) center).getDelegate();
		Controller.getCurrentController().getSelection().centerNode(nodeModel);
	}

	@Override
	public void edit(Node node) {
		editImpl(node, true);
	}

	@Override
	public void editInPopup(Node node) {
		editImpl(node, false);
	}

	private void editImpl(Node node, boolean editInline) {
	    final NodeModel nodeModel = ((NodeProxy) node).getDelegate();
		Controller.getCurrentController().getSelection().selectAsTheOnlyOneSelected(nodeModel);
		((MTextController) TextController.getController()).edit(FirstAction.EDIT_CURRENT, !editInline);
    }

	@Override
	public Node getSelected() {
		reportArbitraryNodeAccess();
		return new NodeProxy(Controller.getCurrentController().getSelection().getSelected(), scriptContext);
	}

	private void reportArbitraryNodeAccess() {
		if (scriptContext != null)
			scriptContext.accessAll();
	}

	@Override
	public List<? extends Node> getSelecteds() {
		reportArbitraryNodeAccess();
		return ProxyUtils.createNodeList(Controller.getCurrentController().getSelection().getOrderedSelection(), scriptContext);
	}

	@Override
	public List<? extends Node> getSortedSelection(final boolean differentSubtrees) {
		reportArbitraryNodeAccess();
		return ProxyUtils.createNodeList(Controller.getCurrentController().getSelection()
		    .getSortedSelection(differentSubtrees), scriptContext);
	}

    @Override
	public void select(final Node toSelect) {
        if (toSelect != null) {
            final NodeModel nodeModel = ((NodeProxy) toSelect).getDelegate();
            Controller.getCurrentModeController().getMapController().displayNode(nodeModel);
            Controller.getCurrentController().getSelection().selectAsTheOnlyOneSelected(nodeModel);
        }
    }

    @Override
	public void selectBranch(final Node branchRoot) {
        if (branchRoot != null) {
            final NodeModel nodeModel = ((NodeProxy) branchRoot).getDelegate();
            Controller.getCurrentModeController().getMapController().displayNode(nodeModel);
            Controller.getCurrentController().getSelection().selectBranch(nodeModel, false);
        }
    }

	@Override
	public void select(final Collection<? extends Node> toSelect) {
		final Iterator<? extends Node> it = toSelect.iterator();
		if (!it.hasNext()) {
			return;
		}
		final Node firstNode = it.next();
		select(firstNode);
		while (it.hasNext()) {
			final Node nextNode = it.next();
			final NodeModel nodeModel = ((NodeProxy) nextNode).getDelegate();
			Controller.getCurrentModeController().getMapController().displayNode(nodeModel);
			Controller.getCurrentController().getSelection().toggleSelected(nodeModel);
		}
	}

    @Override
	public void selectMultipleNodes(final Collection<? extends Node> toSelect) {
	    select(toSelect);
	}

	@Override
	public void deactivateUndo() {
		final MapModel map = Controller.getCurrentController().getMap();
		if (map instanceof MapModel) {
			MModeController modeController = ((MModeController) Controller.getCurrentModeController());
			modeController.deactivateUndo((MMapModel) map);
		}
	}

	@Override
	public void undo() {
		final MapModel map = Controller.getCurrentController().getMap();
		final IUndoHandler undoHandler = map.getExtension(IUndoHandler.class);
		undoHandler.undo();
	}

	@Override
	public void redo() {
		final MapModel map = Controller.getCurrentController().getMap();
		final IUndoHandler undoHandler = map.getExtension(IUndoHandler.class);
		undoHandler.redo();
	}

	@Override
	public void setStatusInfo(final String info) {
		final ViewController viewController = getViewController();
		viewController.out(info);
	}

	private ViewController getViewController() {
		return Controller.getCurrentController().getViewController();
	}

	private IMapViewManager getMapViewManager() {
		return Controller.getCurrentController().getMapViewManager();
	}

	@Override
	public void setStatusInfo(final String infoPanelKey, final String info) {
		final ViewController viewController = getViewController();
		viewController.addStatusInfo(infoPanelKey, info, null);
	}

	@Override
	public void setStatusInfo(final String infoPanelKey, final String info, final String iconKey) {
		final ViewController viewController = getViewController();
		viewController.addStatusInfo(infoPanelKey, info, MindIconFactory.createStandardIcon(iconKey));
	}

	@Override
	@Deprecated
	public void setStatusInfo(final String infoPanelKey, final Icon icon) {
		final ViewController viewController = getViewController();
		viewController.addStatusInfo(infoPanelKey, null, icon);
	}

	@Override
	public FreeplaneVersion getFreeplaneVersion() {
		return FreeplaneVersion.getVersion();
	}

	@Override
	public File getUserDirectory() {
	    return new File(ResourceController.getResourceController().getFreeplaneUserDirectory());
    }

	@Override
	@Deprecated
	public List<? extends Node> find(final ICondition condition) {
		reportArbitraryNodeAccess();
		return ProxyUtils.find(condition, currentMapRootNode(), scriptContext);
	}

	@Override
	public List<? extends Node> find(NodeCondition condition) {
		reportArbitraryNodeAccess();
		return ProxyUtils.find(condition, currentMapRootNode(), scriptContext);
	}

	@Override
	public List<? extends Node> find(boolean withAncestors, boolean withDescendants, NodeCondition condition) {
		reportArbitraryNodeAccess();
		return ProxyUtils.find(withAncestors, withDescendants, condition, currentMapRootNode(), scriptContext);
	}

	private NodeModel currentMapRootNode() {
		return Controller.getCurrentController().getMap().getRootNode();
	}
	@Override
	public List<? extends Node> find(final Closure<Boolean> closure) {
		reportArbitraryNodeAccess();
		return ProxyUtils.find(closure, currentMapRootNode(), scriptContext);
	}
	@Override
	public List<? extends Node> find(boolean withAncestors, boolean withDescendants, final Closure<Boolean> closure) {
		reportArbitraryNodeAccess();
		return ProxyUtils.find(withAncestors, withDescendants, closure, currentMapRootNode(), scriptContext);
	}

	// NodeRO: R
	@Override
	public List<? extends Node> findAll() {
		reportArbitraryNodeAccess();
		return ProxyUtils.findAll(currentMapRootNode(), scriptContext, true);
    }

	// NodeRO: R
	@Override
	public List<? extends Node> findAllDepthFirst() {
		reportArbitraryNodeAccess();
		return ProxyUtils.findAll(currentMapRootNode(), scriptContext, false);
    }

	@Override
	public Map newMap() {
		final MMapIO mapIO = MMapIO.getInstance();
		final MapModel newMap = mapIO.newMapFromDefaultTemplate();
		return new MapProxy(newMap, scriptContext);
	}


    @Override
	public float getZoom() {
	    return getMapViewManager().getZoom();
    }

    @Override
	public void setZoom(float ratio) {
    	getMapViewManager().setZoom(ratio);
    }

    @Override
	public boolean isInteractive() {
        return !Boolean.parseBoolean(System.getProperty("nonInteractive"));
    }

    @Override
	public List<String> getExportTypeDescriptions() {
        final ArrayList<String> list = new ArrayList<String>();
        for (FileFilter fileFilter : ExportController.getContoller().getMapExportFileFilters()) {
            list.add(fileFilter.getDescription());
        }
        return list;
    }

    @Override
	public void export(Map map, File destFile, String exportTypeDescription, boolean overwriteExisting) {
		List<FileFilter> fileFilters = ExportController.getContoller().getMapExportFileFilters();
		final FileFilter filter = findExportFileFilterByDescription(fileFilters, exportTypeDescription);
        if (filter == null) {
            throw new IllegalArgumentException("no export defined for '" + exportTypeDescription + "'");
        }
        else if (!overwriteExisting && destFile.exists()) {
            throw new RuntimeException("destination file " + destFile.getAbsolutePath()
                    + " already exists - set overwriteExisting to true?");
        }
		HashMap<FileFilter, IExportEngine> exportEngines = ExportController.getContoller().getMapExportEngines();
		final IExportEngine exportEngine = exportEngines.get(filter);
		MapModel mapDelegate = ((MapProxy) map).getDelegate();
		exportEngine.export(Collections.singletonList(mapDelegate.getRootNode()), destFile);
		LogUtils.info("exported " + map.getFile() + " to " + destFile.getAbsolutePath());
    }

    private FileFilter findExportFileFilterByDescription(List<FileFilter> fileFilters, String exportTypeDescription) {
		for (FileFilter fileFilter : fileFilters) {
            if (fileFilter.getDescription().equals(exportTypeDescription))
                return fileFilter;
        }
        return null;
    }

    @Override
	public List<Map> getOpenMaps() {
    	Collection<MapModel> mapModels = getMapViewManager().getMaps().values();
    	ArrayList<Map> mapProxies = new ArrayList<Map>(mapModels.size());
    	for (MapModel mapModel : mapModels) {
	        mapProxies.add(new MapProxy(mapModel, scriptContext));
        }
    	return mapProxies;
    }

	@Override
	public Proxy.Loader mapLoader(File file) {
		return LoaderProxy.of(file, scriptContext);
	}

	@Override
	public Proxy.Loader load(File file) {
		return mapLoader(file);
	}

	@Override
	public Proxy.Loader mapLoader(URL url) {
		return LoaderProxy.of(url, scriptContext);
	}

	@Override
	public Proxy.Loader load(URL url) {
		return mapLoader(url);
	}

	@Override
	public Proxy.Loader load(String file) {
		return LoaderProxy.of(file, scriptContext);
	}

	@Override
	public Proxy.Loader mapLoader(String file) {
		return mapLoader(file);
	}

	@Override
	public Map newMap(URL url) {
		return mapLoader(url).withView().load();
	}

	@Override
	public Map newMapFromTemplate(File templateFile) {
		return mapLoader(templateFile).withView().saveAfterLoading().load();
	}

	@Override
	public Script script(File file) {
		return new FileScriptProxy(file, scriptContext);
	}

	@Override
	public Script script(String script, String type) {
		return new StringScriptProxy(script, type, scriptContext);
	}

}
