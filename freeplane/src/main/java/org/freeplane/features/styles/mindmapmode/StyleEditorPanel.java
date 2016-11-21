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
package org.freeplane.features.styles.mindmapmode;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.plaf.basic.BasicButtonUI;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.resources.TranslatedObject;
import org.freeplane.core.resources.components.BooleanProperty;
import org.freeplane.core.resources.components.ColorProperty;
import org.freeplane.core.resources.components.ComboProperty;
import org.freeplane.core.resources.components.FontProperty;
import org.freeplane.core.resources.components.IPropertyControl;
import org.freeplane.core.resources.components.NextColumnProperty;
import org.freeplane.core.resources.components.NextLineProperty;
import org.freeplane.core.resources.components.NumberProperty;
import org.freeplane.core.resources.components.QuantityProperty;
import org.freeplane.core.resources.components.SeparatorProperty;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.LengthUnits;
import org.freeplane.core.ui.components.JComboBoxWithBorder;
import org.freeplane.core.ui.components.RenderedContent;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.ui.textchanger.TranslatedElement;
import org.freeplane.core.util.ColorUtils;
import org.freeplane.core.util.HtmlUtils;
import org.freeplane.core.util.Quantity;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.DashVariant;
import org.freeplane.features.cloud.CloudController;
import org.freeplane.features.cloud.CloudModel;
import org.freeplane.features.cloud.mindmapmode.MCloudController;
import org.freeplane.features.edge.AutomaticEdgeColor;
import org.freeplane.features.edge.AutomaticEdgeColorHook;
import org.freeplane.features.edge.EdgeController;
import org.freeplane.features.edge.EdgeModel;
import org.freeplane.features.edge.EdgeStyle;
import org.freeplane.features.edge.mindmapmode.MEdgeController;
import org.freeplane.features.format.FormatController;
import org.freeplane.features.format.IFormattedObject;
import org.freeplane.features.format.PatternFormat;
import org.freeplane.features.icon.IconController;
import org.freeplane.features.icon.mindmapmode.MIconController;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.link.NodeLinks;
import org.freeplane.features.link.mindmapmode.MLinkController;
import org.freeplane.features.map.AMapChangeListenerAdapter;
import org.freeplane.features.map.IMapSelection;
import org.freeplane.features.map.INodeChangeListener;
import org.freeplane.features.map.INodeSelectionListener;
import org.freeplane.features.map.MapChangeEvent;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeChangeEvent;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.nodelocation.LocationController;
import org.freeplane.features.nodelocation.LocationModel;
import org.freeplane.features.nodelocation.mindmapmode.MLocationController;
import org.freeplane.features.nodestyle.NodeBorderModel;
import org.freeplane.features.nodestyle.NodeSizeModel;
import org.freeplane.features.nodestyle.NodeStyleController;
import org.freeplane.features.nodestyle.NodeStyleModel;
import org.freeplane.features.nodestyle.NodeStyleModel.Shape;
import org.freeplane.features.nodestyle.NodeStyleModel.TextAlign;
import org.freeplane.features.nodestyle.ShapeConfigurationModel;
import org.freeplane.features.nodestyle.mindmapmode.MNodeStyleController;
import org.freeplane.features.styles.AutomaticLayout;
import org.freeplane.features.styles.AutomaticLayoutController;
import org.freeplane.features.styles.IStyle;
import org.freeplane.features.styles.LogicalStyleController;
import org.freeplane.features.styles.LogicalStyleModel;
import org.freeplane.features.styles.MapStyle;
import org.freeplane.features.text.TextController;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.FormLayout;

public class StyleEditorPanel extends JPanel {
	private static final int FONT_SIZE = Math.round(UITools.FONT_SCALE_FACTOR * 8);
	private static final TranslatedObject AUTOMATIC_LAYOUT_DISABLED = new TranslatedObject("automatic_layout_disabled");

	private class BgColorChangeListener extends ChangeListener {
		public BgColorChangeListener(final BooleanProperty mSet, final IPropertyControl mProperty) {
			super(mSet, mProperty);
		}

		@Override
		void applyValue(final boolean enabled, final NodeModel node, final PropertyChangeEvent evt) {
			final MNodeStyleController styleController = (MNodeStyleController) Controller
					.getCurrentModeController().getExtension(
							NodeStyleController.class);
			styleController.setBackgroundColor(node, enabled ? mNodeBackgroundColor.getColorValue() : null);
		}
	}

	private class NodeShapeChangeListener extends ChangeListener {
		public NodeShapeChangeListener(final BooleanProperty mSet, final IPropertyControl... mProperty) {
			super(mSet, mProperty);
		}

		@Override
		void applyValue(final boolean enabled, final NodeModel node, final PropertyChangeEvent evt) {
			final MNodeStyleController styleController = (MNodeStyleController) Controller
			.getCurrentModeController().getExtension(
					NodeStyleController.class);
			if(enabled){
				styleController.setShapeConfiguration(node, ShapeConfigurationModel.NULL_SHAPE
						.withShape(NodeStyleModel.Shape.valueOf(mNodeShape.getValue()))
						.withHorizontalMargin(mShapeHorizontalMargin.getQuantifiedValue())
						.withVerticalMargin(mShapeVerticalMargin.getQuantifiedValue())
						.withUniform(mUniformShape.getBooleanValue())
						);
			}
			else
				styleController.setShapeConfiguration(node, ShapeConfigurationModel.NULL_SHAPE);
			final Shape shape = styleController.getShape(node);
			enableShapeConfigurationProperties(enabled, shape);
		}
	}

	private class ColorChangeListener extends ChangeListener {
		public ColorChangeListener(final BooleanProperty mSet, final IPropertyControl mProperty) {
			super(mSet, mProperty);
		}

		@Override
		void applyValue(final boolean enabled, final NodeModel node, final PropertyChangeEvent evt) {
			final MNodeStyleController styleController = (MNodeStyleController) Controller
			.getCurrentModeController().getExtension(
					NodeStyleController.class);
			styleController.setColor(node, enabled ? mNodeColor.getColorValue() : null);
		}
	}

	private class FontBoldChangeListener extends ChangeListener {
		public FontBoldChangeListener(final BooleanProperty mSet, final IPropertyControl mProperty) {
			super(mSet, mProperty);
		}

		@Override
		void applyValue(final boolean enabled, final NodeModel node, final PropertyChangeEvent evt) {
			final MNodeStyleController styleController = (MNodeStyleController) Controller
			.getCurrentModeController().getExtension(
					NodeStyleController.class);
			styleController.setBold(node, enabled ? mNodeFontBold.getBooleanValue() : null);
		}
	}

	private class FontItalicChangeListener extends ChangeListener {
		public FontItalicChangeListener(final BooleanProperty mSet, final IPropertyControl mProperty) {
			super(mSet, mProperty);
		}

		@Override
		void applyValue(final boolean enabled, final NodeModel node, final PropertyChangeEvent evt) {
			final MNodeStyleController styleController = (MNodeStyleController) Controller
			.getCurrentModeController().getExtension(
					NodeStyleController.class);
			styleController.setItalic(node, enabled ? mNodeFontItalic.getBooleanValue() : null);
		}
	}

	private class FontHyperlinkChangeListener extends ChangeListener {
		public FontHyperlinkChangeListener(final BooleanProperty mSet, final IPropertyControl mProperty) {
			super(mSet, mProperty);
		}

		@Override
		void applyValue(final boolean enabled, final NodeModel node, final PropertyChangeEvent evt) {
			final MLinkController styleController = (MLinkController) Controller
			.getCurrentModeController().getExtension(
				LinkController.class);
			styleController.setFormatNodeAsHyperlink(node, enabled ? mNodeFontHyperlink.getBooleanValue() : null);
		}
	}

	private class FontSizeChangeListener extends ChangeListener {
		public FontSizeChangeListener(final BooleanProperty mSet, final IPropertyControl mProperty) {
			super(mSet, mProperty);
		}

		@Override
		void applyValue(final boolean enabled, final NodeModel node, final PropertyChangeEvent evt) {
			final MNodeStyleController styleController = (MNodeStyleController) Controller
			.getCurrentModeController().getExtension(
					NodeStyleController.class);
			try {
	            styleController.setFontSize(node, enabled ? Integer.valueOf(mNodeFontSize.getValue()) : null);
            }
            catch (NumberFormatException e) {
            }
		}
	}

	private class FontNameChangeListener extends ChangeListener {
		public FontNameChangeListener(final BooleanProperty mSet, final IPropertyControl mProperty) {
			super(mSet, mProperty);
		}

		@Override
		void applyValue(final boolean enabled, final NodeModel node, final PropertyChangeEvent evt) {
			final MNodeStyleController styleController = (MNodeStyleController) Controller
			.getCurrentModeController().getExtension(
					NodeStyleController.class);
			styleController.setFontFamily(node, enabled ? mNodeFontName.getValue() : null);
		}
	}

	private class EdgeColorChangeListener extends ChangeListener {
		public EdgeColorChangeListener(final BooleanProperty mSet, final IPropertyControl mProperty) {
			super(mSet, mProperty);
		}

		@Override
		void applyValue(final boolean enabled, final NodeModel node, final PropertyChangeEvent evt) {
			final MEdgeController edgeController = (MEdgeController) MEdgeController.getController();
			edgeController.setColor(node, enabled ? mEdgeColor.getColorValue() : null);
		}
	}

	private class EdgeStyleChangeListener extends ChangeListener {
		public EdgeStyleChangeListener(final BooleanProperty mSet, final IPropertyControl mProperty) {
			super(mSet, mProperty);
		}

		@Override
		void applyValue(final boolean enabled, final NodeModel node,
				final PropertyChangeEvent evt) {
			final MEdgeController styleController = (MEdgeController) Controller
					.getCurrentModeController().getExtension(
							EdgeController.class);
			styleController.setStyle(node, enabled ? EdgeStyle.getStyle(mEdgeStyle.getValue()) : null);
		}
	}
	private class TextAlignmentChangeListener extends ChangeListener {
		public TextAlignmentChangeListener(final BooleanProperty mSet, final IPropertyControl mProperty) {
			super(mSet, mProperty);
		}

		@Override
		void applyValue(final boolean enabled, final NodeModel node,
				final PropertyChangeEvent evt) {
			final MNodeStyleController styleController = (MNodeStyleController) Controller
					.getCurrentModeController().getExtension(NodeStyleController.class);
			styleController.setTextAlign(node, enabled ? TextAlign.valueOf(mNodeTextAlignment.getValue()) : null);
		}
	}
	
	

	private class EdgeWidthChangeListener extends ChangeListener {
		public EdgeWidthChangeListener(final BooleanProperty mSet, final IPropertyControl mProperty) {
			super(mSet, mProperty);
		}

		@Override
		void applyValue(final boolean enabled, final NodeModel node, final PropertyChangeEvent evt) {
			final MEdgeController styleController = (MEdgeController) Controller
			.getCurrentModeController().getExtension(
					EdgeController.class);
			styleController.setWidth(node, enabled ? Integer.parseInt(mEdgeWidth.getValue()): EdgeModel.DEFAULT_WIDTH);
		}
	}

	private class EdgeDashChangeListener extends ChangeListener {
		public EdgeDashChangeListener(final BooleanProperty mSet, final IPropertyControl mProperty) {
			super(mSet, mProperty);
		}

		@Override
		void applyValue(final boolean enabled, final NodeModel node, final PropertyChangeEvent evt) {
			final MEdgeController styleController = (MEdgeController) Controller
			.getCurrentModeController().getExtension(
					EdgeController.class);
			styleController.setDash(node, enabled ? DashVariant.valueOf(mEdgeDash.getValue()): null);
		}
	}

	private class MaxNodeWidthChangeListener extends ChangeListener {
		public MaxNodeWidthChangeListener(final BooleanProperty mSet, final IPropertyControl mProperty) {
			super(mSet, mProperty);
		}

		@Override
		void applyValue(final boolean enabled, final NodeModel node, final PropertyChangeEvent evt) {
			final MNodeStyleController styleController = (MNodeStyleController) Controller
			.getCurrentModeController().getExtension(NodeStyleController.class);
			styleController.setMaxNodeWidth(node, enabled ? mMaxNodeWidth.getQuantifiedValue(): null);
		}
	}

	private class IconSizeChangeListener extends ChangeListener {
		public IconSizeChangeListener(final BooleanProperty mSet, final IPropertyControl mProperty) {
			super(mSet, mProperty);
		}

		@Override
		void applyValue(boolean enabled, NodeModel node, PropertyChangeEvent evt) {
			final MIconController iconController = (MIconController) IconController.getController();
			iconController.changeIconSize(node, enabled ? mIconSize.getQuantifiedValue() : null);
		}
	}

	private class MinNodeWidthChangeListener extends ChangeListener {
		public MinNodeWidthChangeListener(final BooleanProperty mSet, final IPropertyControl mProperty) {
			super(mSet, mProperty);
		}

		@Override
		void applyValue(final boolean enabled, final NodeModel node, final PropertyChangeEvent evt) {
			final MNodeStyleController styleController = (MNodeStyleController) Controller
			.getCurrentModeController().getExtension(NodeStyleController.class);
			styleController.setMinNodeWidth(node, enabled ? mMinNodeWidth.getQuantifiedValue(): null);
		}
	}


	private class BorderWidthMatchesEdgeWidthListener extends ChangeListener {
		public BorderWidthMatchesEdgeWidthListener(final BooleanProperty mSet, final IPropertyControl mProperty) {
			super(mSet, mProperty);
		}

		@Override
		void applyValue(final boolean enabled, final NodeModel node, final PropertyChangeEvent evt) {
			final MNodeStyleController styleController = (MNodeStyleController) Controller
			.getCurrentModeController().getExtension(NodeStyleController.class);
			styleController.setBorderWidthMatchesEdgeWidth(node, enabled ? mBorderWidthMatchesEdgeWidth.getBooleanValue(): null);
		}
	}

	private class BorderWidthListener extends ChangeListener {
		public BorderWidthListener(final BooleanProperty mSet, final IPropertyControl mProperty) {
			super(mSet, mProperty);
		}

		@Override
		void applyValue(final boolean enabled, final NodeModel node, final PropertyChangeEvent evt) {
			final MNodeStyleController styleController = (MNodeStyleController) Controller
			.getCurrentModeController().getExtension(NodeStyleController.class);
			styleController.setBorderWidth(node, enabled ? mBorderWidth.getQuantifiedValue(): null);
		}
	}
	
	private class BorderDashMatchesEdgeDashListener extends ChangeListener {
		public BorderDashMatchesEdgeDashListener(final BooleanProperty mSet, final IPropertyControl mProperty) {
			super(mSet, mProperty);
		}

		@Override
		void applyValue(final boolean enabled, final NodeModel node, final PropertyChangeEvent evt) {
			final MNodeStyleController styleController = (MNodeStyleController) Controller
			.getCurrentModeController().getExtension(NodeStyleController.class);
			styleController.setBorderDashMatchesEdgeDash(node, enabled ? mBorderDashMatchesEdgeDash.getBooleanValue(): null);
		}
	}

	private class BorderDashListener extends ChangeListener {
		public BorderDashListener(final BooleanProperty mSet, final IPropertyControl mProperty) {
			super(mSet, mProperty);
		}

		@Override
		void applyValue(final boolean enabled, final NodeModel node, final PropertyChangeEvent evt) {
			final MNodeStyleController styleController = (MNodeStyleController) Controller
			.getCurrentModeController().getExtension(NodeStyleController.class);
			styleController.setBorderDash(node, enabled ? DashVariant.valueOf(mBorderDash.getValue()): null);
		}
	}
	
	private class BorderColorMatchesEdgeColorListener extends ChangeListener {
		public BorderColorMatchesEdgeColorListener(final BooleanProperty mSet, final IPropertyControl mProperty) {
			super(mSet, mProperty);
		}

		@Override
		void applyValue(final boolean enabled, final NodeModel node, final PropertyChangeEvent evt) {
			final MNodeStyleController styleController = (MNodeStyleController) Controller
			.getCurrentModeController().getExtension(NodeStyleController.class);
			styleController.setBorderColorMatchesEdgeColor(node, enabled ? mBorderColorMatchesEdgeColor.getBooleanValue(): null);
		}
	}

	private class BorderColorListener extends ChangeListener {
		public BorderColorListener(final BooleanProperty mSet, final IPropertyControl mProperty) {
			super(mSet, mProperty);
		}

		@Override
		void applyValue(final boolean enabled, final NodeModel node, final PropertyChangeEvent evt) {
			final MNodeStyleController styleController = (MNodeStyleController) Controller
			.getCurrentModeController().getExtension(NodeStyleController.class);
			styleController.setBorderColor(node, enabled ? mBorderColor.getColorValue(): null);
		}
	}
	
	private class ChildDistanceChangeListener extends ChangeListener {
		public ChildDistanceChangeListener(final BooleanProperty mSet, final IPropertyControl mProperty) {
			super(mSet, mProperty);
		}

		@Override
		void applyValue(final boolean enabled, final NodeModel node, final PropertyChangeEvent evt) {
			final MLocationController locationController = (MLocationController) Controller.getCurrentModeController().getExtension(LocationController.class);
			locationController.setMinimalDistanceBetweenChildren(node, enabled ? mChildDistance.getQuantifiedValue(): LocationModel.DEFAULT_VGAP);
		}
	}
	
	private class CloudColorChangeListener extends ChangeListener {
		public CloudColorChangeListener(final BooleanProperty mSet, final IPropertyControl mProperty) {
			super(mSet, mProperty);
		}

		@Override
		void applyValue(final boolean enabled, final NodeModel node,
				final PropertyChangeEvent evt) {
			final MCloudController styleController = (MCloudController) Controller
					.getCurrentModeController().getExtension(
							CloudController.class);
			if (enabled) {
				styleController.setColor(node, mCloudColor.getColorValue());
			}
			else {
				styleController.setCloud(node, false);
			}
		}
	}

	private class CloudShapeChangeListener extends ChangeListener {
		public CloudShapeChangeListener(final BooleanProperty mSet, final IPropertyControl mProperty) {
			super(mSet, mProperty);
		}

		@Override
		void applyValue(final boolean enabled, final NodeModel node,
				final PropertyChangeEvent evt) {
			final MCloudController styleController = (MCloudController) Controller
					.getCurrentModeController().getExtension(
						CloudController.class);
			if (enabled) {
				styleController.setShape(node, CloudModel.Shape.valueOf(mCloudShape.getValue()));
			}
			else {
				styleController.setCloud(node, false);
			}
		}
	}

	private class NodeNumberingChangeListener extends ChangeListener {
		public NodeNumberingChangeListener(final BooleanProperty mSet, final IPropertyControl mProperty) {
			super(mSet, mProperty);
		}

		@Override
		void applyValue(final boolean enabled, final NodeModel node, final PropertyChangeEvent evt) {
			final MNodeStyleController styleController = (MNodeStyleController) Controller.getCurrentModeController()
			    .getExtension(NodeStyleController.class);
			styleController.setNodeNumbering(node, enabled ? mNodeNumbering.getBooleanValue() : null);
		}
	}
	
	private class NodeFormatChangeListener extends ChangeListener {
		public NodeFormatChangeListener(final BooleanProperty mSet, final IPropertyControl mProperty) {
			super(mSet, mProperty);
		}

		@Override
		void applyValue(final boolean enabled, final NodeModel node, final PropertyChangeEvent evt) {
			final MNodeStyleController styleController = (MNodeStyleController) Controller.getCurrentModeController()
			    .getExtension(NodeStyleController.class);
			styleController.setNodeFormat(node, enabled ? mNodeFormat.getSelectedPattern() : null);
		}
	}

	private class StyleChangeListener implements PropertyChangeListener{

		public StyleChangeListener() {
        }

		public void propertyChange(PropertyChangeEvent evt) {
			if(internalChange){
				return;
			}
			BooleanProperty isSet = (BooleanProperty) evt.getSource();
			final MLogicalStyleController styleController = (MLogicalStyleController) LogicalStyleController.getController();
			if(isSet.getBooleanValue()){
				styleController.setStyle((IStyle) uiFactory.getStyles().getSelectedItem());
			}
			else{
				styleController.setStyle(null);
			}
        }
		
	}
	private abstract class ChangeListener implements PropertyChangeListener {
		final private Collection<IPropertyControl> properties;
		final private BooleanProperty mSet;

		public ChangeListener(final BooleanProperty mSet, final IPropertyControl... properties) {
			super();
			this.mSet = mSet;
			this.properties = Arrays.asList(properties);
		}

		abstract void applyValue(final boolean enabled, NodeModel node, PropertyChangeEvent evt);

		public void propertyChange(final PropertyChangeEvent evt) {
			if (internalChange) {
				return;
			}
			final boolean enabled;
			if (evt.getSource().equals(mSet)) {
				enabled = mSet.getBooleanValue();
			}
			else {
				assert properties.contains(evt.getSource());
				enabled = true;
			}
			final IMapSelection selection = Controller.getCurrentController().getSelection();
			final Collection<NodeModel> nodes = selection.getSelection();
			if (enabled )
				internalChange = true;
			for (final NodeModel node : nodes) {
				applyValue(enabled, node, evt);
			}
			if (enabled  && ! mSet.getBooleanValue())
				mSet.setValue(true);
			internalChange = false;
			setStyle(selection.getSelected());
		}
	}

	private static final String CLOUD_COLOR = "cloudcolor";
	private static final String EDGE_COLOR = "edgecolor";
	private static final String EDGE_STYLE = "edgestyle";
	private static final String CLOUD_SHAPE = "cloudshape";
	private static final String[] EDGE_STYLES = enumStrings(EdgeStyle.class, EdgeStyle.values().length - 1);
	private static final String[] CLOUD_SHAPES = StyleEditorPanel.enumStrings(CloudModel.Shape.class);
	private static final String EDGE_WIDTH = "edgewidth";
	private static final String EDGE_DASH = "edgedash";
//	private static final String ICON = "icon";
	private static final String NODE_BACKGROUND_COLOR = "nodebackgroundcolor";
	private static final String NODE_COLOR = "nodecolor";
	private static final String NODE_FONT_BOLD = "nodefontbold";
	private static final String NODE_FONT_ITALIC = "nodefontitalic";
	private static final String NODE_FONT_HYPERLINK = "nodefonthyperlink";
	private static final String NODE_FONT_NAME = "nodefontname";
	private static final String NODE_FONT_SIZE = "nodefontsize";
	private static final String NODE_NUMBERING = "nodenumbering";
	private static final String NODE_SHAPE = "nodeshape";
	private static final String NODE_TEXT_COLOR = "standardnodetextcolor";
	private static final String NODE_FORMAT = "nodeformat";
	private static final String TEXT_ALIGNMENT = "textalignment";
	private static final String[] TEXT_ALIGNMENTS = StyleEditorPanel.enumStrings(TextAlign.class);
	/**
	* 
	*/
	private static final long serialVersionUID = 1L;
	private static final String SET_RESOURCE = "set_property_text";
	private static final String MAX_TEXT_WIDTH = "max_node_width";
	private static final String MIN_NODE_WIDTH = "min_node_width";
	private static final String VERTICAL_CHILD_GAP = "vertical_child_gap";
	private static final String SHAPE_HORIZONTAL_MARGIN = "shape_horizontal_margin";
	private static final String SHAPE_VERTICAL_MARGIN = "shape_vertical_margin";
	private static final String UNIFORM_SHAPE = "uniform_shape";
	private static final String BORDER_WIDTH_MATCHES_EDGE_WIDTH = "border_width_matches_edge_width";
	private static final String BORDER_WIDTH = "border_width";
	private static final String BORDER_DASH_MATCHES_EDGE_DASH = "border_dash_matches_edge_dash";
	private static final String BORDER_DASH = "border_dash";
	private static final String BORDER_COLOR_MATCHES_EDGE_COLOR = "border_color_matches_edge_color";
	private static final String BORDER_COLOR = "border_color";
	
	
	private static final String ICON_SIZE = "icon_size";


	private  static <U extends Enum<U>> String[] enumStrings(Class<U> enumerationClass, int length) {
		final U[] enumConstants = enumerationClass.getEnumConstants();
		final String[] strings = new String[length];
		for (int i = 0; i < length; i++) {
			strings[i] = enumConstants[i].toString();
		}
		return strings;
	}

	private  static <U extends Enum<U>> String[] enumStrings(Class<U> enumerationClass) {
		return enumStrings(enumerationClass, enumerationClass.getEnumConstants().length);
	}

	private boolean internalChange;
	private List<IPropertyControl> mControls;
	
	private BooleanProperty mSetCloud;
	private ColorProperty mCloudColor;
	private ComboProperty mCloudShape;

	private BooleanProperty mSetEdgeColor;
	private ColorProperty mEdgeColor;

	private BooleanProperty mSetEdgeStyle;
	private ComboProperty mEdgeStyle;
	
	private BooleanProperty mSetEdgeWidth;
	private NumberProperty mEdgeWidth;

	private BooleanProperty mSetEdgeDash;
	private ComboProperty mEdgeDash;

	private BooleanProperty mSetNodeBackgroundColor;
	private ColorProperty mNodeBackgroundColor;

	private BooleanProperty mSetNodeColor;
	private ColorProperty mNodeColor;

	private BooleanProperty mSetNodeFontBold;
	private BooleanProperty mNodeFontBold;

	private BooleanProperty mSetNodeFontItalic;
	private BooleanProperty mNodeFontItalic;

	private BooleanProperty mSetNodeFontHyperlink;
	private BooleanProperty mNodeFontHyperlink;

	private BooleanProperty mSetNodeFontName;
	private FontProperty mNodeFontName;

	private BooleanProperty mSetNodeFontSize;
	private ComboProperty mNodeFontSize;

	private BooleanProperty mSetNodeNumbering;
	private BooleanProperty mNodeNumbering;

	private BooleanProperty mSetNodeShape;
	private ComboProperty mNodeShape;
	
	private BooleanProperty mSetBorderWidthMatchesEdgeWidth;
	private BooleanProperty mBorderWidthMatchesEdgeWidth;
	
	private BooleanProperty mSetBorderWidth;
	private QuantityProperty<LengthUnits> mBorderWidth;

	private BooleanProperty mSetBorderDashMatchesEdgeDash;
	private BooleanProperty mBorderDashMatchesEdgeDash;
	
	private BooleanProperty mSetBorderDash;
	private ComboProperty mBorderDash;

	private BooleanProperty mSetBorderColorMatchesEdgeColor;
	private BooleanProperty mBorderColorMatchesEdgeColor;
	
	private BooleanProperty mSetBorderColor;
	private ColorProperty mBorderColor;

	private QuantityProperty<LengthUnits> mShapeHorizontalMargin;
	private QuantityProperty<LengthUnits> mShapeVerticalMargin;
	private BooleanProperty mUniformShape;

	private BooleanProperty mSetNodeFormat;
	private EditablePatternComboProperty mNodeFormat;
	
	private BooleanProperty mSetMaxNodeWidth;
	private QuantityProperty<LengthUnits> mMaxNodeWidth;

	private BooleanProperty mSetMinNodeWidth;
	private QuantityProperty<LengthUnits> mMinNodeWidth;

	private BooleanProperty mSetChildDistance;
	private QuantityProperty<LengthUnits> mChildDistance;

	private BooleanProperty mSetNodeTextAlignment;
	private ComboProperty mNodeTextAlignment;
	private QuantityProperty<LengthUnits> mIconSize;
	
	private BooleanProperty mSetStyle;
	private BooleanProperty mSetIconSize;
	
	private final boolean addStyleBox;
	private final MUIFactory uiFactory;
	private final ModeController modeController;
	private JButton mNodeStyleButton;
	private JButton mMapStyleButton;

	/**
	 * @throws HeadlessException
	 */
	public StyleEditorPanel(final ModeController modeController, final MUIFactory uiFactory,
	                        final boolean addStyleBox) throws HeadlessException {
		super();
		this.modeController = modeController;
		this.addStyleBox = addStyleBox;
		this.uiFactory = uiFactory;
		addHierarchyListener(new HierarchyListener() {
			
			public void hierarchyChanged(HierarchyEvent e) {
				if(isDisplayable()){
					removeHierarchyListener(this);
					init();
				}
			}
		});
	}

	private void addBgColorControl(final List<IPropertyControl> controls) {
		mSetNodeBackgroundColor = new BooleanProperty(StyleEditorPanel.SET_RESOURCE);
		controls.add(mSetNodeBackgroundColor);
		mNodeBackgroundColor = new ColorProperty(StyleEditorPanel.NODE_BACKGROUND_COLOR, ResourceController
		    .getResourceController().getDefaultProperty(NODE_BACKGROUND_COLOR));
		controls.add(mNodeBackgroundColor);
		final BgColorChangeListener listener = new BgColorChangeListener(mSetNodeBackgroundColor, mNodeBackgroundColor);
		mSetNodeBackgroundColor.addPropertyChangeListener(listener);
		mNodeBackgroundColor.addPropertyChangeListener(listener);
	}

    private void addFormatControl(final List<IPropertyControl> controls) {
        mSetNodeFormat = new BooleanProperty(StyleEditorPanel.SET_RESOURCE);
        controls.add(mSetNodeFormat);
        mNodeFormat = new EditablePatternComboProperty(StyleEditorPanel.NODE_FORMAT,
            PatternFormat.getIdentityPatternFormat(), FormatController.getController().getAllFormats());
        controls.add(mNodeFormat);
        final NodeFormatChangeListener listener = new NodeFormatChangeListener(mSetNodeFormat, mNodeFormat);
        mSetNodeFormat.addPropertyChangeListener(listener);
        mNodeFormat.addPropertyChangeListener(listener);
    }
	
	private void addNodeNumberingControl(final List<IPropertyControl> controls) {
		mSetNodeNumbering = new BooleanProperty(StyleEditorPanel.SET_RESOURCE);
		controls.add(mSetNodeNumbering);
		mNodeNumbering = new BooleanProperty(StyleEditorPanel.NODE_NUMBERING);
		controls.add(mNodeNumbering);
		final NodeNumberingChangeListener listener = new NodeNumberingChangeListener(mSetNodeNumbering, mNodeNumbering);
		mSetNodeNumbering.addPropertyChangeListener(listener);
		mNodeNumbering.addPropertyChangeListener(listener);
	}

	private void addCloudColorControl(final List<IPropertyControl> controls) {
		mSetCloud = new BooleanProperty(StyleEditorPanel.SET_RESOURCE);
		controls.add(mSetCloud);
		mCloudColor = new ColorProperty(StyleEditorPanel.CLOUD_COLOR, ResourceController.getResourceController()
		    .getDefaultProperty(CloudController.RESOURCES_CLOUD_COLOR));
		controls.add(mCloudColor);
		final CloudColorChangeListener listener = new CloudColorChangeListener(mSetCloud, mCloudColor);
		mSetCloud.addPropertyChangeListener(listener);
		mCloudColor.addPropertyChangeListener(listener);
	}

	private void addColorControl(final List<IPropertyControl> controls) {
		mSetNodeColor = new BooleanProperty(StyleEditorPanel.SET_RESOURCE);
		controls.add(mSetNodeColor);
		mNodeColor = new ColorProperty(StyleEditorPanel.NODE_COLOR, ResourceController.getResourceController()
		    .getDefaultProperty(NODE_TEXT_COLOR));
		controls.add(mNodeColor);
		final ColorChangeListener listener = new ColorChangeListener(mSetNodeColor, mNodeColor);
		mSetNodeColor.addPropertyChangeListener(listener);
		mNodeColor.addPropertyChangeListener(listener);
	}

	private void addEdgeColorControl(final List<IPropertyControl> controls) {
		mSetEdgeColor = new BooleanProperty(StyleEditorPanel.SET_RESOURCE);
		controls.add(mSetEdgeColor);
		mEdgeColor = new ColorProperty(StyleEditorPanel.EDGE_COLOR, ColorUtils.colorToString(EdgeController.STANDARD_EDGE_COLOR));
		controls.add(mEdgeColor);
		final EdgeColorChangeListener listener = new EdgeColorChangeListener(mSetEdgeColor, mEdgeColor);
		mSetEdgeColor.addPropertyChangeListener(listener);
		mEdgeColor.addPropertyChangeListener(listener);
	}

	private void addEdgeStyleControl(final List<IPropertyControl> controls) {
		mSetEdgeStyle = new BooleanProperty(StyleEditorPanel.SET_RESOURCE);
		controls.add(mSetEdgeStyle);
		mEdgeStyle = new ComboProperty(StyleEditorPanel.EDGE_STYLE, EDGE_STYLES);
		controls.add(mEdgeStyle);
		final EdgeStyleChangeListener listener = new EdgeStyleChangeListener(mSetEdgeStyle, mEdgeStyle);
		mSetEdgeStyle.addPropertyChangeListener(listener);
		mEdgeStyle.addPropertyChangeListener(listener);
	}

	private void addCloudShapeControl(final List<IPropertyControl> controls) {
		mCloudShape = new ComboProperty(StyleEditorPanel.CLOUD_SHAPE, CLOUD_SHAPES);
		controls.add(mCloudShape);
		final CloudShapeChangeListener listener = new CloudShapeChangeListener(mSetCloud, mCloudShape);
		mSetCloud.addPropertyChangeListener(listener);
		mCloudShape.addPropertyChangeListener(listener);
	}

	private void addEdgeWidthControl(final List<IPropertyControl> controls) {
		mSetEdgeWidth = new BooleanProperty(StyleEditorPanel.SET_RESOURCE);
		controls.add(mSetEdgeWidth);
		mEdgeWidth = new NumberProperty(StyleEditorPanel.EDGE_WIDTH, 0, 100, 1);
		controls.add(mEdgeWidth);
		final EdgeWidthChangeListener listener = new EdgeWidthChangeListener(mSetEdgeWidth, mEdgeWidth);
		mSetEdgeWidth.addPropertyChangeListener(listener);
		mEdgeWidth.addPropertyChangeListener(listener);
	}

	private void addEdgeDashControl(final List<IPropertyControl> controls) {
		mSetEdgeDash = new BooleanProperty(StyleEditorPanel.SET_RESOURCE);
		controls.add(mSetEdgeDash);
		mEdgeDash = ComboProperty.of(EDGE_DASH, DashVariant.class);
		controls.add(mEdgeDash);
		final EdgeDashChangeListener listener = new EdgeDashChangeListener(mSetEdgeDash, mEdgeDash);
		mSetEdgeDash.addPropertyChangeListener(listener);
		mEdgeDash.addPropertyChangeListener(listener);
	}

	private void addMaxNodeWidthControl(final List<IPropertyControl> controls) {
		mSetMaxNodeWidth = new BooleanProperty(StyleEditorPanel.SET_RESOURCE);
		controls.add(mSetMaxNodeWidth);
		mMaxNodeWidth = new QuantityProperty<LengthUnits>(StyleEditorPanel.MAX_TEXT_WIDTH, 0, 100000, 0.1, LengthUnits.px);
		controls.add(mMaxNodeWidth);
		final MaxNodeWidthChangeListener listener = new MaxNodeWidthChangeListener(mSetMaxNodeWidth, mMaxNodeWidth);
		mSetMaxNodeWidth.addPropertyChangeListener(listener);
		mMaxNodeWidth.addPropertyChangeListener(listener);
	}

	private void addMinNodeWidthControl(final List<IPropertyControl> controls) {
		mSetMinNodeWidth = new BooleanProperty(StyleEditorPanel.SET_RESOURCE);
		controls.add(mSetMinNodeWidth);
		mMinNodeWidth = new QuantityProperty<LengthUnits>(StyleEditorPanel.MIN_NODE_WIDTH, 0, 100000, 0.1, LengthUnits.px);
		controls.add(mMinNodeWidth);
		final MinNodeWidthChangeListener listener = new MinNodeWidthChangeListener(mSetMinNodeWidth, mMinNodeWidth);
		mSetMinNodeWidth.addPropertyChangeListener(listener);
		mMinNodeWidth.addPropertyChangeListener(listener);
	}

	private void addIconSizeControl(final List<IPropertyControl> controls) {
		mSetIconSize = new BooleanProperty(StyleEditorPanel.SET_RESOURCE);
		controls.add(mSetIconSize);
		mIconSize = new QuantityProperty<LengthUnits>(StyleEditorPanel.ICON_SIZE, 0, 256, 4, LengthUnits.px);
		controls.add(mIconSize);
		final IconSizeChangeListener listener = new IconSizeChangeListener(mSetIconSize, mIconSize);
		mSetIconSize.addPropertyChangeListener(listener);
		mIconSize.addPropertyChangeListener(listener);
	}

	private void addBorderWidthControl(final List<IPropertyControl> controls) {
		mSetBorderWidth = new BooleanProperty(StyleEditorPanel.SET_RESOURCE);
		controls.add(mSetBorderWidth);
		mBorderWidth = new QuantityProperty<LengthUnits>(StyleEditorPanel.BORDER_WIDTH, 0, 100000, 0.1, LengthUnits.px);
		controls.add(mBorderWidth);
		final BorderWidthListener listener = new BorderWidthListener(mSetBorderWidth, mBorderWidth);
		mSetBorderWidth.addPropertyChangeListener(listener);
		mBorderWidth.addPropertyChangeListener(listener);
	}
	
	private void addBorderWidthMatchesEdgeWidthControl(final List<IPropertyControl> controls) {
		mSetBorderWidthMatchesEdgeWidth = new BooleanProperty(StyleEditorPanel.SET_RESOURCE);
		controls.add(mSetBorderWidthMatchesEdgeWidth);
		mBorderWidthMatchesEdgeWidth = new BooleanProperty(StyleEditorPanel.BORDER_WIDTH_MATCHES_EDGE_WIDTH);
		controls.add(mBorderWidthMatchesEdgeWidth);
		final BorderWidthMatchesEdgeWidthListener listener = new BorderWidthMatchesEdgeWidthListener(mSetBorderWidthMatchesEdgeWidth, mBorderWidthMatchesEdgeWidth);
		mSetBorderWidthMatchesEdgeWidth.addPropertyChangeListener(listener);
		mBorderWidthMatchesEdgeWidth.addPropertyChangeListener(listener);
	}

	private void addBorderDashControl(final List<IPropertyControl> controls) {
		mSetBorderDash = new BooleanProperty(StyleEditorPanel.SET_RESOURCE);
		controls.add(mSetBorderDash);
		mBorderDash = ComboProperty.of(StyleEditorPanel.BORDER_DASH, DashVariant.class);
		controls.add(mBorderDash);
		final BorderDashListener listener = new BorderDashListener(mSetBorderDash, mBorderDash);
		mSetBorderDash.addPropertyChangeListener(listener);
		mBorderDash.addPropertyChangeListener(listener);
	}
	
	private void addBorderDashMatchesEdgeDashControl(final List<IPropertyControl> controls) {
		mSetBorderDashMatchesEdgeDash = new BooleanProperty(StyleEditorPanel.SET_RESOURCE);
		controls.add(mSetBorderDashMatchesEdgeDash);
		mBorderDashMatchesEdgeDash = new BooleanProperty(StyleEditorPanel.BORDER_DASH_MATCHES_EDGE_DASH);
		controls.add(mBorderDashMatchesEdgeDash);
		final BorderDashMatchesEdgeDashListener listener = new BorderDashMatchesEdgeDashListener(mSetBorderDashMatchesEdgeDash, mBorderDashMatchesEdgeDash);
		mSetBorderDashMatchesEdgeDash.addPropertyChangeListener(listener);
		mBorderDashMatchesEdgeDash.addPropertyChangeListener(listener);
	}

	private void addBorderColorControl(final List<IPropertyControl> controls) {
		mSetBorderColor = new BooleanProperty(StyleEditorPanel.SET_RESOURCE);
		controls.add(mSetBorderColor);
		mBorderColor = new ColorProperty(StyleEditorPanel.BORDER_COLOR, ColorUtils.colorToString(EdgeController.STANDARD_EDGE_COLOR));
		controls.add(mBorderColor);
		final BorderColorListener listener = new BorderColorListener(mSetBorderColor, mBorderColor);
		mSetBorderColor.addPropertyChangeListener(listener);
		mBorderColor.addPropertyChangeListener(listener);
	}
	
	private void addBorderColorMatchesEdgeColorControl(final List<IPropertyControl> controls) {
		mSetBorderColorMatchesEdgeColor = new BooleanProperty(StyleEditorPanel.SET_RESOURCE);
		controls.add(mSetBorderColorMatchesEdgeColor);
		mBorderColorMatchesEdgeColor = new BooleanProperty(StyleEditorPanel.BORDER_COLOR_MATCHES_EDGE_COLOR);
		controls.add(mBorderColorMatchesEdgeColor);
		final BorderColorMatchesEdgeColorListener listener = new BorderColorMatchesEdgeColorListener(mSetBorderColorMatchesEdgeColor, mBorderColorMatchesEdgeColor);
		mSetBorderColorMatchesEdgeColor.addPropertyChangeListener(listener);
		mBorderColorMatchesEdgeColor.addPropertyChangeListener(listener);
	}

	private void addChildDistanceControl(final List<IPropertyControl> controls) {
		mSetChildDistance = new BooleanProperty(StyleEditorPanel.SET_RESOURCE);
		controls.add(mSetChildDistance);
		mChildDistance = new  QuantityProperty<LengthUnits>(StyleEditorPanel.VERTICAL_CHILD_GAP, 0, 1000, 0.1, LengthUnits.px);
		controls.add(mChildDistance);
		final ChildDistanceChangeListener listener = new ChildDistanceChangeListener(mSetChildDistance, mChildDistance);
		mSetChildDistance.addPropertyChangeListener(listener);
		mChildDistance.addPropertyChangeListener(listener);
	}

	private void addFontBoldControl(final List<IPropertyControl> controls) {
		mSetNodeFontBold = new BooleanProperty(StyleEditorPanel.SET_RESOURCE);
		controls.add(mSetNodeFontBold);
		mNodeFontBold = new BooleanProperty(StyleEditorPanel.NODE_FONT_BOLD);
		controls.add(mNodeFontBold);
		final FontBoldChangeListener listener = new FontBoldChangeListener(mSetNodeFontBold, mNodeFontBold);
		mSetNodeFontBold.addPropertyChangeListener(listener);
		mNodeFontBold.addPropertyChangeListener(listener);
	}

	private void addFontItalicControl(final List<IPropertyControl> controls) {
		mSetNodeFontItalic = new BooleanProperty(StyleEditorPanel.SET_RESOURCE);
		controls.add(mSetNodeFontItalic);
		mNodeFontItalic = new BooleanProperty(StyleEditorPanel.NODE_FONT_ITALIC);
		controls.add(mNodeFontItalic);
		final FontItalicChangeListener listener = new FontItalicChangeListener(mSetNodeFontItalic, mNodeFontItalic);
		mSetNodeFontItalic.addPropertyChangeListener(listener);
		mNodeFontItalic.addPropertyChangeListener(listener);
	}

	private void addFontHyperlinkControl(final List<IPropertyControl> controls) {
		mSetNodeFontHyperlink = new BooleanProperty(StyleEditorPanel.SET_RESOURCE);
		controls.add(mSetNodeFontHyperlink);
		mNodeFontHyperlink = new BooleanProperty(StyleEditorPanel.NODE_FONT_HYPERLINK);
		controls.add(mNodeFontHyperlink);
		final FontHyperlinkChangeListener listener = new FontHyperlinkChangeListener(mSetNodeFontHyperlink, mNodeFontHyperlink);
		mSetNodeFontHyperlink.addPropertyChangeListener(listener);
		mNodeFontHyperlink.addPropertyChangeListener(listener);
	}

	private void addFontNameControl(final List<IPropertyControl> controls) {
		mSetNodeFontName = new BooleanProperty(StyleEditorPanel.SET_RESOURCE);
		controls.add(mSetNodeFontName);
		mNodeFontName = new FontProperty(StyleEditorPanel.NODE_FONT_NAME);
		controls.add(mNodeFontName);
		final FontNameChangeListener listener = new FontNameChangeListener(mSetNodeFontName, mNodeFontName);
		mSetNodeFontName.addPropertyChangeListener(listener);
		mNodeFontName.addPropertyChangeListener(listener);
	}

	private void addFontSizeControl(final List<IPropertyControl> controls) {
		mSetNodeFontSize = new BooleanProperty(StyleEditorPanel.SET_RESOURCE);
		controls.add(mSetNodeFontSize);
		final List<String> sizesVector = new ArrayList<String>(Arrays.asList(MUIFactory.FONT_SIZES));
		mNodeFontSize = new ComboProperty(StyleEditorPanel.NODE_FONT_SIZE, sizesVector, sizesVector);
		mNodeFontSize.setEditable(true);
		controls.add(mNodeFontSize);
		final FontSizeChangeListener listener = new FontSizeChangeListener(mSetNodeFontSize, mNodeFontSize);
		mSetNodeFontSize.addPropertyChangeListener(listener);
		mNodeFontSize.addPropertyChangeListener(listener);
	}

	private void addNodeShapeControls(final List<IPropertyControl> controls) {
		mSetNodeShape = new BooleanProperty(StyleEditorPanel.SET_RESOURCE);
		controls.add(mSetNodeShape);
		mNodeShape = new ComboProperty(StyleEditorPanel.NODE_SHAPE, enumStrings(NodeStyleModel.Shape.class));
		controls.add(mNodeShape);
		controls.add(new NextColumnProperty(2));
		mShapeHorizontalMargin = new QuantityProperty<LengthUnits>(StyleEditorPanel.SHAPE_HORIZONTAL_MARGIN, 0, 1000, 0.1, LengthUnits.pt);
		controls.add(mShapeHorizontalMargin);
		controls.add(new NextColumnProperty(2));
		mShapeVerticalMargin = new QuantityProperty<LengthUnits>(StyleEditorPanel.SHAPE_VERTICAL_MARGIN, 0, 1000, 0.1, LengthUnits.pt);
		controls.add(mShapeVerticalMargin);
		controls.add(new NextColumnProperty(2));
		mUniformShape = new BooleanProperty(StyleEditorPanel.UNIFORM_SHAPE);
		controls.add(mUniformShape);
		final NodeShapeChangeListener listener = new NodeShapeChangeListener(mSetNodeShape, mNodeShape, mShapeHorizontalMargin, mShapeVerticalMargin, mUniformShape);
		mSetNodeShape.addPropertyChangeListener(listener);
		mNodeShape.addPropertyChangeListener(listener);
		mShapeHorizontalMargin.addPropertyChangeListener(listener);
		mShapeVerticalMargin.addPropertyChangeListener(listener);
		mUniformShape.addPropertyChangeListener(listener);
	}

	private void addNodeTextAlignmentControl(final List<IPropertyControl> controls) {
		mSetNodeTextAlignment = new BooleanProperty(StyleEditorPanel.SET_RESOURCE);
		controls.add(mSetNodeTextAlignment);
		final Vector<String> possibleTranslations = new Vector<String>(TEXT_ALIGNMENTS.length);
		for (int i = 0; i < TEXT_ALIGNMENTS.length; i++) {
			possibleTranslations.add(TextUtils.getText("TextAlignAction." + TEXT_ALIGNMENTS[i] + ".text"));
		}
		Vector<String> translations = possibleTranslations;
		mNodeTextAlignment = new ComboProperty(StyleEditorPanel.TEXT_ALIGNMENT, Arrays.asList(TEXT_ALIGNMENTS), translations);
		controls.add(mNodeTextAlignment);
		final TextAlignmentChangeListener listener = new TextAlignmentChangeListener(mSetNodeTextAlignment, mNodeTextAlignment);
		mSetNodeTextAlignment.addPropertyChangeListener(listener);
		mNodeTextAlignment.addPropertyChangeListener(listener);
	}

	private List<IPropertyControl> getControls() {
		final List<IPropertyControl> controls = new ArrayList<IPropertyControl>();
		controls.add(new SeparatorProperty("OptionPanel.separator.NodeColors"));
		addColorControl(controls);
		addBgColorControl(controls);
		controls.add(new SeparatorProperty("OptionPanel.separator.NodeText"));
		addFormatControl(controls);
		addNodeNumberingControl(controls);
		controls.add(new SeparatorProperty("OptionPanel.separator.NodeShape"));
		addNodeShapeControls(controls);
		addMinNodeWidthControl(controls);
		addMaxNodeWidthControl(controls);
		addChildDistanceControl(controls);
		controls.add(new SeparatorProperty("OptionPanel.separator.NodeBorder"));
		addBorderWidthMatchesEdgeWidthControl(controls);
		addBorderWidthControl(controls);
		mBorderWidthMatchesEdgeWidth.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				enableOrDisableBorderWidthControls();
			}
		});
		
		addBorderDashMatchesEdgeDashControl(controls);
		addBorderDashControl(controls);
		mBorderDashMatchesEdgeDash.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				enableOrDisableBorderDashControls();
			}
		});
		
		addBorderColorMatchesEdgeColorControl(controls);
		addBorderColorControl(controls);
		mBorderColorMatchesEdgeColor.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				enableOrDisableBorderColorControls();
			}
		});
		
		controls.add(new NextLineProperty());
		controls.add(new SeparatorProperty("OptionPanel.separator.NodeFont"));
		addFontNameControl(controls);
		addFontSizeControl(controls);
		addFontBoldControl(controls);
		addFontItalicControl(controls);
		addNodeTextAlignmentControl(controls);
		addFontHyperlinkControl(controls);
		controls.add(new NextLineProperty());
		controls.add(new SeparatorProperty("OptionPanel.separator.EdgeControls"));
		addEdgeWidthControl(controls);
		addEdgeDashControl(controls);
		addEdgeStyleControl(controls);
		addEdgeColorControl(controls);
		controls.add(new NextLineProperty());
		controls.add(new SeparatorProperty("OptionPanel.separator.CloudControls"));
		addCloudColorControl(controls);
		controls.add(new NextLineProperty());
		controls.add(new NextColumnProperty(2));
		addCloudShapeControl(controls);
		controls.add(new SeparatorProperty("OptionPanel.separator.IconControls"));
		addIconSizeControl(controls);
		return controls;
	}
	
	
	/**
	 * Creates all controls and adds them to the frame.
	 * @param modeController 
	 */
	private void init() {
		if(mControls != null)
			return;
		final String form = "right:max(20dlu;p), 2dlu, p, 1dlu,right:max(20dlu;p), 4dlu, 80dlu, 7dlu";
		final FormLayout rightLayout = new FormLayout(form, "");
		final DefaultFormBuilder rightBuilder = new DefaultFormBuilder(rightLayout);
		rightBuilder.border(Borders.DLU2);
		new SeparatorProperty("OptionPanel.separator.NodeStyle").layout(rightBuilder);
		if (addStyleBox) {
			addAutomaticLayout(rightBuilder);
			addStyleBox(rightBuilder);
		}
		mNodeStyleButton = addStyleButton(rightBuilder, "actual_node_styles", modeController.getAction(ManageNodeConditionalStylesAction.NAME));
		if (addStyleBox) {
			mMapStyleButton = addStyleButton(rightBuilder, "actual_map_styles", modeController.getAction(ManageMapConditionalStylesAction.NAME));
		}
		mControls = getControls();
		for (final IPropertyControl control : mControls) {
			control.layout(rightBuilder);
		}
		add(rightBuilder.getPanel(), BorderLayout.CENTER);
		addListeners();
		setFont(this, FONT_SIZE);
	}

	private JButton addStyleButton(DefaultFormBuilder rightBuilder, String label, AFreeplaneAction action) {
	    final JButton button = new JButton(){
			private static final long serialVersionUID = 1L;
			{
				setUI(BasicButtonUI.createUI(this));
				
			}
		};
	    button.addActionListener(action);
	    button.setHorizontalAlignment(SwingConstants.LEFT);
	    final String labelText = TextUtils.getText(label);
	    UITools.addTitledBorder(button, labelText, FONT_SIZE);
		TranslatedElement.BORDER.setKey(button, label);
	    rightBuilder.append(button, rightBuilder.getColumnCount());
		rightBuilder.nextLine();
		return button;
    }

	private void addStyleBox(final DefaultFormBuilder rightBuilder) {
	    mStyleBox = uiFactory.createStyleBox();
	    mSetStyle = new BooleanProperty(StyleEditorPanel.SET_RESOURCE);
		final StyleChangeListener listener = new StyleChangeListener();
		mSetStyle.addPropertyChangeListener(listener);
		mSetStyle.layout(rightBuilder);
	    rightBuilder.append(new JLabel(TextUtils.getText("style")));
	    rightBuilder.append(mStyleBox);
	    rightBuilder.nextLine();
    }

	private JComboBox mAutomaticLayoutComboBox;
	private JComboBox mAutomaticEdgeColorComboBox;
	private Container mStyleBox;
	private void addAutomaticLayout(final DefaultFormBuilder rightBuilder) {
		{
		if(mAutomaticLayoutComboBox == null){
			 TranslatedObject[] automaticLayoutTypes = TranslatedObject.fromEnum(AutomaticLayout.class);
			 mAutomaticLayoutComboBox = new JComboBoxWithBorder(automaticLayoutTypes);
			 DefaultComboBoxModel automaticLayoutComboBoxModel = (DefaultComboBoxModel) mAutomaticLayoutComboBox.getModel();
			 automaticLayoutComboBoxModel.addElement(AUTOMATIC_LAYOUT_DISABLED);
			 automaticLayoutComboBoxModel.setSelectedItem(AUTOMATIC_LAYOUT_DISABLED);
			 mAutomaticLayoutComboBox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(internalChange)
						return;
					final ModeController modeController = Controller.getCurrentModeController();
					AutomaticLayoutController al = modeController.getExtension(AutomaticLayoutController.class);
					TranslatedObject selectedItem = (TranslatedObject)mAutomaticLayoutComboBox.getSelectedItem();
					al.undoableDeactivateHook(Controller.getCurrentController().getMap().getRootNode());
					if(!selectedItem.equals(AUTOMATIC_LAYOUT_DISABLED)){
						al.undoableActivateHook(Controller.getCurrentController().getMap().getRootNode(), (AutomaticLayout) selectedItem.getObject());
					}
				}
			});
		}
	    appendLabeledComponent(rightBuilder, "AutomaticLayoutAction.text", mAutomaticLayoutComboBox);
		}
		{
			
			if(mAutomaticEdgeColorComboBox == null){
	 			 TranslatedObject[] automaticLayoutTypes = TranslatedObject.fromEnum(AutomaticEdgeColor.class.getSimpleName() + "." , AutomaticEdgeColor.Rule.class);
	 			 mAutomaticEdgeColorComboBox = new JComboBoxWithBorder(automaticLayoutTypes);
				 DefaultComboBoxModel automaticEdgeColorComboBoxModel = (DefaultComboBoxModel) mAutomaticEdgeColorComboBox.getModel();
				 automaticEdgeColorComboBoxModel.addElement(AUTOMATIC_LAYOUT_DISABLED);
				 automaticEdgeColorComboBoxModel.setSelectedItem(AUTOMATIC_LAYOUT_DISABLED);
				 mAutomaticEdgeColorComboBox.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if(internalChange)
							return;
						final ModeController modeController = Controller.getCurrentModeController();
						AutomaticEdgeColorHook hook = modeController.getExtension(AutomaticEdgeColorHook.class);
						TranslatedObject selectedItem = (TranslatedObject)mAutomaticEdgeColorComboBox.getSelectedItem();
						final MapModel map = Controller.getCurrentController().getMap();
						final AutomaticEdgeColor oldExtension = (AutomaticEdgeColor) hook.getMapHook(map);
						final int colorCount = oldExtension == null ? 0 : oldExtension.getColorCounter();
						final NodeModel rootNode = map.getRootNode();
						hook.undoableDeactivateHook(rootNode);
						if(!selectedItem.equals(AUTOMATIC_LAYOUT_DISABLED)){
						final AutomaticEdgeColor newExtension = new  AutomaticEdgeColor((AutomaticEdgeColor.Rule) selectedItem.getObject(), colorCount);
							hook.undoableActivateHook(rootNode, newExtension);
						}
					}
				});
			}
			appendLabeledComponent(rightBuilder, "AutomaticEdgeColorHookAction.text", mAutomaticEdgeColorComboBox);
		}
	}

	private void appendLabeledComponent(final DefaultFormBuilder rightBuilder, String labelKey, Component component) {
		final String text = TextUtils.getText(labelKey);
	    final JLabel label = new JLabel(text);
		TranslatedElement.TEXT.setKey(label, labelKey);
		rightBuilder.append(label, 5);
	    rightBuilder.append(component);
	    rightBuilder.nextLine();
	}

	private void setFont(Container c, float size) {
		c.setFont(c.getFont().deriveFont(size));
		for(int i = 0; i < c.getComponentCount(); i++){
			setFont((Container) c.getComponent(i), size);
		}
    }

	public void setStyle( final NodeModel node) {
		if (internalChange) {
			return;
		}
		internalChange = true;
		try {
			final LogicalStyleController logicalStyleController = LogicalStyleController.getController();
			if(addStyleBox){
				final boolean isStyleSet = LogicalStyleModel.getStyle(node) != null;
				mSetStyle.setValue(isStyleSet);
				setStyleList(mMapStyleButton, logicalStyleController.getMapStyleNames(node, "\n"));
			}
			setStyleList(mNodeStyleButton, logicalStyleController.getNodeStyleNames(node, "\n"));
			final NodeStyleController styleController = NodeStyleController.getController();
			{
				final Color nodeColor = NodeStyleModel.getColor(node);
				final Color viewNodeColor = styleController.getColor(node);
				mSetNodeColor.setValue(nodeColor != null);
				mNodeColor.setColorValue(viewNodeColor);
			}
			{
				final Color color = NodeStyleModel.getBackgroundColor(node);
				final Color viewColor = styleController.getBackgroundColor(node);
				mSetNodeBackgroundColor.setValue(color != null);
				mNodeBackgroundColor.setColorValue(viewColor != null ? viewColor : Controller.getCurrentController()
				    .getMapViewManager().getBackgroundColor(node));
			}
			{
				final NodeStyleModel.Shape shape = NodeStyleModel.getShape(node);
				ShapeConfigurationModel viewShape = styleController.getShapeConfiguration(node);
				final boolean enabled = shape != null;
				mSetNodeShape.setValue(enabled);
				mNodeShape.setValue(viewShape.getShape().toString());
				enableShapeConfigurationProperties(enabled, shape);
				mShapeHorizontalMargin.setQuantifiedValue(viewShape.getHorizontalMargin());
				mShapeVerticalMargin.setQuantifiedValue(viewShape.getVerticalMargin());
				mUniformShape.setValue(viewShape.isUniform());
			}
			final NodeSizeModel nodeSizeModel = NodeSizeModel.getModel(node);
			{
				final Quantity<LengthUnits> width = nodeSizeModel != null ? nodeSizeModel.getMaxNodeWidth() : null;
				final Quantity<LengthUnits> viewWidth = styleController.getMaxWidth(node);
				mSetMaxNodeWidth.setValue(width != null);
				mMaxNodeWidth.setQuantifiedValue(viewWidth);
			}
			{
				final Quantity<LengthUnits> width = nodeSizeModel != null ? nodeSizeModel.getMinNodeWidth() : null;
				final Quantity<LengthUnits> viewWidth = styleController.getMinWidth(node);
				mSetMinNodeWidth.setValue(width != null);
				mMinNodeWidth.setQuantifiedValue(viewWidth);
			}
			final NodeBorderModel nodeBorderModel = NodeBorderModel.getModel(node);
			{
				final Boolean match = nodeBorderModel != null ? nodeBorderModel.getBorderWidthMatchesEdgeWidth() : null;
				final Boolean viewMatch = styleController.getBorderWidthMatchesEdgeWidth(node);
				mSetBorderWidthMatchesEdgeWidth.setValue(match != null);
				mBorderWidthMatchesEdgeWidth.setValue(viewMatch);
			}
			{
				final Quantity<LengthUnits> width = nodeBorderModel != null ? nodeBorderModel.getBorderWidth() : null;
				final Quantity<LengthUnits> viewWidth = styleController.getBorderWidth(node);
				mSetBorderWidth.setValue(width != null);
				mBorderWidth.setQuantifiedValue(viewWidth);
				enableOrDisableBorderWidthControls();
			}
			{
				final Boolean match = nodeBorderModel != null ? nodeBorderModel.getBorderDashMatchesEdgeDash() : null;
				final Boolean viewMatch = styleController.getBorderDashMatchesEdgeDash(node);
				mSetBorderDashMatchesEdgeDash.setValue(match != null);
				mBorderDashMatchesEdgeDash.setValue(viewMatch);
			}
			{
				final DashVariant dash = nodeBorderModel != null ? nodeBorderModel.getBorderDash() : null;
				final DashVariant viewDash = styleController.getBorderDash(node);
				mSetBorderDash.setValue(dash != null);
				mBorderDash.setValue(viewDash.name());
				enableOrDisableBorderDashControls();
			}
			{
				final Boolean match = nodeBorderModel != null ? nodeBorderModel.getBorderColorMatchesEdgeColor() : null;
				final Boolean viewMatch = styleController.getBorderColorMatchesEdgeColor(node);
				mSetBorderColorMatchesEdgeColor.setValue(match != null);
				mBorderColorMatchesEdgeColor.setValue(viewMatch);
			}
			{
				final Color color = nodeBorderModel != null ? nodeBorderModel.getBorderColor() : null;
				final Color viewColor = styleController.getBorderColor(node);
				mSetBorderColor.setValue(color != null);
				mBorderColor.setColorValue(viewColor);
				enableOrDisableBorderColorControls();
			}
			{
				final Quantity<LengthUnits> iconSize = node.getSharedData().getIcons().getIconSize();
				final Quantity<LengthUnits> viewedIconSize = IconController.getController().getIconSize(node);
				mSetIconSize.setValue(iconSize != null);
				mIconSize.setQuantifiedValue(viewedIconSize);
			}
			{
				final LocationModel locationModel = LocationModel.getModel(node);
				final LocationController locationController = modeController.getExtension(LocationController.class);
				final Quantity<LengthUnits> gap = locationModel.getVGap();
				final Quantity<LengthUnits> viewGap = locationController.getMinimalDistanceBetweenChildren(node);
				mSetChildDistance.setValue(gap != LocationModel.DEFAULT_VGAP);
				mChildDistance.setQuantifiedValue(viewGap);
			}
			
			final EdgeController edgeController = EdgeController.getController();
			final EdgeModel edgeModel = EdgeModel.getModel(node);
			{
				final Color edgeColor = edgeModel != null ? edgeModel.getColor() : null;
				final Color viewColor = edgeController.getColor(node);
				mSetEdgeColor.setValue(edgeColor != null);
				mEdgeColor.setColorValue(viewColor);
			}
			{
				final EdgeStyle style = edgeModel != null ? edgeModel.getStyle() : null;
				final EdgeStyle viewStyle = edgeController.getStyle(node);
				mSetEdgeStyle.setValue(style != null);
				mEdgeStyle.setValue(viewStyle.toString());
			}
			{
				final int width = edgeModel != null ? edgeModel.getWidth() : EdgeModel.DEFAULT_WIDTH;
				final int viewWidth = edgeController.getWidth(node);
				mSetEdgeWidth.setValue(width != EdgeModel.DEFAULT_WIDTH);
				mEdgeWidth.setValue(Integer.toString(viewWidth));
			}
			{
				final DashVariant dash = edgeModel != null ? edgeModel.getDash() : null;
				final DashVariant viewDash = edgeController.getDash(node);
				mSetEdgeDash.setValue(dash != null);
				mEdgeDash.setValue(viewDash.name());
			}
			{
				final CloudController cloudController = CloudController.getController();
				final CloudModel cloudModel = CloudModel.getModel(node);
				final Color viewCloudColor = cloudController.getColor(node);
				mSetCloud.setValue(cloudModel != null);
				mCloudColor.setColorValue(viewCloudColor);

				final CloudModel.Shape viewCloudShape = cloudController.getShape(node);
				mCloudShape.setValue(viewCloudShape != null ? viewCloudShape.toString() : CloudModel.Shape.ARC.toString());
			}
			{
				final String fontFamilyName = NodeStyleModel.getFontFamilyName(node);
				final String viewFontFamilyName = styleController.getFontFamilyName(node);
				mSetNodeFontName.setValue(fontFamilyName != null);
				mNodeFontName.setValue(viewFontFamilyName);
			}
			{
				final Integer fontSize = NodeStyleModel.getFontSize(node);
				final Integer viewfontSize = styleController.getFontSize(node);
				mSetNodeFontSize.setValue(fontSize != null);
				mNodeFontSize.setValue(viewfontSize.toString());
			}
			{
				final Boolean bold = NodeStyleModel.isBold(node);
				final Boolean viewbold = styleController.isBold(node);
				mSetNodeFontBold.setValue(bold != null);
				mNodeFontBold.setValue(viewbold);
			}
			{
				final Boolean italic = NodeStyleModel.isItalic(node);
				final Boolean viewitalic = styleController.isItalic(node);
				mSetNodeFontItalic.setValue(italic != null);
				mNodeFontItalic.setValue(viewitalic);
			}
			{
				final TextAlign style = NodeStyleModel.getTextAlign(node);
				final TextAlign viewStyle = styleController.getTextAlign(node);
				mSetNodeTextAlignment.setValue(style != null);
				mNodeTextAlignment.setValue(viewStyle.toString());
			}
			{
				final Boolean hyperlink = NodeLinks.formatNodeAsHyperlink(node);
				final Boolean viewhyperlink = LinkController.getController().formatNodeAsHyperlink(node);
				mSetNodeFontHyperlink.setValue(hyperlink != null);
				mNodeFontHyperlink.setValue(viewhyperlink);
			}
			{
				final Boolean nodeNumbering = NodeStyleModel.getNodeNumbering(node);
				final Boolean viewNodeNumbering = styleController.getNodeNumbering(node);
				mSetNodeNumbering.setValue(nodeNumbering != null);
				mNodeNumbering.setValue(viewNodeNumbering);
			}
			{
				String nodeFormat = NodeStyleModel.getNodeFormat(node);
				String viewNodeFormat = TextController.getController().getNodeFormat(node);
				mSetNodeFormat.setValue(nodeFormat != null);
				if (viewNodeFormat == null && node.getUserObject() instanceof IFormattedObject)
					viewNodeFormat = ((IFormattedObject)node.getUserObject()).getPattern();
				mNodeFormat.setValue(viewNodeFormat);
			}
			if(mAutomaticLayoutComboBox != null){
				final ModeController modeController = Controller.getCurrentModeController();
				AutomaticLayoutController al = modeController.getExtension(AutomaticLayoutController.class);
				IExtension extension = al.getExtension(node);
				if(extension == null)
					mAutomaticLayoutComboBox.setSelectedItem(AUTOMATIC_LAYOUT_DISABLED);
				else
					mAutomaticLayoutComboBox.setSelectedIndex(((AutomaticLayout)extension).ordinal());
			}
			if(mAutomaticEdgeColorComboBox != null){
				final ModeController modeController = Controller.getCurrentModeController();
				AutomaticEdgeColorHook al = (AutomaticEdgeColorHook) modeController.getExtension(AutomaticEdgeColorHook.class);
				final AutomaticEdgeColor extension = (AutomaticEdgeColor) al.getExtension(node);
				if(extension == null)
					mAutomaticEdgeColorComboBox.setSelectedItem(AUTOMATIC_LAYOUT_DISABLED);
				else
					mAutomaticEdgeColorComboBox.setSelectedIndex(extension.rule.ordinal());
			}
		}
		finally {
			internalChange = false;
		}
	}

	private void setStyleList(JButton btn, String styles) {
		if("".equals(styles)){
			btn.setToolTipText(null);
			btn.setText(" ");
			return;
		}
		btn.setToolTipText(HtmlUtils.plainToHTML(styles));
		final String text = styles.replaceAll("\n", ", ");
		btn.setText(text);
    }

	private void addListeners() {
		final Controller controller = Controller.getCurrentController();
		final ModeController modeController = Controller.getCurrentModeController();
		final MapController mapController = modeController.getMapController();
		mapController.addNodeSelectionListener(new INodeSelectionListener() {
			public void onSelect(final NodeModel node) {
				final IMapSelection selection = controller.getSelection();
				if (selection == null) {
					return;
				}
				if (selection.size() == 1) {
					setComponentsEnabled(true);
					setStyle(node);
				}
			}

			public void setComponentsEnabled(boolean enabled) {
				final Container panel = (Container) getComponent(0);
				for (int i = 0; i < panel.getComponentCount(); i++) {
					panel.getComponent(i).setEnabled(enabled);
				}
			}

			public void onDeselect(final NodeModel node) {
				setComponentsEnabled(false);
			}
		});
		mapController.addNodeChangeListener(new INodeChangeListener() {
			public void nodeChanged(final NodeChangeEvent event) {
				final IMapSelection selection = controller.getSelection();
				if (selection == null) {
					return;
				}
				final NodeModel node = event.getNode();
				if (selection.getSelected().equals(node)) {
					setStyle(node);
				}
			}
		});
		mapController.addMapChangeListener(new AMapChangeListenerAdapter() {

			@Override
            public void mapChanged(MapChangeEvent event) {
				if(! MapStyle.MAP_STYLES.equals(event.getProperty()))
					return;
				final IMapSelection selection = controller.getSelection();
				if (selection == null) {
					return;
				}
				final NodeModel node = selection.getSelected();
				setStyle(node);
            }
			
		});
	}

	private void enableShapeConfigurationProperties(final boolean enabled, final Shape shape) {
		final boolean enableConfigurationProperties = enabled && shape.hasConfiguration;
		mShapeHorizontalMargin.setEnabled(enableConfigurationProperties);
		mShapeVerticalMargin.setEnabled(enableConfigurationProperties);
		mUniformShape.setEnabled(enableConfigurationProperties);
	}

	public void enableOrDisableBorderWidthControls() {
		final boolean borderWidthCanBeSet = ! mBorderWidthMatchesEdgeWidth.getBooleanValue();
		mSetBorderWidth.setEnabled(borderWidthCanBeSet);
		mBorderWidth.setEnabled(borderWidthCanBeSet);
	}

	public void enableOrDisableBorderDashControls() {
		final boolean borderDashCanBeSet = ! mBorderDashMatchesEdgeDash.getBooleanValue();
		mSetBorderDash.setEnabled(borderDashCanBeSet);
		mBorderDash.setEnabled(borderDashCanBeSet);
	}

	public void enableOrDisableBorderColorControls() {
		final boolean borderColorCanBeSet = ! mBorderColorMatchesEdgeColor.getBooleanValue();
		mSetBorderColor.setEnabled(borderColorCanBeSet);
		mBorderColor.setEnabled(borderColorCanBeSet);
	}
}
