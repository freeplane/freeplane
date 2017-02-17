/**
 * author: Marcel Genzmehr
 * 14.11.2011
 */
package org.freeplane.plugin.workspace.components.dialog;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;
import org.freeplane.plugin.workspace.URIUtils;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;

import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * 
 */
public class WorkspaceNewFolderPanel extends JPanel implements ActionListener {

	public static final int MODE_VIRTUAL_ONLY = 1;
	public static final int MODE_PHYSICAL_ONLY = 2;
	public static final int MODE_VIRTUAL_PHYSICAL = MODE_VIRTUAL_ONLY|MODE_PHYSICAL_ONLY;
	public static final int VIRTUAL = 1;
	public static final int PHYSICAL = 2;
	
	private static final long serialVersionUID = 3900806255189377784L;
	
	
	
	private JCheckBox chbkLinkFolder;
	private JTextField textField;
	private JLabel lblFolder;
	private JButton button;
	JLabel lblName;
	private JTextField txtFoldername;
	
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	public WorkspaceNewFolderPanel(int chooserMode, final AWorkspaceTreeNode targetNode) {
		setMinimumSize(new Dimension(320, 160));
		setPreferredSize(new Dimension(320, 160));
		setLayout(new FormLayout(new ColumnSpec[] {
				FormSpecs.RELATED_GAP_COLSPEC,
				FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC,
				FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.LABEL_COMPONENT_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormSpecs.RELATED_GAP_COLSPEC,
				FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC,},
			new RowSpec[] {
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,}));
		
		JLabel lblPath = new JLabel(TextUtils.getText("workspace.action.node.new.folder.dialog.parent.label"));
		lblPath.setHorizontalAlignment(SwingConstants.RIGHT);
		add(lblPath, "2, 2, right, default");
		
		JLabel lblParentpath = new JLabel("/");
		add(lblParentpath, "4, 2, 3, 1");
		lblParentpath.setText(getParentPath(targetNode));
		
		{
			
			lblName = new JLabel(TextUtils.getText("workspace.action.node.new.folder.dialog.input1.label"));
			lblName.setHorizontalAlignment(SwingConstants.RIGHT);
			add(lblName, "2, 4, right, default");
			
			txtFoldername = new JTextField();
			txtFoldername.setText(TextUtils.getText("workspace.action.node.new.folder.dialog.input1.default"));
			add(txtFoldername, "4, 4, 5, 1, fill, default");
		}
		
		JSeparator separator = new JSeparator();
		add(separator, "2, 6, 7, 1");
		
		{
			chbkLinkFolder = new JCheckBox(TextUtils.getText("workspace.action.node.new.folder.dialog.disk.label"));
			chbkLinkFolder.addActionListener(this);
			chbkLinkFolder.setEnabled((chooserMode&MODE_PHYSICAL_ONLY) > 0);
			chbkLinkFolder.setSelected((chooserMode&MODE_VIRTUAL_ONLY) == 0);
			add(chbkLinkFolder, "2, 8, 7, 1");
			
			lblFolder = new JLabel(TextUtils.getText("workspace.action.node.new.folder.dialog.input2.label"));
			add(lblFolder, "2, 11, 3, 1, right, default");
			
			textField = new JTextField();
			add(textField, "6, 11, fill, default");
			
			button = new JButton(new AbstractAction("...") {
				
				private static final long serialVersionUID = 1L;
	
				public void actionPerformed(ActionEvent e) {
					JFileChooser chooser = new JFileChooser(URIUtils.getAbsoluteFile(WorkspaceController.getCurrentProject().getProjectHome()));
					chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					chooser.setFileHidingEnabled(true);
					int result = chooser.showOpenDialog(UITools.getFrame());
					if(result == JFileChooser.APPROVE_OPTION) {
						File oldPath = new File("");
						try {
							oldPath = new File(textField.getText());
						}
						catch (Exception ex) {
						}
						textField.setText(chooser.getSelectedFile().getPath());
						String folderName = txtFoldername.getText();
						if (folderName.length()==0 || folderName.equals(TextUtils.getText("workspace.action.node.new.folder.dialog.input1.default")) || oldPath.getName().equals(folderName) ) {
							txtFoldername.setText(chooser.getSelectedFile().getName());
						}
					}
				}
			});
			add(button, "8, 11");
			
			enablePhysicalInput(chbkLinkFolder.isSelected());
		}
	}
	
	/**
	 * @param targetNode
	 * @return
	 */
	private String getParentPath(AWorkspaceTreeNode targetNode) {
		String name = "";
		if(targetNode.getParent() != null) {
			name += getParentPath(targetNode.getParent());
		}
		name += "/" + targetNode.getName();
		return name;
	}
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	public String getFolderName() {
		return txtFoldername.getText();
	}
	
	public String getLinkPath() {
		if(isLinkedFolder()) {
			return textField.getText();
		} 
		
		return null;
	}
	
	public boolean isLinkedFolder() {
		return chbkLinkFolder.isSelected();
	}
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/

	
	public void actionPerformed(ActionEvent e) {
		if(e.getSource().equals(chbkLinkFolder)) {
			if(chbkLinkFolder.isSelected()) {
				enablePhysicalInput(true);
			} else {
				enablePhysicalInput(false);
			}
						
		}	
	}
	
	private void enablePhysicalInput(boolean enabled) {
		lblFolder.setEnabled(enabled);
		textField.setEnabled(enabled);
		button.setEnabled(enabled);
	}
}
