/**
 * author: Marcel Genzmehr
 * 23.08.2011
 */
package org.docear.plugin.core.workspace.node;

import java.awt.Component;
import java.io.File;
import java.net.URI;
import java.util.Iterator;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.freeplane.core.ui.IndexedTree;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.WorkspaceUtils;
import org.freeplane.plugin.workspace.config.node.FolderNode;
import org.freeplane.plugin.workspace.config.node.VirtualFolderNode;
import org.freeplane.plugin.workspace.controller.IWorkspaceNodeEventListener;
import org.freeplane.plugin.workspace.controller.WorkspaceNodeEvent;
import org.freeplane.plugin.workspace.io.annotation.ExportAsAttribute;

/**
 * 
 */
public class FolderTypeProjectsNode extends FolderNode implements IWorkspaceNodeEventListener, FileAlterationListener {
	private static final Icon DEFAULT_ICON = new ImageIcon(FolderTypeLibraryNode.class.getResource("/images/project-open-2.png"));
	private boolean doMonitoring = false;
	private URI pathURI = null;
	private final FileAlterationMonitor monitor;
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	
	public FolderTypeProjectsNode(String type) {
		super(type);
		monitor = new FileAlterationMonitor(10000);
		try {
			monitor.start();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		// TODO Auto-generated constructor stub
	}
	
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	public void setPathURI(URI uri) {
		this.pathURI = uri;
		File file = WorkspaceUtils.resolveURI(uri);
		setObserver(file);
	}
	
	@ExportAsAttribute("path")
	public URI getPathURI() {
		return this.pathURI;		
	}
	
	public void enableMonitoring(boolean enable) {
		this.doMonitoring = enable;
		try {		
			if(enable) {
				monitor.start();
				
			}
			else {
				monitor.stop();
			}
		} 
		catch (Exception e) {
			e.printStackTrace();
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
	
	private void setObserver(File directory) {
		Iterator<FileAlterationObserver> observers = monitor.getObservers().iterator();
		while(observers.hasNext()) {
			observers.next();
			observers.remove();
		}
		if(directory.exists() && directory.isDirectory()) {
			FileAlterationObserver observer = new FileAlterationObserver(directory);
			observer.addListener(this);
			monitor.addObserver(observer);
		}
		
	}
	
	private void rescanFolder() {
		IndexedTree indexTree = WorkspaceController.getController().getIndexTree();
		String key = (String) indexTree.getKeyByUserObject(this);
		if(key == null) {
			//FIXME: DOCEAR> remove this node from "Controller.getCurrentController().getResourceController().removePropertyChangeListener(this);" !!!ConcurrentModificationException
			return;
		}
		final DefaultMutableTreeNode node = indexTree.get(key);
		try {
			File file = WorkspaceUtils.resolveURI(getPathURI());
			if (file != null) {
				WorkspaceController.getController().getIndexTree().removeChildElements(key);
				WorkspaceController.getController().getFilesystemReader().scanFilesystem(key, file);
				WorkspaceController.getController().getViewModel().reload(node);
			}			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	
	/**
	 * {@inheritDoc}
	 */
	public void handleEvent(WorkspaceNodeEvent event) {
		if (event.getType() == WorkspaceNodeEvent.MOUSE_RIGHT_CLICK) {			
			Component component = (Component) event.getSource();

			WorkspaceController.getController().getPopups()
					.showPopup(VirtualFolderNode.POPUP_KEY, component, event.getX(), event.getY());

		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void onStart(FileAlterationObserver observer) {
		// TODO Auto-generated method stub
		//System.out.println("onStart: " + observer);
	}

	/**
	 * {@inheritDoc}
	 */
	public void onDirectoryCreate(File directory) {
		// TODO Auto-generated method stub
		System.out.println("onDirectoryCreate: " + directory);		
	}

	/**
	 * {@inheritDoc}
	 */
	public void onDirectoryChange(File directory) {
		// TODO Auto-generated method stub
		System.out.println("onDirectoryChange: " + directory);	
	}

	/**
	 * {@inheritDoc}
	 */
	public void onDirectoryDelete(File directory) {
		// TODO Auto-generated method stub
		System.out.println("onDirectoryDelete: " + directory);	
	}

	/**
	 * {@inheritDoc}
	 */
	public void onFileCreate(File file) {
		// TODO Auto-generated method stub
		System.out.println("onFileCreate: " + file);
		rescanFolder();
	}

	/**
	 * {@inheritDoc}
	 */
	public void onFileChange(File file) {
		// TODO Auto-generated method stub
		System.out.println("onFileChange: " + file);	
	}

	/**
	 * {@inheritDoc}
	 */
	public void onFileDelete(File file) {
		// TODO Auto-generated method stub
		System.out.println("onFileDelete: " + file);	
	}

	/**
	 * {@inheritDoc}
	 */
	public void onStop(FileAlterationObserver observer) {
		// TODO Auto-generated method stub
		//System.out.println("onStop: " + observer);	
	}
}
