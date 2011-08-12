/**
 * author: Marcel Genzmehr
 * 29.07.2011
 */
package org.freeplane.plugin.workspace.dnd;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.Enumeration;
import java.util.Hashtable;

import org.freeplane.core.util.LogUtils;

/**
 * 
 */
public class WorkspaceTransferable implements Transferable {
	
	public static DataFlavor WORKSPACE_DROP_ACTION_FLAVOR; // = new DataFlavor("text/drop-action; class=java.lang.String");
	public static DataFlavor WORKSPACE_NODE_FLAVOR; // = new DataFlavor("application/x-java-jvm-local-objectref; class=org.freeplane.plugin.workspace.config.node.AWorkspaceNode");
	public static DataFlavor WORKSPACE_FILE_LIST_FLAVOR; // = DataFlavor.javaFileListFlavor;
	public static DataFlavor WORKSPACE_FREEPLANE_NODE_FLAVOR; // = new DataFlavor("text/freeplane-nodes; class=java.lang.String");
	public static DataFlavor WORKSPACE_SERIALIZED_FLAVOR;
	public static DataFlavor WORKSPACE_GNOME_FILE_LIST_FLAVOR;
	static {
		try {
			WORKSPACE_DROP_ACTION_FLAVOR = new DataFlavor("text/drop-action; class=java.lang.String");
			WORKSPACE_NODE_FLAVOR = new DataFlavor("application/x-java-jvm-local-objectref; class=java.util.List");
			WORKSPACE_FILE_LIST_FLAVOR = DataFlavor.javaFileListFlavor;
			WORKSPACE_FREEPLANE_NODE_FLAVOR = new DataFlavor("text/freeplane-nodes; class=java.lang.String");
			WORKSPACE_SERIALIZED_FLAVOR = new DataFlavor("application/x-java-serialized-object; class=java.lang.String");
			WORKSPACE_GNOME_FILE_LIST_FLAVOR = new DataFlavor("text/uri-list; class=java.lang.String");
		}
		catch (final Exception e) {
			LogUtils.severe(e);
		}
	}
	
	private final Hashtable<DataFlavor, Object> dataMap = new Hashtable<DataFlavor, Object>();
	
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	
	public WorkspaceTransferable(DataFlavor flavor, Object data) {
		addData(flavor, data);
	}

	public boolean addData(DataFlavor flavor, Object data) {
		dataMap.put(flavor, data);
		return true;
	}
	
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/
	
	// Returns an object which represents the data to be transferred.
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
		if(dataMap.containsKey(flavor)) {
			return dataMap.get(flavor);
		}
		throw new UnsupportedFlavorException(flavor);
	}

	public DataFlavor[] getTransferDataFlavors() {
		System.out.println("getTransferDataFlavors");
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
		System.out.println("isDataFlavorSupported: " + flavor);
		if(dataMap.containsKey(flavor)) {
			return true;
		}
		return false;
	}
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
}
