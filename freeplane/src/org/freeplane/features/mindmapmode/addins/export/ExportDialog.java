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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.freeplane.core.resources.FpStringUtils;
import org.freeplane.core.resources.FreeplaneResourceBundle;
import org.freeplane.core.resources.ResourceController;
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
	/** the (MindMap) file to export from. */
	private File xmlSourceFile = null;
	/** the JFileChooser dialog used to choose filter and the file to export to. */
	private JFileChooser filechooser = null;
	/** a hash where the key is the filter description and the value the filename of
	 * the corresponding XSLT sheet. */
	private HashMap<String,File> filtermap = new HashMap<String,File>();
	/** Maximum number of lines we read in each XSLT files for performance reasons */
	final private int MAX_READ_LINES = 5;
	/** A pattern which is "MINDMAPEXPORTFILTER ext1;ext2;... File format description" */
	final private String EXPORT_FILTER_PATTERN = "^.*MINDMAPEXPORTFILTER\\s+(\\S+)\\s+(.*)(?:\\s+-->)?$";

	/**
	 * Constructor that accepts one parameter. This constructor does <i>not</i> the
	 * export per itself.
	 * @param xmlSFile the XML source file (most probably a mindmap) to export later from.
	 */
	public ExportDialog(final File xmlSFile) {
		super();
		xmlSourceFile = xmlSFile;
	}

	/**
	 * A function to call again and again in order to export the same XML source file.
	 * @see #export(Component)
	 */
	public void export() {
		export(null);
	}

	/**
	 * A function to call again and again in order to export the same XML source file.
	 * @param parentframe a parent component for the dialogs to appear (can be null).
	 */
	public void export(Component parentframe) {
		gatherXsltScripts();

		if (filtermap.isEmpty()) {
			JOptionPane.showMessageDialog(parentframe,
					FpStringUtils.formatText("xslt_export_file_not_found_in_dirs",
							getXsltUserDirectory().getAbsolutePath(),
							getXsltSysDirectory().getAbsolutePath()
							), 
					FreeplaneResourceBundle.getText("xslt_export_not_possible"), 
					JOptionPane.WARNING_MESSAGE);
					/* "No XSLT export file could be found,\n neither in '"
					+ getXsltUserDirectory() + "'\n nor in '"
					+ getXsltSysDirectory() + "'.",
					"Freeplane XSLT export not possible", */
			return;
		}
	
		
		// Finish to setup the File Chooser...
		filechooser.setAcceptAllFileFilterUsed(false); // the user can't select an "All Files filter"
		filechooser.setDialogTitle(FreeplaneResourceBundle.getText("export_using_xslt"));
		filechooser.setToolTipText(FreeplaneResourceBundle.getText("select_file_export_to")); // "Select the file to export to"

		// And then use it
		int returnVal = filechooser.showSaveDialog(parentframe);
	    if(returnVal == JFileChooser.APPROVE_OPTION) {
			XmlExporter xe = new XmlExporter();
			// we check which filter has been selected by the user and use its
			// description as key for the map to get the corresponding XSLT file
			File xsltFile = filtermap.get(
					((ExampleFileFilter)filechooser.getFileFilter()).getDescription());
			xe.transForm(xmlSourceFile, xsltFile, filechooser.getSelectedFile());
	    }
	}

	/**
	 * This method populates the {@link #filechooser filechooser} field
	 * (especially the {@link JFileChooser#getChoosableFileFilters() choosable
	 * file filters}) and the {@link #filtermap filtermap} field.
	 */
	private void gatherXsltScripts() {
		if (filechooser == null) { filechooser = new JFileChooser(); }
		
		gatherXsltScripts(getXsltUserDirectory());
		gatherXsltScripts(getXsltSysDirectory());
	}
	
	/**
	 * This methods checks all readable files ending in '.xsl' from a given directory,
	 * and passes them to the method {@link #extractFilterFromFile}.
	 * @param xsltdir the directory where XSLT files are to be searched for
	 */
	private void gatherXsltScripts(File xsltdir) {
		if ( ! (xsltdir.isDirectory() && xsltdir.canRead()) ) { return; }
		if (filechooser == null) { filechooser = new JFileChooser(); }
		
		// we list the files using an anonymous filter class that accepts only files
		// readable by the user and with name ending in .xsl
		File xslfiles[] = xsltdir.listFiles(new FileFilter() {
			public boolean accept(File pathname) {
				return (pathname.isFile() && pathname.canRead() 
						&& pathname.getPath().toLowerCase().endsWith(".xsl"));
			}
		});
		
		// we compile the pattern for performance reasons.
    	Pattern p = Pattern.compile(EXPORT_FILTER_PATTERN);

    	// then for each found file, we check and extract a potentially present filter
		for (int i = 0; i < xslfiles.length; i++) {
			extractFilterFromFile(xslfiles[i], p);
		}
	}

	/**
	 * The function checks if the pattern matches with one of the lines in the file.
	 * @param somefile the file to open and search a line matching the pattern.
	 * @param p the pattern to search for, it must define 2 groups,
	 * 	the first one containing a semi-column separated list of file extensions,
	 * 	the second one a file type description.
	 */
	private void extractFilterFromFile(File somefile, Pattern p) {
		BufferedReader xsl = null;
		try {
		    xsl = new BufferedReader(new FileReader(somefile));
		    String line;
		    int l = 0;
		    // ...we open it and check if it contains the right marker
		    while ((line = xsl.readLine()) != null && l < MAX_READ_LINES) {
		    	 Matcher m = p.matcher(line);
		    	 if (m.matches()) { // if it does
		    		 ExampleFileFilter eff = new ExampleFileFilter(
		    				 m.group(1).split(";"), m.group(2));
		    		 // we don't want to overwrite an already existing filter
		    		 if (! filtermap.containsKey(eff.getDescription())) {
		    			 // we add it as filter and in the map.
		    			 filechooser.addChoosableFileFilter(eff);
		    			 filtermap.put(eff.getDescription(),somefile);
		    		 }
		    		 // we want to allow for more than one filter line per XSLT file
		    		 // so we don't exit once we've found one and even account for
		    		 // the fact that we might trespass the MAX_READ_LINES limit
		    		 l--;
		    	 }
		        l++;
		    }
		} catch (IOException e) {
			LogTool.warn(e.getMessage());
		} finally {
		    if (xsl != null) {
		    	try {
		    		xsl.close();
		    	} catch (IOException e) {
		    		LogTool.warn(e.getMessage());
		    		e.printStackTrace();
		    	}
			}
		}
	}

	/**
	 * A simple help function to get the directory where to search for XSLT 
	 * export files written by the user.
	 * @return The user directory where XSLT export files are supposed to be.
	 */
	private File getXsltUserDirectory() {
		// TODO dpolivaev 26.02.2009 How to get Freeplane's user directory resp. the user's XSLT directory
		return new File(ResourceController.getResourceController().getFreeplaneUserDirectory(),
				"xslt");
	}

	/**
	 * A simple help function to get the directory where to search for XSLT 
	 * export files distributed with Freeplane.
	 * @return The system directory where XSLT export files are supposed to be.
	 */
	private File getXsltSysDirectory() {
		// TODO dpolivaev 26.02.2009 How to get Freeplane's base directory resp. the system XSLT directory
		return new File(ResourceController.getResourceController().getResourceBaseDir(),
				"xslt");
	}
}
