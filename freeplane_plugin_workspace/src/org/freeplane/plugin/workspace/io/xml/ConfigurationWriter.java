/**
 * author: Marcel Genzmehr
 * 25.07.2011
 */
package org.freeplane.plugin.workspace.io.xml;

import java.io.IOException;
import java.io.Writer;

import javax.swing.tree.DefaultMutableTreeNode;

import org.freeplane.core.io.IAttributeWriter;
import org.freeplane.core.io.IElementWriter;
import org.freeplane.core.io.ITreeWriter;
import org.freeplane.core.io.WriteManager;
import org.freeplane.core.io.xml.TreeXmlWriter;
import org.freeplane.features.map.MapWriter;
import org.freeplane.features.map.MapWriter.Hint;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.config.node.AWorkspaceNode;

/**
 * StringWriter writer = new StringWriter();
			WorkspaceController.getCurrentWorkspaceController().saveConfigurationAsXML(writer);
			System.out.println(writer.toString());
 */
public class ConfigurationWriter implements IElementWriter, IAttributeWriter {

	final private WriteManager writeManager;

	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	public ConfigurationWriter(final WriteManager manager) {
		writeManager = manager;
		
	}
	

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/
	
	public void writeConfigurationAsXml(final Writer fileout) throws IOException {
		final TreeXmlWriter xmlWriter = new TreeXmlWriter(writeManager, fileout);
		xmlWriter.setHint(Hint.MODE, MapWriter.Mode.FILE);
		writeNode(xmlWriter, (DefaultMutableTreeNode) WorkspaceController.getController().getViewModel().getRoot());
		xmlWriter.flush();
		fileout.close();
	}

	private void writeNode(final ITreeWriter xmlWriter, final DefaultMutableTreeNode node) throws IOException {
		final String nodeTag = ((AWorkspaceNode)node.getUserObject()).getTagName();
		if(nodeTag == null) return;

		xmlWriter.addElement(node, nodeTag);
	}

	public void writeNodeAsXml(final Writer writer, final DefaultMutableTreeNode node) throws IOException {
		final TreeXmlWriter xmlWriter = new TreeXmlWriter(writeManager, writer);
		writeNode(xmlWriter, node);
		xmlWriter.flush();
	}

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/

	public void writeAttributes(ITreeWriter writer, Object userObject, String tag) {
		System.out.println("write attributes for "+tag);
	}

	public void writeContent(ITreeWriter writer, Object element, String tag) throws IOException {
		final DefaultMutableTreeNode node = (DefaultMutableTreeNode) element;
		writeNode(writer, node);
	}
}
