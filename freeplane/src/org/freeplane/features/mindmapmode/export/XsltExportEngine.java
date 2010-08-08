package org.freeplane.features.mindmapmode.export;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.freeplane.core.controller.Controller;
import org.freeplane.features.common.map.MapModel;
import org.freeplane.features.common.map.ModeController;
import org.freeplane.features.common.map.MapWriter.Mode;

public class XsltExportEngine implements IExportEngine {
	public XsltExportEngine(File xsltFile) {
	    super();
	    this.xsltFile = xsltFile;
    }
	final private File xsltFile;

	public void export(MapModel map, File toFile) {
		final Source xsltSource = new StreamSource(xsltFile);
		final XmlExporter xe = new XmlExporter();
		final Source xmlSource = getMapXml(map);
		xe.transform(xmlSource, xsltSource, toFile);
	}
	/**
	 * @param mode 
	 * @throws IOException
	 */
	private StreamSource getMapXml(final MapModel map) {
		final StringWriter writer = new StringWriter();
		final ModeController modeController = Controller.getCurrentModeController();
		try {
			modeController.getMapController().getFilteredXml(map, writer, Mode.EXPORT, true);
		}
		catch (final IOException e) {
			e.printStackTrace();
		}
		final StringReader stringReader = new StringReader(writer.getBuffer().toString());
		return new StreamSource(stringReader);
	}
}
