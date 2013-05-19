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
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.TreePath;

import org.freeplane.core.util.LogUtils;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.components.IWorkspaceView;
import org.freeplane.plugin.workspace.components.WorkspaceNodeRenderer;
import org.freeplane.plugin.workspace.features.AWorkspaceModeExtension;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;
import org.freeplane.plugin.workspace.nodes.WorkspaceRootNode;
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
	
	private Map<Class<? extends AWorkspaceTreeNode>, INodeDropHandler> dropHandlers = new LinkedHashMap<Class<? extends AWorkspaceTreeNode>, INodeDropHandler>();

	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	public WorkspaceTransferHandler(JTree tree) {		
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
		WorkspaceTransferable transferable = null;
		if (comp instanceof JTree) {
			JTree t = (JTree) comp;			
			for (TreePath p : t.getSelectionPaths()) {
				AWorkspaceTreeNode node = (AWorkspaceTreeNode) p.getLastPathComponent();
				if (node instanceof IWorkspaceTransferableCreator) {
					if(transferable == null) {
						transferable = ((IWorkspaceTransferableCreator)node).getTransferable();
					}
					else {
						transferable.merge(((IWorkspaceTransferableCreator)node).getTransferable());
					}
				} 
			}
		}		
		return transferable;

	}

	public boolean importData(JComponent comp, Transferable transf) {
		if (comp instanceof JTree) {
			JTree t = (JTree) comp;			
			for (TreePath p : t.getSelectionPaths()) {
				AWorkspaceTreeNode targetNode = (AWorkspaceTreeNode) p.getLastPathComponent();
				if (DnDController.isDropAllowed(targetNode)) {
					if(transf == null) {
		        		return false;
		        	}
		        	try {
						return handleDrop(targetNode, transf, DnDConstants.ACTION_COPY);
					} catch (NoDropHandlerFoundExeption e) {
						LogUtils.info("org.freeplane.plugin.workspace.dnd.WorkspaceTransferHandler.importData(comp, transf): " + e.getMessage());
					}
				}
			}
		}		
		return false;
	}

	public int getSourceActions(JComponent comp) {
		AWorkspaceModeExtension ctrl = WorkspaceController.getCurrentModeExtension();
		if(ctrl.getView().containsComponent(comp)) {
			TreePath selectionPath = ctrl.getView().getSelectionPath();
			if(selectionPath != null) {
				if(selectionPath.getLastPathComponent() instanceof WorkspaceRootNode) {
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
	
	public boolean handleDrop(AWorkspaceTreeNode targetNode, Transferable transf, int dndAction) throws NoDropHandlerFoundExeption {
		if(targetNode == null) {
			throw new IllegalArgumentException("targetNode is NULL");
		}
		
		INodeDropHandler h = findHandler(targetNode.getClass());
		if(h == null) {
			throw new NoDropHandlerFoundExeption(targetNode);
		}
		
		if(h.acceptDrop(transf)) {
			return h.processDrop(targetNode, transf, dndAction);
		}
		return false;
	}
	
	private INodeDropHandler findHandler(Class<?> clzz) {
		if(clzz == null) {
			return null;
		}
		
		INodeDropHandler h = dropHandlers.get(clzz);
//		if(h == null) {
//		
//			for (Class<?> interf : clzz.getInterfaces()) {
//				h = findHandler(interf);
//				if(h != null) {
//					return h;
//				}
//			}
//			
//			h = findHandler((Class<?>) clzz.getSuperclass());
//		}
//		
		return h;
	}
	
	public void registerNodeDropHandler(Class<? extends AWorkspaceTreeNode> clzz, INodeDropHandler handler) {
		if(clzz == null || handler == null) {
			return;
		}
		synchronized (dropHandlers) {
			dropHandlers.put(clzz, handler);
		}
	}
	
	private boolean processDrop(AWorkspaceTreeNode targetNode, DropTargetDropEvent event) {
		event.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
		

		final Transferable transferable = event.getTransferable();
		final int dropAction = event.getDropAction();
		try {
			if(!targetNode.getAllowsChildren()) {
				targetNode = targetNode.getParent();
			}
			if(!DnDController.isDropAllowed(targetNode)) {
				event.dropComplete(false);
				return false;
			}
			if(handleDrop(targetNode, transferable, dropAction)) {
				event.dropComplete(true);
				return true;
			}
		} catch (NoDropHandlerFoundExeption e) {
			LogUtils.info("org.freeplane.plugin.workspace.dnd.WorkspaceTransferHandler.processDrop(targetNode, event): "+ e.getMessage());
		}
		
		event.dropComplete(false);
		return false;
	}

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	/* DropTarget Methods */

	public final void drop(DropTargetDropEvent event) {
		IWorkspaceView view = WorkspaceController.getCurrentModeExtension().getView();
		if(view == null) {
			return;
		}
		
		TreePath targetPath = view.getPathForLocation(event.getLocation().x, event.getLocation().y);
		if(targetPath != null) {
			AWorkspaceTreeNode targetNode = (AWorkspaceTreeNode) targetPath.getLastPathComponent();
			if(processDrop(targetNode, event)) {
				return;
			}	
		}
		event.rejectDrop();
	}

	public final void dragEnter(DropTargetDragEvent dtde) {
		dtde.getDropTargetContext().getDropTarget().setDefaultActions(COPY_OR_MOVE);
		//LogUtils.info("org.freeplane.plugin.workspace.dnd.WorkspaceTransferHandler.dragEnter(dtde)");
	}

	public final void dragExit(DropTargetEvent dte) {
		dte.getDropTargetContext().getDropTarget().setDefaultActions(COPY);
		//LogUtils.info("org.freeplane.plugin.workspace.dnd.WorkspaceTransferHandler.dragExit(dte)");
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
		//LogUtils.info("org.freeplane.plugin.workspace.dnd.WorkspaceTransferHandler.dropActionChanged(dtde)");
	}

	

	/***********************************************************************************
	 * INTERNAL CLASSES
	 **********************************************************************************/
	
	

}
