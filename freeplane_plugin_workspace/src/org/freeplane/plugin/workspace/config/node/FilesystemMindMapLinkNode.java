package org.freeplane.plugin.workspace.config.node;

import java.awt.Component;
import java.io.File;
import java.net.URI;
import java.net.URL;

import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.url.mindmapmode.MFileManager;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.config.PopupMenus;
import org.freeplane.plugin.workspace.controller.IWorkspaceNodeEventListener;
import org.freeplane.plugin.workspace.controller.WorkspaceNodeEvent;
import org.freeplane.plugin.workspace.io.annotation.ExportAsAttribute;

public class FilesystemMindMapLinkNode extends AWorkspaceNode implements IWorkspaceNodeEventListener {
	private URI linkPath;
	private final String POPUP_KEY = "/filesystem_mindmap_link";

	public FilesystemMindMapLinkNode(String id) {
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
		System.out.println("event: "+event.getType());
		if (event.getType() == WorkspaceNodeEvent.MOUSE_LEFT_DBLCLICK) {
			System.out.println("doublecklicked MindmapNode");
			try {
				URL absoluteUrl = getLinkPath().toURL().openConnection().getURL();				
				File f = new File(absoluteUrl.getFile());
				if (!f.exists()) {
					createNewMindmap(f);
				}
				Controller.getCurrentModeController().getMapController().newMap(absoluteUrl, false);

			}
			catch (Exception e) {
				LogUtils.warn("could not open document (" + getLinkPath() + ")", e);
			}
		}
		else if (event.getType() == WorkspaceNodeEvent.MOUSE_RIGHT_CLICK) {			
			Component component = (Component) event.getSource();

			WorkspaceController.getCurrentWorkspaceController().getPopups()
					.showPopup(POPUP_KEY, component, event.getX(), event.getY());

		}
	}

	private boolean createNewMindmap(final File f) {
		if (!createFolderStructure(f)) {
			return false;
		}

		MFileManager mFileManager = MFileManager.getController(Controller.getCurrentModeController());
		mFileManager.newMap();
		MapModel map = Controller.getCurrentController().getMap();
		map.getRootNode().setText(getName());
		
		mFileManager.save(Controller.getCurrentController().getMap(), f);
		Controller.getCurrentController().close(false);

		LogUtils.info("New Mindmap Created: " + f.getAbsolutePath());
		return true;
	}

	private boolean createFolderStructure(final File f) {
		final File folder = f.getParentFile();
		if (folder.exists()) {
			return true;
		}
		return folder.mkdirs();
	}

	public String getTagName() {
		return "filesystem_mindmap_link";
	}
}
