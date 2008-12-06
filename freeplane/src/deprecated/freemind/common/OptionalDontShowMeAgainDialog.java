/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
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
package deprecated.freemind.common;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;

import org.freeplane.controller.Freeplane;
import org.freeplane.main.Tools;
import org.freeplane.ui.FreemindMenuBar;

/**
 * Dialog with a decision that can be disabled.
 *
 * @author foltin
 */
public class OptionalDontShowMeAgainDialog {
	public interface IDontShowPropertyHandler {
		/**
		 * @return accepted are the following values as return values: * ""
		 *         (means: show this dialog) * "true" (means: the answer was ok
		 *         and I want to remember that). * "false" (means: the answer
		 *         was cancel and I want to remember that).
		 */
		String getProperty();

		void setProperty(String pValue);
	}

	/**
	 * Standard property handler, if you have a controller and a property.
	 */
	public static class StandardPropertyHandler implements
	        IDontShowPropertyHandler {
		final private String mPropertyName;

		public StandardPropertyHandler(final String pPropertyName) {
			mPropertyName = pPropertyName;
		}

		public String getProperty() {
			return Freeplane.getController().getResourceController()
			    .getProperty(mPropertyName);
		}

		public void setProperty(final String pValue) {
			Freeplane.getController().getResourceController().setProperty(
			    mPropertyName, pValue);
		}
	}

	public final static int BOTH_OK_AND_CANCEL_OPTIONS_ARE_STORED = 1;
	public final static int ONLY_OK_SELECTION_IS_STORED = 0;
	final private Component mComponent;
	private JDialog mDialog;
	private JCheckBox mDontShowAgainBox;
	final private IDontShowPropertyHandler mDontShowPropertyHandler;
	final private String mMessageId;
	final private int mMessageType;
	final private JFrame mParent;
	private int mResult = JOptionPane.CANCEL_OPTION;
	final private String mTitleId;

	public OptionalDontShowMeAgainDialog(
	                                     final JFrame pFrame,
	                                     final Component pComponent,
	                                     final String pMessageId,
	                                     final String pTitleId,
	                                     final IDontShowPropertyHandler pDontShowPropertyHandler,
	                                     final int pMessageType) {
		mComponent = pComponent;
		mParent = pFrame;
		mMessageId = pMessageId;
		mTitleId = pTitleId;
		mDontShowPropertyHandler = pDontShowPropertyHandler;
		mMessageType = pMessageType;
	}

	private void close(final int pResult) {
		mResult = pResult;
		if (mDontShowAgainBox.isSelected()) {
			if (mMessageType == OptionalDontShowMeAgainDialog.ONLY_OK_SELECTION_IS_STORED) {
				if (mResult == JOptionPane.OK_OPTION) {
					mDontShowPropertyHandler.setProperty("true");
				}
			}
			else {
				mDontShowPropertyHandler
				    .setProperty((mResult == JOptionPane.OK_OPTION) ? "true"
				            : "false");
			}
		}
		else {
			mDontShowPropertyHandler.setProperty("");
		}
		mDialog.setVisible(false);
		mDialog.dispose();
	}

	/**
	 * @return an int from JOptionPane (eg. JOptionPane.OK_OPTION).
	 */
	public int getResult() {
		return mResult;
	}

	public OptionalDontShowMeAgainDialog show() {
		final String property = mDontShowPropertyHandler.getProperty();
		if (Tools.safeEquals(property, "true")) {
			mResult = JOptionPane.OK_OPTION;
			return this;
		}
		if (Tools.safeEquals(property, "false")) {
			mResult = JOptionPane.CANCEL_OPTION;
			return this;
		}
		mDialog = null;
		mDialog = new JDialog(mParent, Freeplane.getController()
		    .getResourceController().getResourceString(mTitleId));
		mDialog.setModal(true);
		mDialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		final AbstractAction cancelAction = new AbstractAction() {
			public void actionPerformed(final ActionEvent pE) {
				close(JOptionPane.CANCEL_OPTION);
			}
		};
		final AbstractAction okAction = new AbstractAction() {
			public void actionPerformed(final ActionEvent pE) {
				close(JOptionPane.OK_OPTION);
			}
		};
		Tools.addEscapeActionToDialog(mDialog, cancelAction);
		mDialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent pE) {
				close(JOptionPane.CANCEL_OPTION);
			}
		});
		mDialog.getContentPane().setLayout(new GridBagLayout());
		mDialog.getContentPane().add(
		    new JLabel(Freeplane.getController().getResourceController()
		        .getResourceString(mMessageId)),
		    new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0,
		        GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(5,
		            5, 0, 0), 0, 0));
		final ImageIcon questionMark = new ImageIcon(Freeplane.getController()
		    .getResourceController().getResource("images/icons/help.png"));
		mDialog.getContentPane().add(
		    new JLabel(questionMark),
		    new GridBagConstraints(0, 0, 1, 2, 1.0, 2.0,
		        GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(5,
		            5, 0, 0), 0, 0));
		String boxString;
		if (mMessageType == OptionalDontShowMeAgainDialog.ONLY_OK_SELECTION_IS_STORED) {
			boxString = "OptionalDontShowMeAgainDialog.dontShowAgain";
		}
		else {
			boxString = "OptionalDontShowMeAgainDialog.rememberMyDescision";
		}
		mDontShowAgainBox = new JCheckBox(Freeplane.getController()
		    .getResourceController().getResourceString(boxString));
		FreemindMenuBar.setLabelAndMnemonic(mDontShowAgainBox, null);
		mDialog.getContentPane().add(
		    mDontShowAgainBox,
		    new GridBagConstraints(0, 2, 3, 1, 1.0, 1.0,
		        GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(5,
		            5, 0, 0), 0, 0));
		final JButton okButton = new JButton(Freeplane.getController()
		    .getResourceController().getResourceString(
		        "OptionalDontShowMeAgainDialog.ok"));
		FreemindMenuBar.setLabelAndMnemonic(okButton, null);
		okButton.addActionListener(okAction);
		mDialog.getContentPane().add(
		    okButton,
		    new GridBagConstraints(2, 3, 1, 1, 1.0, 1.0,
		        GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(5,
		            5, 0, 0), 0, 0));
		final JButton cancelButton = new JButton(Freeplane.getController()
		    .getResourceController().getResourceString(
		        "OptionalDontShowMeAgainDialog.cancel"));
		FreemindMenuBar.setLabelAndMnemonic(cancelButton, null);
		cancelButton.addActionListener(cancelAction);
		mDialog.getContentPane().add(
		    cancelButton,
		    new GridBagConstraints(3, 3, 1, 1, 1.0, 1.0,
		        GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(5,
		            5, 0, 0), 0, 0));
		mDialog.getRootPane().setDefaultButton(okButton);
		mDialog.pack();
		Tools.setDialogLocationRelativeTo(mDialog, mComponent);
		mDialog.setVisible(true);
		return this;
	}
}
