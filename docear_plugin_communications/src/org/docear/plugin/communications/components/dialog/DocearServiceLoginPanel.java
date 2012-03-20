package org.docear.plugin.communications.components.dialog;

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

public class DocearServiceLoginPanel  extends JPanel {
	
	
	
	

	private static final long serialVersionUID = 1L;
	private JPasswordField password;
	private JTextField username;
	
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
	}
	
	public String getUsername() {
		return username.getText();
	}
	
	public String getPassword() {
		return new String(password.getPassword());
	}

}
