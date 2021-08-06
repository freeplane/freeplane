package org.freeplane.features.url.mindmapmode;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.LabelAndMnemonicSetter;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;
import org.freeplane.view.swing.features.filepreview.MindMapPreview;

class TemplateChooser {
	final private JCheckBox follow;
	private Box verticalBox;
	private JCheckBox mDontShowAgainBox;
	private JComboBox<String> templateComboBox;
	private static final TemplateManager templateManager = TemplateManager.INSTANCE;


	public TemplateChooser() {
        final ResourceController resourceController = ResourceController.getResourceController();
		final String standardTemplatePath = resourceController.getProperty(MFileManager.STANDARD_TEMPLATE);
		final TreeSet<String> availableMapTemplates = templateManager.collectAvailableMapTemplates();
		availableMapTemplates.add(standardTemplatePath);
		verticalBox = Box.createVerticalBox();
		follow = new JCheckBox();
		LabelAndMnemonicSetter.setLabelAndMnemonic(follow, TextUtils.getRawText("followMindMap"));
		follow.setAlignmentX(Component.LEFT_ALIGNMENT);
		follow.setSelected(false);
		verticalBox.add(follow);

		MindMapPreview mindMapPreview = new MindMapPreview();
		templateComboBox = new JComboBox<>(new Vector<>(availableMapTemplates));
		templateComboBox.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED) {
					mindMapPreview.updateView(templateManager.existingTemplateFile(e.getItem().toString()));
				}
			}
		});
		templateComboBox.setSelectedItem(standardTemplatePath);
		templateComboBox.setAlignmentX(0f);
		verticalBox.add(templateComboBox);
		mindMapPreview.setAlignmentX(0f);
		verticalBox.add(mindMapPreview);
		final String checkBoxText = TextUtils.getRawText("OptionalDontShowMeAgainDialog.rememberMyDescision");
		mDontShowAgainBox = new JCheckBox();
		mDontShowAgainBox.setAlignmentX(0f);
		LabelAndMnemonicSetter.setLabelAndMnemonic(mDontShowAgainBox, checkBoxText);
		verticalBox.add(mDontShowAgainBox);
	}


	File chosenTemplateFile() {
		int option = JOptionPane.showConfirmDialog(UITools.getCurrentFrame(), verticalBox, TextUtils.getText("select_template"),
			JOptionPane.PLAIN_MESSAGE);
		final String selectedTemplate = (String) templateComboBox.getSelectedItem();
		if(option == JOptionPane.CLOSED_OPTION)
		    return null;
		if (mDontShowAgainBox.isSelected()) {
			final ResourceController resourceController = ResourceController.getResourceController();
			resourceController.setProperty("skip_template_selection", true);
			resourceController.setProperty("follow_mind_map_by_default", follow.isSelected());
			resourceController.setProperty(MFileManager.STANDARD_TEMPLATE, selectedTemplate);
		}
		return templateManager.existingTemplateFile(selectedTemplate);
	}

	public boolean isConnectChecked() {
		return follow.isSelected();
	}
}