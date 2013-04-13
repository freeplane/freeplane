package org.freeplane.plugin.workspace.mindmapmode;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

import org.freeplane.core.util.Compat;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.url.UrlManager;
import org.freeplane.features.url.mindmapmode.MFileManager;

public class MModeWorkspaceUrlManager extends MFileManager {
	//WORKSPACE - test: implementation of workspace/project relative uri resolving
	
	public static MModeWorkspaceUrlManager getController() {
		final ModeController modeController = Controller.getCurrentModeController();
		return (MModeWorkspaceUrlManager) modeController.getExtension(UrlManager.class);
	}
	
    protected void init() {
    	
    }
    
    public URI getAbsoluteUri(final MapModel map, final URI uri) throws MalformedURLException {
    	if(uri == null) {
			return null;
		}
    	URL url = null;
    	try {
    		url = getAbsoluteUrl(map, uri);
    		if(url == null) {
    			return null;
    		}
			return url.toURI();
		} catch (URISyntaxException e) {
			try {
				return new URI(url.getProtocol(), url.getHost(), url.getPath(), url.getQuery(), null);
			}
			catch (Exception ex) {
				throw new MalformedURLException(e.getMessage());
			}			
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
				if(map == null) {
					urlConnection = (new File(uri.toString())).toURI().toURL().openConnection();
				} 
				else {
					urlConnection = getAbsoluteUri(map, uri).toURL().openConnection();
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
		try {
			URI baseUri = null;
			if(map != null && map.getURL() != null) {
				baseUri = map.getURL().toURI();
			}
			return getAbsoluteUrl(baseUri, uri);
		} catch (URISyntaxException e) {
			LogUtils.warn(e);
		}
		return null;
	}
	
	public URL getAbsoluteUrl(final URI base, final URI uri) throws MalformedURLException {
		if(uri == null) {
			if(base == null) {
				return null;
			}
			return base.toURL();
		}
		URI preResolved = getAbsoluteURI(uri);
		if(base == null) {
			if(preResolved != null) {
				return preResolved.toURL();
			}
			return null;
		}
		return super.getAbsoluteUrl(base, preResolved);
	}
	
	public URI getAbsoluteURI(final URI uri) {
    	try {
    		if(uri.getScheme() == null) {
    			return uri;
    		}
    		return uri.toURL().openConnection().getURL().toURI();
    	} catch (IOException ex) {
    		LogUtils.warn(ex);
    		return null;
    	} catch (URISyntaxException ex) {
    		LogUtils.warn(ex);
    		return null;
    	}
	}
}
