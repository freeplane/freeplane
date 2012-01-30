/**
 * author: Marcel Genzmehr
 * 18.08.2011
 */
package org.docear.plugin.core.workspace.node;

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.IBibtexDatabase;
import org.docear.plugin.core.IDocearLibrary;
import org.docear.plugin.core.event.DocearEvent;
import org.docear.plugin.core.event.DocearEventType;
import org.docear.plugin.core.event.IDocearEventListener;
import org.docear.plugin.core.workspace.actions.DocearLibraryNewMindmap;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.WorkspaceUtils;
import org.freeplane.plugin.workspace.config.node.LinkTypeFileNode;
import org.freeplane.plugin.workspace.config.node.PhysicalFolderNode;
import org.freeplane.plugin.workspace.controller.IWorkspaceNodeEventListener;
import org.freeplane.plugin.workspace.controller.WorkspaceNodeEvent;
import org.freeplane.plugin.workspace.dnd.IDropAcceptor;
import org.freeplane.plugin.workspace.dnd.IWorkspaceTransferableCreator;
import org.freeplane.plugin.workspace.dnd.WorkspaceTransferable;
import org.freeplane.plugin.workspace.io.node.DefaultFileNode;
import org.freeplane.plugin.workspace.io.node.MindMapFileNode;
import org.freeplane.plugin.workspace.model.WorkspacePopupMenu;
import org.freeplane.plugin.workspace.model.WorkspacePopupMenuBuilder;
import org.freeplane.plugin.workspace.model.node.AFolderNode;
import org.freeplane.plugin.workspace.model.node.AWorkspaceTreeNode;

public class FolderTypeLibraryNode extends AFolderNode implements IDocearEventListener, IDocearLibrary, IWorkspaceNodeEventListener, IWorkspaceTransferableCreator, IDropAcceptor, TreeModelListener {
	private static final Icon DEFAULT_ICON = new ImageIcon(FolderTypeLibraryNode.class.getResource("/images/folder-database.png"));

	private static final long serialVersionUID = 1L;
	
	private final static String PLACEHOLDER_PROFILENAME = "@@PROFILENAME@@";
	private static final String DEFAULT_LIBRARY_PATH = "workspace:/"+PLACEHOLDER_PROFILENAME+"/Library";
	private final static Pattern PATTERN = Pattern.compile(PLACEHOLDER_PROFILENAME);
	
	
	private final Vector<URI> mindmapIndex = new Vector<URI>();
	private final Vector<IBibtexDatabase> referencesIndex = new Vector<IBibtexDatabase>();
	private final Set<FolderTypeProjectsNode> projectIndex = new HashSet<FolderTypeProjectsNode>();

	private static WorkspacePopupMenu popupMenu = null;
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	public FolderTypeLibraryNode(String type) {
		super(type);
		DocearEvent event = new DocearEvent(this, DocearEventType.NEW_LIBRARY);
		DocearController.getController().dispatchDocearEvent(event);
		DocearController.getController().addDocearEventListener(this);
		WorkspaceUtils.getModel().addTreeModelListener(this);
	}	
	
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/
	
	public void initializePopup() {
		if (popupMenu  == null) {
			ModeController modeController = Controller.getCurrentModeController();
			modeController.addAction(new DocearLibraryNewMindmap());
			
			popupMenu = new WorkspacePopupMenu();
			
			WorkspacePopupMenuBuilder.addActions(popupMenu, new String[] {WorkspacePopupMenuBuilder.createSubMenu(TextUtils.getRawText("workspace.action.new.label")),
					"workspace.action.node.new.folder",
					"workspace.action.node.new.link",
					"workspace.action.node.new.directory",
					WorkspacePopupMenuBuilder.SEPARATOR,
					"workspace.action.library.new.mindmap",
					WorkspacePopupMenuBuilder.endSubMenu(),
					WorkspacePopupMenuBuilder.SEPARATOR,						
					"workspace.action.node.paste",
					"workspace.action.node.copy",
					"workspace.action.node.cut",
					WorkspacePopupMenuBuilder.SEPARATOR,
					"workspace.action.node.refresh",
					"workspace.action.node.delete"
			});
		}
	}
	
	public boolean setIcons(DefaultTreeCellRenderer renderer) {
		renderer.setOpenIcon(DEFAULT_ICON);
		renderer.setClosedIcon(DEFAULT_ICON);
		renderer.setLeafIcon(DEFAULT_ICON);
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
		referencesIndex.clear();
		if(ref == null) {
			return;
		}
		referencesIndex.add(ref);
		DocearEvent event = new DocearEvent(this, DocearEventType.LIBRARY_CHANGED, ref);
		DocearController.getController().dispatchDocearEvent(event);
	}
	
	protected void addProjectToIndex(FolderTypeProjectsNode project) {
		projectIndex.clear();
		if(project == null) {
			return;
		}
		projectIndex.add(project);
	}
	
	/**
	 * @param file
	 * @return
	 */
	private AWorkspaceTreeNode createFSNodeLinks(File file) {
		AWorkspaceTreeNode node = null;
		if(file.isDirectory()) {
			PhysicalFolderNode pNode = new PhysicalFolderNode();
			pNode.setPath(WorkspaceUtils.getWorkspaceRelativeURI(file));
			node = pNode;
		}
		else {
			LinkTypeFileNode lNode = new LinkTypeFileNode();
			lNode.setLinkPath(WorkspaceUtils.getWorkspaceRelativeURI(file));
			node = lNode;
		}
		node.setName(file.getName());
		return node;
	}

	
	private void processWorkspaceNodeDrop(List<AWorkspaceTreeNode> nodes, int dropAction) {
		try {	
			for(AWorkspaceTreeNode node : nodes) {
				AWorkspaceTreeNode newNode = null;
				if(node instanceof DefaultFileNode) {					
					newNode = createFSNodeLinks(((DefaultFileNode) node).getFile());
				}
				else {
					if(dropAction == DnDConstants.ACTION_COPY) {
						newNode = node.clone();
					} 
					else if (dropAction == DnDConstants.ACTION_MOVE) {
						AWorkspaceTreeNode parent = node.getParent();
						WorkspaceUtils.getModel().removeNodeFromParent(node);
						parent.refresh();
						newNode = node;
					}
				}
				if(newNode == null) {
					continue;
				}
				WorkspaceUtils.getModel().addNodeTo(newNode, this);
			}
			WorkspaceUtils.saveCurrentConfiguration();
			
		}
		catch (Exception e) {
			LogUtils.warn(e);
		}
		refresh();
	}
	
	private void processFileListDrop(List<File> files, int dropAction) {
		try {		
			for(File srcFile : files) {
				WorkspaceUtils.getModel().addNodeTo(createFSNodeLinks(srcFile), this);		
			}
			WorkspaceUtils.saveCurrentConfiguration();
		}
		catch (Exception e) {
			LogUtils.warn(e);
		}
		refresh();
	}
	
	private void processUriListDrop(List<URI> uris, int dropAction) {
		try {			
			for(URI uri : uris) {
				File srcFile = new File(uri);
				if(srcFile == null || !srcFile.exists()) {
					continue;
				}
				WorkspaceUtils.getModel().addNodeTo(createFSNodeLinks(srcFile), this);
			};
			WorkspaceUtils.saveCurrentConfiguration();
		}
		catch (Exception e) {
			LogUtils.warn(e);
		}
		refresh();
		
	}	
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	
	public void handleEvent(DocearEvent event) {
		if(event.getType() == DocearEventType.LIBRARY_NEW_MINDMAP_INDEXING_REQUEST) {
			if(event.getEventObject() instanceof URI) {
				URI uri = (URI) event.getEventObject();
				if(!mindmapIndex.contains(uri)) {
					LogUtils.info("DOCEAR: adding mindmap to library: "+ uri);
					addMindmapToIndex(uri);
				}
			}			
		} 
		else if(event.getType() == DocearEventType.LIBRARY_NEW_REFERENCES_INDEXING_REQUEST) {
			if(event.getEventObject() instanceof IBibtexDatabase) {
				if(!referencesIndex.contains((IBibtexDatabase) event.getEventObject())) {
					LogUtils.info("DOCEAR: adding reference database to library: "+ event.getEventObject());
					addReferenceToIndex((IBibtexDatabase) event.getEventObject());
				}
			}			
		}
		else if(event.getType() == DocearEventType.LIBRARY_EMPTY_MINDMAP_INDEX_REQUEST) {
			mindmapIndex.removeAllElements();			
		} 
		else if(event.getType() == DocearEventType.LIBRARY_NEW_PROJECT_INDEXING_REQUEST) {
			if(event.getEventObject() instanceof FolderTypeProjectsNode) {
				if(!projectIndex.contains((FolderTypeProjectsNode) event.getEventObject())) {
					LogUtils.info("DOCEAR: adding project to library: "+ event.getEventObject());
					addProjectToIndex((FolderTypeProjectsNode) event.getEventObject());
				}
			}			
		}
		
	}
	
	public void handleEvent(WorkspaceNodeEvent event) {
		if (event.getType() == WorkspaceNodeEvent.MOUSE_RIGHT_CLICK) {
			showPopup( (Component) event.getBaggage(), event.getX(), event.getY());
		}
		
	}
	
	public List<URI> getMindmaps() {
		return mindmapIndex;
	}
	
	public Set<FolderTypeProjectsNode> getProjects() {
		return projectIndex;
	}
	
	public List<URI> getProjectPaths() {
		List<URI> uriList= new ArrayList<URI>();
		Iterator<FolderTypeProjectsNode> iter= projectIndex.iterator();
		while(iter.hasNext()) {
			uriList.add(iter.next().getPath());
		}
		return uriList;
	}
	
	public URI getLibraryPath() {		
		Matcher mainMatcher = PATTERN.matcher(DEFAULT_LIBRARY_PATH);
		String ret = mainMatcher.replaceAll(WorkspaceController.getController().getPreferences().getWorkspaceProfileHome());
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
		for(DataFlavor flavor : flavors) {
			if(WorkspaceTransferable.WORKSPACE_FILE_LIST_FLAVOR.equals(flavor)
				|| WorkspaceTransferable.WORKSPACE_URI_LIST_FLAVOR.equals(flavor)
				|| WorkspaceTransferable.WORKSPACE_NODE_FLAVOR.equals(flavor)
			) {
				return true;
			}
		}
		return false;
	}
	
	@SuppressWarnings("unchecked")
	public boolean processDrop(Transferable transferable, int dropAction) {
		try {
			if(transferable.isDataFlavorSupported(WorkspaceTransferable.WORKSPACE_NODE_FLAVOR)) {
				processWorkspaceNodeDrop((List<AWorkspaceTreeNode>) transferable.getTransferData(WorkspaceTransferable.WORKSPACE_NODE_FLAVOR), dropAction);	
			}
			else if(transferable.isDataFlavorSupported(WorkspaceTransferable.WORKSPACE_FILE_LIST_FLAVOR)) {
				processFileListDrop((List<File>) transferable.getTransferData(WorkspaceTransferable.WORKSPACE_FILE_LIST_FLAVOR), dropAction);
			} 
			else if(transferable.isDataFlavorSupported(WorkspaceTransferable.WORKSPACE_URI_LIST_FLAVOR)) {
				ArrayList<URI> uriList = new ArrayList<URI>();
				String uriString = (String) transferable.getTransferData(WorkspaceTransferable.WORKSPACE_URI_LIST_FLAVOR);
				if (!uriString.startsWith("file://")) {
					return false;
				}
				String[] uriArray = uriString.split("\r\n");
				for(String singleUri : uriArray) {
					try {
						uriList.add(URI.create(singleUri));
					}
					catch (Exception e) {
						LogUtils.info("DOCEAR - "+ e.getMessage());
					}
				}
				processUriListDrop(uriList, dropAction);	
			}
		}
		catch (Exception e) {
			LogUtils.warn(e);
		}
		return true;
	}

	
	public boolean processDrop(DropTargetDropEvent event) {
		event.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
		Transferable transferable = event.getTransferable();
		if(processDrop(transferable, event.getDropAction())) {
			event.dropComplete(true);
			return true;
		}
		event.dropComplete(false);
		return false;
	}
	
	
	
	public Transferable getTransferable() {
		return null;
	}
	
	public WorkspacePopupMenu getContextMenu() {
		if (popupMenu == null) {
			initializePopup();
		}
		
		return popupMenu;
	}

	public URI getPath() {
		// this is a virtual folder, no path is needed
		return null;
	}

	public void treeNodesChanged(TreeModelEvent e) {		
	}
	
	public void treeNodesInserted(TreeModelEvent event) {
		if(this.getTreePath().isDescendant(event.getTreePath())) {
			for(Object newNode : event.getChildren()) {
				if(newNode instanceof MindMapFileNode) {
					URI uri = ((MindMapFileNode)newNode).getFile().toURI();
					if(!mindmapIndex.contains(uri)) {
						LogUtils.info("DOCEAR: adding new mindmap to library: "+ uri);
						mindmapIndex.add(uri);
					}
				} 
				else
				if(newNode instanceof LinkTypeFileNode && ((LinkTypeFileNode)newNode).getLinkPath() != null) {
					URI uri = WorkspaceUtils.absoluteURI(((LinkTypeFileNode)newNode).getLinkPath());
					if((new File(uri)).getName().endsWith(".mm") && !mindmapIndex.contains(uri)) {
						LogUtils.info("DOCEAR: adding new mindmap to library: "+ uri);
						mindmapIndex.add(uri);	
					}
				}
			}
		}
	}

	public void treeNodesRemoved(TreeModelEvent event) {
		//TODO: propagate other filetypes
		if(this.getTreePath().isDescendant(event.getTreePath())) {
			for(Object newNode : event.getChildren()) {
				if(newNode instanceof MindMapFileNode) {
					URI uri = ((MindMapFileNode)newNode).getFile().toURI();
					if(mindmapIndex.contains(uri)) {
						LogUtils.info("DOCEAR: mindmap removed from library: "+ uri);
						mindmapIndex.remove(uri);
					}
				} 
				else
				if(newNode instanceof LinkTypeFileNode) {
					URI uri = WorkspaceUtils.absoluteURI(((LinkTypeFileNode)newNode).getLinkPath());
					if((new File(uri)).getName().endsWith(".mm") && mindmapIndex.contains(uri)) {
						LogUtils.info("DOCEAR: mindmap removed from library: "+ uri);
						mindmapIndex.remove(uri);	
					}
				}
			}
		}
		
	}
	

	/* (non-Javadoc)
	 * @see javax.swing.event.TreeModelListener#treeStructureChanged(javax.swing.event.TreeModelEvent)
	 */
	public void treeStructureChanged(TreeModelEvent e) {
		// TODO Auto-generated method stub
		
	}

	
	
	
}
