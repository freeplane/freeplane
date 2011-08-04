package org.freeplane.plugin.workspace.config.node;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.url.mindmapmode.MFileManager;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.controller.IWorkspaceNodeEventListener;
import org.freeplane.plugin.workspace.controller.WorkspaceNodeEvent;
import org.freeplane.plugin.workspace.io.annotation.ExportAsAttribute;

public class FilesystemMindMapLinkNode extends AWorkspaceNode implements IWorkspaceNodeEventListener {
	private URL linkPath;

	public FilesystemMindMapLinkNode(String id) {
		super(id);
	}

	public URL getLinkURL() {
		return this.linkPath;
	}

	@ExportAsAttribute("path")
	public String getLinkPath() {
		if (linkPath == null || linkPath.getPath() == null) {
			return "";
		}
		try {
			URI path = new URI(linkPath.getPath());
			URI workspaceLocation = new URI(WorkspaceController.getCurrentWorkspaceController().getWorkspaceLocation());

			System.out.println("PATH: " + path);
			System.out.println("WORKSPACE: " + workspaceLocation);
			System.out.println("RELATIVE PATH: " + workspaceLocation.relativize(path).getPath());

			return workspaceLocation.relativize(path).getPath();
		}
		catch (URISyntaxException e) {
			e.printStackTrace();
		}

		return linkPath.getPath();
	}

	public void setLinkPath(URL linkPath) {
		this.linkPath = linkPath;
	}

	public void handleEvent(WorkspaceNodeEvent event) {
		System.out.println("event: "+event.getType());
		if (event.getType() == WorkspaceNodeEvent.MOUSE_LEFT_DBLCLICK) {
			System.out.println("doublecklicked MindmapNode");
			try {
				File f = new File(getLinkURL().getPath());
				if (!f.exists()) {
					createNewMindmap(f);
				}
				Controller.getCurrentModeController().getMapController().newMap(getLinkURL(), false);

			}
			catch (Exception e) {
				LogUtils.warn("could not open document (" + getLinkURL().getPath() + ")", e);
			}
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
