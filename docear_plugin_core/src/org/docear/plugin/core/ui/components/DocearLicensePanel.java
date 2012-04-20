package org.docear.plugin.core.ui.components;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class DocearLicensePanel extends JPanel {
	
	private static final long serialVersionUID = 1L;
	private JTextArea txtrLicense;

	public DocearLicensePanel() {
		setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrollPane = new JScrollPane();
		add(scrollPane, BorderLayout.CENTER);
		
		txtrLicense = new JTextArea();
		txtrLicense.setColumns(80);
		txtrLicense.setRows(20);
		txtrLicense.setFont(new Font("Monospaced", Font.PLAIN, 11));
		txtrLicense.setEditable(false);
		txtrLicense.setLineWrap(true);
		txtrLicense.setWrapStyleWord(true);
		scrollPane.setViewportView(txtrLicense);
	}

	public void setLicenseText(final String text) {
		if(text != null) {
			txtrLicense.setText(text);
			txtrLicense.setSelectionStart(0);
			txtrLicense.setSelectionEnd(0);
		}
	}
	
}
