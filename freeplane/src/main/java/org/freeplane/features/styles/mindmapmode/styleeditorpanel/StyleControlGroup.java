package org.freeplane.features.styles.mindmapmode.styleeditorpanel;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.resources.TranslatedObject;
import org.freeplane.core.resources.components.BooleanProperty;
import org.freeplane.core.ui.components.JComboBoxWithBorder;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.ui.textchanger.TranslatedElement;
import org.freeplane.core.ui.textchanger.TranslatedElementFactory;
import org.freeplane.core.util.HtmlUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.edge.AutomaticEdgeColor;
import org.freeplane.features.edge.AutomaticEdgeColorHook;
import org.freeplane.features.edge.EdgeController;
import org.freeplane.features.edge.mindmapmode.MEdgeController;
import org.freeplane.features.map.MapModel;
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
import org.freeplane.features.url.mindmapmode.MFileManager;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormSpecs;

class StyleControlGroup implements ControlGroup{
    private static final String REDEFINE_STYLE = "change_style";
    private static final String FOR_THIS_MAP = "changes_style_for_this_map";
    private static final String FOR_NEW_MAPS = "changes_style_for_new_maps";
    private static final String FOR_NEW_MAPS_TOOLTIP =FOR_NEW_MAPS + ".tooltip";
    private boolean internalChange;
	private BooleanProperty mSetStyle;
	private JButton mNodeStyleButton;
	private JButton mMapStyleButton;
    private JRadioButton redefinesStyleForCurrentMapAndTemplate;
	private final boolean addStyleBox;
	private JComboBox mAutomaticLayoutComboBox;
	private JComboBox mAutomaticEdgeColorComboBox;
	private JButton mEditEdgeColorsBtn;
	private Container mStyleBox;
	
	private final MUIFactory uiFactory;
	private final ModeController modeController;
	
	private static final TranslatedObject AUTOMATIC_LAYOUT_DISABLED = new TranslatedObject("automatic_layout_disabled");
    private TitledBorder redefineStyleBtnBorder;

	
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
	public void setStyle(NodeModel node, boolean canEdit) {
		internalChange = true;
		try {
			final LogicalStyleController logicalStyleController = LogicalStyleController.getController();
			if(addStyleBox){
				final boolean isStyleSet = LogicalStyleModel.getStyle(node) != null;
				mSetStyle.setValue(isStyleSet);
				setStyleList(mMapStyleButton, logicalStyleController.getMapStyleNames(node, "\n"));
	            IStyle firstStyle = logicalStyleController.getFirstStyle(node);
	            final String labelText = TextUtils.format(REDEFINE_STYLE, firstStyle);
	            redefineStyleBtnBorder.setTitle(labelText);;

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
				if(extension == null) {
					mAutomaticEdgeColorComboBox.setSelectedItem(AUTOMATIC_LAYOUT_DISABLED);
					mEditEdgeColorsBtn.setEnabled(false);
				} else {
					mAutomaticEdgeColorComboBox.setSelectedIndex(extension.rule.ordinal());
					mEditEdgeColorsBtn.setEnabled(canEdit);
				}
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
	public void addControlGroup(DefaultFormBuilder formBuilder) {
		if (addStyleBox) {
			addAutomaticLayout(formBuilder);
			addStyleBox(formBuilder);
		}
		mNodeStyleButton = addButton(formBuilder, "actual_node_styles", modeController.getAction(ManageNodeConditionalStylesAction.NAME));
		if (addStyleBox) {
			mMapStyleButton = addButton(formBuilder, "actual_map_styles", modeController.getAction(ManageMapConditionalStylesAction.NAME));
			JButton redefineStyleBtn = TranslatedElementFactory.createButton("ApplyAction.text");
			redefineStyleBtn.addActionListener(e -> {
			     final NodeModel node = Controller.getCurrentController().getSelection().getSelected();
			        MapStyle.getController().redefineStyle(node, redefinesStyleForCurrentMapAndTemplate.isSelected());

			});
			redefineStyleBtnBorder = UITools.addTitledBorder(redefineStyleBtn, "", StyleEditorPanel.FONT_SIZE);
			addComponent(formBuilder, redefineStyleBtn);

            JRadioButton redefinesStyleForCurrentMapOnly = new JRadioButton();
            redefinesStyleForCurrentMapOnly.setSelected(true);
            redefinesStyleForCurrentMapOnly.setText(TextUtils.getRawText(FOR_THIS_MAP));
            
            redefinesStyleForCurrentMapAndTemplate = new JRadioButton(); 
            updateTemplateName();
            
            ButtonGroup redefineStyleButtonGroup = new ButtonGroup();
            redefineStyleButtonGroup.add(redefinesStyleForCurrentMapOnly);
            redefineStyleButtonGroup.add(redefinesStyleForCurrentMapAndTemplate);
            formBuilder.append(redefinesStyleForCurrentMapOnly, 5);
            formBuilder.append(redefinesStyleForCurrentMapAndTemplate, 2);
            formBuilder.nextLine();

            ResourceController.getResourceController().addPropertyChangeListener((propertyName, newValue, oldValue) -> {
                if(! MFileManager.STANDARD_TEMPLATE.equals(propertyName))
                    return;
                updateTemplateName();
            });


		}
	}

    private void updateTemplateName() {
        String standardTemplate = ResourceController.getResourceController().getProperty(MFileManager.STANDARD_TEMPLATE);
        redefinesStyleForCurrentMapAndTemplate.setText(
                TextUtils.format(FOR_NEW_MAPS, standardTemplate));
        redefinesStyleForCurrentMapAndTemplate.setToolTipText(
                TextUtils.format(FOR_NEW_MAPS_TOOLTIP, standardTemplate));
    }
	private JButton addButton(DefaultFormBuilder formBuilder, String label, ActionListener action) {
	    final JButton button = addButton(formBuilder, action);
		TranslatedElement.BORDER.setKey(button, label);
		final String labelText = TextUtils.getText(label);
		UITools.addTitledBorder(button, labelText, StyleEditorPanel.FONT_SIZE);
		return button;
    }

    private JButton addButton(DefaultFormBuilder formBuilder, ActionListener action) {
        final JButton button = new JButton();
        button.setHorizontalAlignment(SwingConstants.LEFT);
	    addComponent(formBuilder, button);
		button.addActionListener(action);
        return button;
    }

    private void addComponent(DefaultFormBuilder formBuilder, final Component component) {
	    formBuilder.append(component, formBuilder.getColumnCount());
		formBuilder.nextLine();
    }

	private void addStyleBox(final DefaultFormBuilder formBuilder) {
	    mStyleBox = uiFactory.createStyleBox();
	    mSetStyle = new BooleanProperty(ControlGroup.SET_RESOURCE);
		final StyleChangeListener listener = new StyleChangeListener();
		mSetStyle.addPropertyChangeListener(listener);
		mSetStyle.appendToForm(formBuilder);
		formBuilder.append(new JLabel(TextUtils.getText("style")));
		formBuilder.append(mStyleBox);
		formBuilder.nextLine();
	}
	private void addAutomaticLayout(final DefaultFormBuilder formBuilder) {
		addStyleControls(formBuilder);
		addEdgeColoringControls(formBuilder);
	}

	private void addStyleControls(final DefaultFormBuilder formBuilder) {
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
		appendLabeledComponent(formBuilder, "AutomaticLayoutAction.text", mAutomaticLayoutComboBox);
	}

	private void addEdgeColoringControls(final DefaultFormBuilder formBuilder) {
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
		appendLabeledComponent(formBuilder, "AutomaticEdgeColorHookAction.text", mAutomaticEdgeColorComboBox);
		
			mEditEdgeColorsBtn= TranslatedElementFactory.createButton("editEdgeColors");
			mEditEdgeColorsBtn.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					final MEdgeController edgeController = (MEdgeController) modeController.getExtension(EdgeController.class);
					edgeController.editEdgeColorConfiguration(Controller.getCurrentController().getMap());
				}
			});
			formBuilder.appendLineGapRow();
			formBuilder.nextLine();
			formBuilder.appendRow(FormSpecs.PREF_ROWSPEC);
			formBuilder.setColumn(1);
			formBuilder.append(mEditEdgeColorsBtn, 7);
			formBuilder.nextLine();
			
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