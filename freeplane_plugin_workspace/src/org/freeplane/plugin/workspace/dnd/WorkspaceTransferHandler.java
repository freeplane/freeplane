/**
 * author: Marcel Genzmehr
 * 27.07.2011
 */
package org.freeplane.plugin.workspace.dnd;

import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.TreePath;

import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.components.WorkspaceNodeRenderer;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;
import org.freeplane.plugin.workspace.nodes.WorkspaceRoot;
/**
 * 
 */
public class WorkspaceTransferHandler extends TransferHandler implements DropTargetListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final Insets DEFAULT_INSETS = new Insets(20, 20, 20, 20);

	private JTree tree;
	private final IDropTargetDispatcher dispatcher;

	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	public WorkspaceTransferHandler(JTree tree) {
		this.dispatcher = new DefaultWorkspaceDropTargetDispatcher();
		
		this.tree = tree;
		this.tree.setTransferHandler(this);
		this.tree.setDragEnabled(true);
		this.tree.setAutoscrolls(true);
		new DropTarget(tree, COPY_OR_MOVE, this);
	}

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	public static WorkspaceTransferHandler configureDragAndDrop(JTree tree) {
		return new WorkspaceTransferHandler(tree);
	}

	private void autoscroll(JTree tree, Point cursorLocation) {
		Insets insets = DEFAULT_INSETS;
		Rectangle outer = tree.getVisibleRect();
		Rectangle inner = new Rectangle(outer.x + insets.left, outer.y + insets.top, outer.width - (insets.left + insets.right),
				outer.height - (insets.top + insets.bottom));
		if (!inner.contains(cursorLocation)) {
			Rectangle scrollRect = new Rectangle(cursorLocation.x - insets.left, cursorLocation.y - insets.top, insets.left
					+ insets.right, insets.top + insets.bottom);
			tree.scrollRectToVisible(scrollRect);
		}
	}

	public Transferable createTransferable(JComponent comp) {
		if (comp instanceof JTree) {
			JTree t = (JTree) comp;			
			for (TreePath p : t.getSelectionPaths()) {
				AWorkspaceTreeNode node = (AWorkspaceTreeNode) p.getLastPathComponent();
				if (node instanceof IWorkspaceTransferableCreator) {
					//FIXME: prepare for multiple node selection
					return ((IWorkspaceTransferableCreator)node).getTransferable();
				} 
			}
		}		
		return null;

	}

	public boolean importData(JComponent comp, Transferable transf) {
		if (comp instanceof JTree) {
			JTree t = (JTree) comp;			
			for (TreePath p : t.getSelectionPaths()) {
				AWorkspaceTreeNode targetNode = (AWorkspaceTreeNode) p.getLastPathComponent();
				if (targetNode instanceof IDropAcceptor) {
					if(transf == null) {
		        		return false;
		        	}
		        	return ((IDropAcceptor) targetNode).processDrop(transf, DnDConstants.ACTION_COPY);
				}
			}
		}		
		return false;
	}

	public int getSourceActions(JComponent comp) {
		if(comp == WorkspaceController.getController().getWorkspaceViewTree()) {
			TreePath selectionPath = WorkspaceController.getController().getWorkspaceViewTree().getSelectionPath();
			if(selectionPath != null) {
				if(selectionPath.getLastPathComponent() instanceof WorkspaceRoot) {
					return NONE;
				}
				//SYSTEM NODES are vulnerable for DnD move events (e.g. Copy a system file link and delete the copy hard afterwards, maybe the original will also be deleted.) 
				if(selectionPath.getLastPathComponent() instanceof AWorkspaceTreeNode 
						&& ( ((AWorkspaceTreeNode) selectionPath.getLastPathComponent()).isSystem() || !((AWorkspaceTreeNode) selectionPath.getLastPathComponent()).isTransferable() ) ) {
					//DOCEAR: REJECT DnD on system nodes for now
					return NONE;
				}
			}
		}
		return comp.getDropTarget().getDefaultActions();
	}

	public void exportToClipboard(JComponent comp, Clipboard clip, int action) throws IllegalStateException {
		super.exportToClipboard(comp, clip, action);
	}

	// Causes the Swing drag support to be initiated.
	public void exportAsDrag(JComponent comp, java.awt.event.InputEvent e, int action) {
		super.exportAsDrag(comp, e, action);
	}

	// Invoked after data has been exported.
	public void exportDone(JComponent source, Transferable data, int action) {
		super.exportDone(source, data, action);
	}

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	/* DropTarget Methods */

	public final void drop(DropTargetDropEvent event) {
		if(WorkspaceController.getController().getWorkspaceViewTree().getPathForLocation(event.getLocation().x, event.getLocation().y) == null) {
			return;
		}
		// new method to handle drop events
		if(this.dispatcher.dispatchDropEvent(event)) {
			return;
		}		
		event.rejectDrop();
	}

	public final void dragEnter(DropTargetDragEvent dtde) {
	}

	public final void dragExit(DropTargetEvent dte) {
	}

	private TreePath lastPathLocation = null;
	public final void dragOver(DropTargetDragEvent dtde) {
		autoscroll(this.tree, dtde.getLocation());		
		TreePath path = tree.getPathForLocation(dtde.getLocation().x, dtde.getLocation().y);
		if(path == lastPathLocation) {
			return;
		}
		WorkspaceNodeRenderer renderer = (WorkspaceNodeRenderer) tree.getCellRenderer();
		if(path != null && path != lastPathLocation) {								
			lastPathLocation = path;			
			renderer.highlightRow(tree.getRowForLocation(dtde.getLocation().x, dtde.getLocation().y));
			tree.repaint();
		} 
		else if(lastPathLocation != null) {			
			lastPathLocation = null;
			renderer.highlightRow(-1);
			tree.repaint();
		}
	}

	public final void dropActionChanged(DropTargetDragEvent dtde) {
	}

	/***********************************************************************************
	 * INTERNAL CLASSES
	 **********************************************************************************/
	
	

}
