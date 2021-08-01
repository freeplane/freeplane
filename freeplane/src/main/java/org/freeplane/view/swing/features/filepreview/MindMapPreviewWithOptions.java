package org.freeplane.view.swing.features.filepreview;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;

import org.freeplane.core.ui.LabelAndMnemonicSetter;
import org.freeplane.core.ui.components.JFreeplaneCustomizableFileChooser;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.url.mindmapmode.MFileManager;

public class MindMapPreviewWithOptions extends Box{
    public static MindMapPreviewWithOptions createFileOpenDialogAndOptions(String title) {
        final ModeController modeController = Controller.getCurrentModeController();
        final MFileManager fileManager = MFileManager.getController(modeController);
        JFreeplaneCustomizableFileChooser fileChooser = fileManager.getMindMapFileChooser();
        MindMapPreviewWithOptions previewWithOptions = new MindMapPreviewWithOptions(fileChooser);
        previewWithOptions.hideOptions();
        fileChooser.setAccessory(previewWithOptions);
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setDialogTitle(title);
        return previewWithOptions;
    }

	private static final long serialVersionUID = 1L;
    private final JCheckBox follow;
    private final JCheckBox associate;
    private final JFileChooser fileChooser;
    public MindMapPreviewWithOptions(JFileChooser fileChooser) {
		this(new MindMapPreview(fileChooser), fileChooser);
	}
	private MindMapPreviewWithOptions(MindMapPreview preview, JFileChooser fileChooser) {
		super(BoxLayout.Y_AXIS);
        this.fileChooser = fileChooser;
		preview.setAlignmentX(LEFT_ALIGNMENT);
		add(preview);
		follow = new JCheckBox();
		LabelAndMnemonicSetter.setLabelAndMnemonic(follow, TextUtils.getRawText("followMindMap"));
		follow.setAlignmentX(LEFT_ALIGNMENT);
        associate = new JCheckBox();
        associate.setSelected(true);
        LabelAndMnemonicSetter.setLabelAndMnemonic(associate, TextUtils.getRawText("associateUpdatedTemplate"));
        associate.setAlignmentX(LEFT_ALIGNMENT);
        Box checkboxes = Box.createHorizontalBox();
        checkboxes.setAlignmentX(LEFT_ALIGNMENT);
        checkboxes.add(follow);
        checkboxes.add(Box.createHorizontalStrut((int) (10 * UITools.FONT_SCALE_FACTOR)));
        checkboxes.add(associate);
        add(checkboxes);
	}
	
    public MindMapPreviewWithOptions selectFollows() {
        follow.setSelected(true);
        return this;
    }
    
    public MindMapPreviewWithOptions associateAlways() {
        associate.setSelected(true);
        associate.setEnabled(false);
        return this;
    }

    public boolean isFollowChecked() {
        return follow.isSelected();
    }

    public boolean isAssociateChecked() {
        return associate.isSelected();
    }
    
    public MindMapPreviewWithOptions hideOptions() {
        follow.setVisible(false);
        associate.setVisible(false);
        return this;
    }


    public JFileChooser getFileChooser() {
        return fileChooser;
    }
}
