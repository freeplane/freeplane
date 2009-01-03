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
package org.freeplane.addins.mindmapmode.export;

import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
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
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.io.IXMLElement;
import org.freeplane.core.map.MapModel;
import org.freeplane.core.map.MindIcon;
import org.freeplane.core.map.ModeController;
import org.freeplane.core.map.NodeModel;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.url.UrlManager;
import org.freeplane.core.util.Tools;
import org.freeplane.features.mindmapmode.MModeController;
import org.freeplane.n3.nanoxml.IXMLParser;
import org.freeplane.n3.nanoxml.IXMLReader;
import org.freeplane.n3.nanoxml.StdXMLReader;
import org.freeplane.n3.nanoxml.XMLParserFactory;

/**
 * @author foltin To change the template for this generated type comment go to
 *         Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class ExportWithXSLT extends ExportAction {
	private static final String NAME_EXTENSION_PROPERTY = "name_extension";

	public static void createXSLTExportActions(final ModeController modeController,
	                                           final String xmlDescriptorFile) {
		try {
			final IXMLParser parser = XMLParserFactory.createDefaultXMLParser();
			final URL resource = Controller.getResourceController().getResource(xmlDescriptorFile);
			final IXMLReader reader = new StdXMLReader(resource.openStream());
			parser.setReader(reader);
			final IXMLElement xml = (IXMLElement) parser.parse();
			final Enumeration actionDescriptors = xml.enumerateChildren();
			while (actionDescriptors.hasMoreElements()) {
				final IXMLElement descriptor = (IXMLElement) actionDescriptors.nextElement();
				final String name = descriptor.getAttribute("name", null);
				final String tooltip = descriptor.getAttribute("tooltip", null);
				final String location = descriptor.getAttribute("location", null);
				final IXMLElement xmlProperties = descriptor.getFirstChildNamed("properties");
				final Properties properties = xmlProperties.getAttributes();
				final ExportWithXSLT action = new ExportWithXSLT(name, tooltip, properties);
				modeController.addAction(name, action);
				modeController.getUserInputListenerFactory().getMenuBuilder().addAction(location,
				    action, location + "/" + name, MenuBuilder.AS_CHILD);
			}
		}
		catch (final Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * For test purposes. True=no error
	 */
	private boolean mTransformResultWithoutError = false;
	final private Properties properties;

	public ExportWithXSLT(final String name, final String tooltip, final Properties properties) {
		super(name);
		this.properties = properties;
		setTooltip(tooltip);
	}

	public void actionPerformed(final ActionEvent e) {
		final ModeController mc = getModeController();
		final MapModel model = Controller.getController().getMap();
		if (Tools.safeEquals(getProperty("file_type"), "user")) {
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
		else {
			final File saveFile = chooseFile();
			if (saveFile == null) {
				return;
			}
			transform(saveFile);
		}
	}

	protected File chooseFile() {
		final String nameExtension = getProperty(ExportWithXSLT.NAME_EXTENSION_PROPERTY);
		return chooseFile(getProperty("file_type"),
		    getTranslatableResourceString("file_description"), nameExtension);
	}

	/**
	 */
	private void copyFilesFromResourcesToDirectory(final String directoryName, final String files,
	                                               final String filePrefix) {
		final StringTokenizer tokenizer = new StringTokenizer(files, ",");
		while (tokenizer.hasMoreTokens()) {
			final String next = tokenizer.nextToken();
			copyFromResource(filePrefix, next, directoryName);
		}
	}

	/**
	 */
	private boolean copyIcons(final String directoryName) {
		boolean success;
		final String iconDirectoryName = directoryName + File.separatorChar + "icons";
		success = createDirectory(iconDirectoryName);
		if (success) {
			copyIconsToDirectory(iconDirectoryName);
		}
		return success;
	}

	/**
	 */
	private void copyIconsToDirectory(final String directoryName2) {
		final Vector iconNames = MindIcon.getAllIconNames();
		for (int i = 0; i < iconNames.size(); ++i) {
			final String iconName = ((String) iconNames.get(i));
			final MindIcon myIcon = MindIcon.factory(iconName);
			copyFromResource(MindIcon.getIconsPath(), myIcon.getIconBaseFileName(), directoryName2);
		}
		final File iconDir = new File(
		    Controller.getResourceController().getFreemindUserDirectory(), "icons");
		if (iconDir.exists()) {
			final String[] userIconArray = iconDir.list(new FilenameFilter() {
				public boolean accept(final File dir, final String name) {
					return name.matches(".*\\.png");
				}
			});
			for (int i = 0; i < userIconArray.length; ++i) {
				final String iconName = userIconArray[i];
				if (iconName.length() == 4) {
					continue;
				}
				copyFromFile(iconDir.getAbsolutePath(), iconName, directoryName2);
			}
		}
	}

	private boolean copyMap(final String pDirectoryName) throws IOException {
		final boolean success = true;
		final BufferedWriter fileout = new BufferedWriter(new OutputStreamWriter(
		    new FileOutputStream(pDirectoryName + File.separator + "map.mm")));
		final MapModel map = Controller.getController().getMap();
		getModeController().getMapController().getFilteredXml(map, fileout);
		return success;
	}

	/**
	 */
	private boolean createDirectory(final String directoryName) {
		final File dir = new File(directoryName);
		if (!dir.exists()) {
			return dir.mkdir();
		}
		return true;
	}

	/**
	 */
	private void createImageFromMap(final String directoryName) {
		if (Controller.getController().getMapView() == null) {
			return;
		}
		final BufferedImage image = createBufferedImage();
		try {
			final FileOutputStream out = new FileOutputStream(directoryName + File.separator
			        + "image.png");
			ImageIO.write(image, "png", out);
			out.close();
		}
		catch (final IOException e1) {
			org.freeplane.core.util.Tools.logException(e1);
		}
	}

	private void export(final File file) {
		final ExportDialog exp = new ExportDialog(file);
		exp.setVisible(true);
	}

	/**
	 * @param create_image
	 */
	private String getAreaCode(final boolean create_image) {
		String areaCode = "";
		if (create_image) {
			final NodeModel root = Controller.getController().getMap().getRootNode();
			final ClickableImageCreator creator = new ClickableImageCreator(root,
			    getModeController(), getProperty("link_replacement_regexp"));
			areaCode = creator.generateHtml();
		}
		return areaCode;
	}

	/**
	 * @throws IOException
	 */
	private String getMapXml() throws IOException {
		final StringWriter writer = new StringWriter();
		final ModeController controller = getModeController();
		final MapModel map = Controller.getController().getMap();
		controller.getMapController().getFilteredXml(map, writer);
		return writer.getBuffer().toString();
	}

	public String getProperty(final String key) {
		return properties.getProperty(key, null);
	}

	private String getTranslatableResourceString(final String resourceName) {
		final String returnValue = getProperty(resourceName);
		if (returnValue != null && returnValue.startsWith("%")) {
			return Controller.getText(returnValue.substring(1));
		}
		return returnValue;
	}

	public boolean isTransformResultWithoutError() {
		return mTransformResultWithoutError;
	}

	/**
	 * @param saveFile
	 */
	public void transform(final File saveFile) {
		try {
			mTransformResultWithoutError = true;
			final boolean create_image = Tools.safeEquals(getProperty("create_html_linked_image"),
			    "true");
			final String areaCode = getAreaCode(create_image);
			final String xsltFileName = getProperty("xslt_file");
			boolean success = transformMapWithXslt(xsltFileName, saveFile, areaCode);
			if (!success) {
				JOptionPane.showMessageDialog(null, getProperty("error_applying_template"),
				    "Freemind", JOptionPane.ERROR_MESSAGE);
				return;
			}
			if (success && Tools.safeEquals(getProperty("create_dir"), "true")) {
				final String directoryName = saveFile.getAbsolutePath() + "_files";
				success = createDirectory(directoryName);
				if (success) {
					final String files = getProperty("files_to_copy");
					final String filePrefix = getProperty("file_prefix");
					copyFilesFromResourcesToDirectory(directoryName, files, filePrefix);
				}
				if (success && Tools.safeEquals(getProperty("copy_icons"), "true")) {
					success = copyIcons(directoryName);
				}
				if (success && Tools.safeEquals(getProperty("copy_map"), "true")) {
					success = copyMap(directoryName);
				}
				if (success && create_image) {
					createImageFromMap(directoryName);
				}
			}
			if (!success) {
				JOptionPane.showMessageDialog(null, getProperty("error_creating_directory"),
				    "Freemind", JOptionPane.ERROR_MESSAGE);
				return;
			}
			if (Tools.safeEquals(getProperty("load_file"), "true")) {
				Controller.getController().getViewController().openDocument(
				    UrlManager.fileToUrl(saveFile));
			}
		}
		catch (final Exception e) {
			org.freeplane.core.util.Tools.logException(e);
			mTransformResultWithoutError = false;
		}
	}

	public boolean transform(final Source xmlSource, final InputStream xsltStream,
	                         final File resultFile, final String areaCode) {
		final Source xsltSource = new StreamSource(xsltStream);
		final Result result = new StreamResult(resultFile);
		try {
			final TransformerFactory transFact = TransformerFactory.newInstance();
			final Transformer trans = transFact.newTransformer(xsltSource);
			trans.setParameter("destination_dir", resultFile.getName() + "_files/");
			trans.setParameter("area_code", areaCode);
			trans.setParameter("folding_type", Controller.getResourceController().getProperty(
			    "html_export_folding"));
			trans.transform(xmlSource, result);
		}
		catch (final Exception e) {
			org.freeplane.core.util.Tools.logException(e);
			return false;
		};
		return true;
	}

	/**
	 * @throws IOException
	 */
	private boolean transformMapWithXslt(final String xsltFileName, final File saveFile,
	                                     final String areaCode) throws IOException {
		final String map = getMapXml();
		final StringReader reader = new StringReader(map);
		final URL xsltUrl = Controller.getResourceController().getResource(xsltFileName);
		if (xsltUrl == null) {
			Logger.global.severe("Can't find " + xsltFileName + " as resource.");
			throw new IllegalArgumentException("Can't find " + xsltFileName + " as resource.");
		}
		final InputStream xsltFile = xsltUrl.openStream();
		return transform(new StreamSource(reader), xsltFile, saveFile, areaCode);
	}
}
