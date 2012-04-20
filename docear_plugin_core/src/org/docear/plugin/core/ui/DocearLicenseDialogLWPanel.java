package org.docear.plugin.core.ui;

import java.awt.Color;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.ui.components.DocearLicensePanel;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class DocearLicenseDialogLWPanel extends JPanel {
	
	private static final long serialVersionUID = 1L;
	
	private JButton okButton;
	private JCheckBox chckbxAcceptTermsOfUse;
	private JCheckBox chckbxAcceptDataPrivacyTerms;
	private JCheckBox chckbxAcceptDataProcessingTerms;

	private final ActionListener actionListener = new ActionListener() {
		public void actionPerformed(final ActionEvent e) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					enableButtonIfPossible();
				}
			});
		}
	};	
	
	public DocearLicenseDialogLWPanel() {		
		setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_COLSPEC,},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("fill:default"),
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("fill:default"),
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("fill:default"),}));
		final DocearLicensePanel licenseText = new DocearLicensePanel();
		chckbxAcceptTermsOfUse = new JCheckBox(TextUtils.getText("docear.license.ckbx.accept.a"));
		add(chckbxAcceptTermsOfUse, "2, 2");
		chckbxAcceptTermsOfUse.addActionListener(actionListener);
		
		JLabel lblTermsOfUse = new TextPopup(TextUtils.getText("docear.license.ckbx.link"));
		lblTermsOfUse.addMouseListener(new MouseListener() {
			public void mouseReleased(MouseEvent e) {}
			public void mousePressed(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			public void mouseEntered(MouseEvent e) {}
			public void mouseClicked(MouseEvent e) {
				licenseText.setLicenseText(getTermsOfUse());
				JOptionPane.showConfirmDialog(DocearLicenseDialogLWPanel.this, licenseText, TextUtils.getText("docear.license.section.a"), JOptionPane.PLAIN_MESSAGE);
			}
		});
		add(lblTermsOfUse, "4, 2, fill, fill");
		
		chckbxAcceptDataPrivacyTerms = new JCheckBox(TextUtils.getText("docear.license.ckbx.accept.b"));
		add(chckbxAcceptDataPrivacyTerms, "2, 4");
		chckbxAcceptDataPrivacyTerms.addActionListener(actionListener);
		
		 
		JLabel lblDataPrivacyTerms = new TextPopup(TextUtils.getText("docear.license.ckbx.link"));
		lblDataPrivacyTerms.addMouseListener(new MouseListener() {
			public void mouseReleased(MouseEvent e) {}
			public void mousePressed(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			public void mouseEntered(MouseEvent e) {}
			public void mouseClicked(MouseEvent e) {
				licenseText.setLicenseText(getDataPrivacyTerms());
				JOptionPane.showConfirmDialog(DocearLicenseDialogLWPanel.this, licenseText, TextUtils.getText("docear.license.section.b"), JOptionPane.PLAIN_MESSAGE);
			}
		});
		add(lblDataPrivacyTerms, "4, 4, fill, fill");
		
		chckbxAcceptDataProcessingTerms = new JCheckBox(TextUtils.getText("docear.license.ckbx.accept.c"));
		add(chckbxAcceptDataProcessingTerms, "2, 6");
		
		chckbxAcceptDataProcessingTerms.addActionListener(actionListener);
		JLabel lblDataProcessingTerms = new TextPopup(TextUtils.getText("docear.license.ckbx.link"));
		lblDataProcessingTerms.addMouseListener(new MouseListener() {
			public void mouseReleased(MouseEvent e) {}
			public void mousePressed(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			public void mouseEntered(MouseEvent e) {}
			public void mouseClicked(MouseEvent e) {
				licenseText.setLicenseText(getDataProcessingTerms());
				JOptionPane.showConfirmDialog(DocearLicenseDialogLWPanel.this, licenseText, TextUtils.getText("docear.license.section.c"), JOptionPane.PLAIN_MESSAGE);
			}
		});
		add(lblDataProcessingTerms, "4, 6, fill, fill");
	}
	
	public void integrateButtons(JButton[] buttons) {
		okButton = buttons[0];
		enableButtonIfPossible();
		for(int i=1; i < buttons.length; i++) {
			buttons[i].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					closeDialogManually();
				}
			});
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
	
	private String getDataProcessingTerms() {
		try {
			return getStringFromStream(DocearController.class.getResourceAsStream("/Docear_data_processing.txt"));
		}
		catch (IOException e) {
			LogUtils.warn(e);
			return "Data Processing";
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
	
	private void closeDialogManually() {
		Container container = getParent();
		while(!(container instanceof JDialog)) {
			container = container.getParent();
		}
		((JDialog)container).dispose();
	}
	
	private void enableButtonIfPossible() {
		if(okButton != null) {
			if(chckbxAcceptTermsOfUse.isSelected() && chckbxAcceptDataPrivacyTerms.isSelected() && chckbxAcceptDataProcessingTerms.isSelected()) {
				okButton.setEnabled(true);
			}
			else {
				okButton.setEnabled(false);
			}			
		}
	}
	
	private class TextPopup extends JLabel implements MouseListener {
		private static final long serialVersionUID = 1L;

		public TextPopup(final String label) {
			super("<html><u>"+label+"</u></html>");
			this.addMouseListener(this);
			this.setForeground(Color.blue);
		}
		
		public void mouseClicked(MouseEvent e) {
			
		}
		public void mousePressed(MouseEvent e) {}
		public void mouseReleased(MouseEvent e) {}
		
		public void mouseEntered(MouseEvent e) {
			this.setForeground(Color.red);
			this.revalidate();
		}
		
		public void mouseExited(MouseEvent e) {
			this.setForeground(Color.blue);
			this.revalidate();
		}
	}

}
