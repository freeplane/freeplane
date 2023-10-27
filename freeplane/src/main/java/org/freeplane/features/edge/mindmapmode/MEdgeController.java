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
package org.freeplane.features.edge.mindmapmode;

import java.awt.Color;
import java.awt.Dimension;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

import org.freeplane.api.Dash;
import org.freeplane.core.ui.components.JRestrictedSizeScrollPane;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.undo.IActor;
import org.freeplane.core.util.ObjectRule;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.edge.EdgeColorConfiguration;
import org.freeplane.features.edge.EdgeController;
import org.freeplane.features.edge.EdgeModel;
import org.freeplane.features.edge.EdgeStyle;
import org.freeplane.features.map.IExtensionCopier;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.styles.LogicalStyleController.StyleOption;
import org.freeplane.features.styles.LogicalStyleKeys;

/**
 * @author Dimitry Polivaev
 */
public class MEdgeController extends EdgeController {
	private static class ExtensionCopier implements IExtensionCopier {
		final private ModeController modeController;

		public ExtensionCopier(ModeController modeController) {
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
			final EdgeModel fromStyle = from.getExtension(EdgeModel.class);
			if (fromStyle == null) {
				return;
			}
			final EdgeModel toStyle = EdgeModel.createEdgeModel(to);
			final Color color = fromStyle.getColor();
			if(color != null)
			    toStyle.setColor(color);
			final EdgeStyle style = fromStyle.getStyle();
			if(style != null)
			    toStyle.setStyle(style);
			final int width = fromStyle.getWidth();
			if(width  != EdgeModel.AUTO_WIDTH)
			    toStyle.setWidth(width);
			final Dash dash = fromStyle.getDash();
			if (dash != null)
				toStyle.setDash(dash);
		}

		@Override
		public void remove(final Object key, final NodeModel from) {
			if (!key.equals(LogicalStyleKeys.NODE_STYLE)) {
				return;
			}
			from.removeExtension(EdgeModel.class);
		}

		@Override
		public void remove(final Object key, final NodeModel from, final NodeModel which) {
			if (!key.equals(LogicalStyleKeys.NODE_STYLE)) {
				return;
			}
			final EdgeModel whichStyle = which.getExtension(EdgeModel.class);
			if (whichStyle == null) {
				return;
			}
			final EdgeModel fromStyle = from.getExtension(EdgeModel.class);
			if (fromStyle == null) {
				return;
			}
			from.removeExtension(fromStyle);
			EdgeModel delta = new EdgeModel();
			final Color color = fromStyle.getColor();
			boolean deltaFound = false;
			if(color != null && whichStyle.getColor() == null){
				delta.setColor(color);
				deltaFound = true;
			}
			final EdgeStyle style = fromStyle.getStyle();
			if(style != null && whichStyle.getStyle() == null){
				delta.setStyle(style);
				deltaFound = true;
			}
			final int width = fromStyle.getWidth();
			if(width  != EdgeModel.AUTO_WIDTH && whichStyle.getWidth() == EdgeModel.AUTO_WIDTH){
				delta.setWidth(width);
				deltaFound = true;
			}
			if(deltaFound)
				from.addExtension(delta);
		}

		@Override
		public void resolveParentExtensions(Object key, NodeModel to) {
			if (!key.equals(LogicalStyleKeys.NODE_STYLE)) {
				return;
			}
			resolveColor(to);
			resolveWidth(to);
			resolveDash(to);
			resolveStyle(to);
        }

		private void resolveColor(NodeModel to) {
	        if (getColorRule(to).hasValue())
				return;
			for(NodeModel source = to.getParentNode(); source != null; source = source.getParentNode() ){
				final ObjectRule<Color, Rules> colorRule = getColorRule(source);
				if(colorRule.hasValue()){
					EdgeModel.createEdgeModel(to).setColor(colorRule.getValue());
					return;
				}
			}
        }

		private ObjectRule<Color, Rules> getColorRule (NodeModel node) {
			return modeController.getExtension(EdgeController.class).getColorRule(node, StyleOption.FOR_UNSELECTED_NODE);
		}

		private void resolveWidth(NodeModel to) {
	        if (null != getWidth(to))
				return;
			for(NodeModel source = to.getParentNode(); source != null; source = source.getParentNode() ){
				final Integer width = getWidth(source);
				if(width != null){
					EdgeModel.createEdgeModel(to).setWidth(width);
					return;
				}
			}
        }

		private Integer getWidth(NodeModel node) {
			return modeController.getExtension(EdgeController.class).getWidth(node, StyleOption.FOR_UNSELECTED_NODE, false);
		}

		private void resolveDash(NodeModel to) {
	        if (null != getDash(to))
				return;
			for(NodeModel source = to.getParentNode(); source != null; source = source.getParentNode() ){
				final Dash dash = getDash(source);
				if(dash != null){
					EdgeModel.createEdgeModel(to).setDash(dash);
					return;
				}
			}
        }

		private Dash getDash(NodeModel node) {
			return modeController.getExtension(EdgeController.class).getDash(node, StyleOption.FOR_UNSELECTED_NODE, false);
		}

		private void resolveStyle(NodeModel to) {
	        if (null != getStyle(to))
				return;
			for(NodeModel source = to.getParentNode(); source != null; source = source.getParentNode() ){
				final EdgeStyle style = getStyle(source);
				if(style != null){
					EdgeModel.createEdgeModel(to).setStyle(style);
					return;
				}
			}
        }
		private EdgeStyle getStyle(NodeModel node) {
			return modeController.getExtension(EdgeController.class).getStyle(node, StyleOption.FOR_UNSELECTED_NODE, false);
		}

	}

	public MEdgeController(final ModeController modeController) {
		super(modeController);
		modeController.registerExtensionCopier(new ExtensionCopier(modeController));
		modeController.addAction(new EdgeColorAction());
		modeController.addAction(new EdgeWidthAction(EdgeModel.PARENT_WIDTH));
		modeController.addAction(new EdgeWidthAction(EdgeModel.THIN_WIDTH));
		modeController.addAction(new EdgeWidthAction(1));
		modeController.addAction(new EdgeWidthAction(2));
		modeController.addAction(new EdgeWidthAction(4));
		modeController.addAction(new EdgeWidthAction(8));
		modeController.addAction(new EdgeStyleAction(EdgeStyle.EDGESTYLE_LINEAR));
		modeController.addAction(new EdgeStyleAction(EdgeStyle.EDGESTYLE_BEZIER));
		modeController.addAction(new EdgeStyleAction(EdgeStyle.EDGESTYLE_SHARP_LINEAR));
		modeController.addAction(new EdgeStyleAction(EdgeStyle.EDGESTYLE_SHARP_BEZIER));
		modeController.addAction(new EdgeStyleAction(EdgeStyle.EDGESTYLE_HORIZONTAL));
		modeController.addAction(new EdgeStyleAction(EdgeStyle.EDGESTYLE_HIDDEN));
		modeController.addAction(new EdgeStyleAsParentAction());
	}

	public void setColor(final NodeModel node, final Color color) {
		final ModeController modeController = Controller.getCurrentModeController();
		final Color oldColor = EdgeModel.createEdgeModel(node).getColor();
		if (color == oldColor || color != null && color.equals(oldColor)) {
			return;
		}
		final IActor actor = new IActor() {
			@Override
			public void act() {
				EdgeModel.createEdgeModel(node).setColor(color);
				modeController.getMapController().nodeChanged(node);
			}

			@Override
			public String getDescription() {
				return "setColor";
			}

			@Override
			public void undo() {
				EdgeModel.createEdgeModel(node).setColor(oldColor);
				modeController.getMapController().nodeChanged(node);
			}
		};
		modeController.execute(actor, node.getMap());
	}

	public void setStyle(final NodeModel node, final EdgeStyle style) {
		final ModeController modeController = Controller.getCurrentModeController();
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
			@Override
			public void act() {
				EdgeModel.createEdgeModel(node).setStyle(style);
				modeController.getMapController().nodeChanged(node);
				edgeStyleRefresh(node);
			}

			private void edgeStyleRefresh(final NodeModel node) {
				for (final NodeModel child : node.getChildren()) {
					if(child.getViewers().isEmpty())
						continue;
					final EdgeModel edge = EdgeModel.getModel(child);
					if (edge == null || edge.getStyle() == null) {
						modeController.getMapController().nodeRefresh(child);
						edgeStyleRefresh(child);
					}
				}
			}

			@Override
			public String getDescription() {
				return "setStyle";
			}

			@Override
			public void undo() {
				EdgeModel.createEdgeModel(node).setStyle(oldStyle);
				modeController.getMapController().nodeChanged(node);
				edgeStyleRefresh(node);
			}
		};
		modeController.execute(actor, node.getMap());
	}

	public void setWidth(final NodeModel node, final int width) {
		final ModeController modeController = Controller.getCurrentModeController();
		final int oldWidth = EdgeModel.createEdgeModel(node).getWidth();
		if (width == oldWidth) {
			return;
		}
		final IActor actor = new IActor() {
			@Override
			public void act() {
				EdgeModel.createEdgeModel(node).setWidth(width);
				modeController.getMapController().nodeChanged(node);
				edgeWidthRefresh(node);
			}

			private void edgeWidthRefresh(final NodeModel node) {
				for (final NodeModel child : node.getChildren()) {
					if(child.getViewers().isEmpty())
						continue;
					final EdgeModel edge = EdgeModel.getModel(child);
					if (edge == null || edge.getWidth() == EdgeModel.PARENT_WIDTH) {
						modeController.getMapController().nodeRefresh(child);
						edgeWidthRefresh(child);
					}
				}
			}

			@Override
			public String getDescription() {
				return "setWidth";
			}

			@Override
			public void undo() {
				EdgeModel.createEdgeModel(node).setWidth(oldWidth);
				modeController.getMapController().nodeChanged(node);
				edgeWidthRefresh(node);
			}
		};
		modeController.execute(actor, node.getMap());
	}

	public void setDash(final NodeModel node, final Dash dash) {
		final ModeController modeController = Controller.getCurrentModeController();
		final Dash oldDash = EdgeModel.createEdgeModel(node).getDash();
		if (dash == oldDash) {
			return;
		}
		final IActor actor = new IActor() {
			@Override
			public void act() {
				EdgeModel.createEdgeModel(node).setDash(dash);
				modeController.getMapController().nodeChanged(node);
				edgeWidthRefresh(node);
			}

			private void edgeWidthRefresh(final NodeModel node) {
				for (final NodeModel child : node.getChildren()) {
					if(child.getViewers().isEmpty())
						continue;
					final EdgeModel edge = EdgeModel.getModel(child);
					if (edge == null || edge.getWidth() == EdgeModel.PARENT_WIDTH) {
						modeController.getMapController().nodeRefresh(child);
						edgeWidthRefresh(child);
					}
				}
			}

			@Override
			public String getDescription() {
				return "setDash";
			}

			@Override
			public void undo() {
				EdgeModel.createEdgeModel(node).setDash(oldDash);
				modeController.getMapController().nodeChanged(node);
				edgeWidthRefresh(node);
			}
		};
		modeController.execute(actor, node.getMap());
	}

	public void editEdgeColorConfiguration(MapModel map) {
		final List<Color> oldColors = edgeColorsConfigurationFactory.create(map).colors;
		final ColorListEditorPanelBuilder colorListEditorPanelBuilder = new ColorListEditorPanelBuilder(oldColors);
		final JComponent panel = colorListEditorPanelBuilder.getPanel();
		JScrollPane jscrollpane = new JRestrictedSizeScrollPane(panel);
		UITools.setScrollbarIncrement(jscrollpane);
		jscrollpane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		jscrollpane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		jscrollpane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 600));
		String title = TextUtils.getText("editEdgeColors");
		final int status = JOptionPane.showConfirmDialog(UITools.getCurrentFrame(), jscrollpane, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		switch(status)
		{
		case JOptionPane.OK_OPTION:
			final List<Color> newColors = colorListEditorPanelBuilder.getColors();
			edgeColorsConfigurationFactory.setConfiguration(map, new EdgeColorConfiguration(newColors));
			break;
		default:
		}
	}
}
