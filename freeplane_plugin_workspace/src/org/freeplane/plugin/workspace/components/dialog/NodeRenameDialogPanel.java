/**
 * author: Marcel Genzmehr
 * 02.02.2012
 */
package org.freeplane.plugin.workspace.components.dialog;

import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import javax.swing.JCheckBox;

/**
 * 
 */
public class NodeRenameDialogPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private JTextField txtNodeName;
	private JCheckBox chckbxRenameLinkToo;
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	public NodeRenameDialogPanel(String oldName) {
		this(oldName,false);
	}
	
	public NodeRenameDialogPanel(String oldName, boolean isMutableNode) {
		setMinimumSize(new Dimension(240,120));
		setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("36px"),
				FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
				ColumnSpec.decode("86px:grow"),
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,}));
		
		JLabel lblName = new JLabel("name:");
		add(lblName, "3, 2, right, default");
		
		txtNodeName = new JTextField();
		txtNodeName.setText(oldName);
		txtNodeName.requestFocus();
		add(txtNodeName, "5, 2, fill, top");
		txtNodeName.setColumns(10);
		
		chckbxRenameLinkToo = new JCheckBox("apply changes to the linked file");
		if(isMutableNode) {			
			add(chckbxRenameLinkToo, "5, 4");
		}
	}
	
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/
	
	public String getText() {
		return txtNodeName.getText();
	}
	
	public boolean applyChangesForLink() {
		return chckbxRenameLinkToo.isSelected();
	}
	
	public void setCheckboxSelected(boolean selected) {
		chckbxRenameLinkToo.setSelected(selected);
	}

}
