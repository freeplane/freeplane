package org.docear.plugin.core.util;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.attribute.AttributeController;
import org.freeplane.features.attribute.NodeAttributeTableModel;
import org.freeplane.features.link.NodeLinks;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.url.UrlManager;
import org.freeplane.plugin.workspace.WorkspaceUtils;
import org.freeplane.view.swing.map.MapView;
import org.freeplane.view.swing.map.attribute.AttributeView;

public class Tools {
	
	//TODO: check if URI refers to a local file !!
	
	public static File getFilefromUri(URI uri){		
		if(uri == null) return null;
		
		try {
			return WorkspaceUtils.resolveURI(uri.normalize());
		} 
		catch (IllegalArgumentException e) {
			//return new File(getAbsoluteUri(uri, map));
			e.printStackTrace();
			return null;
		}
	}
	
	public static URI getAbsoluteUri(NodeModel node){
		URI uri = NodeLinks.getValidLink(node);
		return Tools.getAbsoluteUri(uri, node.getMap());
	}
	
	public static URI getAbsoluteUri(NodeModel node, MapModel map){
		URI uri = NodeLinks.getValidLink(node);
		return Tools.getAbsoluteUri(uri, map);
	}
	
	public static URI getAbsoluteUri(URI uri){
		return Tools.getAbsoluteUri(uri, Controller.getCurrentController().getMap());
	}
	
	public static URI getAbsoluteUri(URI uri, MapModel map){
		if(uri == null) return null;
		try{
			// uri with scheme is always "absolute" ( so are workspace relative links)
			if(!uri.isAbsolute()){
				final UrlManager urlManager = (UrlManager) Controller.getCurrentModeController().getExtension(UrlManager.class);
				//MapModel map = Controller.getCurrentController().getMap();
				if(map == null || urlManager == null) return null;
				if(uri.getScheme() == null ||uri.getScheme().equals(""))
				uri = urlManager.getAbsoluteUri(map, uri);				
			}
			if(uri.getScheme().equals("file")) return uri;
			return uri.toURL().openConnection().getURL().toURI();
		} 
		catch(IllegalArgumentException e){
			return null;
		} 
		catch (MalformedURLException e) {
			return null;
		}
		catch (URISyntaxException e) {
			return null;
		}
		catch (IOException e) {
			return null;
		}
	}
	
	
	
	
    public static boolean isFile(URI uri) {
    	final String scheme = uri.getScheme();
		return scheme != null && scheme.equalsIgnoreCase("file");
    }

    public static boolean hasHost(URI uri) {
        String host = uri.getHost();
        return host != null && !"".equals(host);
    }
	
	public static List<File> textURIListToFileList(String data) {
	    List<File> list = new ArrayList<File>();
	    StringTokenizer stringTokenizer = new StringTokenizer(data, "\r\n");
	    while(stringTokenizer.hasMoreTokens()) {
	    	String string = stringTokenizer.nextToken();
	    	// the line is a comment (as per the RFC 2483)
	    	if (string.startsWith("#")) continue;
		    		    
			try {
				URI uri = new URI(string);
				File file = new File(uri);
			    list.add(file);
			} catch (URISyntaxException e) {
				LogUtils.warn("DocearNodeDropListener could not parse uri to file because an URISyntaxException occured. URI: " + string);
			} catch (IllegalArgumentException e) {
				LogUtils.warn("DocearNodeDropListener could not parse uri to file because an IllegalArgumentException occured. URI: " + string);
		    }	    
	    }	     
	    return list;
	}

	public static String reshapeString(String s, int i) {
		s = s.trim();
		if(s.length() > i){
			s = s.substring(0, i - 4);
			s = s + "...";
		}
		return s;
	}
	
	public static boolean exists(URI uri) {
		if(Controller.getCurrentController() != null && Controller.getCurrentController().getMap() != null){
			return Tools.exists(uri, Controller.getCurrentController().getMap());
		}
		else{
			return Tools.exists(uri, null);
		}
	}

	public static boolean exists(URI uri, MapModel map) {
		uri = Tools.getAbsoluteUri(uri, map);
		try {
			if(uri.toURL().openConnection().getContentLength() > 0) {
				return true;
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	public static boolean FileIsLocatedInDir(URI absoluteFile, URI absoluteDir, boolean readSubDirectories){
		if(!absoluteFile.isAbsolute() || !absoluteDir.isAbsolute()) return false;
		
		final File file = Tools.getFilefromUri(absoluteFile);
		File dir = Tools.getFilefromUri(absoluteDir);
		File[] matchingFiles = dir.listFiles(new FilenameFilter() {
			
			public boolean accept(File dir, String name) {				
				return name.equals(file.getName());
			}
		});
		
		if(matchingFiles.length > 0){
			return true;
		}
		else if(readSubDirectories){
			File[] subDirs = dir.listFiles(new DirectoryFileFilter());
			if(subDirs != null && subDirs.length > 0){
				for(File subDir : subDirs){
					if(Tools.FileIsLocatedInDir(file.toURI(), subDir.toURI(), readSubDirectories)){
						return true;
					}
				}
			}
		}
		return false;
	}

	public static Collection<URI> getFilteredFileList(URI absoluteURI, FileFilter fileFilter, boolean readSubDirectories) {
		Collection<URI> result = new ArrayList<URI>();
		Collection<File> tempResult = new ArrayList<File>();
		if(!absoluteURI.isAbsolute()) return result;
		
		File monitoringDir = Tools.getFilefromUri(absoluteURI);
		File[] monitorFiles = monitoringDir.listFiles(fileFilter);
		if(monitorFiles != null && monitorFiles.length > 0){
			tempResult.addAll(Arrays.asList(monitorFiles));
		}	
		for(File file : tempResult){
			result.add(file.toURI());
		}
		if(readSubDirectories){
			File[] subDirs = monitoringDir.listFiles(new DirectoryFileFilter());
			if(subDirs != null && subDirs.length > 0){
				for(File subDir : subDirs){
					result.addAll(Tools.getFilteredFileList(subDir.toURI(), fileFilter, readSubDirectories));
				}
			}			
		}		
		return result;
	}

	public static List<String> getStringList(String property) {
		List<String> result = new ArrayList<String>();
		
		if(property == null || property.length() <= 0) return result;
		property = property.trim();		
		String[] list = property.split("\\|");
		for(String s : list){
			if(s != null && s.length() > 0){
				result.add(s);
			}
		}
		return result;
	}
	
	public static boolean setAttributeValue(NodeModel target, String attributeKey, Object value){
		if(target == null || attributeKey == null || value == null) return false;
		
		NodeAttributeTableModel attributes = AttributeController.getController().createAttributeTableModel(target);
		if(attributes != null){
			if(attributes.getAttributeKeyList().contains(TextUtils.getText(attributeKey))){
				//attributes.getAttribute(attributes.getAttributePosition(TextUtils.getText(attributeKey))).setValue(value);
				AttributeController.getController().performSetValueAt(attributes, value, attributes.getAttributePosition(attributeKey), 1);
				AttributeView attributeView = (((MapView) Controller.getCurrentController().getViewController().getMapView()).getSelected()).getAttributeView();
	    		attributeView.setOptimalColumnWidths();
				return true;
			}
			else{
				AttributeController.getController().performInsertRow(attributes, attributes.getRowCount(), TextUtils.getText(attributeKey), value); 
				AttributeView attributeView = (((MapView) Controller.getCurrentController().getViewController().getMapView()).getSelected()).getAttributeView();
	    		attributeView.setOptimalColumnWidths();
				return true;
			}
		}
		return false;	
	}

	public static Object getAttributeValue(NodeModel target, String attributeKey) {
		if(target == null || attributeKey == null) return null;
		NodeAttributeTableModel attributes = AttributeController.getController().createAttributeTableModel(target);
		if(attributes != null){
			if(attributes.getAttributeKeyList().contains(TextUtils.getText(attributeKey))){
				return attributes.getAttribute(attributes.getAttributePosition(TextUtils.getText(attributeKey))).getValue();				
			}
		}
		return null;
	}
	
	public static void removeAttributeValue(NodeModel target, String attributeKey) {
		if(target == null || attributeKey == null) return;
		NodeAttributeTableModel attributes = AttributeController.getController().createAttributeTableModel(target);
		if(attributes != null){
			if(attributes.getAttributeKeyList().contains(TextUtils.getText(attributeKey))){
				AttributeController.getController().performRemoveRow(attributes, attributes.getAttributePosition(attributeKey));
				AttributeView attributeView = (((MapView) Controller.getCurrentController().getViewController().getMapView()).getSelected()).getAttributeView();
	    		attributeView.setOptimalColumnWidths();		
			}
		}		
	}
	
	public static List<String> getAllAttributeKeys(NodeModel target){
		if(target == null) return new ArrayList<String>();
		NodeAttributeTableModel attributes = AttributeController.getController().createAttributeTableModel(target);
		if(attributes != null){
			return attributes.getAttributeKeyList();
		}
		return new ArrayList<String>();
	}	
	
	public static String getStackTraceAsString(Exception exception){ 
		StringWriter sw = new StringWriter(); 
		PrintWriter pw = new PrintWriter(sw); 
		pw.print(" [ "); 
		pw.print(exception.getClass().getName()); 
		pw.print(" ] "); 
		pw.print(exception.getMessage()); 
		exception.printStackTrace(pw); 
		return sw.toString(); 
	}

}
