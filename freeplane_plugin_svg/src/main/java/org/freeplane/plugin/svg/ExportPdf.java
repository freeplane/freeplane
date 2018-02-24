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

import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.transcoder.SVGAbstractTranscoder;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.fop.svg.AbstractFOPTranscoder;
import org.apache.fop.svg.PDFTranscoder;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.features.export.mindmapmode.ExportController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.view.swing.map.MapView;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author foltin
 */
class ExportPdf extends ExportVectorGraphic {

	public ExportPdf() {
	}

	public void export(MapModel map, File chosenFile) {
		if (!ExportController.getContoller().checkCurrentMap(map)){
			return;
		}
		try {
			final MapView view = (MapView) Controller.getCurrentController().getMapViewManager().getMapViewComponent();
			if (view == null) {
				return;
			}
			Controller.getCurrentController().getViewController().setWaitingCursor(true);
			final SVGGraphics2D g2d = fillSVGGraphics2D(view);
			final PDFTranscoder pdfTranscoder = new PDFTranscoder();
			/*
			 * according to https: &aid=1921334&group_id=7118 Submitted By:
			 * Frank Spangenberg (f_spangenberg) Summary: Large mind maps
			 * produce invalid PDF
			 */
			pdfTranscoder.addTranscodingHint(SVGAbstractTranscoder.KEY_MAX_HEIGHT, new Float(19200));
			pdfTranscoder.addTranscodingHint(SVGAbstractTranscoder.KEY_MAX_WIDTH, new Float(19200));
			pdfTranscoder.addTranscodingHint(ImageTranscoder.KEY_PIXEL_UNIT_TO_MILLIMETER, 25.4f/72f/UITools.FONT_SCALE_FACTOR);
			pdfTranscoder.addTranscodingHint(AbstractFOPTranscoder.KEY_AUTO_FONTS, Boolean.FALSE);
			pdfTranscoder.addTranscodingHint(AbstractFOPTranscoder.KEY_STROKE_TEXT, Boolean.TRUE);
			/* end patch */
			final Document doc = g2d.getDOMFactory();
			final Element rootE = doc.getDocumentElement();
			g2d.getRoot(rootE);
			final TranscoderInput input = new TranscoderInput(doc);
			final FileOutputStream ostream = new FileOutputStream(chosenFile);
			final BufferedOutputStream bufStream = new BufferedOutputStream(ostream);
			final TranscoderOutput output = new TranscoderOutput(bufStream);
			pdfTranscoder.transcode(input, output);
			ostream.flush();
			ostream.close();
		}
		catch (final Exception ex) {
			org.freeplane.core.util.LogUtils.warn(ex);
			UITools.errorMessage(ex.getLocalizedMessage());
		}
		finally{
			Controller.getCurrentController().getViewController().setWaitingCursor(false);
		}
	}

}
