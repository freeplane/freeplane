package org.freeplane.features.export.mindmapmode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.MapWriter.Mode;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;

public class XsltExportEngine implements IExportEngine {
	public XsltExportEngine(File xsltFile) {
	    super();
	    this.xsltFile = xsltFile;
    }
	final private File xsltFile;

	public void export(MapModel map, File toFile) {
		final Source xsltSource = new StreamSource(xsltFile);
		final Source xmlSource = getMapXml(map);
		FileOutputStream outputStream = null;
        try {
        	outputStream = new FileOutputStream(toFile);
        	final Result result = new StreamResult(outputStream);
        	final TransformerFactory transFact = TransformerFactory.newInstance();
        	final Transformer trans = transFact.newTransformer(xsltSource);
        	trans.transform(xmlSource, result);
        }
        catch (final Exception e) {
        	UITools.errorMessage(TextUtils.getText("export_failed"));
        	LogUtils.warn(e);
        }
        finally {
        	try {
        		if (outputStream != null) {
        			outputStream.close();
        		}
        	}
        	catch (final IOException e) {
        		LogUtils.severe(e);
        	}
        }
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
