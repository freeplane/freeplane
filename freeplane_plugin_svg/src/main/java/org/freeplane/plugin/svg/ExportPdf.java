/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file author is Christian Foltin
 *  It is modified by Dimitry Polivaev in 2008.
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
package org.freeplane.plugin.svg;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.batik.svggen.SVGGeneratorContext;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.transcoder.SVGAbstractTranscoder;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.fop.configuration.ConfigurationException;
import org.apache.fop.configuration.DefaultConfiguration;
import org.apache.fop.svg.AbstractFOPTranscoder;
import org.apache.fop.svg.PDFTranscoder;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.features.export.mindmapmode.ExportController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.view.swing.map.MapView;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author foltin
 */
class ExportPdf extends ExportVectorGraphic {

	private static final String PDF_CONVERT_TEXT_TO_SHAPES = "org.freeplane.plugin.svg.export.pdf.convert_text_to_shapes";
	public ExportPdf() {
	}

	@Override
	public void export(List<NodeModel> branches, File chosenFile) {
		MapModel map = branches.get(0).getMap();
		if (!ExportController.getContoller().checkCurrentMap(map)){
			return;
		}
		try {
			final MapView view = (MapView) Controller.getCurrentController().getMapViewManager().getMapViewComponent();
			if (view == null) {
				return;
			}
			Controller.getCurrentController().getViewController().setWaitingCursor(true);
			transcodeSvgToPdfFile(chosenFile, fillSVGGraphics2D(view));
		}
		catch (final Exception ex) {
			org.freeplane.core.util.LogUtils.warn(ex);
			UITools.errorMessage(ex.getLocalizedMessage());
		}
		finally{
			Controller.getCurrentController().getViewController().setWaitingCursor(false);
		}
	}

	private void transcodeSvgToPdfFile(File chosenFile, SVGGraphics2D g2d) throws TranscoderException, IOException {
		final Document doc = g2d.getDOMFactory();
		final Element rootE = doc.getDocumentElement();
		g2d.getRoot(rootE);
		final TranscoderInput input = new TranscoderInput(doc);
		final FileOutputStream ostream = new FileOutputStream(chosenFile);
		final BufferedOutputStream bufStream = new BufferedOutputStream(ostream);
		final TranscoderOutput output = new TranscoderOutput(bufStream);
		final PDFTranscoder transcoder = createPdfTranscoder();
		transcoder.transcode(input, output);
		ostream.flush();
		ostream.close();
	}

	@NotNull
	private PDFTranscoder createPdfTranscoder() {
		final PDFTranscoder pdfTranscoder = new PDFTranscoder();
		/*
		 * according to https: &aid=1921334&group_id=7118 Submitted By:
		 * Frank Spangenberg (f_spangenberg) Summary: Large mind maps
		 * produce invalid PDF
		 */
		pdfTranscoder.addTranscodingHint(SVGAbstractTranscoder.KEY_MAX_HEIGHT, new Float(19200));
		pdfTranscoder.addTranscodingHint(SVGAbstractTranscoder.KEY_MAX_WIDTH, new Float(19200));
		/* end patch */
		pdfTranscoder.addTranscodingHint(ImageTranscoder.KEY_PIXEL_UNIT_TO_MILLIMETER, 25.4f/72f/ UITools.FONT_SCALE_FACTOR);
		if(ResourceController.getResourceController().getBooleanProperty(PDF_CONVERT_TEXT_TO_SHAPES)) {
			pdfTranscoder.addTranscodingHint(AbstractFOPTranscoder.KEY_AUTO_FONTS, Boolean.FALSE);
		}
		else {
            DefaultConfiguration c = new DefaultConfiguration("cfg");
            DefaultConfiguration fonts = new DefaultConfiguration("fonts");
            DefaultConfiguration autodetect = new DefaultConfiguration("auto-detect");
            fonts.addChild(autodetect);
            c.addChild(fonts);
            try {
				pdfTranscoder.configure(c);
			}
			catch (ConfigurationException e) {
				throw new RuntimeException(e);
			}
		}
		return pdfTranscoder;
	}



	@Override
	protected SVGGeneratorContext createGeneratorContext(final Document domFactory) {
		final SVGGeneratorContext ctx = super.createGeneratorContext(domFactory);
		if(ResourceController.getResourceController().getBooleanProperty(PDF_CONVERT_TEXT_TO_SHAPES))
			ctx.setEmbeddedFontsOn(true);
		return ctx;
	}

}
