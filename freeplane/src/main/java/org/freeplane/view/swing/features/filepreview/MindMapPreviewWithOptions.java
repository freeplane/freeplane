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

	public MindMapPreviewWithOptions(JFileChooser fileChooser, boolean selectFollow) {
		this(new MindMapPreview(fileChooser), selectFollow);
	}
	private MindMapPreviewWithOptions(MindMapPreview preview, boolean selectFollow) {
		super(BoxLayout.Y_AXIS);
		preview.setAlignmentX(LEFT_ALIGNMENT);
		add(preview);
		follow = new JCheckBox();
		follow.setSelected(selectFollow);
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
}
