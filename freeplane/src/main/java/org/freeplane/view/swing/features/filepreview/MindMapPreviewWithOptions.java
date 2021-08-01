package org.freeplane.view.swing.features.filepreview;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;

import org.freeplane.core.ui.LabelAndMnemonicSetter;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;

public class MindMapPreviewWithOptions extends Box{

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
        LabelAndMnemonicSetter.setLabelAndMnemonic(associate, TextUtils.getRawText("associateMindMap"));
        associate.setAlignmentX(LEFT_ALIGNMENT);
        Box checkboxes = Box.createHorizontalBox();
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
