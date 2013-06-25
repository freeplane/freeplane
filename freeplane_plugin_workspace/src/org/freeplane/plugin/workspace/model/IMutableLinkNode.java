/**
 * author: Marcel Genzmehr
 * 22.11.2011
 */
package org.freeplane.plugin.workspace.model;

/**
 * 
 */
public interface IMutableLinkNode {
	/**
	 * Changes the name of this node.
	 * 
	 * @param newName the new name string
	 * @param renameLink set to <code>true</code> if the linked file name should also be renamed, else <code>false</code>
	 * @return <code>true</code> if the name was successfully changed, else <code>false</code>
	 */
	public boolean changeName(String newName, boolean renameLink);
}
