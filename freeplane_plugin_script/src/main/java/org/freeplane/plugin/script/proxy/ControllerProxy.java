/**
 *
 */
package org.freeplane.plugin.script.proxy;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.Icon;
import javax.swing.filechooser.FileFilter;

import org.freeplane.api.Loader;
import org.freeplane.api.Map;
import org.freeplane.api.Node;
import org.freeplane.api.NodeCondition;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.IEditHandler.FirstAction;
import org.freeplane.core.undo.IUndoHandler;
import org.freeplane.core.util.Compat;
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
		if (scriptContext != null)
			scriptContext.accessAll();
		return new NodeProxy(Controller.getCurrentController().getSelection().getSelected(), scriptContext);
	}

	@Override
	public List<? extends Node> getSelecteds() {
		if (scriptContext != null)
			scriptContext.accessAll();
		return ProxyUtils.createNodeList(Controller.getCurrentController().getSelection().getOrderedSelection(), scriptContext);
	}

	@Override
	public List<? extends Node> getSortedSelection(final boolean differentSubtrees) {
		if (scriptContext != null)
			scriptContext.accessAll();
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
		if (scriptContext != null)
			scriptContext.accessAll();
		return ProxyUtils.find(condition, currentMapRootNode(), scriptContext);
	}

	@Override
	public List<? extends Node> find(NodeCondition condition) {
		if (scriptContext != null)
			scriptContext.accessAll();
		return ProxyUtils.find(condition, currentMapRootNode(), scriptContext);
	}

	private NodeModel currentMapRootNode() {
		return Controller.getCurrentController().getMap().getRootNode();
	}
	@Override
	public List<? extends Node> find(final Closure<Boolean> closure) {
		if (scriptContext != null)
			scriptContext.accessAll();
		return ProxyUtils.find(closure, currentMapRootNode(), scriptContext);
	}

	// NodeRO: R
	@Override
	public List<? extends Node> findAll() {
		if (scriptContext != null)
			scriptContext.accessAll();
		return ProxyUtils.findAll(currentMapRootNode(), scriptContext, true);
    }

	// NodeRO: R
	@Override
	public List<? extends Node> findAllDepthFirst() {
		if (scriptContext != null)
			scriptContext.accessAll();
		return ProxyUtils.findAll(currentMapRootNode(), scriptContext, false);
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
        for (FileFilter fileFilter : ExportController.getContoller().getFileFilters()) {
            list.add(fileFilter.getDescription());
        }
        return list;
    }

    @Override
	public void export(Map map, File destFile, String exportTypeDescription, boolean overwriteExisting) {
        final FileFilter filter = findExportFileFilterByDescription(exportTypeDescription);
        if (filter == null) {
            throw new IllegalArgumentException("no export defined for '" + exportTypeDescription + "'");
        }
        else if (!overwriteExisting && destFile.exists()) {
            throw new RuntimeException("destination file " + destFile.getAbsolutePath()
                    + " already exists - set overwriteExisting to true?");
        }
        else {
            final IExportEngine exportEngine = ExportController.getContoller().getFilterMap().get(filter);
            exportEngine.export(((MapProxy) map).getDelegate(), destFile);
            LogUtils.info("exported " + map.getFile() + " to " + destFile.getAbsolutePath());
        }
    }

    private FileFilter findExportFileFilterByDescription(String exportTypeDescription) {
        for (FileFilter fileFilter : ExportController.getContoller().getFileFilters()) {
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
	public Loader load(File file) {
		return LoaderProxy.of(file, scriptContext);
	}

	@Override
	public Loader load(URL url) {
		return LoaderProxy.of(url, scriptContext);
	}

	@Override
	public Loader load(String file) {
		return LoaderProxy.of(file, scriptContext);
	}

	@Override
	public Map newMap() {
		return load().getMap();
	}

	private Loader load() {
		return LoaderProxy.of(scriptContext);
	}

	@Override
	public Map newMap(URL url) {
		return load(url).getMap();
	}

	@Override
	public Map newMapFromTemplate(File templateFile) {
		return load(templateFile).unsaved().getMap();
	}

}
