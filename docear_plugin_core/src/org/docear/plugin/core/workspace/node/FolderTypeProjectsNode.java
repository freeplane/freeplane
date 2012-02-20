/**
 * author: Marcel Genzmehr
 * 23.08.2011
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
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.docear.plugin.core.CoreConfiguration;
import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.event.DocearEvent;
import org.docear.plugin.core.event.DocearEventType;
import org.docear.plugin.core.workspace.actions.DocearProjectEnableMonitoringAction;
import org.docear.plugin.core.workspace.node.config.NodeAttributeObserver;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.WorkspaceUtils;
import org.freeplane.plugin.workspace.components.menu.WorkspacePopupMenu;
import org.freeplane.plugin.workspace.components.menu.WorkspacePopupMenuBuilder;
import org.freeplane.plugin.workspace.dnd.IDropAcceptor;
import org.freeplane.plugin.workspace.dnd.WorkspaceTransferable;
import org.freeplane.plugin.workspace.event.IWorkspaceNodeActionListener;
import org.freeplane.plugin.workspace.event.WorkspaceActionEvent;
import org.freeplane.plugin.workspace.io.IFileSystemRepresentation;
import org.freeplane.plugin.workspace.io.annotation.ExportAsAttribute;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;
import org.freeplane.plugin.workspace.nodes.AFolderNode;
import org.freeplane.plugin.workspace.nodes.DefaultFileNode;
import org.freeplane.plugin.workspace.nodes.LinkTypeFileNode;


public class FolderTypeProjectsNode extends AFolderNode implements IWorkspaceNodeActionListener, FileAlterationListener, ChangeListener, IDropAcceptor, IFileSystemRepresentation {

	private static final long serialVersionUID = 1L;
	private static final Icon DEFAULT_ICON = new ImageIcon(FolderTypeLibraryNode.class.getResource("/images/project-open-2.png"));
	private boolean doMonitoring = false;
	private URI pathURI = null;
	private boolean locked = false;
	private static WorkspacePopupMenu popupMenu = null;
	private boolean first = true;
	private boolean fsChanges = false;
	private boolean orderDescending;
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	
	public FolderTypeProjectsNode() {
		this("projects");
	}
	
	public FolderTypeProjectsNode(String type) {
		super(type);
		CoreConfiguration.projectPathObserver.addChangeListener(this);
	}
	
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	public void setPath(URI uri) {
		locked = true;
		if(isMonitoring()) {
			enableMonitoring(false);
			this.pathURI = uri;
			if (uri != null) {
				createPathIfNeeded(getPath());
				first  = true;
				enableMonitoring(true);
			}
		} 
		else {
			this.pathURI = uri;
			createPathIfNeeded(getPath());
		}		
		CoreConfiguration.projectPathObserver.setUri(uri);
		DocearEvent event = new DocearEvent(this, DocearEventType.LIBRARY_NEW_PROJECT_INDEXING_REQUEST, this);
		DocearController.getController().dispatchDocearEvent(event);
		locked = false;
	}
	
	@ExportAsAttribute(name="path")
	public URI getPath() {
		return this.pathURI;		
	}
	
	public void enableMonitoring(boolean enable) {
		if(getPath() == null) {
			this.doMonitoring = enable;
		} 
		else {
			File file = WorkspaceUtils.resolveURI(getPath());
			if(enable != this.doMonitoring) {
				this.doMonitoring = enable;
				if(file == null) {
					return;
				}
				try {		
					if(enable) {					
						WorkspaceController.getController().getFileSystemAlterationMonitor().addFileSystemListener(file, this);
					}
					else {
						WorkspaceController.getController().getFileSystemAlterationMonitor().removeFileSystemListener(file, this);
					}
				} 
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	@ExportAsAttribute(name="monitor")
	public boolean isMonitoring() {
		return this.doMonitoring;
	}
	
	public boolean setIcons(DefaultTreeCellRenderer renderer) {
		renderer.setOpenIcon(DEFAULT_ICON);
		renderer.setClosedIcon(DEFAULT_ICON);
		renderer.setLeafIcon(DEFAULT_ICON);
		return true;
	}
	
	public void disassociateReferences()  {
		CoreConfiguration.projectPathObserver.removeChangeListener(this);
	}

	protected AWorkspaceTreeNode clone(FolderTypeProjectsNode node) {
		node.setPath(getPath());
		node.enableMonitoring(isMonitoring());
		return super.clone(node);
	}
	
	private void processWorkspaceNodeDrop(List<AWorkspaceTreeNode> nodes, int dropAction) {
		try {	
			File targetDir = WorkspaceUtils.resolveURI(getPath());
			for(AWorkspaceTreeNode node : nodes) {
				if(node instanceof DefaultFileNode) {					
					if(targetDir != null && targetDir.isDirectory()) {
						if(dropAction == DnDConstants.ACTION_COPY) {
							((DefaultFileNode) node).copyTo(targetDir);
						} 
						else if(dropAction == DnDConstants.ACTION_MOVE) {
							((DefaultFileNode) node).moveTo(targetDir);
							AWorkspaceTreeNode parent = node.getParent();
							WorkspaceUtils.getModel().removeNodeFromParent(node);
							parent.refresh();
						}
					}
				}
				else if(node instanceof LinkTypeFileNode) {
					File srcFile = WorkspaceUtils.resolveURI(((LinkTypeFileNode) node).getLinkPath());
					if(targetDir != null && targetDir.isDirectory()) {
						FileUtils.copyFileToDirectory(srcFile, targetDir);
						if(dropAction == DnDConstants.ACTION_MOVE) {
							AWorkspaceTreeNode parent = node.getParent();
							WorkspaceUtils.getModel().removeNodeFromParent(node);
							parent.refresh();
						}
					}
				}
			}
			refresh();
		}
		catch (Exception e) {
			LogUtils.warn(e);
		}	
	}
	
	private void processFileListDrop(List<File> files, int dropAction) {
		try {
			File targetDir = WorkspaceUtils.resolveURI(getPath());			
			for(File srcFile : files) {
				if(srcFile.isDirectory()) {
					FileUtils.copyDirectoryToDirectory(srcFile, targetDir);
				}
				else {
					FileUtils.copyFileToDirectory(srcFile, targetDir, true);
				}				
			}
			refresh();
		}
		catch (Exception e) {
			LogUtils.warn(e);
		}
		refresh();
	}
	
	private void processUriListDrop(List<URI> uris, int dropAction) {
		try {
			File targetDir = WorkspaceUtils.resolveURI(getPath());			
			for(URI uri : uris) {
				File srcFile = new File(uri);
				if(srcFile == null || !srcFile.exists()) {
					continue;
				}
				if(srcFile.isDirectory()) {
					FileUtils.copyDirectoryToDirectory(srcFile, targetDir);
				}
				else {
					FileUtils.copyFileToDirectory(srcFile, targetDir, true);
				}				
			}
			refresh();
		}
		catch (Exception e) {
			LogUtils.warn(e);
		}
		refresh();
		
	}
		
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	
	public void handleAction(WorkspaceActionEvent event) {
		if (event.getType() == WorkspaceActionEvent.MOUSE_RIGHT_CLICK) {
			showPopup((Component) event.getBaggage(), event.getX(), event.getY());
		}
	}

	public void onStart(FileAlterationObserver observer) {
		fsChanges = false;
		if(first ) return;
		// called when the observer starts a check cycle. do nth so far. 
	}

	public void onDirectoryCreate(File directory) {
		if(first) return;
		fsChanges = true;
	}

	public void onDirectoryChange(File directory) {
		if(first) return;
		fsChanges = true;
	}

	public void onDirectoryDelete(File directory) {
		if(first) return;
		fsChanges = true;
	}

	public void onFileCreate(File file) {
		if(first) return;
		fsChanges = true;
	}

	public void onFileChange(File file) {
		if(first) return;
		fsChanges = true;
	}

	public void onFileDelete(File file) {
		if(first) return;
		fsChanges = true;	
	}

	public void onStop(FileAlterationObserver observer) {
		if(!first && fsChanges) {
			fsChanges=false;
			refresh();
		}
		first = false;
	}

	
	public void refresh() {
		try {
			File file = WorkspaceUtils.resolveURI(getPath());
			if (file != null) {
				WorkspaceUtils.getModel().removeAllElements(this);
				WorkspaceController.getController().getFilesystemMgr().scanFileSystem(this, file);
				WorkspaceUtils.getModel().reload(this);
				WorkspaceController.getController().getExpansionStateHandler().restoreExpansionStates();
			}			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public AWorkspaceTreeNode clone() {
		FolderTypeProjectsNode node = new FolderTypeProjectsNode(getType());
		return clone(node);
	}

	public void initializePopup() {
		if (popupMenu == null) {
			ModeController modeController = Controller.getCurrentModeController();
			modeController.addAction(new DocearProjectEnableMonitoringAction());
			
			popupMenu = new WorkspacePopupMenu();
			WorkspacePopupMenuBuilder.addActions(popupMenu, new String[] {
					WorkspacePopupMenuBuilder.createSubMenu(TextUtils.getRawText("workspace.action.new.label")),
					"workspace.action.node.new.folder",
					"workspace.action.file.new.mindmap",
					//WorkspacePopupMenuBuilder.SEPARATOR,
					//"workspace.action.file.new.file",
					WorkspacePopupMenuBuilder.endSubMenu(),
					WorkspacePopupMenuBuilder.SEPARATOR,
					"workspace.action.docear.uri.change",
					WorkspacePopupMenuBuilder.SEPARATOR,
					"workspace.action.node.cut",
					"workspace.action.node.copy",						
					"workspace.action.node.paste",
					WorkspacePopupMenuBuilder.SEPARATOR,
					"workspace.action.node.rename",
					"workspace.action.node.remove",
					"workspace.action.file.delete",
					WorkspacePopupMenuBuilder.SEPARATOR,
					"workspace.action.node.physical.sort",
					WorkspacePopupMenuBuilder.SEPARATOR,					
					"workspace.action.docear.project.enable.monitoring",
					"workspace.action.node.refresh"	
			});
		}
		
	}	
	
	public WorkspacePopupMenu getContextMenu() {
		if (popupMenu == null) {
			initializePopup();
		}
		return popupMenu;
	}
	
	private void createPathIfNeeded(URI uri) {
		File file = WorkspaceUtils.resolveURI(uri);

		if (file != null) {
			if (!file.exists()) {
				if (file.mkdirs()) {
					
				}
			}
		}
		else {
			LogUtils.warn("no project folder selected!");
		}

		
	}

	public void stateChanged(ChangeEvent e) {
		if(!locked && e.getSource() instanceof NodeAttributeObserver) {			
			URI uri = ((NodeAttributeObserver) e.getSource()).getUri();
			this.setPath(uri);
			this.refresh();
		}
		
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
	
	public File getFile() {
		return WorkspaceUtils.resolveURI(this.getPath());
	}
	
	public void orderDescending(boolean enable) {
		this.orderDescending = enable;
	}

	@ExportAsAttribute(name="orderDescending")
	public boolean orderDescending() {
		return orderDescending;
	}
}
