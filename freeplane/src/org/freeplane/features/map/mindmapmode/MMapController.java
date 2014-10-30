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
package org.freeplane.features.map.mindmapmode;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.JOptionPane;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.undo.IActor;
import org.freeplane.core.util.Compat;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.EncryptionModel;
import org.freeplane.features.map.FirstGroupNode;
import org.freeplane.features.map.FreeNode;
import org.freeplane.features.map.IMapSelection;
import org.freeplane.features.map.INodeSelectionListener;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.SummaryNode;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.features.nodelocation.mindmapmode.MLocationController;
import org.freeplane.features.styles.LogicalStyleModel;
import org.freeplane.features.styles.MapStyleModel;
import org.freeplane.features.styles.MapViewLayout;
import org.freeplane.features.styles.mindmapmode.MLogicalStyleController;
import org.freeplane.features.text.TextController;
import org.freeplane.features.text.mindmapmode.MTextController;
import org.freeplane.features.ui.IMapViewManager;
import org.freeplane.features.ui.ViewController;
import org.freeplane.features.url.UrlManager;
import org.freeplane.features.url.mindmapmode.MFileManager;
import org.freeplane.features.url.mindmapmode.MFileManager.AlternativeFileMode;
import org.freeplane.main.addons.AddOnsController;
import org.freeplane.n3.nanoxml.XMLException;

/**
 * @author Dimitry Polivaev
 */
public class MMapController extends MapController {
	public static final int NEW_CHILD = 2;
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

	public NodeModel addNewNode(int newNodeMode) {
		stopEditing();
		final NodeModel targetNode = getSelectedNode();
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
					startEditingAfterSelect(newNode);
					select(newNode);
					break;
				}
				else {
					newNodeMode = MMapController.NEW_CHILD;
				}
			}
			case MMapController.NEW_CHILD: {
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
				startEditingAfterSelect(newNode);
				select(newNode);
				break;
			}
			default:
				newNode = null;
		}
		return newNode;
	}

	private void startEditingAfterSelect(final NodeModel newNode) {
		final Component component = Controller.getCurrentController().getMapViewManager().getComponent(newNode);
		if(component == null)
			return;
		component.addFocusListener(new FocusListener() {
			public void focusLost(FocusEvent e) {
			}

			public void focusGained(FocusEvent e) {
				e.getComponent().removeFocusListener(this);
				final TextController textController = TextController.getController();
				((MTextController) textController).edit(newNode, newNode.getParentNode(), true, false, false);
			}
		});
    }

	private void stopEditing() {
		final ModeController modeController = Controller.getCurrentModeController();
		final TextController textController = TextController.getController();
		if (textController instanceof MTextController) {
			((MTextController) textController).stopEditing();
		}
		if (textController instanceof MTextController) {
			modeController.startTransaction();
			try {
				((MTextController) TextController.getController()).stopEditing();
			}
			finally {
				modeController.commit();
			}
		}
    }

	public void addNewSummaryNodeStartEditing(final int summaryLevel, final int start, final int end){
		stopEditing();
		ModeController modeController = getMModeController();
		final IMapSelection selection = modeController.getController().getSelection();
		NodeModel selected = selection.getSelected();
		final NodeModel parentNode = selected.getParentNode();
		final boolean isLeft = selected.isLeft();
		final NodeModel newNode = addNewNode(parentNode, end+1, isLeft);
		final SummaryNode summary = modeController.getExtension(SummaryNode.class);
		summary.undoableActivateHook(newNode, summary);
		final FirstGroupNode firstGroup = modeController.getExtension(FirstGroupNode.class);
		final NodeModel firstNode = (NodeModel) parentNode.getChildAt(start);
		firstGroup.undoableActivateHook(firstNode, firstGroup);
		int level = summaryLevel;
		for(int i = start+1; i < end; i++){
			NodeModel node = (NodeModel) parentNode.getChildAt(i);
			if(isLeft != node.isLeft())
				continue;
			if(SummaryNode.isSummaryNode(node))
				level++;
			else
				level = 0;
			if(level == summaryLevel && SummaryNode.isFirstGroupNode(node))
				firstGroup.undoableActivateHook(node, firstGroup);
		}
		startEditingAfterSelect(newNode);
		select(newNode);

	}

	public NodeModel addNewNode(final NodeModel parent, final int index, final boolean newNodeIsLeft) {
		if (!isWriteable(parent)) {
			UITools.errorMessage(TextUtils.getText("node_is_write_protected"));
			return null;
		}
		final NodeModel newNode = newNode("", parent.getMap());
		if(addNewNode(newNode, parent, index, newNodeIsLeft))
			return newNode;
		else
			return null;
	}

	public boolean addNewNode(final NodeModel newNode, final NodeModel parent, final int index, final boolean newNodeIsLeft) {
		if (!isWriteable(parent)) {
			UITools.errorMessage(TextUtils.getText("node_is_write_protected"));
			return false;
		}
	    final MapModel map = parent.getMap();
		newNode.setLeft(newNodeIsLeft);
		final IActor actor = new IActor() {
			public void act() {
				insertNodeIntoWithoutUndo(newNode, parent, index);
			}

			public String getDescription() {
				return "addNewNode";
			}

			public void undo() {
				deleteWithoutUndo(newNode);
			}
		};
		Controller.getCurrentModeController().execute(actor, map);
		return true;
    }

	/**
	 * Return false if user has canceled.
	 */
	@Override
	public boolean close(final MapModel map, final boolean force) {
		if (!force && !map.isSaved()) {
			final List<Component> views = Controller.getCurrentController().getMapViewManager().getViews(map);
			if (views.size() == 1) {
				final String text = TextUtils.getText("save_unsaved") + "\n" + map.getTitle();
				final String title = TextUtils.getText("SaveAction.text");
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
		return super.close(map, force);
	}

	private void createActions() {
		final ModeController modeController = Controller.getCurrentModeController();
		modeController.addAction(new NewMapViewAction());
		modeController.addAction(new NewSiblingAction());
		modeController.addAction(new NewPreviousSiblingAction());
		modeController.addAction(new NewChildAction());
		modeController.addAction(new NewSummaryAction());
		modeController.addAction(new NewFreeNodeAction());
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

	public void moveNode(NodeModel node, int i) {
		   moveNode(node, node.getParentNode(), i);
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
		FreeNode r = Controller.getCurrentModeController().getExtension(FreeNode.class);
		final IExtension extension = node.getExtension(FreeNode.class);
        if (extension != null) {
        	r.undoableToggleHook(node, extension);
        	if (MapStyleModel.FLOATING_STYLE.equals(LogicalStyleModel.getStyle(node)))
        		((MLogicalStyleController)MLogicalStyleController.getController(getMModeController())).setStyle(node, null);
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
		Controller.getCurrentModeController().getExtension(FreeNode.class).undoableDeactivateHook(node);
        moveNode(node, newParent, newIndex, isLeft, changeSide);
	}

	public void moveNodes(NodeModel selected, Collection<NodeModel> movedNodes, final int direction) {
		final List<NodeModel> mySelecteds = new ArrayList<NodeModel>(movedNodes);
        final IActor actor = new IActor() {
            public void act() {
				_moveNodes(mySelecteds, direction);
            }

            public String getDescription() {
                return "moveNodes";
            }

            public void undo() {
				_moveNodes(mySelecteds, -direction);
            }
        };
        Controller.getCurrentModeController().execute(actor, selected.getMap());
	}

	private void _moveNodes(final List<NodeModel> movedNodes, final int direction) {
        final Comparator<Object> comparator = (direction == -1) ? null : new Comparator<Object>() {
            public int compare(final Object o1, final Object o2) {
                final int i1 = ((Integer) o1).intValue();
                final int i2 = ((Integer) o2).intValue();
                return i2 - i1;
            }
        };
		if (movedNodes.size() == 0)
			return;
		NodeModel selected = getSelectedNode();
		Collection<NodeModel> selectedNodes = new ArrayList<NodeModel>(getSelectedNodes());
		final NodeModel parent = movedNodes.get(0).getParentNode();
        if (parent != null) {
            final Vector<NodeModel> sortedChildren = getSortedSiblings(parent);
            final TreeSet<Integer> range = new TreeSet<Integer>(comparator);
            for (final NodeModel node : movedNodes) {
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
			for (NodeModel selectedNode : selectedNodes) {
				selection.makeTheSelected(selectedNode);
            }
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
        final NodeModel destinationNode = sortedNodesIndices.get(newPositionInVector);
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

	public MapModel newModel(NodeModel existingNode) {
	    // use new MMapModel() instead of calling this method with a null arg
		if(existingNode == null)
			throw new NullPointerException("null node not allowed.");
		final MMapModel mindMapMapModel = new MMapModel();
		mindMapMapModel.setRoot(existingNode);
		mindMapMapModel.registryNodeRecursive(existingNode);
		fireMapCreated(mindMapMapModel);
		return mindMapMapModel;
    }

	@Override
	public MapModel newModel() {
		final MMapModel mindMapMapModel = new MMapModel();
		mindMapMapModel.createNewRoot();
		fireMapCreated(mindMapMapModel);
		return mindMapMapModel;
	}


	@Override
    public void setSaved(final MapModel mapModel, final boolean saved) {
		final boolean setTitle = saved != mapModel.isSaved();
		mapModel.setSaved(saved);
		if (setTitle) {
			final Controller controller = Controller.getCurrentController();
			controller.getMapViewManager().setTitle();
			final AFreeplaneAction saveAction = controller.getModeController().getAction("SaveAction");
			if(saveAction != null)
				saveAction.setEnabled();
		}
	}

	public NodeModel addFreeNode(final Point pt, final boolean newNodeIsLeft) {
		final ModeController modeController = Controller.getCurrentModeController();
		final TextController textController = TextController.getController();
		if (textController instanceof MTextController) {
			((MTextController) textController).stopEditing();
				modeController.forceNewTransaction();
		}
		final NodeModel target = getRootNode();
		final NodeModel targetNode = target;
		final boolean parentFolded = isFolded(targetNode);
		if (parentFolded) {
			setFolded(targetNode, false);
		}
		if (!isWriteable(target)) {
			UITools.errorMessage(TextUtils.getText("node_is_write_protected"));
			return null;
		}
		final NodeModel newNode = newNode("", target.getMap());
		LogicalStyleModel.createExtension(newNode).setStyle(MapStyleModel.FLOATING_STYLE);
		newNode.addExtension(modeController.getExtension(FreeNode.class));
		if(! addNewNode(newNode, target, -1, newNodeIsLeft))
			return null;
		((MLocationController)MLocationController.getController(modeController)).moveNodePosition(newNode, -1, pt.x, pt.y);
		final Component component = Controller.getCurrentController().getMapViewManager().getComponent(newNode);
		if (component == null)
			return newNode;
		component.addFocusListener(new FocusListener() {
			public void focusLost(FocusEvent e) {
			}

			public void focusGained(FocusEvent e) {
				e.getComponent().removeFocusListener(this);
				((MTextController) textController).edit(newNode, targetNode, true, false, false);
			}
		});
		select(newNode);
		return newNode;
	}

	/**@deprecated -- use MMapIO*/
	@Deprecated
	public boolean newUntitledMap(final URL url) throws FileNotFoundException, IOException, URISyntaxException, XMLException{
        try {
        	Controller.getCurrentController().getViewController().setWaitingCursor(true);
        	final MapModel newModel = new MMapModel();
        	UrlManager.getController().load(url, newModel);
        	newModel.setURL(null);
        	fireMapCreated(newModel);
        	newMapView(newModel);
        	return true;
        }
        finally {
        	Controller.getCurrentController().getViewController().setWaitingCursor(false);
        }
	}

	/**@throws XMLException
	 * @deprecated -- use MMapIO*/
	@Deprecated
	@Override
    public boolean newMap(URL url) throws FileNotFoundException, IOException, URISyntaxException, XMLException {
		// load as documentation map if necessary
		if(getMModeController().containsExtension(DocuMapAttribute.class)){
			return newDocumentationMap(url);
		}
		final IMapViewManager mapViewManager = Controller.getCurrentController().getMapViewManager();
		if (mapViewManager.tryToChangeToMapView(url))
			return false;
		if (AddOnsController.getController().installIfAppropriate(url))
			return false;
		URL alternativeURL = null;
		try {
			final File file = Compat.urlToFile(url);
			if(file == null){
				alternativeURL =  url;
			}
			else{
				if(file.exists()){
					final MFileManager fileManager = MFileManager.getController(getMModeController());
					File alternativeFile = fileManager.getAlternativeFile(file, AlternativeFileMode.AUTOSAVE);
					if(alternativeFile != null){
						if (alternativeFile.getAbsoluteFile().equals(file.getAbsoluteFile()) )
							alternativeURL =  url;
						else
							alternativeURL = Compat.fileToUrl(alternativeFile);
					}
					else
						return false;
				}
				else{
					alternativeURL = url;
				}
			}
		}
		catch (MalformedURLException e) {
		}
		catch (URISyntaxException e) {
		}

		if(alternativeURL == null)
			return false;
		Controller.getCurrentController().getViewController().setWaitingCursor(true);
		try{
			final MapModel newModel = new MMapModel();
    		final MFileManager fileManager = MFileManager.getController(getMModeController());
    		fileManager.loadAndLock(alternativeURL, newModel);
			newModel.setURL(url);
			newModel.setSaved(alternativeURL.equals(url));
			fireMapCreated(newModel);
			newMapView(newModel);
			return true;
		}
		finally {
			Controller.getCurrentController().getViewController().setWaitingCursor(false);
		}
    }

	/**@throws XMLException
	 * @deprecated -- use MMapIO*/
	@Deprecated
	public boolean newDocumentationMap(final URL url) throws FileNotFoundException, IOException, URISyntaxException, XMLException{
		final IMapViewManager mapViewManager = Controller.getCurrentController().getMapViewManager();
		if (mapViewManager.tryToChangeToMapView(url))
			return false;
        try {
        	Controller.getCurrentController().getViewController().setWaitingCursor(true);
        	final MapModel newModel = new MMapModel();
        	newModel.addExtension(DocuMapAttribute.instance);
        	UrlManager.getController().load(url, newModel);
        	newModel.setReadOnly(true);
        	fireMapCreated(newModel);
        	newMapView(newModel);
        	return true;
        }
        finally {
        	Controller.getCurrentController().getViewController().setWaitingCursor(false);
        }
	}

	/**@throws XMLException
	 * @deprecated -- use MMapIO*/
	@Deprecated
    public boolean restoreCurrentMap() throws FileNotFoundException, IOException, URISyntaxException, XMLException {
	    final Controller controller = Controller.getCurrentController();
        final MapModel map = controller.getMap();
        final URL url = map.getURL();
        if(url == null){
        	UITools.errorMessage(TextUtils.getText("map_not_saved"));
        	return false;
        }

		if(map.containsExtension(DocuMapAttribute.class)){
			controller.close(true);
			return newDocumentationMap(url);
		}

		final URL alternativeURL = MFileManager.getController(getMModeController()).getAlternativeURL(url, AlternativeFileMode.ALL);
		if(alternativeURL == null)
			return false;
		Controller.getCurrentController().getViewController().setWaitingCursor(true);
		try{
			final MapModel newModel = new MMapModel();
			((MFileManager)MFileManager.getController()).loadAndLock(alternativeURL, newModel);
			newModel.setURL(url);
			newModel.setSaved(alternativeURL.equals(url));
			fireMapCreated(newModel);
			controller.close(true);
			newMapView(newModel);
			return true;
		}
		finally {
			Controller.getCurrentController().getViewController().setWaitingCursor(false);
		}
	}


}
