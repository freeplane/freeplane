/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is created by Dimitry Polivaev in 2008.
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
package org.freeplane.features.url;

import static java.util.Arrays.asList;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.Compat;
import org.freeplane.core.util.FileUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.MapWriter.Mode;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mapio.MapIO;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.n3.nanoxml.XMLException;
import org.freeplane.n3.nanoxml.XMLParseException;

/**
 * @author Dimitry Polivaev
 */
public class UrlManager implements IExtension {
	public static final String FREEPLANE_FILE_EXTENSION_WITHOUT_DOT = "mm";
	public static final String FREEPLANE_FILE_EXTENSION = "." + FREEPLANE_FILE_EXTENSION_WITHOUT_DOT;
	public static final String FREEPLANE_ADD_ON_FILE_EXTENSION = ".addon." + FREEPLANE_FILE_EXTENSION_WITHOUT_DOT;
	private static File lastCurrentDir = null;
	public static final String MAP_URL = "map_url";

	public static UrlManager getController() {
		final ModeController modeController = Controller.getCurrentModeController();
		return modeController.getExtension(UrlManager.class);
	}

	public static void install( final UrlManager urlManager) {
		final ModeController modeController = Controller.getCurrentModeController();
		modeController.addExtension(UrlManager.class, urlManager);
		urlManager.init();
	}

// // 	final private Controller controller;
// 	final private ModeController modeController;

	public UrlManager() {
		super();
	}

	protected void init() {
//		this.modeController = modeController;
//		controller = modeController.getController();
		createActions();
	}

	/**
	 *
	 */
	private void createActions() {
	}

	public JFileChooser getFileChooser(final FileFilter filter, boolean useDirectorySelector) {
		return getFileChooser(filter, useDirectorySelector, false);
	}

	/**
	 * Creates a file chooser with the last selected directory as default.
	 * @param useDirectorySelector
	 */
	@SuppressWarnings("serial")
    public JFileChooser getFileChooser(final FileFilter filter, boolean useDirectorySelector, boolean showHiddenFiles) {
		final File parentFile = getMapsParentFile(Controller.getCurrentController().getMap());
		if (parentFile != null && getLastCurrentDir() == null) {
			setLastCurrentDir(parentFile);
		}
		final JFileChooser chooser = new JFileChooser(){
 			@Override
            protected JDialog createDialog(Component parent) throws HeadlessException {
 				final JDialog dialog = super.createDialog(parent);
	            final JComponent selector = createDirectorySelector(this);

	            //Close dialog when escape is pressed
	            InputMap in = dialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
	            in.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0), "escape");
	            ActionMap aMap = dialog.getRootPane().getActionMap();
	            aMap.put("escape", new AbstractAction()
	            		{
	            		public void actionPerformed (ActionEvent e)
	            		{
	            			dialog.dispose();
	            		}
	            });
	            if(selector != null){
	            	dialog.getContentPane().add(selector, BorderLayout.NORTH);
	            	dialog.pack();
	            }

				return dialog;
            }

		};
		if (getLastCurrentDir() != null) {
			chooser.setCurrentDirectory(getLastCurrentDir());
		}
		if (showHiddenFiles) {
			chooser.setFileHidingEnabled(false);
		}
		if (filter != null) {
			chooser.addChoosableFileFilter(filter);
			chooser.setFileFilter(filter);
		}
		return chooser;
	}

	protected JComponent createDirectorySelector(JFileChooser chooser) {
        return null;
    }
	public File getLastCurrentDir() {
		return lastCurrentDir;
	}

	protected File getMapsParentFile(final MapModel map) {
		if ((map != null) && (map.getFile() != null) && (map.getFile().getParentFile() != null)) {
			return map.getFile().getParentFile();
		}
		return null;
	}

	public void handleLoadingException(final Exception ex) {
		final String exceptionType = ex.getClass().getName();
		if (exceptionType.equals(XMLParseException.class.getName())) {
			final int showDetail = JOptionPane.showConfirmDialog(Controller.getCurrentController().getMapViewManager().getMapViewComponent(),
			    TextUtils.getText("map_corrupted"), "Freeplane", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
			if (showDetail == JOptionPane.YES_OPTION) {
				UITools.errorMessage(ex);
			}
		}
		else if (exceptionType.equals(FileNotFoundException.class.getName())) {
			UITools.errorMessage(ex.getMessage());
		}
		else if (exceptionType.equals("org.freeplane.features.url.mindmapmode.SkipException")) {
			return;
		}
		else {
			LogUtils.severe(ex);
			UITools.errorMessage(ex);
		}
	}

	/**@deprecated -- use {@link MapIO#loadCatchExceptions(URL url, MapModel map)} */
	@Deprecated
	public boolean loadCatchExceptions(final URL url, final MapModel map){
		InputStreamReader urlStreamReader = null;
		try {
			urlStreamReader = load(url, map);
			return true;
		}
		catch (final XMLException ex) {
			LogUtils.warn(ex);
		}
		catch (final IOException ex) {
			LogUtils.warn(ex);
		}
		catch (final RuntimeException ex) {
			LogUtils.severe(ex);
		}
		finally {
			FileUtils.silentlyClose(urlStreamReader);
		}
		UITools.errorMessage(TextUtils.format("url_open_error", url.toString()));
		return false;
	}

	/**@deprecated -- use {@link MapIO#load(URL url, MapModel map)} */
	@Deprecated
	public InputStreamReader load(final URL url, final MapModel map)
			throws IOException, XMLException {
		InputStreamReader urlStreamReader;
		setURL(map, url);
		urlStreamReader = new InputStreamReader(url.openStream());
		final ModeController modeController = Controller.getCurrentModeController();
		modeController.getMapController().getMapReader().createNodeTreeFromXml(map, urlStreamReader, Mode.FILE);
		return urlStreamReader;
	}

    /**@deprecated -- use {@link MapIO#load(URL url, MapModel map)} */
    @Deprecated
    public boolean loadImpl(final URL url, final MapModel map){
        return loadCatchExceptions(url, map);
    }

    /**@deprecated -- use LinkController*/
	@Deprecated
	public void loadURL(URI uri) {
		final String uriString = uri.toString();
		if (uriString.startsWith("#")) {
			final String target = uri.getFragment();
			try {
				final ModeController modeController = Controller.getCurrentModeController();
				final MapController mapController = modeController.getMapController();
				final NodeModel node = mapController.getNodeFromID(target);
				if (node != null) {
					mapController.select(node);
				}
			}
			catch (final Exception e) {
				LogUtils.warn("link " + target + " not found", e);
				UITools.errorMessage(TextUtils.format("link_not_found", target));
			}
			return;
		}
		try {
			final String extension = FileUtils.getExtension(uri.getRawPath());
			if(! uri.isAbsolute()){
				URI absoluteUri = getAbsoluteUri(uri);
				if (absoluteUri == null) {
					final MapModel map = Controller.getCurrentController().getMap();
					if (map.getURL() == null)
						UITools.errorMessage(TextUtils.getText("map_not_saved"));
					else
						UITools.errorMessage(TextUtils.format("link_not_found", String.valueOf(uri)));
					return;
				}
				uri = absoluteUri;
			}
			//DOCEAR: mindmaps can be linked in a mindmap --> therefore project-relative-paths are possible
			if(! asList("file", "smb").contains(uri.getScheme())) {
				try {
					uri = uri.toURL().openConnection().getURL().toURI().normalize();
				}
				catch (Exception e) {
					LogUtils.warn("link " + uri + " not found", e);
					UITools.errorMessage(TextUtils.format("link_not_found", uri.toString()));
				}
			}
			try {
				if ((extension != null)
				        && extension.equals(UrlManager.FREEPLANE_FILE_EXTENSION_WITHOUT_DOT)) {
					final URL url = new URL(uri.getScheme(), uri.getHost(), uri.getPath());
					final ModeController modeController = Controller.getCurrentModeController();
					modeController.getMapController().newMap(url);
					final String ref = uri.getFragment();
					if (ref != null) {
						final ModeController newModeController = Controller.getCurrentModeController();
						final MapController newMapController = newModeController.getMapController();
						newMapController.select(newMapController.getNodeFromID(ref));
					}
					return;
				}
				Controller.getCurrentController().getViewController().openDocument(uri);
			}
			catch (final Exception e) {
				LogUtils.warn("link " + uri + " not found", e);
				UITools.errorMessage(TextUtils.format("link_not_found", uri.toString()));
			}
			return;
		}
		catch (final MalformedURLException ex) {
			LogUtils.warn("URL " + uriString + " not found", ex);
			UITools.errorMessage(TextUtils.format("link_not_found", uriString));
		}
	}

	private URI getAbsoluteUri(final URI uri) throws MalformedURLException {
		if (uri.isAbsolute()) {
			return uri;
		}
		final MapModel map = Controller.getCurrentController().getMap();
		return getAbsoluteUri(map, uri);
	}


	public URI getAbsoluteUri(final MapModel map, final URI uri) throws MalformedURLException {


		//DOCEAR - added project relative uri resolution
		URI resolvedURI;
		try {
			resolvedURI = uri.toURL().openConnection().getURL().toURI();
		} catch (IOException ex) {
			LogUtils.severe(ex);
			return null;
		} catch (URISyntaxException ex) {
			LogUtils.severe(ex);
			return null;
		} catch (IllegalArgumentException ex) {
			resolvedURI = uri;
		}

		if (resolvedURI.isAbsolute()) {
			return resolvedURI;
		}
		final String path = resolvedURI.getPath();
		try {
			URL context = map.getURL();
			if(context == null)
				return null;
			final URL url = new URL(context, path);
			return new URI(url.getProtocol(), url.getHost(), url.getPath(), uri.getQuery(), uri.getFragment());
		}
		catch (final URISyntaxException e) {
			LogUtils.severe(e);
			return null;
		}
	}

	public File getAbsoluteFile(final MapModel map, final URI uri) {
		if(uri == null) {
			return null;
		}
		try {
			URLConnection urlConnection;
			// windows drive letters are interpreted as uri schemes -> make a file from the scheme-less uri string and use this to resolve the path
			if(Compat.isWindowsOS() && (uri.getScheme() != null && uri.getScheme().length() == 1)) {
				urlConnection = (new File(uri.toString())).toURI().toURL().openConnection();
			}
			else if(uri.getScheme() == null && !uri.getPath().startsWith(File.separator)) {
				if(map != null) {
					urlConnection = (new File(uri.toString())).toURI().toURL().openConnection();
				}
				else {
					urlConnection = UrlManager.getController().getAbsoluteUri(map, uri).toURL().openConnection();
				}
			}
			else {
				urlConnection = uri.toURL().openConnection();
			}

			if (urlConnection == null) {
				return null;
			}
			else {
				URI absoluteUri = urlConnection.getURL().toURI().normalize();
				if("file".equalsIgnoreCase(absoluteUri.getScheme())){
					return new File(absoluteUri);
				}
			}
		}
		catch (URISyntaxException e) {
			LogUtils.warn(e);
		}
		catch (IOException e) {
			LogUtils.warn(e);
		}
		catch (Exception e){
			LogUtils.warn(e);
		}
		return null;

	}

	public URL getAbsoluteUrl(final MapModel map, final URI uri) throws MalformedURLException {
		final String path = uri.isOpaque() ? uri.getSchemeSpecificPart() : uri.getPath();
		final StringBuilder sb = new StringBuilder(path);
		final String query = uri.getQuery();
		if (query != null) {
			sb.append('?');
			sb.append(query);
		}
		final String fragment = uri.getFragment();
		if (fragment != null) {
			sb.append('#');
			sb.append(fragment);
		}
		if (!uri.isAbsolute() || uri.isOpaque() || uri.getScheme().length()>0) {
			final URL mapUrl = map.getURL();
			final String scheme = uri.getScheme();
			if (scheme == null || mapUrl.getProtocol().equals(scheme)) {
				final URL url = new URL(mapUrl, sb.toString());
				return url;
			}
		}
		final URL url = new URL(uri.getScheme(), uri.getHost(), uri.getPort(), sb.toString());
		return url;
	}

	public URL getAbsoluteUrl(final URI base, final URI uri) throws MalformedURLException {
		final String path = uri.isOpaque() ? uri.getSchemeSpecificPart() : uri.getPath();
		final StringBuilder sb = new StringBuilder(path);
		final String query = uri.getQuery();
		if (query != null) {
			sb.append('?');
			sb.append(query);
		}
		final String fragment = uri.getFragment();
		if (fragment != null) {
			sb.append('#');
			sb.append(fragment);
		}
		if (!uri.isAbsolute() || uri.isOpaque() || uri.getScheme().length()>0) {
			final URL baseUrl = base.toURL();
			final String scheme = uri.getScheme();
			if (scheme == null || baseUrl.getProtocol().equals(scheme)) {
				final URL url = new URL(baseUrl, sb.toString());
				return url;
			}
		}
		final URL url = new URL(uri.getScheme(), uri.getHost(), uri.getPort(), sb.toString());
		return url;
	}

	public void setLastCurrentDir(final File lastCurrentDir) {
		UrlManager.lastCurrentDir = lastCurrentDir;
	}

	protected void setURL(final MapModel map, final URL url) {
		map.setURL(url);
	}

	public File defaultTemplateFile() {
		return null;
	}
}
