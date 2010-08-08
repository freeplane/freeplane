package org.freeplane.features.mindmapmode.export;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.filechooser.FileFilter;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.common.map.MapModel;
import org.freeplane.features.common.map.ModeController;
import org.freeplane.features.mindmapmode.text.ExampleFileFilter;
import org.freeplane.n3.nanoxml.IXMLParser;
import org.freeplane.n3.nanoxml.IXMLReader;
import org.freeplane.n3.nanoxml.StdXMLReader;
import org.freeplane.n3.nanoxml.XMLElement;
import org.freeplane.n3.nanoxml.XMLParserFactory;

/**
 * A registry of all XSLT scripts that are available to transform a .mm file into another format.
 * The XSLT file directories are scanned anew by each instance of this class to account for changes during uptime.
 * The filterMap maps descriptions onto a XSLT file. This enables multiple filters for one file extension.
 */
public class ExportController implements IExtension{
	final private static String EXPORT_FILTER_PATTERN = "^.*MINDMAPEXPORTFILTER\\s+(\\S+)\\s+(.*)(?:\\s+-->)?$";
	/** A pattern which is "MINDMAPEXPORTFILTER ext1;ext2;... File format description" */
	final private static String FILE_NAME_PATTERN = "mm2([\\w]+)\\.xsl";
	final private static Pattern COMPILED_EXPORT_FILTER_PATTERN = Pattern.compile(EXPORT_FILTER_PATTERN);
	final private static Pattern COMPILED_FILE_NAME_PATTERN = Pattern.compile(FILE_NAME_PATTERN);
	/** Maximum number of lines we read in each XSLT files for performance reasons */
	final private static int MAX_READ_LINES = 5;

	/** a hash where the key is the file extension and the value the filename of
	 * the corresponding XSLT sheet. */
	final private HashMap<FileFilter, IExportEngine> filterMap = new HashMap<FileFilter, IExportEngine>();
	final private ArrayList<FileFilter> fileFilters = new ArrayList<FileFilter>();

	public static void install(ExportController exportController) {
	    Controller.getCurrentModeController().addExtension(ExportController.class, exportController);
    }
	
	public ExportController(final String xmlDescriptorFile) {
		final ModeController modeController = Controller.getCurrentModeController();
		final ExportAction action = new ExportAction();
		modeController.addAction(action);

		final ExportToHTMLAction exportToHTMLAction = new ExportToHTMLAction();
		addExportEngine(exportToHTMLAction.getFileFilter(), exportToHTMLAction);
		final ExportBranchToHTMLAction exportBranchToHTMLAction = new ExportBranchToHTMLAction();
		addExportEngine(exportBranchToHTMLAction.getFileFilter(), exportBranchToHTMLAction);
		
		final ExportToOoWriter exportToOoWriter = new ExportToOoWriter();
		
		addExportEngine(exportToOoWriter.getFileFilter(), exportToOoWriter);
		createImageExporters();
		createXSLTExportActions(xmlDescriptorFile);
		gatherXsltScripts();
	}

	public void createImageExporters() {
		final ExportToImage pngExport = new ExportToImage("png","Portable Network Graphic (PNG)");
		addExportEngine(pngExport.getFileFilter(), pngExport);
		final ExportToImage jpgExport = new ExportToImage("jpg","Compressed image (JPEG)");
		addExportEngine(jpgExport.getFileFilter(), jpgExport);
	}
	
	private void createXSLTExportActions( final String xmlDescriptorFile) {
		try {
			final IXMLParser parser = XMLParserFactory.createDefaultXMLParser();
			final URL resource = ResourceController.getResourceController().getResource(xmlDescriptorFile);
			final IXMLReader reader = new StdXMLReader(resource.openStream());
			parser.setReader(reader);
			final XMLElement xml = (XMLElement) parser.parse();
			final Enumeration<XMLElement> actionDescriptors = xml.enumerateChildren();
			while (actionDescriptors.hasMoreElements()) {
				final XMLElement descriptor = actionDescriptors.nextElement();
				final String name = descriptor.getAttribute("name", null);
				final XMLElement xmlProperties = descriptor.getFirstChildNamed("properties");
				final Properties properties = xmlProperties.getAttributes();
				final ExportWithXSLT action = new ExportWithXSLT(name, properties);
				addExportEngine(action.getFileFilter(), action);
			}
		}
		catch (final Exception e) {
			LogUtils.severe(e);
		}
	}

	/** This method populates the {@link #filterMap} and the {@link #fileFilters} field. */
	private void gatherXsltScripts() {
		gatherXsltScripts(getXsltSysDirectory());
		// overwrite with user settings
		gatherXsltScripts(getXsltUserDirectory());
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
		BufferedReader xsl = null;
		try {
			xsl = new BufferedReader(new FileReader(xsltFile));
			String line;
			int l = 0;
			boolean keyFound = false;
			// ...we open it and check if it contains the right marker
			while ((line = xsl.readLine()) != null && l < MAX_READ_LINES) {
				final Matcher m = COMPILED_EXPORT_FILTER_PATTERN.matcher(line);
				if (m.matches()) { // if it does
					keyFound = true;
					final String[] extensions = m.group(1).split("\\s*;\\s*");
					final String description = m.group(2).trim();
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
				final String description = TextUtils.formatText("exported_file", extension);
				addXsltFile(new String[] { extension }, description, xsltFile);
			}
		}
		catch (final IOException e) {
			LogUtils.warn(e);
			UITools.errorMessage(TextUtils.getText("export_failed"));
		}
		finally {
			if (xsl != null) {
				try {
					xsl.close();
				}
				catch (final IOException e) {
					LogUtils.severe(e);
				}
			}
		}
	}

	private void addXsltFile(final String[] extensions, final String description, final File xsltFile) {
		final ExampleFileFilter filter = new ExampleFileFilter(extensions, description);
		final XsltExportEngine exporter = new XsltExportEngine(xsltFile);
		addExportEngine(filter, exporter);
	}

	public void addExportEngine(final FileFilter filter, final IExportEngine exporter) {
	    fileFilters.add(filter);
		filterMap.put(filter, exporter);
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

	/** returns a Map(description -> xsltFile). */
	public HashMap<FileFilter, IExportEngine> getFilterMap() {
    	return filterMap;
    }

	/** returns a list of all appropriate FileFilters for a FileChooser. */
	public List<FileFilter> getFileFilters() {
    	return fileFilters;
    }

	public static ExportController getContoller() {
		return getController(Controller.getCurrentModeController());
    }

	public static ExportController getController(ModeController modeController) {
		return (ExportController) modeController.getExtension(ExportController.class);
    }

	public boolean checkCurrentMap(MapModel map) {
		if(map.equals(Controller.getCurrentController().getMap())){
			return true;
		}
		UITools.errorMessage(TextUtils.getText("export_works_for_current_map_only"));
	    return false;
    }

}
