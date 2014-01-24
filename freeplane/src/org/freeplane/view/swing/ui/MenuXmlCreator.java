/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2013 Dimitry Polivaev
 *
 *  This file author is Dimitry Polivaev
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
package org.freeplane.view.swing.ui;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.XsltPipeReaderFactory;

/**
 * @author Dimitry Polivaev
 * 29.09.2013
 */
public class MenuXmlCreator {

	private static final String MM = "mm";
	private static final String XML = "xml";
	private final String xslt;

	public MenuXmlCreator(String xslt) {
	    super();
	    this.xslt = xslt;
    }

	public URL menuResource(String menuStructureXmlPath) {
		try {
	        updateXml(menuStructureXmlPath);
        }
        catch (IOException e) {
        	LogUtils.warn(e);
        }
	    URL xmlResource = ResourceController.getResourceController().getResource(menuStructureXmlPath);
	    return xmlResource;
	}

	private void updateXml(String menuStructureXmlPath) throws IOException{
	    ResourceController resourceController = ResourceController.getResourceController();
		URL xmlResource = resourceController.getResource(menuStructureXmlPath);
	    String menuStructureMapPath = menuStructureXmlPath.subSequence(0, menuStructureXmlPath.length() - XML.length()) + MM;
	    URL mmResource = resourceController.getResource(menuStructureMapPath);
	    if (isFile(mmResource)) {
	        File mmFile = toFile(mmResource);
			if (!isFile(xmlResource) || toFile(xmlResource).lastModified() <= mmFile.lastModified()) {
	        	transformMindMapToXml(mmFile);
	        }
        }
	}

	private void transformMindMapToXml(File mmFile) throws IOException {
		Writer out = null;
		try {
	        String xml = new XsltPipeReaderFactory().transform(mmFile, xslt);
	        String mmPath = mmFile.getPath();
	        String xmlPath = mmPath.substring(0, mmPath.length() - MM.length()) + XML;
	        out = new BufferedWriter(new OutputStreamWriter(
	            new FileOutputStream(xmlPath), "UTF-8"));
	        out.write(xml);
		}
		catch(Exception ex){
			LogUtils.warn(ex);
		}
        finally {
    		try {
	            if (out != null )out.close();
            }
            catch (Exception e) {
            }
        }
    }

	private File toFile(URL url) {
	    return new File (url.getFile());
    }

	private boolean isFile(URL url) {
	    return url != null && "file".equals(url.getProtocol());
    }
}
