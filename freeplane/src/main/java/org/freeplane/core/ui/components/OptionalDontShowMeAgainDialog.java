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
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;

import org.apache.commons.lang.StringUtils;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.LabelAndMnemonicSetter;
import org.freeplane.core.ui.textchanger.TranslatedElementFactory;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.IMapSelection;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;

/**
 * Dialog with a decision that can be disabled.
 *
 * @author foltin
 */
public class OptionalDontShowMeAgainDialog {
	public static final String CONFIRMATION = "confirmation";
	public static final String REMEMBER_MY_DESCISION = "OptionalDontShowMeAgainDialog.rememberMyDescision";
	public static final String DONT_SHOW_AGAIN = "OptionalDontShowMeAgainDialog.dontShowAgain";
	public enum MessageType {ONLY_OK_SELECTION_IS_STORED, ONLY_CANCEL_SELECTION_IS_STORED,
	    BOTH_OK_AND_CANCEL_OPTIONS_ARE_STORED , ONLY_OK_SELECTION_IS_SHOWN }

	static public int show(final String propertyName, final MessageType messageType) {
		return show("OptionPanel." + propertyName, CONFIRMATION, propertyName, messageType);
	}

	static public int show(final String messageId, final String propertyName, final MessageType messageType) {
		return show(messageId, CONFIRMATION, propertyName, messageType);
	}


    static public int showWithExplanation(final String messageId, final String explanationId,
                           final String propertyName, final MessageType messageType) {
        return new OptionalDontShowMeAgainDialog(messageId, explanationId, null, propertyName, messageType).show()
            .getResult();
    }

    static public int show(final String messageId, final String titleId,
	                       final String propertyName, final MessageType messageType) {
		return new OptionalDontShowMeAgainDialog(messageId, null, titleId, propertyName, messageType).show()
		    .getResult();
	}

	// // 	private final Controller controller;
	private JDialog dialog;
	private JCheckBox mDontShowAgainBox;
    final private String messageId;
    final private String explanationId;
	final private MessageType messageType;
	final private NodeModel node;
	final private Frame parentComponent;
	final private String propertyName;
	private int mResult = JOptionPane.CANCEL_OPTION;
	final private String titleId;

	private OptionalDontShowMeAgainDialog(final String messageId, String explanationId, final String titleId,
	                                      final String propertyName, final MessageType messageType) {
		//		this.controller = controller;
		Controller controller = Controller.getCurrentController();
        this.explanationId = explanationId != null ? explanationId : messageId + ".explanation";
		this.parentComponent = UITools.getCurrentFrame();
		final IMapSelection selection = controller.getSelection();
		if (selection != null) {
			this.node = selection.getSelected();
		}
		else {
			this.node = null;
		}
		this.messageId = messageId;
		this.titleId = titleId;
		this.propertyName = propertyName;
		this.messageType = messageType;
	}

	private void close(final int pResult) {
		mResult = pResult;
		if (mDontShowAgainBox.isSelected()) {
		    switch (messageType) {
            case ONLY_OK_SELECTION_IS_STORED:
                if (mResult == JOptionPane.OK_OPTION) {
                    setProperty("true");
                }
                break;

            case ONLY_CANCEL_SELECTION_IS_STORED:
                if (mResult == JOptionPane.CANCEL_OPTION) {
                    setProperty("false");
                }
                break;
            default:
                setProperty((mResult == JOptionPane.OK_OPTION) ? "true" : "false");
                break;
            }
		}
		dialog.setVisible(false);
		dialog.dispose();
	}

	private String getProperty() {
		return ResourceController.getResourceController().getProperty(propertyName);
	}

	/**
	 * @return an int from JOptionPane (eg. JOptionPane.OK_OPTION).
	 */
	private int getResult() {
		return mResult;
	}

	private void setProperty(final String value) {
		ResourceController.getResourceController().setProperty(propertyName, value);
	}

	private OptionalDontShowMeAgainDialog show() {
		final String property = getProperty();
		if (messageType != MessageType.ONLY_CANCEL_SELECTION_IS_STORED && StringUtils.equals(property, "true")) {
			mResult = JOptionPane.OK_OPTION;
			return this;
		}
        if ((messageType == MessageType.BOTH_OK_AND_CANCEL_OPTIONS_ARE_STORED
                || messageType == MessageType.ONLY_CANCEL_SELECTION_IS_STORED)
                && StringUtils.equals(property, "false")) {
            mResult = JOptionPane.CANCEL_OPTION;
            return this;
        }
        if (GraphicsEnvironment.isHeadless()) {
            mResult = JOptionPane.CANCEL_OPTION;
            return this;
        }
        String boxString;
        if (messageType != MessageType.BOTH_OK_AND_CANCEL_OPTIONS_ARE_STORED) {
            boxString = DONT_SHOW_AGAIN;
        }
        else {
            boxString = REMEMBER_MY_DESCISION;
        }
		dialog = new JDialog(parentComponent, TextUtils.getText(titleId != null ? titleId : boxString));
		dialog.setModal(true);
		dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		final AbstractAction cancelAction = new AbstractAction() {
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(final ActionEvent pE) {
				close(JOptionPane.CANCEL_OPTION);
			}
		};
		final AbstractAction okAction = new AbstractAction() {
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(final ActionEvent pE) {
				close(JOptionPane.OK_OPTION);
			}
		};
		UITools.addEscapeActionToDialog(dialog, cancelAction);
		dialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent pE) {
				close(JOptionPane.CANCEL_OPTION);
			}
		});
		dialog.getContentPane().setLayout(new GridBagLayout());
		String message = TextUtils.getText(messageId);
		final String explanation = TextUtils.getOptionalText(explanationId, null);
        JTextArea textArea = new JTextArea(explanation == null
                ? message
                : message + "\n" + explanation);
		textArea.setLineWrap(true);
		textArea.setEditable(false);
		textArea.setColumns(60);
		textArea.setSize(textArea.getPreferredSize());
		dialog.getContentPane().add(
		    textArea,
		    new GridBagConstraints(1, 0, GridBagConstraints.REMAINDER, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(
		        5, 5, 0, 0), 0, 0));
		final Icon questionMark;
		if (messageType == MessageType.ONLY_OK_SELECTION_IS_SHOWN) {
			questionMark = ResourceController.getResourceController().getIcon("/images/warning_msg.svg?useAccentColor=true");
		}
		else {
			questionMark = ResourceController.getResourceController().getIcon("/images/question_msg.svg?useAccentColor=true");
		}
		dialog.getContentPane().add(
		    new JLabel(questionMark),
		    new GridBagConstraints(0, 0, 1, 2, 1.0, 2.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(
		        5, 5, 0, 0), 0, 0));
		mDontShowAgainBox = new JCheckBox(TextUtils.getRawText(boxString));
		LabelAndMnemonicSetter.setLabelAndMnemonic(mDontShowAgainBox, null);
		dialog.getContentPane().add(
		    mDontShowAgainBox,
		    new GridBagConstraints(0, 2, 3, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(
		        5, 5, 0, 0), 0, 0));
		final String okText;
		if (messageType == MessageType.ONLY_OK_SELECTION_IS_SHOWN) {
			okText = TextUtils.getRawText("ok");
		}
		else {
			okText = TextUtils.getRawText("OptionalDontShowMeAgainDialog.ok");
		}
		final JButton okButton = new JButton(okText);
		TranslatedElementFactory.createTooltip(okButton, explanationId);
		LabelAndMnemonicSetter.setLabelAndMnemonic(okButton, null);
		okButton.addActionListener(okAction);
		dialog.getContentPane().add(
		    okButton,
		    new GridBagConstraints(1, 3, 1, 1, 1.0, 1.0, GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(
		        5, 5, 0, 0), 0, 0));
		if (messageType != MessageType.ONLY_OK_SELECTION_IS_SHOWN) {
			final JButton cancelButton = new JButton(TextUtils.getRawText("OptionalDontShowMeAgainDialog.cancel"));
			LabelAndMnemonicSetter.setLabelAndMnemonic(cancelButton, null);
			cancelButton.addActionListener(cancelAction);
			dialog.getContentPane().add(
			    cancelButton,
			    new GridBagConstraints(2, 3, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH,
			        new Insets(
			            5, 5, 0, 0),
			        0, 0));
		}
		dialog.getRootPane().setDefaultButton(okButton);
		dialog.pack();
		if (node != null) {
			UITools.setDialogLocationRelativeTo(dialog, node);
		}
		else {
			dialog.setLocationRelativeTo(null);
		}
		dialog.setVisible(true);
		return this;
	}
}
