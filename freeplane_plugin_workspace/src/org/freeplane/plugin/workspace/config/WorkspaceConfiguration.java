package org.freeplane.plugin.workspace.config;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import javax.swing.tree.MutableTreeNode;

import org.freeplane.core.io.ReadManager;
import org.freeplane.core.io.xml.TreeXmlReader;
import org.freeplane.core.ui.IndexedTree;
import org.freeplane.n3.nanoxml.XMLException;
import org.freeplane.plugin.workspace.config.node.FilesystemFolderCreator;
import org.freeplane.plugin.workspace.config.node.FilesystemLinkCreator;
import org.freeplane.plugin.workspace.config.node.GroupCreator;
import org.freeplane.plugin.workspace.config.node.WorkspaceCreator;

public class WorkspaceConfiguration {
	final private ReadManager readManager;
	private IndexedTree tree;
	
	public WorkspaceConfiguration(URL xmlFile) {
		readManager = new ReadManager();
		tree = new IndexedTree(null);
		initReadManager();
		this.load(xmlFile);
	}
	
	private void initReadManager() {
		readManager.addElementHandler("workspace_structure", new WorkspaceCreator(tree));
		readManager.addElementHandler("group", new GroupCreator(tree));
		readManager.addElementHandler("filesystem_folder", new FilesystemFolderCreator(tree));
		readManager.addElementHandler("filesystem_link", new FilesystemLinkCreator(tree));
	}
	
	public void load(final URL xmlFile) {
		final TreeXmlReader reader = new TreeXmlReader(readManager);
		try {
			reader.load(new InputStreamReader(new BufferedInputStream(xmlFile.openStream())));
		}
		catch (final IOException e) {
			throw new RuntimeException(e);
		}
		catch (final XMLException e) {
			throw new RuntimeException(e);
		}
	}
	
	public MutableTreeNode getConigurationRoot() {
		return tree.getRoot();
	}
	
}
