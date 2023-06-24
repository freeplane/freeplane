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

import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.security.AccessController;
import java.security.PrivilegedAction;

import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.ui.components.JFreeplaneCustomizableFileChooser;
import org.freeplane.core.ui.components.PopupDialog;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.Compat;
import org.freeplane.core.util.FileUtils;
import org.freeplane.core.util.Hyperlink;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.explorer.MapExplorerController;
import org.freeplane.features.map.DocuMapAttribute;
import org.freeplane.features.map.IMapSelectionListener;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.MapWriter.Mode;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.n3.nanoxml.XMLException;
import org.freeplane.n3.nanoxml.XMLParseException;

/**
 * @author Dimitry Polivaev
 */
public class UrlManager implements IExtension {
	public static final String SMB_SCHEME = "smb";
	public static final String FREEPLANE_SCHEME = "freeplane";
	public static final String FILE_SCHEME = "file";
	public static final String FREEPLANE_FILE_EXTENSION_WITHOUT_DOT = "mm";
	public static final String FREEPLANE_FILE_EXTENSION = "." + FREEPLANE_FILE_EXTENSION_WITHOUT_DOT;
	public static final String FREEPLANE_ADD_ON_FILE_EXTENSION = ".addon." + FREEPLANE_FILE_EXTENSION_WITHOUT_DOT;
	private File lastCurrentDir = null;
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

	public static URI getAbsoluteUri(final MapModel map, URI uri) throws MalformedURLException {
	    if (uri == null  || uri.isAbsolute()) {
	        return uri;
	    }
	    final String path = uri.getPath();
	    try {
	        URL context = map.getURL();
	        if(context == null)
	            return null;
	        final URL url = new URL(context, path != null && path.isEmpty() ? "." : path);
	        return new URI(url.getProtocol(), url.getHost(), url.getPath(), uri.getQuery(), uri.getFragment());
	    }
	    catch (final URISyntaxException e) {
	        LogUtils.severe(e);
	        return null;
	    }
	}



// // 	final private Controller controller;
// 	final private ModeController modeController;

	public UrlManager() {
		super();
		Controller.getCurrentController().getMapViewManager().addMapSelectionListener(new IMapSelectionListener() {
			@Override
			public void afterMapChange(MapModel oldMap, MapModel newMap) {
				if(newMap != null)
				updateLastDirectoryFromMap(newMap);
			}
		});
	}

	protected UrlManager(File file) {
        this();
        lastCurrentDir = file;
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

	/**
	 * Creates a file chooser with the last selected directory as default.
	 * @param useDirectorySelector
	 */
    public JFreeplaneCustomizableFileChooser getFileChooser() {
		JFreeplaneCustomizableFileChooser choosery = getFileChooserNotFollowingDirectoryChanges();
		choosery.addHierarchyListener(new  HierarchyListener() {
            @Override
            public void hierarchyChanged(HierarchyEvent e) {
                if(0 != (e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) && ! choosery.isShowing()) {
                    setLastCurrentDir(choosery.getCurrentDirectory());
                }}});
		return choosery;

	}

    @SuppressWarnings("serial")
	protected JFreeplaneCustomizableFileChooser getFileChooserNotFollowingDirectoryChanges() {
		JFreeplaneCustomizableFileChooser choosery = AccessController.doPrivileged((PrivilegedAction<JFreeplaneCustomizableFileChooser>)() -> {
            final JFreeplaneCustomizableFileChooser chooser = new JFreeplaneCustomizableFileChooser(getLastCurrentDir());
            chooser.addCustomizer(PopupDialog::closeOnEscape);
            return chooser;
        });
		return choosery;
	}

    public JFreeplaneCustomizableFileChooser getFileChooser(final FileFilter filter) {
        JFreeplaneCustomizableFileChooser chooser = getFileChooser();
        chooser.addChoosableFileFilter(filter);
        chooser.setFileFilter(filter);
        return chooser;
	}
	public File getLastCurrentDir() {
		updateLastDirectoryFromCurrentMap();
		return lastCurrentDir;
	}

	private void updateLastDirectoryFromCurrentMap() {
		final MapModel map = Controller.getCurrentController().getMap();
		updateLastDirectoryFromMap(map);
	}

	private void updateLastDirectoryFromMap(final MapModel map) {
	    if(map == null || map.containsExtension(DocuMapAttribute.class))
	        return;
		final File lastChoosenDir = LastChoosenDirectory.get(map);
		if (lastChoosenDir != null) {
			this.lastCurrentDir = lastChoosenDir;
		}
	}

    private void updateLastDirectory(final MapModel map, final File lastChoosenDir) {
        if(map == null || lastChoosenDir == null || map.containsExtension(DocuMapAttribute.class))
            return;
        LastChoosenDirectory.set(map, lastChoosenDir);
    }

	public void handleLoadingException(final Exception ex) {
		Throwable rootCause = ExceptionUtils.getRootCause(ex);
		if(rootCause == null)
			rootCause = ex;
		String rootCauseMessage = ExceptionUtils.getMessage(rootCause);
		final String exceptionType = rootCause.getClass().getName();
		if (exceptionType.equals(XMLParseException.class.getName())) {
			final int showDetail = JOptionPane.showConfirmDialog(Controller.getCurrentController().getMapViewManager().getMapViewComponent(),
			    TextUtils.getText("map_corrupted"), "Freeplane", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
			if (showDetail == JOptionPane.YES_OPTION) {
				UITools.errorMessage(rootCauseMessage);
			}
		}
		else if (exceptionType.equals(FileNotFoundException.class.getName())) {
			UITools.errorMessage(rootCauseMessage);
		}
		else {
			LogUtils.severe(ex);
			UITools.errorMessage(rootCauseMessage);
		}
	}

	public boolean loadCatchExceptions(final URL url, final MapModel map){
		try {
			load(url, map);
			return true;
		}
		catch (final XMLException ex) {
			LogUtils.warn(ex);
		}
		catch (final IOException ex) {
			LogUtils.warn(ex);
		}
		catch (final RuntimeException ex) {
			try {
				final String urlString = url.toString();
				LogUtils.severe("Can not load url " + urlString, ex);
			} catch (Exception e) {
				LogUtils.severe("Can not load url", ex);
			}
		}
		UITools.errorMessage(TextUtils.format("url_open_error", url.toString()));
		return false;
	}


	public void load(final URL url, final MapModel map)
			throws IOException, XMLException {
		setURL(map, url);
		InputStream inputStream = getLocation(url).openStream();
		try (InputStreamReader urlStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
			final ModeController modeController = Controller.getCurrentModeController();
			modeController.getMapController().getMapReader().createNodeTreeFromXml(map, urlStreamReader, Mode.FILE);
		}
	}

	public URL getLocation(final URL url) throws IOException {
		URLConnection connection = url.openConnection();
		if(connection instanceof HttpURLConnection){
			int responseCode = ((HttpURLConnection)connection).getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP
				|| responseCode == HttpURLConnection.HTTP_MOVED_PERM
					|| responseCode == HttpURLConnection.HTTP_SEE_OTHER){
				String redirectUrl = connection.getHeaderField("Location");
				return getLocation(new URL(redirectUrl));
			}
		}
		return url;
	}

    /**@deprecated -- use LinkController*/
	@Deprecated
	public void loadHyperlink(Hyperlink link) {
		final String uriString = link.toUriFriendlyDecodedString();
		if (uriString.startsWith("#")) {
			loadLocalLinkURI(uriString);
		}
		else {
			final NodeAndMapReference nodeAndMapReference = new NodeAndMapReference(uriString);
			if (nodeAndMapReference.hasNodeReference()) {
				loadNodeReferenceURI(nodeAndMapReference);
			}
			else {
				loadOtherHyperlink(link, nodeAndMapReference.hasFreeplaneFileExtension());
			}
		}
	}

	private void loadLocalLinkURI(final String uriString) {
		final String target = uriString.substring(1);
		selectNode(null, target);
	}

	public void selectNode(NodeModel start, final String localReference) {
		try {
			final NodeModel node = Controller.getCurrentModeController().getExtension(MapExplorerController.class).getNodeAt(start, localReference);
			if (node != null) {
				final MapController mapController = getMapController();
				mapController.select(node);
			}
			else {
				final String errorMessage = TextUtils.format("link_not_found", localReference);
				Controller.getCurrentController().getViewController().err(errorMessage);
			}
		}
		catch (final Exception e) {
			LogUtils.severe("link " + localReference + " not found", e);
		}
	}

	private void loadNodeReferenceURI(final NodeAndMapReference nodeAndMapReference) {
	    try {
	        if(loadOtherHyperlink(new Hyperlink(new URI(nodeAndMapReference.getMapReference())), true)) {
	            final MapModel map = Controller.getCurrentController().getMap();
	            selectNode(map.getRootNode(), nodeAndMapReference.getNodeReference());
	        }
	    } catch (URISyntaxException e) {
	        LogUtils.severe(e);
	    }
	}

	private boolean loadOtherHyperlink(Hyperlink link, final boolean hasFreeplaneFileExtension) {
		URI uri = link.getUri();
		try {
			if(! uri.isAbsolute()){
				URI absoluteUri = getAbsoluteUri(uri);
				if (absoluteUri == null) {
					final MapModel map = Controller.getCurrentController().getMap();
					if (map.getURL() == null)
						UITools.errorMessage(TextUtils.getText("map_not_saved"));
					else
						UITools.errorMessage(TextUtils.format("link_not_found", String.valueOf(uri)));
					return false;
				}
				uri = absoluteUri;
				link = new Hyperlink(absoluteUri);
			}
			//DOCEAR: mindmaps can be linked in a mindmap --> therefore project-relative-paths are possible
			if(! asList(FILE_SCHEME, SMB_SCHEME, FREEPLANE_SCHEME).contains(uri.getScheme())) {
				try {
					uri = uri.toURL().openConnection().getURL().toURI().normalize();
				}
				catch (Exception e) {
					// ignore all exceptions due to unknown protocols
				}
			}
			try {
				if (hasFreeplaneFileExtension) {
					FreeplaneUriConverter freeplaneUriConverter = new FreeplaneUriConverter();
					final URL url = freeplaneUriConverter.freeplaneUrl(uri);
					final ModeController modeController = Controller.getCurrentModeController();
					modeController.getMapController().openMap(url);
					return true;
				}
				Controller.getCurrentController().getViewController().openDocument(link);
			}
			catch (final Exception e) {
				LogUtils.warn("link " + uri + " not found", e);
				UITools.errorMessage(TextUtils.format("link_not_found", uri.toString()));
			}
		}
		catch (final MalformedURLException ex) {
			LogUtils.warn("URL " + uri + " not found", ex);
			UITools.errorMessage(TextUtils.format("link_not_found", uri));
		}
		return false;
	}

	private MapController getMapController() {
		final ModeController modeController = Controller.getCurrentModeController();
		final MapController mapController = modeController.getMapController();
		return mapController;
	}

	public void loadMap(String map)
			throws URISyntaxException {

		if (map.startsWith(UrlManager.FREEPLANE_SCHEME + ':')) {
			String fixedUri = new FreeplaneUriConverter().fixPartiallyDecodedFreeplaneUriComingFromInternetExplorer(map);
			loadHyperlink(new Hyperlink(new URI(fixedUri)));
			return;
		}

		if(map.startsWith("http://") || map.startsWith("https://")|| map.startsWith("file:")) {
			loadHyperlink(new Hyperlink(new URI(map)));
		}
		else {
			if (!FileUtils.isAbsolutePath(map)) {
				map = System.getProperty("user.dir") + System.getProperty("file.separator") + map;
			}
			final NodeAndMapReference nodeAndMapReference = new NodeAndMapReference(map);
			if(nodeAndMapReference.hasFreeplaneFileExtension()) {
			    final URI uri = new File(nodeAndMapReference.getMapReference()).toURI();
			    final URI uriWithNodeReference = new URI(uri.getScheme(), null, uri.getPath(), nodeAndMapReference.getNodeReference());
			    loadHyperlink(new Hyperlink(uriWithNodeReference));
			}
			else {
			    LogUtils.warn("Invalid mind map file extension, not opened: " + map);
			}
		}
	}

	private URI getAbsoluteUri(final URI uri) throws MalformedURLException {
		if (uri.isAbsolute()) {
			return uri;
		}
		final MapModel map = Controller.getCurrentController().getMap();
		return getAbsoluteUri(map, uri);
	}


    //DOCEAR - added project relative uri resolution
	@SuppressWarnings("unused")
    private URI resolveWorkspaceRelatedUri(final URI uri) {
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
	    return resolvedURI;
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
					urlConnection = UrlManager.getAbsoluteUri(map, uri).toURL().openConnection();
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

	private void setLastCurrentDir(final File lastChoosenDir) {
		this.lastCurrentDir = lastChoosenDir;
		updateLastDirectory(Controller.getCurrentController().getMap(), lastChoosenDir);
	}

	protected void setURL(final MapModel map, final URL url) {
		map.setURL(url);
	}

	public File defaultTemplateFile() {
		return null;
	}
}
