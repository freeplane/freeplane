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
package org.freeplane.controller.print;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.print.PageFormat;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.freeplane.controller.Controller;
import org.freeplane.controller.Freeplane;
import org.freeplane.main.Tools;
import org.freeplane.ui.FreemindMenuBar;

class PageAction extends AbstractPrintAction {
	PageAction(final PrintController controller) {
		super(controller);
		FreemindMenuBar.setLabelAndMnemonic(this, Freeplane.getText("page"));
	}

	public void actionPerformed(final ActionEvent e) {
		if (!getPrintController().acquirePrinterJobAndPageFormat()) {
			return;
		}
		final Controller controller = Freeplane.getController();
		final JDialog dialog = new JDialog(Freeplane.getController()
		    .getViewController().getJFrame(), Freeplane
		    .getText("printing_settings"), /*
																						 * modal=
																						 */
		true);
		final JCheckBox fitToPage = new JCheckBox(Freeplane
		    .getText("fit_to_page"), Freeplane.getController()
		    .getResourceController().getBoolProperty("fit_to_page"));
		final JLabel userZoomL = new JLabel(Freeplane.getText("user_zoom"));
		final JTextField userZoom = new JTextField(controller
		    .getResourceController().getProperty("user_zoom"), 3);
		userZoom.setEditable(!fitToPage.isSelected());
		final JButton okButton = new JButton();
		FreemindMenuBar.setLabelAndMnemonic(okButton, Freeplane.getText("ok"));
		final Tools.IntHolder eventSource = new Tools.IntHolder();
		final JPanel panel = new JPanel();
		final GridBagLayout gridbag = new GridBagLayout();
		final GridBagConstraints c = new GridBagConstraints();
		eventSource.setValue(0);
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				eventSource.setValue(1);
				dialog.dispose();
			}
		});
		fitToPage.addItemListener(new ItemListener() {
			public void itemStateChanged(final ItemEvent e) {
				userZoom
				    .setEditable(e.getStateChange() == ItemEvent.DESELECTED);
			}
		});
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 2;
		gridbag.setConstraints(fitToPage, c);
		panel.add(fitToPage);
		c.gridy = 1;
		c.gridwidth = 1;
		gridbag.setConstraints(userZoomL, c);
		panel.add(userZoomL);
		c.gridx = 1;
		c.gridwidth = 1;
		gridbag.setConstraints(userZoom, c);
		panel.add(userZoom);
		c.gridy = 2;
		c.gridx = 0;
		c.gridwidth = 3;
		c.insets = new Insets(10, 0, 0, 0);
		gridbag.setConstraints(okButton, c);
		panel.add(okButton);
		panel.setLayout(gridbag);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setContentPane(panel);
		dialog.setLocationRelativeTo(Freeplane.getController()
		    .getViewController().getJFrame());
		dialog.getRootPane().setDefaultButton(okButton);
		dialog.pack();
		dialog.setVisible(true);
		if (eventSource.getValue() == 1) {
			controller.getResourceController().setProperty("user_zoom",
			    userZoom.getText());
			controller.getResourceController().setProperty("fit_to_page",
			    (fitToPage.isSelected() ? "true" : "false"));
		}
		else {
			return;
		}
		final PrintController printController = getPrintController();
		printController.setPageFormat(printController.getPrinterJob()
		    .pageDialog(printController.getPageFormat()));
		if (printController.getPageFormat().getOrientation() == PageFormat.LANDSCAPE) {
			controller.getResourceController().setProperty("page_orientation",
			    "landscape");
		}
		else if (printController.getPageFormat().getOrientation() == PageFormat.PORTRAIT) {
			controller.getResourceController().setProperty("page_orientation",
			    "portrait");
		}
		else if (printController.getPageFormat().getOrientation() == PageFormat.REVERSE_LANDSCAPE) {
			controller.getResourceController().setProperty("page_orientation",
			    "reverse_landscape");
		}
	}
}
