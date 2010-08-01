/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2009 Eric Lavarde, Freeplane admins
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
package org.freeplane.features.mindmapmode.export;

import java.awt.Component;
import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.text.MessageFormat;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.FileUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mindmapmode.text.ExampleFileFilter;

/**
 * This class uses the JFileChooser dialog to allow users to choose a file name to
 * export to. The filter selection is created by gathering all the *.xsl files
 * present in the user-specific and system-specific Export-directories of Freeplane.
 * Those files are recognised by their extension (.xsl) but also by the fact that they
 * contain within the 5 first lines a string of the form:
 * <pre>MINDMAPEXPORT <i>extensions</i> <i>description</i></pre>
 * where the fields in italic are relative to the file format to which the mindmap will
 * be exported to using this specific XSLT sheet:
 * <ul>
 * <li><i>extensions</i> is a semi-column separated list of acceptable file extensions
 * without asterisk or dot, e.g. "jpg;jpeg".</li>
 * <li><i>description</i> is a description of the file format, e.g. "JPEG image".</li>
 * </ul>
 * Only the first unique combination of extensions and description will be kept, in such
 * a way that users can "overwrite" an already existing XSLT sheet with their own
 * version.
 * @author Eric Lavarde
 * @see javax.swing.JFileChooser
 *
 */
public class ExportDialog {
	/** the JFileChooser dialog used to choose filter and the file to export to. */
	final private JFileChooser fileChooser = new JFileChooser();
	final private XsltFileRegistry xsltFileRegistry;

	/**
	 * This constructor does <i>not</i> the export per itself.
	 * It populates the {@link #fileChooser} field
	 * (especially the {@link JFileChooser#getChoosableFileFilters() choosable
	 * file filters}).
	 */
	public ExportDialog() {
		super();
		xsltFileRegistry = XsltFileRegistry.newInstance();
		fileChooser.setAcceptAllFileFilterUsed(false); // the user can't select an "All Files filter"
		fileChooser.setDialogTitle(TextUtils.getText("export_using_xslt"));
		fileChooser.setToolTipText(TextUtils.getText("select_file_export_to"));
		for (FileFilter filter : xsltFileRegistry.getFileFilters()) {
	        fileChooser.addChoosableFileFilter(filter);
        }
	}

	/**
	 * A function to call again and again in order to export the same XML source file.
	 * @see #export(Component)
	 */
	/**
	 * A function to call again and again in order to export the same XML source file.
	 * @param parentframe a parent component for the dialogs to appear (can be null).
	 * @param streamSource 
	 */
	void export(final Component parentframe, final StreamSource xmlSource, final File xmlSourceFile) {
		if (xsltFileRegistry.getFilterMap().isEmpty()) {
			JOptionPane.showMessageDialog(parentframe, TextUtils.formatText("xslt_export_file_not_found_in_dirs",
			    xsltFileRegistry.getXsltUserDirectory().getAbsolutePath(), xsltFileRegistry.getXsltSysDirectory()
			        .getAbsolutePath()), TextUtils.getText("xslt_export_not_possible"), JOptionPane.WARNING_MESSAGE);
			/* "No XSLT export file could be found,\n neither in '"
			+ getXsltUserDirectory() + "'\n nor in '"
			+ getXsltSysDirectory() + "'.",
			"Freeplane XSLT export not possible", */
			return;
		}
		// Finish to setup the File Chooser...
		// And then use it
		final String absolutePathWithoutExtension;
		if (xmlSourceFile != null) {
			absolutePathWithoutExtension = FileUtils.removeExtension(xmlSourceFile.getAbsolutePath());
		}
		else {
			absolutePathWithoutExtension = null;
		}
		final PropertyChangeListener filterChangeListener = new PropertyChangeListener() {
			final private File selectedFile = absolutePathWithoutExtension == null ? null : new File(
			    absolutePathWithoutExtension);

			public void propertyChange(final PropertyChangeEvent evt) {
				if (evt.getPropertyName().equals(JFileChooser.FILE_FILTER_CHANGED_PROPERTY)) {
					final ExampleFileFilter filter = (ExampleFileFilter) evt.getNewValue();
					if (filter == null) {
						return;
					}
					final File acceptableFile = getAcceptableFile(selectedFile, filter);
					EventQueue.invokeLater(new Runnable() {
						public void run() {
							fileChooser.setSelectedFile(acceptableFile);
						}
					});
					return;
				}
				if (evt.getPropertyName().equals(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY)) {
					if (evt.getNewValue() == null) {
						final ExampleFileFilter filter = (ExampleFileFilter) fileChooser.getFileFilter();
						final File acceptableFile = getAcceptableFile(selectedFile, filter);
						EventQueue.invokeLater(new Runnable() {
							public void run() {
								fileChooser.setSelectedFile(acceptableFile);
							}
						});
					}
					return;
				}
			}
		};
		filterChangeListener.propertyChange(new PropertyChangeEvent(fileChooser,
		    JFileChooser.FILE_FILTER_CHANGED_PROPERTY, null, fileChooser.getFileFilter()));
		try {
			fileChooser.addPropertyChangeListener(filterChangeListener);
			final int returnVal = fileChooser.showSaveDialog(parentframe);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				final XmlExporter xe = new XmlExporter();
				// we check which filter has been selected by the user and use its
				// description as key for the map to get the corresponding XSLT file
				final ExampleFileFilter fileFilter = (ExampleFileFilter) fileChooser.getFileFilter();
				final File xsltFile = xsltFileRegistry.getFilterMap().get(fileFilter.getDescription());
				final File selectedFile = getAcceptableFile(fileChooser.getSelectedFile(), fileFilter);
				if (selectedFile == null) {
					return;
				}
				if (selectedFile.isDirectory()) {
					return;
				}
				if (selectedFile.exists()) {
					final String overwriteText = MessageFormat.format(TextUtils.getText("file_already_exists"),
					    new Object[] { selectedFile.toString() });
					final int overwriteMap = JOptionPane.showConfirmDialog(UITools.getFrame(), overwriteText,
					    overwriteText, JOptionPane.YES_NO_OPTION);
					if (overwriteMap != JOptionPane.YES_OPTION) {
						return;
					}
				}
				final Source xsltSource = new StreamSource(xsltFile);
				xe.transform(xmlSource, xsltSource, selectedFile);
			}
		}
		finally {
			fileChooser.removePropertyChangeListener(filterChangeListener);
		}
	}

	private File getAcceptableFile(File selectedFile, final ExampleFileFilter fileFilter) {
		if (selectedFile == null) {
			return null;
		}
		if (!fileFilter.accept(selectedFile)) {
			selectedFile = new File(selectedFile.getAbsolutePath() + '.' + fileFilter.getExtensionProposal());
		}
		return selectedFile;
	}
}
