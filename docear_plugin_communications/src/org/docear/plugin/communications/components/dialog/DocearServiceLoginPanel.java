package org.docear.plugin.communications.components.dialog;

import java.awt.AWTEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.TextUtils;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class DocearServiceLoginPanel  extends JPanel implements KeyListener {
	
	
	
	

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
		username.addKeyListener(this);
		add(username, "4, 2, fill, default");
		username.setColumns(10);
		
		
		JLabel lblPassword = new JLabel(TextUtils.getText("docear.service.connect.password.label"));
		add(lblPassword, "2, 4, right, default");
		
		password = new JPasswordField();
		password.addKeyListener(this);
		add(password, "4, 4, fill, default");
		
		scrollPane = new JScrollPane();		
		txtrLicense = new JTextArea();
		scrollPane.setViewportView(txtrLicense);
		txtrLicense.setRows(10);
		txtrLicense.setEditable(false);
		txtrLicense.setText("license");
		
		chckbxAcceptLicense = new JCheckBox("accept license");
		chckbxAcceptLicense.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				enableButtonIfPossible(e);			
			}
		});
		chckbxAcceptLicense.setSelected(true);
//		add(scrollPane, "4, 6, fill, fill");
//		add(chckbxAcceptLicense, "4, 8");
	}
	
	public String getUsername() {
		return username.getText();
	}
	
	public String getPassword() {
		return new String(password.getPassword());
	}

	
	public void ctrlOKButton(JButton button) {
		okButton = button;
		enableButtonIfPossible(null);
	}
	
	public void setLicenseText(String text) {
		txtrLicense.setText(text);
		txtrLicense.setSelectionStart(0);
		txtrLicense.setSelectionEnd(0);
	}
	
	private void enableButtonIfPossible(AWTEvent event) {
		if(okButton != null) {
			if(chckbxAcceptLicense.isSelected() && (username.getText().trim().length() > 0) && (password.getPassword().length > 0)) {
				okButton.setEnabled(true);
			}
			else {
				okButton.setEnabled(false);
			}
		}
	}
	
	public void keyTyped(KeyEvent e) {}
	
	public void keyReleased(KeyEvent e) {}
	
	public void keyPressed(final KeyEvent e) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				enableButtonIfPossible(e);
			}
		});				
	}

}
