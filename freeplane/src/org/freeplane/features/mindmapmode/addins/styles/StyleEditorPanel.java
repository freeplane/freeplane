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
package org.freeplane.features.mindmapmode.addins.styles;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.HeadlessException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Map.Entry;

import javax.swing.JPanel;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.icon.IconController;
import org.freeplane.core.icon.MindIcon;
import org.freeplane.core.modecontroller.IMapSelection;
import org.freeplane.core.modecontroller.INodeChangeListener;
import org.freeplane.core.modecontroller.INodeSelectionListener;
import org.freeplane.core.modecontroller.MapController;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.modecontroller.NodeChangeEvent;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.resources.ui.BooleanProperty;
import org.freeplane.core.resources.ui.ColorProperty;
import org.freeplane.core.resources.ui.ComboProperty;
import org.freeplane.core.resources.ui.FontProperty;
import org.freeplane.core.resources.ui.IPropertyControl;
import org.freeplane.core.resources.ui.IconProperty;
import org.freeplane.core.resources.ui.NextLineProperty;
import org.freeplane.core.resources.ui.SeparatorProperty;
import org.freeplane.features.common.cloud.CloudController;
import org.freeplane.features.common.cloud.CloudModel;
import org.freeplane.features.common.edge.EdgeController;
import org.freeplane.features.common.edge.EdgeModel;
import org.freeplane.features.common.edge.EdgeStyle;
import org.freeplane.features.common.nodestyle.NodeStyleController;
import org.freeplane.features.common.nodestyle.NodeStyleModel;
import org.freeplane.features.mindmapmode.cloud.MCloudController;
import org.freeplane.features.mindmapmode.edge.MEdgeController;
import org.freeplane.features.mindmapmode.icon.MIconController;
import org.freeplane.features.mindmapmode.nodestyle.MNodeStyleController;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class StyleEditorPanel extends JPanel {
	private class BgColorChangeListener extends ChangeListener {
		public BgColorChangeListener(final BooleanProperty mSet, final IPropertyControl mProperty) {
			super(mSet, mProperty);
		}

		@Override
		void applyValue(final boolean enabled, final NodeModel node, PropertyChangeEvent evt) {
			final MNodeStyleController styleController = (MNodeStyleController) mMindMapController
			    .getExtension(NodeStyleController.class);
			styleController.setBackgroundColor(node, enabled ? mNodeBackgroundColor.getColorValue() : null);
		}
	}
	private class NodeShapeChangeListener extends ChangeListener {
		public NodeShapeChangeListener(final BooleanProperty mSet, final IPropertyControl mProperty) {
			super(mSet, mProperty);
		}

		@Override
		void applyValue(final boolean enabled, final NodeModel node, PropertyChangeEvent evt) {
			final MNodeStyleController styleController = (MNodeStyleController) mMindMapController
			    .getExtension(NodeStyleController.class);
			styleController.setShape(node, enabled ? mNodeShape.getValue() : null);
		}
	}
	
	private class ColorChangeListener extends ChangeListener {
		public ColorChangeListener(final BooleanProperty mSet, final IPropertyControl mProperty) {
			super(mSet, mProperty);
		}

		@Override
		void applyValue(final boolean enabled, final NodeModel node, PropertyChangeEvent evt) {
			final MNodeStyleController styleController = (MNodeStyleController) mMindMapController
			    .getExtension(NodeStyleController.class);
			styleController.setColor(node, enabled ? mNodeColor.getColorValue() : null);
		}
	}
	private class FontBoldChangeListener extends ChangeListener {
		public FontBoldChangeListener(final BooleanProperty mSet, final IPropertyControl mProperty) {
			super(mSet, mProperty);
		}

		@Override
		void applyValue(final boolean enabled, final NodeModel node, PropertyChangeEvent evt) {
			final MNodeStyleController styleController = (MNodeStyleController) mMindMapController
			    .getExtension(NodeStyleController.class);
			styleController.setBold(node, enabled ? mNodeFontBold.getBooleanValue() : null);
		}
	}
	
	private class FontItalicChangeListener extends ChangeListener {
		public FontItalicChangeListener(final BooleanProperty mSet, final IPropertyControl mProperty) {
			super(mSet, mProperty);
		}

		@Override
		void applyValue(final boolean enabled, final NodeModel node, PropertyChangeEvent evt) {
			final MNodeStyleController styleController = (MNodeStyleController) mMindMapController
			    .getExtension(NodeStyleController.class);
			styleController.setItalic(node, enabled ? mNodeFontItalic.getBooleanValue() : null);
		}
	}
	private class FontSizeChangeListener extends ChangeListener {
		public FontSizeChangeListener(final BooleanProperty mSet, final IPropertyControl mProperty) {
			super(mSet, mProperty);
		}

		@Override
		void applyValue(final boolean enabled, final NodeModel node, PropertyChangeEvent evt) {
			final MNodeStyleController styleController = (MNodeStyleController) mMindMapController
			    .getExtension(NodeStyleController.class);
			styleController.setFontSize(node, enabled ? Integer.valueOf(mNodeFontSize.getValue()) : null);
		}
	}
	private class FontNameChangeListener extends ChangeListener {
		public FontNameChangeListener(final BooleanProperty mSet, final IPropertyControl mProperty) {
			super(mSet, mProperty);
		}

		@Override
		void applyValue(final boolean enabled, final NodeModel node, PropertyChangeEvent evt) {
			final MNodeStyleController styleController = (MNodeStyleController) mMindMapController
			    .getExtension(NodeStyleController.class);
			styleController.setFontFamily(node, enabled ? mNodeFontName.getValue() : null);
		}
	}
	private class IconChangeListener extends ChangeListener {
		public IconChangeListener(final BooleanProperty mSet, final IPropertyControl mProperty) {
			super(mSet, mProperty);
		}

		@Override
		void applyValue(final boolean enabled, final NodeModel node, PropertyChangeEvent evt) {
			final MIconController styleController = (MIconController) mMindMapController
			    .getExtension(IconController.class);
			styleController.removeAllIcons(node);
			if(enabled)
				styleController.addIcon(node,  mIcon.getIcon());
		}
	}
	

	
	private class EdgeColorChangeListener extends ChangeListener {
		public EdgeColorChangeListener(final BooleanProperty mSet, final IPropertyControl mProperty) {
			super(mSet, mProperty);
		}

		@Override
		void applyValue(final boolean enabled, final NodeModel node, PropertyChangeEvent evt) {
			final MEdgeController styleController = (MEdgeController) mMindMapController
			    .getExtension(EdgeController.class);
			styleController.setColor(node, enabled ? mEdgeColor.getColorValue() : null);
		}
	}

	private class EdgeStyleChangeListener extends ChangeListener {
		public EdgeStyleChangeListener(final BooleanProperty mSet, final IPropertyControl mProperty) {
			super(mSet, mProperty);
		}

		@Override
		void applyValue(final boolean enabled, final NodeModel node, PropertyChangeEvent evt) {
			final MEdgeController styleController = (MEdgeController) mMindMapController
			    .getExtension(EdgeController.class);
			styleController.setStyle(node, enabled ? EdgeStyle.getStyle(mEdgeStyle.getValue()) : null);
		}
	}
	private class EdgeWidthChangeListener extends ChangeListener {
		public EdgeWidthChangeListener(final BooleanProperty mSet, final IPropertyControl mProperty) {
			super(mSet, mProperty);
		}

		@Override
		void applyValue(final boolean enabled, final NodeModel node, PropertyChangeEvent evt) {
			final MEdgeController styleController = (MEdgeController) mMindMapController
			    .getExtension(EdgeController.class);
			styleController.setWidth(node, enabled ? getEdgeWidthTransformation().get(mEdgeWidth.getValue()) : EdgeModel.DEFAULT_WIDTH);
		}
	}
	
	private class CloudColorChangeListener extends ChangeListener {
		public CloudColorChangeListener(final BooleanProperty mSet, final IPropertyControl mProperty) {
			super(mSet, mProperty);
		}

		@Override
		void applyValue(final boolean enabled, final NodeModel node, PropertyChangeEvent evt) {
			final MCloudController styleController = (MCloudController) mMindMapController
			    .getExtension(CloudController.class);
			if(enabled)
				styleController.setColor(node, mCloudColor.getColorValue());
			else 
				styleController.setCloud(node, false);
		}
	}
	private abstract class ChangeListener implements PropertyChangeListener {
		final private IPropertyControl mProperty;
		final private BooleanProperty mSet;

		public ChangeListener(final BooleanProperty mSet, final IPropertyControl mProperty) {
			super();
			this.mSet = mSet;
			this.mProperty = mProperty;
		}

		abstract void applyValue(final boolean enabled, NodeModel node, PropertyChangeEvent evt);

		public void propertyChange(final PropertyChangeEvent evt) {
			if (internalChange) {
				return;
			}
			final boolean enabled;
			if (evt.getSource().equals(mSet)) {
				enabled = mSet.getBooleanValue();
				mProperty.setEnabled(enabled);
			}
			else {
				assert evt.getSource().equals(mProperty);
				enabled = true;
			}
			final List<NodeModel> nodes = mMindMapController.getController().getSelection().getSelection();
			internalChange = true;
			for (final NodeModel node : nodes) {
				applyValue(enabled, node, evt);
			}
			internalChange = false;
		}
	}

	private static final String CLOUD_COLOR = "cloudcolor";
	private static final String EDGE_COLOR = "edgecolor";
	private static final String EDGE_STYLE = "edgestyle";
	private static final String[] EDGE_STYLES = StyleEditorPanel.initializeEdgeStyles();
	private static final String EDGE_WIDTH = "edgewidth";
	private static final String[] EDGE_WIDTHS = new String[] { "EdgeWidth_thin", "EdgeWidth_1",
	        "EdgeWidth_2", "EdgeWidth_4", "EdgeWidth_8" };
	private static final String ICON = "icon";
	private static final String NODE_BACKGROUND_COLOR = "nodebackgroundcolor";
	private static final String NODE_COLOR = "nodecolor";
	private static final String NODE_FONT_BOLD = "nodefontbold";
	private static final String NODE_FONT_ITALIC = "nodefontitalic";
	private static final String NODE_FONT_NAME = "nodefontname";
	private static final String NODE_FONT_SIZE = "nodefontsize";
	private static final String NODE_SHAPE = "nodeshape";
	private static final String NODE_TEXT_COLOR = "standardnodetextcolor";
	/**
	* 
	*/
	private static final long serialVersionUID = 1L;
	private static final String SET_CLOUD_COLOR = StyleEditorPanel.SET_RESOURCE;
	private static final String SET_EDGE_COLOR = StyleEditorPanel.SET_RESOURCE;
	private static final String SET_EDGE_STYLE = StyleEditorPanel.SET_RESOURCE;
	private static final String SET_EDGE_WIDTH = StyleEditorPanel.SET_RESOURCE;
	private static final String SET_ICON = StyleEditorPanel.SET_RESOURCE;
	private static final String SET_NODE_BACKGROUND_COLOR = StyleEditorPanel.SET_RESOURCE;
	private static final String SET_NODE_COLOR = StyleEditorPanel.SET_RESOURCE;
	private static final String SET_NODE_FONT_BOLD = StyleEditorPanel.SET_RESOURCE;
	private static final String SET_NODE_FONT_ITALIC = StyleEditorPanel.SET_RESOURCE;
	private static final String SET_NODE_FONT_NAME = StyleEditorPanel.SET_RESOURCE;
	private static final String SET_NODE_FONT_SIZE = StyleEditorPanel.SET_RESOURCE;
	private static final String SET_NODE_STYLE = StyleEditorPanel.SET_RESOURCE;
	private static final String SET_RESOURCE = "set_property_text";

	private static String[] initializeEdgeStyles() {
		final EdgeStyle[] enumConstants = EdgeStyle.class.getEnumConstants();
		final String[] strings = new String[enumConstants.length];
		for (int i = 0; i < enumConstants.length; i++) {
			strings[i] = enumConstants[i].toString();
		}
		return strings;
	}

	private boolean internalChange;
	private ColorProperty mCloudColor;
	private List<IPropertyControl> mControls;
	private ColorProperty mEdgeColor;
	private ComboProperty mEdgeStyle;
	private ComboProperty mEdgeWidth;
	private IconProperty mIcon;
	private List<MindIcon> mIconInformationVector;
	private final ModeController mMindMapController;
	private ColorProperty mNodeBackgroundColor;
	private ColorProperty mNodeColor;
	private BooleanProperty mNodeFontBold;
	private BooleanProperty mNodeFontItalic;
	private FontProperty mNodeFontName;
	private ComboProperty mNodeFontSize;
	private ComboProperty mNodeShape;
	private BooleanProperty mSetCloudColor;
	private BooleanProperty mSetEdgeColor;
	private BooleanProperty mSetEdgeStyle;
	private BooleanProperty mSetEdgeWidth;
	private BooleanProperty mSetIcon;
	private BooleanProperty mSetNodeBackgroundColor;
	private BooleanProperty mSetNodeColor;
	private BooleanProperty mSetNodeFontBold;
	private BooleanProperty mSetNodeFontItalic;
	private BooleanProperty mSetNodeFontName;
	private BooleanProperty mSetNodeFontSize;
	private BooleanProperty mSetNodeShape;
	final private String[] sizes = new String[] { "2", "4", "6", "8", "10", "12", "14", "16", "18", "20", "22", "24",
	        "30", "36", "48", "72" };
	private boolean addStyleBox;
	private MUIFactory uiFactory;

	/**
	 * @throws HeadlessException
	 */
	public StyleEditorPanel(final ModeController pMindMapController, MUIFactory uiFactory, boolean addStyleBox) throws HeadlessException {
		super();
		mMindMapController = pMindMapController;
		this.addStyleBox = addStyleBox;
		this.uiFactory = uiFactory;
	}

	private void addBgColorControl(final List<IPropertyControl> controls) {
		mSetNodeBackgroundColor = new BooleanProperty(StyleEditorPanel.SET_NODE_BACKGROUND_COLOR);
		controls.add(mSetNodeBackgroundColor);
		mNodeBackgroundColor = new ColorProperty(StyleEditorPanel.NODE_BACKGROUND_COLOR, ResourceController
		    .getResourceController().getDefaultProperty(NODE_BACKGROUND_COLOR));
		controls.add(mNodeBackgroundColor);
		final BgColorChangeListener listener = new BgColorChangeListener(mSetNodeBackgroundColor,
		    mNodeBackgroundColor);
		mSetNodeBackgroundColor.addPropertyChangeListener(listener);
		mNodeBackgroundColor.addPropertyChangeListener(listener);
	}

	private void addCloudColorControl(final List<IPropertyControl> controls) {
		mSetCloudColor = new BooleanProperty(StyleEditorPanel.SET_CLOUD_COLOR);
		controls.add(mSetCloudColor);
		mCloudColor = new ColorProperty(StyleEditorPanel.CLOUD_COLOR, ResourceController.getResourceController()
		    .getDefaultProperty(CloudController.RESOURCES_CLOUD_COLOR));
		controls.add(mCloudColor);
		final CloudColorChangeListener listener = new CloudColorChangeListener(mSetCloudColor,
			mCloudColor);
		mSetCloudColor.addPropertyChangeListener(listener);
		mCloudColor.addPropertyChangeListener(listener);
	}

	private void addColorControl(final List<IPropertyControl> controls) {
		mSetNodeColor = new BooleanProperty(StyleEditorPanel.SET_NODE_COLOR);
		controls.add(mSetNodeColor);
		mNodeColor = new ColorProperty(StyleEditorPanel.NODE_COLOR, ResourceController.getResourceController()
		    .getDefaultProperty(NODE_TEXT_COLOR));
		controls.add(mNodeColor);
		final ColorChangeListener listener = new ColorChangeListener(mSetNodeColor,
			mNodeColor);
		mSetNodeColor.addPropertyChangeListener(listener);
		mNodeColor.addPropertyChangeListener(listener);
	}

	private void addEdgeColorControl(final List<IPropertyControl> controls) {
		mSetEdgeColor = new BooleanProperty(StyleEditorPanel.SET_EDGE_COLOR);
		controls.add(mSetEdgeColor);
		mEdgeColor = new ColorProperty(StyleEditorPanel.EDGE_COLOR, ResourceController.getResourceController()
		    .getDefaultProperty(EdgeController.RESOURCES_EDGE_COLOR));
		controls.add(mEdgeColor);
		final EdgeColorChangeListener listener = new EdgeColorChangeListener(mSetEdgeColor,
			mEdgeColor);
		mSetEdgeColor.addPropertyChangeListener(listener);
		mEdgeColor.addPropertyChangeListener(listener);
	}

	private void addEdgeStyleControl(final List<IPropertyControl> controls) {
		mSetEdgeStyle = new BooleanProperty(StyleEditorPanel.SET_EDGE_STYLE);
		controls.add(mSetEdgeStyle);
		mEdgeStyle = new ComboProperty(StyleEditorPanel.EDGE_STYLE, EDGE_STYLES);
		controls.add(mEdgeStyle);
		final EdgeStyleChangeListener listener = new EdgeStyleChangeListener(mSetEdgeStyle,
			mEdgeStyle);
		mSetEdgeStyle.addPropertyChangeListener(listener);
		mEdgeStyle.addPropertyChangeListener(listener);
	}

	private void addEdgeWidthControl(final List<IPropertyControl> controls) {
		mSetEdgeWidth = new BooleanProperty(StyleEditorPanel.SET_EDGE_WIDTH);
		controls.add(mSetEdgeWidth);
		mEdgeWidth = new ComboProperty(StyleEditorPanel.EDGE_WIDTH, EDGE_WIDTHS);
		controls.add(mEdgeWidth);
		final EdgeWidthChangeListener listener = new EdgeWidthChangeListener(mSetEdgeWidth,
			mEdgeWidth);
		mSetEdgeWidth.addPropertyChangeListener(listener);
		mEdgeWidth.addPropertyChangeListener(listener);
	}

	private void addFontBoldControl(final List<IPropertyControl> controls) {
		mSetNodeFontBold = new BooleanProperty(StyleEditorPanel.SET_NODE_FONT_BOLD);
		controls.add(mSetNodeFontBold);
		mNodeFontBold = new BooleanProperty(StyleEditorPanel.NODE_FONT_BOLD);
		controls.add(mNodeFontBold);
		final FontBoldChangeListener listener = new FontBoldChangeListener(mSetNodeFontBold,
			mNodeFontBold);
		mSetNodeFontBold.addPropertyChangeListener(listener);
		mNodeFontBold.addPropertyChangeListener(listener);
	}

	private void addFontItalicControl(final List<IPropertyControl> controls) {
		mSetNodeFontItalic = new BooleanProperty(StyleEditorPanel.SET_NODE_FONT_ITALIC);
		controls.add(mSetNodeFontItalic);
		mNodeFontItalic = new BooleanProperty(StyleEditorPanel.NODE_FONT_ITALIC);
		controls.add(mNodeFontItalic);
		final FontItalicChangeListener listener = new FontItalicChangeListener(mSetNodeFontItalic,
			mNodeFontItalic);
		mSetNodeFontItalic.addPropertyChangeListener(listener);
		mNodeFontItalic.addPropertyChangeListener(listener);
	}

	private void addFontNameControl(final List<IPropertyControl> controls) {
		mSetNodeFontName = new BooleanProperty(StyleEditorPanel.SET_NODE_FONT_NAME);
		controls.add(mSetNodeFontName);
		mNodeFontName = new FontProperty(StyleEditorPanel.NODE_FONT_NAME);
		controls.add(mNodeFontName);
		final FontNameChangeListener listener = new FontNameChangeListener(mSetNodeFontName,
			mNodeFontName);
		mSetNodeFontName.addPropertyChangeListener(listener);
		mNodeFontName.addPropertyChangeListener(listener);
	}

	private void addFontSizeControl(final List<IPropertyControl> controls) {
		mSetNodeFontSize = new BooleanProperty(StyleEditorPanel.SET_NODE_FONT_SIZE);
		controls.add(mSetNodeFontSize);
		final List<String> sizesVector = new ArrayList<String>(Arrays.asList(sizes));
		mNodeFontSize = new ComboProperty(StyleEditorPanel.NODE_FONT_SIZE, sizesVector, sizesVector);
		controls.add(mNodeFontSize);
		final FontSizeChangeListener listener = new FontSizeChangeListener(mSetNodeFontSize,
			mNodeFontSize);
		mSetNodeFontSize.addPropertyChangeListener(listener);
		mNodeFontSize.addPropertyChangeListener(listener);
	}

	private void addNodeIconControl(final List<IPropertyControl> controls) {
		mIconInformationVector = new ArrayList<MindIcon>();
		final ModeController controller = mMindMapController;
		final Collection<MindIcon> mindIcons = ((MIconController) IconController.getController(controller))
		    .getMindIcons();
		mIconInformationVector.addAll(mindIcons);
		mSetIcon = new BooleanProperty(StyleEditorPanel.SET_ICON);
		controls.add(mSetIcon);
		mIcon = new IconProperty(StyleEditorPanel.ICON, mIconInformationVector);
		controls.add(mIcon);
		final IconChangeListener listener = new IconChangeListener(mSetIcon,
			mIcon);
		mSetIcon.addPropertyChangeListener(listener);
		mIcon.addPropertyChangeListener(listener);
	}

	private void addNodeShapeControl(final List<IPropertyControl> controls) {
		mSetNodeShape = new BooleanProperty(StyleEditorPanel.SET_NODE_STYLE);
		controls.add(mSetNodeShape);
		mNodeShape = new ComboProperty(StyleEditorPanel.NODE_SHAPE, new String[] { "fork", "bubble", "as_parent",
		        "combined" });
		controls.add(mNodeShape);
		final NodeShapeChangeListener listener = new NodeShapeChangeListener(mSetNodeShape,
			mNodeShape);
		mSetNodeShape.addPropertyChangeListener(listener);
		mNodeShape.addPropertyChangeListener(listener);
	}

	private List<IPropertyControl> getControls() {
		final List<IPropertyControl> controls = new ArrayList<IPropertyControl>();
		controls.add(new SeparatorProperty("OptionPanel.separator.NodeColors"));
		addColorControl(controls);
		addBgColorControl(controls);
		controls.add(new SeparatorProperty("OptionPanel.separator.NodeShape"));
		addNodeShapeControl(controls);
		addNodeIconControl(controls);
		controls.add(new NextLineProperty());
		controls.add(new SeparatorProperty("OptionPanel.separator.NodeFont"));
		addFontNameControl(controls);
		addFontSizeControl(controls);
		addFontBoldControl(controls);
		addFontItalicControl(controls);
		controls.add(new NextLineProperty());
		controls.add(new SeparatorProperty("OptionPanel.separator.EdgeControls"));
		addEdgeWidthControl(controls);
		addEdgeStyleControl(controls);
		addEdgeColorControl(controls);
		controls.add(new NextLineProperty());
		controls.add(new SeparatorProperty("OptionPanel.separator.CloudControls"));
		addCloudColorControl(controls);
		return controls;
	}

	private HashMap<String, Integer> getEdgeWidthTransformation() {
		final HashMap<String, Integer> transformator = new HashMap<String, Integer>(
		    StyleEditorPanel.EDGE_WIDTHS.length);
		int i = 0;
		transformator.put(StyleEditorPanel.EDGE_WIDTHS[i++], EdgeModel.WIDTH_THIN);
		transformator.put(StyleEditorPanel.EDGE_WIDTHS[i++], 1);
		transformator.put(StyleEditorPanel.EDGE_WIDTHS[i++], 2);
		transformator.put(StyleEditorPanel.EDGE_WIDTHS[i++], 4);
		transformator.put(StyleEditorPanel.EDGE_WIDTHS[i++], 8);
		return transformator;
	}

	/**
	 * Creates all controls and adds them to the frame.
	 * @param modeController 
	 */
	public void init(ModeController modeController) {
		final String form = "right:max(40dlu;p), 4dlu, 20dlu, 7dlu,right:max(40dlu;p), 4dlu, 80dlu, 7dlu";
		final FormLayout rightLayout = new FormLayout(form, "");
		final DefaultFormBuilder rightBuilder = new DefaultFormBuilder(rightLayout);
		rightBuilder.setDefaultDialogBorder();
		if(addStyleBox){
			String label = ResourceBundles.getText("OptionPanel.separator.NodeStyle");
			rightBuilder.appendSeparator(label);
			Container styleBox = uiFactory.createStyleBox();
			rightBuilder.nextLine();
			rightBuilder.append("");
			rightBuilder.append(styleBox, 5);
			rightBuilder.nextLine();
		}
		mControls = getControls();
		for (final IPropertyControl control : mControls) {
			control.layout(rightBuilder);
		}
		add(rightBuilder.getPanel(), BorderLayout.CENTER);
		addListeners(modeController);
	}

	public void setStyle(final ModeController modeController, final NodeModel node) {
		if(internalChange){
			return;
		}
		internalChange = true;
		try {
			final NodeStyleController styleController = NodeStyleController.getController(modeController);
			{
				final Color nodeColor = NodeStyleModel.getColor(node);
				final Color viewNodeColor = styleController.getColor(node);
				mSetNodeColor.setValue(nodeColor != null);
				mNodeColor.setColorValue(viewNodeColor);
				mNodeColor.setEnabled(mSetNodeColor.getBooleanValue());
			}
			{
				final Color color = NodeStyleModel.getBackgroundColor(node);
				final Color viewColor = styleController.getBackgroundColor(node);
				mSetNodeBackgroundColor.setValue(color != null);
				mNodeBackgroundColor.setColorValue(viewColor != null ? viewColor : modeController.getController().getMapViewManager().getBackgroundColor(node));
				mNodeBackgroundColor.setEnabled(mSetNodeBackgroundColor.getBooleanValue());
			}
			{
				final String shape = NodeStyleModel.getShape(node);
				final String viewShape = styleController.getShape(node);
				mSetNodeShape.setValue(shape != null);
				mNodeShape.setValue(viewShape);
				mNodeShape.setEnabled(mSetNodeShape.getBooleanValue());
			}
			final EdgeController edgeController = EdgeController.getController(modeController);
			final EdgeModel edgeModel = EdgeModel.getModel(node);
			{
				final Color edgeColor = edgeModel != null ? edgeModel.getColor() : null;
				final Color viewColor = edgeController.getColor(node);
				mSetEdgeColor.setValue(edgeColor != null);
				mEdgeColor.setColorValue(viewColor);
				mEdgeColor.setEnabled(mSetEdgeColor.getBooleanValue());
			}
			{
				final EdgeStyle style = edgeModel != null ? edgeModel.getStyle() : null;
				final EdgeStyle viewStyle = edgeController.getStyle(node);
				mSetEdgeStyle.setValue(style != null);
				mEdgeStyle.setValue(viewStyle.toString());
				mEdgeStyle.setEnabled(mSetEdgeStyle.getBooleanValue());
			}
			{
				final int width = edgeModel != null ? edgeModel.getWidth() : EdgeModel.DEFAULT_WIDTH;
				final int viewWidth = edgeController.getWidth(node);
				mSetEdgeWidth.setValue(width != EdgeModel.DEFAULT_WIDTH);
				mEdgeWidth.setValue(transformEdgeWidth(viewWidth));
				mEdgeWidth.setEnabled(mSetEdgeWidth.getBooleanValue());
			}
			final CloudController cloudController = CloudController.getController(modeController);
			final CloudModel cloudModel = CloudModel.getModel(node);
			final Color viewCloudColor = cloudController.getColor(node);
			mSetCloudColor.setValue(cloudModel != null);
			mCloudColor.setColorValue(viewCloudColor);
			mCloudColor.setEnabled(mSetCloudColor.getBooleanValue());
			{
				final String fontFamilyName = NodeStyleModel.getFontFamilyName(node);
				final String viewFontFamilyName = styleController.getFontFamilyName(node);
				mSetNodeFontName.setValue(fontFamilyName != null);
				mNodeFontName.setValue(viewFontFamilyName);
				mNodeFontName.setEnabled(mSetNodeFontName.getBooleanValue());
			}
			{
				final Integer fontSize = NodeStyleModel.getFontSize(node);
				final Integer viewfontSize = styleController.getFontSize(node);
				mSetNodeFontSize.setValue(fontSize != null);
				mNodeFontSize.setValue(viewfontSize.toString());
				mNodeFontSize.setEnabled(mSetNodeFontSize.getBooleanValue());
			}
			{
				final Boolean bold = NodeStyleModel.isBold(node);
				final Boolean viewbold = styleController.isBold(node);
				mSetNodeFontBold.setValue(bold != null);
				mNodeFontBold.setValue(viewbold);
				mNodeFontBold.setEnabled(mSetNodeFontBold.getBooleanValue());
			}
			{
				final Boolean italic = NodeStyleModel.isItalic(node);
				final Boolean viewitalic = styleController.isItalic(node);
				mSetNodeFontItalic.setValue(italic != null);
				mNodeFontItalic.setValue(viewitalic);
				mNodeFontItalic.setEnabled(mSetNodeFontItalic.getBooleanValue());
			}
			MindIcon icon = mIconInformationVector.get(0);
			try {
				icon = node.getIcon(0);
				mSetIcon.setValue(true);
			}
			catch (final IndexOutOfBoundsException e) {
				mSetIcon.setValue(false);
			}
			mIcon.setValue(icon.getName());
			mIcon.setEnabled(mSetIcon.getBooleanValue());
		}
		finally {
			internalChange = false;
		}
	}

	private String transformEdgeWidth(final int edgeWidth) {
		if (edgeWidth == EdgeModel.DEFAULT_WIDTH) {
			return null;
		}
		final HashMap<String, Integer> transformator = getEdgeWidthTransformation();
		for (final Entry<String, Integer> transformatorEntry : transformator.entrySet()) {
			final Integer width = transformatorEntry.getValue();
			if (edgeWidth == width.intValue()) {
				return transformatorEntry.getKey();
			}
		}
		return null;
	}
	
	private void addListeners(final ModeController modeController) {
		final Controller controller = modeController.getController();
		final MapController mapController = modeController.getMapController();
		mapController.addNodeSelectionListener(new INodeSelectionListener() {
			public void onSelect(NodeModel node) {
				final IMapSelection selection = controller.getSelection();
				if(selection.size() == 1 ){
					setStyle(modeController, node);
				}
			}
			
			public void onDeselect(NodeModel node) {
			}
		});
		mapController.addNodeChangeListener(new INodeChangeListener() {
			public void nodeChanged(NodeChangeEvent event) {
				final IMapSelection selection = controller.getSelection();
				if(selection == null){
					return;
				}
				final NodeModel node = event.getNode();
				if(selection.getSelected().equals(node)){
					setStyle(modeController, node);
				}
			}
		});
	}

}
