package org.freeplane.core.ui;

/**
 * Marks actions where title and icon path is available.
 * 
 * @author Robert Ladstaetter
 */
public interface IResourceFreeplaneAction extends IFreeplaneAction {
	public String getTitle();

	public String getIconPath();
}
