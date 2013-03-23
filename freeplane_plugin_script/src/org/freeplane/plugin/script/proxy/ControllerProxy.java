/**
 * 
 */
package org.freeplane.plugin.script.proxy;

import groovy.lang.Closure;

import java.io.File;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import javax.swing.Icon;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.IEditHandler.FirstAction;
import org.freeplane.core.undo.IUndoHandler;
import org.freeplane.core.util.FreeplaneIconUtils;
import org.freeplane.core.util.FreeplaneVersion;
import org.freeplane.features.filter.condition.ICondition;
import org.freeplane.features.map.IMapSelection;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.mindmapmode.MMapModel;
import org.freeplane.features.mapio.MapIO;
import org.freeplane.features.mapio.mindmapmode.MMapIO;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.features.text.TextController;
import org.freeplane.features.text.mindmapmode.MTextController;
import org.freeplane.features.ui.IMapViewManager;
import org.freeplane.features.ui.ViewController;
import org.freeplane.plugin.script.ScriptContext;
import org.freeplane.plugin.script.proxy.Proxy.Map;
import org.freeplane.plugin.script.proxy.Proxy.Node;

class ControllerProxy implements Proxy.Controller {
	private final ScriptContext scriptContext;

	public ControllerProxy(final ScriptContext scriptContext) {
		this.scriptContext = scriptContext;
	}

	public void centerOnNode(final Node center) {
		final NodeModel nodeModel = ((NodeProxy) center).getDelegate();
		Controller.getCurrentController().getSelection().centerNode(nodeModel);
	}

	public void edit(Node node) {
		editImpl(node, true);
	}

	public void editInPopup(Node node) {
		editImpl(node, false);
	}

	private void editImpl(Node node, boolean editInline) {
	    final NodeModel nodeModel = ((NodeProxy) node).getDelegate();
		Controller.getCurrentController().getSelection().selectAsTheOnlyOneSelected(nodeModel);
		((MTextController) TextController.getController()).edit(FirstAction.EDIT_CURRENT, !editInline);
    }

	public Node getSelected() {
		if (scriptContext != null)
			scriptContext.accessAll();
		return new NodeProxy(Controller.getCurrentController().getSelection().getSelected(), scriptContext);
	}

	public List<Node> getSelecteds() {
		if (scriptContext != null)
			scriptContext.accessAll();
		return ProxyUtils.createNodeList(Controller.getCurrentController().getSelection().getOrderedSelection(), scriptContext);
	}

	public List<Node> getSortedSelection(final boolean differentSubtrees) {
		if (scriptContext != null)
			scriptContext.accessAll();
		return ProxyUtils.createNodeList(Controller.getCurrentController().getSelection()
		    .getSortedSelection(differentSubtrees), scriptContext);
	}

    public void select(final Node toSelect) {
        if (toSelect != null) {
            final NodeModel nodeModel = ((NodeProxy) toSelect).getDelegate();
            Controller.getCurrentController().getSelection().selectAsTheOnlyOneSelected(nodeModel);
        }
    }

    public void selectBranch(final Node branchRoot) {
        if (branchRoot != null) {
            final NodeModel nodeModel = ((NodeProxy) branchRoot).getDelegate();
            Controller.getCurrentModeController().getMapController().displayNode(nodeModel);
            Controller.getCurrentController().getSelection().selectBranch(nodeModel, false);
        }
    }

	public void selectMultipleNodes(final List<Node> toSelect) {
		final IMapSelection selection = Controller.getCurrentController().getSelection();
		final Iterator<Node> it = toSelect.iterator();
		if (!it.hasNext()) {
			return;
		}
		selection.selectAsTheOnlyOneSelected(((NodeProxy) it.next()).getDelegate());
		while (it.hasNext()) {
			final NodeModel nodeModel = ((NodeProxy) it.next()).getDelegate();
			Controller.getCurrentController().getSelection().toggleSelected(nodeModel);
		}
	}

	public void deactivateUndo() {
		final MapModel map = Controller.getCurrentController().getMap();
		if (map instanceof MapModel) {
			MModeController modeController = ((MModeController) Controller.getCurrentModeController());
			modeController.deactivateUndo((MMapModel) map);
		}
	}

	public void undo() {
		final MapModel map = Controller.getCurrentController().getMap();
		final IUndoHandler undoHandler = (IUndoHandler) map.getExtension(IUndoHandler.class);
		undoHandler.undo();
	}

	public void redo() {
		final MapModel map = Controller.getCurrentController().getMap();
		final IUndoHandler undoHandler = (IUndoHandler) map.getExtension(IUndoHandler.class);
		undoHandler.redo();
	}

	public void setStatusInfo(final String info) {
		final ViewController viewController = getViewController();
		viewController.out(info);
	}

	private ViewController getViewController() {
		return Controller.getCurrentController().getViewController();
	}

	public void setStatusInfo(final String infoPanelKey, final String info) {
		final ViewController viewController = getViewController();
		viewController.addStatusInfo(infoPanelKey, info, null);
	}

	public void setStatusInfo(final String infoPanelKey, final String info, final String iconKey) {
		final ViewController viewController = getViewController();
		viewController.addStatusInfo(infoPanelKey, info, FreeplaneIconUtils.createStandardIcon(iconKey));
	}

	@Deprecated
	public void setStatusInfo(final String infoPanelKey, final Icon icon) {
		final ViewController viewController = getViewController();
		viewController.addStatusInfo(infoPanelKey, null, icon);
	}

	public FreeplaneVersion getFreeplaneVersion() {
		return FreeplaneVersion.getVersion();
	}

	public File getUserDirectory() {
	    return new File(ResourceController.getResourceController().getFreeplaneUserDirectory());
    }

	@Deprecated
	public List<Node> find(final ICondition condition) {
		if (scriptContext != null)
			scriptContext.accessAll();
		return ProxyUtils.find(condition, Controller.getCurrentController().getMap().getRootNode(), scriptContext);
	}

	public List<Node> find(final Closure<Boolean> closure) {
		if (scriptContext != null)
			scriptContext.accessAll();
		return ProxyUtils.find(closure, Controller.getCurrentController().getMap().getRootNode(), scriptContext);
	}

	// NodeRO: R
	public List<Node> findAll() {
		if (scriptContext != null)
			scriptContext.accessAll();
		return ProxyUtils.findAll(Controller.getCurrentController().getMap().getRootNode(), scriptContext, true);
    }

	// NodeRO: R
	public List<Node> findAllDepthFirst() {
		if (scriptContext != null)
			scriptContext.accessAll();
		return ProxyUtils.findAll(Controller.getCurrentController().getMap().getRootNode(), scriptContext, false);
    }

	public Map newMap() {
		final MapModel oldMap = Controller.getCurrentController().getMap();
		final MMapIO mapIO = (MMapIO) Controller.getCurrentModeController().getExtension(MapIO.class);
		final MapModel newMap = mapIO.newMapFromDefaultTemplate();
		restartTransaction(oldMap, newMap);
		return new MapProxy(newMap, scriptContext);
	}

	public Map newMap(URL url) {
		try {
			final MapModel oldMap = Controller.getCurrentController().getMap();
			Controller.getCurrentModeController().getMapController().newMap(url);
			final IMapViewManager mapViewManager = Controller.getCurrentController().getMapViewManager();
			final String key = mapViewManager.checkIfFileIsAlreadyOpened(url);
			// make the map the current map even if it was already opened
			if (key == null || !mapViewManager.tryToChangeToMapView(key))
				throw new RuntimeException("map " + url + " does not seem to be opened");
			final MapModel newMap = mapViewManager.getModel();
			restartTransaction(oldMap, newMap);
			return new MapProxy(newMap, scriptContext);
		}
		catch (Exception e) {
			throw new RuntimeException("error on newMap", e);
		}
	}

	private void restartTransaction(final MapModel oldMap, final MapModel newmap) {
		final IUndoHandler oldUndoHandler = (IUndoHandler) oldMap.getExtension(IUndoHandler.class);
		final IUndoHandler newUndoHandler = (IUndoHandler) newmap.getExtension(IUndoHandler.class);
		final int transactionLevel = oldUndoHandler.getTransactionLevel();
        if(transactionLevel == 0){
            return;
        }
		if(transactionLevel == 1){
		    oldUndoHandler.commit();
		    newUndoHandler.startTransaction();
		    return;
		}
		throw new RuntimeException("can not create map inside transaction");
	}

    public float getZoom() {
	    return getViewController().getZoom();
    }
    
    public void setZoom(float ratio) {
    	getViewController().setZoom(ratio);
    }

    public boolean isInteractive() {
        return !Boolean.parseBoolean(System.getProperty("nonInteractive"));
    }
}
