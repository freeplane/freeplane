/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is modified by Dimitry Polivaev in 2008.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.freeplane.core.ui.components;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.WindowConstants;

import org.freeplane.core.util.TextUtils;

/** */
public class EnterPasswordDialog extends JDialog {
	public static final int CANCEL = -1;
	public static final int OK = 1;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private boolean enterTwoPasswords = true;
	private JButton jCancelButton = null;
	private javax.swing.JPanel jContentPane = null;
	private JLabel jLabel = null;
	private JLabel jLabel1 = null;
	private JLabel jLabel2 = null;
	private JButton jOKButton = null;
	private JPasswordField jPasswordField = null;
	private JPasswordField jPasswordField1 = null;
	private StringBuilder password = null;
	private int result = EnterPasswordDialog.CANCEL;


	/**
	 * This is the default constructor
	 */
	public EnterPasswordDialog(final Frame frame, final boolean enterTwoPasswords) {
		super(frame, "", true /* =modal */);
		this.enterTwoPasswords = enterTwoPasswords;
		this.setTitle(TextUtils.getText("accessories/plugins/EncryptNode.properties_0"));
		this.setContentPane(getJContentPane());
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent we) {
				cancelPressed();
			}
		});
		pack();
	}

	private void cancelPressed() {
		password = null;
		result = EnterPasswordDialog.CANCEL;
		close();
	}

	/**
	 */
	private boolean checkEqualAndMinimumSize() {
		final char[] a1 = jPasswordField.getPassword();
		if (a1.length < 2) {
			return false;
		}
		if (enterTwoPasswords) {
			final char[] a2 = jPasswordField1.getPassword();
			if (a1.length != a2.length) {
				return false;
			}
			for (int i = 0; i < a1.length; i++) {
				if (a1[i] != a2[i]) {
					return false;
				}
			}
		}
		return true;
	}

	private void close() {
		this.dispose();
	}

	/**
	 * This method initializes jButton1
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getJCancelButton() {
		if (jCancelButton == null) {
			jCancelButton = new JButton();
			jCancelButton.setAction(new AbstractAction() {
				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

				public void actionPerformed(final ActionEvent e) {
					cancelPressed();
				}
			});
			jCancelButton.setText(TextUtils.getText("accessories/plugins/EncryptNode.properties_7"));
		}
		return jCancelButton;
	}

	/**
	 * This method initializes jContentPane
	 *
	 * @return javax.swing.JPanel
	 */
	private javax.swing.JPanel getJContentPane() {
		if (jContentPane == null) {
			jLabel2 = new JLabel();
			jLabel1 = new JLabel();
			jLabel = new JLabel();
			final GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			final GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			final GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			final GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
			final GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
			final GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
			final GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
			jContentPane = new javax.swing.JPanel();
			jContentPane.setLayout(new GridBagLayout());
			gridBagConstraints1.gridx = 0;
			gridBagConstraints1.gridy = 1;
			gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
			jLabel.setText(TextUtils.getText("accessories/plugins/EncryptNode.properties_2"));
			gridBagConstraints2.gridx = 0;
			gridBagConstraints2.gridy = 2;
			gridBagConstraints2.fill = java.awt.GridBagConstraints.HORIZONTAL;
			jLabel1.setText(TextUtils.getText("accessories/plugins/EncryptNode.properties_3"));
			gridBagConstraints3.gridx = 1;
			gridBagConstraints3.gridy = 1;
			gridBagConstraints3.weightx = 1.0;
			gridBagConstraints3.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints4.gridx = 1;
			gridBagConstraints4.gridy = 2;
			gridBagConstraints4.weightx = 1.0;
			gridBagConstraints4.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints5.gridx = 0;
			gridBagConstraints5.gridy = 0;
			gridBagConstraints5.gridwidth = 2;
			gridBagConstraints5.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints5.anchor = java.awt.GridBagConstraints.CENTER;
			gridBagConstraints5.insets = new java.awt.Insets(0, 0, 20, 0);
			jLabel2.setText(TextUtils.getText("accessories/plugins/EncryptNode.properties_4"));
			jLabel2.setToolTipText(TextUtils.getText("accessories/plugins/EncryptNode.properties_5"));
			gridBagConstraints6.gridx = 0;
			gridBagConstraints6.gridy = 3;
			gridBagConstraints6.insets = new java.awt.Insets(20, 0, 0, 0);
			gridBagConstraints7.gridx = 1;
			gridBagConstraints7.gridy = 3;
			gridBagConstraints7.insets = new java.awt.Insets(20, 0, 0, 0);
			jContentPane.add(jLabel, gridBagConstraints1);
			jContentPane.add(getJPasswordField(), gridBagConstraints3);
			jContentPane.add(jLabel2, gridBagConstraints5);
			if (enterTwoPasswords) {
				jContentPane.add(getJPasswordField1(), gridBagConstraints4);
				jContentPane.add(jLabel1, gridBagConstraints2);
			}
			jContentPane.add(getJOKButton(), gridBagConstraints6);
			jContentPane.add(getJCancelButton(), gridBagConstraints7);
			getRootPane().setDefaultButton(getJOKButton());
		}
		return jContentPane;
	}

	/**
	 * This method initializes jButton
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getJOKButton() {
		if (jOKButton == null) {
			jOKButton = new JButton();
			jOKButton.setAction(new AbstractAction() {
				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

				public void actionPerformed(final ActionEvent e) {
					okPressed();
				}
			});
			jOKButton.setText(TextUtils.getText("accessories/plugins/EncryptNode.properties_6"));
		}
		return jOKButton;
	}

	/**
	 * This method initializes jPasswordField
	 *
	 * @return javax.swing.JPasswordField
	 */
	private JPasswordField getJPasswordField() {
		if (jPasswordField == null) {
			jPasswordField = new JPasswordField(20);
		}
		return jPasswordField;
	}

	/**
	 * This method initializes jPasswordField1
	 *
	 * @return javax.swing.JPasswordField
	 */
	private JPasswordField getJPasswordField1() {
		if (jPasswordField1 == null) {
			jPasswordField1 = new JPasswordField();
		}
		return jPasswordField1;
	}

	/**
	 * @return Returns the password.
	 */
	public StringBuilder getPassword() {
		return password;
	}

	/**
	 * @return Returns the result.
	 */
	public int getResult() {
		return result;
	}

	private void okPressed() {
		if (!checkEqualAndMinimumSize()) {
			JOptionPane.showMessageDialog(this, TextUtils.getText("accessories/plugins/EncryptNode.properties_1"));
			return;
		}
		password = new StringBuilder();
		password.append(jPasswordField.getPassword());
		result = EnterPasswordDialog.OK;
		close();
	}
}
