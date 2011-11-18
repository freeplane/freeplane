package org.freeplane.plugin.workspace.config.node;

import java.awt.Component;
import java.io.File;
import java.net.URI;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.WorkspaceUtils;
import org.freeplane.plugin.workspace.controller.IWorkspaceNodeEventListener;
import org.freeplane.plugin.workspace.controller.WorkspaceNodeEvent;
import org.freeplane.plugin.workspace.io.action.FileNodeNewDirectoryAction;
import org.freeplane.plugin.workspace.io.action.FileNodeNewFileAction;
import org.freeplane.plugin.workspace.io.action.FileNodeNewMindmapAction;
import org.freeplane.plugin.workspace.io.annotation.ExportAsAttribute;
import org.freeplane.plugin.workspace.model.WorkspacePopupMenu;
import org.freeplane.plugin.workspace.model.WorkspacePopupMenuBuilder;
import org.freeplane.plugin.workspace.model.node.AFolderNode;
import org.freeplane.plugin.workspace.model.node.AWorkspaceTreeNode;

public class PhysicalFolderNode extends AFolderNode implements IWorkspaceNodeEventListener, FileAlterationListener {
	
	private static final long serialVersionUID = 1L;
	private static Icon FOLDER_OPEN_ICON = new ImageIcon(PhysicalFolderNode.class.getResource("/images/16x16/folder-orange_open.png"));
	private static final Icon FOLDER_CLOSED_ICON = new ImageIcon(PhysicalFolderNode.class.getResource("/images/16x16/folder-orange.png"));
	
	private static WorkspacePopupMenu popupMenu = null;
	
	private URI folderPath;
	private boolean doMonitoring = false;
	private boolean first;
	
	public PhysicalFolderNode() {
		this(AFolderNode.FOLDER_TYPE_PHYSICAL);
	}

	public PhysicalFolderNode(String id) {
		super(id);
	}

	@ExportAsAttribute("path")
	public URI getPath() {
		return folderPath;
	}

	public void setPath(URI uri) {
		if(isMonitoring()) {
			enableMonitoring(false);
			this.folderPath = uri;
			createIfNeeded(getPath());
			enableMonitoring(true);
		} 
		else {
			this.folderPath = uri;
			createIfNeeded(getPath());
		}		
	}
	
	private void createIfNeeded(URI uri) {
		File file = WorkspaceUtils.resolveURI(uri);
		if (file != null && !file.exists()) {
			file.mkdirs();			
		}
	}

	public void initializePopup() {
		if (popupMenu == null) {
			ModeController modeController = Controller.getCurrentModeController();
			modeController.addAction(new FileNodeNewDirectoryAction());
			modeController.addAction(new FileNodeNewMindmapAction());
			modeController.addAction(new FileNodeNewFileAction());
			
			if (popupMenu == null) {			
				popupMenu = new WorkspacePopupMenu();
				WorkspacePopupMenuBuilder.addActions(popupMenu, new String[] {
						WorkspacePopupMenuBuilder.createSubMenu(TextUtils.getRawText("workspace.action.new.label")),
						"workspace.action.file.new.directory",
						"workspace.action.file.new.mindmap",
						//WorkspacePopupMenuBuilder.SEPARATOR,
						//"workspace.action.file.new.file",
						WorkspacePopupMenuBuilder.endSubMenu(),
						WorkspacePopupMenuBuilder.SEPARATOR,
						"workspace.action.node.enable.monitoring",
						WorkspacePopupMenuBuilder.SEPARATOR,						
						"workspace.action.node.paste",
						"workspace.action.node.copy",
						"workspace.action.node.cut",
						WorkspacePopupMenuBuilder.SEPARATOR,
						"workspace.action.node.rename",
						WorkspacePopupMenuBuilder.SEPARATOR,
						"workspace.action.node.refresh",
						"workspace.action.node.delete"		
				});
			}
		}
	}
	
	public void enableMonitoring(boolean enable) {
		if(getPath() == null) {
			this.doMonitoring = enable;
		} 
		else {
			File file = WorkspaceUtils.resolveURI(getPath());
			if(enable != this.doMonitoring) {
				this.doMonitoring = enable;
				first = true;
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
	
	@ExportAsAttribute("monitor")
	public boolean isMonitoring() {
		return this.doMonitoring;
	}

	public void handleEvent(WorkspaceNodeEvent event) {
		if (event.getType() == WorkspaceNodeEvent.MOUSE_RIGHT_CLICK) {
			showPopup( (Component) event.getBaggage(), event.getX(), event.getY());
		}
	}
	
	public boolean setIcons(DefaultTreeCellRenderer renderer) {
		renderer.setOpenIcon(FOLDER_OPEN_ICON);
		renderer.setClosedIcon(FOLDER_CLOSED_ICON);
		renderer.setLeafIcon(FOLDER_CLOSED_ICON);
		return true;
	}

	public String toString() {
		return this.getClass().getSimpleName() + "[id=" + this.getId() + ";name=" + this.getName() + ";path="
				+ this.getPath() + "]";
	}

	public void refresh() {
		File folder;
		try {
			folder = WorkspaceUtils.resolveURI(getPath());
			if (folder.isDirectory()) {
				WorkspaceUtils.getModel().removeAllElements(this);
				WorkspaceController.getController().getFilesystemMgr().scanFileSystem(this, folder);
				WorkspaceUtils.getModel().reload(this);
				WorkspaceController.getController().getExpansionStateHandler().restoreExpansionStates();				
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}		
	}
	
	protected AWorkspaceTreeNode clone(PhysicalFolderNode node) {		
		node.setPath(getPath());		
		return super.clone(node);
	}
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/

	public void onStart(FileAlterationObserver observer) {
		if(first ) return;
		// called when the observer starts a check cycle. do nth so far. 
	}

	public void onDirectoryCreate(File directory) {
		if(first) return;
		// FIXME: don't do refresh, because it always scans the complete directory. instead, implement single node insertion.
		System.out.println("onDirectoryCreate: " + directory);
		refresh();
	}

	public void onDirectoryChange(File directory) {
		if(first) return;
		// FIXME: don't do refresh, because it always scans the complete directory. instead, implement single node change.
		System.out.println("onDirectoryChange: " + directory);
		refresh();
	}

	public void onDirectoryDelete(File directory) {
		if(first) return;
		// FIXME: don't do refresh, because it always scans the complete directory. instead, implement single node remove.
		System.out.println("onDirectoryDelete: " + directory);
		refresh();
	}

	public void onFileCreate(File file) {
		if(first) return;
		// FIXME: don't do refresh, because it always scans the complete directory. instead, implement single node insertion.
		System.out.println("onFileCreate: " + file);
		refresh();
	}

	public void onFileChange(File file) {
		if(first) return;
		// FIXME: don't do refresh, because it always scans the complete directory. instead, implement single node change.
		System.out.println("onFileChange: " + file);
		refresh();
	}

	public void onFileDelete(File file) {
		if(first) return;
		// FIXME: don't do refresh, because it always scans the complete directory. instead, implement single node remove.
		System.out.println("onFileDelete: " + file);
		refresh();
	}

	public void onStop(FileAlterationObserver observer) {
		first = false;
	}
	
	public WorkspacePopupMenu getContextMenu() {
		if (popupMenu == null) {
			initializePopup();
		}
		return popupMenu;
	}
	
	public AWorkspaceTreeNode clone() {
		PhysicalFolderNode node = new PhysicalFolderNode(getType());
		return clone(node);
	}
}
