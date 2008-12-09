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

import org.freeplane.controller.Controller;
import org.freeplane.map.tree.NodeModel;
import org.freeplane.map.tree.filemode.FMapController;
import org.freeplane.map.tree.view.MainView;
import org.freeplane.map.tree.view.NodeView;
import org.freeplane.modes.ModeController;

import deprecated.freemind.extensions.IHookFactory;

public class FModeController extends ModeController {
	static public final String MODENAME = "File";

	FModeController() {
		super();
	}

	public boolean extendSelection(final MouseEvent e) {
		final NodeView newlySelectedNodeView = ((MainView) e.getComponent())
		    .getNodeView();
		final boolean extend = e.isControlDown();
		final boolean range = e.isShiftDown();
		final boolean branch = e.isAltGraphDown() || e.isAltDown();
		/*
		 * windows alt, linux altgraph ....
		 */
		boolean retValue = false;
		if (extend || range || branch
		        || !getMapView().isSelected(newlySelectedNodeView)) {
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
			String link = newlySelectedNodeView.getModel().getLink();
			link = (link != null ? link : " ");
			Controller.getController().getViewController().out(link);
		}
		return retValue;
	}

	@Override
	public IHookFactory getHookFactory() {
		throw new IllegalArgumentException("Not implemented yet.");
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
			getLinkController().loadURL();
		}
		else {
			final NodeModel node = (component).getNodeView().getModel();
			((FMapController) getMapController()).toggleFolded(node);
		}
	}

	@Override
	public void shutdown() {
		super.shutdown();
	}

	@Override
	public void startup() {
		final Controller controller = Controller.getController();
		controller.getMapViewManager().changeToMode(MODENAME);
		if (controller.getMapView() == null) {
			((FMapController) getMapController()).newMap(new File(
			    File.separator));
		}
		super.startup();
	}

	@Override
	protected void updateMenus(final String resource) {
		super.updateMenus(resource);
	}
}
