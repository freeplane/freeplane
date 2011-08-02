package org.freeplane.plugin.workspace.config.creator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.FileChannel;

import org.freeplane.core.util.LogUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.url.mindmapmode.MFileManager;
import org.freeplane.n3.nanoxml.XMLElement;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.config.node.AWorkspaceNode;
import org.freeplane.plugin.workspace.config.node.FilesystemLinkNode;

public class FilesystemLinkCreator extends AConfigurationNodeCreator {

	public FilesystemLinkCreator() {
	}

	@Override
	public AWorkspaceNode getNode(String id, XMLElement data) {
		FilesystemLinkNode node = new FilesystemLinkNode(id);
		String name = data.getAttribute("name", null);
		node.setName(name == null ? "reference" : name);

		String path = data.getAttribute("path", null);
		if (path != null) {
			LogUtils.info("FilesystemLinkPath: " + path);
			File f = new File(path);
			if (!f.isAbsolute()) {
				path = WorkspaceController.getCurrentWorkspaceController().getWorkspaceLocation() + File.separator + path;
				f = new File(path);
				System.out.println("MAP: " + f.getAbsolutePath());
				if (!f.exists() && f.getName().endsWith("mm")) {
					createNewMindmap(f);
				}
			}
			URL url = null;
			try {
				url = new URL("file://" + path);
				LogUtils.info("FilesystemFolderCreator.getNode: " + url);
			}
			catch (MalformedURLException e) {
				e.printStackTrace();
			}
			node.setLinkPath(url);
		}
		return node;

	}

	private boolean createNewMindmap(final File f) {
		if (!createFolderStructure(f)) {
			return false;
		}
//		MModeController mModeController = MModeController.getMModeController();
//				
//		final MapController mapController = Controller.getCurrentModeController().getMapController();
//		final MapModel map = mapController.newMap(((NodeModel) null));
//		
//		MFileManager mFileManager = MFileManager.getController(mModeController);
//		mFileManager.loadDefault(map);
//		mFileManager.save(map, f);
//		map.destroy();
		
		MFileManager mFileManager = MFileManager.getController(Controller.getCurrentModeController());
		File defaultFile = mFileManager.defaultTemplateFile();		
		System.out.println("defaultFile: "+defaultFile.getAbsolutePath());
		System.out.println("File: "+f.getAbsolutePath());
		try {
			FileChannel from = new FileInputStream(defaultFile).getChannel();
			FileChannel to = new FileOutputStream(f).getChannel();
			to.transferFrom(from, 0, from.size());
			from.close();
			to.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
//		try {
//			model.setURL(f.toURL());
//		}
//		catch (MalformedURLException e) {			
//			e.printStackTrace();
//		}
//		mFileManager.save(model, f);
		
		
		//mFileManager.save(Controller.getCurrentController().getMap(), f);
		//mModeController.getMapController().close(true);
//		mFileManager.
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

}
