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
package org.freeplane.core.ui.components;

import java.awt.Frame;
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
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;

import org.apache.commons.lang.StringUtils;
import org.freeplane.core.controller.Controller;
import org.freeplane.core.modecontroller.IMapSelection;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.MenuBuilder;

/**
 * Dialog with a decision that can be disabled.
 *
 * @author foltin
 */
public class OptionalDontShowMeAgainDialog {
	public final static int BOTH_OK_AND_CANCEL_OPTIONS_ARE_STORED = 1;
	public final static int ONLY_OK_SELECTION_IS_STORED = 0;

	static public int show(final Controller controller, final String pMessageId, final String pTitleId,
	                       final String pPropertyName, final int pMessageType) {
		return new OptionalDontShowMeAgainDialog(controller, pMessageId, pTitleId, pPropertyName, pMessageType).show()
		    .getResult();
	}

	private final Controller controller;
	private JDialog mDialog;
	private JCheckBox mDontShowAgainBox;
	final private String mMessageId;
	final private int mMessageType;
	final private NodeModel mNode;
	final private Frame mParent;
	final private String mPropertyName;
	private int mResult = JOptionPane.CANCEL_OPTION;
	final private String mTitleId;

	private OptionalDontShowMeAgainDialog(final Controller controller, final String pMessageId, final String pTitleId,
	                                      final String pPropertyName, final int pMessageType) {
		this.controller = controller;
		mParent = controller.getViewController().getFrame();
		final IMapSelection selection = controller.getSelection();
		if (selection != null) {
			mNode = selection.getSelected();
		}
		else {
			mNode = null;
		}
		mMessageId = pMessageId;
		mTitleId = pTitleId;
		mPropertyName = pPropertyName;
		mMessageType = pMessageType;
	}

	private void close(final int pResult) {
		mResult = pResult;
		if (mDontShowAgainBox.isSelected()) {
			if (mMessageType == OptionalDontShowMeAgainDialog.ONLY_OK_SELECTION_IS_STORED) {
				if (mResult == JOptionPane.OK_OPTION) {
					setProperty("true");
				}
			}
			else {
				setProperty((mResult == JOptionPane.OK_OPTION) ? "true" : "false");
			}
		}
		else {
			setProperty("");
		}
		mDialog.setVisible(false);
		mDialog.dispose();
	}

	private String getProperty() {
		return ResourceController.getResourceController().getProperty(mPropertyName);
	}

	/**
	 * @return an int from JOptionPane (eg. JOptionPane.OK_OPTION).
	 */
	private int getResult() {
		return mResult;
	}

	private void setProperty(final String value) {
		ResourceController.getResourceController().setProperty(mPropertyName, value);
	}

	private OptionalDontShowMeAgainDialog show() {
		final String property = getProperty();
		if (StringUtils.equals(property, "true")) {
			mResult = JOptionPane.OK_OPTION;
			return this;
		}
		if (mMessageType == BOTH_OK_AND_CANCEL_OPTIONS_ARE_STORED && StringUtils.equals(property, "false")) {
			mResult = JOptionPane.CANCEL_OPTION;
			return this;
		}
		mDialog = null;
		mDialog = new JDialog(mParent, ResourceBundles.getText(mTitleId));
		mDialog.setModal(true);
		mDialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		final AbstractAction cancelAction = new AbstractAction() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void actionPerformed(final ActionEvent pE) {
				close(JOptionPane.CANCEL_OPTION);
			}
		};
		final AbstractAction okAction = new AbstractAction() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void actionPerformed(final ActionEvent pE) {
				close(JOptionPane.OK_OPTION);
			}
		};
		UITools.addEscapeActionToDialog(mDialog, cancelAction);
		mDialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent pE) {
				close(JOptionPane.CANCEL_OPTION);
			}
		});
		mDialog.getContentPane().setLayout(new GridBagLayout());
		mDialog.getContentPane().add(
		    new JLabel(ResourceBundles.getText(mMessageId)),
		    new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(
		        5, 5, 0, 0), 0, 0));
		final ImageIcon questionMark = new ImageIcon(ResourceController.getResourceController().getResource(
		    "/images/icons/help.png"));
		mDialog.getContentPane().add(
		    new JLabel(questionMark),
		    new GridBagConstraints(0, 0, 1, 2, 1.0, 2.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(
		        5, 5, 0, 0), 0, 0));
		String boxString;
		if (mMessageType == OptionalDontShowMeAgainDialog.ONLY_OK_SELECTION_IS_STORED) {
			boxString = "OptionalDontShowMeAgainDialog.dontShowAgain";
		}
		else {
			boxString = "OptionalDontShowMeAgainDialog.rememberMyDescision";
		}
		mDontShowAgainBox = new JCheckBox(ResourceBundles.getText(boxString));
		MenuBuilder.setLabelAndMnemonic(mDontShowAgainBox, null);
		mDialog.getContentPane().add(
		    mDontShowAgainBox,
		    new GridBagConstraints(0, 2, 3, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(
		        5, 5, 0, 0), 0, 0));
		final JButton okButton = new JButton(ResourceBundles.getText("OptionalDontShowMeAgainDialog.ok"));
		MenuBuilder.setLabelAndMnemonic(okButton, null);
		okButton.addActionListener(okAction);
		mDialog.getContentPane().add(
		    okButton,
		    new GridBagConstraints(2, 3, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(
		        5, 5, 0, 0), 0, 0));
		final JButton cancelButton = new JButton(ResourceBundles.getText("OptionalDontShowMeAgainDialog.cancel"));
		MenuBuilder.setLabelAndMnemonic(cancelButton, null);
		cancelButton.addActionListener(cancelAction);
		mDialog.getContentPane().add(
		    cancelButton,
		    new GridBagConstraints(3, 3, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(
		        5, 5, 0, 0), 0, 0));
		mDialog.getRootPane().setDefaultButton(okButton);
		mDialog.pack();
		if (mNode != null) {
			UITools.setDialogLocationRelativeTo(mDialog, controller, mNode);
		}
		else {
			mDialog.setLocationRelativeTo(null);
		}
		mDialog.setVisible(true);
		return this;
	}
}
