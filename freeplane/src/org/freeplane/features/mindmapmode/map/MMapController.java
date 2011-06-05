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
package org.freeplane.features.mindmapmode.map;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.JOptionPane;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.controller.IMapSelection;
import org.freeplane.core.controller.INodeSelectionListener;
import org.freeplane.core.frame.ViewController;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.undo.IActor;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.common.map.EncryptionModel;
import org.freeplane.features.common.map.MapController;
import org.freeplane.features.common.map.MapModel;
import org.freeplane.features.common.map.ModeController;
import org.freeplane.features.common.map.NodeModel;
import org.freeplane.features.common.styles.MapStyleModel;
import org.freeplane.features.common.styles.MapViewLayout;
import org.freeplane.features.common.text.TextController;
import org.freeplane.features.common.url.UrlManager;
import org.freeplane.features.mindmapmode.MModeController;
import org.freeplane.features.mindmapmode.file.MFileManager;
import org.freeplane.features.mindmapmode.text.MTextController;
import org.freeplane.n3.nanoxml.XMLParseException;

/**
 * @author Dimitry Polivaev
 */
public class MMapController extends MapController {
	public static final int NEW_CHILD = 2;
	public static final int NEW_CHILD_WITHOUT_FOCUS = 1;
	public static final int NEW_SIBLING_BEFORE = 4;
	public static final int NEW_SIBLING_BEHIND = 3;
	public static final String RESOURCES_CONVERT_TO_CURRENT_VERSION = "convert_to_current_version";

	public MMapController(ModeController modeController) {
		super(modeController);
		createActions();
			addNodeSelectionListener(new INodeSelectionListener() {
				public void onSelect(final NodeModel node) {
					final ViewController viewController = Controller.getCurrentController().getViewController();
					if (ResourceController.getResourceController().getBooleanProperty("display_node_id")) {
						viewController.addStatusInfo("display_node_id", "ID=" + node.createID(), null);
					}
				}

				public void onDeselect(final NodeModel node) {
					final ViewController viewController = Controller.getCurrentController().getViewController();
					viewController.addStatusInfo("display_node_id", null, null);
				}
			});
	}

	public NodeModel addNewNode(int newNodeMode, final KeyEvent e) {
		final ModeController modeController = Controller.getCurrentModeController();
		final TextController textController = TextController.getController();
		if (textController instanceof MTextController) {
			((MTextController) textController).stopEditing();
		}
		final NodeModel target = getSelectedNode();
		if (textController instanceof MTextController) {
			modeController.startTransaction();
			try {
				((MTextController) TextController.getController()).stopEditing();
			}
			finally {
				modeController.commit();
			}
		}
		final NodeModel targetNode = target;
		final NodeModel newNode;
		switch (newNodeMode) {
			case MMapController.NEW_SIBLING_BEFORE:
			case MMapController.NEW_SIBLING_BEHIND: {
				if (!targetNode.isRoot()) {
					final NodeModel parent = targetNode.getParentNode();
					int childPosition = parent.getChildPosition(targetNode);
					if (newNodeMode == MMapController.NEW_SIBLING_BEHIND) {
						childPosition++;
					}
					newNode = addNewNode(parent, childPosition, targetNode.isLeft());
					if (newNode == null) {
						return null;
					}
					select(newNode);
					if (e != null) {
						((MTextController) textController).edit(newNode, targetNode, e, true, false, false);
					}
					else {
						EventQueue.invokeLater(new Runnable() {
							public void run() {
								((MTextController) textController).edit(newNode, targetNode, e, true, false, false);
							}
						});
					}
					break;
				}
				else {
					newNodeMode = MMapController.NEW_CHILD;
				}
			}
			case MMapController.NEW_CHILD:
			case MMapController.NEW_CHILD_WITHOUT_FOCUS: {
				final boolean parentFolded = isFolded(targetNode);
				if (parentFolded) {
					setFolded(targetNode, false);
				}
				final int position = ResourceController.getResourceController().getProperty("placenewbranches").equals(
				    "last") ? targetNode.getChildCount() : 0;
				newNode = addNewNode(targetNode, position, targetNode.isNewChildLeft());
				if (newNode == null) {
					return null;
				}
				if (newNodeMode == MMapController.NEW_CHILD) {
					select(newNode);
				}
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						((MTextController) textController).edit(newNode, targetNode, e, true, parentFolded, false);
					}
				});
				break;
			}
			default:
				newNode = null;
		}
		return newNode;
	}

	public NodeModel addNewNode(final NodeModel parent, final int index, final boolean newNodeIsLeft) {
		final MMapController mapController = (MMapController) Controller.getCurrentModeController().getMapController();
		if (!mapController.isWriteable(parent)) {
			final String message = TextUtils.getText("node_is_write_protected");
			UITools.errorMessage(message);
			return null;
		}
		final MapModel map = parent.getMap();
		final NodeModel newNode = Controller.getCurrentModeController().getMapController().newNode("", map);
		newNode.setLeft(newNodeIsLeft);
		final IActor actor = new IActor() {
			public void act() {
				(Controller.getCurrentModeController().getMapController()).insertNodeIntoWithoutUndo(newNode, parent, index);
			}

			public String getDescription() {
				return "addNewNode";
			}

			public void undo() {
				mapController.deleteWithoutUndo(newNode);
			}
		};
		Controller.getCurrentModeController().execute(actor, map);
		return newNode;
	}

	/**
	 * Return false if user has canceled.
	 */
	@Override
	public boolean close(final boolean force) {
		final MapModel map = Controller.getCurrentController().getMap();
		if (!force && !map.isSaved()) {
			final List<Component> views = Controller.getCurrentController().getMapViewManager().getViews(map);
			if (views.size() == 1) {
				final String text = TextUtils.getText("save_unsaved") + "\n" + map.getTitle();
				final String title = TextUtils.removeMnemonic(TextUtils.getText("SaveAction.text"));
				final int returnVal = JOptionPane.showOptionDialog(
				    Controller.getCurrentController().getViewController().getContentPane(), text, title,
				    JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
				if (returnVal == JOptionPane.YES_OPTION) {
					final boolean savingNotCancelled = ((MFileManager) UrlManager.getController())
					    .save(map);
					if (!savingNotCancelled) {
						return false;
					}
				}
				else if ((returnVal == JOptionPane.CANCEL_OPTION) || (returnVal == JOptionPane.CLOSED_OPTION)) {
					return false;
				}
			}
		}
		return super.close(force);
	}

	private void createActions() {
		final ModeController modeController = Controller.getCurrentModeController();
		modeController.addAction(new NewMapViewAction());
		modeController.addAction(new NewSiblingAction());
		modeController.addAction(new NewPreviousSiblingAction());
		modeController.addAction(new NewChildAction());
		modeController.addAction(new NewSummaryAction());
		modeController.addAction(new DeleteAction());
		modeController.addAction(new NodeUpAction());
		modeController.addAction(new NodeDownAction());
	}

	public void deleteNode(final NodeModel node) {
		final NodeModel parentNode = node.getParentNode();
		final int index = parentNode.getIndex(node);
		final IActor actor = new IActor() {
        	public void act() {
        		deleteWithoutUndo(node);
        	}
        
        	public String getDescription() {
        		return "delete";
        	}
        
        	public void undo() {
        		(Controller.getCurrentModeController().getMapController()).insertNodeIntoWithoutUndo(node, parentNode, index);
        	}
        };
		Controller.getCurrentModeController().execute(actor, node.getMap());
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
		return (MModeController) Controller.getCurrentModeController();
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
				(Controller.getCurrentModeController().getMapController()).insertNodeIntoWithoutUndo(node, parentNode, index);
			}

			public String getDescription() {
				return "insertNode";
			}

			public void undo() {
				((MMapController) Controller.getCurrentModeController().getMapController()).deleteWithoutUndo(node);
			}
		};
		Controller.getCurrentModeController().execute(actor, node.getMap());
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
		return ((MFileManager) UrlManager.getController()).loadTree(map, file);
	}

	public void moveNode(final NodeModel child, final NodeModel newParent, final int childCount) {
		moveNode(child, newParent, childCount, false, false);
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
		Controller.getCurrentModeController().execute(actor, newParent.getMap());
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
        final NodeModel newParent = target.getParentNode();
        final NodeModel oldParent = node.getParentNode();
		int newIndex = newParent.getChildPosition(target);
	    if(newParent.equals(oldParent)){ 
	        final int oldIndex = oldParent.getChildPosition(node);
            if(oldIndex < newIndex)
                newIndex--;
	    }
        moveNode(node, newParent, newIndex, isLeft, changeSide);
	}

	public void moveNodes(final NodeModel selected, final List<NodeModel> selecteds, final int direction) {
        final IActor actor = new IActor() {
            public void act() {
                _moveNodes(selected, selecteds, direction);
            }

            public String getDescription() {
                return "moveNodes";
            }

            public void undo() {
                _moveNodes(selected, selecteds, -direction);
            }
        };
        Controller.getCurrentModeController().execute(actor, selected.getMap());
	}
    private void _moveNodes(final NodeModel selected, final List<NodeModel> selecteds, final int direction) {
        final Comparator<Object> comparator = (direction == -1) ? null : new Comparator<Object>() {
            public int compare(final Object o1, final Object o2) {
                final int i1 = ((Integer) o1).intValue();
                final int i2 = ((Integer) o2).intValue();
                return i2 - i1;
            }
        };
        if (!selected.isRoot()) {
            final NodeModel parent = selected.getParentNode();
            final Vector<NodeModel> sortedChildren = getSortedSiblings(parent);
            final TreeSet<Integer> range = new TreeSet<Integer>(comparator);
            for (final NodeModel node : selecteds) {
                if (node.getParent() != parent) {
                    LogUtils.warn("Not all selected nodes have the same parent.");
                    return;
                }
                range.add(new Integer(sortedChildren.indexOf(node)));
            }
            Integer last = range.iterator().next();
            for (final Integer newInt : range) {
                if (Math.abs(newInt.intValue() - last.intValue()) > 1) {
                    LogUtils.warn("Not adjacent nodes. Skipped. ");
                    return;
                }
                last = newInt;
            }
            for (final Integer position : range) {
                final NodeModel node = sortedChildren.get(position.intValue());
                moveNodeTo(node, direction);
            }
            final IMapSelection selection = Controller.getCurrentController().getSelection();
            selection.selectAsTheOnlyOneSelected(selected);
            for (final Integer position : range) {
                final NodeModel node = sortedChildren.get(position.intValue());
                selection.makeTheSelected(node);
            }
            Controller.getCurrentController().getViewController().obtainFocusForSelected();
        }
    }
    private int moveNodeTo(final NodeModel child, final int direction) {
        final NodeModel parent = child.getParentNode();
        final int index = parent.getIndex(child);
        int newIndex = index;
        final int maxIndex = parent.getChildCount();
        final Vector<NodeModel> sortedNodesIndices = getSortedSiblings(parent);
        int newPositionInVector = sortedNodesIndices.indexOf(child) + direction;
        if (newPositionInVector < 0) {
            newPositionInVector = maxIndex - 1;
        }
        if (newPositionInVector >= maxIndex) {
            newPositionInVector = 0;
        }
        final NodeModel destinationNode = (NodeModel) sortedNodesIndices.get(newPositionInVector);
        newIndex = parent.getIndex(destinationNode);
        ((MMapController) Controller.getCurrentModeController().getMapController()).moveNodeToWithoutUndo(child, parent, newIndex, false,
            false);
        return newIndex;
    }
    /**
     * Sorts nodes by their left/right status. The left are first.
     */
    private Vector<NodeModel> getSortedSiblings(final NodeModel node) {
        final Vector<NodeModel> nodes = new Vector<NodeModel>();
        for (final NodeModel child : Controller.getCurrentModeController().getMapController().childrenUnfolded(node)) {
            nodes.add(child);
        }
        if(! node.isRoot()){
            return nodes;
        }
        final MapStyleModel mapStyleModel = MapStyleModel.getExtension(node.getMap());
        MapViewLayout layoutType = mapStyleModel.getMapViewLayout();
        if(layoutType.equals(MapViewLayout.OUTLINE)){
            return nodes;
        }

        Collections.sort(nodes, new Comparator<Object>() {
            public int compare(final Object o1, final Object o2) {
                if (o1 instanceof NodeModel) {
                    final NodeModel n1 = (NodeModel) o1;
                    if (o2 instanceof NodeModel) {
                        final NodeModel n2 = (NodeModel) o2;
                        final int b1 = n1.isLeft() ? 0 : 1;
                        final int b2 = n2.isLeft() ? 0 : 1;
                        return b1 - b2;
                    }
                }
                throw new IllegalArgumentException("Elements in LeftRightComparator are not comparable.");
            }
        });
        return nodes;
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
		final MMapModel mindMapMapModel = new MMapModel(root);
		fireMapCreated(mindMapMapModel);
		return mindMapMapModel;
	}


	public void setSaved(final MapModel mapModel, final boolean saved) {
		final boolean setTitle = saved != mapModel.isSaved();
		mapModel.setSaved(saved);
		if (setTitle) {
			Controller.getCurrentModeController().getController().getViewController().setTitle();
		}
	}
}
