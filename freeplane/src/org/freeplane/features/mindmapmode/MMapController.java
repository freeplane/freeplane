/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is created by Dimitry Polivaev in 2008.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.freeplane.features.mindmapmode;

import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ListIterator;

import javax.swing.JOptionPane;

import org.apache.commons.lang.StringUtils;
import org.freeplane.core.controller.Controller;
import org.freeplane.core.io.NodeBuilder;
import org.freeplane.core.modecontroller.INodeSelectionListener;
import org.freeplane.core.modecontroller.MapController;
import org.freeplane.core.model.EncryptionModel;
import org.freeplane.core.model.MapModel;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.FpStringUtils;
import org.freeplane.core.resources.IFreeplanePropertyListener;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.undo.IActor;
import org.freeplane.core.url.UrlManager;
import org.freeplane.features.mindmapmode.file.MFileManager;
import org.freeplane.n3.nanoxml.XMLParseException;

/**
 * @author Dimitry Polivaev
 */
public class MMapController extends MapController {
	static private DeleteAction delete;
	public static final int NEW_CHILD = 2;
	public static final int NEW_CHILD_WITHOUT_FOCUS = 1;
	public static final int NEW_SIBLING_BEFORE = 4;
	public static final int NEW_SIBLING_BEHIND = 3;
	static private NewChildAction newChild;
	public static final String RESOURCES_CONVERT_TO_CURRENT_VERSION = "convert_to_current_version";
	private static IFreeplanePropertyListener sSaveIdPropertyChangeListener;

	public MMapController(final MModeController modeController) {
		super(modeController);
		if (sSaveIdPropertyChangeListener == null) {
			sSaveIdPropertyChangeListener = new IFreeplanePropertyListener() {
				public void propertyChanged(final String propertyName, final String newValue, final String oldValue) {
					if (propertyName.equals("save_only_intrisically_needed_ids")) {
						MapController.setSaveOnlyIntrinsicallyNeededIds(Boolean.valueOf(newValue).booleanValue());
					}
				}
			};
			ResourceController.getResourceController().addPropertyChangeListenerAndPropagate(
			    sSaveIdPropertyChangeListener);
		}
		createActions(modeController);
		addNodeSelectionListener(new INodeSelectionListener() {
			public void onSelect(final NodeModel node) {
				if (ResourceController.getResourceController().getBooleanProperty("display_node_id")) {
					getController().getViewController().addStatusInfo("display_node_id", "ID=" + node.createID());
				}
			}

			public void onDeselect(final NodeModel node) {
				getController().getViewController().addStatusInfo("display_node_id", null);
			}
		});
	}

	public NodeModel addNewNode(final int newNodeMode, final KeyEvent e) {
		return newChild.addNewNode(newNodeMode, e);
	}

	public NodeModel addNewNode(final NodeModel parent, final int index, final boolean newNodeIsLeft) {
		return newChild.addNewNode(parent, index, newNodeIsLeft);
	}

	/**
	 * Return false if user has canceled.
	 */
	@Override
	public boolean close(final boolean force) {
		final MapModel map = getController().getMap();
		if (!force && !map.isSaved()) {
			final String text = ResourceBundles.getText("save_unsaved") + "\n" + map.getTitle();
			final String title = FpStringUtils.removeMnemonic(ResourceBundles.getText("SaveAction.text"));
			final int returnVal = JOptionPane.showOptionDialog(getController().getViewController().getContentPane(),
			    text, title, JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
			if (returnVal == JOptionPane.YES_OPTION) {
				final boolean savingNotCancelled = ((MFileManager) UrlManager.getController(getModeController()))
				    .save(map);
				if (!savingNotCancelled) {
					return false;
				}
			}
			else if ((returnVal == JOptionPane.CANCEL_OPTION) || (returnVal == JOptionPane.CLOSED_OPTION)) {
				return false;
			}
		}
		return super.close(force);
	}

	private void createActions(final MModeController modeController) {
		final Controller controller = modeController.getController();
		modeController.addAction(new NewMapAction(controller));
		modeController.addAction(new NewSiblingAction(controller));
		modeController.addAction(new NewPreviousSiblingAction(controller));
		newChild = new NewChildAction(controller);
		modeController.addAction(newChild);
		delete = new DeleteAction(controller);
		modeController.addAction(delete);
		modeController.addAction(new NodeUpAction(controller));
		modeController.addAction(new NodeDownAction(controller));
	}

	public void deleteNode(final NodeModel node) {
		delete.delete(node);
	}

	/**
	 */
	public void deleteWithoutUndo(final NodeModel selectedNode) {
		final NodeModel oldParent = selectedNode.getParentNode();
		firePreNodeDelete(oldParent, selectedNode, oldParent.getIndex(selectedNode));
		final MapModel map = selectedNode.getMap();
		setSaved(map, false);
		oldParent.remove(selectedNode);
		fireNodeDeleted(oldParent, selectedNode, oldParent.getIndex(selectedNode));
	}

	public MModeController getMModeController() {
		return (MModeController) getModeController();
	}

	public void insertNode(final NodeModel node, final NodeModel parent) {
		insertNode(node, parent, parent.getChildCount());
	}

	public void insertNode(final NodeModel node, final NodeModel target, final boolean asSibling, final boolean isLeft,
	                       final boolean changeSide) {
		NodeModel parent;
		if (asSibling) {
			parent = target.getParentNode();
		}
		else {
			parent = target;
		}
		if (changeSide) {
			node.setParent(parent);
			node.setLeft(isLeft);
		}
		if (asSibling) {
			insertNode(node, parent, parent.getChildPosition(target));
		}
		else {
			insertNode(node, parent, parent.getChildCount());
		}
	}

	public void insertNode(final NodeModel node, final NodeModel parentNode, final int index) {
		final IActor actor = new IActor() {
			public void act() {
				(getModeController().getMapController()).insertNodeIntoWithoutUndo(node, parentNode, index);
			}

			public String getDescription() {
				return "insertNode";
			}

			public void undo() {
				((MMapController) getModeController().getMapController()).deleteWithoutUndo(node);
			}
		};
		getModeController().execute(actor, node.getMap());
	}

	@Override
	public void insertNodeIntoWithoutUndo(final NodeModel newNode, final NodeModel parent, final int index) {
		setSaved(parent.getMap(), false);
		super.insertNodeIntoWithoutUndo(newNode, parent, index);
	}

	public boolean isWriteable(final NodeModel targetNode) {
		final EncryptionModel encryptionModel = EncryptionModel.getModel(targetNode);
		if (encryptionModel != null) {
			return encryptionModel.isAccessible();
		}
		return true;
	}

	public NodeModel loadTree(final MapModel map, final File file) throws XMLParseException, IOException {
		return ((MFileManager) UrlManager.getController(getModeController())).loadTree(map, file);
	}

	public void moveNode(final NodeModel node, final NodeModel targetNode, final boolean asSibling,
	                     final boolean isLeft, final boolean changeSide) {
		if (asSibling) {
			moveNodeBefore(node, targetNode, isLeft, changeSide);
		}
		else {
			moveNodeAsChild(node, targetNode, isLeft, changeSide);
		}
	}

	public void moveNode(final NodeModel node, final NodeModel directSibling, final int childCount) {
		moveNode(node, directSibling, childCount, false, false);
	}

	public void moveNode(final NodeModel child, final NodeModel newParent, final int newIndex, final boolean isLeft,
	                     final boolean changeSide) {
		final NodeModel oldParent = child.getParentNode();
		final int oldIndex = oldParent.getChildPosition(child);
		final boolean wasLeft = child.isLeft();
		if (oldParent == newParent && oldIndex == newIndex && changeSide == false) {
			return;
		}
		final IActor actor = new IActor() {
			public void act() {
				moveNodeToWithoutUndo(child, newParent, newIndex, isLeft, changeSide);
			}

			public String getDescription() {
				return "moveNode";
			}

			public void undo() {
				moveNodeToWithoutUndo(child, oldParent, oldIndex, wasLeft, changeSide);
			}
		};
		getModeController().execute(actor, newParent.getMap());
	}

	public void moveNodeAsChild(final NodeModel node, final NodeModel selectedParent, final boolean isLeft,
	                            final boolean changeSide) {
		int position = selectedParent.getChildCount();
		if (node.getParent() == selectedParent) {
			position--;
		}
		moveNode(node, selectedParent, position, isLeft, changeSide);
	}

	public void moveNodeBefore(final NodeModel node, final NodeModel target, final boolean isLeft,
	                           final boolean changeSide) {
		NodeModel parent;
		parent = target.getParentNode();
		moveNode(node, parent, parent.getChildPosition(target), isLeft, changeSide);
	}

	public void moveNodes(final NodeModel selected, final List selecteds, final int direction) {
		((NodeUpAction) getModeController().getAction("NodeUpAction")).moveNodes(selected, selecteds, direction);
	}

	/**
	 * The direction is used if side left and right are present. then the next
	 * suitable place on the same side# is searched. if there is no such place,
	 * then the side is changed.
	 *
	 * @return returns the new index.
	 */
	int moveNodeToWithoutUndo(final NodeModel child, final NodeModel newParent, final int newIndex,
	                          final boolean isLeft, final boolean changeSide) {
		final NodeModel oldParent = child.getParentNode();
		final int oldIndex = oldParent.getIndex(child);
		firePreNodeMoved(oldParent, oldIndex, newParent, child, newIndex);
		oldParent.remove(child);
		if (changeSide) {
			child.setParent(newParent);
			child.setLeft(isLeft);
		}
		newParent.insert(child, newIndex);
		fireNodeMoved(oldParent, oldIndex, newParent, child, newIndex);
		setSaved(newParent.getMap(), false);
		return newIndex;
	}

	@Override
	public MapModel newModel(final NodeModel root) {
		final MMapModel mindMapMapModel = new MMapModel(root, getModeController());
		fireMapCreated(mindMapMapModel);
		return mindMapMapModel;
	}

	/**
	 */
	@Override
	public void setFolded(final NodeModel node, final boolean folded) {
		if (node.getChildCount() == 0) {
			return;
		}
		if (node.isFolded() == folded) {
			return;
		}
		toggleFolded(node);
	}

	@Override
	public void toggleFolded() {
		toggleFolded(getSelectedNodes().listIterator());
	}

	@Override
	public void toggleFolded(final ListIterator listIterator) {
		while (listIterator.hasNext()) {
			toggleFolded((NodeModel) listIterator.next());
		}
	}

	private void toggleFolded(final NodeModel node) {
		if (!getModeController().getMapController().hasChildren(node)
		        && !StringUtils.equals(ResourceController.getResourceController().getProperty("enable_leaves_folding"),
		            "true")) {
			return;
		}
		final IActor actor = new IActor() {
			public void act() {
				_setFolded(node, !node.isFolded());
				final ResourceController resourceController = ResourceController.getResourceController();
				if (resourceController.getProperty(NodeBuilder.RESOURCES_SAVE_FOLDING).equals(
				    NodeBuilder.RESOURCES_ALWAYS_SAVE_FOLDING)) {
					setSaved(node.getMap(), false);
				}
				nodeRefresh(node);
			}

			public String getDescription() {
				return "toggleFolded";
			}

			public void undo() {
				act();
			}
		};
		getModeController().execute(actor, node.getMap());
	}
}
