package org.docear.plugin.core.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;

import org.freeplane.core.util.TextUtils;

public class DocearLicenseDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();

	/**
	 * Show the dialog.
	 */
	public void showDialog(Component parent) {	
		this.setLocationRelativeTo(parent);
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		this.setTitle(TextUtils.getText("license", "License"));		
		this.setVisible(true);		
	}	

	/**
	 * Create the dialog.
	 */
	public DocearLicenseDialog(String licenseText) {
		setResizable(false);		
		setBounds(100, 100, 600, 400);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			JPanel panel = new JPanel();
			contentPanel.add(panel, BorderLayout.SOUTH);
			{
				JButton btnOk = new JButton(TextUtils.getText("GrabKeyDialog.common.ok", "OK"));
				btnOk.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						dispose();
					}
				});
				panel.add(btnOk);
			}
		}
		{
			JPanel panel = new JPanel();
			panel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
			contentPanel.add(panel, BorderLayout.CENTER);
			panel.setLayout(new BorderLayout(5, 5));
			{
				JScrollPane scrollPane = new JScrollPane();
				panel.add(scrollPane, BorderLayout.CENTER);
				{
					JTextArea textArea = new JTextArea();
					textArea.setEditable(false);
					textArea.setWrapStyleWord(true);
					textArea.setLineWrap(true);
					textArea.setText(licenseText);
					textArea.setCaretPosition(0);
					scrollPane.setViewportView(textArea);
				}
			}
		}
	}

}
