/**
 * author: Marcel Genzmehr
 * 29.07.2011
 */
package org.freeplane.plugin.workspace.dnd;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import org.freeplane.core.util.LogUtils;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;
import org.freeplane.plugin.workspace.nodes.AFolderNode;

/**
 * 
 */
public class WorkspaceTransferable implements Transferable {
	
	public static DataFlavor WORKSPACE_DROP_ACTION_FLAVOR; // = new DataFlavor("text/drop-action; class=java.lang.String");
	public static DataFlavor WORKSPACE_NODE_FLAVOR; // = new DataFlavor("application/x-java-jvm-local-objectref; class=org.freeplane.plugin.workspace.config.node.AWorkspaceNode");
	public static DataFlavor WORKSPACE_FILE_LIST_FLAVOR; // = DataFlavor.javaFileListFlavor;
	public static DataFlavor WORKSPACE_FREEPLANE_NODE_FLAVOR; // = new DataFlavor("text/freeplane-nodes; class=java.lang.String");
	public static DataFlavor WORKSPACE_SERIALIZED_FLAVOR;
	public static DataFlavor WORKSPACE_URI_LIST_FLAVOR;
	public static DataFlavor WORKSPACE_MOVE_NODE_FLAVOR;
	static {
		try {
			WORKSPACE_DROP_ACTION_FLAVOR = new DataFlavor("text/drop-action; class=java.lang.String");
			WORKSPACE_NODE_FLAVOR = new DataFlavor("application/x-java-jvm-local-objectref; class=java.util.List");
			WORKSPACE_FILE_LIST_FLAVOR = new DataFlavor("application/x-java-file-list; class=java.util.List");
			WORKSPACE_FREEPLANE_NODE_FLAVOR = new DataFlavor("text/freeplane-nodes; class=java.lang.String");
			WORKSPACE_SERIALIZED_FLAVOR = new DataFlavor("application/x-java-serialized-object; class=java.lang.String");
			WORKSPACE_URI_LIST_FLAVOR = new DataFlavor("text/uri-list; class=java.lang.String");
			WORKSPACE_MOVE_NODE_FLAVOR = new DataFlavor("text/move-action; class=java.lang.String");
		}
		catch (final Exception e) {
			LogUtils.severe(e);
		}
	}
	
	private final Hashtable<DataFlavor, Object> dataMap = new Hashtable<DataFlavor, Object>();
	private boolean isCopy = true;
	
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	
	public WorkspaceTransferable(DataFlavor flavor, Object data) {
		addData(flavor, data);
	}

	public WorkspaceTransferable() {
	}

	
	
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/
	
	public boolean isCopy() {
		return this.isCopy ;
	}
	
	public void setAsCopy(boolean asCopy) {
		boolean old = isCopy();
		this.isCopy = asCopy;
		if(old != isCopy()) {
			if(isCopy()) {
				dataMap.remove(WORKSPACE_MOVE_NODE_FLAVOR);
			}
			else {
				dataMap.put(WORKSPACE_MOVE_NODE_FLAVOR, "move-action");
			}
		}
	}
	
	public boolean addData(DataFlavor flavor, Object data) {
		dataMap.put(flavor, data);
		return true;
	}	
	
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
		if(dataMap.containsKey(flavor)) {
			return dataMap.get(flavor);
		}
		throw new UnsupportedFlavorException(flavor);
	}

	public DataFlavor[] getTransferDataFlavors() {
		DataFlavor[] flavors = new DataFlavor[dataMap.size()];
		int i = 0;
		for(Enumeration<DataFlavor> e = dataMap.keys(); e.hasMoreElements(); i++) {
			flavors[i] = e.nextElement();
		}
		return flavors;
	}

	// Returns whether or not the specified data flavor is supported for
	// this object.
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		if(dataMap.containsKey(flavor)) {
			return true;
		}
		return false;
	}

	public void merge(WorkspaceTransferable transferable) {
		if(transferable == null) {
			return;
		}
		for(DataFlavor flavor : transferable.getTransferDataFlavors()) {
			if(isDataFlavorSupported(flavor)) {
				try {
					merge(flavor, transferable.getTransferData(flavor));
				} catch (UnsupportedFlavorException e) {
					//cannot happen
				}
			}
			else {
				try {
					addData(flavor, transferable.getTransferData(flavor));
				} catch (UnsupportedFlavorException e) {
					//cannot happen
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void merge(DataFlavor flavor, Object transferData) {
		if(flavor.equals(WORKSPACE_URI_LIST_FLAVOR)) {
			mergeURIList((String)transferData);
		}
		else if(flavor.equals(WORKSPACE_FILE_LIST_FLAVOR)) {
			mergeFileList((List<File>)transferData);
		}
		else if(flavor.equals(WORKSPACE_NODE_FLAVOR)) {
			mergeNodeList((List<AWorkspaceTreeNode>)transferData);
		}
		
	}

	@SuppressWarnings("unchecked")
	private void mergeNodeList(List<AWorkspaceTreeNode> transferData) {
		List<AWorkspaceTreeNode> nodes = (List<AWorkspaceTreeNode>) dataMap.get(WORKSPACE_NODE_FLAVOR);
		for (AWorkspaceTreeNode newNode : transferData) {
			if(!nodes.contains(newNode)) {
				nodes.add(newNode);
			}			
		}		
	}

	@SuppressWarnings("unchecked")
	private void mergeFileList(List<File> transferData) {
		List<File> files = (List<File>) dataMap.get(WORKSPACE_FILE_LIST_FLAVOR);
		for (File newFile : transferData) {
			if(!files.contains(newFile)) {
				files.add(newFile);
			}			
		}
	}

	private void mergeURIList(String transferData) {
		String URI_SEP = "\r\n";
		String[] uris = transferData.split(URI_SEP);
		StringBuffer buffer = new StringBuffer((String)dataMap.get(WORKSPACE_URI_LIST_FLAVOR));
		for (String uri : uris) {
			if(buffer.indexOf(uri) < 0) {
				buffer.append(URI_SEP);
				buffer.append(uri);
			}
		}
		
	}
	
	@SuppressWarnings("unchecked")
	public boolean contains(AWorkspaceTreeNode node) {
		if(node != null) {
			List<AWorkspaceTreeNode> nodes = (List<AWorkspaceTreeNode>) dataMap.get(WORKSPACE_NODE_FLAVOR);
			if(nodes != null) {
				for (AWorkspaceTreeNode inNode : nodes) {
					if(inNode.getKey().equals(node.getKey())) {
						return true;
					}
				}
			}
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	public void refreshNodes() {
		List<AWorkspaceTreeNode> nodes = (List<AWorkspaceTreeNode>) dataMap.get(WORKSPACE_NODE_FLAVOR);
		if(nodes != null) {
			for (AWorkspaceTreeNode node : nodes) {
				if(!(node instanceof AFolderNode)) {
					node.getParent().refresh();
				}
				else {
					node.refresh();
				}
			}
		}
	}
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
}
