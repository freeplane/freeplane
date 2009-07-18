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
import java.awt.image.RenderedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.lang.StringUtils;
import org.freeplane.core.controller.Controller;
import org.freeplane.core.io.MapWriter.Mode;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.MapModel;
import org.freeplane.core.model.MindIcon;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.url.UrlManager;
import org.freeplane.core.util.Compat;
import org.freeplane.core.util.LogTool;
import org.freeplane.core.util.ResUtil;
import org.freeplane.features.mindmapmode.MModeController;
import org.freeplane.n3.nanoxml.IXMLParser;
import org.freeplane.n3.nanoxml.IXMLReader;
import org.freeplane.n3.nanoxml.StdXMLReader;
import org.freeplane.n3.nanoxml.XMLElement;
import org.freeplane.n3.nanoxml.XMLParserFactory;

/**
 * @author foltin To change the template for this generated type comment go to
 *         Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class ExportWithXSLTDialogAction extends ExportAction {

	public ExportWithXSLTDialogAction(Controller controller) {
	    super("ExportWithXSLTDialog", controller);
    }
	/**
     * 
     */
    private static final long serialVersionUID = 1L;
	static private final ExportDialog exp = new ExportDialog();
	
	private void export(final File file) {
		exp.export(getController().getViewController().getFrame(), file);
	}
	public void actionPerformed(final ActionEvent e) {
		final ModeController mc = getModeController();
		final MapModel model = getController().getMap();
		if (model == null) {
			return;
		}
		if ((model.getFile() == null) || model.isReadOnly()) {
			if (((MModeController) mc).save()) {
				export(model.getFile());
				return;
			}
			else {
				return;
			}
		}
		else {
			export(model.getFile());
		}
	}
}
