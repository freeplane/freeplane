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
import java.util.ArrayList;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.features.attribute.Attribute;
import org.freeplane.features.attribute.AttributeController;
import org.freeplane.features.attribute.NodeAttributeTableModel;
import org.freeplane.features.attribute.mindmapmode.MAttributeController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.plugin.script.ScriptEditorPanel.IScriptModel;
import org.freeplane.plugin.script.ScriptEditorPanel.ScriptHolder;

/**
 * @author foltin
 */
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
// 		final private MModeController mMindMapController;
		final private NodeModel mNode;
		/**
		 * Of AttributeHolder
		 */
		final private ArrayList<AttributeHolder> mScripts;

		private NodeScriptModel(final ArrayList<AttributeHolder> pScripts, final NodeModel node) {
			mScripts = pScripts;
			mNode = node;
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
				for (final AttributeHolder holder : mScripts) {
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
				for (final AttributeHolder holder : mScripts) {
					final Attribute attribute = holder.mAttribute;
					final int position = holder.mPosition;
					final MAttributeController attributeController = (MAttributeController) AttributeController
					    .getController();
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

		public Object executeScript(final int pIndex, final PrintStream pOutStream, final IFreeplaneScriptErrorHandler pErrorHandler) {
			final String script = getScript(pIndex).getScript();
			ModeController mMindMapController = Controller.getCurrentModeController();
			return ScriptingEngine.executeScript(mMindMapController.getMapController().getSelectedNode(), script,
			    pErrorHandler, pOutStream, null, ScriptingPermissions.getPermissiveScriptingPermissions());
		}

		public int getAmountOfScripts() {
			return mScripts.size();
		}

		public ScriptHolder getScript(final int pIndex) {
			final Attribute attribute = ((AttributeHolder) mScripts.get(pIndex)).mAttribute;
			return new ScriptHolder(attribute.getName(), attribute.getValue().toString());
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

	private static final long serialVersionUID = 1L;

	public ScriptEditor() {
		super("ScriptEditor");
	}

	public void actionPerformed(final ActionEvent e) {
		final ModeController modeController = Controller.getCurrentModeController();
		final NodeModel node = modeController.getMapController().getSelectedNode();
		final ArrayList<AttributeHolder> scripts = new ArrayList<AttributeHolder>();
		for (int position = 0; position < NodeAttributeTableModel.getModel(node).getAttributeTableLength(); position++) {
			final Attribute attribute = NodeAttributeTableModel.getModel(node).getAttribute(position);
			if (attribute.getName().startsWith(ScriptingEngine.SCRIPT_PREFIX)) {
				scripts.add(new AttributeHolder(new Attribute(attribute), position));
			}
		}
		final NodeScriptModel nodeScriptModel = new NodeScriptModel(scripts, node);
		final ScriptEditorPanel scriptEditorPanel = new ScriptEditorPanel(nodeScriptModel, true);
		scriptEditorPanel.setVisible(true);
	}
}
