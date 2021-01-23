package org.freeplane.view.swing.features.filepreview;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;

import org.freeplane.core.ui.LabelAndMnemonicSetter;
import org.freeplane.core.util.TextUtils;

public class MindMapPreviewWithOptions extends Box{

	private static final long serialVersionUID = 1L;
	private JCheckBox follow;
	private MindMapPreview preview;

	public MindMapPreviewWithOptions(JFileChooser fileChooser, boolean selectFollow) {
		this(new MindMapPreview(fileChooser), selectFollow);
	}
	private MindMapPreviewWithOptions(MindMapPreview preview, boolean selectFollow) {
		super(BoxLayout.Y_AXIS);
		this.preview = preview;
		preview.setAlignmentX(LEFT_ALIGNMENT);
		add(preview);
		follow = new JCheckBox();
		follow.setSelected(selectFollow);
		LabelAndMnemonicSetter.setLabelAndMnemonic(follow, TextUtils.getRawText("followMindMap"));
		follow.setAlignmentX(LEFT_ALIGNMENT);
		add(follow);
	}

	public boolean isFollowChecked() {
		return follow.isSelected();
	}
}
