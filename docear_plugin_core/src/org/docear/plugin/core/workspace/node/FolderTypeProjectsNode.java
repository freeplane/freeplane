/**
 * author: Marcel Genzmehr
 * 23.08.2011
 */
package org.docear.plugin.core.workspace.node;

import java.awt.Component;
import java.io.File;
import java.net.URI;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.docear.plugin.core.CoreConfiguration;
import org.docear.plugin.core.workspace.node.config.NodeAttributeObserver;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.WorkspaceUtils;
import org.freeplane.plugin.workspace.controller.IWorkspaceNodeEventListener;
import org.freeplane.plugin.workspace.controller.WorkspaceNodeEvent;
import org.freeplane.plugin.workspace.io.annotation.ExportAsAttribute;
import org.freeplane.plugin.workspace.model.WorkspacePopupMenu;
import org.freeplane.plugin.workspace.model.node.AFolderNode;
import org.freeplane.plugin.workspace.model.node.AWorkspaceTreeNode;


public class FolderTypeProjectsNode extends AFolderNode implements IWorkspaceNodeEventListener, FileAlterationListener, ChangeListener {

	private static final long serialVersionUID = 1L;
	private static final Icon DEFAULT_ICON = new ImageIcon(FolderTypeLibraryNode.class.getResource("/images/project-open-2.png"));
	private boolean doMonitoring = false;
	private URI pathURI = null;
	private boolean locked = false;
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	
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
			createIfNeeded(getPath());
			enableMonitoring(true);
		} 
		else {
			this.pathURI = uri;
			createIfNeeded(getPath());
		}		
		CoreConfiguration.projectPathObserver.setValue(WorkspaceUtils.resolveURI(uri).getPath());
		locked = false;
	}
	
	private void createIfNeeded(URI uri) {
		File file = WorkspaceUtils.resolveURI(uri);
		if (file != null && !file.exists()) {
			file.mkdirs();			
		}
	}
	
	@ExportAsAttribute("path")
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
	
	@ExportAsAttribute("monitor")
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
		
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	
	public void handleEvent(WorkspaceNodeEvent event) {
		if (event.getType() == WorkspaceNodeEvent.MOUSE_RIGHT_CLICK) {
			showPopup((Component) event.getBaggage(), event.getX(), event.getY());
		}
	}

	public void onStart(FileAlterationObserver observer) {
		// called when the observer starts a check cycle. do nth so far. 
	}

	public void onDirectoryCreate(File directory) {
		// FIXME: don't do refresh, because it always scans the complete directory. instead, implement single node insertion.
		System.out.println("onDirectoryCreate: " + directory);
		refresh();
	}

	public void onDirectoryChange(File directory) {
		// FIXME: don't do refresh, because it always scans the complete directory. instead, implement single node change.
		System.out.println("onDirectoryChange: " + directory);
		refresh();
	}

	public void onDirectoryDelete(File directory) {
		// FIXME: don't do refresh, because it always scans the complete directory. instead, implement single node remove.
		System.out.println("onDirectoryDelete: " + directory);
		refresh();
	}

	public void onFileCreate(File file) {
		// FIXME: don't do refresh, because it always scans the complete directory. instead, implement single node insertion.
		System.out.println("onFileCreate: " + file);
		refresh();
	}

	public void onFileChange(File file) {
		// FIXME: don't do refresh, because it always scans the complete directory. instead, implement single node change.
		System.out.println("onFileChange: " + file);
		refresh();
	}

	public void onFileDelete(File file) {
		// FIXME: don't do refresh, because it always scans the complete directory. instead, implement single node remove.
		System.out.println("onFileDelete: " + file);
		refresh();
	}

	public void onStop(FileAlterationObserver observer) {
		// called when the observer ends a check cycle. do nth so far.
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
	
	public WorkspacePopupMenu getContextMenu() {
		return null;
	}

	public void stateChanged(ChangeEvent e) {
		if(!locked && e.getSource() instanceof NodeAttributeObserver) {
			String path = (String) ((NodeAttributeObserver) e.getSource()).getValue();
			URI uri;
			try{
				uri = URI.create(path);
			}
			catch (Exception ex) {
				return;
			}
			this.setPath(uri);
		}
		
	}

	
}
