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
import javax.swing.tree.DefaultTreeCellRenderer;

import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.WorkspaceUtils;
import org.freeplane.plugin.workspace.config.node.AFolderNode;
import org.freeplane.plugin.workspace.controller.IWorkspaceNodeEventListener;
import org.freeplane.plugin.workspace.controller.WorkspaceNodeEvent;
import org.freeplane.plugin.workspace.io.annotation.ExportAsAttribute;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;
import org.freeplane.plugin.workspace.model.WorkspacePopupMenu;


public class FolderTypeProjectsNode extends AFolderNode implements IWorkspaceNodeEventListener, FileAlterationListener {

	private static final long serialVersionUID = 1L;
	private static final Icon DEFAULT_ICON = new ImageIcon(FolderTypeLibraryNode.class.getResource("/images/project-open-2.png"));
	private boolean doMonitoring = false;
	private URI pathURI = null;
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	
	public FolderTypeProjectsNode(String type) {
		super(type);	
	}
	
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	public void setPathURI(URI uri) {
		if(isMonitoring()) {
			enableMonitoring(false);
			this.pathURI = uri;
			enableMonitoring(true);
		} 
		else {
			this.pathURI = uri;
		}		
	}
	
	@ExportAsAttribute("path")
	public URI getPathURI() {
		return this.pathURI;		
	}
	
	public void enableMonitoring(boolean enable) {
		File file = WorkspaceUtils.resolveURI(getPathURI());
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
	

	protected AWorkspaceTreeNode clone(FolderTypeProjectsNode node) {
		node.setPathURI(getPathURI());
		node.enableMonitoring(isMonitoring());
		return super.clone(node);
	}
		
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	
	/**
	 * {@inheritDoc}
	 */
	public void handleEvent(WorkspaceNodeEvent event) {
		if (event.getType() == WorkspaceNodeEvent.MOUSE_RIGHT_CLICK) {
			showPopup((Component) event.getBaggage(), event.getX(), event.getY());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void onStart(FileAlterationObserver observer) {
		// TODO Auto-generated method stub
		//System.out.println("checking " + observer.getDirectory());
	}

	/**
	 * {@inheritDoc}
	 */
	public void onDirectoryCreate(File directory) {
		// TODO Auto-generated method stub
		System.out.println("onDirectoryCreate: " + directory);
		refresh();
	}

	/**
	 * {@inheritDoc}
	 */
	public void onDirectoryChange(File directory) {
		// TODO Auto-generated method stub
		System.out.println("onDirectoryChange: " + directory);
		refresh();
	}

	/**
	 * {@inheritDoc}
	 */
	public void onDirectoryDelete(File directory) {
		// TODO Auto-generated method stub
		System.out.println("onDirectoryDelete: " + directory);
		refresh();
	}

	/**
	 * {@inheritDoc}
	 */
	public void onFileCreate(File file) {
		// TODO Auto-generated method stub
		System.out.println("onFileCreate: " + file);
		refresh();
	}

	/**
	 * {@inheritDoc}
	 */
	public void onFileChange(File file) {
		// TODO Auto-generated method stub
		System.out.println("onFileChange: " + file);
		refresh();
	}

	/**
	 * {@inheritDoc}
	 */
	public void onFileDelete(File file) {
		// TODO Auto-generated method stub
		System.out.println("onFileDelete: " + file);
		refresh();
	}

	/**
	 * {@inheritDoc}
	 */
	public void onStop(FileAlterationObserver observer) {
		// TODO Auto-generated method stub
		//System.out.println("onStop: " + observer.getDirectory());	
	}

	
	public void refresh() {
		try {
			File file = WorkspaceUtils.resolveURI(getPathURI());
			if (file != null) {
				WorkspaceUtils.getModel().removeAllElements(this);
				WorkspaceController.getController().getFilesystemReader().scanFileSystem(this, file);
				WorkspaceUtils.getModel().reload(this);
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

	
}
