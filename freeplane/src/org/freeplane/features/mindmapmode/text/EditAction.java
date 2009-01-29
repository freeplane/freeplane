/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is modified by Dimitry Polivaev in 2008.
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
package org.freeplane.features.mindmapmode.text;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.frame.ViewController;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.FreeplaneAction;
import org.freeplane.core.ui.components.OptionalDontShowMeAgainDialog;
import org.freeplane.core.undo.IUndoableActor;
import org.freeplane.core.util.HtmlTools;
import org.freeplane.core.util.Tools;
import org.freeplane.features.common.text.TextController;
import org.freeplane.features.mindmapmode.MMapController;
import org.freeplane.features.mindmapmode.MModeController;

class EditAction extends FreeplaneAction {
	private static final Pattern HTML_HEAD = Pattern.compile("\\s*<head>.*</head>", Pattern.DOTALL);
	private EditNodeBase mCurrentEditDialog = null;

	public EditAction() {
		super("edit_node");
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * freeplane.controller.actions.ActorXml#act(freeplane.controller.actions.
	 * generated.instance.XmlAction)
	 */
	public void actionPerformed(final ActionEvent arg0) {
		getModeController().getMapController().getSelectedNode();
		edit(null, false, false);
	}

	public void edit(final KeyEvent e, final boolean addNew, final boolean editLong) {
		final NodeModel selectedNode = Controller.getController().getSelection().getSelected();
		if (selectedNode != null) {
			if (e == null || !addNew) {
				edit(selectedNode, selectedNode, e, false, false, editLong);
			}
			else if (!getModeController().isBlocked()) {
				((MMapController) getModeController().getMapController()).addNewNode(
				    MMapController.NEW_SIBLING_BEHIND, e);
			}
			if (e != null) {
				e.consume();
			}
		}
	}

	void edit(final NodeModel nodeModel, final NodeModel prevSelectedModel,
	                 final KeyEvent firstEvent, final boolean isNewNode,
	                 final boolean parentFolded, final boolean editLong) {
		if (nodeModel == null) {
			return;
		}
		final ViewController viewController = Controller.getController().getViewController();
		final Component map = viewController.getMapView();
		map.validate();
		map.invalidate();
		final Component node = viewController.getComponent(
		    nodeModel);
		node.requestFocus();
		stopEditing();
		getModeController().setBlocked(true);
		String text = nodeModel.toString();
		final String htmlEditingOption = Controller.getResourceController().getProperty(
		    "html_editing_option");
		final boolean editDefinitivelyLong = ((IMainView) node).isLong() || editLong;
		final boolean isHtmlNode = HtmlTools.isHtmlNode(text);
		String useRichTextInNewLongNodes = "true";
		if (!isHtmlNode && editDefinitivelyLong) {
			final int showResult = new OptionalDontShowMeAgainDialog(viewController.getJFrame(), nodeModel, "edit.edit_rich_text",
			    "edit.decision", new OptionalDontShowMeAgainDialog.StandardPropertyHandler(
			        ResourceController.RESOURCES_REMIND_USE_RICH_TEXT_IN_NEW_LONG_NODES),
			    OptionalDontShowMeAgainDialog.BOTH_OK_AND_CANCEL_OPTIONS_ARE_STORED).show()
			    .getResult();
			useRichTextInNewLongNodes = (showResult == JOptionPane.OK_OPTION) ? "true" : "false";
		}
		final boolean editHtml = isHtmlNode
		        || (editDefinitivelyLong && Tools.safeEquals(useRichTextInNewLongNodes, "true"));
		final boolean editInternalWysiwyg = editHtml
		        && Tools.safeEquals(htmlEditingOption, "internal-wysiwyg");
		final boolean editExternal = editHtml && Tools.safeEquals(htmlEditingOption, "external");
		if (editHtml && !isHtmlNode) {
			text = HtmlTools.plainToHTML(text);
		}
		if (editInternalWysiwyg) {
			final EditNodeWYSIWYG editNodeWYSIWYG = new EditNodeWYSIWYG(nodeModel, text,
			    firstEvent, getModeController(), new EditNodeBase.IEditControl() {
				    public void cancel() {
					    getModeController().setBlocked(false);
					    mCurrentEditDialog = null;
				    }

				    public void ok(final String newText) {
					    setHtmlText(nodeModel, newText);
					    cancel();
				    }

				    public void split(final String newText, final int position) {
					    ((MTextController) TextController.getController(Controller
					        .getModeController())).splitNode(nodeModel, position, newText);
					    viewController.obtainFocusForSelected();
					    cancel();
				    }
			    });
			mCurrentEditDialog = editNodeWYSIWYG;
			editNodeWYSIWYG.show();
			return;
		}
		if (editExternal) {
			final EditNodeExternalApplication editNodeExternalApplication = new EditNodeExternalApplication(
			    nodeModel, text, firstEvent, getModeController(), new EditNodeBase.IEditControl() {
				    public void cancel() {
					    getModeController().setBlocked(false);
					    mCurrentEditDialog = null;
				    }

				    public void ok(final String newText) {
					    setHtmlText(nodeModel, newText);
					    cancel();
				    }

				    public void split(final String newText, final int position) {
					    ((MTextController) TextController.getController(Controller
					        .getModeController())).splitNode(nodeModel, position, newText);
					    viewController.obtainFocusForSelected();
					    cancel();
				    }
			    });
			mCurrentEditDialog = editNodeExternalApplication;
			editNodeExternalApplication.show();
			return;
		}
		if (editDefinitivelyLong) {
			final EditNodeDialog nodeEditDialog = new EditNodeDialog(nodeModel, text, firstEvent,
			    getModeController(), new EditNodeBase.IEditControl() {
				    public void cancel() {
					    getModeController().setBlocked(false);
					    mCurrentEditDialog = null;
				    }

				    public void ok(final String newText) {
					    setNodeText(nodeModel, newText);
					    cancel();
				    }

				    public void split(final String newText, final int position) {
					    ((MTextController) TextController.getController(Controller
					        .getModeController())).splitNode(nodeModel, position, newText);
					    viewController.obtainFocusForSelected();
					    cancel();
				    }
			    });
			mCurrentEditDialog = nodeEditDialog;
			nodeEditDialog.show();
			return;
		}
		final INodeTextFieldCreator textFieldCreator = (INodeTextFieldCreator) Controller
		    .getController().getMapViewManager();
		final AbstractEditNodeTextField textfield = textFieldCreator.createNodeTextField(nodeModel,
		    text, firstEvent, getModeController(), new EditNodeBase.IEditControl() {
			    public void cancel() {
				    if (isNewNode) {
					    Controller.getController().getSelection().selectAsTheOnlyOneSelected(
					        nodeModel);
					    ((MModeController) getModeController()).undo();
					    getModeController().getMapController().select(prevSelectedModel);
					    if (parentFolded) {
						    getModeController().getMapController().setFolded(prevSelectedModel,
						        true);
					    }
				    }
				    endEdit();
			    }

			    private void endEdit() {
				    viewController.obtainFocusForSelected();
				    getModeController().setBlocked(false);
				    mCurrentEditDialog = null;
			    }

			    public void ok(final String newText) {
				    setNodeText(nodeModel, newText);
				    endEdit();
			    }

			    public void split(final String newText, final int position) {
			    }
		    });
		mCurrentEditDialog = textfield;
		textfield.show();
	}

	private void setHtmlText(final NodeModel node, final String newText) {
		final String body = EditAction.HTML_HEAD.matcher(newText).replaceFirst("");
		setNodeText(node, body);
	}

	public void setNodeText(final NodeModel node, final String newText) {
		final String oldText = node.toString();
		if (oldText.equals(newText)){
			return;
		}
		final IUndoableActor actor = new IUndoableActor() {
			public void act() {
				if (!oldText.equals(newText)) {
					node.setText(newText);
					getModeController().getMapController().nodeChanged(node, NodeModel.NODE_TEXT,
					    oldText, newText);
				}
			}

			public String getDescription() {
				return "editAction";
			}

			public void undo() {
				if (!oldText.equals(newText)) {
					node.setText(oldText);
					getModeController().getMapController().nodeChanged(node, NodeModel.NODE_TEXT,
					    newText, oldText);
				}
			}
		};
		getModeController().execute(actor);
	}

	public void stopEditing() {
		if (mCurrentEditDialog != null) {
			mCurrentEditDialog.closeEdit();
			mCurrentEditDialog = null;
		}
	}
}
