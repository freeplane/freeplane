/**
 * author: Marcel Genzmehr
 * 18.08.2011
 */
package org.docear.plugin.core.workspace.creator;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;

import org.docear.plugin.core.workspace.node.LinkTypeReferencesNode;
import org.freeplane.n3.nanoxml.XMLElement;
import org.freeplane.plugin.workspace.WorkspaceUtils;
import org.freeplane.plugin.workspace.config.creator.AWorkspaceNodeCreator;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;

/**
 * 
 */
public class LinkTypeReferencesCreator extends AWorkspaceNodeCreator {

	public static final String LINK_TYPE_REFERENCES = "references";
	
	private static final String DEFAULT_REFERENCE_TEMPLATE = "/conf/reference_db.bib";

	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	private void copyDefaultsTo(File config) throws FileNotFoundException, IOException {
		String referenceContent;
		referenceContent = getFileContent(DEFAULT_REFERENCE_TEMPLATE);
		
		DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(config)));
		out.write(referenceContent.getBytes());
		out.close();
	}
	
	private String getFileContent(String filename) throws IOException {
		InputStream in = getClass().getResourceAsStream(filename);
		Writer writer = new StringWriter();
		char[] buffer = new char[1024];

		try {
			Reader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
			int n;

			while ((n = reader.read(buffer)) != -1) {
				writer.write(buffer, 0, n);
			}

		}
		finally {
			in.close();
		}

		return writer.toString();
	}
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	
	public AWorkspaceTreeNode getNode(XMLElement data) {
		String type = data.getAttribute("type", LINK_TYPE_REFERENCES);
		LinkTypeReferencesNode node = new LinkTypeReferencesNode(type);
		//TODO: add missing attribute handling
		String path = data.getAttribute("path", null);
		if (path == null) {
			return null;
		}	
		node.setLinkPath(URI.create(path));
		String name = "not yet set!";
		try {
			File file = WorkspaceUtils.resolveURI(node.getLinkPath());
			if(file != null) {
				if (!file.getParentFile().exists()) {
					if(!file.getParentFile().mkdirs()) {
						return null;
					}
				}
				if(!file.exists()) {
					if(!file.createNewFile()) {
						return null;
					} else {
						copyDefaultsTo(file);
					}
				}
				name = data.getAttribute("name", file.getName());
			}			
		}
		catch (IOException e) {
			return null;
		}
		node.setName(name);
		return node;
	}
}
