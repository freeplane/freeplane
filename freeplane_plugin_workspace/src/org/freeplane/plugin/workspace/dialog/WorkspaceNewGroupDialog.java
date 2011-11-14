/**
 * author: Marcel Genzmehr
 * 14.11.2011
 */
package org.freeplane.plugin.workspace.dialog;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.freeplane.plugin.workspace.view.DialogCallback;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * 
 */
public class WorkspaceNewGroupDialog extends JDialog {

	private static final long serialVersionUID = 3900806255189377784L;

	private JTextField txtNodepath;
	private JTextField txtGroupname;
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	public WorkspaceNewGroupDialog(String title, final DialogCallback callback) {
		assert(callback != null);
		
		setTitle(title);
		setModal(true);
		getContentPane().setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("14dlu"),
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,}));
		
		JLabel lblPath = new JLabel("Path:");
		lblPath.setEnabled(false);
		lblPath.setHorizontalAlignment(SwingConstants.RIGHT);
		getContentPane().add(lblPath, "2, 2, right, default");
		
		txtNodepath = new JTextField();
		txtNodepath.setEditable(false);
		txtNodepath.setEnabled(false);
		txtNodepath.setText("nodepath");
		getContentPane().add(txtNodepath, "4, 2, fill, default");
		txtNodepath.setColumns(10);
		
		JButton btnNewButton = new JButton("...");
		btnNewButton.setEnabled(false);
		getContentPane().add(btnNewButton, "6, 2");
		
		JLabel lblName = new JLabel("Name:");
		lblName.setHorizontalAlignment(SwingConstants.RIGHT);
		getContentPane().add(lblName, "2, 6, right, default");
		
		txtGroupname = new JTextField();
		txtGroupname.setText("GroupName");
		getContentPane().add(txtGroupname, "4, 6, 3, 1, fill, default");
		txtGroupname.setColumns(10);
		
		JPanel panel = new JPanel();
		getContentPane().add(panel, "2, 8, 5, 1, fill, fill");
		panel.setLayout(new FormLayout(new ColumnSpec[] {
				ColumnSpec.decode("4dlu:grow"),
				ColumnSpec.decode("50dlu"),
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("50dlu"),},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,}));
		
		JButton btnOk = new JButton("OK");
		btnOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {		
				callback.callback(e);
			}
		});
		panel.add(btnOk, "2, 2");
		
		JButton btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener() {			
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		panel.add(btnCancel, "4, 2");
	}
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
}
