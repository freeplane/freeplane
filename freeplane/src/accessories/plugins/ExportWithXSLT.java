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
package accessories.plugins;

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

import org.freeplane.controller.Controller;
import org.freeplane.main.Tools;
import org.freeplane.map.icon.MindIcon;
import org.freeplane.map.tree.MapModel;
import org.freeplane.map.tree.NodeModel;
import org.freeplane.modes.ModeController;
import org.freeplane.modes.mindmapmode.MModeController;

import accessories.plugins.util.html.ClickableImageCreator;
import accessories.plugins.util.xslt.ExportDialog;
import deprecated.freemind.extensions.ExportHook;

/**
 * @author foltin To change the template for this generated type comment go to
 *         Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class ExportWithXSLT extends ExportHook {
	private static final String NAME_EXTENSION_PROPERTY = "name_extension";
	/**
	 * For test purposes. True=no error
	 */
	private boolean mTransformResultWithoutError = false;

	/**
	 *
	 */
	public ExportWithXSLT() {
		super();
	}

	protected File chooseFile() {
		String nameExtension = null;
		if (getProperties().containsKey(ExportWithXSLT.NAME_EXTENSION_PROPERTY)) {
			nameExtension = getResourceString(ExportWithXSLT.NAME_EXTENSION_PROPERTY);
		}
		return chooseFile(getResourceString("file_type"),
		    getTranslatableResourceString("file_description"), nameExtension);
	}

	/**
	 */
	private void copyFilesFromResourcesToDirectory(final String directoryName,
	                                               final String files,
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
		final String iconDirectoryName = directoryName + File.separatorChar
		        + "icons";
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
			copyFromResource(MindIcon.getIconsPath(), myIcon
			    .getIconBaseFileName(), directoryName2);
		}
		final File iconDir = new File(Controller.getResourceController()
		    .getFreemindUserDirectory(), "icons");
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
				copyFromFile(iconDir.getAbsolutePath(), iconName,
				    directoryName2);
			}
		}
	}

	private boolean copyMap(final String pDirectoryName) throws IOException {
		final boolean success = true;
		final BufferedWriter fileout = new BufferedWriter(
		    new OutputStreamWriter(new FileOutputStream(pDirectoryName
		            + File.separator + "map.mm")));
		final MModeController controller = (MModeController) getController();
		final MapModel map = Controller.getController().getMap();
		controller.getFilteredXml(map, fileout);
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
			final FileOutputStream out = new FileOutputStream(directoryName
			        + File.separator + "image.png");
			ImageIO.write(image, "png", out);
			out.close();
		}
		catch (final IOException e1) {
			org.freeplane.main.Tools.logException(e1);
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
			final NodeModel root = Controller.getController().getMap()
			    .getRootNode();
			final ClickableImageCreator creator = new ClickableImageCreator(
			    root, getController(),
			    getResourceString("link_replacement_regexp"));
			areaCode = creator.generateHtml();
		}
		return areaCode;
	}

	/**
	 * @throws IOException
	 */
	private StringWriter getMapXml() throws IOException {
		final StringWriter writer = new StringWriter();
		final MModeController controller = (MModeController) getController();
		final MapModel map = Controller.getController().getMap();
		controller.getFilteredXml(map, writer);
		return writer;
	}

	private String getTranslatableResourceString(final String resourceName) {
		final String returnValue = getResourceString(resourceName);
		if (returnValue != null && returnValue.startsWith("%")) {
			return getController().getText(returnValue.substring(1));
		}
		return returnValue;
	}

	public boolean isTransformResultWithoutError() {
		return mTransformResultWithoutError;
	}

	/*
	 * (non-Javadoc)
	 * @see freemind.extensions.MindMapHook#startupMapHook()
	 */
	@Override
	public void startup() {
		super.startup();
		final ModeController mc = getController();
		final MapModel model = Controller.getController().getMap();
		if (Tools.safeEquals(getResourceString("file_type"), "user")) {
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

	/**
	 * @param saveFile
	 */
	public void transform(final File saveFile) {
		try {
			mTransformResultWithoutError = true;
			final boolean create_image = Tools.safeEquals(
			    getResourceString("create_html_linked_image"), "true");
			final String areaCode = getAreaCode(create_image);
			final String xsltFileName = getResourceString("xslt_file");
			boolean success = transformMapWithXslt(xsltFileName, saveFile,
			    areaCode);
			if (!success) {
				JOptionPane.showMessageDialog(null,
				    getResourceString("error_applying_template"), "Freemind",
				    JOptionPane.ERROR_MESSAGE);
				return;
			}
			if (success
			        && Tools
			            .safeEquals(getResourceString("create_dir"), "true")) {
				final String directoryName = saveFile.getAbsolutePath()
				        + "_files";
				success = createDirectory(directoryName);
				if (success) {
					final String files = getResourceString("files_to_copy");
					final String filePrefix = getResourceString("file_prefix");
					copyFilesFromResourcesToDirectory(directoryName, files,
					    filePrefix);
				}
				if (success
				        && Tools.safeEquals(getResourceString("copy_icons"),
				            "true")) {
					success = copyIcons(directoryName);
				}
				if (success
				        && Tools.safeEquals(getResourceString("copy_map"),
				            "true")) {
					success = copyMap(directoryName);
				}
				if (success && create_image) {
					createImageFromMap(directoryName);
				}
			}
			if (!success) {
				JOptionPane.showMessageDialog(null,
				    getResourceString("error_creating_directory"), "Freemind",
				    JOptionPane.ERROR_MESSAGE);
				return;
			}
			if (Tools.safeEquals(getResourceString("load_file"), "true")) {
				Controller.getController().getViewController().openDocument(
				    Tools.fileToUrl(saveFile));
			}
		}
		catch (final Exception e) {
			org.freeplane.main.Tools.logException(e);
			mTransformResultWithoutError = false;
		}
	}

	public boolean transform(final Source xmlSource,
	                         final InputStream xsltStream,
	                         final File resultFile, final String areaCode) {
		final Source xsltSource = new StreamSource(xsltStream);
		final Result result = new StreamResult(resultFile);
		try {
			final TransformerFactory transFact = TransformerFactory
			    .newInstance();
			final Transformer trans = transFact.newTransformer(xsltSource);
			trans.setParameter("destination_dir", resultFile.getName()
			        + "_files/");
			trans.setParameter("area_code", areaCode);
			trans.setParameter("folding_type", Controller
			    .getResourceController().getProperty("html_export_folding"));
			trans.transform(xmlSource, result);
		}
		catch (final Exception e) {
			org.freeplane.main.Tools.logException(e);
			return false;
		};
		return true;
	}

	/**
	 * @throws IOException
	 */
	private boolean transformMapWithXslt(final String xsltFileName,
	                                     final File saveFile,
	                                     final String areaCode)
	        throws IOException {
		final StringWriter writer = getMapXml();
		final StringReader reader = new StringReader(writer.getBuffer()
		    .toString());
		final URL xsltUrl = getResource(xsltFileName);
		if (xsltUrl == null) {
			Logger.global
			    .severe("Can't find " + xsltFileName + " as resource.");
			throw new IllegalArgumentException("Can't find " + xsltFileName
			        + " as resource.");
		}
		final InputStream xsltFile = xsltUrl.openStream();
		return transform(new StreamSource(reader), xsltFile, saveFile, areaCode);
	}
}
