/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is modified by Dimitry Polivaev in 2008.
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
package org.freeplane.modes.filemode;

import java.awt.event.MouseEvent;
import java.io.File;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.map.ModeController;
import org.freeplane.core.map.NodeModel;
import org.freeplane.map.link.LinkController;
import org.freeplane.map.link.NodeLinks;
import org.freeplane.modes.ui.UserInputListenerFactory;
import org.freeplane.view.swing.map.MainView;
import org.freeplane.view.swing.map.NodeView;

public class FModeController extends ModeController {
	static public final String MODENAME = "File";

	FModeController() {
		super();
	}

	public boolean extendSelection(final MouseEvent e) {
		final NodeView newlySelectedNodeView = ((MainView) e.getComponent()).getNodeView();
		final boolean extend = e.isControlDown();
		final boolean range = e.isShiftDown();
		final boolean branch = e.isAltGraphDown() || e.isAltDown();
		/*
		 * windows alt, linux altgraph ....
		 */
		boolean retValue = false;
		if (extend || range || branch || !getMapView().isSelected(newlySelectedNodeView)) {
			if (!range) {
				if (extend) {
					getMapView().toggleSelected(newlySelectedNodeView);
				}
				else {
					select(newlySelectedNodeView);
				}
				retValue = true;
			}
			else {
				retValue = getMapView().selectContinuous(newlySelectedNodeView);
			}
			if (branch) {
				getMapView().selectBranch(newlySelectedNodeView, extend);
				retValue = true;
			}
		}
		if (retValue) {
			e.consume();
			String link = NodeLinks.getLink(newlySelectedNodeView.getModel());
			link = (link != null ? link : " ");
			Controller.getController().getViewController().out(link);
		}
		return retValue;
	}

	@Override
	public String getModeName() {
		return FModeController.MODENAME;
	}

	@Override
	public void plainClick(final MouseEvent e) {
		/* perform action only if one selected node. */
		if (getSelectedNodes().size() != 1) {
			return;
		}
		final MainView component = (MainView) e.getComponent();
		if (component.isInFollowLinkRegion(e.getX())) {
			LinkController.getController(this).loadURL();
		}
		else {
			final NodeModel node = (component).getNodeView().getModel();
			((FMapController) getMapController()).toggleFolded(node);
		}
	}

	@Override
	public void startup() {
		final Controller controller = Controller.getController();
		controller.getMapViewManager().changeToMode(MODENAME);
		if (controller.getMapView() == null) {
			((FMapController) getMapController()).newMap(new File(File.separator));
		}
		super.startup();
	}

	
	protected void updateMenus(final String resource) {
		final UserInputListenerFactory userInputListenerFactory = (UserInputListenerFactory) getUserInputListenerFactory();
		userInputListenerFactory.setMenuStructure(resource);
		userInputListenerFactory.updateMenus(this);
		super.updateMenus();
	}
}
