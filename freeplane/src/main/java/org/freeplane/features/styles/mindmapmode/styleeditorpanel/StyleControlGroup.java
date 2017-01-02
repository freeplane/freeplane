package org.freeplane.features.styles.mindmapmode.styleeditorpanel;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.plaf.basic.BasicButtonUI;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.resources.TranslatedObject;
import org.freeplane.core.resources.components.BooleanProperty;
import org.freeplane.core.resources.components.IPropertyControl;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.JComboBoxWithBorder;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.ui.textchanger.TranslatedElement;
import org.freeplane.core.util.HtmlUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.edge.AutomaticEdgeColor;
import org.freeplane.features.edge.AutomaticEdgeColorHook;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.styles.AutomaticLayout;
import org.freeplane.features.styles.AutomaticLayoutController;
import org.freeplane.features.styles.IStyle;
import org.freeplane.features.styles.LogicalStyleController;
import org.freeplane.features.styles.LogicalStyleModel;
import org.freeplane.features.styles.mindmapmode.MLogicalStyleController;
import org.freeplane.features.styles.mindmapmode.MUIFactory;
import org.freeplane.features.styles.mindmapmode.ManageMapConditionalStylesAction;
import org.freeplane.features.styles.mindmapmode.ManageNodeConditionalStylesAction;

import com.jgoodies.forms.builder.DefaultFormBuilder;

class StyleControlGroup implements ControlGroup{
	private boolean internalChange;
	private BooleanProperty mSetStyle;
	private JButton mNodeStyleButton;
	private JButton mMapStyleButton;
	private final boolean addStyleBox;
	private JComboBox mAutomaticLayoutComboBox;
	private JComboBox mAutomaticEdgeColorComboBox;
	private Container mStyleBox;
	
	private final MUIFactory uiFactory;
	private final ModeController modeController;
	
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

	
	public StyleControlGroup(boolean addStyleBox, MUIFactory uiFactory, ModeController modeController) {
		super();
		this.addStyleBox = addStyleBox;
		this.uiFactory = uiFactory;
		this.modeController = modeController;
	}

	@Override
	public void setStyle(NodeModel node) {
		internalChange = true;
		try {
			final LogicalStyleController logicalStyleController = LogicalStyleController.getController();
			if(addStyleBox){
				final boolean isStyleSet = LogicalStyleModel.getStyle(node) != null;
				mSetStyle.setValue(isStyleSet);
				setStyleList(mMapStyleButton, logicalStyleController.getMapStyleNames(node, "\n"));
			}
			setStyleList(mNodeStyleButton, logicalStyleController.getNodeStyleNames(node, "\n"));
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
		finally{
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

	@Override
	public void addControlGroup(List<IPropertyControl> controls, DefaultFormBuilder formBuilder) {
		if (addStyleBox) {
			addAutomaticLayout(formBuilder);
			addStyleBox(formBuilder);
		}
		mNodeStyleButton = addStyleButton(formBuilder, "actual_node_styles", modeController.getAction(ManageNodeConditionalStylesAction.NAME));
		if (addStyleBox) {
			mMapStyleButton = addStyleButton(formBuilder, "actual_map_styles", modeController.getAction(ManageMapConditionalStylesAction.NAME));
		}
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
	    UITools.addTitledBorder(button, labelText, StyleEditorPanel.FONT_SIZE);
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

}