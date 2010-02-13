/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
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

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.transform.stream.StreamSource;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.io.MapWriter.Mode;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.MapModel;
import org.freeplane.features.mindmapmode.MModeController;

/**
 * @author foltin To change the template for this generated type comment go to
 *         Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class ExportWithXSLTDialogAction extends ExportAction {
	static private final ExportDialog exp = new ExportDialog();
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ExportWithXSLTDialogAction(final Controller controller) {
		super("ExportWithXSLTDialog", controller);
	}

	public void actionPerformed(final ActionEvent e) {
		final MapModel model = getController().getMap();
		if (model == null) {
			return;
		}
		export(model.getFile());
	}

	private void export(final File file) {
		exp.export(getController().getViewController().getFrame(), getMapXml(Mode.EXPORT), file);
	}
	/**
	 * @param mode 
	 * @throws IOException
	 */
	private StreamSource getMapXml(final Mode mode){
		final StringWriter writer = new StringWriter();
		final ModeController modeController = getModeController();
		final Controller controller = modeController.getController();
		final MapModel map = controller.getMap();
		try {
			modeController.getMapController().getFilteredXml(map, writer, mode);
		} catch (IOException e) {
			e.printStackTrace();
		}
		StringReader stringReader = new StringReader(writer.getBuffer().toString());
		return new StreamSource(stringReader);
	}
}
