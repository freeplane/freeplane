package org.docear.plugin.backup.components.dialog;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

import org.docear.plugin.backup.BackupController;
import org.docear.plugin.communications.CommunicationsController;
import org.docear.plugin.communications.features.AccountRegisterer;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;
import org.swingplus.JHyperlink;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class DocearIRChoiceDialogPanel extends JPanel {
	public static final int ALLOW_CONTENT_RESEARCH = 1;
	public static final int ALLOW_CONTENT_ENHANCE_APP = 2;
	public static final int ALLOW_USAGE_RESEARCH = 4;
	public static final int ALLOW_USAGE_ENHANCE_APP = 8;	

	private static final long serialVersionUID = 1L;

	private final JTextField txtUsername;
	private final JTextField txtEmail;
	private final JPasswordField pwdPassword;
	private final JPasswordField pwdRetypepasswd;
	
	private boolean registrationNecessary = false;

	private JTextArea txtrLicense;
	private JCheckBox chckbxAcceptLicense;
	
	private JCheckBox chckbxAllowbackup;
	private JCheckBox chckbxAllowcontentresearch;
	private JCheckBox chckbxAllowcontentapp;
	private JCheckBox chckbxAllowusageresearch;
	private JCheckBox chckbxAllowusageapp;

	boolean removed = false;
	private JButton okButton;

	private JRadioButton rdbtnRegister;
	private JRadioButton rdbtnLogin;
	private JLabel lblRetypePassword;
	private JLabel lblEmail;
	
	private final KeyListener keyListener = new KeyListener() {
		public void keyTyped(KeyEvent e) {}
		public void keyReleased(KeyEvent e) {}
		public void keyPressed(final KeyEvent e) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					enableButtonIfPossible(e);
				}
			});
		}
	};
	
	private final ActionListener actionListener = new ActionListener() {
		public void actionPerformed(final ActionEvent e) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					enableButtonIfPossible(e);
				}
			});
		}
	};
	
	public DocearIRChoiceDialogPanel() {
		setLayout(new FormLayout(new ColumnSpec[] {
				ColumnSpec.decode("right:default:grow"),
				FormFactory.RELATED_GAP_COLSPEC,},
			new RowSpec[] {
				RowSpec.decode("fill:default"),
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("fill:default"),
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("fill:default"),
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("fill:default:grow"),}));
		
		JPanel headPanel = new JPanel();
		headPanel.setBackground(Color.WHITE);
		headPanel.setPreferredSize(new Dimension(400, 80));
		headPanel.setMaximumSize(new Dimension(400, 80));
		add(headPanel, "1, 1, fill, fill");
		headPanel.setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("left:default:grow"),
				FormFactory.RELATED_GAP_COLSPEC,},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("top:default:grow"),
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,}));
		
		JLabel lblDescriptionhead = new JLabel(TextUtils.getText("docear.uploadchooser.header.title"));
		lblDescriptionhead.setFont(new Font("Tahoma", Font.BOLD, 12));
		headPanel.add(lblDescriptionhead, "2, 2, 3, 1");
		
		JLabel lblDescriptiontext = new JLabel(TextUtils.getText("docear.uploadchooser.header.help.text"));
		JHyperlink link = new JHyperlink(TextUtils.getText("docear.uploadchooser.header.help.link.text"), "http://www.docear.org/support/user-manual/#backup");
		
		headPanel.add(lblDescriptiontext, "2, 4");
		headPanel.add(link, "4, 4");
		
		JSeparator separator_6 = new JSeparator();
		separator_6.setForeground(Color.BLACK);
		headPanel.add(separator_6, "1, 6, 5, 1");
		
		JPanel uploadPanel = new JPanel();
		uploadPanel.setBorder(new TitledBorder(null, TextUtils.getText("docear.uploadchooser.section.upload"), TitledBorder.LEADING, TitledBorder.TOP, null, null));
		add(uploadPanel, "1, 3, fill, fill");
		uploadPanel.setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("right:default"),
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("center:default:grow"),
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("center:default:grow"),
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("center:default:grow"),},
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
				FormFactory.DEFAULT_ROWSPEC,}));
		
		JLabel lblBackup = new JLabel(TextUtils.getText("docear.uploadchooser.table.label.backup"));
		uploadPanel.add(lblBackup, "4, 2");
		
		JLabel lblResearch = new JLabel(TextUtils.getText("docear.uploadchooser.table.label.research"));
		uploadPanel.add(lblResearch, "8, 2");
		
		JLabel lblEnhanceApp = new JLabel(TextUtils.getText("docear.uploadchooser.table.label.enhancements"));
		uploadPanel.add(lblEnhanceApp, "12, 2");
		
		JSeparator separator_1 = new JSeparator();
		uploadPanel.add(separator_1, "2, 4, 11, 1");
		
		JLabel lblContent = new JLabel(TextUtils.getText("docear.uploadchooser.table.label.content"));
		uploadPanel.add(lblContent, "2, 6");
		
		chckbxAllowbackup = new JCheckBox();
		chckbxAllowbackup.addActionListener(actionListener);
		uploadPanel.add(chckbxAllowbackup, "4, 6");
		
		chckbxAllowcontentresearch = new JCheckBox();
		chckbxAllowcontentresearch.addActionListener(actionListener);
		uploadPanel.add(chckbxAllowcontentresearch, "8, 6");
		
		chckbxAllowcontentapp = new JCheckBox();
		uploadPanel.add(chckbxAllowcontentapp, "12, 6");
		chckbxAllowcontentapp.addActionListener(actionListener);
		
		JSeparator separator_4 = new JSeparator();
		uploadPanel.add(separator_4, "2, 8");
		
		JLabel lblUsage = new JLabel(TextUtils.getText("docear.uploadchooser.table.label.usage"));
		uploadPanel.add(lblUsage, "2, 10");
		
		chckbxAllowusageresearch = new JCheckBox();
		chckbxAllowusageresearch.addActionListener(actionListener);
		uploadPanel.add(chckbxAllowusageresearch, "8, 10");
		
		chckbxAllowusageapp = new JCheckBox();
		chckbxAllowusageapp.addActionListener(actionListener);
		uploadPanel.add(chckbxAllowusageapp, "12, 10");
		
		JSeparator separator_7 = new JSeparator();
		uploadPanel.add(separator_7, "2, 12");
		
		JPanel userDataPane = new JPanel();
		userDataPane.setBorder(new TitledBorder(null, TextUtils.getText("docear.uploadchooser.section.userdata"), TitledBorder.LEADING, TitledBorder.TOP, null, null));
		add(userDataPane, "1, 5, fill, fill");
		userDataPane.setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),},
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
				FormFactory.DEFAULT_ROWSPEC,}));
		
		rdbtnLogin = new JRadioButton(TextUtils.getText("docear.uploadchooser.method.login"));
		userDataPane.add(rdbtnLogin, "2, 2");
		rdbtnLogin.setSelected(true);
		
		rdbtnLogin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				enableRegistration(false);
				enableButtonIfPossible(e);
			}		
		});
		
		rdbtnRegister = new JRadioButton(TextUtils.getText("docear.uploadchooser.method.register"));
		userDataPane.add(rdbtnRegister, "4, 2");
		
		rdbtnRegister.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				enableRegistration(true);
				enableButtonIfPossible(e);
			}
		});
		
		JLabel lblUsername = new JLabel(TextUtils.getText("docear.uploadchooser.username.label"));
		userDataPane.add(lblUsername, "2, 4");
		
		lblEmail = new JLabel("eMail");
		userDataPane.add(lblEmail, "4, 4");
		
		txtUsername = new JTextField();
		userDataPane.add(txtUsername, "2, 6");
		txtUsername.setText(ResourceController.getResourceController().getProperty("docear.service.connect.username",""));
		txtUsername.setColumns(10);
		txtUsername.addKeyListener(keyListener);
		
		
		txtEmail = new JTextField();
		userDataPane.add(txtEmail, "4, 6");
		txtEmail.setColumns(10);
		txtEmail.addKeyListener(keyListener);
		
		JLabel lblPassword = new JLabel(TextUtils.getText("docear.uploadchooser.passwd.label1"));
		userDataPane.add(lblPassword, "2, 8");
		lblRetypePassword = new JLabel(TextUtils.getText("docear.uploadchooser.passwd.label2"));
		userDataPane.add(lblRetypePassword, "4, 8");
		
		pwdPassword = new JPasswordField();
		pwdPassword.addKeyListener(keyListener);
		userDataPane.add(pwdPassword, "2, 10");
		
		pwdRetypepasswd = new JPasswordField();
		pwdRetypepasswd.addKeyListener(keyListener);
		userDataPane.add(pwdRetypepasswd, "4, 10");
		
		JPanel licensePane = new JPanel();
		licensePane.setBorder(new TitledBorder(null, TextUtils.getText("docear.uploadchooser.section.license"), TitledBorder.LEADING, TitledBorder.TOP, null, null));
		add(licensePane, "1, 7, fill, fill");
		licensePane.setLayout(new FormLayout(new ColumnSpec[] {
				ColumnSpec.decode("93px:grow"),},
			new RowSpec[] {
				RowSpec.decode("114px"),
				FormFactory.LINE_GAP_ROWSPEC,
				RowSpec.decode("23px"),}));
		//chckbxAcceptLicense.setSelected(true);
		
		JScrollPane scrollPane = new JScrollPane();
		licensePane.add(scrollPane, "1, 1");
		
		txtrLicense = new JTextArea();
		scrollPane.setViewportView(txtrLicense);
		txtrLicense.setRows(6);
		txtrLicense.setEditable(false);
		txtrLicense.setText("Licence");
		
		chckbxAcceptLicense = new JCheckBox("acceptLicense");
		licensePane.add(chckbxAcceptLicense, "1, 3");
		chckbxAcceptLicense.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				enableButtonIfPossible(e);			
			}
		});
		enableRegistration(false);
	}
	
	
	
	public final boolean isRegistrationNecessary() {
		return this.registrationNecessary;
	}


	public final String getUserName() {
		return txtUsername.getText();
	}



	public final String getEmail() {
		return txtEmail.getText();
	}
	
	public final boolean allowBackup() {
		return chckbxAllowbackup.isSelected();
	}
	
	public final boolean allowContentResearch() {
		return chckbxAllowcontentresearch.isSelected();
	}
	
	public final boolean allowContentEnhanceApps() {
		return chckbxAllowcontentapp.isSelected();
	}
	
	public final boolean allowUsageResearch() {
		return chckbxAllowusageresearch.isSelected();
	}
	
	public final boolean allowUsageEnhanceApps() {
		return chckbxAllowusageapp.isSelected();
	}
	
	public final boolean isLicenseAccepted() {
		return chckbxAcceptLicense.isSelected();
	}



	public final String getPassword() {
		return new String(pwdPassword.getPassword());
	}


	public void setLicenseText(String text) {
		txtrLicense.setText(text);
		txtrLicense.setSelectionStart(0);
		txtrLicense.setSelectionEnd(0);
	}



	public void integrateButtons(JButton[] buttons) {
		okButton = buttons[0];
		enableButtonIfPossible(null);
		for(JButton button : buttons) {
			button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (checkAccountSettings()) {
						closeDialogManually();
					}
				}
			});
		}
	}
	
	public void paint(Graphics g) {
		if(!removed) {
			removed = true;
			removeDialogWindowControls();			
		}
		super.paint(g);
	}
		
	private void closeDialogManually() {
		Container container = getParent();
		while(!(container instanceof JDialog)) {
			container = container.getParent();
		}
		((JDialog)container).dispose();
	}
	
	private void removeDialogWindowControls() {
		Container container = getParent();
		while(!(container instanceof JDialog)) {
			container = container.getParent();
		}		
		((JDialog)container).setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		((JDialog)container).addWindowListener(new WindowListener() {
			
			public void windowOpened(WindowEvent e) {}			
			public void windowIconified(WindowEvent e) {}
			public void windowDeiconified(WindowEvent e) {}
			public void windowDeactivated(WindowEvent e) {}
			public void windowActivated(WindowEvent e) {}
			public void windowClosed(WindowEvent e) {}
			public void windowClosing(WindowEvent e) {
				//maybe reset all presets instead of showing a warning
				JOptionPane.showMessageDialog((Component) e.getSource(), TextUtils.getText("docear.uploadchooser.dialog_x.warn"),"", JOptionPane.WARNING_MESSAGE);
			}		
		});
	}
	
	private void enableButtonIfPossible(AWTEvent event) {
		if(okButton != null) {
			if(chckbxAllowbackup.isSelected()) {
				if(rdbtnLogin.isSelected() && chckbxAcceptLicense.isSelected() && txtUsername.getText().trim().length() > 2 && pwdPassword.getPassword() != null && pwdPassword.getPassword().length > 5) {	
					okButton.setEnabled(true);
				}
				else if(rdbtnRegister.isSelected() && chckbxAcceptLicense.isSelected() && txtUsername.getText().trim().length() > 2 && pwdPassword.getPassword().length > 5 && txtEmail.getText().trim().length() > 6 && pwdRetypepasswd.getPassword().length > 5 && getPassword().equals(new String(pwdRetypepasswd.getPassword()))) {	
					okButton.setEnabled(true);
				}
				else {
					okButton.setEnabled(false);
				}
			} else {
				if((chckbxAllowcontentapp.isSelected() || chckbxAllowcontentresearch.isSelected() || chckbxAllowusageapp.isSelected() || chckbxAllowusageresearch.isSelected()) && chckbxAcceptLicense.isSelected()) {
					okButton.setEnabled(true);
				}
				else if(!chckbxAllowcontentapp.isSelected() && !chckbxAllowcontentresearch.isSelected() && !chckbxAllowusageapp.isSelected() && !chckbxAllowusageresearch.isSelected() && !chckbxAllowbackup.isSelected()) {
					okButton.setEnabled(true);
				}
				else {
					okButton.setEnabled(false);
				}
			}
		}
	}
	
	public void enableRegistration(boolean enabled) {
		rdbtnRegister.setSelected(enabled);
		rdbtnLogin.setSelected(!enabled);
		lblRetypePassword.setEnabled(enabled);
		pwdRetypepasswd.setEnabled(enabled);
		lblEmail.setEnabled(enabled);
		txtEmail.setEnabled(enabled);
		registrationNecessary = enabled;
	}
	
	public int getIrCode() {
		int code = 0;
		if (allowContentResearch()) {
			code += ALLOW_CONTENT_RESEARCH;
		}
		if (allowContentEnhanceApps()) {
			code += ALLOW_CONTENT_ENHANCE_APP;
		}
		if (allowUsageResearch()) {
			code += ALLOW_USAGE_RESEARCH;
		}
		if (allowUsageEnhanceApps()) {
			code += ALLOW_USAGE_ENHANCE_APP;
		}
		
		return code;
	}
	
	private boolean checkAccountSettings() {
		if(!chckbxAllowcontentapp.isSelected() && !chckbxAllowcontentresearch.isSelected() && !chckbxAllowusageapp.isSelected() && !chckbxAllowusageresearch.isSelected() && !chckbxAllowbackup.isSelected()) {
			return true;
		}
		
		AccountRegisterer accountRegisterer = new AccountRegisterer();
		int code = getIrCode();
		
		if (isRegistrationNecessary()) {
			if (isEmpty(getPassword()) || isEmpty(getEmail())  || !getPassword().equals(new String(pwdRetypepasswd.getPassword()))) {
				JOptionPane.showMessageDialog(UITools.getFrame(), TextUtils.getText("docear.uploadchooser.warning.enterall"), TextUtils.getText("docear.uploadchooser.warning.enterall.title"), JOptionPane.WARNING_MESSAGE);
				return false;
			}
			else {
				if (accountRegisterer.createRegisteredUser(getName(), getPassword(), getEmail(), null, true)) {
					return true;
				}
				else {
					JOptionPane.showMessageDialog(UITools.getFrame(), TextUtils.getText("docear.uploadchooser.warning.notregistered"), TextUtils.getText("docear.uploadchooser.warning.notregistered.title"), JOptionPane.WARNING_MESSAGE);
					return false;
				}
			}
		}
				
		if (allowBackup()) {			
			CommunicationsController.getController().tryToConnect(getUserName(), getPassword(), true, false);
			
			
			if (BackupController.getController().isBackupAllowed()) {
				return true;
			}
			else {
				return false;
			}
		}
		else if (code > 0) {
			//if user name is empty --> create anonymous user automatically when the information retrieval action runs 
			if (!isEmpty(getUserName())) {
				if (isEmpty(getPassword())) {
					JOptionPane.showMessageDialog(UITools.getFrame(), TextUtils.getText("docear.uploadchooser.warning.nopassword"), TextUtils.getText("docear.uploadchooser.warning.nopassword.title"), JOptionPane.WARNING_MESSAGE);
					return false;
				}
				else {
					CommunicationsController.getController().tryToConnect(getUserName(), getPassword(), true, false);
					if (!isEmpty(CommunicationsController.getController().getRegisteredAccessToken())) {
						return true;
					}
					else {
						return false;
					}
				}
			}
			else {
				//if user name is empty --> create anonymous user automatically when the information retrieval action runs 
				return true;
			}
			
		}
		
		return true;
	}

	private boolean isEmpty(String s) {
		return s==null || s.trim().length()==0;
	}

}
