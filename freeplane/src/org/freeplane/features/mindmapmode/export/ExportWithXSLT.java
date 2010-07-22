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
package org.freeplane.features.mindmapmode.export;

import java.awt.event.ActionEvent;
import java.awt.image.RenderedImage;
import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.ListModel;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.lang.StringUtils;
import org.freeplane.core.controller.Controller;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.Compat;
import org.freeplane.core.util.FileUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.common.icon.UIIcon;
import org.freeplane.features.common.map.MapModel;
import org.freeplane.features.common.map.ModeController;
import org.freeplane.features.common.map.MapWriter.Mode;
import org.freeplane.features.common.url.UrlManager;
import org.freeplane.n3.nanoxml.IXMLParser;
import org.freeplane.n3.nanoxml.IXMLReader;
import org.freeplane.n3.nanoxml.StdXMLReader;
import org.freeplane.n3.nanoxml.XMLElement;
import org.freeplane.n3.nanoxml.XMLParserFactory;

/**
 * @author foltin To change the template for this generated type comment go to
 *         Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class ExportWithXSLT extends ExportAction {
//	private static final IconStore STORE = IconStoreFactory.create();
	private static final String NAME_EXTENSION_PROPERTY = "name_extension";
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @param map 
	 */
	static void copyIconsToDirectory(final MapModel map, final String directoryName) {
		final ListModel icons = map.getIconRegistry().getIconsAsListModel();
		for (int i = 0; i < icons.getSize(); i++) {
			final UIIcon icon = (UIIcon) icons.getElementAt(i);
			final String iconName = icon.getName();
			final StringBuilder sb = new StringBuilder(directoryName);
			final int lastIndexOfSeparator = iconName.lastIndexOf('/');
			if (lastIndexOfSeparator != -1) {
				sb.append(File.separatorChar);
				sb.append(iconName.substring(0, lastIndexOfSeparator));
			}
			final File destinationDirectory = new File(sb.toString());
			destinationDirectory.mkdirs();
			FileUtils.copyFromURL(icon.getUrl(), destinationDirectory);
		}
	}

	public static void createXSLTExportActions( final String xmlDescriptorFile) {
		try {
			final ModeController modeController = Controller.getCurrentModeController();
			final MenuBuilder menuBuilder = modeController.getUserInputListenerFactory().getMenuBuilder();
			{
				final ExportToHTMLAction e1 = new ExportToHTMLAction();
				modeController.addAction(e1);
				menuBuilder.addAnnotatedAction(e1);
				final ExportBranchToHTMLAction e2 = new ExportBranchToHTMLAction();
				modeController.addAction(e2);
				menuBuilder.addAnnotatedAction(e2);
				final ExportWithXSLTDialogAction action = new ExportWithXSLTDialogAction();
				modeController.addAction(action);
				menuBuilder.addAction("/menu_bar/file/export/dialog", action, MenuBuilder.AS_CHILD);
			}
			final IXMLParser parser = XMLParserFactory.createDefaultXMLParser();
			final URL resource = ResourceController.getResourceController().getResource(xmlDescriptorFile);
			final IXMLReader reader = new StdXMLReader(resource.openStream());
			parser.setReader(reader);
			final XMLElement xml = (XMLElement) parser.parse();
			final Enumeration<XMLElement> actionDescriptors = xml.enumerateChildren();
			while (actionDescriptors.hasMoreElements()) {
				final XMLElement descriptor = actionDescriptors.nextElement();
				final String name = descriptor.getAttribute("name", null);
				final String location = descriptor.getAttribute("location", null);
				final XMLElement xmlProperties = descriptor.getFirstChildNamed("properties");
				final Properties properties = xmlProperties.getAttributes();
				final ExportWithXSLT action = new ExportWithXSLT(name, properties);
				modeController.addAction(action);
				menuBuilder.addAction(location, action, MenuBuilder.AS_CHILD);
			}
		}
		catch (final Exception e) {
			LogUtils.severe(e);
		}
	}

	/**
	 * For test purposes. True=no error
	 */
	private boolean mTransformResultWithoutError = false;
	final private Properties properties;

	public ExportWithXSLT(final String name, final Properties properties) {
		super(name);
		this.properties = properties;
	}

	public void actionPerformed(final ActionEvent e) {
		final File saveFile = chooseFile();
		if (saveFile == null) {
			return;
		}
		transform(saveFile);
	}

	protected File chooseFile() {
		final String nameExtension = getProperty(ExportWithXSLT.NAME_EXTENSION_PROPERTY);
		return chooseFile(getProperty("file_type"), getTranslatableResourceString("file_description"), nameExtension);
	}

	/**
	 */
	private void copyFilesFromResourcesToDirectory(final String directoryName, final String files,
	                                               final String filePrefix) {
		final StringTokenizer tokenizer = new StringTokenizer(files, ",");
		final File destinationDirectory = new File(directoryName);
		while (tokenizer.hasMoreTokens()) {
			final String next = tokenizer.nextToken();
			FileUtils.copyFromResource(filePrefix, next, destinationDirectory);
		}
	}

	/**
	 * @param map 
	 */
	private boolean copyIcons(final MapModel map, final String directoryName) {
		boolean success;
		final String iconDirectoryName = directoryName + File.separatorChar + "icons";
		success = FileUtils.createDirectory(iconDirectoryName);
		if (success) {
			ExportWithXSLT.copyIconsToDirectory(map, iconDirectoryName);
		}
		return success;
	}

	private boolean copyMap(final MapModel map, final String pDirectoryName) {
		boolean success = true;
		try {
			final BufferedWriter fileout = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
			    pDirectoryName + File.separator + "map" + UrlManager.FREEPLANE_FILE_EXTENSION)));
			Controller.getCurrentModeController().getMapController().getFilteredXml(map, fileout, Mode.EXPORT, true);
		}
		catch (final IOException e) {
			success = false;
		}
		return success;
	}

	/**
	 */
	private void createImageFromMap(final String directoryName) {
		if (Controller.getCurrentController().getViewController().getMapView() == null) {
			return;
		}
		final RenderedImage image = createBufferedImage();
		try {
			final FileOutputStream out = new FileOutputStream(directoryName + File.separator + "image.png");
			ImageIO.write(image, "png", out);
			out.close();
		}
		catch (final IOException e1) {
			LogUtils.severe(e1);
		}
	}

	/**
	 * @param create_image
	 */
	private String getAreaCode(final boolean create_image) {
		String areaCode = "";
		if (create_image) {
			areaCode = Controller.getCurrentController().getMapViewManager().createHtmlMap();
		}
		return areaCode;
	}

	/**
	 * @param mode 
	 * @throws IOException
	 */
	private String getMapXml(final Mode mode) throws IOException {
		final StringWriter writer = new StringWriter();
		final ModeController modeController = Controller.getCurrentModeController();
		final Controller controller = modeController.getController();
		final MapModel map = controller.getMap();
		modeController.getMapController().getFilteredXml(map, writer, mode, true);
		return writer.getBuffer().toString();
	}

	String getProperty(final String key) {
		final String property = getProperty(key, null);
		if (property == null || !property.startsWith("$")) {
			return property;
		}
		return System.getProperty(property.substring(1), null);
	}

	String getProperty(final String key, final String value) {
		return properties.getProperty(key, value);
	}

	private String getTranslatableResourceString(final String resourceName) {
		final String returnValue = getProperty(resourceName);
		if (returnValue != null && returnValue.startsWith("%")) {
			return TextUtils.getText(returnValue.substring(1));
		}
		return returnValue;
	}

	public boolean isTransformResultWithoutError() {
		return mTransformResultWithoutError;
	}

	/**
	 * @param saveFile
	 */
	protected void transform(final File saveFile) {
		try {
			mTransformResultWithoutError = true;
			final boolean create_image = StringUtils.equals(getProperty("create_html_linked_image"), "true");
			final MapModel map = Controller.getCurrentController().getMap();
			final String areaCode = getAreaCode(create_image);
			final String xsltFileName = getProperty("xslt_file");
			boolean success = transformMapWithXslt(xsltFileName, saveFile, areaCode);
			if (!success) {
				JOptionPane.showMessageDialog(UITools.getFrame(), getProperty("error_applying_template"), "Freeplane",
				    JOptionPane.ERROR_MESSAGE);
				return;
			}
			if (success && StringUtils.equals(getProperty("create_dir"), "true")) {
				final String directoryName = saveFile.getAbsolutePath() + "_files";
				success = FileUtils.createDirectory(directoryName);
				if (success) {
					final String files = getProperty("files_to_copy");
					final String filePrefix = getProperty("file_prefix");
					copyFilesFromResourcesToDirectory(directoryName, files, filePrefix);
				}
				if (success && StringUtils.equals(getProperty("copy_icons"), "true")) {
					success = copyIcons(map, directoryName);
				}
				if (success && StringUtils.equals(getProperty("copy_map"), "true")) {
					success = copyMap(map, directoryName);
				}
				if (success && create_image) {
					createImageFromMap(directoryName);
				}
			}
			if (!success) {
				JOptionPane.showMessageDialog(UITools.getFrame(), getProperty("error_creating_directory"), "Freeplane",
				    JOptionPane.ERROR_MESSAGE);
				return;
			}
			if (StringUtils.equals(getProperty("load_file"), "true")) {
				Controller.getCurrentController().getViewController().openDocument(Compat.fileToUrl(saveFile));
			}
		}
		catch (final Exception e) {
			LogUtils.severe(e);
			mTransformResultWithoutError = false;
		}
	}

	/**
	 * @throws IOException
	 */
	private boolean transformMapWithXslt(final String xsltFileName, final File saveFile, final String areaCode)
	        throws IOException {
		final Mode mode = Mode.valueOf(getProperty("mode", Mode.EXPORT.name()));
		final String map = getMapXml(mode);
		final StringReader reader = new StringReader(map);
		final URL xsltUrl = ResourceController.getResourceController().getResource(xsltFileName);
		if (xsltUrl == null) {
			LogUtils.severe("Can't find " + xsltFileName + " as resource.");
			throw new IllegalArgumentException("Can't find " + xsltFileName + " as resource.");
		}
		final InputStream xsltFile = new BufferedInputStream(xsltUrl.openStream());
		final Source xsltSource = new StreamSource(xsltFile);
		final Result result = new StreamResult(saveFile);
		try {
			final TransformerFactory transFact = TransformerFactory.newInstance();
			final Transformer trans = transFact.newTransformer(xsltSource);
			trans.setParameter("destination_dir", saveFile.getName() + "_files/");
			trans.setParameter("area_code", areaCode);
			trans.setParameter("folding_type", ResourceController.getResourceController().getProperty(
			    "html_export_folding"));
			trans.transform(new StreamSource(reader), result);
		}
		catch (final Exception e) {
			LogUtils.warn(e);
			return false;
		};
		return true;
	}
}
