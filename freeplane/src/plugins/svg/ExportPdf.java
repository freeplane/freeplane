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
package plugins.svg;

import java.awt.event.ActionEvent;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import javax.swing.JOptionPane;

import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.transcoder.SVGAbstractTranscoder;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.fop.svg.PDFTranscoder;
import org.freeplane.controller.ActionDescriptor;
import org.freeplane.controller.Controller;
import org.freeplane.map.tree.view.MapView;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author foltin
 */
@ActionDescriptor(
       name="plugins/ExportPdf.xml_name",
       locations={"/menu_bar/file/export/export"}
)
public class ExportPdf extends ExportVectorGraphic {
	public ExportPdf() {
		super();
	}

	public void actionPerformed(final ActionEvent e) {
		final File chosenFile = chooseFile("pdf", Controller
		    .getText("export_pdf_text"), null);
		if (chosenFile == null) {
			return;
		}
		try {
			final MapView view = Controller.getController().getMapView();
			if (view == null) {
				return;
			}
			Controller.getController().getViewController().setWaitingCursor(
			    true);
			final SVGGraphics2D g2d = fillSVGGraphics2D(view);
			final PDFTranscoder pdfTranscoder = new PDFTranscoder();
			/*
			 * according to https: &aid=1921334&group_id=7118 Submitted By:
			 * Frank Spangenberg (f_spangenberg) Summary: Large mind maps
			 * produce invalid PDF
			 */
			pdfTranscoder.addTranscodingHint(
			    SVGAbstractTranscoder.KEY_MAX_HEIGHT, new Float(19200));
			pdfTranscoder.addTranscodingHint(
			    SVGAbstractTranscoder.KEY_MAX_WIDTH, new Float(19200));
			/* end patch */
			final Document doc = g2d.getDOMFactory();
			final Element rootE = doc.getDocumentElement();
			g2d.getRoot(rootE);
			final TranscoderInput input = new TranscoderInput(doc);
			final FileOutputStream ostream = new FileOutputStream(chosenFile);
			final BufferedOutputStream bufStream = new BufferedOutputStream(
			    ostream);
			final TranscoderOutput output = new TranscoderOutput(bufStream);
			pdfTranscoder.transcode(input, output);
			ostream.flush();
			ostream.close();
		}
		catch (final Exception ex) {
			org.freeplane.main.Tools.logException(ex);
			JOptionPane.showMessageDialog(Controller.getController()
			    .getViewController().getContentPane(),
			    ex.getLocalizedMessage(), null, JOptionPane.ERROR_MESSAGE);
		}
		Controller.getController().getViewController().setWaitingCursor(false);
	}
}
