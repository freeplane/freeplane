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
package org.freeplane.addins.export.mindmapmode;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import org.freeplane.controller.Controller;
import org.freeplane.main.ExampleFileFilter;


public class ExportDialog extends JFrame {
	class ExportListener implements ActionListener {
		private boolean cancel = false;
		boolean exitSystem = true;
		private ExportDialog parent = null;
		XmlExporter xe = null;

		public ExportListener(final ExportDialog m) {
			parent = m;
		}

		public ExportListener(final ExportDialog m, final boolean pCancel) {
			parent = m;
			cancel = pCancel;
		}

		public void actionPerformed(final ActionEvent e) {
			if (!cancel) {
				xe = new XmlExporter();
				xe
				    .transForm(parent.xmlFile, new File(
				        parent.fieldXsltFileName.getText()), new File(
				        parent.fieldTargetFileName.getText()));
			}
			Controller
			    .getResourceController()
			    .setProperty(
			        ExportDialog.ACCESSORIES_PLUGINS_UTIL_XSLT_EXPORT_DIALOG_STORE_XSLT,
			        fieldXsltFileName.getText());
			Controller
			    .getResourceController()
			    .setProperty(
			        ExportDialog.ACCESSORIES_PLUGINS_UTIL_XSLT_EXPORT_DIALOG_STORE_TARGET,
			        fieldTargetFileName.getText());
			parent.setVisible(false);
			parent.dispose();
			/*
			 * if (exitSystem) { System.exit(0); }
			 */
		}
	}

	private static final String ACCESSORIES_PLUGINS_UTIL_XSLT_EXPORT_DIALOG_STORE_TARGET = "accessories.plugins.util.xslt.ExportDialog.store.target";
	private static final String ACCESSORIES_PLUGINS_UTIL_XSLT_EXPORT_DIALOG_STORE_XSLT = "accessories.plugins.util.xslt.ExportDialog.store.xslt";
	protected JTextField fieldTargetFileName = null;
	protected JTextField fieldXsltFileName = null;
	protected File xmlFile = null;

	public ExportDialog(final File nxmlFile) {
		super("ExportDialog");
		xmlFile = nxmlFile;
		setBackground(Color.lightGray);
		this.addWindowListener(new WindowClosingAdapter(false));
		final GridBagLayout gbl = new GridBagLayout();
		GridBagConstraints gbc;
		getContentPane().setLayout(gbl);
		/*
		 * List list = new List(); for (int i = 0; i < 20; ++i) {
		 * list.add("This is item " + i); } gbc = makegbc(0, 0, 1, 3);
		 * gbc.weightx = 100; gbc.weighty = 100; gbc.fill =
		 * GridBagConstraints.BOTH; gbl.setConstraints(list, gbc);
		 * getContentPane().add(list);
		 */
		final String lastXsltFileName = Controller
		    .getResourceController()
		    .getProperty(
		        ExportDialog.ACCESSORIES_PLUGINS_UTIL_XSLT_EXPORT_DIALOG_STORE_XSLT);
		final String lastTargetFileName = Controller
		    .getResourceController()
		    .getProperty(
		        ExportDialog.ACCESSORIES_PLUGINS_UTIL_XSLT_EXPORT_DIALOG_STORE_TARGET);
		gbc = makegbc(0, 0, 1, 1);
		gbc.fill = GridBagConstraints.NONE;
		final JLabel label = new JLabel("Choose XSL File ");
		gbl.setConstraints(label, gbc);
		getContentPane().add(label);
		gbc = makegbc(1, 0, 1, 1);
		gbc.weightx = 300;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		fieldXsltFileName = new JTextField(lastXsltFileName);
		fieldXsltFileName.setColumns(20);
		gbl.setConstraints(fieldXsltFileName, gbc);
		getContentPane().add(fieldXsltFileName);
		gbc = makegbc(0, 1, 1, 1);
		gbc.fill = GridBagConstraints.NONE;
		final JLabel labeli = new JLabel("choose ExportFile ");
		gbl.setConstraints(labeli, gbc);
		getContentPane().add(labeli);
		gbc = makegbc(1, 1, 1, 1);
		gbc.weightx = 100;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		fieldTargetFileName = new JTextField(lastTargetFileName);
		fieldTargetFileName.setColumns(20);
		gbl.setConstraints(fieldTargetFileName, gbc);
		getContentPane().add(fieldTargetFileName);
		final JButton xslbutton = new JButton("Browse");
		gbc = makegbc(2, 0, 1, 1);
		gbc.fill = GridBagConstraints.NONE;
		gbl.setConstraints(xslbutton, gbc);
		xslbutton.addActionListener(new FileChooseListener(0,
		    fieldXsltFileName, xslbutton, xmlFile));
		getContentPane().add(xslbutton);
		final JButton exportbutton = new JButton("Browse");
		gbc = makegbc(2, 1, 1, 1);
		gbc.fill = GridBagConstraints.NONE;
		exportbutton.addActionListener(new FileChooseListener(1,
		    fieldTargetFileName, exportbutton, xmlFile));
		gbl.setConstraints(exportbutton, gbc);
		getContentPane().add(exportbutton);
		final JButton button = new JButton("Export");
		gbc = makegbc(2, 2, 1, 1);
		gbc.fill = GridBagConstraints.NONE;
		button.addActionListener(new ExportListener(this));
		gbl.setConstraints(button, gbc);
		getContentPane().add(button);
		final JButton cbutton = new JButton("Cancel");
		gbc = makegbc(1, 2, 1, 1);
		gbc.anchor = GridBagConstraints.EAST;
		gbc.fill = GridBagConstraints.NONE;
		gbl.setConstraints(cbutton, gbc);
		cbutton.addActionListener(new ExportListener(this, true));
		getContentPane().add(cbutton);
		pack();
	}

	private GridBagConstraints makegbc(final int x, final int y,
	                                   final int width, final int height) {
		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = x;
		gbc.gridy = y;
		gbc.gridwidth = width;
		gbc.gridheight = height;
		gbc.insets = new Insets(1, 1, 1, 1);
		gbc.anchor = GridBagConstraints.WEST;
		return gbc;
	}
}

class FileChooseListener implements ActionListener {
	final private String expch = "Define a File to Export";
	private JTextField jtf = null;
	private int kind = 0;
	private Component parent = null;
	private String WindowTitle = null;
	private File xf = null;
	final private String xslch = "Choose XSL Template";

	public FileChooseListener(final int wit, final JTextField jt,
	                          final Component c, final File mmFile) {
		parent = c;
		jtf = jt;
		kind = wit;
		xf = mmFile;
		if (kind == 0) {
			WindowTitle = xslch;
		}
		else {
			WindowTitle = expch;
		}
	}

	public void actionPerformed(final ActionEvent e) {
		JFileChooser chooser;
		if (kind == 0) {
			new StringBuffer("");
			chooser = new JFileChooser();
		}
		else {
			chooser = new JFileChooser(xf.getParentFile());
		}
		ExampleFileFilter filter = null;
		if (kind == 0) {
			filter = new ExampleFileFilter(new String("xsl"),
			    "XSLT Templatefile");
			chooser.setFileFilter(filter);
		};
		final int returnVal = chooser.showDialog(parent, WindowTitle);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			try {
				if (kind == 0) {
					if (!new File(chooser.getSelectedFile().getAbsolutePath())
					    .exists()) {
						final Object Message = "The XSL Template chosen doesn't seem to exist. \nPlease Choose another.";
						JOptionPane.showMessageDialog(null, Message,
						    "Warning File does not exist",
						    JOptionPane.WARNING_MESSAGE);
					}
					else {
						jtf
						    .setText(chooser.getSelectedFile()
						        .getAbsolutePath());
					};
				}
				if (kind == 1) {
					if (!new File(chooser.getSelectedFile().getAbsolutePath())
					    .exists()) {
						jtf
						    .setText(chooser.getSelectedFile()
						        .getAbsolutePath());
					}
					else {
						final int i = JOptionPane.showConfirmDialog(null,
						    "File exists. Do You want to overwrite?",
						    "Warning, File exists", 2);
						if (i == JOptionPane.YES_OPTION) {
							jtf.setText(chooser.getSelectedFile()
							    .getAbsolutePath());
						}
					};
				};
			}
			catch (final Exception ex) {
				System.out.println("exeption:" + ex);
			}
			{
			}
		}
	}
}
