package org.freeplane.features.export.mindmapmode;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.swing.filechooser.FileFilter;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.io.xml.XMLLocalParserFactory;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.FileUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.n3.nanoxml.IXMLParser;
import org.freeplane.n3.nanoxml.IXMLReader;
import org.freeplane.n3.nanoxml.StdXMLReader;
import org.freeplane.n3.nanoxml.XMLElement;

/**
 * A registry of all XSLT scripts that are available to transform a .mm file into another format.
 * The XSLT file directories are scanned anew by each instance of this class to account for changes during uptime.
 * The filterMap maps descriptions onto a XSLT file. This enables multiple filters for one file extension.
 */
public class ExportController implements IExtension{
	/** a hash where the key is the file extension and the value the filename of
	 * the corresponding XSLT sheet. */
	final private HashMap<FileFilter, IExportEngine> mapExportEngines = new HashMap<FileFilter, IExportEngine>();
	final private ArrayList<FileFilter> mapExportFileFilters = new ArrayList<FileFilter>();

	final private HashMap<FileFilter, IExportEngine> branchExportEngines = new HashMap<FileFilter, IExportEngine>();
	final private ArrayList<FileFilter> branchExportFileFilters = new ArrayList<FileFilter>();

	public static void install(ExportController exportController) {
	    Controller.getCurrentModeController().addExtension(ExportController.class, exportController);
    }
	
	public ExportController(final String xmlDescriptorFile) {
		final ModeController modeController = Controller.getCurrentModeController();
		modeController.addAction(new ExportAction());
		modeController.addAction(new ExportBranchesAction());

		final ExportToHTML exportToHTML = new ExportToHTML();
		addMapExportEngine(exportToHTML.getFileFilter(), exportToHTML);
		final ExportBranchesToHTML exportBranchesToHTML = new ExportBranchesToHTML();
		addBranchExportEngine(exportBranchesToHTML.getFileFilter(), exportBranchesToHTML);
		
		final ExportToOoWriter exportToOoWriter = new ExportToOoWriter();

		addMapExportEngine(exportToOoWriter.getFileFilter(), exportToOoWriter);
		addBranchExportEngine(exportToOoWriter.getFileFilter(), exportToOoWriter);
		createImageExporters();
		createXSLTExportActions(xmlDescriptorFile);
		new XsltExportEngineFactory().gatherXsltScripts(this);
		Collections.sort(mapExportFileFilters, new Comparator<FileFilter>() {
			public int compare(FileFilter f1, FileFilter f2) {
	            return f1.getDescription().compareToIgnoreCase(f2.getDescription());
            }
		});
	}

	public void createImageExporters() {
		final ExportToImage pngExport = new ExportToImage("png","Portable Network Graphic (PNG)");
		addMapExportEngine(pngExport.getFileFilter(), pngExport);
		final ExportToImage jpgExport = new ExportToImage("jpg","Compressed image (JPEG)");
		addMapExportEngine(jpgExport.getFileFilter(), jpgExport);
	}
	
	private void createXSLTExportActions( final String xmlDescriptorFile) {
		InputStream xmlDescriptorStream = null;
		try {
			final IXMLParser parser = XMLLocalParserFactory.createLocalXMLParser();
			final URL resource = ResourceController.getResourceController().getResource(xmlDescriptorFile);
			xmlDescriptorStream = resource.openStream();
			final IXMLReader reader = new StdXMLReader(xmlDescriptorStream);
			parser.setReader(reader);
			final XMLElement xml = (XMLElement) parser.parse();
			final Enumeration<XMLElement> actionDescriptors = xml.enumerateChildren();
			while (actionDescriptors.hasMoreElements()) {
				final XMLElement descriptor = actionDescriptors.nextElement();
				final String name = descriptor.getAttribute("name", null);
				final XMLElement xmlProperties = descriptor.getFirstChildNamed("properties");
				final Properties properties = xmlProperties.getAttributes();
				final ExportWithXSLT action = new ExportWithXSLT(name, properties);
				FileFilter fileFilter = action.getFileFilter();
				addMapExportEngine(fileFilter, action);
				if(Boolean.parseBoolean(properties.getProperty("branch_export")))
					addBranchExportEngine(fileFilter, action);
			}
		}
		catch (final Exception e) {
			LogUtils.severe(e);
		}
		finally {
			FileUtils.silentlyClose(xmlDescriptorStream);
		}
	}

	public void addMapExportEngine(final FileFilter filter, final IExportEngine exporter) {
		if (! mapExportEngines.values().contains(exporter))
		{
			mapExportFileFilters.add(filter);
			mapExportEngines.put(filter, exporter);
		}
    }

	public void addBranchExportEngine(final FileFilter filter, final IExportEngine exporter) {
		if (! branchExportEngines.values().contains(exporter))
		{
			branchExportFileFilters.add(filter);
			branchExportEngines.put(filter, exporter);
		}
	}
	public HashMap<FileFilter, IExportEngine> getMapExportEngines() {
		return mapExportEngines;
	}
	public HashMap<FileFilter, IExportEngine> getBranchExportEngines() {
		return branchExportEngines;
	}

	public List<FileFilter> getMapExportFileFilters() {
		return mapExportFileFilters;
	}

	public List<FileFilter> getBranchExportFileFilters() {
		return branchExportFileFilters;
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
