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

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.freeplane.core.resources.FreeplaneResourceBundle;
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
	private static final long serialVersionUID = 2736613545540923942L;

	PageAction(final PrintController controller) {
		super(controller);
		MenuBuilder.setLabelAndMnemonic(this, FreeplaneResourceBundle.getText("page"));
	}

	public void actionPerformed(final ActionEvent e) {
		if (!getPrintController().acquirePrinterJobAndPageFormat()) {
			return;
		}
		final Frame frame = getPrintController().getController().getViewController().getFrame();
		final JDialog dialog = new JDialog(frame, FreeplaneResourceBundle.getText("printing_settings"), /* modal=*/
		true);
		final JCheckBox fitToPage = new JCheckBox(FreeplaneResourceBundle.getText("fit_to_page"), ResourceController
		    .getResourceController().getBooleanProperty("fit_to_page"));
		final JLabel userZoomL = new JLabel(FreeplaneResourceBundle.getText("user_zoom"));
		final JTextField userZoom = new JTextField(ResourceController.getResourceController().getProperty("user_zoom"),
		    3);
		userZoom.setEditable(!fitToPage.isSelected());
		final JButton okButton = new JButton();
		MenuBuilder.setLabelAndMnemonic(okButton, FreeplaneResourceBundle.getText("ok"));
		final JPanel panel = new JPanel();
		final GridBagLayout gridbag = new GridBagLayout();
		final GridBagConstraints c = new GridBagConstraints();
		final ActionListenerImplementation aListener = new ActionListenerImplementation(dialog);
		okButton.addActionListener(aListener);
		fitToPage.addItemListener(new ItemListener() {
			public void itemStateChanged(final ItemEvent e) {
				userZoom.setEditable(e.getStateChange() == ItemEvent.DESELECTED);
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
		dialog.setLocationRelativeTo(frame);
		dialog.getRootPane().setDefaultButton(okButton);
		dialog.pack();
		dialog.setVisible(true);
		if (aListener.getEventSource() == 1) {
			ResourceController.getResourceController().setProperty("user_zoom", userZoom.getText());
			ResourceController.getResourceController().setProperty("fit_to_page",
			    (fitToPage.isSelected() ? "true" : "false"));
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

	@Override
	public String getName() {
		return NAME;
	}
}
