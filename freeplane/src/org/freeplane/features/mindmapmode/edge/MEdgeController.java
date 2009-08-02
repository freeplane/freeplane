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
import org.freeplane.core.undo.IActor;
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
		modeController.addAction(new EdgeColorAction(controller));
		modeController.addAction(new EdgeWidthAction(modeController, EdgeModel.WIDTH_PARENT));
		modeController.addAction(new EdgeWidthAction(modeController, EdgeModel.WIDTH_THIN));
		modeController.addAction(new EdgeWidthAction(modeController, 1));
		modeController.addAction(new EdgeWidthAction(modeController, 2));
		modeController.addAction(new EdgeWidthAction(modeController, 4));
		modeController.addAction(new EdgeWidthAction(modeController, 8));
		modeController.addAction(new EdgeStyleAction(modeController, EdgeStyle.EDGESTYLE_LINEAR));
		modeController.addAction(new EdgeStyleAction(modeController, EdgeStyle.EDGESTYLE_BEZIER));
		modeController.addAction(new EdgeStyleAction(modeController, EdgeStyle.EDGESTYLE_SHARP_LINEAR));
		modeController.addAction(new EdgeStyleAction(modeController, EdgeStyle.EDGESTYLE_SHARP_BEZIER));
		modeController.addAction(new EdgeStyleAction(modeController, EdgeStyle.EDGESTYLE_HORIZONTAL));
		modeController.addAction(new EdgeStyleAction(modeController, EdgeStyle.EDGESTYLE_HIDDEN));
		modeController.addAction(new EdgeStyleAsParentAction(modeController));
	}

	public void setColor(final NodeModel node, final Color color) {
		final ModeController modeController = getModeController();
		final Color oldColor = EdgeModel.createEdgeModel(node).getColor();
		if (color == oldColor || color != null && color.equals(oldColor)) {
			return;
		}
		final IActor actor = new IActor() {
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
		modeController.execute(actor, node.getMap());
	}

	public void setStyle(final NodeModel node, final EdgeStyle style) {
		final ModeController modeController = getModeController();
		final EdgeStyle oldStyle;
		if (style != null) {
			oldStyle = EdgeModel.createEdgeModel(node).getStyle();
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
		final IActor actor = new IActor() {
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
		modeController.execute(actor, node.getMap());
	}

	public void setWidth(final NodeModel node, final int width) {
		final ModeController modeController = getModeController();
		final int oldWidth = EdgeModel.createEdgeModel(node).getWidth();
		if (width == oldWidth) {
			return;
		}
		final IActor actor = new IActor() {
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
		modeController.execute(actor, node.getMap());
	}
}
