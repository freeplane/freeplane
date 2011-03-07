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
package org.freeplane.features.mindmapmode.nodestyle;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.ListIterator;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.extension.IExtensionCopier;
import org.freeplane.core.ui.AMultipleNodeAction;
import org.freeplane.core.undo.IActor;
import org.freeplane.features.common.map.MapController;
import org.freeplane.features.common.map.ModeController;
import org.freeplane.features.common.map.NodeModel;
import org.freeplane.features.common.nodestyle.NodeStyleController;
import org.freeplane.features.common.nodestyle.NodeStyleModel;
import org.freeplane.features.common.styles.LogicalStyleKeys;

/**
 * @author Dimitry Polivaev
 */
public class MNodeStyleController extends NodeStyleController {
	private static class StyleCopier implements IExtensionCopier {
		public void copy(final Object key, final NodeModel from, final NodeModel to) {
			if (!key.equals(LogicalStyleKeys.NODE_STYLE)) {
				return;
			}
			copy(from, to);
		}

		public void copy(final NodeModel from, final NodeModel to) {
			final NodeStyleModel fromStyle = (NodeStyleModel) from.getExtension(NodeStyleModel.class);
			if (fromStyle == null) {
				return;
			}
			fromStyle.copyTo(NodeStyleModel.createNodeStyleModel(to));
		}

		public void remove(final Object key, final NodeModel from) {
			if (!key.equals(LogicalStyleKeys.NODE_STYLE)) {
				return;
			}
			from.removeExtension(NodeStyleModel.class);
		}

		public void remove(final Object key, final NodeModel from, final NodeModel which) {
			if (!key.equals(LogicalStyleKeys.NODE_STYLE)) {
				return;
			}
			final NodeStyleModel whichStyle = (NodeStyleModel) which.getExtension(NodeStyleModel.class);
			if (whichStyle == null) {
				return;
			}
			final NodeStyleModel fromStyle = (NodeStyleModel) from.getExtension(NodeStyleModel.class);
			if (fromStyle == null) {
				return;
			}
			if (null != whichStyle.isBold()) {
				fromStyle.setBold(null);
			}
			if (null != whichStyle.isItalic()) {
				fromStyle.setItalic(null);
			}
			if (null != whichStyle.getFontFamilyName()) {
				fromStyle.setFontFamilyName(null);
			}
			if (null != whichStyle.getFontSize()) {
				fromStyle.setFontSize(null);
			}
			if (null != whichStyle.getShape()) {
				fromStyle.setShape(null);
			}
			if (null != whichStyle.getColor()) {
				fromStyle.setColor(null);
			}
			if (null != whichStyle.getBackgroundColor()) {
				fromStyle.setBackgroundColor(null);
			}
			if (null != whichStyle.getNodeTextTemplate()) {
				fromStyle.setNodeTextTemplate(null);
			}
			if (null != whichStyle.getNodeNumbering()) {
				fromStyle.setNodeNumbering(null);
			}
		}
	}

	public MNodeStyleController(final ModeController modeController) {
		super(modeController);
		modeController.registerExtensionCopier(new StyleCopier());
		modeController.addAction(new BoldAction());
		modeController.addAction(new ItalicAction());
		modeController.addAction(new CopyFormat());
		modeController.addAction(new PasteFormat());
		modeController.addAction(new RemoveFormatAction());
		final AMultipleNodeAction increaseNodeFont = new AMultipleNodeAction("IncreaseNodeFontAction") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void actionPerformed(final ActionEvent e, final NodeModel node) {
				increaseFontSize(node, 1);
			}
		};
		modeController.addAction(increaseNodeFont);
		final AMultipleNodeAction decreaseNodeFont = new AMultipleNodeAction("DecreaseNodeFontAction") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void actionPerformed(final ActionEvent e, final NodeModel node) {
				increaseFontSize(node, -1);
			}
		};
		modeController.addAction(decreaseNodeFont);
		modeController.addAction(new NodeColorAction());
		modeController.addAction(new NodeColorBlendAction());
		modeController.addAction(new NodeBackgroundColorAction());
		modeController.addAction(new NodeShapeAction(NodeStyleModel.STYLE_FORK));
		modeController.addAction(new NodeShapeAction(NodeStyleModel.STYLE_BUBBLE));
	}

	public void copyStyle(final NodeModel source, final NodeModel target) {
		final NodeStyleModel sourceStyleModel = NodeStyleModel.getModel(source);
		if (sourceStyleModel != null) {
			setColor(target, sourceStyleModel.getColor());
			setBackgroundColor(target, sourceStyleModel.getBackgroundColor());
			setShape(target, sourceStyleModel.getShape());
			setFontFamily(target, sourceStyleModel.getFontFamilyName());
			setFontSize(target, sourceStyleModel.getFontSize());
			setBold(target, sourceStyleModel.isBold());
			setItalic(target, sourceStyleModel.isItalic());
			setNodeTextTemplate(target, sourceStyleModel.getNodeTextTemplate());
			setNodeNumbering(target, sourceStyleModel.getNodeNumbering());
		}
	}

	private NodeStyleModel createOwnStyleModel(final NodeModel node) {
		{
			final NodeStyleModel styleModel = NodeStyleModel.getModel(node);
			if (styleModel != null) {
				return styleModel;
			}
		}
		final ModeController modeController = Controller.getCurrentModeController();
		final IActor actor = new IActor() {
			public void act() {
				node.addExtension(new NodeStyleModel());
			}

			public String getDescription() {
				return null;
			}

			public void undo() {
				node.removeExtension(NodeStyleModel.class);
			}
		};
		modeController.execute(actor, node.getMap());
		return NodeStyleModel.getModel(node);
	}

	/**
	*
	*/
	public void increaseFontSize(final NodeModel node, final int increment) {
		final int newSize = getFontSize(node) + increment;
		if (newSize > 0) {
			setFontSize(node, newSize);
		}
	}

	public void setBackgroundColor(final NodeModel node, final Color color) {
		final ModeController modeController = Controller.getCurrentModeController();
		final Color oldColor = NodeStyleModel.getBackgroundColor(node);
		if (color == oldColor || color != null && color.equals(oldColor)) {
			return;
		}
		final IActor actor = new IActor() {
			public void act() {
				NodeStyleModel.setBackgroundColor(node, color);
				Controller.getCurrentModeController().getMapController().nodeChanged(node);
			}

			public String getDescription() {
				return "setBackgroundColor";
			}

			public void undo() {
				NodeStyleModel.setBackgroundColor(node, oldColor);
				Controller.getCurrentModeController().getMapController().nodeChanged(node);
			}
		};
		modeController.execute(actor, node.getMap());
	}

	/**
	 * @param bold
	 */
	public void setBold(final NodeModel node, final Boolean bold) {
		final Boolean oldBold = NodeStyleModel.isBold(node);
		if (oldBold == bold || oldBold != null && oldBold.equals(bold)) {
			return;
		}
		createOwnStyleModel(node);
		final ModeController modeController = Controller.getCurrentModeController();
		final IActor actor = new IActor() {
			public void act() {
				final NodeStyleModel styleModel = NodeStyleModel.getModel(node);
				styleModel.setBold(bold);
				Controller.getCurrentModeController().getMapController().nodeChanged(node);
			}

			public String getDescription() {
				return "setBold";
			}

			public void undo() {
				final NodeStyleModel styleModel = NodeStyleModel.getModel(node);
				styleModel.setBold(oldBold);
				Controller.getCurrentModeController().getMapController().nodeChanged(node);
			}
		};
		modeController.execute(actor, node.getMap());
	}

	public void setColor(final NodeModel node, final Color color) {
		final ModeController modeController = Controller.getCurrentModeController();
		final Color oldColor = NodeStyleModel.getColor(node);
		if (oldColor == color || oldColor != null && oldColor.equals(color)) {
			return;
		}
		final IActor actor = new IActor() {
			public void act() {
				NodeStyleModel.setColor(node, color);
				Controller.getCurrentModeController().getMapController().nodeChanged(node);
			}

			public String getDescription() {
				return "setColor";
			}

			public void undo() {
				NodeStyleModel.setColor(node, oldColor);
				Controller.getCurrentModeController().getMapController().nodeChanged(node);
			}
		};
		modeController.execute(actor, node.getMap());
	}

	/**
	 * @param fontFamily
	 */
	public void setFontFamily(final NodeModel node, final String fontFamily) {
		final String oldFontFamily = NodeStyleModel.getFontFamilyName(node);
		if (oldFontFamily == fontFamily || oldFontFamily != null && oldFontFamily.equals(fontFamily)) {
			return;
		}
		createOwnStyleModel(node);
		final ModeController modeController = Controller.getCurrentModeController();
		final IActor actor = new IActor() {
			public void act() {
				final NodeStyleModel styleModel = NodeStyleModel.getModel(node);
				styleModel.setFontFamilyName(fontFamily);
				Controller.getCurrentModeController().getMapController().nodeChanged(node);
			}

			public String getDescription() {
				return "setFontFamily";
			}

			public void undo() {
				final NodeStyleModel styleModel = NodeStyleModel.getModel(node);
				styleModel.setFontFamilyName(oldFontFamily);
				Controller.getCurrentModeController().getMapController().nodeChanged(node);
			}
		};
		modeController.execute(actor, node.getMap());
	}

	public void setFontFamily(final String fontFamily) {
		for (final NodeModel selected : Controller.getCurrentModeController().getMapController().getSelectedNodes()) {
			setFontFamily(selected, fontFamily);
		}
	}

	public void setFontSize(final int size) {
		final List<NodeModel> selectedNodes = Controller.getCurrentModeController().getMapController().getSelectedNodes();
		for (final NodeModel selected : selectedNodes) {
			setFontSize(selected, size);
		}
	}

	/**
	 * @param fontSize
	 */
	public void setFontSize(final NodeModel node, final Integer fontSize) {
		final Integer oldFontSize = NodeStyleModel.getFontSize(node);
		if (oldFontSize == fontSize || oldFontSize != null && oldFontSize.equals(fontSize)) {
			return;
		}
		createOwnStyleModel(node);
		final ModeController modeController = Controller.getCurrentModeController();
		final IActor actor = new IActor() {
			public void act() {
				final NodeStyleModel styleModel = NodeStyleModel.getModel(node);
				styleModel.setFontSize(fontSize);
				Controller.getCurrentModeController().getMapController().nodeChanged(node);
			}

			public String getDescription() {
				return "setFontSize";
			}

			public void undo() {
				final NodeStyleModel styleModel = NodeStyleModel.getModel(node);
				styleModel.setFontSize(oldFontSize);
				Controller.getCurrentModeController().getMapController().nodeChanged(node);
			}
		};
		modeController.execute(actor, node.getMap());
	}

	/**
	 * @param italic
	 */
	public void setItalic(final NodeModel node, final Boolean italic) {
		final Boolean oldItalic = NodeStyleModel.isItalic(node);
		if (oldItalic == italic || oldItalic != null && oldItalic.equals(italic)) {
			return;
		}
		createOwnStyleModel(node);
		final ModeController modeController = Controller.getCurrentModeController();
		final IActor actor = new IActor() {
			public void act() {
				final NodeStyleModel styleModel = NodeStyleModel.getModel(node);
				styleModel.setItalic(italic);
				Controller.getCurrentModeController().getMapController().nodeChanged(node);
			}

			public String getDescription() {
				return "setItalic";
			}

			public void undo() {
				final NodeStyleModel styleModel = NodeStyleModel.getModel(node);
				styleModel.setItalic(oldItalic);
				Controller.getCurrentModeController().getMapController().nodeChanged(node);
			}
		};
		modeController.execute(actor, node.getMap());
	}

	public void setNodeNumbering(final NodeModel node, final Boolean enableNodeNumbering) {
		final ModeController modeController = Controller.getCurrentModeController();
		final Boolean oldValue = NodeStyleModel.getNodeNumbering(node);
		final IActor actor = new IActor() {
			public void act() {
				NodeStyleModel.setNodeNumbering(node, enableNodeNumbering);
				final MapController mapController = modeController.getMapController();
				mapController.setSaved(node.getMap(), false);
				mapController.delayedNodeRefresh(node, NodeStyleController.NODE_NUMBERING, oldValue, enableNodeNumbering);
			}

			public String getDescription() {
				return "setNodeNumbering";
			}

			public void undo() {
				NodeStyleModel.setNodeNumbering(node, oldValue);
				final MapController mapController = modeController.getMapController();
				mapController.setSaved(node.getMap(), false);
				modeController.getMapController().delayedNodeRefresh(node, NodeStyleController.NODE_NUMBERING, enableNodeNumbering, oldValue);
			}
		};
		modeController.execute(actor, node.getMap());
    }

	public void setNodeTextTemplate(final NodeModel node, final String template) {
		final ModeController modeController = Controller.getCurrentModeController();
		final String oldTemplate = NodeStyleModel.getNodeTextTemplate(node);
		final IActor actor = new IActor() {
			public void act() {
				NodeStyleModel.setNodeTextTemplate(node, template);
				modeController.getMapController().nodeChanged(node);
			}

			public String getDescription() {
				return "setNodeTextTemplate";
			}

			public void undo() {
				NodeStyleModel.setNodeTextTemplate(node, oldTemplate);
				modeController.getMapController().nodeChanged(node);
			}
		};
		modeController.execute(actor, node.getMap());
    }

	public void setShape(final NodeModel node, final String shape) {
		final ModeController modeController = Controller.getCurrentModeController();
		final String oldShape = NodeStyleModel.getShape(node);
		final IActor actor = new IActor() {
			public void act() {
				NodeStyleModel.setShape(node, shape);
				modeController.getMapController().nodeChanged(node);
				nodeShapeRefresh(node);
			}

			public String getDescription() {
				return "setShape";
			}

			private void nodeShapeRefresh(final NodeModel node) {
				final ListIterator<NodeModel> childrenFolded = modeController.getMapController().childrenFolded(node);
				while (childrenFolded.hasNext()) {
					final NodeModel child = childrenFolded.next();
					if (NodeStyleModel.getShape(child) == null) {
						modeController.getMapController().nodeRefresh(child);
						nodeShapeRefresh(child);
					}
				}
			}

			public void undo() {
				NodeStyleModel.setShape(node, oldShape);
				modeController.getMapController().nodeChanged(node);
				nodeShapeRefresh(node);
			}
		};
		modeController.execute(actor, node.getMap());
	}
}
