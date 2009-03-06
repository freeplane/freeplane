/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is created by Dimitry Polivaev in 2008.
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
package org.freeplane.features.mindmapmode.edge;

import java.awt.Color;
import java.util.ListIterator;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.undo.IUndoableActor;
import org.freeplane.features.common.edge.EdgeController;
import org.freeplane.features.common.edge.EdgeModel;
import org.freeplane.features.common.edge.EdgeStyle;

/**
 * @author Dimitry Polivaev
 */
public class MEdgeController extends EdgeController {
	public MEdgeController(final ModeController modeController) {
		super(modeController);
		final Controller controller = modeController.getController();
		modeController.putAction("edgeColor", new EdgeColorAction(controller));
		modeController.putAction("EdgeWidth_WIDTH_PARENT", new EdgeWidthAction(modeController, EdgeModel.WIDTH_PARENT));
		modeController.putAction("EdgeWidth_WIDTH_THIN", new EdgeWidthAction(modeController, EdgeModel.WIDTH_THIN));
		modeController.putAction("EdgeWidth_1", new EdgeWidthAction(modeController, 1));
		modeController.putAction("EdgeWidth_2", new EdgeWidthAction(modeController, 2));
		modeController.putAction("EdgeWidth_4", new EdgeWidthAction(modeController, 4));
		modeController.putAction("EdgeWidth_8", new EdgeWidthAction(modeController, 8));
		modeController.putAction("EdgeStyle_linear", new EdgeStyleAction(modeController, EdgeStyle.EDGESTYLE_LINEAR));
		modeController.putAction("EdgeStyle_bezier", new EdgeStyleAction(modeController, EdgeStyle.EDGESTYLE_BEZIER));
		modeController.putAction("EdgeStyle_sharp_linear", new EdgeStyleAction(modeController,
		    EdgeStyle.EDGESTYLE_SHARP_LINEAR));
		modeController.putAction("EdgeStyle_sharp_bezier", new EdgeStyleAction(modeController,
		    EdgeStyle.EDGESTYLE_SHARP_BEZIER));
		modeController.putAction("EdgeStyle_hidden", new EdgeStyleAction(modeController, EdgeStyle.EDGESTYLE_HIDDEN));
		modeController.putAction("EdgeStyle_as_parent", new EdgeStyleAsParentAction(modeController));
	}

	public void setColor(final NodeModel node, final Color color) {
		final ModeController modeController = getModeController();
		final Color oldColor = getColor(node);
		if (color.equals(oldColor)) {
			return;
		}
		final IUndoableActor actor = new IUndoableActor() {
			public void act() {
				EdgeModel.createEdgeModel(node).setColor(color);
				modeController.getMapController().nodeChanged(node);
			}

			public String getDescription() {
				return "setColor";
			}

			public void undo() {
				EdgeModel.createEdgeModel(node).setColor(oldColor);
				modeController.getMapController().nodeChanged(node);
			}
		};
		modeController.execute(actor);
	}

	public void setStyle(final NodeModel node, final String style) {
		final ModeController modeController = getModeController();
		final String oldStyle;
		if (style != null) {
			oldStyle = getStyle(node);
			if (style.equals(oldStyle)) {
				return;
			}
		}
		else {
			oldStyle = EdgeModel.createEdgeModel(node).getStyle();
			if (oldStyle == null) {
				return;
			}
		}
		final IUndoableActor actor = new IUndoableActor() {
			public void act() {
				EdgeModel.createEdgeModel(node).setStyle(style);
				modeController.getMapController().nodeChanged(node);
				edgeStyleRefresh(node);
			}

			private void edgeStyleRefresh(final NodeModel node) {
				final ListIterator childrenFolded = modeController.getMapController().childrenFolded(node);
				while (childrenFolded.hasNext()) {
					final NodeModel child = (NodeModel) childrenFolded.next();
					final EdgeModel edge = EdgeModel.getModel(child);
					if (edge == null || edge.getStyle() == null) {
						modeController.getMapController().nodeRefresh(child);
						edgeStyleRefresh(child);
					}
				}
			}

			public String getDescription() {
				return "setStyle";
			}

			public void undo() {
				EdgeModel.createEdgeModel(node).setStyle(oldStyle);
				modeController.getMapController().nodeChanged(node);
				edgeStyleRefresh(node);
			}
		};
		modeController.execute(actor);
	}

	public void setWidth(final NodeModel node, final int width) {
		final ModeController modeController = getModeController();
		final int oldWidth = getWidth(node);
		if (width == oldWidth) {
			return;
		}
		final IUndoableActor actor = new IUndoableActor() {
			public void act() {
				EdgeModel.createEdgeModel(node).setWidth(width);
				modeController.getMapController().nodeChanged(node);
				edgeWidthRefresh(node);
			}

			private void edgeWidthRefresh(final NodeModel node) {
				final ListIterator childrenFolded = modeController.getMapController().childrenFolded(node);
				while (childrenFolded.hasNext()) {
					final NodeModel child = (NodeModel) childrenFolded.next();
					final EdgeModel edge = EdgeModel.getModel(child);
					if (edge == null || edge.getWidth() == EdgeModel.WIDTH_PARENT) {
						modeController.getMapController().nodeRefresh(child);
						edgeWidthRefresh(child);
					}
				}
			}

			public String getDescription() {
				return "setWidth";
			}

			public void undo() {
				EdgeModel.createEdgeModel(node).setWidth(oldWidth);
				modeController.getMapController().nodeChanged(node);
				edgeWidthRefresh(node);
			}
		};
		modeController.execute(actor);
	}
}
