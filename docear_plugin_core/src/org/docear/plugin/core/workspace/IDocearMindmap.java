/**
 * author: Marcel Genzmehr
 * 22.11.2011
 */
package org.docear.plugin.core.workspace;

/**
 * 
 */
public interface IDocearMindmap {
	/**
	 * Changes the name of this node.
	 * 
	 * @param newName the new name string
	 * @return <code>true</code> if the name was successfully changed, else <code>false</code>
	 */
	public boolean changeNameTo(String newName);
}
