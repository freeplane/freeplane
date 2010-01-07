/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file author is Christian Foltin
 *  It is modified by Dimitry Polivaev in 2008.
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
package org.freeplane.plugin.script;

import java.awt.event.ActionEvent;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.Vector;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.ActionLocationDescriptor;
import org.freeplane.features.common.attribute.Attribute;
import org.freeplane.features.common.attribute.AttributeController;
import org.freeplane.features.common.attribute.NodeAttributeTableModel;
import org.freeplane.features.mindmapmode.MModeController;
import org.freeplane.features.mindmapmode.attribute.MAttributeController;
import org.freeplane.plugin.script.ScriptEditorPanel.IScriptModel;
import org.freeplane.plugin.script.ScriptEditorPanel.ScriptHolder;
import org.freeplane.plugin.script.ScriptingEngine.IErrorHandler;

/**
 * @author foltin
 */
@ActionLocationDescriptor(locations = { "/menu_bar/extras/first/scripting" })
class ScriptEditor extends AFreeplaneAction {
	final private class AttributeHolder {
		Attribute mAttribute;
		int mPosition;

		public AttributeHolder(final Attribute pAttribute, final int pPosition) {
			super();
			mAttribute = pAttribute;
			mPosition = pPosition;
		}
	}

	final private class NodeScriptModel implements IScriptModel {
		private boolean isDirty = false;
		final private MModeController mMindMapController;
		final private NodeModel mNode;
		/**
		 * Of AttributeHolder
		 */
		final private Vector mScripts;

		private NodeScriptModel(final Vector pScripts, final NodeModel node, final MModeController pMindMapController) {
			mScripts = pScripts;
			mNode = node;
			mMindMapController = pMindMapController;
		}

		public int addNewScript() {
			final int index = mScripts.size();
			/**
			 * is in general different from index, as not all attributes need to
			 * be scripts.
			 */
			final int attributeIndex = NodeAttributeTableModel.getModel(mNode).getAttributeTableLength();
			final String scriptName = ScriptingEngine.SCRIPT_PREFIX;
			int scriptNameSuffix = 1;
			boolean found;
			do {
				found = false;
				for (final Iterator iterator = mScripts.iterator(); iterator.hasNext();) {
					final AttributeHolder holder = (AttributeHolder) iterator.next();
					if ((scriptName + scriptNameSuffix).equals(holder.mAttribute.getName())) {
						found = true;
						scriptNameSuffix++;
						break;
					}
				}
			} while (found);
			mScripts.add(new AttributeHolder(new Attribute(scriptName + scriptNameSuffix, ""), attributeIndex));
			isDirty = true;
			return index;
		}

		public ScriptEditorWindowConfigurationStorage decorateDialog(final ScriptEditorPanel pPanel,
		                                                             final String pWindow_preference_storage_property) {
			final String marshalled = ResourceController.getResourceController().getProperty(
			    pWindow_preference_storage_property);
			return ScriptEditorWindowConfigurationStorage.decorateDialog(marshalled, pPanel);
		}

		public void endDialog(final boolean pIsCanceled) {
			if (!pIsCanceled) {
				final int attributeTableLength = NodeAttributeTableModel.getModel(mNode).getAttributeTableLength();
				for (final Iterator iter = mScripts.iterator(); iter.hasNext();) {
					final AttributeHolder holder = (AttributeHolder) iter.next();
					final Attribute attribute = holder.mAttribute;
					final int position = holder.mPosition;
					final MAttributeController attributeController = (MAttributeController) AttributeController
					    .getController(mMindMapController);
					if (attributeTableLength <= position) {
						attributeController.addAttribute(mNode, attribute);
					}
					else if (NodeAttributeTableModel.getModel(mNode).getAttribute(position).getValue() != attribute
					    .getValue()) {
						attributeController.setAttribute(mNode, position, attribute);
					}
				}
			}
		}

		public boolean executeScript(final int pIndex, final PrintStream pOutStream, final IErrorHandler pErrorHandler) {
			final String script = getScript(pIndex).getScript();
			ScriptingEngine.setNoUserPermissionRequired(true);
			return ScriptingEngine.executeScript(mMindMapController.getMapController().getSelectedNode(),
			    script, mMindMapController, pErrorHandler, pOutStream, reg.getScriptCookies());
		}

		public int getAmountOfScripts() {
			return mScripts.size();
		}

		public ScriptHolder getScript(final int pIndex) {
			final Attribute attribute = ((AttributeHolder) mScripts.get(pIndex)).mAttribute;
			return new ScriptHolder(attribute.getName(), attribute.getValue());
		}

		public boolean isDirty() {
			return isDirty;
		}

		public void setScript(final int pIndex, final ScriptHolder pScript) {
			final AttributeHolder oldHolder = (AttributeHolder) mScripts.get(pIndex);
			if (!pScript.mScriptName.equals(oldHolder.mAttribute.getName())) {
				isDirty = true;
			}
			if (!pScript.mScript.equals(oldHolder.mAttribute.getValue())) {
				isDirty = true;
			}
			oldHolder.mAttribute.setName(pScript.mScriptName);
			oldHolder.mAttribute.setValue(pScript.mScript);
		}

		public void storeDialogPositions(final ScriptEditorPanel pPanel,
		                                 final ScriptEditorWindowConfigurationStorage pStorage,
		                                 final String pWindow_preference_storage_property) {
			pStorage.storeDialogPositions(pPanel, pWindow_preference_storage_property);
		}
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	final private ScriptingRegistration reg;

	public ScriptEditor(final Controller controller, final ScriptingRegistration reg) {
		super("ScriptEditor", controller);
		this.reg = reg;
	}

	public void actionPerformed(final ActionEvent e) {
		final ModeController modeController = getModeController();
		final NodeModel node = modeController.getMapController().getSelectedNode();
		final Vector scripts = new Vector();
		for (int position = 0; position < NodeAttributeTableModel.getModel(node).getAttributeTableLength(); position++) {
			final Attribute attribute = NodeAttributeTableModel.getModel(node).getAttribute(position);
			if (attribute.getName().startsWith(ScriptingEngine.SCRIPT_PREFIX)) {
				scripts.add(new AttributeHolder(attribute, position));
			}
		}
		final NodeScriptModel nodeScriptModel = new NodeScriptModel(scripts, node, (MModeController) modeController);
		final ScriptEditorPanel scriptEditorPanel = new ScriptEditorPanel(modeController.getController(),
		    nodeScriptModel, true);
		scriptEditorPanel.setVisible(true);
	}
}
