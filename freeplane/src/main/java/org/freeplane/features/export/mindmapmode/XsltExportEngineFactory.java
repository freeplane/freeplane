package org.freeplane.features.export.mindmapmode;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.ExampleFileFilter;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;

class XsltExportEngineFactory {
	final private static String EXPORT_FILTER_PATTERN = "^.*MINDMAPEXPORTFILTER\\s+(\\S+)\\s+(.*)(?:\\s+-->)?$";
	/** A pattern which is "MINDMAPEXPORTFILTER ext1;ext2;... File format description" */
	final private static String FILE_NAME_PATTERN = "mm2([\\w]+)\\.xsl";
	final private static Pattern COMPILED_EXPORT_FILTER_PATTERN = Pattern.compile(EXPORT_FILTER_PATTERN);
	final private static Pattern COMPILED_FILE_NAME_PATTERN = Pattern.compile(FILE_NAME_PATTERN);
	/** Maximum number of lines we read in each XSLT files for performance reasons */
	final private static int MAX_READ_LINES = 5;
	private ExportController controller;

	/** This method populates the {@link #filterMap} and the {@link #fileFilters} field. */
	void gatherXsltScripts(ExportController controller) {
		try{
			this.controller = controller;
		// look for user xslt scripts first
		gatherXsltScripts(getXsltUserDirectory());
		// use system xslt scripts as a fallback
		gatherXsltScripts(getXsltSysDirectory());
		}
		finally{
			this.controller = null;
		}
	}

	/**
	 * A simple help function to get the directory where to search for XSLT 
	 * export files distributed with Freeplane.
	 * @return The system directory where XSLT export files are supposed to be.
	 */
	File getXsltSysDirectory() {
		return new File(ResourceController.getResourceController().getResourceBaseDir(), "xslt");
	}

	/**
	 * A simple help function to get the directory where to search for XSLT 
	 * export files written by the user.
	 * @return The user directory where XSLT export files are supposed to be.
	 */
	File getXsltUserDirectory() {
		return new File(ResourceController.getResourceController().getFreeplaneUserDirectory(), "xslt");
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
		final File xsltFiles[] = xsltdir.listFiles(new java.io.FileFilter() {
			public boolean accept(final File pathname) {
				return (pathname.isFile() && pathname.canRead() && pathname.getPath().toLowerCase().endsWith(".xsl"));
			}
		});
		// we compile the pattern for performance reasons.
		// then for each found file, we check and extract a potentially present filter
		for (int i = 0; i < xsltFiles.length; i++) {
			extractFilterFromFile(xsltFiles[i]);
		}
	}

	/**
	 * The function checks if the pattern matches with one of the lines in the file.
	 * @param xsltFile the file to open and search a line matching the pattern.
	 */
	private void extractFilterFromFile(final File xsltFile) {
		try (BufferedReader xsl = new BufferedReader(new FileReader(xsltFile))){
			String line;
			int l = 0;
			boolean keyFound = false;
			// ...we open it and check if it contains the right marker
			while ((line = xsl.readLine()) != null && l < MAX_READ_LINES) {
				final Matcher m = COMPILED_EXPORT_FILTER_PATTERN.matcher(line);
				if (m.matches()) { // if it does
					keyFound = true;
					final String[] extensions = m.group(1).split("\\s*;\\s*");
					String description = m.group(2).trim();
					if(description.startsWith("%")){
						description = TextUtils.getText(description.substring(1));
					}
					addXsltFile(extensions, description, xsltFile);
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
			final Matcher m = COMPILED_FILE_NAME_PATTERN.matcher(xsltFile.getName());
			if (m.matches()) { // if it does
				final String extension = m.group(1);
				final String description = TextUtils.format("exported_file", extension);
				addXsltFile(new String[] { extension }, description, xsltFile);
			}
		}
		catch (final IOException e) {
			LogUtils.warn(e);
			UITools.errorMessage(TextUtils.getText("export_failed"));
		}
	}

	private void addXsltFile(final String[] extensions, String description, final File xsltFile) {
		final ExampleFileFilter filter = new ExampleFileFilter(extensions, TextUtils.getOptionalTranslation(description));
		final XsltExportEngine exporter = new XsltExportEngine(xsltFile);
		controller.addMapExportEngine(filter, exporter);
		controller.addBranchExportEngine(filter, exporter);
	}

}
