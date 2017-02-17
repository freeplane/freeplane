package org.freeplane.plugin.workspace.components.dialog;

import java.io.File;
import java.util.Locale;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.freeplane.core.util.TextUtils;

import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class FileExistsDialogPanel extends JPanel {

	private static final long serialVersionUID = 5240830108148173268L;
	private JCheckBox chckbxUseForAll;

	public FileExistsDialogPanel(File targetFile, String message) {
		if(targetFile == null) {
			throw new IllegalArgumentException("NULL");
		}
		
		setLayout(new FormLayout(new ColumnSpec[] {
				FormSpecs.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),},
			new RowSpec[] {
				FormSpecs.RELATED_GAP_ROWSPEC,
				RowSpec.decode("fill:default:grow"),
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,}));
		
		JLabel lblNewLabel = new JLabel(TextUtils.format(message, targetFile.getName(), targetFile.getParent()));
		lblNewLabel.setVerticalAlignment(SwingConstants.TOP);
		add(lblNewLabel, "2, 2");
		
		chckbxUseForAll = new JCheckBox(TextUtils.getText(FileExistsDialogPanel.class.getSimpleName().toLowerCase(Locale.ENGLISH)+".apply2all"));
		add(chckbxUseForAll, "2, 4");
	}
	
	public boolean applyToAll() {
		return chckbxUseForAll.isSelected();
	}

}
