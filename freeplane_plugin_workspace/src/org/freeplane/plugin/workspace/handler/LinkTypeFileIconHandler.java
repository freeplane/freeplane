/**
 * author: Marcel Genzmehr
 * 28.12.2011
 */
package org.freeplane.plugin.workspace.handler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.apache.commons.io.FilenameUtils;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.LogUtils;
import org.freeplane.plugin.workspace.URIUtils;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;
import org.freeplane.plugin.workspace.nodes.LinkTypeFileNode;

/**
 * 
 */
public class LinkTypeFileIconHandler implements INodeTypeIconHandler {

	private HashMap<String, Icon> iconMap = new HashMap<String, Icon>();
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	public LinkTypeFileIconHandler() {
		Properties properties = new Properties();
		InputStream inStream = LinkTypeFileIconHandler.this.getClass().getResourceAsStream("/conf/fileIcons.properties");
		try {
			properties.load(inStream);
			init(properties);
		}
		catch (IOException e) {
			LogUtils.severe("could not load icon configuration for LinkTypeFileIconHandler", e);
		}
	}
	
	
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/
	/**
	 * @param ext
	 * @return
	 */
	private Icon selectIconForExtension(String ext) {
		return iconMap.get(ext);
	}
	
	/**
	 * @param properties
	 */
	private void init(Properties properties) {
		Enumeration<Object> keys = properties.keys();
		while(keys.hasMoreElements()) {
			String key = keys.nextElement().toString();
			if(key.endsWith(".icon")) {
				String keyName = key.substring(0, key.indexOf("."));
				String iconPath = properties.getProperty(key, null);
				URL url = null;
				if(iconPath !=null) {
					url = LinkTypeFileIconHandler.this.getClass().getResource(iconPath);
					if(url == null) {
						url = ResourceController.class.getResource(iconPath);
						if(url == null) {
							url = ClassLoader.getSystemResource(iconPath);
							if(url == null) {
								url = ClassLoader.getSystemResource(iconPath);
								if(url == null) {
									continue;
								}
							}					
						}				
					}
				}
				Icon icon = new ImageIcon(url);
				String[] extensions = properties.getProperty(keyName+".extensions", "").split(";");
				for(String ext : extensions) {
					iconMap.put(ext, icon);
				}
				
			}
		}
	}

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	
	public Icon getIconForNode(AWorkspaceTreeNode node) {
		assert(node instanceof LinkTypeFileNode);
		File file = URIUtils.getAbsoluteFile(((LinkTypeFileNode) node).getLinkURI());
		if(file != null) {
			String ext = FilenameUtils.getExtension(file.getName());
			return selectIconForExtension(ext);
		}
		return null;
	}

	
}
