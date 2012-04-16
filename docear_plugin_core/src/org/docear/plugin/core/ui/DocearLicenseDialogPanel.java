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
import javax.swing.UIManager;
import java.awt.Color;

public class DocearLicenseDialogPanel extends JPanel {
	
	public enum LICENSE_POSITION {
		TOP, MIDDLE, BOTTOM 
	}
	
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
	private JPanel SectionLicenseCPanel;
	private JScrollPane scrollPane_2;
	private JTextArea txtrLicenseC;
	private JCheckBox chckbxAcceptLicenseC;
	
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
		
		txtrLicenseA = new JTextArea();
		txtrLicenseA.setEditable(false);
		txtrLicenseA.setRows(10);
		txtrLicenseA.setText("LicenseA");
		scrollPane.setViewportView(txtrLicenseA);
		
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
		
		txtrLicenseB = new JTextArea();
		txtrLicenseB.setEditable(false);
		txtrLicenseB.setRows(10);
		txtrLicenseB.setText("LicenseB");
		scrollPane_1.setViewportView(txtrLicenseB);
		
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
		
		txtrLicenseC = new JTextArea();
		txtrLicenseC.setEditable(false);
		txtrLicenseC.setRows(10);
		txtrLicenseC.setText("LicenseC");
		scrollPane_2.setViewportView(txtrLicenseC);
		
		chckbxAcceptLicenseA = new JCheckBox(TextUtils.getText("docear.license.ckbx.accept.a"));
		add(chckbxAcceptLicenseA, "2, 8");
		
		chckbxAcceptLicenseB = new JCheckBox(TextUtils.getText("docear.license.ckbx.accept.b"));
		add(chckbxAcceptLicenseB, "4, 8");
		chckbxAcceptLicenseB.addActionListener(actionListener);
		
		chckbxAcceptLicenseC = new JCheckBox(TextUtils.getText("docear.license.ckbx.accept.c"));
		add(chckbxAcceptLicenseC, "6, 8");
		
		chckbxAcceptLicenseC.addActionListener(actionListener);
		chckbxAcceptLicenseA.addActionListener(actionListener);
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
	
	public void setLicenseText(LICENSE_POSITION position, String text) {
		switch(position) {
			case BOTTOM: {
				txtrLicenseC.setText(text);
				txtrLicenseC.setSelectionStart(0);
				txtrLicenseC.setSelectionEnd(0);
				break;
			}
			case MIDDLE: {
				txtrLicenseB.setText(text);
				txtrLicenseB.setSelectionStart(0);
				txtrLicenseB.setSelectionEnd(0);
				break;
			}
			default: {
				txtrLicenseA.setText(text);
				txtrLicenseA.setSelectionStart(0);
				txtrLicenseA.setSelectionEnd(0);
				break;
			}
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
			if(chckbxAcceptLicenseA.isSelected() && chckbxAcceptLicenseB.isSelected() && chckbxAcceptLicenseC.isSelected()) {
				okButton.setEnabled(true);
			}
			else {
				okButton.setEnabled(false);
			}			
		}
	}

}
