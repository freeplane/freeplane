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
package org.freeplane.core.resources.components;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.util.Arrays;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.LabelAndMnemonicSetter;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.FileUtils;
import org.freeplane.core.util.TextUtils;

import com.jgoodies.forms.builder.DefaultFormBuilder;

/**
 * A PathProperty has an absolute path as a model. It's visual representation
 * is a text field containing the path and a 'browse' button.
 * It's configuration has (non-mandatory) options:
 *  - dir [true|false]: if the configured path is a directory
 *  - suffixes: a comma-separated string containing allowed suffixes (without the dot)
 */
public class PathProperty extends PropertyBean implements IPropertyControl {
	private class SelectFileAction implements ActionListener {
		public void actionPerformed(final ActionEvent e) {
			final Object source = e.getSource();
			if (source == selectButton) {
				final JFileChooser chooser = createFileChooser();
				int result = chooser.showOpenDialog(chooser);
				if (result == JFileChooser.APPROVE_OPTION)
					setValue(chooser.getSelectedFile().getAbsolutePath());
			}
		}
	}

	final private boolean isDir;
	final private String[] suffixes;
	private String value;
	private JTextField filenameField;
	private JButton selectButton;

	public PathProperty(final String name, final boolean isDir, final String[] suffixes) {
		super(name);
		this.isDir = isDir;
		this.suffixes = suffixes;
	}

	private JFileChooser createFileChooser() {
		final JFileChooser fileChooser = UITools.newFileChooser();
		if (value != null) {
			fileChooser.setSelectedFile(new File(path()));
		}
		FileFilter filter = null;
		if (isDir) {
		    fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		}
		else if (suffixes != null) {
			filter = new FileFilter() {
				@Override
				public String getDescription() {
					return Arrays.asList(suffixes).toString();
				}

				@Override
				public boolean accept(File f) {
					String extension = FileUtils.getExtension(f);
					for (String suffix : suffixes) {
						if (suffix.equalsIgnoreCase(extension))
							return true;
					}
					return false;
				}
			};
		}
		fileChooser.setFileFilter(filter);
		fileChooser.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent pE) {
				firePropertyChangeEvent();
			}
		});
		return fileChooser;
	}

	private String path() {
        if (value == null) {
            return null;
        }
        String freeplaneUserDirectory = ResourceController.getResourceController().getFreeplaneUserDirectory();
        String path = TextUtils.replaceAtBegin(value, "{freeplaneuserdir}", freeplaneUserDirectory);
        path = TextUtils.replaceAtBegin(path, "{user.home}", System.getProperty("user.home"));
        return path;
    }

    @Override
	public String getValue() {
		return value;
	}

	public void appendToForm(final DefaultFormBuilder builder) {
		final Box box = Box.createHorizontalBox();
		box.setBorder(new EmptyBorder(5, 0, 5, 0));
		filenameField = new JTextField();
		filenameField.setText(value);
		filenameField.addFocusListener(new FocusListener() {
			public void focusLost(FocusEvent e) {
				final String text = filenameField.getText();
				if (text == null || text.length() == 0) {
					filenameField.setText(value);
					JOptionPane.showConfirmDialog(e.getComponent(), TextUtils.getText("OptionPanel.path_property_may_not_be_empty"), "", JOptionPane.WARNING_MESSAGE);
				}
				else {
					value = text;
				}
			}
			public void focusGained(FocusEvent e) {
			}
		});
		box.add(filenameField);
		box.add(Box.createHorizontalStrut(3));
		selectButton = new JButton();
		LabelAndMnemonicSetter.setLabelAndMnemonic(selectButton, TextUtils.getText("browse"));
		selectButton.addActionListener(new SelectFileAction());
		selectButton.setMaximumSize(new Dimension(1000, 1000));
		box.add(selectButton);
		appendToForm(builder, box);
	}

	public void setEnabled(final boolean enabled) {
		if (selectButton != null)
			selectButton.setEnabled(enabled);
		if (filenameField != null)
			filenameField.setEnabled(enabled);
	}

	@Override
	public void setValue(String value) {
	    this.value = value;
		if (filenameField != null)
			filenameField.setText(value);
	}
}
