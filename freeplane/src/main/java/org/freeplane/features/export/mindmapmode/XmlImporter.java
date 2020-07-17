package org.freeplane.features.export.mindmapmode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import javax.swing.JOptionPane;

import org.freeplane.core.util.Compat;
import org.freeplane.core.util.FileUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.core.util.XsltPipeReaderFactory;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.MapWriter.Mode;
import org.freeplane.features.map.mindmapmode.MMapModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.n3.nanoxml.XMLException;
import org.freeplane.n3.nanoxml.XMLParseException;

public class XmlImporter	{

	final private String xsltResource;
	public XmlImporter(final String xsltResource){
		this.xsltResource = xsltResource;
	}

	public void importXml(final File file) throws XMLParseException, MalformedURLException, IOException, URISyntaxException, XMLException{
		final File directory = file.getParentFile();
		final File outputFile = new File (directory, file.getName() + org.freeplane.features.url.UrlManager.FREEPLANE_FILE_EXTENSION);
		importXml(file, outputFile);
	}

	public void importXml(final File inputFile, final File outputFile) throws FileNotFoundException, IOException,
			XMLParseException, URISyntaxException, XMLException, MalformedURLException {
		try(FileInputStream in = new FileInputStream(inputFile)){
			importXml(in, outputFile);
		}
	}

	public void importXml(final InputStream in, final File outputFile) throws IOException, FileNotFoundException,
	XMLParseException, URISyntaxException, XMLException, MalformedURLException {
		final URL mapUrl = Compat.fileToUrl(outputFile);
		if(outputFile.exists()){
			if(Controller.getCurrentController().getMapViewManager().tryToChangeToMapView(mapUrl))
				return;
			final int overwriteMap = JOptionPane.showConfirmDialog(Controller.getCurrentController()
					.getMapViewManager().getMapViewComponent(), TextUtils.getText("map_already_exists"), "Freeplane",
					JOptionPane.YES_NO_OPTION);
			if (overwriteMap != JOptionPane.YES_OPTION) {
				return ;
			}
		}
		newMap(in, outputFile);
	}

	private void newMap(final InputStream in, final File outputFile)
			throws IOException, XMLException, MalformedURLException {
		final Reader reader = new XsltPipeReaderFactory(xsltResource).getReader(in);
		final ModeController modeController = Controller.getCurrentModeController();
		final MapController mapController = modeController.getMapController();
		final MapModel map = new MMapModel();
		modeController.getMapController().getMapReader().createNodeTreeFromXml(map, reader, Mode.FILE);
		final URL mapUrl = Compat.fileToUrl(outputFile);
		map.setURL(mapUrl);
		map.setSaved(false);
		mapController.fireMapCreated(map);
		mapController.createMapView(map);
	}

}