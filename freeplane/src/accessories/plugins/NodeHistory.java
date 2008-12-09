/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package accessories.plugins;

import java.awt.EventQueue;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.Action;
import javax.swing.JMenuItem;

import org.freeplane.controller.Controller;
import org.freeplane.map.tree.NodeModel;
import org.freeplane.map.tree.view.MapView;
import org.freeplane.map.tree.view.NodeView;
import org.freeplane.modes.INodeSelectionListener;
import org.freeplane.modes.ModeController;
import org.freeplane.modes.mindmapmode.MModeController;
import org.freeplane.modes.mindmapmode.NodeHookAction;
import org.freeplane.ui.IMenuItemEnabledListener;

import deprecated.freemind.extensions.IHookRegistration;
import deprecated.freemind.modes.mindmapmode.hooks.MindMapNodeHookAdapter;

/**
 * @author foltin
 */
public class NodeHistory extends MindMapNodeHookAdapter {
	private static class NodeHolder {
		public String mMapViewName;
		public String mNodeId;

		public NodeHolder(final NodeModel pNode,
		                  final MModeController pMindMapController) {
			mNodeId = pNode.createID();
			final MapView mapView = getModuleGivenModeController(pMindMapController);
			if (mapView == null) {
				throw new IllegalArgumentException(
				    "MapView not present to controller " + pMindMapController);
			}
			mMapViewName = mapView.getName();
		}

		private MapView getMapView() {
			MapView mapView = null;
			final Map mapViews = Controller.getController().getMapViewManager()
			    .getMapViews();
			for (final Iterator iter = mapViews.keySet().iterator(); iter
			    .hasNext();) {
				final String mapViewName = (String) iter.next();
				if (mapViewName != null && mapViewName.equals(mMapViewName)) {
					mapView = (MapView) mapViews.get(mapViewName);
					break;
				}
			}
			return mapView;
		}

		private ModeController getModeController() {
			ModeController modeController = null;
			final MapView mapView = getMapView();
			if (mapView != null) {
				modeController = mapView.getModeController();
			}
			return modeController;
		}

		private MapView getModuleGivenModeController(
		                                             final MModeController pMindMapController) {
			MapView mapView = null;
			for (final Iterator iter = Controller.getController()
			    .getMapViewManager().getMapViews().entrySet().iterator(); iter
			    .hasNext();) {
				final Map.Entry mapEntry = (Map.Entry) iter.next();
				mapView = (MapView) mapEntry.getValue();
				if (pMindMapController.equals(mapView.getModeController())) {
					break;
				}
			}
			return mapView;
		}

		/** @return null, if node not found. */
		public NodeModel getNode() {
			final ModeController modeController = getModeController();
			if (modeController != null) {
				return modeController.getMapController().getNodeFromID(mNodeId);
			}
			return null;
		}

		public boolean isIdentical(final NodeModel pNode,
		                           final MModeController pMindMapController) {
			final String id = pNode.createID();
			final MapView mapView = getModuleGivenModeController(pMindMapController);
			if (mapView != null) {
				return id.equals(mNodeId);
			}
			return false;
		}
	}

	public static class Registration implements IHookRegistration,
	        INodeSelectionListener, IMenuItemEnabledListener {
		final private MModeController controller;

		public Registration(final ModeController controller) {
			this.controller = (MModeController) controller;
		}

		public void deRegister() {
			controller.removeNodeSelectionListener(this);
		}

		public boolean isEnabled(final JMenuItem pItem, final Action pAction) {
			final String hookName = ((NodeHookAction) pAction).getHookName();
			if ("accessories/plugins/NodeHistoryBack.properties"
			    .equals(hookName)) {
				return NodeHistory.sCurrentPosition > 1;
			}
			else {
				return NodeHistory.sCurrentPosition < NodeHistory.sNodeVector
				    .size();
			}
		}

		public void onDeselect(final NodeView pNode) {
		}

		public void onSelect(final NodeView pNode) {
			/*******************************************************************
			 * don't denote positions, if somebody navigates through them. *
			 */
			if (!NodeHistory.sPreventRegistration) {
				if (NodeHistory.sCurrentPosition > 0
				        && ((NodeHolder) NodeHistory.sNodeVector
				            .get(NodeHistory.sCurrentPosition - 1))
				            .isIdentical(pNode.getModel(), controller)) {
					return;
				}
				if (NodeHistory.sCurrentPosition != NodeHistory.sNodeVector
				    .size()) {
					/***********************************************************
					 * * we change the selected in the middle of our vector.
					 * Thus we remove all the coming nodes:
					 **********************************************************/
					for (int i = NodeHistory.sNodeVector.size() - 1; i >= NodeHistory.sCurrentPosition; --i) {
						NodeHistory.sNodeVector.removeElementAt(i);
					}
				}
				NodeHistory.sNodeVector.add(new NodeHolder(pNode.getModel(),
				    controller));
				NodeHistory.sCurrentPosition++;
				while (NodeHistory.sNodeVector.size() > 100) {
					NodeHistory.sNodeVector.removeElementAt(0);
					NodeHistory.sCurrentPosition--;
				}
			}
		}

		public void register() {
			controller.addNodeSelectionListener(this);
		}
	}

	private static int sCurrentPosition = 0;
	/** Of NodeHolder */
	private static Vector sNodeVector = new Vector();
	private static boolean sPreventRegistration = false;

	/**
	 *
	 */
	public NodeHistory() {
		super();
	}

	@Override
	public void invoke(final NodeModel node) {
		super.invoke(node);
		final MModeController modeController = getMindMapController();
		final String direction = getResourceString("direction");
		if ("back".equals(direction)) {
			if (NodeHistory.sCurrentPosition > 1) {
				--NodeHistory.sCurrentPosition;
			}
			else {
				return;
			}
		}
		else {
			if (NodeHistory.sCurrentPosition < NodeHistory.sNodeVector.size()) {
				++NodeHistory.sCurrentPosition;
			}
			else {
				return;
			}
		}
		if (NodeHistory.sCurrentPosition == 0) {
			return;
		}
		final NodeHolder nodeHolder = (NodeHolder) NodeHistory.sNodeVector
		    .get(NodeHistory.sCurrentPosition - 1);
		final Controller mainController = Controller.getController();
		final NodeModel toBeSelected = (nodeHolder).getNode();
		boolean changeModule = false;
		MapView newModule = null;
		if (nodeHolder.getModeController() != getMindMapController()) {
			changeModule = true;
			newModule = nodeHolder.getMapView();
			if (newModule == null) {
				invoke(node);
				return;
			}
		}
		final boolean fChangeModule = changeModule;
		final MapView fNewModule = newModule;
		NodeHistory.sPreventRegistration = true;
		/***********************************************************************
		 * as the selection is restored after invoke, we make this trick to
		 * change it.
		 **********************************************************************/
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				ModeController c = modeController;
				if (fChangeModule) {
					final boolean res = mainController.getMapViewManager()
					    .changeToMapView(fNewModule.toString());
					if (!res) {
						Logger.global.warning("Can't change to map mapView "
						        + fNewModule);
						NodeHistory.sPreventRegistration = false;
						return;
					}
					c = fNewModule.getModeController();
				}
				if (!toBeSelected.isRoot()) {
					c.getMapController().setFolded(
					    toBeSelected.getParentNode(), false);
				}
				final NodeView nodeView = c.getNodeView(toBeSelected);
				if (nodeView != null) {
					c.select(nodeView);
					NodeHistory.sPreventRegistration = false;
				}
			}
		});
	}
}
