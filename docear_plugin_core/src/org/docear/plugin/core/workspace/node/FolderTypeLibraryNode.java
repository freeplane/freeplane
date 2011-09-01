/**
 * author: Marcel Genzmehr
 * 18.08.2011
 */
package org.docear.plugin.core.workspace.node;

import java.net.URI;
import java.util.List;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.IDocearLibrary;
import org.docear.plugin.core.event.DocearEvent;
import org.docear.plugin.core.event.DocearEventType;
import org.docear.plugin.core.event.IDocearEventListener;
import org.freeplane.core.util.LogUtils;
import org.freeplane.plugin.workspace.config.node.FolderNode;

/**
 * 
 */
public class FolderTypeLibraryNode extends FolderNode implements IDocearEventListener, IDocearLibrary {
	private static final Icon DEFAULT_ICON = new ImageIcon(FolderTypeLibraryNode.class.getResource("/images/folder-database.png"));
	
	private final Vector<URI> mindmapIndex = new Vector<URI>();
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	public FolderTypeLibraryNode(String type) {
		super(type);
		DocearController.getController().addDocearEventListener(this);
		DocearEvent event = new DocearEvent(this, DocearEventType.NEW_LIBRARY);
		DocearController.getController().dispatchDocearEvent(event);
	}	
	
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/
	
	public boolean setIcons(DefaultTreeCellRenderer renderer) {
		renderer.setOpenIcon(DEFAULT_ICON);
		renderer.setClosedIcon(DEFAULT_ICON);
		renderer.setLeafIcon(DEFAULT_ICON);
		return true;
	}
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	
	public void handleEvent(DocearEvent event) {
		if(event.getType() == DocearEventType.LIBRARY_NEW_MINDMAP_INDEXING_REQUEST) {
			if(event.getEventObject() instanceof URI) {
				URI uri = (URI) event.getEventObject();
				if(!mindmapIndex.contains(uri)) {
					LogUtils.info("DOCEAR: adding new mindmap to library: "+ uri);
					mindmapIndex.add(uri);
				}
			}			
		}
		else if(event.getType() == DocearEventType.LIBRARY_EMPTY_MINDMAP_INDEX_REQUEST) {
			mindmapIndex.removeAllElements();			
		}
		
	}
	
	public List<URI> getMindmaps() {
		return mindmapIndex;
	}
}
