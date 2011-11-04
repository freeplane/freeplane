/**
 * author: Marcel Genzmehr
 * 18.08.2011
 */
package org.docear.plugin.core.workspace.node;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.IBibtexDatabase;
import org.docear.plugin.core.IDocearLibrary;
import org.docear.plugin.core.event.DocearEvent;
import org.docear.plugin.core.event.DocearEventType;
import org.docear.plugin.core.event.IDocearEventListener;
import org.freeplane.core.util.Compat;
import org.freeplane.core.util.LogUtils;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.WorkspaceUtils;
import org.freeplane.plugin.workspace.config.node.AFolderNode;
import org.freeplane.plugin.workspace.config.node.LinkTypeFileNode;
import org.freeplane.plugin.workspace.controller.IWorkspaceNodeEventListener;
import org.freeplane.plugin.workspace.controller.WorkspaceNodeEvent;
import org.freeplane.plugin.workspace.dnd.IDropAcceptor;
import org.freeplane.plugin.workspace.dnd.WorkspaceTransferable;
import org.freeplane.plugin.workspace.io.INodeCreatedListener;
import org.freeplane.plugin.workspace.io.NodeCreatedEvent;
import org.freeplane.plugin.workspace.io.node.MindMapFileNode;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;

public class FolderTypeLibraryNode extends AFolderNode implements IDocearEventListener, IDocearLibrary, IWorkspaceNodeEventListener, IDropAcceptor, INodeCreatedListener {
	private static final Icon DEFAULT_ICON = new ImageIcon(FolderTypeLibraryNode.class.getResource("/images/folder-database.png"));
	
	private final static String PLACEHOLDER_PROFILENAME = "@@PROFILENAME@@";
	private static final String DEFAULT_LIBRARY_PATH = "workspace:/"+PLACEHOLDER_PROFILENAME+"/Library";
	private final static Pattern PATTERN = Pattern.compile(PLACEHOLDER_PROFILENAME);
	
	
	private final Vector<URI> mindmapIndex = new Vector<URI>();
	private final Vector<IBibtexDatabase> referencesIndex = new Vector<IBibtexDatabase>();
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	public FolderTypeLibraryNode(String type) {
		super(type);
		DocearController.getController().addDocearEventListener(this);
		DocearEvent event = new DocearEvent(this, DocearEventType.NEW_LIBRARY);
		DocearController.getController().dispatchDocearEvent(event);
		WorkspaceController.getController().getFilesystemReader().addNodeCreatedListener(this);
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
	
	/**
	 * @param targetNode
	 * @param file
	 */
	private void handleFileDrop(AWorkspaceTreeNode targetNode, final File file) {
		if(file.isDirectory()) {
			WorkspaceUtils.createPhysicalFolderNode(file, targetNode);
		}
		else {
			WorkspaceUtils.createLinkTypeFileNode(file, targetNode);
		}
		WorkspaceController.getController().reloadWorkspace();
	}
	

	/**
	 * @param event
	 * @param targetNode
	 * @param transferable
	 * @return
	 * @throws UnsupportedFlavorException
	 * @throws IOException
	 */
	@SuppressWarnings({ "unchecked"})
	private boolean handleWorkspaceNodes(DropTargetDropEvent event, AWorkspaceTreeNode targetNode, Transferable transferable)
			throws UnsupportedFlavorException, IOException {
		event.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
		List<AWorkspaceTreeNode> nodeList = (List<AWorkspaceTreeNode>)transferable.getTransferData(WorkspaceTransferable.WORKSPACE_NODE_FLAVOR);
		for(AWorkspaceTreeNode node : nodeList) {
			if(event.getDropAction() == DnDConstants.ACTION_MOVE) {
				WorkspaceUtils.getModel().moveNodeTo(node, targetNode);
			} 
			else 
			if(event.getDropAction() == DnDConstants.ACTION_COPY) {
				WorkspaceUtils.getModel().copyNodeTo(node, targetNode);								
			}
		}
		WorkspaceUtils.getModel().reload(targetNode);
		event.getDropTargetContext().dropComplete(true);
		return true;
	}

	/**
	 * @param event
	 * @param targetNode
	 * @param transferable
	 * @return
	 * @throws UnsupportedFlavorException
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws MalformedURLException
	 */
	private boolean handleUriList(DropTargetDropEvent event, AWorkspaceTreeNode targetNode, Transferable transferable)
			throws UnsupportedFlavorException, IOException, URISyntaxException, MalformedURLException {
		event.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
		String list = (String) transferable.getTransferData(WorkspaceTransferable.WORKSPACE_URI_LIST_FLAVOR);
		System.out.println(list);
		//FIXME: allow multiple files 
		if (!list.startsWith("file://")) {
			return false;
		}
		final URI uri = new URI(new URL(list).toString());
		final URL url = new URL(uri.getScheme(), uri.getHost(), uri.getPath());
		final File file = Compat.urlToFile(url);
		handleFileDrop(targetNode, file);
		
		event.getDropTargetContext().dropComplete(true);
		return true;
	}

	/**
	 * @param event
	 * @param targetNode
	 * @param transferable
	 * @return
	 * @throws UnsupportedFlavorException
	 * @throws IOException
	 */
	private boolean handleFileList(DropTargetDropEvent event, AWorkspaceTreeNode targetNode, Transferable transferable)
			throws UnsupportedFlavorException, IOException {
		event.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
		List<?> list = (List<?>) transferable.getTransferData(WorkspaceTransferable.WORKSPACE_FILE_LIST_FLAVOR);
		for (Object item : list) {
			if(item instanceof File) {
				File file = (File)item;
				handleFileDrop(targetNode, file);
			}					
		}				
		event.getDropTargetContext().dropComplete(true);
		return true;
	}
	
	protected AWorkspaceTreeNode clone(FolderTypeLibraryNode node) {		
		for(IBibtexDatabase ref : referencesIndex) {
			node.addReferenceToIndex(ref);
		}
		for(URI uri : mindmapIndex) {
			node.addMindmapToIndex(uri);
		}
		return super.clone(node);
	}
	
	public AWorkspaceTreeNode clone() {
		FolderTypeLibraryNode node = new FolderTypeLibraryNode(getType());
		return clone(node);
	}
	
	protected void addMindmapToIndex(URI uri) {
		mindmapIndex.add(uri);
	}
	
	protected void addReferenceToIndex(IBibtexDatabase ref) {
		referencesIndex.add(ref);
	}
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	
	public void handleEvent(DocearEvent event) {
		if(event.getType() == DocearEventType.LIBRARY_NEW_MINDMAP_INDEXING_REQUEST) {
			if(event.getEventObject() instanceof URI) {
				URI uri = (URI) event.getEventObject();
				if(!mindmapIndex.contains(uri)) {
					LogUtils.info("DOCEAR: adding new mindmap to library: "+ uri);
					addMindmapToIndex(uri);
				}
			}			
		}
		if(event.getType() == DocearEventType.LIBRARY_NEW_REFERENCES_INDEXING_REQUEST) {
			if(event.getEventObject() instanceof IBibtexDatabase) {
				if(!referencesIndex.contains((IBibtexDatabase) event.getEventObject())) {
					LogUtils.info("DOCEAR: adding new reference database to library: "+ event.getEventObject());
					addReferenceToIndex((IBibtexDatabase) event.getEventObject());
				}
			}			
		}
		else if(event.getType() == DocearEventType.LIBRARY_EMPTY_MINDMAP_INDEX_REQUEST) {
			mindmapIndex.removeAllElements();			
		}
		
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void handleEvent(WorkspaceNodeEvent event) {
		//TODO: DOCEAR do nothing atm 
	}
	public List<URI> getMindmaps() {
		return mindmapIndex;
	}
	
	public URI getLibraryPath() {		
		Matcher mainMatcher = PATTERN.matcher(DEFAULT_LIBRARY_PATH);
		String ret = mainMatcher.replaceAll("." + WorkspaceController.getController().getPreferences().getWorkspaceProfile());
		return WorkspaceUtils.absoluteURI(URI.create(ret));		
	}

	public URI getBibtexDatabase() {
		URI uri = null;
		if(referencesIndex.size() > 0) {
			return referencesIndex.get(0).getUri();
		}
		return uri;
	}

	public boolean acceptDrop(DataFlavor[] flavors) {
		// TODO Auto-generated method stub
		return true;
	}

	public boolean processDrop(DropTargetDropEvent event) {
		try {
			Transferable transferable = event.getTransferable();
			if (transferable.isDataFlavorSupported(WorkspaceTransferable.WORKSPACE_FILE_LIST_FLAVOR)) {
				return handleFileList(event, this, transferable);
			} 
			else
			if (transferable.isDataFlavorSupported(WorkspaceTransferable.WORKSPACE_URI_LIST_FLAVOR)) {
				return handleUriList(event, this, transferable);
			} 
			else 
			if (transferable.isDataFlavorSupported(WorkspaceTransferable.WORKSPACE_NODE_FLAVOR)) {
				return handleWorkspaceNodes(event, this, transferable);
			}
			
		}
		catch (Exception e) {
			return false;
		}
		return false;
	}	
	

	public void nodeCreated(NodeCreatedEvent event) {
		//TODO: propagate other filetypes
		if(event.getCreatedNode().getKey().startsWith(this.getKey())) {
			if(event.getCreatedNode() instanceof MindMapFileNode) {
				URI uri = ((MindMapFileNode)event.getCreatedNode()).getFile().toURI();
				if(!mindmapIndex.contains(uri)) {
					LogUtils.info("DOCEAR: adding new mindmap to library: "+ uri);
					mindmapIndex.add(uri);
				}
			} 
			else
			if(event.getCreatedNode() instanceof LinkTypeFileNode) {
				URI uri = WorkspaceUtils.absoluteURI(((LinkTypeFileNode)event.getCreatedNode()).getLinkPath());
				if(!mindmapIndex.contains(uri)) {
					LogUtils.info("DOCEAR: adding new mindmap to library: "+ uri);
					mindmapIndex.add(uri);
				}				
			}
		}
	}
	
}
