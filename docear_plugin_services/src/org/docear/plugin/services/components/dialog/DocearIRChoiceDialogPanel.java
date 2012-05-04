package org.docear.plugin.services.components.dialog;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

import org.docear.plugin.communications.CommunicationsController;
import org.docear.plugin.communications.features.DocearServiceException;
import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.ui.MultiLineActionLabel;
import org.docear.plugin.core.ui.components.DocearLicensePanel;
import org.docear.plugin.services.ServiceController;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.Controller;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class DocearIRChoiceDialogPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private JTextField txtUsername;
	private JTextField txtEmail;
	private JPasswordField pwdPassword;
	private JPasswordField pwdRetypepasswd;
	
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
	
	private List<ActionListener> listeners = new  ArrayList<ActionListener>();
	
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
	private JCheckBox chckbxSendnewsletter;
	private JComboBox genderChooser;
	private JLabel lblRequiredFields;
	private JLabel lblBirthyear;
	private JTextField txtBirthYear;
	private JPanel lblMoreinfo;
	private JLabel lblAdvice;
	private JPanel LegalMattersPane;
	private JScrollPane scrollPane;
	private JTextArea txtrLicense;
	private JPanel panel;
	private JCheckBox chckbxAcceptTermsOfService;
	private JPanel lblAcceptTermsOfService;
	private JPanel panel_1;
	private JCheckBox chckbxAcceptDataUsage;
	private JPanel panel_2;
	private JPanel lblAcceptDataUsageTerms;

	private String adviceText1;
	private String adviceText2;
	
	public DocearIRChoiceDialogPanel(final boolean withoutLicense) {
		setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_COLSPEC,},
			new RowSpec[] {
				RowSpec.decode("fill:default"),
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("fill:default"),
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("top:default:grow"),
				FormFactory.DEFAULT_ROWSPEC,}));
		
		initOptionSection();
		
		initUserDataSection();
		
		initLegalMattersSection(withoutLicense);		
		
		adviceText1 = TextUtils.getText("docear.uploadchooser.advice1.text");
		adviceText2 = TextUtils.getText("docear.uploadchooser.advice2.text");
		lblAdvice = new JLabel(adviceText1);
		lblAdvice.setForeground(Color.RED);
		lblAdvice.setVerticalAlignment(SwingConstants.TOP);
		lblAdvice.setFont(new Font("Tahoma", Font.BOLD, 11));
		
		add(lblAdvice, "2, 6");
		
		enableRegistration(isEmpty(txtUsername.getText()));
		
	}



	private void initLegalMattersSection(boolean withoutLicense) {
		LegalMattersPane = new JPanel();
		LegalMattersPane.setBorder(new TitledBorder(null, TextUtils.getText("docear.uploadchooser.section.legal_matters"), TitledBorder.LEADING, TitledBorder.TOP, null, null));
		add(LegalMattersPane, "2, 5, fill, fill");
		LegalMattersPane.setLayout(new FormLayout(new ColumnSpec[] {
				ColumnSpec.decode("default:grow"),},
			new RowSpec[] {
				RowSpec.decode("default:grow"),
				RowSpec.decode("default:grow"),
				RowSpec.decode("default:grow"),}));
		
		scrollPane = new JScrollPane();
		LegalMattersPane.add(scrollPane, "1, 1, fill, fill");
		
		txtrLicense = new JTextArea();
		txtrLicense.setColumns(80);
		txtrLicense.setRows(10);
		txtrLicense.setLineWrap(true);
		txtrLicense.setWrapStyleWord(true);
		txtrLicense.setEditable(false);
		txtrLicense.setFont(new Font("Monospaced", Font.PLAIN, 11));
		txtrLicense.setText(getDataProcessingTerms());
		scrollPane.setViewportView(txtrLicense);
		
		panel_1 = new JPanel();
		LegalMattersPane.add(panel_1, "1, 2, fill, fill");
		panel_1.setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.DEFAULT_COLSPEC,
				ColumnSpec.decode("default:grow"),},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("top:default:grow"),}));
		
		chckbxAcceptDataUsage = new JCheckBox();
		chckbxAcceptDataUsage.addActionListener(actionListener);
		panel_1.add(chckbxAcceptDataUsage, "1, 2");
		
		final DocearLicensePanel licenseText = new DocearLicensePanel();
		
		lblAcceptDataUsageTerms = /*new JPanel();//*/ new MultiLineActionLabel(TextUtils.getText("docear.uploadchooser.usage.accept.text"));
		panel_1.add(lblAcceptDataUsageTerms, "2, 2, fill, fill");
		
		panel = new JPanel();
		LegalMattersPane.add(panel, "1, 3, fill, fill");
		panel.setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.DEFAULT_COLSPEC,
				ColumnSpec.decode("default:grow"),},
			new RowSpec[] {
				RowSpec.decode("top:default:grow"),}));
		
		chckbxAcceptTermsOfService = new JCheckBox();
		chckbxAcceptTermsOfService.addActionListener(actionListener);
		panel.add(chckbxAcceptTermsOfService, "1, 1");
		
		lblAcceptTermsOfService = /*new JPanel();//*/ new MultiLineActionLabel(TextUtils.getText("docear.uploadchooser.licenses.accept.text"));
		((MultiLineActionLabel) lblAcceptTermsOfService).addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if("tos".equals(e.getActionCommand())) {
					licenseText.setLicenseText(getTermsOfUse());
					JOptionPane.showConfirmDialog(DocearIRChoiceDialogPanel.this, licenseText, TextUtils.getText("docear.license.terms_of_use.title"), JOptionPane.PLAIN_MESSAGE, JOptionPane.PLAIN_MESSAGE, null);
					return;
				}
				if("dps".equals(e.getActionCommand())) {
					licenseText.setLicenseText(getDataPrivacyTerms());
					JOptionPane.showConfirmDialog(DocearIRChoiceDialogPanel.this, licenseText, TextUtils.getText("docear.license.data_privacy.title"), JOptionPane.PLAIN_MESSAGE, JOptionPane.PLAIN_MESSAGE, null);
					return;
				}
			
			}
		});
		panel.add(lblAcceptTermsOfService, "2, 1, fill, fill");
		
		if(withoutLicense) {
			chckbxAcceptDataUsage.setEnabled(false);
			chckbxAcceptDataUsage.setSelected(true);
			chckbxAcceptTermsOfService.setEnabled(false);
			chckbxAcceptTermsOfService.setSelected(true);
		}
	}


	private void initUserDataSection() {
		JPanel userDataPane = new JPanel();
		userDataPane.setBorder(new TitledBorder(null, TextUtils.getText("docear.uploadchooser.section.userdata"), TitledBorder.LEADING, TitledBorder.TOP, null, null));
		add(userDataPane, "2, 3, fill, fill");
		userDataPane.setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,},
			new RowSpec[] {
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("default:grow"),
				FormFactory.DEFAULT_ROWSPEC,}));
		
		rdbtnLogin = new JRadioButton(TextUtils.getText("docear.uploadchooser.method.login"));
		rdbtnLogin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				enableRegistration(false);
				enableButtonIfPossible(e);
			}		
		});
		userDataPane.add(rdbtnLogin, "2, 1, 3, 1");		
		
		rdbtnRegister = new JRadioButton(TextUtils.getText("docear.uploadchooser.method.register"));				
		rdbtnRegister.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				enableRegistration(true);
				enableButtonIfPossible(e);
			}
		});
		userDataPane.add(rdbtnRegister, "6, 1, 3, 1");
		
		txtUsername = /*new JTextField();	//*/ new OverlayTextField(TextUtils.getText("docear.uploadchooser.username.label"));	
		txtUsername.setText(ResourceController.getResourceController().getProperty("docear.service.connect.username",""));
		txtUsername.setForeground(new Color(txtUsername.getForeground().getRGB(), false));
		txtUsername.setColumns(10);
		txtUsername.addKeyListener(keyListener);
		userDataPane.add(txtUsername, "2, 3");
		
		JLabel lblUsername = new JLabel("*");
		userDataPane.add(lblUsername, "4, 3");
				
		pwdPassword = /*new JPasswordField(); //*/ new OverlayPasswordField(TextUtils.getText("docear.uploadchooser.passwd.label1"));
		pwdPassword.addKeyListener(keyListener);
		
		
		txtEmail = /*new JTextField(); //*/ new OverlayTextField(TextUtils.getText("docear.uploadchooser.mail.label"));
		txtEmail.setColumns(10);
		txtEmail.addKeyListener(keyListener);
		userDataPane.add(txtEmail, "6, 3");
		
		lblEmail = new JLabel("*");
		userDataPane.add(lblEmail, "8, 3");
		userDataPane.add(pwdPassword, "2, 5");

		JLabel lblPassword = new JLabel("*");
		userDataPane.add(lblPassword, "4, 5");
		
		pwdRetypepasswd = /*new JPasswordField(); //*/ new OverlayPasswordField(TextUtils.getText("docear.uploadchooser.passwd.label2"));
		pwdRetypepasswd.addKeyListener(keyListener);
		userDataPane.add(pwdRetypepasswd, "6, 5");
		
		lblRetypePassword = new JLabel("*");
		userDataPane.add(lblRetypePassword, "8, 5");
		
		chckbxSendnewsletter = new JCheckBox(TextUtils.getText("docear.uploadchooser.news.text"));
		userDataPane.add(chckbxSendnewsletter, "2, 7, 3, 1");
		chckbxSendnewsletter.setSelected(true);
		
		panel_2 = new JPanel();
		userDataPane.add(panel_2, "6, 7, 3, 1, fill, fill");
		panel_2.setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				ColumnSpec.decode("4dlu:grow"),
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,}));
		
		lblBirthyear = new JLabel(TextUtils.getText("docear.uploadchooser.birthyear.label"));
		lblBirthyear.setHorizontalAlignment(SwingConstants.RIGHT);
		panel_2.add(lblBirthyear, "1, 2");
		
		txtBirthYear = new JTextField();
		panel_2.add(txtBirthYear, "3, 2");
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
		
		lblGender = new JLabel(TextUtils.getText("docear.uploadchooser.gender.label"));
		lblGender.setHorizontalAlignment(SwingConstants.RIGHT);
		panel_2.add(lblGender, "5, 2");
		
		genderChooser = new JComboBox(new Object[] {"",TextUtils.getText("docear.uploadchooser.gender.male"),TextUtils.getText("docear.uploadchooser.gender.female")});
		panel_2.add(genderChooser, "7, 2");
		genderChooser.setEditable(false);
		
		lblRequiredFields = new JLabel("* "+TextUtils.getText("docear.uploadchooser.required.text"));
		lblRequiredFields.setFont(new Font("Tahoma", Font.BOLD, 9));
		userDataPane.add(lblRequiredFields, "2, 8, 6, 1");
	}


	private void initOptionSection() {
		JPanel uploadPanel = new JPanel();
		uploadPanel.setBorder(new TitledBorder(null, TextUtils.getText("docear.uploadchooser.section.upload"), TitledBorder.LEADING, TitledBorder.TOP, null, null));
		add(uploadPanel, "2, 1, fill, fill");
		uploadPanel.setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("left:default:grow"),
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("left:default:grow"),},
			new RowSpec[] {
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,}));
		
		chckbxAllowbackup = new JCheckBox(TextUtils.getText("docear.uploadchooser.ckbx.backup"));
		chckbxAllowbackup.setSelected(Boolean.parseBoolean(ResourceController.getResourceController().getProperty(ServiceController.DOCEAR_SAVE_BACKUP, "false")));
		chckbxAllowbackup.addActionListener(actionListener);
		uploadPanel.add(chckbxAllowbackup, "2, 1");
		
		int irNumber = Integer.parseInt(ResourceController.getResourceController().getProperty(ServiceController.DOCEAR_INFORMATION_RETRIEVAL, "13"));
		
		chckbxAllowRecommendations = new JCheckBox(TextUtils.getText("docear.uploadchooser.ckbx.recommendations"));	
		chckbxAllowRecommendations.addActionListener(actionListener);
		chckbxAllowRecommendations.setSelected((irNumber&ServiceController.ALLOW_RECOMMENDATIONS) > 0);
		uploadPanel.add(chckbxAllowRecommendations, "4, 1");
		
		chckbxAllowResearchContent = new JCheckBox(TextUtils.getText("docear.uploadchooser.ckbx.research.content"));
		chckbxAllowResearchContent.setSelected((irNumber&ServiceController.ALLOW_RESEARCH) > 0);
		chckbxAllowResearchContent.addActionListener(actionListener);
		uploadPanel.add(chckbxAllowResearchContent, "6, 1");
		
		
		chckbxAllowIR = new JCheckBox(TextUtils.getText("docear.uploadchooser.ckbx.ir.content"));
		chckbxAllowIR.setSelected((irNumber&ServiceController.ALLOW_INFORMATION_RETRIEVAL) > 0);
		chckbxAllowIR.addActionListener(actionListener);
		
		chckbxAllowResearchUsage = new JCheckBox(TextUtils.getText("docear.uploadchooser.ckbx.research.usage"));
		chckbxAllowResearchUsage.setSelected((irNumber&ServiceController.ALLOW_USAGE_MINING) > 0);
		chckbxAllowResearchUsage.addActionListener(actionListener);
		uploadPanel.add(chckbxAllowResearchUsage, "8, 1");
		uploadPanel.add(chckbxAllowIR, "10, 1");
		
		lblMoreinfo = new MultiLineActionLabel(TextUtils.getText("docear.uploadchooser.more.text"));
		((MultiLineActionLabel)lblMoreinfo).addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if("more info".equals(e.getActionCommand())) {
					try {
						Controller.getCurrentController().getViewController().openDocument(new URI("http://www.docear.org/give-back/share-your-data/"));
					} 
					catch (Exception e1) {
						LogUtils.warn("could not open link to \"more info\"!");
					}
				}
			}
		});
		((MultiLineActionLabel)lblMoreinfo).setHorizontalAlignment(MultiLineActionLabel.RIGHT);
		uploadPanel.add(lblMoreinfo, "8, 3, 3, 1");
	}
	
	
	
	public void addActionListener(ActionListener listener) {
		if(listeners.contains(listener)) {
			return;
		}
		listeners.add(listener);
	}
	
	public void removeActionListener(ActionListener listener) {
		listeners.remove(listener);
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


	public final String getPassword() throws DocearServiceException {
		String pwd = new String(pwdPassword.getPassword());
		if(registrationNecessary && !pwd.equals(new String(pwdRetypepasswd.getPassword()))) {
			throw new DocearServiceException(TextUtils.getText("docear.uploadchooser.warning.not_matching_passwords"));
		}
		return new String(pwdPassword.getPassword());
	}

	public void integrateButtons(final JButton[] buttons) {
		okButton = buttons[0];
		enableButtonIfPossible(null);
		for(int i=0; i < buttons.length; i++) {
			final int id = i;
			buttons[i].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					//closeDialogManually();
					fireActionEvent(buttons[id] ,id, "");
				}
			});
		}
	}
	
	
	private void fireActionEvent(Object source, int id, String command) {
		ActionEvent event = new ActionEvent(this, id, command);
		if(listeners.size() <= 0 ) {
			Container cont = getParent();
			while(!(cont instanceof JOptionPane)) {
				cont = cont.getParent();
			}
			((JOptionPane)cont).setValue(source);
			close();
		}
		else {
			for(ActionListener listener : listeners) {
				listener.actionPerformed(event);
			}
		}
		
	}
	public void clearUserData() {
		txtUsername.setText("");
		txtEmail.setText("");
		pwdPassword.setText("");
		pwdRetypepasswd.setText("");
	}
	
	public void close() {
		closeDialogManually();
	}
		
	private void closeDialogManually() {
		Container container = getParent();
		while(!(container instanceof JDialog)) {
			container = container.getParent();
		}
		((JDialog)container).dispose();
	}
		
	private void enableButtonIfPossible(AWTEvent event) {
		if(okButton != null) {
			lblAdvice.setText(adviceText2);
			lblAdvice.setOpaque(true);
			if(!chckbxAcceptDataUsage.isSelected() || !chckbxAcceptTermsOfService.isSelected()) {
				okButton.setEnabled(false);
				lblAdvice.setText(adviceText1);
				lblAdvice.setForeground(new Color(0xFFFF0000, true));
			}
			else
			if(chckbxAllowbackup.isSelected()) {
				if(rdbtnLogin.isSelected() && txtUsername.getText().trim().length() > 0 && !isEmpty(CommunicationsController.getController().getAccessToken())) {
					okButton.setEnabled(true);					
					lblAdvice.setForeground(new Color(0x00000000, true));
				}
				else if(rdbtnLogin.isSelected() && txtUsername.getText().trim().length() > 0 && pwdPassword.getPassword() != null && pwdPassword.getPassword().length > 0) {	
					okButton.setEnabled(true);					
					lblAdvice.setForeground(new Color(0x00000000, true));
				}
				else if(rdbtnRegister.isSelected() && txtUsername.getText().trim().length() > 0 && pwdPassword.getPassword().length > 0 && txtEmail.getText().trim().length() > 0 && pwdRetypepasswd.getPassword().length > 0 ) {	
					okButton.setEnabled(true);
					lblAdvice.setForeground(new Color(0x00000000, true));
				}
				else {
					okButton.setEnabled(false);
					lblAdvice.setForeground(new Color(0xFFFF0000, true));
				}
			} else {
				if((chckbxAllowIR.isSelected() || chckbxAllowResearchContent.isSelected() || chckbxAllowResearchUsage.isSelected())) {
					okButton.setEnabled(true);
					lblAdvice.setForeground(new Color(0x00000000, true));
				}
				else if(!chckbxAllowIR.isSelected() && !chckbxAllowResearchContent.isSelected() && !chckbxAllowResearchUsage.isSelected() && !chckbxAllowbackup.isSelected()) {
					okButton.setEnabled(true);
					lblAdvice.setForeground(new Color(0x00000000, true));
				}
				else {
					okButton.setEnabled(false);
					lblAdvice.setForeground(new Color(0xFFFF0000, true));
				}
			}
		}
	}
	
	public JButton getOkButton() {
		return okButton;
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
		chckbxSendnewsletter.setEnabled(enabled);
		lblBirthyear.setEnabled(enabled);
		txtBirthYear.setEnabled(enabled);
		registrationNecessary = enabled;
	}
	
	public int getIrCode() {
		int code = 0;
		if (allowContentResearch()) {
			code += ServiceController.ALLOW_RESEARCH;
		}
		if (allowInformationRetrieval()) {
			code += ServiceController.ALLOW_INFORMATION_RETRIEVAL;
		}
		if (allowUsageResearch()) {
			code += ServiceController.ALLOW_USAGE_MINING;
		}
		if (allowRecommendations()) {
			code += ServiceController.ALLOW_RECOMMENDATIONS;
		}
		
		return code;
	}

	private boolean isEmpty(String s) {
		return s==null || s.trim().length()==0;
	}
	
	private String getDataProcessingTerms() {
		try {
			return getStringFromStream(DocearController.class.getResourceAsStream("/Docear_data_processing.txt"));
		}
		catch (IOException e) {
			LogUtils.warn(e);
			return "Data Processing";
		}
	}
	
	private String getDataPrivacyTerms() {
		try {
			return getStringFromStream(DocearController.class.getResourceAsStream("/Docear_data_privacy.txt"));
		}
		catch (IOException e) {
			LogUtils.warn(e);
			return "Data Privacy";
		}
	}
	
	private String getTermsOfUse() {
		try {
			return getStringFromStream(DocearController.class.getResourceAsStream("/Docear_terms_of_use.txt"));
		}
		catch (IOException e) {
			LogUtils.warn(e);
			return "Terms of Use";
		}
	}
	
	private String getStringFromStream(InputStream is) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
		StringBuilder sb = new StringBuilder();
	
		String line;
		while ((line = br.readLine()) != null) {
			sb.append(line + System.getProperty("line.separator"));
		}
	
		br.close();
		return sb.toString();
	}
	
	
	class OverlayTextField extends JTextField implements FocusListener {
		
		private static final long serialVersionUID = 1L;
		private final String startText;

		public OverlayTextField(String initText) {
			super();
			this.startText = initText;
			addFocusListener(this);
			setGhostText();
		}
		
		public String getText() {
			if(getTextInternal().equals(startText)) {
				return "";
			}
			return super.getText();
		}
				
		public void focusGained(FocusEvent e) {
			removeGhostText();
		}

		public void focusLost(FocusEvent e) {
			setGhostText();
		}
		
		public void paint(Graphics g) {
			setGhostText();
			super.paint(g);
		}
		
		boolean inGhostSet = false;
		private void setGhostText() {
			if(inGhostSet) return;
			inGhostSet = true;
			if(!hasFocus() && "".equals(getTextInternal().trim()) ) {
				setForeground(new Color(0x88FFFFFF&getForeground().getRGB(), true));
				setText(startText);
			}
			inGhostSet = false;
		}
		
		private void removeGhostText() {
			setForeground(new Color(getForeground().getRGB(), false));
			if(startText.equals(getTextInternal())) {
				setText("");
			}
			revalidate();
			repaint();
		}
		
		private String getTextInternal() {
			return super.getText();
		}

		
	}
	
	class OverlayPasswordField extends JPasswordField implements FocusListener {
		
		private static final long serialVersionUID = 1L;
		private final String startText;
		private char echoChar;
		
		public OverlayPasswordField(String initText) {
			super();
			this.startText = initText;
			addFocusListener(this);
			echoChar = getEchoChar();
			setGhostText();
		}
				
		public void focusGained(FocusEvent e) {
			removeGhostText();
		}

		public void focusLost(FocusEvent e) {
			setGhostText();
		}
		
		public void paint(Graphics g) {
			setGhostText();
			super.paint(g);
		}
		
		public char[] getPassword() {
			String pw = getTextInternal();
			if(startText.equals(pw)) {
				return new char[]{};
			}
			return super.getPassword();
		}
		
		
		boolean inGhostSet = false;
		private void setGhostText() {
			if(inGhostSet) return;
			inGhostSet = true;
			//setEchoChar(echoChar);
			if(!hasFocus() && "".equals(getTextInternal()) ) {
				setEchoChar((char) 0);
				setForeground(new Color(0x88FFFFFF&getForeground().getRGB(), true));
				setText(startText);
			}
			inGhostSet = false;
		}
		
		private String getTextInternal() {
			return new String(super.getPassword());
		}
		
		private void removeGhostText() {
			setForeground(new Color(getForeground().getRGB(), false));
			if(startText.equals(getTextInternal())) {
				setEchoChar(echoChar);
				setText("");
			}
			revalidate();
			repaint();
		}

		
	}

	public Integer getBirthYear() {
		try {
			return new Integer(txtBirthYear.getText());
		}
		catch(Exception e) {
		}
		
		return null;
		
	}
}
