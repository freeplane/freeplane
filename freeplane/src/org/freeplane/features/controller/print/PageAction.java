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
package org.freeplane.features.controller.print;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.print.PageFormat;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.MenuBuilder;

class PageAction extends AbstractPrintAction {
	private final class ActionListenerImplementation implements ActionListener {
		private final JDialog dialog;
		private int eventSource;

		private ActionListenerImplementation(final JDialog dialog) {
			this.dialog = dialog;
		}

		public void actionPerformed(final ActionEvent e) {
			eventSource = 1;
			dialog.dispose();
		}

		public int getEventSource() {
			return eventSource;
		}
	}

	static final String NAME = "page";
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	PageAction(final PrintController controller) {
		super("PageAction", controller);
	}

	public void actionPerformed(final ActionEvent e) {
		if (!getPrintController().acquirePrinterJobAndPageFormat()) {
			return;
		}
		final Frame frame = getPrintController().getController().getViewController().getFrame();
		final JDialog dialog = new JDialog(frame, ResourceBundles.getText("printing_settings"), /* modal=*/
		true);
		final ButtonGroup fitButtons = new ButtonGroup();
		final FitMap fitMap = FitMap.valueOf();
		final JRadioButton fitToPage = new JRadioButton(ResourceBundles.getText("fit_map_to_page"),
		    fitMap == FitMap.PAGE);
		fitButtons.add(fitToPage);
		final JRadioButton fitToWidth = new JRadioButton(ResourceBundles.getText("fit_map_to_page_width"),
		    fitMap == FitMap.WIDTH);
		fitButtons.add(fitToWidth);
		final JRadioButton fitToHeighth = new JRadioButton(ResourceBundles.getText("fit_map_to_page_height"),
		    fitMap == FitMap.HEIGHT);
		fitButtons.add(fitToHeighth);
		final JRadioButton userDefaultScale = new JRadioButton(ResourceBundles.getText("user_defined_scale"),
		    fitMap == FitMap.USER_DEFINED);
		fitButtons.add(userDefaultScale);
		final JLabel userZoomL = new JLabel(ResourceBundles.getText("user_zoom"));
		final JTextField userZoom = new JTextField(ResourceController.getResourceController().getProperty("user_zoom"),
		    3);
		userZoom.setEditable(userDefaultScale.isSelected());
		final JButton okButton = new JButton();
		MenuBuilder.setLabelAndMnemonic(okButton, ResourceBundles.getText("ok"));
		final JPanel panel = new JPanel();
		final GridBagLayout gridbag = new GridBagLayout();
		final ActionListenerImplementation aListener = new ActionListenerImplementation(dialog);
		okButton.addActionListener(aListener);
		userDefaultScale.addItemListener(new ItemListener() {
			public void itemStateChanged(final ItemEvent e) {
				userZoom.setEditable(e.getStateChange() == ItemEvent.SELECTED);
			}
		});
		final GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 2;
		c.anchor = GridBagConstraints.LINE_START;
		gridbag.setConstraints(fitToPage, c);
		panel.add(fitToPage);
		c.gridy++;
		gridbag.setConstraints(fitToWidth, c);
		panel.add(fitToWidth);
		c.gridy++;
		gridbag.setConstraints(fitToHeighth, c);
		panel.add(fitToHeighth);
		c.gridy++;
		gridbag.setConstraints(userDefaultScale, c);
		panel.add(userDefaultScale);
		c.gridy++;
		c.gridwidth = 1;
		gridbag.setConstraints(userZoomL, c);
		panel.add(userZoomL);
		c.gridx = 1;
		c.gridwidth = 1;
		gridbag.setConstraints(userZoom, c);
		panel.add(userZoom);
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 3;
		c.insets = new Insets(10, 0, 0, 0);
		c.anchor = GridBagConstraints.CENTER;
		gridbag.setConstraints(okButton, c);
		panel.add(okButton);
		panel.setLayout(gridbag);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setContentPane(panel);
		dialog.setLocationRelativeTo(frame);
		dialog.getRootPane().setDefaultButton(okButton);
		dialog.pack();
		dialog.setVisible(true);
		if (aListener.getEventSource() == 1) {
			ResourceController.getResourceController().setProperty("user_zoom", userZoom.getText());
			final FitMap fitMapDecision;
			if (fitToPage.isSelected()) {
				fitMapDecision = FitMap.PAGE;
			}
			else if (fitToWidth.isSelected()) {
				fitMapDecision = FitMap.WIDTH;
			}
			else if (fitToHeighth.isSelected()) {
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
		printController.setPageFormat(printController.getPrinterJob().pageDialog(printController.getPageFormat()));
		if (printController.getPageFormat().getOrientation() == PageFormat.LANDSCAPE) {
			ResourceController.getResourceController().setProperty("page_orientation", "landscape");
		}
		else if (printController.getPageFormat().getOrientation() == PageFormat.PORTRAIT) {
			ResourceController.getResourceController().setProperty("page_orientation", "portrait");
		}
		else if (printController.getPageFormat().getOrientation() == PageFormat.REVERSE_LANDSCAPE) {
			ResourceController.getResourceController().setProperty("page_orientation", "reverse_landscape");
		}
	}
}
