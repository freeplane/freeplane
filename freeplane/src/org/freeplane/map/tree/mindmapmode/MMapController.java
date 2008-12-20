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
package org.freeplane.map.tree.mindmapmode;

import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import javax.swing.JOptionPane;

import org.freeplane.controller.Controller;
import org.freeplane.controller.resources.ResourceController;
import org.freeplane.io.xml.n3.nanoxml.XMLParseException;
import org.freeplane.main.Tools;
import org.freeplane.map.tree.MapController;
import org.freeplane.map.tree.MapModel;
import org.freeplane.map.tree.NodeModel;
import org.freeplane.map.url.mindmapmode.FileManager;
import org.freeplane.modes.mindmapmode.EncryptionModel;
import org.freeplane.modes.mindmapmode.MModeController;

import deprecated.freemind.common.OptionalDontShowMeAgainDialog;
import deprecated.freemind.preferences.IFreemindPropertyListener;

/**
 * @author Dimitry Polivaev
 */
public class MMapController extends MapController {
	static private DeleteAction delete;
	private static final String EXPECTED_START_STRINGS[] = {
	        "<map version=\"" + Controller.XML_VERSION + "\"", "<map version=\"0.7.1\"" };
	private static final String FREEMIND_VERSION_UPDATER_XSLT = "freemind/modes/mindmapmode/freemind_version_updater.xslt";
	public static final int NEW_CHILD = 2;
	public static final int NEW_CHILD_WITHOUT_FOCUS = 1;
	public static final int NEW_SIBLING_BEFORE = 4;
	public static final int NEW_SIBLING_BEHIND = 3;
	static private NewChildAction newChild;
	private static IFreemindPropertyListener sSaveIdPropertyChangeListener;

	public MMapController(final MModeController modeController) {
		super(modeController);
		if (sSaveIdPropertyChangeListener == null) {
			sSaveIdPropertyChangeListener = new IFreemindPropertyListener() {
				public void propertyChanged(final String propertyName, final String newValue,
				                            final String oldValue) {
					if (propertyName.equals("save_only_intrisically_needed_ids")) {
						MapController.setSaveOnlyIntrinsicallyNeededIds(Boolean.valueOf(newValue)
						    .booleanValue());
					}
				}
			};
			Controller.getResourceController().addPropertyChangeListenerAndPropagate(
			    sSaveIdPropertyChangeListener);
		}
		createActions(modeController);
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
		final MapModel map = Controller.getController().getMap();
		if (!force && !map.isSaved()) {
			final String text = getModeController().getText("save_unsaved") + "\n" + map.getTitle();
			final String title = Tools.removeMnemonic(getModeController().getText("save"));
			final int returnVal = JOptionPane.showOptionDialog(Controller.getController()
			    .getViewController().getContentPane(), text, title,
			    JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
			if (returnVal == JOptionPane.YES_OPTION) {
				final boolean savingNotCancelled = ((FileManager) getModeController()
				    .getUrlManager()).save(map);
				if (!savingNotCancelled) {
					return false;
				}
			}
			else if ((returnVal == JOptionPane.CANCEL_OPTION)
			        || (returnVal == JOptionPane.CLOSED_OPTION)) {
				return false;
			}
		}
		return super.close(force);
	}

	private void createActions(final MModeController modeController) {
		modeController.addAction("newSibling", new NewSiblingAction());
		modeController.addAction("newPreviousSibling", new NewPreviousSiblingAction());
		newChild = new NewChildAction();
		modeController.addAction("newChild", newChild);
		delete = new DeleteAction();
		modeController.addAction("deleteChild", delete);
		modeController.addAction("undoableToggleFolded", new ToggleFoldedAction());
		modeController.addAction("undoableToggleChildrenFolded", new ToggleChildrenFoldedAction());
		modeController.addAction("nodeUp", new NodeUpAction());
		modeController.addAction("nodeDown", new NodeDownAction());
	}

	public void deleteNode(final NodeModel node) {
		delete.delete(node);
	}

	/**
	 */
	public void deleteWithoutUndo(final NodeModel selectedNode) {
		final NodeModel oldParent = selectedNode.getParentNode();
		firePreNodeDelete(selectedNode);
		final MapModel map = selectedNode.getMap();
		map.setSaved(false);
		map.removeNodeFromParent(selectedNode);
		fireNodeDeleted(oldParent, selectedNode);
	}

	public MModeController getMModeController() {
		return (MModeController) getModeController();
	}

	@Override
	public void insertNodeIntoWithoutUndo(final NodeModel newNode, final NodeModel parent,
	                                      final int index) {
		parent.getMap().setSaved(false);
		super.insertNodeIntoWithoutUndo(newNode, parent, index);
	}

	public boolean isWriteable(final NodeModel targetNode) {
		final EncryptionModel encryptionModel = targetNode.getEncryptionModel();
		if (encryptionModel != null) {
			return encryptionModel.isAccessible();
		}
		return true;
	}

	@Override
	public void load(final MapModel map, final URL url) throws FileNotFoundException, IOException,
	        XMLParseException, URISyntaxException {
		final File file = Tools.urlToFile(url);
		if (!file.exists()) {
			throw new FileNotFoundException(Tools.expandPlaceholders(getModeController().getText(
			    "file_not_found"), file.getPath()));
		}
		if (!file.canWrite()) {
			((MindMapMapModel) map).setReadOnly(true);
		}
		else {
			try {
				final String lockingUser = tryToLock(map, file);
				if (lockingUser != null) {
					Controller.getController().informationMessage(
					    Tools.expandPlaceholders(getModeController().getText("map_locked_by_open"),
					        file.getName(), lockingUser));
					((MindMapMapModel) map).setReadOnly(true);
				}
				else {
					((MindMapMapModel) map).setReadOnly(false);
				}
			}
			catch (final Exception e) {
				org.freeplane.main.Tools.logException(e);
				Controller.getController().informationMessage(
				    Tools.expandPlaceholders(getModeController().getText("locking_failed_by_open"),
				        file.getName()));
				((MindMapMapModel) map).setReadOnly(true);
			}
		}
		final NodeModel root = loadTree(map, file);
		if (root != null) {
			((MindMapMapModel) map).setRoot(root);
		}
		((MindMapMapModel) map).setFile(file);
	}

	public NodeModel loadTree(final MapModel map, final File file) throws XMLParseException,
	        IOException {
		int versionInfoLength;
		versionInfoLength = EXPECTED_START_STRINGS[0].length();
		final StringBuffer buffer = readFileStart(file, versionInfoLength);
		Reader reader = null;
		for (int i = 0; i < EXPECTED_START_STRINGS.length; i++) {
			versionInfoLength = EXPECTED_START_STRINGS[i].length();
			String mapStart = "";
			if (buffer.length() >= versionInfoLength) {
				mapStart = buffer.substring(0, versionInfoLength);
			}
			if (mapStart.startsWith(EXPECTED_START_STRINGS[i])) {
				reader = Tools.getActualReader(file);
				break;
			}
		}
		if (reader == null) {
			final int showResult = new OptionalDontShowMeAgainDialog(Controller.getController()
			    .getViewController().getJFrame(), getModeController().getSelectedView(),
			    "really_convert_to_current_version", "confirmation",
			    new OptionalDontShowMeAgainDialog.StandardPropertyHandler(
			        ResourceController.RESOURCES_CONVERT_TO_CURRENT_VERSION),
			    OptionalDontShowMeAgainDialog.ONLY_OK_SELECTION_IS_STORED).show().getResult();
			if (showResult != JOptionPane.OK_OPTION) {
				reader = Tools.getActualReader(file);
			}
			else {
				reader = Tools.getUpdateReader(file, FREEMIND_VERSION_UPDATER_XSLT);
			}
		}
		try {
			return createNodeTreeFromXml(map, reader);
		}
		catch (final Exception ex) {
			final String errorMessage = "Error while parsing file:" + ex;
			System.err.println(errorMessage);
			org.freeplane.main.Tools.logException(ex);
			final NodeModel result = new NodeModel(map);
			result.setText(errorMessage);
			return result;
		}
		finally {
			if (reader != null) {
				reader.close();
			}
		}
	}

	@Override
	public void loadURL(final String relative) {
		final MapModel map = Controller.getController().getMap();
		if (map.getFile() == null) {
			Controller.getController().getViewController().out(
			    "You must save the current map first!");
			final boolean result = ((FileManager) getModeController().getUrlManager()).save(map);
			if (!result) {
				return;
			}
		}
		super.loadURL(relative);
	}

	public void moveNodes(final NodeModel selected, final List selecteds, final int direction) {
		((NodeUpAction) getModeController().getAction("nodeUp")).moveNodes(selected, selecteds,
		    direction);
	}

	/**
	 * The direction is used if side left and right are present. then the next
	 * suitable place on the same side# is searched. if there is no such place,
	 * then the side is changed.
	 *
	 * @return returns the new index.
	 */
	int moveNodeToWithoutUndo(final NodeModel child, final NodeModel newParent, final int newIndex) {
		final MapModel map = child.getMap();
		final NodeModel oldParent = child.getParentNode();
		map.removeNodeFromParent(child);
		map.insertNodeInto(child, newParent, newIndex);
		fireNodeMoved(oldParent, newParent, child, newIndex);
		return newIndex;
	}

	@Override
	public MapModel newModel(final NodeModel root) {
		final MindMapMapModel mindMapMapModel = new MindMapMapModel(root, getModeController());
		fireMapCreated(mindMapMapModel);
		return mindMapMapModel;
	}

	/**
	 * Returns pMinimumLength bytes of the files content.
	 *
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private StringBuffer readFileStart(final File file, final int pMinimumLength) {
		BufferedReader in = null;
		final StringBuffer buffer = new StringBuffer();
		try {
			in = new BufferedReader(new FileReader(file));
			String str;
			while ((str = in.readLine()) != null) {
				buffer.append(str);
				if (buffer.length() >= pMinimumLength) {
					break;
				}
			}
			in.close();
		}
		catch (final Exception e) {
			org.freeplane.main.Tools.logException(e);
			return new StringBuffer();
		}
		return buffer;
	}

	public void repaintMap() {
		getMapView().repaint();
	}

	@Override
	public void setFolded(final NodeModel node, final boolean folded) {
		((ToggleFoldedAction) getModeController().getAction("undoableToggleFolded")).setFolded(
		    node, folded);
	}

	/**
	*
	*/
	public void setToolTip(final NodeModel node, final String key, final String value) {
		node.setToolTip(key, value);
		nodeRefresh(node);
	}

	@Override
	public void toggleFolded() {
		((ToggleFoldedAction) getModeController().getAction("undoableToggleFolded")).toggleFolded();
	}

	/**
	 * Attempts to lock the map using a semaphore file
	 *
	 * @return If the map is locked, return the name of the locking user,
	 *         otherwise return null.
	 * @throws Exception
	 *             , when the locking failed for other reasons than that the
	 *             file is being edited.
	 */
	public String tryToLock(final MapModel map, final File file) throws Exception {
		final String lockingUser = ((MindMapMapModel) map).getLockManager().tryToLock(file);
		final String lockingUserOfOldLock = ((MindMapMapModel) map).getLockManager()
		    .popLockingUserOfOldLock();
		if (lockingUserOfOldLock != null) {
			Controller.getController().informationMessage(
			    Tools.expandPlaceholders(getModeController().getText("locking_old_lock_removed"),
			        file.getName(), lockingUserOfOldLock));
		}
		if (lockingUser == null) {
			((MindMapMapModel) map).setReadOnly(false);
		}
		return lockingUser;
	}
}
