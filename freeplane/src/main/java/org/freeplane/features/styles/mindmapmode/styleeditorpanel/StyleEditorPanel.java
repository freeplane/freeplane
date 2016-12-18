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
package org.freeplane.features.styles.mindmapmode.styleeditorpanel;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.freeplane.core.resources.components.IPropertyControl;
import org.freeplane.core.resources.components.NextColumnProperty;
import org.freeplane.core.resources.components.NextLineProperty;
import org.freeplane.core.resources.components.SeparatorProperty;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.JComboBoxWithBorder;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.ui.textchanger.TranslatedElement;
import org.freeplane.core.util.HtmlUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.cloud.CloudController;
import org.freeplane.features.cloud.CloudModel;
import org.freeplane.features.cloud.mindmapmode.MCloudController;
import org.freeplane.features.edge.AutomaticEdgeColor;
import org.freeplane.features.edge.AutomaticEdgeColorHook;
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
import org.freeplane.features.nodestyle.NodeStyleController;
import org.freeplane.features.nodestyle.NodeStyleModel;
import org.freeplane.features.nodestyle.NodeStyleModel.TextAlign;
import org.freeplane.features.nodestyle.mindmapmode.MNodeStyleController;
import org.freeplane.features.styles.AutomaticLayout;
import org.freeplane.features.styles.AutomaticLayoutController;
import org.freeplane.features.styles.IStyle;
import org.freeplane.features.styles.LogicalStyleController;
import org.freeplane.features.styles.LogicalStyleModel;
import org.freeplane.features.styles.MapStyle;
import org.freeplane.features.styles.mindmapmode.MLogicalStyleController;
import org.freeplane.features.styles.mindmapmode.MUIFactory;
import org.freeplane.features.styles.mindmapmode.ManageMapConditionalStylesAction;
import org.freeplane.features.styles.mindmapmode.ManageNodeConditionalStylesAction;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.Paddings;
import com.jgoodies.forms.layout.FormLayout;

public class StyleEditorPanel extends JPanel {
	private static final int FONT_SIZE = Math.round(UITools.FONT_SCALE_FACTOR * 8);
	private static final TranslatedObject AUTOMATIC_LAYOUT_DISABLED = new TranslatedObject("automatic_layout_disabled");


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
<<<<<<< HEAD
=======

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
>>>>>>> 8a2a6b6087202854f207918bef9d7a721552aa8d

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

//	private class NodeNumberingChangeListener extends ChangeListener {
//		public NodeNumberingChangeListener(final BooleanProperty mSet, final IPropertyControl mProperty) {
//			super(mSet, mProperty);
//		}
//
//		@Override
//		void applyValue(final boolean enabled, final NodeModel node, final PropertyChangeEvent evt) {
//			final MNodeStyleController styleController = (MNodeStyleController) Controller.getCurrentModeController()
//			    .getExtension(NodeStyleController.class);
//			styleController.setNodeNumbering(node, enabled ? mNodeNumbering.getBooleanValue() : null);
//		}
//	}
	
//	private class NodeFormatChangeListener extends ChangeListener {
//		public NodeFormatChangeListener(final BooleanProperty mSet, final IPropertyControl mProperty) {
//			super(mSet, mProperty);
//		}
//
//		@Override
//		void applyValue(final boolean enabled, final NodeModel node, final PropertyChangeEvent evt) {
//			final MNodeStyleController styleController = (MNodeStyleController) Controller.getCurrentModeController()
//			    .getExtension(NodeStyleController.class);
//			styleController.setNodeFormat(node, enabled ? mNodeFormat.getSelectedPattern() : null);
//		}
//	}

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
	private static final String CLOUD_SHAPE = "cloudshape";
	private static final String[] CLOUD_SHAPES = EnumToStringMapper.getStringValuesOf(CloudModel.Shape.class);
//	private static final String ICON = "icon";
	private static final String NODE_FONT_HYPERLINK = "nodefonthyperlink";
	private static final String TEXT_ALIGNMENT = "textalignment";
	private static final String[] TEXT_ALIGNMENTS = EnumToStringMapper.getStringValuesOf(TextAlign.class);
	/**
	* 
	*/
	private static final long serialVersionUID = 1L;
<<<<<<< HEAD
=======
	private static final String BORDER_COLOR_MATCHES_EDGE_COLOR = "border_color_matches_edge_color";
	private static final String BORDER_COLOR = "border_color";
	
>>>>>>> 8a2a6b6087202854f207918bef9d7a721552aa8d
	
	
	private boolean internalChange;
	private List<IPropertyControl> mControls;
	
	private BooleanProperty mSetCloud;
	private ColorProperty mCloudColor;
	private ComboProperty mCloudShape;

	@SuppressWarnings("serial")
	Map<String, ControlGroup> controlGroups = new HashMap<String, ControlGroup>() {{
		put(EdgeWidthControlGroup.EDGE_WIDTH, new EdgeWidthControlGroup());
		put(EdgeDashControlGroup.EDGE_DASH, new EdgeDashControlGroup());
		put(EdgeStyleControlGroup.EDGE_STYLE, new EdgeStyleControlGroup());
		put(EdgeColorControlGroup.EDGE_COLOR, new NodeColorControlGroup());
		put(NodeBackgroundColorControlGroup.NODE_BACKGROUND_COLOR, new NodeBackgroundColorControlGroup());
		put(NodeColorControlGroup.NODE_COLOR, new NodeColorControlGroup());
		put(FontBoldControlGroup.NODE_FONT_BOLD, new FontBoldControlGroup());
		put(FontItalicControlGroup.NODE_FONT_ITALIC, new FontItalicControlGroup());
		put(FontNameControlGroup.NODE_FONT_NAME, new FontNameControlGroup());
		put(FontSizeControlGroup.NODE_FONT_SIZE, new FontSizeControlGroup());
		put(FormatControlGroup.NODE_FORMAT, new FormatControlGroup());
		put(NodeNumberingControlGroup.NODE_NUMBERING, new NodeNumberingControlGroup());
		put(NodeShapeControlGroup.NODE_SHAPE, new NodeShapeControlGroup());
		put(MinNodeWidthControlGroup.MIN_NODE_WIDTH, new MinNodeWidthControlGroup());
		put(MaxNodeWidthControlGroup.MAX_NODE_WIDTH, new MaxNodeWidthControlGroup());
		put(ChildDistanceControlGroup.VERTICAL_CHILD_GAP, new ChildDistanceControlGroup());
		put(BorderWidthControlGroup.BORDER_WIDTH, new BorderWidthControlGroup());
		put(BorderDashControlGroup.BORDER_DASH, new BorderDashControlGroup());
<<<<<<< HEAD
		put(BorderColorControlGroup.BORDER_COLOR, new BorderColorControlGroup());
=======
>>>>>>> 8a2a6b6087202854f207918bef9d7a721552aa8d
	}};
	
	
	private BooleanProperty mSetNodeFontHyperlink;
	private BooleanProperty mNodeFontHyperlink;

<<<<<<< HEAD
=======
	private BooleanProperty mSetBorderColorMatchesEdgeColor;
	private BooleanProperty mBorderColorMatchesEdgeColor;
	
	private BooleanProperty mSetBorderColor;
	private ColorProperty mBorderColor;

>>>>>>> 8a2a6b6087202854f207918bef9d7a721552aa8d
	private BooleanProperty mSetNodeTextAlignment;
	private ComboProperty mNodeTextAlignment;

	
	private BooleanProperty mSetStyle;
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

	private void addCloudColorControl(final List<IPropertyControl> controls) {
		mSetCloud = new BooleanProperty(ControlGroup.SET_RESOURCE);
		controls.add(mSetCloud);
		mCloudColor = new ColorProperty(StyleEditorPanel.CLOUD_COLOR, ResourceController.getResourceController()
		    .getDefaultProperty(CloudController.RESOURCES_CLOUD_COLOR));
		controls.add(mCloudColor);
		final CloudColorChangeListener listener = new CloudColorChangeListener(mSetCloud, mCloudColor);
		mSetCloud.addPropertyChangeListener(listener);
		mCloudColor.addPropertyChangeListener(listener);
	}

	private void addCloudShapeControl(final List<IPropertyControl> controls) {
		mCloudShape = new ComboProperty(StyleEditorPanel.CLOUD_SHAPE, CLOUD_SHAPES);
		controls.add(mCloudShape);
		final CloudShapeChangeListener listener = new CloudShapeChangeListener(mSetCloud, mCloudShape);
		mSetCloud.addPropertyChangeListener(listener);
		mCloudShape.addPropertyChangeListener(listener);
	}

<<<<<<< HEAD
=======

	private void addBorderColorControl(final List<IPropertyControl> controls) {
		mSetBorderColor = new BooleanProperty(ControlGroup.SET_RESOURCE);
		controls.add(mSetBorderColor);
		mBorderColor = new ColorProperty(StyleEditorPanel.BORDER_COLOR, ColorUtils.colorToString(EdgeController.STANDARD_EDGE_COLOR));
		controls.add(mBorderColor);
		final BorderColorListener listener = new BorderColorListener(mSetBorderColor, mBorderColor);
		mSetBorderColor.addPropertyChangeListener(listener);
		mBorderColor.addPropertyChangeListener(listener);
	}
	
	private void addBorderColorMatchesEdgeColorControl(final List<IPropertyControl> controls) {
		mSetBorderColorMatchesEdgeColor = new BooleanProperty(ControlGroup.SET_RESOURCE);
		controls.add(mSetBorderColorMatchesEdgeColor);
		mBorderColorMatchesEdgeColor = new BooleanProperty(StyleEditorPanel.BORDER_COLOR_MATCHES_EDGE_COLOR);
		controls.add(mBorderColorMatchesEdgeColor);
		final BorderColorMatchesEdgeColorListener listener = new BorderColorMatchesEdgeColorListener(mSetBorderColorMatchesEdgeColor, mBorderColorMatchesEdgeColor);
		mSetBorderColorMatchesEdgeColor.addPropertyChangeListener(listener);
		mBorderColorMatchesEdgeColor.addPropertyChangeListener(listener);
	}

>>>>>>> 8a2a6b6087202854f207918bef9d7a721552aa8d
	private void addFontHyperlinkControl(final List<IPropertyControl> controls) {
		mSetNodeFontHyperlink = new BooleanProperty(ControlGroup.SET_RESOURCE);
		controls.add(mSetNodeFontHyperlink);
		mNodeFontHyperlink = new BooleanProperty(StyleEditorPanel.NODE_FONT_HYPERLINK);
		controls.add(mNodeFontHyperlink);
		final FontHyperlinkChangeListener listener = new FontHyperlinkChangeListener(mSetNodeFontHyperlink, mNodeFontHyperlink);
		mSetNodeFontHyperlink.addPropertyChangeListener(listener);
		mNodeFontHyperlink.addPropertyChangeListener(listener);
	}

	private void addNodeTextAlignmentControl(final List<IPropertyControl> controls) {
		mSetNodeTextAlignment = new BooleanProperty(ControlGroup.SET_RESOURCE);
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
		controlGroups.get(NodeColorControlGroup.NODE_COLOR).addControlGroup(controls);
		controls.add(new SeparatorProperty("OptionPanel.separator.NodeText"));
		controlGroups.get(FormatControlGroup.NODE_FORMAT).addControlGroup(controls);
		controlGroups.get(NodeNumberingControlGroup.NODE_NUMBERING).addControlGroup(controls);
		
		controls.add(new SeparatorProperty("OptionPanel.separator.NodeShape"));
		controlGroups.get(NodeShapeControlGroup.NODE_SHAPE).addControlGroup(controls);
		controlGroups.get(MinNodeWidthControlGroup.MIN_NODE_WIDTH).addControlGroup(controls);
		controlGroups.get(MaxNodeWidthControlGroup.MAX_NODE_WIDTH).addControlGroup(controls);
		controlGroups.get(ChildDistanceControlGroup.VERTICAL_CHILD_GAP).addControlGroup(controls);
		
		controls.add(new SeparatorProperty("OptionPanel.separator.NodeBorder"));
		controlGroups.get(BorderWidthControlGroup.BORDER_WIDTH).addControlGroup(controls);
		controlGroups.get(BorderDashControlGroup.BORDER_DASH).addControlGroup(controls);
		
<<<<<<< HEAD
		controlGroups.get(BorderColorControlGroup.BORDER_COLOR).addControlGroup(controls);
=======
		addBorderColorMatchesEdgeColorControl(controls);
		addBorderColorControl(controls);
		mBorderColorMatchesEdgeColor.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				enableOrDisableBorderColorControls();
			}
		});
>>>>>>> 8a2a6b6087202854f207918bef9d7a721552aa8d
		
		controls.add(new NextLineProperty());
		controls.add(new SeparatorProperty("OptionPanel.separator.NodeFont"));
		// to be added...  ControlGroup sep = new GroupSeparator("OptionPanel.separator.NodeFont");
		
		controlGroups.get(FontNameControlGroup.NODE_FONT_NAME).addControlGroup(controls);
		controlGroups.get(FontSizeControlGroup.NODE_FONT_SIZE).addControlGroup(controls);
		
		controlGroups.get(FontBoldControlGroup.NODE_FONT_BOLD).addControlGroup(controls);
		controlGroups.get(FontItalicControlGroup.NODE_FONT_ITALIC).addControlGroup(controls);
		addNodeTextAlignmentControl(controls);
		addFontHyperlinkControl(controls);
		controls.add(new NextLineProperty());
		controls.add(new SeparatorProperty("OptionPanel.separator.EdgeControls"));
		
		controlGroups.get(EdgeWidthControlGroup.EDGE_WIDTH).addControlGroup(controls);
		controlGroups.get(EdgeDashControlGroup.EDGE_DASH).addControlGroup(controls);
		controlGroups.get(EdgeStyleControlGroup.EDGE_STYLE).addControlGroup(controls);
		controlGroups.get(EdgeColorControlGroup.EDGE_COLOR).addControlGroup(controls);
		controlGroups.get(NodeBackgroundColorControlGroup.NODE_BACKGROUND_COLOR).addControlGroup(controls);
		
		controls.add(new NextLineProperty());
		controls.add(new SeparatorProperty("OptionPanel.separator.CloudControls"));
		addCloudColorControl(controls);
		controls.add(new NextLineProperty());
		controls.add(new NextColumnProperty(2));
		addCloudShapeControl(controls);
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
		rightBuilder.border(Paddings.DLU2);
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
	    mSetStyle = new BooleanProperty(ControlGroup.SET_RESOURCE);
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
<<<<<<< HEAD
=======
			final NodeBorderModel nodeBorderModel = NodeBorderModel.getModel(node);
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
>>>>>>> 8a2a6b6087202854f207918bef9d7a721552aa8d

			for (ControlGroup controlGroup : controlGroups.values()) {
				controlGroup.setStyle(node);
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

<<<<<<< HEAD
=======
	public void enableOrDisableBorderColorControls() {
		final boolean borderColorCanBeSet = ! mBorderColorMatchesEdgeColor.getBooleanValue();
		mSetBorderColor.setEnabled(borderColorCanBeSet);
		mBorderColor.setEnabled(borderColorCanBeSet);
	}
>>>>>>> 8a2a6b6087202854f207918bef9d7a721552aa8d
}
