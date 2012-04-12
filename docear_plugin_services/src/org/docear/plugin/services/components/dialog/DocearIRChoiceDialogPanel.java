package org.docear.plugin.services.components.dialog;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

import org.docear.plugin.communications.CommunicationsController;
import org.docear.plugin.communications.features.AccountRegisterer;
import org.docear.plugin.services.ServiceController;
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
	public static final int ALLOW_CONTENT_IR = 2;
	public static final int ALLOW_USAGE_RESEARCH = 4;
	public static final int ALLOW_USAGE_IR = 8;
	public static final int ALLOW_RECOMMENDATIONS = 16;

	private static final long serialVersionUID = 1L;

	private final JTextField txtUsername;
	private final JTextField txtEmail;
	private final JPasswordField pwdPassword;
	private final JPasswordField pwdRetypepasswd;
	
	private boolean registrationNecessary = false;
	
	private JCheckBox chckbxAllowbackup;
	private JCheckBox chckbxAllowResearchContent;
	private JCheckBox chckbxAllowIRContent;
	private JCheckBox chckbxAllowResearchUsage;
	private JCheckBox chckbxAllowIRUsage;

	boolean removed = false;
	private JButton okButton;

	private JRadioButton rdbtnRegister;
	private JRadioButton rdbtnLogin;
	private JLabel lblRetypePassword;
	private JLabel lblEmail;
	
	private final boolean allowCancelAction;
	
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
	private JCheckBox chckbxAllowRecommendations;
	private JLabel lblGender;
	private JLabel lblNewsletter;
	private JCheckBox chckbxSendnewsletter;
	private JComboBox genderChooser;
	private JLabel lblRequiredFields;
	
	public DocearIRChoiceDialogPanel(boolean allowCancelAction) {
		this.allowCancelAction = allowCancelAction;
		setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("right:default:grow"),
				FormFactory.RELATED_GAP_COLSPEC,},
			new RowSpec[] {
				RowSpec.decode("fill:35dlu"),
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("fill:default"),
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("fill:default"),}));
		
		JPanel headPanel = new JPanel();
		headPanel.setBackground(Color.WHITE);
		headPanel.setPreferredSize(new Dimension(400, 80));
		headPanel.setMaximumSize(new Dimension(400, 80));
		add(headPanel, "1, 1, 3, 1, fill, fill");
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
				RowSpec.decode("top:default:grow"),}));
		
		JLabel lblDescriptionhead = new JLabel(TextUtils.getText("docear.uploadchooser.header.title"));
		lblDescriptionhead.setFont(new Font("Tahoma", Font.BOLD, 12));
		headPanel.add(lblDescriptionhead, "2, 2, 3, 1");
		
		JLabel lblDescriptiontext = new JLabel(TextUtils.getText("docear.uploadchooser.header.help.text"));
		JHyperlink link = new JHyperlink(TextUtils.getText("docear.uploadchooser.header.help.link.text"), "http://www.docear.org/support/user-manual/#backup");
		
		headPanel.add(lblDescriptiontext, "2, 4");
		headPanel.add(link, "4, 4");
		
		JPanel uploadPanel = new JPanel();
		uploadPanel.setBorder(new TitledBorder(null, TextUtils.getText("docear.uploadchooser.section.upload"), TitledBorder.LEADING, TitledBorder.TOP, null, null));
		add(uploadPanel, "2, 3, fill, fill");
		uploadPanel.setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("left:default:grow"),
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("left:default:grow"),},
			new RowSpec[] {
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,}));
		
		chckbxAllowbackup = new JCheckBox(TextUtils.getText("docear.uploadchooser.ckbx.backup"));
		chckbxAllowbackup.setSelected(ServiceController.getController().isBackupEnabled());
		chckbxAllowbackup.addActionListener(actionListener);
		uploadPanel.add(chckbxAllowbackup, "2, 1");
		
		chckbxAllowIRUsage = new JCheckBox(TextUtils.getText("docear.uploadchooser.ckbx.ir.usage"));
		chckbxAllowIRUsage.setSelected((ServiceController.getController().getInformationRetrievalCode()&ALLOW_USAGE_IR) > 0);
		chckbxAllowIRUsage.addActionListener(actionListener);
		
		chckbxAllowResearchUsage = new JCheckBox(TextUtils.getText("docear.uploadchooser.ckbx.research.usage"));
		chckbxAllowResearchUsage.setSelected((ServiceController.getController().getInformationRetrievalCode()&ALLOW_USAGE_RESEARCH) > 0);
		chckbxAllowResearchUsage.addActionListener(actionListener);
		
		chckbxAllowResearchContent = new JCheckBox(TextUtils.getText("docear.uploadchooser.ckbx.research.content"));
		chckbxAllowResearchContent.addActionListener(actionListener);		
		
		chckbxAllowRecommendations = new JCheckBox(TextUtils.getText("docear.uploadchooser.ckbx.recommendations"));
		chckbxAllowRecommendations.addActionListener(actionListener);		
		chckbxAllowRecommendations.setSelected((ServiceController.getController().getInformationRetrievalCode()&ALLOW_RECOMMENDATIONS) > 0);
		uploadPanel.add(chckbxAllowRecommendations, "2, 3");
		chckbxAllowResearchContent.setSelected((ServiceController.getController().getInformationRetrievalCode()&ALLOW_CONTENT_RESEARCH) > 0);
		uploadPanel.add(chckbxAllowResearchContent, "2, 5");
		uploadPanel.add(chckbxAllowResearchUsage, "4, 5");
		
		chckbxAllowIRContent = new JCheckBox(TextUtils.getText("docear.uploadchooser.ckbx.ir.content"));
		chckbxAllowIRContent.setSelected((ServiceController.getController().getInformationRetrievalCode()&ALLOW_CONTENT_IR) > 0);
		uploadPanel.add(chckbxAllowIRContent, "2, 7");
		chckbxAllowIRContent.addActionListener(actionListener);
		uploadPanel.add(chckbxAllowIRUsage, "4, 7");
		
		JPanel userDataPane = new JPanel();
		userDataPane.setBorder(new TitledBorder(null, TextUtils.getText("docear.uploadchooser.section.userdata"), TitledBorder.LEADING, TitledBorder.TOP, null, null));
		add(userDataPane, "2, 5, fill, fill");
		userDataPane.setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
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
		
		JLabel lblUsername = new JLabel(TextUtils.getText("docear.uploadchooser.username.label")+" *");
		userDataPane.add(lblUsername, "2, 4");
		
		lblEmail = new JLabel(TextUtils.getText("docear.uploadchooser.mail.label")+" *");
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
		
		JLabel lblPassword = new JLabel(TextUtils.getText("docear.uploadchooser.passwd.label1")+" *");
		userDataPane.add(lblPassword, "2, 8");
		lblRetypePassword = new JLabel(TextUtils.getText("docear.uploadchooser.passwd.label2")+" *");
		userDataPane.add(lblRetypePassword, "4, 8");
		
		pwdPassword = new JPasswordField();
		pwdPassword.addKeyListener(keyListener);
		userDataPane.add(pwdPassword, "2, 10");
		
		pwdRetypepasswd = new JPasswordField();
		pwdRetypepasswd.addKeyListener(keyListener);
		userDataPane.add(pwdRetypepasswd, "4, 10");
		
		lblGender = new JLabel(TextUtils.getText("docear.uploadchooser.gender.label"));
		userDataPane.add(lblGender, "2, 12");
		
		lblNewsletter = new JLabel(TextUtils.getText("docear.uploadchooser.news.label"));
		userDataPane.add(lblNewsletter, "4, 12");		
		
		genderChooser = new JComboBox(new Object[] {"",TextUtils.getText("docear.uploadchooser.gender.male"),TextUtils.getText("docear.uploadchooser.gender.female")});
		genderChooser.setEditable(false);
		userDataPane.add(genderChooser, "2, 14");
		
		chckbxSendnewsletter = new JCheckBox(TextUtils.getText("docear.uploadchooser.news.text"));
		userDataPane.add(chckbxSendnewsletter, "4, 14");
		
		lblRequiredFields = new JLabel("* "+TextUtils.getText("docear.uploadchooser.required.text"));
		lblRequiredFields.setFont(new Font("Tahoma", Font.BOLD, 9));
		userDataPane.add(lblRequiredFields, "2, 16, 3, 1");
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
		return chckbxAllowResearchContent.isSelected();
	}
	
	public final boolean allowContentIR() {
		return chckbxAllowIRContent.isSelected();
	}
	
	public final boolean allowUsageResearch() {
		return chckbxAllowResearchUsage.isSelected();
	}
	
	public final boolean allowUsageIR() {
		return chckbxAllowIRUsage.isSelected();
	}
	
	public final boolean allowRecommendations() {
		return chckbxAllowRecommendations.isSelected();
	}
	
	public final boolean wantsNewsletter() {
		return chckbxSendnewsletter.isSelected();
	}
	
	public final Boolean isMale() {
		if(genderChooser.getSelectedIndex() == 1) {
			return true;
		}
		else if(genderChooser.getSelectedIndex() == 2) {
			return false;
		}
		return null;
	}


	public final String getPassword() {
		return new String(pwdPassword.getPassword());
	}

	public void integrateButtons(JButton[] buttons) {
		okButton = buttons[0];
		enableButtonIfPossible(null);
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (checkAccountSettings()) {
					closeDialogManually();
				}
			}
		});
		for(int i=1; i < buttons.length; i++) {
			buttons[i].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					closeDialogManually();
				}
			});
		}
	}
	
	public void paint(Graphics g) {
		if(!removed && !allowCancelAction) {
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
				if(rdbtnLogin.isSelected() && txtUsername.getText().trim().length() > 2 && pwdPassword.getPassword() != null && pwdPassword.getPassword().length > 5) {	
					okButton.setEnabled(true);
				}
				else if(rdbtnRegister.isSelected() && txtUsername.getText().trim().length() > 2 && pwdPassword.getPassword().length > 5 && txtEmail.getText().trim().length() > 6 && pwdRetypepasswd.getPassword().length > 5 && getPassword().equals(new String(pwdRetypepasswd.getPassword()))) {	
					okButton.setEnabled(true);
				}
				else {
					okButton.setEnabled(false);
				}
			} else {
				if((chckbxAllowIRContent.isSelected() || chckbxAllowResearchContent.isSelected() || chckbxAllowIRUsage.isSelected() || chckbxAllowResearchUsage.isSelected())) {
					okButton.setEnabled(true);
				}
				else if(!chckbxAllowIRContent.isSelected() && !chckbxAllowResearchContent.isSelected() && !chckbxAllowIRUsage.isSelected() && !chckbxAllowResearchUsage.isSelected() && !chckbxAllowbackup.isSelected()) {
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
		lblGender.setEnabled(enabled);
		genderChooser.setEnabled(enabled);
		lblNewsletter.setEnabled(enabled);
		chckbxSendnewsletter.setEnabled(enabled);
		registrationNecessary = enabled;
	}
	
	public int getIrCode() {
		int code = 0;
		if (allowContentResearch()) {
			code += ALLOW_CONTENT_RESEARCH;
		}
		if (allowContentIR()) {
			code += ALLOW_CONTENT_IR;
		}
		if (allowUsageResearch()) {
			code += ALLOW_USAGE_RESEARCH;
		}
		if (allowUsageIR()) {
			code += ALLOW_USAGE_IR;
		}
		if (allowRecommendations()) {
			code += ALLOW_RECOMMENDATIONS;
		}
		
		return code;
	}
	
	private boolean checkAccountSettings() {
		if(!chckbxAllowIRContent.isSelected() && !chckbxAllowResearchContent.isSelected() && !chckbxAllowIRUsage.isSelected() && !chckbxAllowResearchUsage.isSelected() && !chckbxAllowbackup.isSelected() && !chckbxAllowRecommendations.isSelected()) {
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
				if (accountRegisterer.createRegisteredUser(getName(), getPassword(), getEmail(), null, wantsNewsletter())) {
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
			
			
			if (ServiceController.getController().isBackupAllowed()) {
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
