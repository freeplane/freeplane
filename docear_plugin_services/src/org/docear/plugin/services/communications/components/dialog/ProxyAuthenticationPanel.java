package org.docear.plugin.services.communications.components.dialog;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JPanel;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.factories.FormFactory;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JPasswordField;

import org.docear.plugin.services.communications.CommunicationsController;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.TextUtils;
import javax.swing.JCheckBox;

public class ProxyAuthenticationPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTextField hostField;
	private JTextField portField;
	private JTextField usernameField;
	private JPasswordField passwordField;
	private JCheckBox chckbxUseProxy;

	/**
	 * Create the panel.
	 */
	public ProxyAuthenticationPanel() {
		setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_COLSPEC,},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				RowSpec.decode("10dlu"),
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,}));
		
		JLabel lblTheProxyNeeds = new JLabel(TextUtils.getText("docear.proxy.connect.infotext.label"));
		add(lblTheProxyNeeds, "4, 2");
		
		chckbxUseProxy = new JCheckBox(TextUtils.getText("docear.proxy.connect.proxy.checkbox"));		
		add(chckbxUseProxy, "4, 4");
		
		JLabel lblHost = new JLabel(TextUtils.getText("docear.proxy.connect.host.label") + ":");
		add(lblHost, "2, 6, right, default");
		
		hostField = new JTextField(ResourceController.getResourceController().getProperty(CommunicationsController.DOCEAR_PROXY_HOST, ""));
		add(hostField, "4, 6, fill, default");
		hostField.setColumns(10);
		
		JLabel lblPort = new JLabel(TextUtils.getText("docear.proxy.connect.port.label") + ":");
		add(lblPort, "2, 8, right, default");
		
		portField = new JTextField(ResourceController.getResourceController().getProperty(CommunicationsController.DOCEAR_PROXY_PORT, ""));
		add(portField, "4, 8, fill, default");
		portField.setColumns(10);
		
		JLabel lblUsername = new JLabel(TextUtils.getText("docear.proxy.connect.username.label") + ":");
		add(lblUsername, "2, 10, right, default");
		
		usernameField = new JTextField(ResourceController.getResourceController().getProperty(CommunicationsController.DOCEAR_PROXY_USERNAME, ""));
		add(usernameField, "4, 10, fill, default");
		usernameField.setColumns(10);
		
		JLabel lblPassword = new JLabel(TextUtils.getText("docear.proxy.connect.password.label") + ":");
		add(lblPassword, "2, 12, right, default");
		
		passwordField = new JPasswordField();
		add(passwordField, "4, 12, fill, default");
		
		chckbxUseProxy.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				hostField.setEnabled(chckbxUseProxy.isSelected());
				portField.setEnabled(chckbxUseProxy.isSelected());
				usernameField.setEnabled(chckbxUseProxy.isSelected());
				passwordField.setEnabled(chckbxUseProxy.isSelected());
			}
		});
		
		chckbxUseProxy.setSelected(ResourceController.getResourceController().getBooleanProperty(CommunicationsController.DOCEAR_USE_PROXY));
		
		portField.addKeyListener(new KeyListener(	) {
			
			public void keyTyped(KeyEvent e) {
				if(!Character.isDigit(e.getKeyChar())){
					e.consume();
				}		
			}
			
			public void keyReleased(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			public void keyPressed(KeyEvent e) {
						
			}
		});
	}

	public JTextField getHostField() {
		return hostField;
	}

	public JTextField getPortField() {
		return portField;
	}

	public JTextField getUsernameField() {
		return usernameField;
	}

	public JPasswordField getPasswordField() {
		return passwordField;
	}

	public JCheckBox getChckbxUseProxy() {
		return chckbxUseProxy;
	}

}
