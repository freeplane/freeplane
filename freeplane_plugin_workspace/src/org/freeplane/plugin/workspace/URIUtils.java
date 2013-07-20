package org.freeplane.plugin.workspace;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import org.freeplane.core.util.LogUtils;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.link.NodeLinks;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.url.UrlManager;

public abstract class URIUtils {

	public static File getAbsoluteFile(URI path) {
		if(path != null) {	
			return getFile(URIUtils.getAbsoluteURI(path));
		}
		return null;
	}

	public static URI getAbsoluteURI(URI uri) {
		try {
			return UrlManager.getController().getAbsoluteUri(null, uri);
		} catch (MalformedURLException e) {
			LogUtils.warn("could not resolve URI: "+ e.getMessage());
		}
		return null;
	}
	
	public static URI resolveURI(URI base, URI uri) {
		try {
			if(base == null) {
				return getAbsoluteURI(uri);
			}
			else {
				URL url = null;
				try {
					url = UrlManager.getController().getAbsoluteUrl(base, uri);
					return url.toURI();
				}
				catch (Exception e) {
					try {
						return new URI(url.getProtocol(), url.getHost(), url.getPath(), url.getQuery(), uri.getFragment());
					}
					catch (Exception ex) {
						LogUtils.warn("could not resolve URI: "+ ex.getMessage());
					}
				}
			}
		} catch (Exception e) {			
			LogUtils.warn("could not resolve URI: "+ e.getMessage());
		}
		return null;
	}
	
	public static URI getAbsoluteURI(NodeModel node) {
		if(node == null) {
			return null;
		}
		URI uri = NodeLinks.getValidLink(node);
		try {
			return UrlManager.getController().getAbsoluteUri(node.getMap(), uri);
		} catch (Exception e) {
			LogUtils.warn(e);
		}
		return null;
	}
	
	public static URI getAbsoluteURI(MapModel map) {
		if(map != null) {
			try {
				return map.getURL().toURI();
			} catch (Exception e) {
				LogUtils.info("Exception in "+ URIUtils.class+".getAbsoluteURI(MapModel): "+ e.getMessage());
			}
		}
		return null;
	}

	public static File getFile(URI absoluteURI) {
		if(absoluteURI != null) {
			if(!"file".equals(absoluteURI.getScheme())) {
				return null;
			}
			try {
				if(absoluteURI.getRawPath().startsWith("//")) {
					new File("\\\\"+absoluteURI.normalize().getPath());
				}
				else {
					return new File(absoluteURI.normalize());
				}
			}
			catch (Exception e) {
				LogUtils.info("Exception in "+ URIUtils.class+".getFile(URI): "+ e.getMessage());
			}
		}
		return null;
	}

	public static URI getRelativeURI(File base, File absoluteFile) {
		try {
			if(base == null) {
				return absoluteFile.toURI();
			}
			
			return LinkController.toLinkTypeDependantURI(base, absoluteFile);
		} catch (Exception e) {
		}
		return null;
	}

	public static URI createURI(String value) {
		if(value == null) {
			return null;
		}
		try {
			return LinkController.createURI(value);
		} catch (Exception e) {
			LogUtils.warn(e);
		}
		return null;
	}
	
	
	public static String encodedFilePath(String path) throws IllegalArgumentException {
		try {
			URI uri = new URI("file", null, path, null);
			return uri.getRawPath();
		}
		catch (Exception e) {
			throw new IllegalArgumentException("Exception in org.freeplane.plugin.workspace.URIUtils.encodeFilePath(path): "+e.getMessage());
		}
	}
	
	public static String decodedFilePath(String path) throws IllegalArgumentException {
		try {
			URI uri = new URI(path);
			return uri.getPath();
		}
		catch (Exception e) {
			throw new IllegalArgumentException("Exception in org.freeplane.plugin.workspace.URIUtils.encodeFilePath(path): "+e.getMessage());
		}
	}
}
