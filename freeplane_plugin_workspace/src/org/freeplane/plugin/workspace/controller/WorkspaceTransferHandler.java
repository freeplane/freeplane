/**
 * author: Marcel Genzmehr
 * 27.07.2011
 */
package org.freeplane.plugin.workspace.controller;

import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.List;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.TreePath;

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

	private static final DataFlavor NODE_FLAVOR = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType, "MutableTreeNode");
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
		DropTarget dropTarget = new DropTarget(tree, this);
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
			Vector<File> paths = new Vector();
			JTree t = (JTree) comp;
			for(TreePath p : t.getSelectionPaths()) {
				if(p.getLastPathComponent() instanceof DefaultFileNode)
					paths.add(((DefaultFileNode)p.getLastPathComponent()).getFile());
			}
			if(paths.size() > 0) return new TransferableObject(paths);
			return new TransferableObject(t.getSelectionPaths());
		}
		return new TransferableObject(null);

	}
	
//	public boolean importData(TransferSupport support) {
//		System.out.println("importData: "+support);
//		return super.importData(support);
//	}
	
	public boolean importData(JComponent comp, Transferable t) {
		System.out.println("importData: "+comp);
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
			Point pt = event.getLocation();
			if (transferable.isDataFlavorSupported(NODE_FLAVOR)) {
				event.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
				event.getDropTargetContext().dropComplete(true);
				Object object = transferable.getTransferData(NODE_FLAVOR);
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

	class TransferableObject implements Transferable {
		private Object payload;

		public TransferableObject(Object load) {
			payload = load;
		}

		// Returns an object which represents the data to be transferred.
		public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
			if (flavor.equals(DataFlavor.javaFileListFlavor) && payload instanceof List) {
				return payload;
			}
			if (flavor.equals(NODE_FLAVOR) && payload instanceof TreePath[])
				return (new ByteArrayInputStream(payload.toString().getBytes()));
			if (flavor.equals(DataFlavor.stringFlavor) && payload instanceof TreePath[]) {
				StringBuffer buffer = new StringBuffer();
				for(TreePath path : (TreePath[])payload) {
					buffer.append(path.toString());
					buffer.append("\r\n");
				}
				return buffer.toString();
			}
			throw new UnsupportedFlavorException(flavor);
		}

		// Returns an array of DataFlavor objects indicating the flavors
		// the data can be provided in.
		public DataFlavor[] getTransferDataFlavors() {
			return new DataFlavor[] {DataFlavor.javaFileListFlavor, DataFlavor.stringFlavor, NODE_FLAVOR };
		}

		// Returns whether or not the specified data flavor is supported for
		// this object.
		public boolean isDataFlavorSupported(DataFlavor flavor) {
			System.out.println("isDataFlavorSupported: " + flavor);
			if(flavor.equals(DataFlavor.javaFileListFlavor) && payload instanceof List) {  
				return true;
			}
			if(flavor.equals(NODE_FLAVOR)) {
				return true;
			}
			if(flavor.equals(DataFlavor.stringFlavor)) {
				return true;
			}
			return false;
		}
	}

}
