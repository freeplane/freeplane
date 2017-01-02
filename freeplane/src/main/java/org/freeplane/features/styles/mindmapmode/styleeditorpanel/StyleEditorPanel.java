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
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.plaf.basic.BasicButtonUI;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.resources.TranslatedObject;
import org.freeplane.core.resources.components.BooleanProperty;
import org.freeplane.core.resources.components.IPropertyControl;
import org.freeplane.core.resources.components.SeparatorProperty;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.JComboBoxWithBorder;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.ui.textchanger.TranslatedElement;
import org.freeplane.core.util.HtmlUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.edge.AutomaticEdgeColor;
import org.freeplane.features.edge.AutomaticEdgeColorHook;
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

	/**
	* 
	*/
	private static final long serialVersionUID = 1L;
	
	private boolean internalChange;
	private List<IPropertyControl> mControls;

	ControlGroup [] controlGroups = {
			new GroupSeparator("OptionPanel.separator.NodeColors"),
			new NodeColorControlGroup(),
			new GroupSeparator("OptionPanel.separator.NodeText"),
			new FormatControlGroup(),
			new NodeNumberingControlGroup(),
			new GroupSeparator("OptionPanel.separator.NodeShape"),
			new NodeShapeControlGroup(),
			new MinNodeWidthControlGroup(),
			new MaxNodeWidthControlGroup(),
			new ChildDistanceControlGroup(),
			new GroupSeparator("OptionPanel.separator.NodeBorder"),
			new BorderWidthAndBorderWidthMatchesEdgeControlGroup(),
			new BorderDashAndDashMatchesEdgeControlGroup(),
			new BorderColorAndColorMatchesEdgeControlGroup(),
			new NextLineControlGroup(),
			new GroupSeparator("OptionPanel.separator.NodeFont"),
			new FontNameControlGroup(),
			new FontSizeControlGroup(),
			new FontBoldControlGroup(),
			new FontItalicControlGroup(),
			new NodeTextAlignmentControlGroup(),
			new NodeFontHyperLinkControlGroup(),
			new NextLineControlGroup(),
			new GroupSeparator("OptionPanel.separator.EdgeControls"),
			new EdgeWidthControlGroup(),
			new EdgeDashControlGroup(),
			new EdgeStyleControlGroup(),
			new EdgeColorControlGroup(),
			new NodeBackgroundColorControlGroup(),
			new NextLineControlGroup(),
			new GroupSeparator("OptionPanel.separator.CloudControls"),
			new CloudColorShapeControlGroup()
	};
	
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

	/**
	 * Creates all controls and adds them to the frame.
	 * @param modeController 
	 */
	private void init() {
		if(mControls != null)
			return;
		final String form = "right:max(20dlu;p), 2dlu, p, 1dlu,right:max(20dlu;p), 4dlu, 80dlu, 7dlu";
		final FormLayout rightLayout = new FormLayout(form, "");
		final DefaultFormBuilder formBuilder = new DefaultFormBuilder(rightLayout);
		formBuilder.border(Paddings.DLU2);
		new SeparatorProperty("OptionPanel.separator.NodeStyle").layout(formBuilder);
		if (addStyleBox) {
			addAutomaticLayout(formBuilder);
			addStyleBox(formBuilder);
		}
		mNodeStyleButton = addStyleButton(formBuilder, "actual_node_styles", modeController.getAction(ManageNodeConditionalStylesAction.NAME));
		if (addStyleBox) {
			mMapStyleButton = addStyleButton(formBuilder, "actual_map_styles", modeController.getAction(ManageMapConditionalStylesAction.NAME));
		}
		final List<IPropertyControl> controls = new ArrayList<IPropertyControl>();
		
		for (ControlGroup controlGroup :controlGroups) {
			controlGroup.addControlGroup(controls, formBuilder);
		}
		mControls = controls;
		for (final IPropertyControl control : mControls) {
			control.layout(formBuilder);
		}
		add(formBuilder.getPanel(), BorderLayout.CENTER);
		addListeners();
		setFont(this, FONT_SIZE);
	}

	private JButton addStyleButton(DefaultFormBuilder formBuilder, String label, AFreeplaneAction action) {
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
	    formBuilder.append(button, formBuilder.getColumnCount());
		formBuilder.nextLine();
		return button;
    }

	private void addStyleBox(final DefaultFormBuilder formBuilder) {
	    mStyleBox = uiFactory.createStyleBox();
	    mSetStyle = new BooleanProperty(ControlGroup.SET_RESOURCE);
		final StyleChangeListener listener = new StyleChangeListener();
		mSetStyle.addPropertyChangeListener(listener);
		mSetStyle.layout(formBuilder);
	    formBuilder.append(new JLabel(TextUtils.getText("style")));
	    formBuilder.append(mStyleBox);
	    formBuilder.nextLine();
    }

	private JComboBox mAutomaticLayoutComboBox;
	private JComboBox mAutomaticEdgeColorComboBox;
	private Container mStyleBox;
	private void addAutomaticLayout(final DefaultFormBuilder formBuilder) {
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
	    appendLabeledComponent(formBuilder, "AutomaticLayoutAction.text", mAutomaticLayoutComboBox);
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
			appendLabeledComponent(formBuilder, "AutomaticEdgeColorHookAction.text", mAutomaticEdgeColorComboBox);
		}
	}

	private void appendLabeledComponent(final DefaultFormBuilder formBuilder, String labelKey, Component component) {
		final String text = TextUtils.getText(labelKey);
	    final JLabel label = new JLabel(text);
		TranslatedElement.TEXT.setKey(label, labelKey);
		formBuilder.append(label, 5);
	    formBuilder.append(component);
	    formBuilder.nextLine();
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

			for (int i=0; i<controlGroups.length; i++) {
				controlGroups[i].setStyle(node);
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

}
