package org.docear.plugin.services.components.dialog;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.net.URISyntaxException;

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
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

import org.docear.plugin.communications.CommunicationsController;
import org.docear.plugin.communications.features.AccountRegisterer;
import org.docear.plugin.communications.features.DocearServiceException;
import org.docear.plugin.communications.features.DocearServiceException.DocearServiceExceptionType;
import org.docear.plugin.services.ServiceController;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.swingplus.JHyperlink;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import java.awt.Color;

public class DocearIRChoiceDialogPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private final JTextField txtUsername;
	private final JTextField txtEmail;
	private final JPasswordField pwdPassword;
	private final JPasswordField pwdRetypepasswd;
	
	private boolean registrationNecessary = false;
	
	private JCheckBox chckbxAllowbackup;
	private JCheckBox chckbxAllowResearchContent;
	private JCheckBox chckbxAllowIR;
	private JCheckBox chckbxAllowResearchUsage;

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
	private JPanel optionalPanel;
	private JLabel lblBirthyear;
	private JTextField txtBirthYear;
	private JLabel lblMoreinfo;
	private JLabel lblConsideration;
	
	public DocearIRChoiceDialogPanel(boolean allowCancelAction) {
		this.allowCancelAction = allowCancelAction;
		setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_COLSPEC,},
			new RowSpec[] {
				RowSpec.decode("fill:default"),
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("fill:default"),
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("top:default"),}));
		
		JPanel uploadPanel = new JPanel();
		uploadPanel.setBorder(new TitledBorder(null, TextUtils.getText("docear.uploadchooser.section.upload"), TitledBorder.LEADING, TitledBorder.TOP, null, null));
		add(uploadPanel, "2, 1, fill, fill");
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
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,}));
		
		chckbxAllowbackup = new JCheckBox(TextUtils.getText("docear.uploadchooser.ckbx.backup"));
		chckbxAllowbackup.setSelected(Boolean.parseBoolean(ResourceController.getResourceController().getProperty(ServiceController.DOCEAR_SAVE_BACKUP, "true")));
		chckbxAllowbackup.addActionListener(actionListener);
		uploadPanel.add(chckbxAllowbackup, "2, 1");
		
		int irNumber = Integer.parseInt(ResourceController.getResourceController().getProperty(ServiceController.DOCEAR_INFORMATION_RETRIEVAL, "13"));
		
		chckbxAllowRecommendations = new JCheckBox(TextUtils.getText("docear.uploadchooser.ckbx.recommendations"));	
		chckbxAllowRecommendations.addActionListener(actionListener);
		chckbxAllowRecommendations.setSelected((irNumber&ServiceController.ALLOW_RECOMMENDATIONS) > 0);
		uploadPanel.add(chckbxAllowRecommendations, "2, 3");
		
		chckbxAllowResearchContent = new JCheckBox(TextUtils.getText("docear.uploadchooser.ckbx.research.content"));
		chckbxAllowResearchContent.setSelected((irNumber&ServiceController.ALLOW_CONTENT_RESEARCH) > 0);
		chckbxAllowResearchContent.addActionListener(actionListener);
		uploadPanel.add(chckbxAllowResearchContent, "2, 5");
		
		chckbxAllowResearchUsage = new JCheckBox(TextUtils.getText("docear.uploadchooser.ckbx.research.usage"));
		chckbxAllowResearchUsage.setSelected((irNumber&ServiceController.ALLOW_USAGE_RESEARCH) > 0);
		chckbxAllowResearchUsage.addActionListener(actionListener);
		uploadPanel.add(chckbxAllowResearchUsage, "4, 5");
		
		
		chckbxAllowIR = new JCheckBox(TextUtils.getText("docear.uploadchooser.ckbx.ir.content"));
		chckbxAllowIR.setSelected((irNumber&ServiceController.ALLOW_INFORMATION_RETRIEVAL) > 0);
		chckbxAllowIR.addActionListener(actionListener);
		uploadPanel.add(chckbxAllowIR, "2, 7");
		
		lblMoreinfo = new JHyperlink(TextUtils.getText("docear.uploadchooser.more.text"), "http://www.docear.org/give-back/share-your-data/");
		uploadPanel.add(lblMoreinfo, "2, 9, 3, 1");
		lblMoreinfo.setHorizontalAlignment(SwingConstants.RIGHT);
		
		JPanel userDataPane = new JPanel();
		userDataPane.setBorder(new TitledBorder(null, TextUtils.getText("docear.uploadchooser.section.userdata"), TitledBorder.LEADING, TitledBorder.TOP, null, null));
		add(userDataPane, "2, 3, fill, fill");
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
				RowSpec.decode("fill:default"),
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,}));
		
		rdbtnLogin = new JRadioButton(TextUtils.getText("docear.uploadchooser.method.login"));
		rdbtnLogin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				enableRegistration(false);
				enableButtonIfPossible(e);
			}		
		});
		userDataPane.add(rdbtnLogin, "2, 2");
		
		
		
		
		rdbtnRegister = new JRadioButton(TextUtils.getText("docear.uploadchooser.method.register"));				
		rdbtnRegister.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				enableRegistration(true);
				enableButtonIfPossible(e);
			}
		});
		userDataPane.add(rdbtnRegister, "4, 2");
		
		JLabel lblUsername = new JLabel(TextUtils.getText("docear.uploadchooser.username.label")+" *");
		userDataPane.add(lblUsername, "2, 4");
		
		lblEmail = new JLabel(TextUtils.getText("docear.uploadchooser.mail.label")+" *");
		userDataPane.add(lblEmail, "4, 4");
		
		txtUsername = new JTextField();		
		txtUsername.setText(ResourceController.getResourceController().getProperty("docear.service.connect.username",""));
		txtUsername.setColumns(10);
		txtUsername.addKeyListener(keyListener);
		userDataPane.add(txtUsername, "2, 6");
		
		
		txtEmail = new JTextField();
		txtEmail.setColumns(10);
		txtEmail.addKeyListener(keyListener);
		userDataPane.add(txtEmail, "4, 6");
		
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
		
		optionalPanel = new JPanel();
		userDataPane.add(optionalPanel, "2, 12, 3, 1, fill, fill");
		optionalPanel.setLayout(new FormLayout(new ColumnSpec[] {
				ColumnSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),},
			new RowSpec[] {
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,}));
		
		lblGender = new JLabel(TextUtils.getText("docear.uploadchooser.gender.label"));
		optionalPanel.add(lblGender, "1, 1");
		
		lblBirthyear = new JLabel(TextUtils.getText("docear.uploadchooser.birthyear.label"));
		optionalPanel.add(lblBirthyear, "3, 1");
		
		lblNewsletter = new JLabel(TextUtils.getText("docear.uploadchooser.news.label"));
		optionalPanel.add(lblNewsletter, "5, 1");
		
		genderChooser = new JComboBox(new Object[] {"",TextUtils.getText("docear.uploadchooser.gender.male"),TextUtils.getText("docear.uploadchooser.gender.female")});
		optionalPanel.add(genderChooser, "1, 3");
		genderChooser.setEditable(false);
		
		txtBirthYear = new JTextField();
		txtBirthYear.setHorizontalAlignment(SwingConstants.RIGHT);
		txtBirthYear.addKeyListener(new KeyListener() {
			public void keyTyped(KeyEvent e) {
				if(e.getKeyChar() >= 32 && !(txtBirthYear.getText().length() < 4 && e.getKeyChar() <= '9' && e.getKeyChar() >= '0')) {
					e.consume();
				}
			}			
			public void keyReleased(KeyEvent e) {}			
			public void keyPressed(KeyEvent e) {}
		});
		txtBirthYear.setColumns(4);
		optionalPanel.add(txtBirthYear, "3, 3, fill, default");
		
		chckbxSendnewsletter = new JCheckBox(TextUtils.getText("docear.uploadchooser.news.text"));
		chckbxSendnewsletter.setSelected(true);
		optionalPanel.add(chckbxSendnewsletter, "5, 3");
		
		lblRequiredFields = new JLabel("* "+TextUtils.getText("docear.uploadchooser.required.text"));
		lblRequiredFields.setFont(new Font("Tahoma", Font.BOLD, 9));
		userDataPane.add(lblRequiredFields, "2, 14");
		
		lblConsideration = new JLabel(TextUtils.getText("docear.uploadchooser.consideration.text"));
		lblConsideration.setForeground(Color.RED);
		add(lblConsideration, "2, 5");
		lblConsideration.setVerticalAlignment(SwingConstants.TOP);
		lblConsideration.setFont(new Font("Tahoma", Font.BOLD, 11));
		enableRegistration(true);
	}
	
	
	
	public final boolean useRegistration() {
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
	
	public final boolean allowInformationRetrieval() {
		return chckbxAllowIR.isSelected();
	}
	
	public final boolean allowUsageResearch() {
		return chckbxAllowResearchUsage.isSelected();
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
				try {
					checkAccountSettings();
					Container cont = getParent();
					while(!(cont instanceof JOptionPane)) {
						cont = cont.getParent();
					}
					((JOptionPane)cont).setValue(e.getSource());
				} 
				catch (DocearServiceException e1) {
					JOptionPane.showMessageDialog(UITools.getFrame(), 
							TextUtils.getText("docear.uploadchooser.warning.notregistered")+e1.getMessage(), 
							TextUtils.getText("docear.uploadchooser.warning.notregistered.title"), 
							JOptionPane.WARNING_MESSAGE);
					LogUtils.info("DocearServiceException: "+e1.getMessage());
					if(DocearServiceExceptionType.NO_CONNECTION.equals(e1.getType())) {
						chckbxAllowbackup.setSelected(false);
						clearUserData();
					}
				} 
				catch (URISyntaxException e1) {
					JOptionPane.showMessageDialog(UITools.getFrame(), TextUtils.getText("docear.uploadchooser.warning.notregistered"), TextUtils.getText("docear.uploadchooser.warning.notregistered.title"), JOptionPane.WARNING_MESSAGE);
					LogUtils.warn(e1);
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
	
	private void clearUserData() {
		txtUsername.setText("");
		txtEmail.setText("");
		pwdPassword.setText("");
		pwdRetypepasswd.setText("");
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
		((JDialog)container).addKeyListener(new KeyListener() {
			
			public void keyTyped(KeyEvent e) {
				e.consume();
			}
			
			public void keyReleased(KeyEvent e) {
				e.consume();				
			}
			
			public void keyPressed(KeyEvent e) {
				e.consume();
			}
		});
	}
	
	private void enableButtonIfPossible(AWTEvent event) {
		if(okButton != null) {
			lblConsideration.setOpaque(true);
			if(chckbxAllowbackup.isSelected()) {
				if(rdbtnLogin.isSelected() && txtUsername.getText().trim().length() > 2 && pwdPassword.getPassword() != null && pwdPassword.getPassword().length > 5) {	
					okButton.setEnabled(true);					
					lblConsideration.setForeground(new Color(0x00000000, true));
				}
				else if(rdbtnRegister.isSelected() && txtUsername.getText().trim().length() > 2 && pwdPassword.getPassword().length > 5 && txtEmail.getText().trim().length() > 6 && pwdRetypepasswd.getPassword().length > 5 && getPassword().equals(new String(pwdRetypepasswd.getPassword()))) {	
					okButton.setEnabled(true);
					lblConsideration.setForeground(new Color(0x00000000, true));
				}
				else {
					okButton.setEnabled(false);
					lblConsideration.setForeground(new Color(0xFFFF0000, true));
				}
			} else {
				if((chckbxAllowIR.isSelected() || chckbxAllowResearchContent.isSelected() || chckbxAllowResearchUsage.isSelected())) {
					okButton.setEnabled(true);
					lblConsideration.setForeground(new Color(0x00000000, true));
				}
				else if(!chckbxAllowIR.isSelected() && !chckbxAllowResearchContent.isSelected() && !chckbxAllowResearchUsage.isSelected() && !chckbxAllowbackup.isSelected()) {
					okButton.setEnabled(true);
					lblConsideration.setForeground(new Color(0x00000000, true));
				}
				else {
					okButton.setEnabled(false);
					lblConsideration.setForeground(new Color(0xFFFF0000, true));
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
		lblBirthyear.setEnabled(enabled);
		txtBirthYear.setEnabled(enabled);
		registrationNecessary = enabled;
	}
	
	public int getIrCode() {
		int code = 0;
		if (allowContentResearch()) {
			code += ServiceController.ALLOW_CONTENT_RESEARCH;
		}
		if (allowInformationRetrieval()) {
			code += ServiceController.ALLOW_INFORMATION_RETRIEVAL;
		}
		if (allowUsageResearch()) {
			code += ServiceController.ALLOW_USAGE_RESEARCH;
		}
		if (allowRecommendations()) {
			code += ServiceController.ALLOW_RECOMMENDATIONS;
		}
		
		return code;
	}
	
	private boolean checkAccountSettings() throws DocearServiceException, URISyntaxException {
		if(!chckbxAllowIR.isSelected() && !chckbxAllowResearchContent.isSelected() && !chckbxAllowResearchUsage.isSelected() && !chckbxAllowbackup.isSelected() && !chckbxAllowRecommendations.isSelected()) {
			return true;
		}
		
		AccountRegisterer accountRegisterer = new AccountRegisterer();
		int code = getIrCode();
		if (useRegistration()) {
			if(!chckbxAllowbackup.isSelected() && isEmpty(getUserName()) && isEmpty(getPassword()) && isEmpty(new String(pwdRetypepasswd.getPassword())) && isEmpty(getEmail())) {
				return true;
			}
			else if (isEmpty(getPassword()) || isEmpty(getEmail())  || !getPassword().equals(new String(pwdRetypepasswd.getPassword()))) {
				throw new DocearServiceException(TextUtils.getText("docear.uploadchooser.warning.enterall"));
			}
			else {
				accountRegisterer.createRegisteredUser(getUserName(), getPassword(), getEmail(), null, wantsNewsletter(), isMale());
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
