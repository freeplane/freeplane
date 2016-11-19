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
package org.freeplane.features.export.mindmapmode;

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
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.ListModel;
import javax.swing.filechooser.FileFilter;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.lang.StringUtils;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.ExampleFileFilter;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.Compat;
import org.freeplane.core.util.FileUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.icon.UIIcon;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.MapWriter.Mode;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.url.UrlManager;

/**
 * @author foltin To change the template for this generated type comment go to
 *         Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class ExportWithXSLT implements IExportEngine {

	private static final Pattern propertyReferenceEpression = Pattern.compile("\\$\\{[^}]+\\}");

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

	/**
	 * For test purposes. True=no error
	 */
	private boolean mTransformResultWithoutError = false;
	final private Properties properties;
	private final String name;

	public ExportWithXSLT(final String name, final Properties properties) {
		this.name = name;
		this.properties = properties;
	}

	/**
	 */
	private void copyFilesFromResourcesToDirectory(final String targetDirectoryName, final String sourceDirectoryPath,
	                                               final String files) {
		final StringTokenizer tokenizer = new StringTokenizer(files, ",");
		final File destinationDirectory = new File(targetDirectoryName);
		while (tokenizer.hasMoreTokens()) {
			final String sourceFile = tokenizer.nextToken();
			int nameStartPosition = sourceFile.lastIndexOf('/') + 1;
			String sourceFileDirectory = nameStartPosition > 0 ? sourceFile.substring(0, nameStartPosition) : "";
			String sourceFileName = nameStartPosition > 0 ? sourceFile.substring(nameStartPosition) : sourceFile;
			FileUtils.copyFromResource(sourceDirectoryPath + sourceFileDirectory, sourceFileName, destinationDirectory);
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

	private boolean copyMap(final MapModel map, final String pDirectoryName, final Mode mode) {
		boolean success = true;
		try {
			final BufferedWriter fileout = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
			    pDirectoryName + File.separator + "map" + UrlManager.FREEPLANE_FILE_EXTENSION)));
			Controller.getCurrentModeController().getMapController().getFilteredXml(map, fileout, mode, Mode.EXPORT.equals(mode));
		}
		catch (final IOException e) {
			success = false;
		}
		return success;
	}

	/**
	 * @param map
	 */
	private boolean createImageFromMap(MapModel map, final String directoryName) {
		if (Controller.getCurrentController().getMapViewManager().getMapViewComponent() == null) {
			return false;
		}
		final RenderedImage image = new ImageCreator(Math.round(UITools.FONT_SCALE_FACTOR * 72)).createBufferedImage(map);
		if(image == null){
			return false;
		}
		try {
			final FileOutputStream out = new FileOutputStream(directoryName + File.separator + "image.png");
			ImageIO.write(image, "png", out);
			out.close();
			return true;
		}
		catch (final IOException e1) {
			LogUtils.severe(e1);
			return false;
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
		modeController.getMapController().getFilteredXml(map, writer, mode, Mode.EXPORT.equals(mode));
		return writer.getBuffer().toString();
	}

	String getProperty(final String key) {
		final String property = getProperty(key, null);
		if (property == null)
	        return property;
		Matcher r = propertyReferenceEpression.matcher(property);
		r.reset();
		boolean result = r.find();
		if (result) {
		    StringBuffer sb = new StringBuffer();
		    do {
		        String propertyReference = r.group();
		        String propertyName = propertyReference.substring(2, propertyReference.length() - 1);
				r.appendReplacement(sb, System.getProperty(propertyName, propertyReference));
		        result = r.find();
		    } while (result);
		    r.appendTail(sb);
		    return sb.toString();
		}
		return property;
	}

	String getProperty(final String key, final String value) {
		return properties.getProperty(key, value);
	}

	public boolean isTransformResultWithoutError() {
		return mTransformResultWithoutError;
	}

	/**
	 * @param saveFile
	 */
	public void export(final MapModel map, final File saveFile) {
		try {
			mTransformResultWithoutError = true;
			final boolean create_image = StringUtils.equals(getProperty("create_html_linked_image"), "true");
			final String areaCode = getAreaCode(create_image);
			final String xsltFileName = getProperty("xslt_file");
			final Mode mode = Mode.valueOf(getProperty("mode", Mode.EXPORT.name()));
			String[] parameters = getProperty("set_properties", "").split(",\\s*");
			boolean success = transformMapWithXslt(xsltFileName, saveFile, areaCode, mode, parameters);
			if (!success) {
				JOptionPane.showMessageDialog(UITools.getCurrentRootComponent(), getProperty("error_applying_template"), "Freeplane",
				    JOptionPane.ERROR_MESSAGE);
				return;
			}
			if (success && StringUtils.equals(getProperty("create_dir"), "true")) {
				final String directoryName = saveFile.getAbsolutePath() + "_files";
				success = FileUtils.createDirectory(directoryName);
				if (success) {
					final String files = getProperty("files_to_copy");
					final String filePrefix = getProperty("file_prefix");
					copyFilesFromResourcesToDirectory(directoryName, filePrefix, files);
				}
				if (success && StringUtils.equals(getProperty("copy_icons"), "true")) {
					success = copyIcons(map, directoryName);
				}
				if (success && StringUtils.equals(getProperty("copy_map"), "true")) {
	                String copyМapХsltFile = getProperty("copy_map_xslt_file");
	                final Mode copymode = Mode.valueOf(getProperty("copymode", Mode.EXPORT.name()));
					if (copyМapХsltFile != null){
	                    success = transformMapWithXslt(copyМapХsltFile, new File(directoryName, "map.mm"), "", copymode, new String[]{});
					} else {
						success = copyMap(map, directoryName, copymode);
					}
				}
				if (success && create_image) {
					success = createImageFromMap(map, directoryName);
				}
			}
			if (!success) {
				JOptionPane.showMessageDialog(UITools.getCurrentRootComponent(), getProperty("error_creating_directory"), "Freeplane",
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

	private boolean transformMapWithXslt(final String xsltFileName, final File saveFile, final String areaCode,
                                         final Mode mode, String[] parameters) throws IOException,
            TransformerFactoryConfigurationError {
	    final String map = getMapXml(mode);
		final StringReader reader = new StringReader(map);
		ResourceController resourceController = ResourceController.getResourceController();
		final URL xsltUrl = resourceController.getResource(xsltFileName);
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
			final String fileName = saveFile.getName();
			final String fileNameEncoded = toRelativeUri(fileName);
			trans.setParameter("file_name", fileNameEncoded);
			trans.setParameter("destination_dir", fileNameEncoded + "_files/");
			trans.setParameter("area_code", areaCode);
			trans.setParameter("folding_type", resourceController.getProperty(
			"html_export_folding"));
			StringBuilder sb = new StringBuilder();
			for(String p : parameters){
				String value = resourceController.getProperty(p, null);
				if(value != null && ! value.equals(resourceController.getDefaultProperty(p))){
					sb.append(p);
					sb.append('=');
					sb.append(value);
					sb.append("$$$");
				}

			}
			trans.setParameter("propertyList", sb.toString());
			trans.transform(new StreamSource(reader), result);
		}
		catch (final Exception e) {
			LogUtils.warn(e);
			return false;
		}
		finally {
			FileUtils.silentlyClose(xsltFile);
		}
		return true;
    }

	private String toRelativeUri(final String fileName) throws URISyntaxException {
		return new URI(null, null, fileName, null).toString();
	}

	public FileFilter getFileFilter() {
		return new ExampleFileFilter(getProperty("file_type"), TextUtils.getText(name + ".text"));
    }
}
