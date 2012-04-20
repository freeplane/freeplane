package org.docear.plugin.core.ui;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

import org.docear.plugin.core.DocearController;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class DocearLicenseDialogPanel extends JPanel {
	
	public enum LICENSE_POSITION {
		TOP, MIDDLE, BOTTOM 
	}
	
	private static final long serialVersionUID = 1L;
	
	private JButton okButton;
	
	private JTextArea txtTermsOfUse;
	private JCheckBox chckbxAcceptTermsOfUse;
	private JTextArea txtDataPrivacyTerms;
	private JCheckBox chckbxAcceptDataPrivacyTerms;

	private final ActionListener actionListener = new ActionListener() {
		public void actionPerformed(final ActionEvent e) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					enableButtonIfPossible();
				}
			});
		}
	};
	private JPanel SectionLicenseCPanel;
	private JScrollPane scrollPane_2;
	private JTextArea txtDataProcessingTerms;
	private JCheckBox chckbxAcceptDataProcessingTerms;
	
	public DocearLicenseDialogPanel() {
		setPreferredSize(new Dimension(600, 450));
		
		setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_COLSPEC,},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("fill:default:grow"),
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("fill:default:grow"),
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("fill:default:grow"),
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,}));
		
		JPanel SectionLicenseAPanel = new JPanel();
		SectionLicenseAPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), TextUtils.getText("docear.license.section.a"), TitledBorder.CENTER, TitledBorder.TOP, null, new Color(0, 0, 0)));
		add(SectionLicenseAPanel, "2, 2, 5, 1, fill, fill");
		SectionLicenseAPanel.setLayout(new FormLayout(new ColumnSpec[] {
				ColumnSpec.decode("default:grow"),
				ColumnSpec.decode("2px"),},
			new RowSpec[] {
				RowSpec.decode("25px:grow"),
				RowSpec.decode("2px"),}));
		
		JScrollPane scrollPane = new JScrollPane();
		SectionLicenseAPanel.add(scrollPane, "1, 1, fill, fill");
		
		txtTermsOfUse = new JTextArea();
		txtTermsOfUse.setEditable(false);
		txtTermsOfUse.setRows(10);
		txtTermsOfUse.setText(getTermsOfUse());
		txtTermsOfUse.setLineWrap(true);
		scrollPane.setViewportView(txtTermsOfUse);
		
		JPanel SectionLicenseBPanel = new JPanel();
		SectionLicenseBPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), TextUtils.getText("docear.license.section.b"), TitledBorder.CENTER, TitledBorder.TOP, null, new Color(0, 0, 0)));
		add(SectionLicenseBPanel, "2, 4, 5, 1, fill, fill");
		SectionLicenseBPanel.setLayout(new FormLayout(new ColumnSpec[] {
				ColumnSpec.decode("default:grow"),
				ColumnSpec.decode("2px"),},
			new RowSpec[] {
				RowSpec.decode("25px:grow"),
				RowSpec.decode("2px"),}));
		
		JScrollPane scrollPane_1 = new JScrollPane();
		SectionLicenseBPanel.add(scrollPane_1, "1, 1, fill, fill");
		
		txtDataPrivacyTerms = new JTextArea();
		txtDataPrivacyTerms.setEditable(false);
		txtDataPrivacyTerms.setRows(10);
		txtDataPrivacyTerms.setText(getDataPrivacyTerms());
		scrollPane_1.setViewportView(txtDataPrivacyTerms);
		
		SectionLicenseCPanel = new JPanel();
		SectionLicenseCPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), TextUtils.getText("docear.license.section.c"), TitledBorder.CENTER, TitledBorder.TOP, null, new Color(0, 0, 0)));
		add(SectionLicenseCPanel, "2, 6, 5, 1, fill, fill");
		SectionLicenseCPanel.setLayout(new FormLayout(new ColumnSpec[] {
				ColumnSpec.decode("default:grow"),
				ColumnSpec.decode("2px"),},
			new RowSpec[] {
				RowSpec.decode("25px:grow"),
				RowSpec.decode("2px"),}));
		
		scrollPane_2 = new JScrollPane();
		SectionLicenseCPanel.add(scrollPane_2, "1, 1, fill, fill");
		
		txtDataProcessingTerms = new JTextArea();
		txtDataProcessingTerms.setEditable(false);
		txtDataProcessingTerms.setRows(10);
		txtDataProcessingTerms.setText(getDataProcessingTerms());
		scrollPane_2.setViewportView(txtDataProcessingTerms);
		
		chckbxAcceptTermsOfUse = new JCheckBox(TextUtils.getText("docear.license.ckbx.accept.a"));
		add(chckbxAcceptTermsOfUse, "2, 8");
		
		chckbxAcceptDataPrivacyTerms = new JCheckBox(TextUtils.getText("docear.license.ckbx.accept.b"));
		add(chckbxAcceptDataPrivacyTerms, "4, 8");
		chckbxAcceptDataPrivacyTerms.addActionListener(actionListener);
		
		chckbxAcceptDataProcessingTerms = new JCheckBox(TextUtils.getText("docear.license.ckbx.accept.c"));
		add(chckbxAcceptDataProcessingTerms, "6, 8");
		
		chckbxAcceptDataProcessingTerms.addActionListener(actionListener);
		chckbxAcceptTermsOfUse.addActionListener(actionListener);
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
	
//	public void setLicenseText(LICENSE_POSITION position, String text) {
//		switch(position) {
//			case BOTTOM: {
//				txtrLicenseC.setText(text);
//				txtrLicenseC.setSelectionStart(0);
//				txtrLicenseC.setSelectionEnd(0);
//				break;
//			}
//			case MIDDLE: {
//				txtrLicenseB.setText(text);
//				txtrLicenseB.setSelectionStart(0);
//				txtrLicenseB.setSelectionEnd(0);
//				break;
//			}
//			default: {
//				txtTermsOfUse.setText(text);
//				txtTermsOfUse.setSelectionStart(0);
//				txtTermsOfUse.setSelectionEnd(0);
//				break;
//			}
//		}
//	}
	
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
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
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

}
