/**
 * author: Marcel Genzmehr
 * 14.11.2011
 */
package org.freeplane.plugin.workspace.components.dialog;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.freeplane.core.ui.components.UITools;
import org.freeplane.plugin.workspace.WorkspaceUtils;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;

import com.jgoodies.forms.factories.FormFactory;
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
	
	
	
	private JRadioButton rdbtnNewRadioButton;
	private JTextField textField;
	private JLabel lblFolder;
	private JButton button;
	
	private JRadioButton rdbtnVirtual;
	JLabel lblName;
	private JTextField txtGroupname;
	
	
	
	private int chooserType = 0;
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	public WorkspaceNewFolderPanel(int chooserMode, final AWorkspaceTreeNode targetNode) {
		setMinimumSize(new Dimension(320, 160));
		setPreferredSize(new Dimension(320, 160));
		setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,}));
		
		JLabel lblPath = new JLabel("Parent:");
		lblPath.setHorizontalAlignment(SwingConstants.RIGHT);
		add(lblPath, "2, 2, right, default");
		
		JLabel lblParentpath = new JLabel("parentPath");
		add(lblParentpath, "4, 2, 3, 1");
		lblParentpath.setText(getParentPath(targetNode));
		
		{
			rdbtnVirtual = new JRadioButton("Virtual");
			rdbtnVirtual.addActionListener(this);
			rdbtnVirtual.setSelected((chooserMode&MODE_VIRTUAL_ONLY) > 0);
			rdbtnVirtual.setEnabled((chooserMode&MODE_VIRTUAL_ONLY) > 0);
			add(rdbtnVirtual, "2, 4, 7, 1");			
			
			lblName = new JLabel("Name:");
			lblName.setHorizontalAlignment(SwingConstants.RIGHT);
			add(lblName, "2, 6, 3, 1, right, default");
			
			txtGroupname = new JTextField();
			txtGroupname.setText("folder");
			add(txtGroupname, "6, 6, 3, 1, fill, default");
			
			enableVirtualInput(rdbtnVirtual.isSelected());
		}
		
		JSeparator separator = new JSeparator();
		add(separator, "2, 8, 7, 1");
		
		{
			rdbtnNewRadioButton = new JRadioButton("Choose from disk");
			rdbtnNewRadioButton.addActionListener(this);
			rdbtnNewRadioButton.setEnabled((chooserMode&MODE_PHYSICAL_ONLY) > 0);
			rdbtnNewRadioButton.setSelected((chooserMode&MODE_VIRTUAL_ONLY) == 0);
			add(rdbtnNewRadioButton, "2, 10, 7, 1");
			
			lblFolder = new JLabel("Folder:");
			add(lblFolder, "2, 13, 3, 1, right, default");
			
			textField = new JTextField();
			add(textField, "6, 13, fill, default");
			
			button = new JButton(new AbstractAction("...") {
				
				private static final long serialVersionUID = 1L;
	
				public void actionPerformed(ActionEvent e) {
					JFileChooser chooser = new JFileChooser(WorkspaceUtils.getWorkspaceBaseFile());
					chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					chooser.setFileHidingEnabled(true);
					int result = chooser.showOpenDialog(UITools.getFrame());
					if(result == JFileChooser.APPROVE_OPTION) {
						textField.setText(chooser.getSelectedFile().getPath());
					}
				}
			});
			add(button, "8, 13");
			
			enablePhysicalInput(rdbtnNewRadioButton.isSelected());
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

	public String getFolderString() {
		if(chooserType == MODE_VIRTUAL_ONLY) {
			return txtGroupname.getText();
		}
		if(chooserType == MODE_PHYSICAL_ONLY) {
			return textField.getText();
		}
		return null;
	}
	
	public int getChoosenType() {
		return chooserType;
	}
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/

	
	public void actionPerformed(ActionEvent e) {
		if(e.getSource().equals(rdbtnNewRadioButton)) {
			this.chooserType = PHYSICAL;
			rdbtnNewRadioButton.setSelected(true);
			rdbtnVirtual.setSelected(false);
			enablePhysicalInput(true);
			enableVirtualInput(false);			
		}
		else if(e.getSource().equals(rdbtnVirtual)) {
			this.chooserType = VIRTUAL;
			rdbtnNewRadioButton.setSelected(false);
			rdbtnVirtual.setSelected(true);
			enablePhysicalInput(false);
			enableVirtualInput(true);
		}	
	}
	
	private void enablePhysicalInput(boolean enabled) {
		lblFolder.setEnabled(enabled);
		textField.setEnabled(enabled);
		button.setEnabled(enabled);
	}
	
	private void enableVirtualInput(boolean enabled) {
		lblName.setEnabled(enabled);
		txtGroupname.setEnabled(enabled);
	}
}
