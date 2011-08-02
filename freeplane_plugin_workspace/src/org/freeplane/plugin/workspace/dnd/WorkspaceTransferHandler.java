/**
 * author: Marcel Genzmehr
 * 27.07.2011
 */
package org.freeplane.plugin.workspace.dnd;

import java.awt.Cursor;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceContext;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Arrays;
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

	private JTree tree;
	private IWorkspaceDragController controller;
	protected DefaultDragGestureRecognizer recognizer;

	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	public WorkspaceTransferHandler(JTree tree, IWorkspaceDragController controller, int action, boolean drawIcon) {
		this.controller = controller;

		this.tree = tree;
		this.tree.setTransferHandler(this);
		this.tree.setDragEnabled(true);
		this.tree.setAutoscrolls(true);
		DropTarget dropTarget = new DropTarget(tree, COPY_OR_MOVE, this);
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
				if (p.getLastPathComponent() instanceof DefaultFileNode)
					paths.add(((DefaultFileNode) p.getLastPathComponent()).getFile());
			}
			if (paths.size() > 0)
				return new WorkspaceTransferable(WorkspaceTransferable.WORKSPACE_FILE_LIST_FLAVOR, paths);
			return new WorkspaceTransferable(WorkspaceTransferable.WORKSPACE_NODE_FLAVOR, Arrays.asList(t.getSelectionPaths()));
		}
		return new WorkspaceTransferable(WorkspaceTransferable.WORKSPACE_SERIALIZED_FLAVOR, "");

	}

	// public boolean importData(TransferSupport support) {
	// System.out.println("importData: "+support);
	// return super.importData(support);
	// }

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
		int srcActions = getSourceActions(comp);
		int dragAction = srcActions & action;
		if (!(e instanceof MouseEvent)) {
			// only mouse events supported for drag operations
			dragAction = NONE;
		}
		if (dragAction != NONE && !GraphicsEnvironment.isHeadless()) {
			if (recognizer == null) {
				recognizer = new DefaultDragGestureRecognizer(new DefaultDragHandler());
				recognizer.getDragSource().addDragSourceListener(new DefaultDragSourceListener());
			}

			recognizer.gestured(comp, (MouseEvent) e, srcActions, dragAction);
		}
		else {
			exportDone(comp, null, NONE);
		}
		//super.exportAsDrag(comp, e, action);
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

	private static class DefaultDragGestureRecognizer extends DragGestureRecognizer {

		DefaultDragGestureRecognizer(DragGestureListener dgl) {
			super(DragSource.getDefaultDragSource(), null, DnDConstants.ACTION_COPY_OR_MOVE, dgl);
			this.getDragSource().addDragSourceListener(new WorkspaceTransferHandler.DefaultDragHandler());
		}

		void gestured(JComponent c, MouseEvent e, int srcActions, int action) {
			setComponent(c);
			setSourceActions(srcActions);
			appendEvent(e);
			fireDragGestureRecognized(action, e.getPoint());
		}

		/**
		 * register this DragGestureRecognizer's Listeners with the Component
		 */
		protected void registerListeners() {
		}

		/**
		 * unregister this DragGestureRecognizer's Listeners with the Component
		 * 
		 * subclasses must override this method
		 */
		protected void unregisterListeners() {
		}

	}

	protected static class DefaultDragHandler implements DragGestureListener, DragSourceListener {

		private boolean scrolls;

		// --- DragGestureListener methods -----------------------------------

		/**
		 * a Drag gesture has been recognized
		 */
		public void dragGestureRecognized(DragGestureEvent dge) {
			JComponent c = (JComponent) dge.getComponent();
			WorkspaceTransferHandler th = (WorkspaceTransferHandler) c.getTransferHandler();
			Transferable t = th.createTransferable(c);
			if (t != null) {
				scrolls = c.getAutoscrolls();
				c.setAutoscrolls(false);
				try {
					dge.startDrag(null, t, this);
					return;
				}
				catch (RuntimeException re) {
					c.setAutoscrolls(scrolls);
				}
			}

			th.exportDone(c, t, NONE);
		}

		// --- DragSourceListener methods -----------------------------------

		/**
		 * as the hotspot enters a platform dependent drop site
		 */
		public void dragEnter(DragSourceDragEvent dsde) {
		}

		/**
		 * as the hotspot moves over a platform dependent drop site
		 */
		public void dragOver(DragSourceDragEvent dsde) {
		}

		/**
		 * as the hotspot exits a platform dependent drop site
		 */
		public void dragExit(DragSourceEvent dsde) {
		}

		/**
		 * as the operation completes
		 */
		public void dragDropEnd(DragSourceDropEvent dsde) {
			DragSourceContext dsc = dsde.getDragSourceContext();
			JComponent c = (JComponent) dsc.getComponent();
			if (dsde.getDropSuccess()) {
				((WorkspaceTransferHandler) c.getTransferHandler()).exportDone(c, dsc.getTransferable(), dsde.getDropAction());
			}
			else {
				((WorkspaceTransferHandler) c.getTransferHandler()).exportDone(c, dsc.getTransferable(), NONE);
			}
			c.setAutoscrolls(scrolls);
		}

		public void dropActionChanged(DragSourceDragEvent dsde) {
		}
	}
	
	private class DefaultDragSourceListener implements DragSourceListener {

		/* (non-Javadoc)
		 * @see java.awt.dnd.DragSourceListener#dragEnter(java.awt.dnd.DragSourceDragEvent)
		 */
		public void dragEnter(DragSourceDragEvent dsde) {
			// TODO Auto-generated method stub
			System.out.println("DragSourceDragEvent"); 
		}

		/* (non-Javadoc)
		 * @see java.awt.dnd.DragSourceListener#dragOver(java.awt.dnd.DragSourceDragEvent)
		 */
		public void dragOver(DragSourceDragEvent dsde) {
			// TODO Auto-generated method stub
			
		}

		/* (non-Javadoc)
		 * @see java.awt.dnd.DragSourceListener#dropActionChanged(java.awt.dnd.DragSourceDragEvent)
		 */
		public void dropActionChanged(DragSourceDragEvent dsde) {
			// TODO Auto-generated method stub
			
		}

		/* (non-Javadoc)
		 * @see java.awt.dnd.DragSourceListener#dragExit(java.awt.dnd.DragSourceEvent)
		 */
		public void dragExit(DragSourceEvent dse) {
			// TODO Auto-generated method stub
			
		}

		/* (non-Javadoc)
		 * @see java.awt.dnd.DragSourceListener#dragDropEnd(java.awt.dnd.DragSourceDropEvent)
		 */
		public void dragDropEnd(DragSourceDropEvent dsde) {
			// TODO Auto-generated method stub
			System.out.println("dragDropEnd");			
		}
		
	}

}
