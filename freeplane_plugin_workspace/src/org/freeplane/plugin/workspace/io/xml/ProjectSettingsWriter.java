/**
 * author: Marcel Genzmehr
 * 25.07.2011
 */
package org.freeplane.plugin.workspace.io.xml;

import java.io.IOException;
import java.io.Writer;

import org.freeplane.core.io.IAttributeWriter;
import org.freeplane.core.io.IElementWriter;
import org.freeplane.core.io.ITreeWriter;
import org.freeplane.core.io.WriteManager;
import org.freeplane.core.io.xml.TreeXmlWriter;
import org.freeplane.features.map.MapWriter;
import org.freeplane.features.map.MapWriter.Hint;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;
import org.freeplane.plugin.workspace.model.project.AWorkspaceProject;


public class ProjectSettingsWriter implements IElementWriter, IAttributeWriter {

	final private WriteManager writeManager;

	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	public ProjectSettingsWriter(final WriteManager manager) {
		writeManager = manager;
		
	}
	

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/
	
	public void storeProject(final Writer writer, AWorkspaceProject project) throws IOException {
		final TreeXmlWriter xmlWriter = new TreeXmlWriter(writeManager, writer);
		xmlWriter.setHint(Hint.MODE, MapWriter.Mode.FILE);
		writeNode(xmlWriter, project.getModel().getRoot());
		xmlWriter.flush();
		writer.close();
	}

	private void writeNode(final ITreeWriter xmlWriter, final AWorkspaceTreeNode node) throws IOException {
		final String nodeTag = node.getTagName();
		if(nodeTag == null) return;

		xmlWriter.addElement(node, nodeTag);
	}

	public void writeNodeAsXml(final Writer writer, final AWorkspaceTreeNode node) throws IOException {
		final TreeXmlWriter xmlWriter = new TreeXmlWriter(writeManager, writer);
		writeNode(xmlWriter, node);
		xmlWriter.flush();
	}

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/

	public void writeAttributes(ITreeWriter writer, Object userObject, String tag) {
	}

	public void writeContent(ITreeWriter writer, Object element, String tag) throws IOException {
		writeNode(writer, (AWorkspaceTreeNode) element);
	}
}
