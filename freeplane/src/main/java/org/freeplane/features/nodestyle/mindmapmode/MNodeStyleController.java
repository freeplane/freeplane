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
package org.freeplane.features.nodestyle.mindmapmode;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.util.Collection;

import org.freeplane.core.ui.AMultipleNodeAction;
import org.freeplane.core.ui.LengthUnits;
import org.freeplane.core.undo.IActor;
import org.freeplane.core.util.Quantity;
import org.freeplane.features.DashVariant;
import org.freeplane.features.map.IExtensionCopier;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.nodestyle.NodeBorderModel;
import org.freeplane.features.nodestyle.NodeSizeModel;
import org.freeplane.features.nodestyle.NodeStyleController;
import org.freeplane.features.nodestyle.NodeStyleModel;
import org.freeplane.features.nodestyle.NodeStyleModel.HorizontalTextAlignment;
import org.freeplane.features.nodestyle.NodeStyleModel.Shape;
import org.freeplane.features.nodestyle.ShapeConfigurationModel;
import org.freeplane.features.styles.LogicalStyleKeys;

/**
 * @author Dimitry Polivaev
 */
public class MNodeStyleController extends NodeStyleController {
	private static class StyleCopier implements IExtensionCopier {
		final private ModeController modeController;

		public StyleCopier(ModeController modeController) {
	        this.modeController = modeController;
        }

		@Override
		public void copy(final Object key, final NodeModel from, final NodeModel to) {
			if (!key.equals(LogicalStyleKeys.NODE_STYLE)) {
				return;
			}
			copy(from, to);
		}

		public void copy(final NodeModel from, final NodeModel to) {
			final NodeStyleModel fromStyle = from.getExtension(NodeStyleModel.class);
			if (fromStyle != null) {
				fromStyle.copyTo(NodeStyleModel.createNodeStyleModel(to));
			}
			final NodeSizeModel fromSize = from.getExtension(NodeSizeModel.class);
			if (fromSize != null) {
				fromSize.copyTo(NodeSizeModel.createNodeSizeModel(to));
			}
			final NodeBorderModel fromBorder = from.getExtension(NodeBorderModel.class);
			if (fromBorder != null) {
				fromBorder.copyTo(NodeBorderModel.createNodeBorderModel(to));
			}

		}

		@Override
		public void remove(final Object key, final NodeModel from) {
			if (!key.equals(LogicalStyleKeys.NODE_STYLE)) {
				return;
			}
			from.removeExtension(NodeStyleModel.class);
			from.removeExtension(NodeSizeModel.class);
			from.removeExtension(NodeBorderModel.class);
		}

		@Override
		public void remove(final Object key, final NodeModel from, final NodeModel which) {
			if (!key.equals(LogicalStyleKeys.NODE_STYLE)) {
				return;
			}
			removeStyleData(key, from, which);
			removeSizeData(key, from, which);
			removeBorderData(key, from, which);
		}

		private void removeSizeData(Object key, NodeModel from, NodeModel which) {
			final NodeSizeModel whichData = which.getExtension(NodeSizeModel.class);
			if (whichData == null) {
				return;
			}
			final NodeSizeModel fromData = from.getExtension(NodeSizeModel.class);
			if (fromData == null) {
				return;
			}
			if (null != whichData.getMaxNodeWidth()) {
				fromData.setMaxNodeWidth(null);
			}
			if (null != whichData.getMinNodeWidth()) {
				fromData.setMinNodeWidth(null);
			}
        }

		private void removeBorderData(Object key, NodeModel from, NodeModel which) {
			final NodeBorderModel whichData = which.getExtension(NodeBorderModel.class);
			if (whichData == null) {
				return;
			}
			final NodeBorderModel fromData = from.getExtension(NodeBorderModel.class);
			if (fromData == null) {
				return;
			}
			if (null != whichData.getBorderWidthMatchesEdgeWidth()) {
				fromData.setBorderWidthMatchesEdgeWidth(null);
			}
			if (null != whichData.getBorderWidth()) {
				fromData.setBorderWidth(null);
			}
			if (null != whichData.getBorderDashMatchesEdgeDash()) {
				fromData.setBorderDashMatchesEdgeDash(null);
			}
			if (null != whichData.getBorderDash()) {
				fromData.setBorderDash(null);
			}
			if (null != whichData.getBorderColorMatchesEdgeColor()) {
				fromData.setBorderColorMatchesEdgeColor(null);
			}
			if (null != whichData.getBorderColor()) {
				fromData.setBorderColor(null);
			}
        }

		private void removeStyleData(Object key, NodeModel from, NodeModel which) {
			final NodeStyleModel whichStyle = which.getExtension(NodeStyleModel.class);
			if (whichStyle == null) {
				return;
			}
			final NodeStyleModel fromStyle = from.getExtension(NodeStyleModel.class);
			if (fromStyle == null) {
				return;
			}
			if (null != whichStyle.isBold()) {
				fromStyle.setBold(null);
			}
			if (null != whichStyle.isStrikedThrough()) {
				fromStyle.setStrikedThrough(null);
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
			if (ShapeConfigurationModel.NULL_SHAPE != whichStyle.getShapeConfiguration()) {
				fromStyle.setShapeConfiguration(ShapeConfigurationModel.NULL_SHAPE);
			}
			if (null != whichStyle.getColor()) {
				fromStyle.setColor(null);
			}
			if (null != whichStyle.getBackgroundColor()) {
				fromStyle.setBackgroundColor(null);
			}
			if (null != whichStyle.getNodeFormat()) {
				fromStyle.setNodeFormat(null);
			}
			if (null != whichStyle.getNodeNumbering()) {
				fromStyle.setNodeNumbering(null);
			}
			if (null != whichStyle.getHorizontalTextAlignment()) {
				fromStyle.setHorizontalTextAlignment(null);
			}
       }

		@Override
		public void resolveParentExtensions(Object key, NodeModel to) {
			if (!key.equals(LogicalStyleKeys.NODE_STYLE)) {
				return;
			}
			resolveShape(to);
       }
		private void resolveShape(NodeModel to) {
	        if (hasOwnShape(to))
				return;
			for(NodeModel source = to.getParentNode(); source != null; source = source.getParentNode() ){
				if(hasOwnShape(source)){
					final Shape shape = getShape(source);
					NodeStyleModel.createNodeStyleModel(to).setShape(shape);
					return;
				}
			}
        }

		private boolean hasOwnShape(NodeModel to) {
	        return ! Shape.as_parent.equals(getShape(to));
        }

		private Shape getShape(NodeModel node) {
			return modeController.getExtension(NodeStyleController.class).getShape(node);
		}

	}

	public MNodeStyleController(final ModeController modeController) {
		super(modeController);
		modeController.registerExtensionCopier(new StyleCopier(modeController));
		modeController.addAction(new BoldAction());
		modeController.addAction(new StrikeThroughAction());
		modeController.addAction(new ItalicAction());
		modeController.addAction(new CopyFormat());
		modeController.addAction(new PasteFormat());
		modeController.addAction(new RemoveFormatAction());
		modeController.addAction(new HorizontalTextAlignmentAction(HorizontalTextAlignment.LEFT));
		modeController.addAction(new HorizontalTextAlignmentAction(HorizontalTextAlignment.CENTER));
		modeController.addAction(new HorizontalTextAlignmentAction(HorizontalTextAlignment.RIGHT));
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
		for(NodeStyleModel.Shape shape : NodeStyleModel.Shape.values()){
			if(shape.equals(Shape.as_parent))
				break;
			modeController.addAction(new NodeShapeAction(shape));
		}
	}

	public void copyStyle(final NodeModel source, final NodeModel target) {
		copyStyleModel(source, target);
		copySizeModel(source, target);
		copyBorderModel(source, target);
	}

	protected void copyStyleModel(final NodeModel source, final NodeModel target) {
	    final NodeStyleModel sourceStyleModel = NodeStyleModel.getModel(source);
		if (sourceStyleModel != null) {
			setColor(target, sourceStyleModel.getColor());
			setBackgroundColor(target, sourceStyleModel.getBackgroundColor());
			setShapeConfiguration(target, sourceStyleModel.getShapeConfiguration());
			setFontFamily(target, sourceStyleModel.getFontFamilyName());
			setFontSize(target, sourceStyleModel.getFontSize());
			setBold(target, sourceStyleModel.isBold());
			setStrikedThrough(target, sourceStyleModel.isStrikedThrough());
			setItalic(target, sourceStyleModel.isItalic());
			setNodeFormat(target, sourceStyleModel.getNodeFormat());
			setNodeNumbering(target, sourceStyleModel.getNodeNumbering());
			setHorizontalTextAlignment(target, sourceStyleModel.getHorizontalTextAlignment());
		}
    }

	private void copySizeModel(final NodeModel source, final NodeModel target) {
	    final NodeSizeModel sourceSizeModel = NodeSizeModel.getModel(source);
		if (sourceSizeModel != null) {
			setMaxNodeWidth(target, sourceSizeModel.getMaxNodeWidth());
			setMinNodeWidth(target, sourceSizeModel.getMinNodeWidth());
		}
    }

	private void copyBorderModel(final NodeModel source, final NodeModel target) {
	    final NodeBorderModel from = NodeBorderModel.getModel(source);
		if (from != null) {
			setBorderWidthMatchesEdgeWidth(target, from.getBorderWidthMatchesEdgeWidth());
			setBorderWidth(target, from.getBorderWidth());
			setBorderDashMatchesEdgeDash(target, from.getBorderDashMatchesEdgeDash());
			setBorderDash(target, from.getBorderDash());
			setBorderColorMatchesEdgeColor(target, from.getBorderColorMatchesEdgeColor());
			setBorderColor(target, from.getBorderColor());
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
			@Override
			public void act() {
				node.addExtension(new NodeStyleModel());
			}

			@Override
			public String getDescription() {
				return null;
			}

			@Override
			public void undo() {
				node.removeExtension(NodeStyleModel.class);
			}
		};
		modeController.execute(actor, node.getMap());
		return NodeStyleModel.getModel(node);
	}

	private NodeSizeModel createOwnSizeModel(final NodeModel node) {
		{
			final NodeSizeModel sizeModel = NodeSizeModel.getModel(node);
			if (sizeModel != null) {
				return sizeModel;
			}
		}
		final ModeController modeController = Controller.getCurrentModeController();
		final IActor actor = new IActor() {
			@Override
			public void act() {
				node.addExtension(new NodeSizeModel());
			}

			@Override
			public String getDescription() {
				return null;
			}

			@Override
			public void undo() {
				node.removeExtension(NodeSizeModel.class);
			}
		};
		modeController.execute(actor, node.getMap());
		return NodeSizeModel.getModel(node);
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
			@Override
			public void act() {
				NodeStyleModel.setBackgroundColor(node, color);
				Controller.getCurrentModeController().getMapController().nodeChanged(node);
			}

			@Override
			public String getDescription() {
				return "setBackgroundColor";
			}

			@Override
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
			@Override
			public void act() {
				final NodeStyleModel styleModel = NodeStyleModel.getModel(node);
				styleModel.setBold(bold);
				Controller.getCurrentModeController().getMapController().nodeChanged(node);
			}

			@Override
			public String getDescription() {
				return "setBold";
			}

			@Override
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
			@Override
			public void act() {
				NodeStyleModel.setColor(node, color);
				Controller.getCurrentModeController().getMapController().nodeChanged(node);
			}

			@Override
			public String getDescription() {
				return "setColor";
			}

			@Override
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
			@Override
			public void act() {
				final NodeStyleModel styleModel = NodeStyleModel.getModel(node);
				styleModel.setFontFamilyName(fontFamily);
				Controller.getCurrentModeController().getMapController().nodeChanged(node);
			}

			@Override
			public String getDescription() {
				return "setFontFamily";
			}

			@Override
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
		final Collection<NodeModel> selectedNodes = Controller.getCurrentModeController().getMapController().getSelectedNodes();
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
			@Override
			public void act() {
				final NodeStyleModel styleModel = NodeStyleModel.getModel(node);
				styleModel.setFontSize(fontSize);
				Controller.getCurrentModeController().getMapController().nodeChanged(node);
			}

			@Override
			public String getDescription() {
				return "setFontSize";
			}

			@Override
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
			@Override
			public void act() {
				final NodeStyleModel styleModel = NodeStyleModel.getModel(node);
				styleModel.setItalic(italic);
				Controller.getCurrentModeController().getMapController().nodeChanged(node);
			}

			@Override
			public String getDescription() {
				return "setItalic";
			}

			@Override
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
			@Override
			public void act() {
				NodeStyleModel.setNodeNumbering(node, enableNodeNumbering);
				final MapController mapController = modeController.getMapController();
				mapController.setSaved(node.getMap(), false);
				mapController.delayedNodeRefresh(node, NodeStyleController.NODE_NUMBERING, oldValue, enableNodeNumbering);
			}

			@Override
			public String getDescription() {
				return "setNodeNumbering";
			}

			@Override
			public void undo() {
				NodeStyleModel.setNodeNumbering(node, oldValue);
				final MapController mapController = modeController.getMapController();
				mapController.setSaved(node.getMap(), false);
				modeController.getMapController().delayedNodeRefresh(node, NodeStyleController.NODE_NUMBERING, enableNodeNumbering, oldValue);
			}
		};
		modeController.execute(actor, node.getMap());
    }

	public void setNodeFormat(final NodeModel node, final String format) {
		final ModeController modeController = Controller.getCurrentModeController();
		final String oldFormat = NodeStyleModel.getNodeFormat(node);
		final IActor actor = new IActor() {
			@Override
			public void act() {
				NodeStyleModel.setNodeFormat(node, format);
				modeController.getMapController().nodeChanged(node);
			}

			@Override
			public String getDescription() {
				return "setNodeFormat";
			}

			@Override
			public void undo() {
				NodeStyleModel.setNodeFormat(node, oldFormat);
				modeController.getMapController().nodeChanged(node);
			}
		};
		modeController.execute(actor, node.getMap());
    }

	public void setShape(final NodeModel node, final String shape) {
		setShape(node, shape == null ? null : Shape.valueOf(shape));
	}

	public void setShape(final NodeModel node, final Shape shape) {
		final ShapeConfigurationModel oldShape = NodeStyleModel.getShapeConfiguration(node);
		setShapeConfiguration(node, oldShape.withShape(shape));
	}

	public void setShapeHorizontalMargin(NodeModel node, Quantity<LengthUnits> margin) {
		final ShapeConfigurationModel oldShape = NodeStyleModel.getShapeConfiguration(node);
		setShapeConfiguration(node, oldShape.withHorizontalMargin(margin));
	}

	public void setShapeVerticalMargin(NodeModel node, Quantity<LengthUnits> margin) {
		final ShapeConfigurationModel oldShape = NodeStyleModel.getShapeConfiguration(node);
		setShapeConfiguration(node, oldShape.withVerticalMargin(margin));
	}

	public void setUniformShape(NodeModel node, boolean uniform) {
		final ShapeConfigurationModel oldShape = NodeStyleModel.getShapeConfiguration(node);
		setShapeConfiguration(node, oldShape.withUniform(uniform));
	}


	public void setShapeConfiguration(final NodeModel node, final ShapeConfigurationModel shape) {
		final ModeController modeController = Controller.getCurrentModeController();
		final ShapeConfigurationModel oldShape = NodeStyleModel.getShapeConfiguration(node);
		final IActor actor = new IActor() {
			@Override
			public void act() {
				NodeStyleModel.setShapeConfiguration(node, shape);
				modeController.getMapController().nodeChanged(node);
				childShapeRefresh(node);
			}

			@Override
			public String getDescription() {
				return "setShape";
			}

			private void childShapeRefresh(final NodeModel node) {
				for (final NodeModel child : node.getChildren()) {
					if(child.getViewers().isEmpty())
						continue;
					final Shape childShape = NodeStyleModel.getShape(child);
					if (childShape == null || NodeStyleModel.Shape.as_parent.equals(childShape)) {
						modeController.getMapController().nodeRefresh(child);
						childShapeRefresh(child);
					}
				}
			}

			@Override
			public void undo() {
				NodeStyleModel.setShapeConfiguration(node, oldShape);
				modeController.getMapController().nodeChanged(node);
				childShapeRefresh(node);
			}
		};
		modeController.execute(actor, node.getMap());
	}
	public void setMinNodeWidth(final NodeModel node, final Quantity<LengthUnits> minNodeWidth) {
		Quantity.assertNonNegativeOrNull(minNodeWidth);
	    final NodeSizeModel sizeModel = createOwnSizeModel(node);
		final Quantity<LengthUnits> oldValue = NodeSizeModel.getMinNodeWidth(node);
		final IActor actor = new IActor() {
			@Override
			public void act() {
				sizeModel.setMinNodeWidth(minNodeWidth);
				final MapController mapController = getModeController().getMapController();
				mapController.nodeChanged(node);
			}

			@Override
			public String getDescription() {
				return "setMinNodeWidth";
			}

			@Override
			public void undo() {
				sizeModel.setMinNodeWidth(oldValue);
				final MapController mapController = getModeController().getMapController();
				mapController.nodeChanged(node);
			}
		};
		getModeController().execute(actor, node.getMap());
		final Quantity<LengthUnits> maxNodeWidth = getMaxWidth(node);
		if(maxNodeWidth != null && minNodeWidth != null && maxNodeWidth.toBaseUnits() < minNodeWidth.toBaseUnits()){
			setMaxNodeWidth(node, minNodeWidth);
		}
    }

	public void setMaxNodeWidth(final NodeModel node, final Quantity<LengthUnits> maxNodeWidth) {
		Quantity.assertNonNegativeOrNull(maxNodeWidth);
	    final NodeSizeModel sizeModel = createOwnSizeModel(node);
		final Quantity<LengthUnits> oldValue = NodeSizeModel.getMaxNodeWidth(node);
		final IActor actor = new IActor() {
			@Override
			public void act() {
				sizeModel.setMaxNodeWidth(maxNodeWidth);
				final MapController mapController = getModeController().getMapController();
				mapController.nodeChanged(node);
			}

			@Override
			public String getDescription() {
				return "setMaxNodeWidth";
			}

			@Override
			public void undo() {
				sizeModel.setMaxNodeWidth(oldValue);
				final MapController mapController = getModeController().getMapController();
				mapController.nodeChanged(node);
			}
		};
		getModeController().execute(actor, node.getMap());
		final Quantity<LengthUnits> minNodeWidth = getMinWidth(node);
		if(maxNodeWidth != null && minNodeWidth != null && maxNodeWidth.toBaseUnitsRounded() < minNodeWidth.toBaseUnitsRounded()){
			setMinNodeWidth(node, maxNodeWidth);
		}
    }


	public void setHorizontalTextAlignment(final NodeModel node, final HorizontalTextAlignment textAlignment) {
		final HorizontalTextAlignment oldTextAlignment = NodeStyleModel.getHorizontalTextAlignment(node);
		final IActor actor = new IActor() {
			@Override
			public void act() {
				NodeStyleModel.setHorizontalTextAlignment(node, textAlignment);
				final MapController mapController = getModeController().getMapController();
				mapController.nodeChanged(node);
			}

			@Override
			public String getDescription() {
				return "setHorizontalTextAlignment";
			}

			@Override
			public void undo() {
				NodeStyleModel.setHorizontalTextAlignment(node, oldTextAlignment);
				final MapController mapController = getModeController().getMapController();
				mapController.nodeChanged(node);
			}
		};
		getModeController().execute(actor, node.getMap());

    }


	public void setBorderWidthMatchesEdgeWidth(final NodeModel node, final Boolean borderWidthMatchesEdgeWidth) {
		final Boolean oldBorderWidthMatchesEdgeWidth = NodeBorderModel.getBorderWidthMatchesEdgeWidth(node);
		final IActor actor = new IActor() {
			@Override
			public void act() {
				NodeBorderModel.setBorderWidthMatchesEdgeWidth(node, borderWidthMatchesEdgeWidth);
				final MapController mapController = getModeController().getMapController();
				mapController.nodeChanged(node);
			}

			@Override
			public String getDescription() {
				return "setBorderWidthMatchesEdgeWidth";
			}

			@Override
			public void undo() {
				NodeBorderModel.setBorderWidthMatchesEdgeWidth(node, oldBorderWidthMatchesEdgeWidth);
				final MapController mapController = getModeController().getMapController();
				mapController.nodeChanged(node);
			}
		};
		getModeController().execute(actor, node.getMap());
    }

	public void setBorderDashMatchesEdgeDash(final NodeModel node, final Boolean borderDashMatchesEdgeDash) {
		final Boolean oldBorderDashMatchesEdgeDash = NodeBorderModel.getBorderDashMatchesEdgeDash(node);
		final IActor actor = new IActor() {
			@Override
			public void act() {
				NodeBorderModel.setBorderDashMatchesEdgeDash(node, borderDashMatchesEdgeDash);
				final MapController mapController = getModeController().getMapController();
				mapController.nodeChanged(node);
			}

			@Override
			public String getDescription() {
				return "setBorderDashMatchesEdgeDash";
			}

			@Override
			public void undo() {
				NodeBorderModel.setBorderDashMatchesEdgeDash(node, oldBorderDashMatchesEdgeDash);
				final MapController mapController = getModeController().getMapController();
				mapController.nodeChanged(node);
			}
		};
		getModeController().execute(actor, node.getMap());
    }


	public void setBorderColorMatchesEdgeColor(final NodeModel node, final Boolean borderColorMatchesEdgeColor) {
		final Boolean oldBorderColorMatchesEdgeColor = NodeBorderModel.getBorderColorMatchesEdgeColor(node);
		final IActor actor = new IActor() {
			@Override
			public void act() {
				NodeBorderModel.setBorderColorMatchesEdgeColor(node, borderColorMatchesEdgeColor);
				final MapController mapController = getModeController().getMapController();
				mapController.nodeChanged(node);
			}

			@Override
			public String getDescription() {
				return "setBorderColorMatchesEdgeColor";
			}

			@Override
			public void undo() {
				NodeBorderModel.setBorderColorMatchesEdgeColor(node, oldBorderColorMatchesEdgeColor);
				final MapController mapController = getModeController().getMapController();
				mapController.nodeChanged(node);
			}
		};
		getModeController().execute(actor, node.getMap());
    }

	public void setBorderWidth(final NodeModel node, final Quantity<LengthUnits> borderWidth) {
		final Quantity<LengthUnits> oldBorderWidth = NodeBorderModel.getBorderWidth(node);
		final IActor actor = new IActor() {
			@Override
			public void act() {
				NodeBorderModel.setBorderWidth(node, borderWidth);
				final MapController mapController = getModeController().getMapController();
				mapController.nodeChanged(node);
			}

			@Override
			public String getDescription() {
				return "setBorderWidth";
			}

			@Override
			public void undo() {
				NodeBorderModel.setBorderWidth(node, oldBorderWidth);
				final MapController mapController = getModeController().getMapController();
				mapController.nodeChanged(node);
			}
		};
		getModeController().execute(actor, node.getMap());

    }

	public void setBorderDash(final NodeModel node, final DashVariant borderDash) {
		final DashVariant oldBorderDash = NodeBorderModel.getBorderDash(node);
		final IActor actor = new IActor() {
			@Override
			public void act() {
				NodeBorderModel.setBorderDash(node, borderDash);
				final MapController mapController = getModeController().getMapController();
				mapController.nodeChanged(node);
			}

			@Override
			public String getDescription() {
				return "setBorderDash";
			}

			@Override
			public void undo() {
				NodeBorderModel.setBorderDash(node, oldBorderDash);
				final MapController mapController = getModeController().getMapController();
				mapController.nodeChanged(node);
			}
		};
		getModeController().execute(actor, node.getMap());

    }

	public void setBorderColor(final NodeModel node, final Color borderColor) {
		final Color oldBorderColor = NodeBorderModel.getBorderColor(node);
		final IActor actor = new IActor() {
			@Override
			public void act() {
				NodeBorderModel.setBorderColor(node, borderColor);
				final MapController mapController = getModeController().getMapController();
				mapController.nodeChanged(node);
			}

			@Override
			public String getDescription() {
				return "setBorderColor";
			}

			@Override
			public void undo() {
				NodeBorderModel.setBorderColor(node, oldBorderColor);
				final MapController mapController = getModeController().getMapController();
				mapController.nodeChanged(node);
			}
		};
		getModeController().execute(actor, node.getMap());

    }

	public void setStrikedThrough(final NodeModel node, final Boolean strikedThrough) {
		final Boolean oldStrikedThrough = NodeStyleModel.isStrikedThrough(node);
		if (oldStrikedThrough == strikedThrough || oldStrikedThrough != null && oldStrikedThrough.equals(strikedThrough)) {
			return;
		}
		createOwnStyleModel(node);
		final ModeController modeController = Controller.getCurrentModeController();
		final IActor actor = new IActor() {
			@Override
			public void act() {
				final NodeStyleModel styleModel = NodeStyleModel.getModel(node);
				styleModel.setStrikedThrough(strikedThrough);
				Controller.getCurrentModeController().getMapController().nodeChanged(node);
			}

			@Override
			public String getDescription() {
				return "setStrikedThrough";
			}

			@Override
			public void undo() {
				final NodeStyleModel styleModel = NodeStyleModel.getModel(node);
				styleModel.setStrikedThrough(oldStrikedThrough);
				Controller.getCurrentModeController().getMapController().nodeChanged(node);
			}
		};
		modeController.execute(actor, node.getMap());
	}
}
