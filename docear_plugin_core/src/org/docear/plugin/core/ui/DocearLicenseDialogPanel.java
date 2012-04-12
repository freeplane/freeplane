package org.docear.plugin.core.ui;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.factories.FormFactory;
import javax.swing.border.TitledBorder;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JCheckBox;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.freeplane.core.util.TextUtils;

public class DocearLicenseDialogPanel extends JPanel {
	
	private static final long serialVersionUID = 1L;
	
	private JButton okButton;
	
	private JTextArea txtrLicenseA;
	private JCheckBox chckbxAcceptLicenseA;
	private JTextArea txtrLicenseB;
	private JCheckBox chckbxAcceptLicenseB;

	private final ActionListener actionListener = new ActionListener() {
		public void actionPerformed(final ActionEvent e) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					enableButtonIfPossible();
				}
			});
		}
	};
	
	public DocearLicenseDialogPanel() {
		setPreferredSize(new Dimension(600, 450));
		
		setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_COLSPEC,},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("fill:default:grow"),
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("fill:default:grow"),}));
		
		JPanel SectionLicenseAPanel = new JPanel();
		SectionLicenseAPanel.setBorder(new TitledBorder(null, TextUtils.getText("docear.license.section.a"), TitledBorder.LEADING, TitledBorder.TOP, null, null));
		add(SectionLicenseAPanel, "2, 2, fill, fill");
		SectionLicenseAPanel.setLayout(new FormLayout(new ColumnSpec[] {
				ColumnSpec.decode("default:grow"),
				ColumnSpec.decode("2px"),},
			new RowSpec[] {
				RowSpec.decode("25px:grow"),
				RowSpec.decode("2px"),
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,}));
		
		JScrollPane scrollPane = new JScrollPane();
		SectionLicenseAPanel.add(scrollPane, "1, 1, fill, fill");
		
		txtrLicenseA = new JTextArea();
		txtrLicenseA.setEditable(false);
		txtrLicenseA.setRows(10);
		txtrLicenseA.setText("LicenseA");
		scrollPane.setViewportView(txtrLicenseA);
		
		chckbxAcceptLicenseA = new JCheckBox(TextUtils.getText("docear.license.ckbx.accept.a"));
		chckbxAcceptLicenseA.addActionListener(actionListener);
		SectionLicenseAPanel.add(chckbxAcceptLicenseA, "1, 4");
		
		JPanel SectionLicenseBPanel = new JPanel();
		SectionLicenseBPanel.setBorder(new TitledBorder(null, TextUtils.getText("docear.license.section.b"), TitledBorder.LEADING, TitledBorder.TOP, null, null));
		add(SectionLicenseBPanel, "2, 4, fill, fill");
		SectionLicenseBPanel.setLayout(new FormLayout(new ColumnSpec[] {
				ColumnSpec.decode("default:grow"),
				ColumnSpec.decode("2px"),},
			new RowSpec[] {
				RowSpec.decode("25px:grow"),
				RowSpec.decode("2px"),
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,}));
		
		JScrollPane scrollPane_1 = new JScrollPane();
		SectionLicenseBPanel.add(scrollPane_1, "1, 1, fill, fill");
		
		txtrLicenseB = new JTextArea();
		txtrLicenseB.setEditable(false);
		txtrLicenseB.setRows(10);
		txtrLicenseB.setText("LicenseB");
		scrollPane_1.setViewportView(txtrLicenseB);
		
		chckbxAcceptLicenseB = new JCheckBox(TextUtils.getText("docear.license.ckbx.accept.b"));
		chckbxAcceptLicenseB.addActionListener(actionListener);
		SectionLicenseBPanel.add(chckbxAcceptLicenseB, "1, 4");
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
	
	public void setLicenseText(boolean textA, String text) {
		if(textA) {
			txtrLicenseA.setText(text);
			txtrLicenseA.setSelectionStart(0);
			txtrLicenseA.setSelectionEnd(0);
		}
		else {
			txtrLicenseB.setText(text);
			txtrLicenseB.setSelectionStart(0);
			txtrLicenseB.setSelectionEnd(0);
		}
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
			if(chckbxAcceptLicenseA.isSelected() && chckbxAcceptLicenseB.isSelected()) {
				okButton.setEnabled(true);
			}
			else {
					okButton.setEnabled(false);
			}			
		}
	}

}
