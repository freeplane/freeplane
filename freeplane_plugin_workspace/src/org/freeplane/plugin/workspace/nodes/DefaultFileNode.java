package org.freeplane.plugin.workspace.nodes;

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTargetDropEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.apache.commons.io.FileUtils;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.Compat;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.mapio.MapIO;
import org.freeplane.features.mode.Controller;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.WorkspaceUtils;
import org.freeplane.plugin.workspace.components.menu.WorkspacePopupMenu;
import org.freeplane.plugin.workspace.components.menu.WorkspacePopupMenuBuilder;
import org.freeplane.plugin.workspace.dnd.IDropAcceptor;
import org.freeplane.plugin.workspace.dnd.IWorkspaceTransferableCreator;
import org.freeplane.plugin.workspace.dnd.WorkspaceTransferable;
import org.freeplane.plugin.workspace.event.IWorkspaceNodeActionListener;
import org.freeplane.plugin.workspace.event.WorkspaceActionEvent;
import org.freeplane.plugin.workspace.io.IFileSystemRepresentation;
import org.freeplane.plugin.workspace.io.annotation.ExportAsAttribute;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;
import org.freeplane.plugin.workspace.model.IMutableLinkNode;

/**
 * 
 */
public class DefaultFileNode extends AWorkspaceTreeNode implements IWorkspaceNodeActionListener, IWorkspaceTransferableCreator, IDropAcceptor, IFileSystemRepresentation, IMutableLinkNode {
	private static final Icon DEFAULT_ICON = new ImageIcon(AWorkspaceTreeNode.class.getResource("/images/16x16/text-x-preview.png"));
	private static final Icon NOT_EXISTING = new ImageIcon(AWorkspaceTreeNode.class.getResource("/images/16x16/cross.png"));
	public static final Icon DOCEAR_ICON = new ImageIcon(ResourceController.class.getResource("/images/docear16.png"));
	public static final Icon FREEPLANE_ICON = new ImageIcon(ResourceController.class.getResource("/images/Freeplane_frame_icon.png"));
	
	
	private static final long serialVersionUID = 1L;
	
	private static WorkspacePopupMenu popupMenu = null;
	
	
	private File file;
	private boolean orderDescending;
	private Icon icon = null;
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	/**
	 * @param name
	 */
	public DefaultFileNode(final String name, final File file) {
		super("physical_file");
		this.setName(name);
		this.file = file;
		icon = WorkspaceController.getController().getNodeTypeIconManager().getIconForNode(this);
	}

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/
	
	public boolean rename(final String name) {
		File newFile = new File(getFile().getParentFile(), name);
		if(getFile().renameTo(newFile)) {
			this.file = newFile;
			icon = WorkspaceController.getController().getNodeTypeIconManager().getIconForNode(this);
			return true;
		}
		return false;
	}
	
	public void delete() {
		getFile().delete();
	}
	
	public void relocateFile(final File parentFolder) {
		File newFile = new File(parentFolder, getName());
		if(newFile.exists()) {
			this.file = newFile;
		}
	}
	
	private void copyFileContent(File source, File destination) {
		try {
			InputStream in = new DataInputStream(new FileInputStream(source));		
			DataOutputStream out = new DataOutputStream(new FileOutputStream(destination));
			byte[] buffer = new byte[1024];
			int len = in.read(buffer);
			while (len != -1) {
				out.write(buffer, 0, len);
				len = in.read(buffer);
			}
			in.close();
			out.close();
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected void copyFileTo(File file, File parentFolder) throws IOException {
		if(parentFolder.isDirectory()) {				
			File target = new File(parentFolder,file.getName());
			if(file.isDirectory()) {
				if(target.mkdir()) {
					for(File child : file.listFiles()) {
						copyFileTo(child, target);
					}
				}
				else {
					throw new IOException("Could not create folder: "+target);
				}
			}
			else {
				if(target.createNewFile()) {
					copyFileContent(file, target);
				}
				else {
					throw new IOException("Could not create file: "+target);
				}
			}
		}
		else {
			throw new IOException("File "+parentFolder+" is no folder");
		}	
	}
	
	/**
	 * @param file
	 * @throws IOException 
	 */
	public void copyTo(File destFile) throws IOException {
		assert(destFile != null);
		
		if(destFile.isDirectory()) {
			if(getFile().isDirectory()) {
				FileUtils.copyDirectoryToDirectory(getFile(), destFile);
			}
			else {
				FileUtils.copyFileToDirectory(getFile(), destFile);
			}
			//copyFileTo(getFile(), getFile());
		} 
		else {
			copyTo(destFile.getParentFile());
		}
		
	
		
	}
	
	public void moveTo(File destFile) throws IOException {
		assert(destFile != null);
		if(destFile.isDirectory()) {
			if(getFile().isDirectory()) {				
				FileUtils.moveDirectoryToDirectory(getFile(), destFile, true);
			}
			else {
				FileUtils.moveFileToDirectory(getFile(), destFile, true);
			}
			//copyFileTo(getFile(), getFile());
		} 
		else {
			copyTo(destFile.getParentFile());
		}
//		copyTo(file);
//		if(!file.delete()) throw new IOException("Could not delete File "+ file);
	}
	
	public boolean isEditable() {
		return false;
	}
	
	public boolean setIcons(DefaultTreeCellRenderer renderer) {
		if(!getFile().exists()) {
			renderer.setLeafIcon(NOT_EXISTING);
			renderer.setOpenIcon(NOT_EXISTING);
			renderer.setClosedIcon(NOT_EXISTING);
			return true;
		}		
		
		if(icon == null) {
			icon = FileSystemView.getFileSystemView().getSystemIcon(getFile());
			renderer.setLeafIcon(icon);
			return true;
		}
		// the next steps should not get reached
		if(icon == null) {
			renderer.setLeafIcon(DEFAULT_ICON);
		} else {
			renderer.setLeafIcon(icon);
		}
		return true;
	}
	
	protected AWorkspaceTreeNode clone(DefaultFileNode node) {
		return super.clone(node);
	}
		
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	
	public File getFile() {
		return this.file;
	}
	
	public void handleAction(WorkspaceActionEvent event) {	
		if(event.getType() == WorkspaceActionEvent.WSNODE_CHANGED) {
			if(changeName(event.getBaggage().toString(), true)) {
				event.consume();
			}
		}
		else if(event.getType() == WorkspaceActionEvent.WSNODE_OPEN_DOCUMENT) {
			
			if(getFile() != null) {
				
				if(!file.exists()) {
					WorkspaceUtils.showFileNotFoundMessage(file);
					return;
				}						
				if(file.getName().toLowerCase().endsWith(".mm") || file.getName().toLowerCase().endsWith(".dcr")) {
					try {
						final URL mapUrl = Compat.fileToUrl(getFile());
						final MapIO mapIO = (MapIO) Controller.getCurrentModeController().getExtension(MapIO.class);
						mapIO.newMap(mapUrl);
//						Controller.getCurrentModeController().getMapController().newMap(mapUrl);
					}
					catch (final Exception e) {
						LogUtils.severe(e);
					}
				}
				else {
					try {
						Controller.getCurrentController().getViewController().openDocument(Compat.fileToUrl(getFile()));
					}
					catch (Exception e) {
						LogUtils.warn("could not open document ("+getFile()+")", e);
					}
				}
			}
		}
		else if (event.getType() == WorkspaceActionEvent.MOUSE_RIGHT_CLICK) {
            showPopup((Component) event.getBaggage(), event.getX(), event.getY());
        }
	}
	
	public final String getTagName() {
		return null;
	}
	
	public boolean acceptDrop(DataFlavor[] flavors) {
		return false;
	}

	public boolean processDrop(DropTargetDropEvent event) {
		return false;
	}
	
	public boolean processDrop(Transferable transferable, int dropAction) {
		return false;
	}
	
	public Transferable getTransferable() {
		WorkspaceTransferable transferable = new WorkspaceTransferable();
		try {
			URI uri = getFile().toURI().toURL().openConnection().getURL().toURI().normalize();
			transferable.addData(WorkspaceTransferable.WORKSPACE_URI_LIST_FLAVOR, uri.toString());
			List<File> fileList = new Vector<File>();
			fileList.add(new File(uri));
			transferable.addData(WorkspaceTransferable.WORKSPACE_FILE_LIST_FLAVOR, fileList);			
		}
		catch (Exception e) {
			LogUtils.warn(e);
		}
		if(!this.isSystem()) {
			List<AWorkspaceTreeNode> objectList = new ArrayList<AWorkspaceTreeNode>();
			objectList.add(this);
			transferable.addData(WorkspaceTransferable.WORKSPACE_NODE_FLAVOR, objectList);
		}
		return transferable;
	}

	public void initializePopup() {
		if (popupMenu == null) {			
			popupMenu = new WorkspacePopupMenu();
			WorkspacePopupMenuBuilder.addActions(popupMenu, new String[] {
					"workspace.action.node.cut",
					"workspace.action.node.copy",
					"workspace.action.node.paste",
					WorkspacePopupMenuBuilder.SEPARATOR,
					"workspace.action.node.rename",
					"workspace.action.node.remove",
					"workspace.action.file.delete",
					WorkspacePopupMenuBuilder.SEPARATOR,
					"workspace.action.node.refresh"
			});
		}
	}

	public AWorkspaceTreeNode clone() {
		DefaultFileNode node = new DefaultFileNode(getName(), getFile());
		return clone(node);
	}
	
	public WorkspacePopupMenu getContextMenu() {
		if (popupMenu == null) {
			initializePopup();
		}
		return popupMenu;
	}

	public boolean changeName(String newName, boolean renameLink) {
		String oldName = getName();
		if(rename(newName)) {
			try {
				WorkspaceUtils.getModel().changeNodeName(this, newName);
				return true;
			}
			catch(Exception ex) {
				// do nth.
			};			
		} 
		else {
			LogUtils.warn("cannot rename "+oldName);
		}
		return false;
	}
	
	public void orderDescending(boolean enable) {
		this.orderDescending = enable;
	}
	
	@ExportAsAttribute(name="orderDescending")
	public boolean orderDescending() {
		return orderDescending;
	}
}
