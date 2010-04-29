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
package org.freeplane.features.mindmapmode.addins.export;

import java.awt.Component;
import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.freeplane.core.resources.FpStringUtils;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.url.UrlManager;
import org.freeplane.core.util.LogTool;
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
	final private static String EXPORT_FILTER_PATTERN = "^.*MINDMAPEXPORTFILTER\\s+(\\S+)\\s+(.*)(?:\\s+-->)?$";
	final private static String FILE_NAME_PATTERN = "mm2([\\w]+)\\.xsl";
	static final Pattern COMPILED_EXPORT_FILTER_PATTERN = Pattern.compile(EXPORT_FILTER_PATTERN);
	static final Pattern COMPILED_FILE_NAME_PATTERN = Pattern.compile(FILE_NAME_PATTERN);
	/** A pattern which is "MINDMAPEXPORTFILTER ext1;ext2;... File format description" */
	/** the JFileChooser dialog used to choose filter and the file to export to. */
	private JFileChooser filechooser = null;
	/** a hash where the key is the filter description and the value the filename of
	 * the corresponding XSLT sheet. */
	private final HashMap<String, File> filtermap = new HashMap<String, File>();
	/** Maximum number of lines we read in each XSLT files for performance reasons */
	final private int MAX_READ_LINES = 5;

	/**
	 * Constructor that accepts one parameter. This constructor does <i>not</i> the
	 * export per itself.
	 */
	public ExportDialog() {
		super();
		filechooser = new JFileChooser();
		filechooser.setAcceptAllFileFilterUsed(false); // the user can't select an "All Files filter"
		filechooser.setDialogTitle(ResourceBundles.getText("export_using_xslt"));
		filechooser.setToolTipText(ResourceBundles.getText("select_file_export_to")); // "Select the file to export to"
	}

	private void addXsltFile(final String[] filters, final String description, final File somefile) {
		final ExampleFileFilter eff = new ExampleFileFilter(filters, description);
		// we don't want to overwrite an already existing filter
		if (!filtermap.containsKey(eff.getDescription())) {
			// we add it as filter and in the map.
			filechooser.addChoosableFileFilter(eff);
			filtermap.put(eff.getDescription(), somefile);
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
		gatherXsltScripts();
		if (filtermap.isEmpty()) {
			JOptionPane.showMessageDialog(parentframe, FpStringUtils.formatText("xslt_export_file_not_found_in_dirs",
			    getXsltUserDirectory().getAbsolutePath(), getXsltSysDirectory().getAbsolutePath()), ResourceBundles
			    .getText("xslt_export_not_possible"), JOptionPane.WARNING_MESSAGE);
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
			absolutePathWithoutExtension = UrlManager.removeExtension(xmlSourceFile.getAbsolutePath());
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
							filechooser.setSelectedFile(acceptableFile);
						}
					});
					return;
				}
				if (evt.getPropertyName().equals(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY)) {
					if (evt.getNewValue() == null) {
						final ExampleFileFilter filter = (ExampleFileFilter) filechooser.getFileFilter();
						final File acceptableFile = getAcceptableFile(selectedFile, filter);
						EventQueue.invokeLater(new Runnable() {
							public void run() {
								filechooser.setSelectedFile(acceptableFile);
							}
						});
					}
					return;
				}
			}
		};
		filterChangeListener.propertyChange(new PropertyChangeEvent(filechooser,
		    JFileChooser.FILE_FILTER_CHANGED_PROPERTY, null, filechooser.getFileFilter()));
		try {
			filechooser.addPropertyChangeListener(filterChangeListener);
			final int returnVal = filechooser.showSaveDialog(parentframe);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				final XmlExporter xe = new XmlExporter();
				// we check which filter has been selected by the user and use its
				// description as key for the map to get the corresponding XSLT file
				final ExampleFileFilter fileFilter = (ExampleFileFilter) filechooser.getFileFilter();
				final File xsltFile = filtermap.get(fileFilter.getDescription());
				final File selectedFile = getAcceptableFile(filechooser.getSelectedFile(), fileFilter);
				if (selectedFile == null) {
					return;
				}
				if (selectedFile.isDirectory()) {
					return;
				}
				if (selectedFile.exists()) {
					final String overwriteText = MessageFormat.format(ResourceBundles.getText("file_already_exists"),
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
			filechooser.removePropertyChangeListener(filterChangeListener);
		}
	}

	/**
	 * The function checks if the pattern matches with one of the lines in the file.
	 * @param somefile the file to open and search a line matching the pattern.
	 */
	private void extractFilterFromFile(final File somefile) {
		BufferedReader xsl = null;
		try {
			xsl = new BufferedReader(new FileReader(somefile));
			String line;
			int l = 0;
			boolean keyFound = false;
			// ...we open it and check if it contains the right marker
			while ((line = xsl.readLine()) != null && l < MAX_READ_LINES) {
				final Matcher m = COMPILED_EXPORT_FILTER_PATTERN.matcher(line);
				if (m.matches()) { // if it does
					keyFound = true;
					final String[] filters = m.group(1).split(";");
					final String description = m.group(2);
					addXsltFile(filters, description, somefile);
					// we want to allow for more than one filter line per XSLT file
					// so we don't exit once we've found one and even account for
					// the fact that we might trespass the MAX_READ_LINES limit
					l--;
				}
				l++;
			}
			if (keyFound) {
				return;
			}
			final Matcher m = COMPILED_FILE_NAME_PATTERN.matcher(somefile.getName());
			if (m.matches()) { // if it does
				final String extension = m.group(1);
				final String[] filters = new String[] { extension };
				addXsltFile(filters, FpStringUtils.formatText("exported_file", extension), somefile);
			}
		}
		catch (final IOException e) {
			LogTool.warn(e);
			UITools.errorMessage(ResourceBundles.getText("export_failed"));
		}
		finally {
			if (xsl != null) {
				try {
					xsl.close();
				}
				catch (final IOException e) {
					LogTool.severe(e);
				}
			}
		}
	}

	/**
	 * This method populates the {@link #filechooser filechooser} field
	 * (especially the {@link JFileChooser#getChoosableFileFilters() choosable
	 * file filters}) and the {@link #filtermap filtermap} field.
	 */
	private void gatherXsltScripts() {
		gatherXsltScripts(getXsltUserDirectory());
		gatherXsltScripts(getXsltSysDirectory());
	}

	/**
		 * This methods checks all readable files ending in '.xsl' from a given directory,
		 * and passes them to the method {@link #extractFilterFromFile}.
		 * @param xsltdir the directory where XSLT files are to be searched for
		 */
	private void gatherXsltScripts(final File xsltdir) {
		if (!(xsltdir.isDirectory() && xsltdir.canRead())) {
			return;
		}
		// we list the files using an anonymous filter class that accepts only files
		// readable by the user and with name ending in .xsl
		final File xslfiles[] = xsltdir.listFiles(new FileFilter() {
			public boolean accept(final File pathname) {
				return (pathname.isFile() && pathname.canRead() && pathname.getPath().toLowerCase().endsWith(".xsl"));
			}
		});
		// we compile the pattern for performance reasons.
		// then for each found file, we check and extract a potentially present filter
		for (int i = 0; i < xslfiles.length; i++) {
			extractFilterFromFile(xslfiles[i]);
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

	/**
	 * A simple help function to get the directory where to search for XSLT 
	 * export files distributed with Freeplane.
	 * @return The system directory where XSLT export files are supposed to be.
	 */
	private File getXsltSysDirectory() {
		return new File(ResourceController.getResourceController().getResourceBaseDir(), "xslt");
	}

	/**
	 * A simple help function to get the directory where to search for XSLT 
	 * export files written by the user.
	 * @return The user directory where XSLT export files are supposed to be.
	 */
	private File getXsltUserDirectory() {
		return new File(ResourceController.getResourceController().getFreeplaneUserDirectory(), "xslt");
	}
}
