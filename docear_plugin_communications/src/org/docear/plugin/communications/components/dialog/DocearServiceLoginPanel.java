package org.docear.plugin.communications.components.dialog;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.TextUtils;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import javax.swing.JCheckBox;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;

public class DocearServiceLoginPanel  extends JPanel {
	
	
	
	

	private static final long serialVersionUID = 1L;
	private JPasswordField password;
	private JTextField username;
	private JTextArea txtrLicense;
	private JButton okButton = null;
	private JCheckBox chckbxAcceptLicense;
	private JScrollPane scrollPane;
	
	public DocearServiceLoginPanel() {
		setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,}));
		
		JLabel lblUsername = new JLabel(TextUtils.getText("docear.service.connect.username.label"));
		add(lblUsername, "2, 2, right, default");
		
		username = new JTextField(ResourceController.getResourceController().getProperty("docear.service.connect.username", ""));
		add(username, "4, 2, fill, default");
		username.setColumns(10);
		
		JLabel lblPassword = new JLabel(TextUtils.getText("docear.service.connect.password.label"));
		add(lblPassword, "2, 4, right, default");
		
		password = new JPasswordField();
		add(password, "4, 4, fill, default");
		
		scrollPane = new JScrollPane();
		add(scrollPane, "4, 6, fill, fill");
		
		txtrLicense = new JTextArea();
		scrollPane.setViewportView(txtrLicense);
		txtrLicense.setRows(10);
		txtrLicense.setEditable(false);
		txtrLicense.setText("license");
		
		chckbxAcceptLicense = new JCheckBox("accept license");
		chckbxAcceptLicense.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(okButton != null) {
					okButton.setEnabled(chckbxAcceptLicense.isSelected());
				}				
			}
		});
		add(chckbxAcceptLicense, "4, 8");
	}
	
	public String getUsername() {
		return username.getText();
	}
	
	public String getPassword() {
		return new String(password.getPassword());
	}

	
	public void ctrlOKButton(JButton button) {
		okButton = button;
		if(okButton != null) {
			okButton.setEnabled(chckbxAcceptLicense.isSelected());
		}
	}
	
	public void setLicenseText(String text) {
		txtrLicense.setText(text);
		txtrLicense.setSelectionStart(0);
		txtrLicense.setSelectionEnd(0);
	}

}
