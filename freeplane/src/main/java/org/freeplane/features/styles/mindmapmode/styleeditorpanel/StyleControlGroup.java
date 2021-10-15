package org.freeplane.features.styles.mindmapmode.styleeditorpanel;

import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.resources.TranslatedObject;
import org.freeplane.core.ui.components.InfoArea;
import org.freeplane.core.ui.components.JComboBoxWithBorder;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.ui.textchanger.TranslatedElement;
import org.freeplane.core.ui.textchanger.TranslatedElementFactory;
import org.freeplane.core.util.HtmlUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.edge.AutomaticEdgeColor;
import org.freeplane.features.edge.AutomaticEdgeColorHook;
import org.freeplane.features.edge.EdgeController;
import org.freeplane.features.edge.mindmapmode.MEdgeController;
import org.freeplane.features.map.IMapChangeListener;
import org.freeplane.features.map.MapChangeEvent;
import org.freeplane.features.map.MapController;
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
import org.freeplane.features.styles.MapStyleModel;
import org.freeplane.features.styles.mindmapmode.MLogicalStyleController;
import org.freeplane.features.styles.mindmapmode.MUIFactory;
import org.freeplane.features.styles.mindmapmode.ManageMapConditionalStylesAction;
import org.freeplane.features.styles.mindmapmode.ManageNodeConditionalStylesAction;
import org.freeplane.features.url.mindmapmode.TemplateManager;
import org.freeplane.view.swing.features.filepreview.MindMapPreviewWithOptions;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormSpecs;
class StyleControlGroup implements ControlGroup{
    private static final int LEFT_MARGIN = (int) (15 * UITools.FONT_SCALE_FACTOR);
    private static final int GAP = (int) (6 * UITools.FONT_SCALE_FACTOR);
    private static final String CHOOSE_TEMPLATE = "choose_template";
    private static final String REDEFINE_STYLE = "change_style";
    private static final String FOR_THIS_MAP = "changes_style_for_this_map";
    private static final String FOR_TEMPLATE = "changes_template_style";
    private static final String FOR_TEMPLATE_TOOLTIP =FOR_TEMPLATE + ".tooltip";
    private boolean internalChange;
	private RevertingProperty mSetStyle;
	private JButton mNodeStyleButton;
	private JButton mMapStyleButton;
	private JLabel styleName;
	private JRadioButton redefinesStyleForCurrentMapOnly;
    private JRadioButton redefinesStyleForCurrentMapAndTemplate;
    private JTextArea redefinedTemplate;
    private JButton associateTemplateButton;
	private final boolean addStyleBox;
	private JComboBox mAutomaticLayoutComboBox;
	private JComboBox mAutomaticEdgeColorComboBox;
	private JButton mEditEdgeColorsBtn;
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
			RevertingProperty isSet = (RevertingProperty) evt.getSource();
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
	            styleName.setText(firstStyle.toString());
	            updateTemplateName(node.getMap());

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
            addStyleBox(formBuilder);
        }
		mNodeStyleButton = addButton(formBuilder, "actual_node_styles", modeController.getAction(ManageNodeConditionalStylesAction.NAME));
		if (addStyleBox) {
			mMapStyleButton = addButton(formBuilder, "actual_map_styles", modeController.getAction(ManageMapConditionalStylesAction.NAME));

            addAutomaticLayout(formBuilder);

            styleName = new JLabel();
            styleName.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            redefinesStyleForCurrentMapOnly = new JRadioButton();
            redefinesStyleForCurrentMapOnly.setSelected(true);
            redefinesStyleForCurrentMapOnly.setText(TextUtils.getRawText(FOR_THIS_MAP));
            redefinesStyleForCurrentMapOnly.setAlignmentX(Component.LEFT_ALIGNMENT);
            
            redefinesStyleForCurrentMapAndTemplate = new JRadioButton(); 
            redefinesStyleForCurrentMapAndTemplate.setText(TextUtils.getText(FOR_TEMPLATE));
            redefinesStyleForCurrentMapAndTemplate.setAlignmentX(Component.LEFT_ALIGNMENT);
            
            redefinedTemplate= new InfoArea();
            redefinedTemplate.setColumns(80);
            redefinedTemplate.setLineWrap(true);
            redefinedTemplate.setWrapStyleWord(false);
            redefinedTemplate.setBorder(BorderFactory.createEmptyBorder(0, LEFT_MARGIN, GAP, 0));
            redefinedTemplate.setFont(redefinedTemplate.getFont().deriveFont(Font.ITALIC));
            redefinedTemplate.setAlignmentX(Component.LEFT_ALIGNMENT);
            
            associateTemplateButton = new JButton(TextUtils.getRawText(CHOOSE_TEMPLATE));
            associateTemplateButton.setAlignmentX(Component.LEFT_ALIGNMENT);
            associateTemplateButton.addActionListener(new ActionListener() {
                
                @Override
                public void actionPerformed(ActionEvent e) {
                    MapModel map = Controller.getCurrentController().getMap();
                    if(map == null)
                        return;
                    MindMapPreviewWithOptions previewWithOptions = MindMapPreviewWithOptions.createFileOpenDialogAndOptions(
                            TextUtils.getText("select_associated_template")
                    );
                    JFileChooser fileChooser = previewWithOptions.getFileChooser();
                    final int returnVal = fileChooser.showOpenDialog(UITools.getCurrentFrame());
                    if (returnVal != JFileChooser.APPROVE_OPTION) {
                        return;
                    }
                    File file = fileChooser.getSelectedFile();
                    try {
                        if(file == null || ! file.exists()){
                            return;
                        } else if (map.getFile() != null && 
                                file.getCanonicalFile().equals(map.getFile().getCanonicalFile())) {
                            MapStyle.getController().setProperty(map, MapStyleModel.ASSOCIATED_TEMPLATE_LOCATION_PROPERTY,
                                    null);
                            return;
                        }
                    } catch (IOException e1) {
                        LogUtils.warn(e1);
                        return;
                    }
                    MapStyle.getController().setProperty(map, MapStyleModel.ASSOCIATED_TEMPLATE_LOCATION_PROPERTY,
                            TemplateManager.INSTANCE.normalizeTemplateLocation(file.toURI()).toString());

                }
            });

            
            ButtonGroup redefineStyleButtonGroup = new ButtonGroup();
            redefineStyleButtonGroup.add(redefinesStyleForCurrentMapOnly);
            redefineStyleButtonGroup.add(redefinesStyleForCurrentMapAndTemplate);
            
            Box styleAndButtonBox = Box.createVerticalBox();
            Box buttonBox = Box.createHorizontalBox();
            buttonBox.setAlignmentX(Component.CENTER_ALIGNMENT);
            Box radioButtonBox = Box.createVerticalBox();
            radioButtonBox.add(redefinesStyleForCurrentMapOnly);
            radioButtonBox.add(redefinesStyleForCurrentMapAndTemplate);
            radioButtonBox.add(redefinedTemplate);
            Box associateTemplateButtonBox = Box.createHorizontalBox();
            associateTemplateButtonBox.setAlignmentX(Component.LEFT_ALIGNMENT);
            associateTemplateButtonBox.add(Box.createHorizontalStrut(LEFT_MARGIN));
            associateTemplateButtonBox.add(associateTemplateButton);
            radioButtonBox.add(associateTemplateButtonBox);
            radioButtonBox.setAlignmentX(Component.LEFT_ALIGNMENT);
            radioButtonBox.setBorder(BorderFactory.createEmptyBorder(0, 0, GAP, 0));
            buttonBox.add(radioButtonBox);
            
            JButton redefineStyleBtn = TranslatedElementFactory.createButton("ApplyAction.text");
            redefineStyleBtn.addActionListener(e -> {
                 final NodeModel node = Controller.getCurrentController().getSelection().getSelected();
                    MapStyle.getController().redefineStyle(node, redefinesStyleForCurrentMapAndTemplate.isSelected());

            });
            redefineStyleBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
            buttonBox.add(redefineStyleBtn);

            styleAndButtonBox.add(styleName);
            styleAndButtonBox.add(buttonBox);
            UITools.addTitledBorder(styleAndButtonBox, TextUtils.format(REDEFINE_STYLE, ""), StyleEditorPanel.FONT_SIZE);
            formBuilder.append(styleAndButtonBox, formBuilder.getColumnCount());
            formBuilder.nextLine();

            MapController mapController = Controller.getCurrentModeController().getMapController();
            mapController.addMapChangeListener(new IMapChangeListener(){
                @Override
                public void mapChanged(MapChangeEvent event) {
                    if(MapStyleModel.ASSOCIATED_TEMPLATE_LOCATION_PROPERTY.equals(event.getProperty())
                            && event.getMap().equals(Controller.getCurrentController().getMap())){
                        updateTemplateName(event.getMap());
                    }
                 }
                
            });
		}
	}

    private void updateTemplateName(MapModel map) {
        String templateLocation = MapStyle.getController().getProperty(map, MapStyleModel.ASSOCIATED_TEMPLATE_LOCATION_PROPERTY);
        String templateDescription = TemplateManager.INSTANCE.describeNormalizedLocation(templateLocation);
        redefinedTemplate.setText(templateDescription);
        redefinesStyleForCurrentMapAndTemplate.setToolTipText(
                TextUtils.format(FOR_TEMPLATE_TOOLTIP, templateDescription));
        if(templateLocation == null)
            redefinesStyleForCurrentMapOnly.setSelected(true);
        redefinesStyleForCurrentMapAndTemplate.setEnabled(templateLocation != null);
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
	    mSetStyle = new RevertingProperty();
		final StyleChangeListener listener = new StyleChangeListener();
		mSetStyle.addPropertyChangeListener(listener);
		formBuilder.append(new JLabel(TextUtils.getText("style")));
		formBuilder.append(mStyleBox);
		mSetStyle.appendToForm(formBuilder);
		formBuilder.nextLine();
	}
	private void addAutomaticLayout(final DefaultFormBuilder formBuilder) {
		addAutomaticLayoutControls(formBuilder);
		addEdgeColoringControls(formBuilder);
	}

	private void addAutomaticLayoutControls(final DefaultFormBuilder formBuilder) {
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
			formBuilder.append(mEditEdgeColorsBtn, formBuilder.getColumnCount());
			formBuilder.nextLine();
			
	}
	
	private void appendLabeledComponent(final DefaultFormBuilder formBuilder, String labelKey, Component component) {
		final String text = TextUtils.getText(labelKey);
	    final JLabel label = new JLabel(text);
		TranslatedElement.TEXT.setKey(label, labelKey);
		formBuilder.append(label, 1);
	    formBuilder.append(component, 3);
	    formBuilder.nextLine();
	}

}