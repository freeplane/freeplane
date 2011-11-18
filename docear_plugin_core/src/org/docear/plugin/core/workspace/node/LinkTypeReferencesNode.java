/**
 * author: Marcel Genzmehr
 * 18.08.2011
 */
package org.docear.plugin.core.workspace.node;

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

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.docear.plugin.core.CoreConfiguration;
import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.IBibtexDatabase;
import org.docear.plugin.core.event.DocearEvent;
import org.docear.plugin.core.event.DocearEventType;
import org.docear.plugin.core.workspace.node.config.NodeAttributeObserver;
import org.freeplane.plugin.workspace.WorkspaceUtils;
import org.freeplane.plugin.workspace.config.node.LinkTypeFileNode;
import org.freeplane.plugin.workspace.controller.WorkspaceNodeEvent;
import org.freeplane.plugin.workspace.model.node.AWorkspaceTreeNode;

/**
 * 
 */
public class LinkTypeReferencesNode extends LinkTypeFileNode /*LinkNode*/ implements IBibtexDatabase, ChangeListener {
	private static final String DEFAULT_REFERENCE_TEMPLATE = "/conf/reference_db.bib";
	private static final Icon DEFAULT_ICON = new ImageIcon(LinkTypeReferencesNode.class.getResource("/images/text-x-bibtex.png"));

	private static final long serialVersionUID = 1L;

	private boolean locked = false;	
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	public LinkTypeReferencesNode(String type) {
		super(type);
		DocearEvent event = new DocearEvent(this, DocearEventType.LIBRARY_NEW_REFERENCES_INDEXING_REQUEST, this);
		DocearController.getController().dispatchDocearEvent(event);
		CoreConfiguration.referencePathObserver.addChangeListener(this);
	}
	
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	public boolean setIcons(DefaultTreeCellRenderer renderer) {
		renderer.setOpenIcon(DEFAULT_ICON);
		renderer.setClosedIcon(DEFAULT_ICON);
		renderer.setLeafIcon(DEFAULT_ICON);
		return true;
	}
	
	public void handleEvent(WorkspaceNodeEvent event) {
		if (event.getType() == WorkspaceNodeEvent.MOUSE_RIGHT_CLICK) {			
			//TODO: DOCEAR implement popup handling if needed
		} 
		else {
			super.handleEvent(event);
		}
	}

	public AWorkspaceTreeNode clone() {
		LinkTypeReferencesNode node = new LinkTypeReferencesNode(this.getType());
		return clone(node);
	}
	
	public void disassociateReferences()  {
		CoreConfiguration.referencePathObserver.removeChangeListener(this);
	}
	
	public void setLinkPath(URI uri) {
		super.setLinkPath(uri);
		locked = true;
		CoreConfiguration.referencePathObserver.setValue(WorkspaceUtils.resolveURI(uri).getPath());
		createIfNeeded(uri);
		locked = false;
	}
	
	private void createIfNeeded(URI uri) {
		try {
			File file = WorkspaceUtils.resolveURI(uri);
			if(file != null) {
				if (!file.getParentFile().exists()) {
					if(!file.getParentFile().mkdirs()) {
						return;
					}
				}
				if(!file.exists()) {
					if(!file.createNewFile()) {
						return;
					} else {
						copyDefaultsTo(file);
					}
				}
				this.setName(file.getName());
			}
		}
		catch (IOException e) {
			return;
		}
	}
	
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
	public URI getUri() {
		return this.getLinkPath();
	}

	public void stateChanged(ChangeEvent e) {
		if(!locked && e.getSource() instanceof NodeAttributeObserver) {
			String path = (String) ((NodeAttributeObserver) e.getSource()).getValue();
			URI uri;
			try{
				uri = (new File(path)).toURI();
			}
			catch (Exception ex) {
				return;
			}
			this.setLinkPath(uri);
		}
	}
}
