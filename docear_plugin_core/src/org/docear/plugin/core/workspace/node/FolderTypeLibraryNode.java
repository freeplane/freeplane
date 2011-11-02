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
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeNode;

import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.IBibtexDatabase;
import org.docear.plugin.core.IDocearLibrary;
import org.docear.plugin.core.event.DocearEvent;
import org.docear.plugin.core.event.DocearEventType;
import org.docear.plugin.core.event.IDocearEventListener;
import org.freeplane.core.ui.IndexedTree;
import org.freeplane.core.util.Compat;
import org.freeplane.core.util.LogUtils;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.WorkspaceUtils;
import org.freeplane.plugin.workspace.config.node.AWorkspaceNode;
import org.freeplane.plugin.workspace.config.node.FolderNode;
import org.freeplane.plugin.workspace.config.node.LinkTypeFileNode;
import org.freeplane.plugin.workspace.config.node.PhysicalFolderNode;
import org.freeplane.plugin.workspace.config.node.VirtualFolderNode;
import org.freeplane.plugin.workspace.controller.IWorkspaceNodeEventListener;
import org.freeplane.plugin.workspace.controller.WorkspaceNodeEvent;
import org.freeplane.plugin.workspace.dnd.IDropAcceptor;
import org.freeplane.plugin.workspace.dnd.WorkspaceTransferable;
import org.freeplane.plugin.workspace.io.INodeCreatedListener;
import org.freeplane.plugin.workspace.io.NodeCreatedEvent;
import org.freeplane.plugin.workspace.io.node.MindMapFileNode;

public class FolderTypeLibraryNode extends FolderNode implements IDocearEventListener, IDocearLibrary, IWorkspaceNodeEventListener, IDropAcceptor, INodeCreatedListener {
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
	private void handleFileDrop(DefaultMutableTreeNode targetNode, final File file) {
		if(file.isDirectory()) {
			WorkspaceUtils.createPhysicalFolderNode(file, targetNode);
		}
		else {
			WorkspaceUtils.createLinkTypeFileNode(file, targetNode);
		}
		WorkspaceController.getController().reloadWorkspace();
	}
	
	@SuppressWarnings("rawtypes")
	private void appendNodeRecursive(IndexedTree.Node targetNode, IndexedTree.Node node) {
		String key = ((IndexedTree.Node)targetNode).getKey().toString();
		AWorkspaceNode nodeObject = (AWorkspaceNode)node.getUserObject();
		IndexedTree.Node newtarget = (IndexedTree.Node) WorkspaceController.getController().getIndexTree().addElement(key, nodeObject, key+"/"+nodeObject.getId(), IndexedTree.AS_CHILD);
		if(nodeObject instanceof VirtualFolderNode) {
			Enumeration children = node.children();
			while(children.hasMoreElements()) {
				appendNodeRecursive(newtarget, (IndexedTree.Node)children.nextElement());
			}
		} 
		else 
		if(nodeObject instanceof PhysicalFolderNode) {
			PhysicalFolderNode phyNode = (PhysicalFolderNode) nodeObject;
			WorkspaceController.getController().getFilesystemReader()
					.scanFilesystem(phyNode, WorkspaceUtils.resolveURI(phyNode.getFolderPath()));			
		} 
		else 
		if(nodeObject instanceof FolderNode) {
			FolderNode dirNode = (FolderNode) nodeObject;
			dirNode.refresh();
		}
		
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
	private boolean handleWorkspaceNodes(DropTargetDropEvent event, DefaultMutableTreeNode targetNode, Transferable transferable)
			throws UnsupportedFlavorException, IOException {
		event.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
		System.out.println();
		List<IndexedTree.Node> nodeList = (List<IndexedTree.Node>)transferable.getTransferData(WorkspaceTransferable.WORKSPACE_NODE_FLAVOR);
		for(IndexedTree.Node node : nodeList) {
			if(event.getDropAction() == DnDConstants.ACTION_MOVE) {
				System.out.println();
				TreeNode parent = node.getParent();
				targetNode.add(node);
				WorkspaceController.getController().getViewModel().reload(parent);
			} 
			else 
			if(event.getDropAction() == DnDConstants.ACTION_COPY) {
				appendNodeRecursive((IndexedTree.Node) targetNode, node);				
			}
		}
		WorkspaceController.getController().getViewModel().reload(targetNode);
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
	private boolean handleUriList(DropTargetDropEvent event, DefaultMutableTreeNode targetNode, Transferable transferable)
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
	private boolean handleFileList(DropTargetDropEvent event, DefaultMutableTreeNode targetNode, Transferable transferable)
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
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	
	public void handleEvent(DocearEvent event) {
		if(event.getType() == DocearEventType.LIBRARY_NEW_MINDMAP_INDEXING_REQUEST) {
			if(event.getEventObject() instanceof URI) {
				URI uri = (URI) event.getEventObject();
				if(!mindmapIndex.contains(uri)) {
					LogUtils.info("DOCEAR: adding new mindmap to library: "+ uri);
					mindmapIndex.add(uri);
				}
			}			
		}
		if(event.getType() == DocearEventType.LIBRARY_NEW_REFERENCES_INDEXING_REQUEST) {
			if(event.getEventObject() instanceof IBibtexDatabase) {
				if(!referencesIndex.contains((IBibtexDatabase) event.getEventObject())) {
					LogUtils.info("DOCEAR: adding new reference database to library: "+ event.getEventObject());
					referencesIndex.add((IBibtexDatabase) event.getEventObject());
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
			DefaultMutableTreeNode targetNode = WorkspaceController.getController().getIndexTree().get(getKey());
			Transferable transferable = event.getTransferable();
			if (transferable.isDataFlavorSupported(WorkspaceTransferable.WORKSPACE_FILE_LIST_FLAVOR)) {
				return handleFileList(event, targetNode, transferable);
			} 
			else
			if (transferable.isDataFlavorSupported(WorkspaceTransferable.WORKSPACE_URI_LIST_FLAVOR)) {
				return handleUriList(event, targetNode, transferable);
			} 
			else 
			if (transferable.isDataFlavorSupported(WorkspaceTransferable.WORKSPACE_NODE_FLAVOR)) {
				return handleWorkspaceNodes(event, targetNode, transferable);
			}
			
		}
		catch (Exception e) {
			return false;
		}
		return false;
	}
	
	

	public void nodeCreated(NodeCreatedEvent event) {
		//TODO: propagate other filetypes
		System.out.println("");
		if(event.getTargetKey().toString().startsWith(getKey().toString())) {
			DefaultMutableTreeNode node = WorkspaceController.getController().getIndexTree().get(event.getNewKey().toString());
			if(node.getUserObject() instanceof MindMapFileNode) {
				URI uri = ((MindMapFileNode)node.getUserObject()).getFile().toURI();
				if(!mindmapIndex.contains(uri)) {
					LogUtils.info("DOCEAR: adding new mindmap to library: "+ uri);
					mindmapIndex.add(uri);
				}
			} 
			else
			if(node.getUserObject() instanceof LinkTypeFileNode) {
				URI uri = WorkspaceUtils.absoluteURI(((LinkTypeFileNode)node.getUserObject()).getLinkPath());
				if(!mindmapIndex.contains(uri)) {
					LogUtils.info("DOCEAR: adding new mindmap to library: "+ uri);
					mindmapIndex.add(uri);
				}				
			}
		}
	}
	
}
