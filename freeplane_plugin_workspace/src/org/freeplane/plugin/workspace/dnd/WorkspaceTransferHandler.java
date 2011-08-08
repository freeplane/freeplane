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
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.freeplane.features.clipboard.ClipboardController;
import org.freeplane.features.clipboard.mindmapmode.MClipboardController;
import org.freeplane.plugin.workspace.io.node.DefaultFileNode;

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
	private IWorkspaceDragController controller;

	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	public WorkspaceTransferHandler(JTree tree, IWorkspaceDragController controller, int action, boolean drawIcon) {
		this.controller = controller;

		this.tree = tree;
		this.tree.setTransferHandler(this);
		this.tree.setDragEnabled(true);
		this.tree.setAutoscrolls(true);
		new DropTarget(tree, COPY_OR_MOVE, this);
	}

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	public static WorkspaceTransferHandler configureDragAndDrop(JTree tree, IWorkspaceDragController controller) {
		return new WorkspaceTransferHandler(tree, controller, DnDConstants.ACTION_COPY_OR_MOVE, true);
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
		System.out.println("createTransferable for " + comp);
		if (comp instanceof JTree) {
			Vector<File> paths = new Vector<File>();
			JTree t = (JTree) comp;
			for (TreePath p : t.getSelectionPaths()) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) p.getLastPathComponent();
				if (node.getUserObject() instanceof DefaultFileNode)
					paths.add(((DefaultFileNode) node.getUserObject()).getFile());
			}
			if (paths.size() > 0) {
				System.out.println("Geschmacksrichtung: " + WorkspaceTransferable.WORKSPACE_FILE_LIST_FLAVOR);
				final MClipboardController clipboardController = (MClipboardController) ClipboardController.getController();
				clipboardController.setClipboardContents(new WorkspaceTransferable(WorkspaceTransferable.WORKSPACE_FILE_LIST_FLAVOR, paths));
				return clipboardController.getClipboardContents();
			}
			return new WorkspaceTransferable(WorkspaceTransferable.WORKSPACE_NODE_FLAVOR, Arrays.asList(t.getSelectionPaths()));
		}
		return new WorkspaceTransferable(WorkspaceTransferable.WORKSPACE_SERIALIZED_FLAVOR, "");

	}

	public boolean importData(JComponent comp, Transferable t) {
		System.out.println("importData: " + comp);
		return super.importData(comp, t);
	}

	public int getSourceActions(JComponent comp) {
		return COPY_OR_MOVE;
	}

	public void exportToClipboard(JComponent comp, Clipboard clip, int action) throws IllegalStateException {
		System.out.println("exportToClipboard");
		super.exportToClipboard(comp, clip, action);
	}

	// Causes the Swing drag support to be initiated.
	public void exportAsDrag(JComponent comp, java.awt.event.InputEvent e, int action) {
		System.out.println("exportAsDrag");
		super.exportAsDrag(comp, e, action);
	}

	// Invoked after data has been exported.
	public void exportDone(JComponent source, Transferable data, int action) {
		System.out.println("exportDone");
		super.exportDone(source, data, action);
	}

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	/* DropTarget Methods */

	public final void drop(DropTargetDropEvent event) {
		System.out.println("drop: " + event.getSource());
		try {
			Transferable transferable = event.getTransferable();
			if (transferable.isDataFlavorSupported(WorkspaceTransferable.WORKSPACE_FILE_LIST_FLAVOR)) {
				event.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
				List<?> files = (List<?>) transferable.getTransferData(WorkspaceTransferable.WORKSPACE_FILE_LIST_FLAVOR);
				for (Object item : files) {
					System.out.println(item.toString());
				}
				event.getDropTargetContext().dropComplete(true);
			}
			else if (transferable.isDataFlavorSupported(WorkspaceTransferable.WORKSPACE_NODE_FLAVOR)) {
				event.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
				List<?> path = (List<?>) transferable.getTransferData(WorkspaceTransferable.WORKSPACE_NODE_FLAVOR);
				for (Object item : path) {
					System.out.println(item.toString());
				}
				event.getDropTargetContext().dropComplete(true);

			}
			else if (transferable.isDataFlavorSupported(WorkspaceTransferable.WORKSPACE_FREEPLANE_NODE_FLAVOR)) {
				event.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
				Object object = transferable.getTransferData(WorkspaceTransferable.WORKSPACE_FREEPLANE_NODE_FLAVOR);
				System.out.println(object.toString());
				event.getDropTargetContext().dropComplete(true);

			}
			else if (transferable.isDataFlavorSupported(WorkspaceTransferable.WORKSPACE_SERIALIZED_FLAVOR)) {
				event.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
				Object object = transferable.getTransferData(WorkspaceTransferable.WORKSPACE_SERIALIZED_FLAVOR);
				System.out.println(object.toString());
				event.getDropTargetContext().dropComplete(true);

			}
			else {
				event.rejectDrop();
			}
		}
		catch (Exception e) {
			event.rejectDrop();
		}
	}

	public final void dragEnter(DropTargetDragEvent dtde) {
		// System.out.println("dragEnter: " + dtde);
	}

	public final void dragExit(DropTargetEvent dte) {
		// System.out.println("dragExit: " + dte);
	}

	public final void dragOver(DropTargetDragEvent dtde) {
		// System.out.println("dragOver: " + dtde);
		autoscroll(this.tree, dtde.getLocation());
	}

	public final void dropActionChanged(DropTargetDragEvent dtde) {
		System.out.println("dropActionChanged: " + dtde);
	}

	/***********************************************************************************
	 * INTERNAL CLASSES
	 **********************************************************************************/
	
	

}
