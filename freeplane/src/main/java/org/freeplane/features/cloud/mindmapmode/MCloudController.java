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
package org.freeplane.features.cloud.mindmapmode;

import java.awt.Color;

import org.freeplane.core.undo.IActor;
import org.freeplane.features.cloud.CloudController;
import org.freeplane.features.cloud.CloudModel;
import org.freeplane.features.cloud.CloudModel.Shape;
import org.freeplane.features.map.IExtensionCopier;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.styles.LogicalStyleKeys;

/**
 * @author Dimitry Polivaev
 */
public class MCloudController extends CloudController {
	private static class ExtensionCopier implements IExtensionCopier {
		public void copy(final Object key, final NodeModel from, final NodeModel to) {
			if (!key.equals(LogicalStyleKeys.NODE_STYLE)) {
				return;
			}
			copy(from, to);
		}

		public void copy(final NodeModel from, final NodeModel to) {
			final CloudModel fromStyle = (CloudModel) from.getExtension(CloudModel.class);
			if (fromStyle == null) {
				return;
			}
			final CloudModel toStyle = CloudModel.createModel(to);
			final Color color = fromStyle.getColor();
			if(color != null)
			    toStyle.setColor(color);
			final Shape shape = fromStyle.getShape();
			if(shape != null)
			    toStyle.setShape(shape);
		}

		public void remove(final Object key, final NodeModel from) {
			if (!key.equals(LogicalStyleKeys.NODE_STYLE)) {
				return;
			}
			from.removeExtension(CloudModel.class);
		}

		public void remove(final Object key, final NodeModel from, final NodeModel which) {
			if (!key.equals(LogicalStyleKeys.NODE_STYLE)) {
				return;
			}
			final CloudModel whichStyle = (CloudModel) which.getExtension(CloudModel.class);
			if (whichStyle == null) {
				return;
			}
			final CloudModel fromStyle = (CloudModel) from.getExtension(CloudModel.class);
			if (fromStyle == null) {
				return;
			}
			from.removeExtension(fromStyle);
		}

		public void resolveParentExtensions(Object key, NodeModel to) {
        }
	}

	public MCloudController(final ModeController modeController) {
		super(modeController);
		modeController.registerExtensionCopier(new ExtensionCopier());
		modeController.addAction(new CloudAction());
		modeController.addAction(new CloudColorAction());
		for(Shape shape : Shape.values()){
				modeController.addAction(new CloudShapeAction(shape));
		}
	}

	public void setCloud(final NodeModel node, final boolean enable) {
		final CloudModel cloud = CloudModel.getModel(node);
		if ((cloud != null) == enable) {
			return;
		}
		final Color color = cloud != null ? cloud.getColor() : CloudController.getStandardColor();
		final Shape shape = cloud != null ? cloud.getShape() : CloudController.getStandardShape();
		final ModeController modeController = Controller.getCurrentModeController();
		final IActor actor = new IActor() {
			public void act() {
				if (enable) {
					enable();
				}
				else {
					disable();
				}
			}

			private void disable() {
				final MapController mapController = modeController.getMapController();
				CloudModel.setModel(node, null);
				mapController.nodeChanged(node);
			}

			private void enable() {
				final CloudModel cloud = new CloudModel();
				cloud.setColor(color);
				cloud.setShape(shape);
				final MapController mapController = modeController.getMapController();
				CloudModel.setModel(node, cloud);
				mapController.nodeChanged(node);
			}

			public String getDescription() {
				return "setCloud";
			}

			public void undo() {
				if (enable) {
					disable();
				}
				else {
					enable();
				}
			}
		};
		modeController.execute(actor, node.getMap());
	}

	public void setColor(final NodeModel node, final Color color) {
		setCloud(node, true);
		final ModeController modeController = Controller.getCurrentModeController();
		final Color oldColor = CloudModel.getModel(node).getColor();
		if (color == oldColor || color != null && color.equals(oldColor)) {
			return;
		}
		final IActor actor = new IActor() {
			public void act() {
				CloudModel.getModel(node).setColor(color);
				modeController.getMapController().nodeChanged(node);
			}

			public String getDescription() {
				return "setColor";
			}

			public void undo() {
				CloudModel.getModel(node).setColor(oldColor);
				modeController.getMapController().nodeChanged(node);
			}
		};
		modeController.execute(actor, node.getMap());
	}

	public void setShape(final NodeModel node, final CloudModel.Shape shape) {
		setCloud(node, true);
		final ModeController modeController = Controller.getCurrentModeController();
		final CloudModel.Shape oldShape = CloudModel.getModel(node).getShape();
		if (shape == oldShape || shape != null && shape.equals(oldShape)) {
			return;
		}
		final IActor actor = new IActor() {
			public void act() {
				CloudModel.getModel(node).setShape(shape);
				modeController.getMapController().nodeChanged(node);
			}

			public String getDescription() {
				return "setShape";
			}

			public void undo() {
				CloudModel.getModel(node).setShape(oldShape);
				modeController.getMapController().nodeChanged(node);
			}
		};
		modeController.execute(actor, node.getMap());
	}
}
