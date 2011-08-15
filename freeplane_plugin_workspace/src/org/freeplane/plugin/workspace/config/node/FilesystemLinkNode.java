package org.freeplane.plugin.workspace.config.node;


import java.awt.Component;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Vector;

import org.freeplane.core.util.Compat;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.config.PopupMenus;
import org.freeplane.plugin.workspace.controller.IWorkspaceNodeEventListener;
import org.freeplane.plugin.workspace.controller.WorkspaceNodeEvent;
import org.freeplane.plugin.workspace.dnd.IWorkspaceTransferableCreator;
import org.freeplane.plugin.workspace.dnd.WorkspaceTransferable;
import org.freeplane.plugin.workspace.io.annotation.ExportAsAttribute;

public class FilesystemLinkNode extends AWorkspaceNode implements IWorkspaceNodeEventListener, IWorkspaceTransferableCreator {
	private final String POPUP_KEY="/filesystem_link";
	private URI linkPath;
	
	
	public FilesystemLinkNode(String id) {
		super(id);	
		initializePopup();
	}
	
	private void initializePopup() {
		PopupMenus popupMenu = WorkspaceController.getCurrentWorkspaceController().getPopups();
		popupMenu.registerPopupMenuNodeDefault(POPUP_KEY);
		popupMenu.buildPopupMenu(POPUP_KEY);
	}
	
	@ExportAsAttribute("path")
	public URI getLinkPath() {
		return linkPath;
	}
	
	public void setLinkPath(URI linkPath) {
		this.linkPath = linkPath;
	}	

	public void handleEvent(WorkspaceNodeEvent event) {
		if(event.getType() == WorkspaceNodeEvent.WSNODE_OPEN_DOCUMENT) {
			try {
				URL url = getLinkPath().toURL().openConnection().getURL();
				Controller.getCurrentController().getViewController().openDocument(Compat.fileToUrl(new File(url.toURI())));
			}
			catch (Exception e) {
				LogUtils.warn("could not open document ("+getLinkPath()+")", e);
			}
		}
		else if (event.getType() == WorkspaceNodeEvent.MOUSE_RIGHT_CLICK) {			
			Component component = (Component) event.getSource();

			WorkspaceController.getCurrentWorkspaceController().getPopups()
					.showPopup(POPUP_KEY, component, event.getX(), event.getY());

		}
	}

	public String getTagName() {
		return "filesystem_link";
	}

	public Transferable getTransferable() {
		WorkspaceTransferable transferable = new WorkspaceTransferable();
		try {
			URI uri = getLinkPath().toURL().openConnection().getURL().toURI().normalize();
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
