package org.freeplane.plugin.workspace.io.node;

import java.awt.Component;
import java.awt.datatransfer.Transferable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.Compat;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.config.node.AWorkspaceNode;
import org.freeplane.plugin.workspace.controller.IWorkspaceNodeEventListener;
import org.freeplane.plugin.workspace.controller.WorkspaceNodeEvent;
import org.freeplane.plugin.workspace.dnd.IWorkspaceTransferableCreator;
import org.freeplane.plugin.workspace.dnd.WorkspaceTransferable;

/**
 * 
 */
public class DefaultFileNode extends AWorkspaceNode implements IWorkspaceNodeEventListener, IWorkspaceTransferableCreator {
	private static final Icon FOLDER_OPEN_ICON = new ImageIcon(AWorkspaceNode.class.getResource("/images/16x16/folder-orange_open.png"));
	private static final Icon FOLDER_CLOSED_ICON = new ImageIcon(AWorkspaceNode.class.getResource("/images/16x16/folder-orange.png"));
	private static final Icon ACROBAT_ICON = new ImageIcon(AWorkspaceNode.class.getResource("/images/16x16/acrobat.png"));
	private static final Icon GRAPHICS_ICON = new ImageIcon(AWorkspaceNode.class.getResource("/images/16x16/image-x-generic.png"));
	private static final Icon DEFAULT_ICON = new ImageIcon(AWorkspaceNode.class.getResource("/images/16x16/text-x-preview.png"));
	private static final Icon WEB_ICON = new ImageIcon(AWorkspaceNode.class.getResource("/images/16x16/text-html-2.png"));
	private static final Icon DOCEAR_ICON = new ImageIcon(ResourceController.class.getResource("/images/docear16.png"));
	private static final Icon FREEPLANE_ICON = new ImageIcon(ResourceController.class.getResource("/images/Freeplane_frame_icon.png"));
	
	
	
	private File file;
	private String fileExtension;
	
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
		
	}
	
	public DefaultFileNode(final String name, final File file, String fileExtension) {
		this(name, file);
		setFileExtension(fileExtension);		
	}

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/
	
	public File getFile() {
		return this.file;
	}
	
	public String getFileExtension() {
		return fileExtension;
	}

	public void setFileExtension(String fileExtension) {
		this.fileExtension = fileExtension;
	}
	
	public boolean rename(final String name) {
		File newFile = new File(getFile().getParentFile() + File.separator + name);
		if(getFile().renameTo(newFile)) {
			this.file = newFile;
			return true;
		}
		return false;
	}
	
	public void delete() {
		getFile().delete();
	}
	
	public void relocateFile(final File parentFolder) {
		File newFile = new File(parentFolder.getPath() + File.separator + getName());
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
	
	private void copyFileTo(File file, File parentFolder) throws IOException {
		if(parentFolder.isDirectory()) {				
			File target = new File(parentFolder.getAbsolutePath()+File.separator+file.getName());
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
	public void copyHere(File file) throws IOException {
		if(getFile().isDirectory()) {
			copyFileTo(file, getFile());
		} 
		else {
			copyFileTo(file, getFile().getParentFile());
		}
		
	
		
	}
	
	public void moveHere(File file) throws IOException {
		copyHere(file);
		if(!file.delete()) throw new IOException("Could not delete File "+ file);
	}
	
	public boolean isEditable() {
		return false;
	}
	
	public boolean setIcons(DefaultTreeCellRenderer renderer) {
		if (getFile().isFile()) {
			if (getFileExtension().equalsIgnoreCase(".pdf")
					|| getFileExtension().equalsIgnoreCase(".ps")) {
				renderer.setLeafIcon(ACROBAT_ICON);
			}
			else if (getFileExtension().equalsIgnoreCase(".jpg")
					|| getFileExtension().equalsIgnoreCase(".png")
					|| getFileExtension().equalsIgnoreCase(".gif")
					|| getFileExtension().equalsIgnoreCase(".bmp")
					|| getFileExtension().equalsIgnoreCase(".jpeg")) {
				renderer.setLeafIcon(GRAPHICS_ICON);
			}
			else if (getFileExtension().equalsIgnoreCase(".mm")
					|| getFileExtension().equalsIgnoreCase(".dcr")) {
				if(ResourceController.getResourceController().getProperty("ApplicationName", "Freeplane").equals("Docear")) {
					renderer.setLeafIcon(DOCEAR_ICON);
				} else {
					renderer.setLeafIcon(FREEPLANE_ICON);
				}
			}
			else if (getFileExtension().equalsIgnoreCase(".html")
					|| getFileExtension().equalsIgnoreCase(".htm")
					|| getFileExtension().equalsIgnoreCase(".css")
					|| getFileExtension().equalsIgnoreCase(".xhtml")) {
				renderer.setLeafIcon(WEB_ICON);
			}			
			else {
				renderer.setLeafIcon(DEFAULT_ICON);
			}
		} 
		else {
			renderer.setOpenIcon(FOLDER_OPEN_ICON);
			renderer.setClosedIcon(FOLDER_CLOSED_ICON);
			renderer.setLeafIcon(FOLDER_CLOSED_ICON);
		}
		return true;
	}
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	
	public void handleEvent(WorkspaceNodeEvent event) {	
		if(event.getType() == WorkspaceNodeEvent.WSNODE_CHANGED) {
			if(rename(event.getBaggage().toString())) {
				setName(event.getBaggage().toString());
			}
			else {
				LogUtils.warn("Could not rename File("+getName()+") to File("+event.getBaggage()+")");
			}
			
		}
		else if(event.getType() == WorkspaceNodeEvent.WSNODE_OPEN_DOCUMENT) {
			
			if(getFile() != null) {
				int dot = getFile().getPath().lastIndexOf('.');
				String fileExt = "";
				if(-1 != dot) {
					fileExt = file.getPath().substring(dot);
				}				
				if(fileExt.equalsIgnoreCase(".mm") || fileExt.equalsIgnoreCase(".dcr")) {
					try {
						final URL mapUrl = Compat.fileToUrl(getFile());
						Controller.getCurrentModeController().getMapController().newMap(mapUrl, false);
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
			
			try {
				Controller.getCurrentController().getViewController().openDocument(Compat.fileToUrl(getFile()));
			}
			catch (Exception e) {
				LogUtils.warn("could not open document ("+getFile()+")", e);
			}
		}
		else if (event.getType() == WorkspaceNodeEvent.MOUSE_RIGHT_CLICK) {
            WorkspaceController.getController().getPopups()
                    .showPhysicalNodePopup((Component) event.getSource(), event.getX(), event.getY());
        }
	}
	
	public final String getTagName() {
		return null;
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
		catch (URISyntaxException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return transferable;
	}
}
