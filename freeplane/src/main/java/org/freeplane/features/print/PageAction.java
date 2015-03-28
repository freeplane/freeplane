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
package org.freeplane.features.print;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.LabelAndMnemonicSetter;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;

class PageAction extends AbstractPrintAction {
	static final String NAME = "page";
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	PageAction(final PrintController controller) {
		super("PageAction", controller);
	}

	public void actionPerformed(final ActionEvent e) {
		if (!getPrintController().acquirePrinterJobAndPageFormat(false)) {
			return;
		}
		final FitMap fitMap = FitMap.valueOf();
		//define controls
		//ButtonGroup
		final ButtonGroup fitButtons = new ButtonGroup();
		//Fit to page
		final JRadioButton fitToPage = new JRadioButton();
		LabelAndMnemonicSetter.setLabelAndMnemonic(fitToPage, TextUtils.getRawText("fit_map_to_page"));
		fitToPage.setSelected(fitMap == FitMap.PAGE);
		fitButtons.add(fitToPage);
		//Fit background image to page
		final JRadioButton fitBackgroundImageToPage = new JRadioButton();
		LabelAndMnemonicSetter.setLabelAndMnemonic(fitBackgroundImageToPage, TextUtils.getRawText("fit_background_to_page"));
		fitBackgroundImageToPage.setSelected(fitMap == FitMap.BACKGROUND);
		fitButtons.add(fitBackgroundImageToPage);
		//Fit width
		final JRadioButton fitToWidth = new JRadioButton();
		LabelAndMnemonicSetter.setLabelAndMnemonic(fitToWidth, TextUtils.getRawText("fit_map_to_page_width"));
		fitToWidth.setSelected(fitMap == FitMap.WIDTH);
		fitButtons.add(fitToWidth);
		//Fit to heighth
		final JRadioButton fitToHeight = new JRadioButton();
		LabelAndMnemonicSetter.setLabelAndMnemonic(fitToHeight, TextUtils.getRawText("fit_map_to_page_height"));
		fitToHeight.setSelected(fitMap == FitMap.HEIGHT);
		fitButtons.add(fitToHeight);
		//User defined
		final JRadioButton userDefaultScale = new JRadioButton();
		LabelAndMnemonicSetter.setLabelAndMnemonic(userDefaultScale, TextUtils.getRawText("user_defined_scale"));
		userDefaultScale.setSelected(fitMap == FitMap.USER_DEFINED);
		fitButtons.add(userDefaultScale);
		//User defined label
		final JLabel userZoomL = new JLabel(TextUtils.getText("user_zoom"));
		//User defined input field
		final JTextField userZoom = new JTextField(ResourceController.getResourceController().getProperty("user_zoom"),
		    3);
		userZoom.setPreferredSize(new Dimension(30, 21));
		if (fitMap == FitMap.USER_DEFINED) {
			userZoom.setEditable(true);
			userZoom.setEnabled(true);
		}
		else {
			userZoom.setEnabled(false);
			userZoom.setEditable(false);
		}
		//Set up dialog content
		final JPanel panel = new JPanel();
		final GridBagLayout gridbag = new GridBagLayout();
		panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(),
		    BorderFactory.createEmptyBorder(10, 0, 10, 0)));
		//Action listener if user defined zoom is selected/deselected
		userDefaultScale.addItemListener(new ItemListener() {
			public void itemStateChanged(final ItemEvent e) {
				final boolean selected = e.getStateChange() == ItemEvent.SELECTED;
				userZoom.setEditable(selected);
				userZoom.setEnabled(selected);
				if (selected) {
					userZoom.requestFocusInWindow();
				}
			}
		});
		final GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 3;
		c.ipady = 10;
		c.ipadx = 4;
		c.anchor = GridBagConstraints.LINE_START;
		c.insets = new Insets(0, 10, 0, 10);
		//fit to page
		gridbag.setConstraints(fitToPage, c);
		panel.add(fitToPage);
		c.gridy++;
		//fit background image to page
		gridbag.setConstraints(fitBackgroundImageToPage, c);
		panel.add(fitBackgroundImageToPage);
		c.gridy++;
		//fit to width
		gridbag.setConstraints(fitToWidth, c);
		panel.add(fitToWidth);
		c.gridy++;
		//fit to height
		gridbag.setConstraints(fitToHeight, c);
		panel.add(fitToHeight);
		c.gridy++;
		//user defined
		gridbag.setConstraints(userDefaultScale, c);
		panel.add(userDefaultScale);
		c.gridy++;
		c.ipady = 1;
		c.gridx = 0;
		c.gridwidth = 1;
		c.insets = new Insets(0, 0, 0, 0);
		//spacer
		final Component b = Box.createRigidArea(new Dimension(25, 1));
		gridbag.setConstraints(b, c);
		panel.add(b);
		c.ipadx = 4;
		c.gridx = 1;
		c.gridwidth = 1;
		//Label
		gridbag.setConstraints(userZoomL, c);
		panel.add(userZoomL);
		c.gridx = 2;
		c.gridwidth = 1;
		c.ipadx = 10;
		c.insets = new Insets(0, 0, 0, 10);
		//input field
		gridbag.setConstraints(userZoom, c);
		panel.add(userZoom);
		panel.setLayout(gridbag);
		//show dialog
		final int result = UITools.showConfirmDialog(null, panel, TextUtils.getText("printing_settings"),
		    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		//evaluate result
		if (result == JOptionPane.OK_OPTION) {
			ResourceController.getResourceController().setProperty("user_zoom", userZoom.getText());
			final FitMap fitMapDecision;
			if (fitToPage.isSelected()) {
				fitMapDecision = FitMap.PAGE;
			}
			else if (fitBackgroundImageToPage.isSelected()) {
				fitMapDecision = FitMap.BACKGROUND;
			}
			else if (fitToWidth.isSelected()) {
				fitMapDecision = FitMap.WIDTH;
			}
			else if (fitToHeight.isSelected()) {
				fitMapDecision = FitMap.HEIGHT;
			}
			else {
				fitMapDecision = FitMap.USER_DEFINED;
			}
			ResourceController.getResourceController().setProperty("fit_map", fitMapDecision.toString());
		}
		else {
			return;
		}
		final PrintController printController = getPrintController();
		printController.pageDialog();
	}
}
