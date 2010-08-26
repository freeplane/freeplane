/**
 * 
 */
package org.freeplane.plugin.script.proxy;

import groovy.lang.Closure;

import java.net.URL;
import java.util.Iterator;
import java.util.List;

import javax.swing.Icon;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.controller.IMapSelection;
import org.freeplane.core.frame.IMapViewManager;
import org.freeplane.core.frame.ViewController;
import org.freeplane.core.undo.IUndoHandler;
import org.freeplane.core.util.FreeplaneIconUtils;
import org.freeplane.core.util.FreeplaneVersion;
import org.freeplane.features.common.filter.condition.ICondition;
import org.freeplane.features.common.map.MapModel;
import org.freeplane.features.common.map.NodeModel;
import org.freeplane.features.mindmapmode.MModeController;
import org.freeplane.features.mindmapmode.map.MMapModel;
import org.freeplane.plugin.script.proxy.Proxy.Map;
import org.freeplane.plugin.script.proxy.Proxy.Node;

class ControllerProxy implements Proxy.Controller {
	public ControllerProxy() {
		super();
	}

	public void centerOnNode(final Node center) {
		final NodeModel nodeModel = ((NodeProxy) center).getDelegate();
		Controller.getCurrentController().getSelection().centerNode(nodeModel);
	}

	public Node getSelected() {
		return new NodeProxy(Controller.getCurrentController().getSelection().getSelected());
	}

	public List<Node> getSelecteds() {
		return ProxyUtils.createNodeList(Controller.getCurrentController().getSelection().getSelection());
	}

	public List<Node> getSortedSelection(final boolean differentSubtrees) {
		return ProxyUtils.createNodeList(Controller.getCurrentController().getSelection().getSortedSelection(
		    differentSubtrees));
	}

	public void select(final Node toSelect) {
		final NodeModel nodeModel = ((NodeProxy) toSelect).getDelegate();
		Controller.getCurrentController().getSelection().selectAsTheOnlyOneSelected(nodeModel);
	}

	public void selectBranch(final Node branchRoot) {
		final NodeModel nodeModel = ((NodeProxy) branchRoot).getDelegate();
		Controller.getCurrentModeController().getMapController().displayNode(nodeModel);
		Controller.getCurrentController().getSelection().selectBranch(nodeModel, false);
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

	@Deprecated
	public List<Node> find(final ICondition condition) {
		return ProxyUtils.find(condition, Controller.getCurrentController().getMap().getRootNode());
	}

	public List<Node> find(final Closure closure) {
		return ProxyUtils.find(closure, Controller.getCurrentController().getMap().getRootNode());
	}

	public Map newMap() {
		final MapModel newMap = Controller.getCurrentModeController().getMapController().newMap(((NodeModel) null));
		return new MapProxy(newMap);
	}

	public Map newMap(URL url) {
		try {
			Controller.getCurrentModeController().getMapController().newMap(url);
			final IMapViewManager mapViewManager = Controller.getCurrentController().getMapViewManager();
			final String key = mapViewManager.checkIfFileIsAlreadyOpened(url);
			// make the map the current map even if it was already opened
			if (key == null || !mapViewManager.tryToChangeToMapView(key))
				throw new RuntimeException("map " + url + " does not seem to be opened");
			return new MapProxy(mapViewManager.getModel());
		}
		catch (Exception e) {
			throw new RuntimeException("error on newMap", e);
		}
	}
}
