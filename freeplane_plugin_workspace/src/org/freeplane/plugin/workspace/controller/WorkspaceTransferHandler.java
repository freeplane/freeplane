/**
 * author: Marcel Genzmehr
 * 27.07.2011
 */
package org.freeplane.plugin.workspace.controller;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.tree.TreePath;

import org.freeplane.features.clipboard.MindMapNodesSelection;

/**
 * 
 */
public class WorkspaceTransferHandler implements DragSourceListener, DragGestureListener, DropTargetListener {

	private static final Insets DEFAULT_INSETS = new Insets(20, 20, 20, 20);
	private static final DataFlavor NODE_FLAVOR = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType, "Node");

	private Object draggedNode;
	private BufferedImage image = null; // buff image

	private JTree tree;
	private IWorkspaceDragController controller;
	private DragSource dragSource; // dragsource
	private Rectangle rect2D = new Rectangle();
	private boolean drawImage;

	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	public WorkspaceTransferHandler(JTree tree, IWorkspaceDragController controller, int action, boolean drawIcon) {
		this.tree = tree;
		this.controller = controller;
		drawImage = drawIcon;
		dragSource = new DragSource();
		dragSource.createDefaultDragGestureRecognizer(tree, action, this);
	}

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	public static WorkspaceTransferHandler configureDragAndDrop(JTree tree, IWorkspaceDragController controller) {
        tree.setAutoscrolls(true);
        return new WorkspaceTransferHandler(tree, controller, DnDConstants.ACTION_COPY_OR_MOVE, true);
    }

	private static void autoscroll(JTree tree, Point cursorLocation) {
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

	private final void paintImage(Point pt) {
		tree.paintImmediately(rect2D.getBounds());
		rect2D.setRect((int) pt.getX(), (int) pt.getY(), image.getWidth(), image.getHeight());
		tree.getGraphics().drawImage(image, (int) pt.getX(), (int) pt.getY(), tree);
	}

	private final void clearImage() {
		tree.paintImmediately(rect2D.getBounds());
	}

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/

	public void dragGestureRecognized(DragGestureEvent dge) {
		TreePath path = tree.getSelectionPath();
		if (path != null) {
			draggedNode = path.getLastPathComponent();
			if (drawImage) {
				Rectangle pathBounds = tree.getPathBounds(path); // getpathbounds
																	// of
																	// selectionpath
				JComponent lbl = (JComponent) tree.getCellRenderer().getTreeCellRendererComponent(tree, draggedNode, false,
						tree.isExpanded(path), tree.getModel().isLeaf(path.getLastPathComponent()), 0, false);// returning
																												// the
																												// label
				lbl.setBounds(pathBounds);// setting bounds to lbl
				image = new BufferedImage(lbl.getWidth(), lbl.getHeight(), java.awt.image.BufferedImage.TYPE_INT_ARGB_PRE);// buffered
																															// image
																															// reference
																															// passing
																															// the
																															// label's
																															// ht
																															// and
																															// width
				Graphics2D graphics = image.createGraphics();// creating
																// the
																// graphics
																// for
																// buffered
																// image
				graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f)); // Sets
																									// the
																									// Composite
																									// for
																									// the
																									// Graphics2D
																									// context
				lbl.setOpaque(false);
				lbl.paint(graphics); // painting the graphics to label
				graphics.dispose();
			}
			//dragSource.startDrag(dge, DragSource.DefaultMoveNoDrop, image, new Point(0, 0), new TransferableNode(draggedNode), this);
			//dragSource.startDrag(dge, DragSource.DefaultMoveNoDrop, image, new Point(0, 0), new MindMapNodesSelection(null, null, null, "Hallo", null, null), this);
			dragSource.startDrag(dge, DragSource.DefaultMoveDrop, new MindMapNodesSelection(null, null, null, "Hallo", null, null), this);
		}

	}

	/* DragSource Methods */

	public void dragDropEnd(DragSourceDropEvent dsde) {
		/*
		 * if(dsde.getDropSuccess() && dsde.getDropAction() ==
		 * DnDConstants.ACTION_MOVE && draggedNodeParent != null) {
		 * ((DefaultTreeModel) tree.getModel())
		 * .nodeStructureChanged(draggedNodeParent); }
		 */
	}

	public final void dragEnter(DragSourceDragEvent dsde) {
		System.out.println("dragEnter: "+dsde);
		int action = dsde.getDropAction();
		if (action == DnDConstants.ACTION_COPY) {
			dsde.getDragSourceContext().setCursor(DragSource.DefaultCopyDrop);
		}
		else {
			if (action == DnDConstants.ACTION_MOVE) {
				dsde.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
			}
			else {
				dsde.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
			}
		}
	}

	public final void dragOver(DragSourceDragEvent dsde) {
		System.out.println("dragOver: "+dsde);
		int action = dsde.getDropAction();
		if (action == DnDConstants.ACTION_COPY) {
			dsde.getDragSourceContext().setCursor(DragSource.DefaultCopyDrop);
		}
		else {
			if (action == DnDConstants.ACTION_MOVE) {
				dsde.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
			}
			else {
				dsde.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
			}
		}
	}

	public final void dropActionChanged(DragSourceDragEvent dsde) {
		int action = dsde.getDropAction();
		if (action == DnDConstants.ACTION_COPY) {
			dsde.getDragSourceContext().setCursor(DragSource.DefaultCopyDrop);
		}
		else {
			if (action == DnDConstants.ACTION_MOVE) {
				dsde.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
			}
			else {
				dsde.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
			}
		}
	}

	public final void dragExit(DragSourceEvent dse) {
		dse.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
	}

	/* DropTarget Methods */

	public final void dragEnter(DropTargetDragEvent dtde) {
		Point pt = dtde.getLocation();
		int action = dtde.getDropAction();
		if (drawImage) {
			paintImage(pt);
		}
		if (controller.canPerformAction(tree, draggedNode, action, pt)) {
			dtde.acceptDrag(action);
		}
		else {
			dtde.rejectDrag();
		}
	}

	public final void dragExit(DropTargetEvent dte) {
		if (drawImage) {
			clearImage();
		}
	}

	public final void dragOver(DropTargetDragEvent dtde) {
		Point pt = dtde.getLocation();
		int action = dtde.getDropAction();
		autoscroll(tree, pt);
		if (drawImage) {
			paintImage(pt);
		}
		if (controller.canPerformAction(tree, draggedNode, action, pt)) {
			dtde.acceptDrag(action);
		}
		else {
			dtde.rejectDrag();
		}
	}

	public final void dropActionChanged(DropTargetDragEvent dtde) {
		Point pt = dtde.getLocation();
		int action = dtde.getDropAction();
		if (drawImage) {
			paintImage(pt);
		}
		if (controller.canPerformAction(tree, draggedNode, action, pt)) {
			dtde.acceptDrag(action);
		}
		else {
			dtde.rejectDrag();
		}
	}

	public final void drop(DropTargetDropEvent dtde) {
		try {
			if (drawImage) {
				clearImage();
			}
			int action = dtde.getDropAction();
			Transferable transferable = dtde.getTransferable();
			Point pt = dtde.getLocation();
			if (transferable.isDataFlavorSupported(NODE_FLAVOR) && controller.canPerformAction(tree, draggedNode, action, pt)) {
				TreePath pathTarget = tree.getPathForLocation(pt.x, pt.y);
				Object node = transferable.getTransferData(NODE_FLAVOR);
				Object newParentNode = pathTarget.getLastPathComponent();
				if (controller.executeDrop(tree, node, newParentNode, action)) {
					dtde.acceptDrop(action);
					dtde.dropComplete(true);
					return;
				}
			}
			dtde.rejectDrop();
			dtde.dropComplete(false);
		}
		catch (Exception e) {
			dtde.rejectDrop();
			dtde.dropComplete(false);
		}
	}

}
