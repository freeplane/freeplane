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

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.map.NodeModel;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.FreeplaneAction;
import org.freeplane.core.ui.components.OptionalDontShowMeAgainDialog;
import org.freeplane.core.undo.IUndoableActor;
import org.freeplane.core.util.HtmlTools;
import org.freeplane.core.util.Tools;
import org.freeplane.features.common.text.TextController;
import org.freeplane.features.mindmapmode.MMapController;
import org.freeplane.features.mindmapmode.MModeController;
import org.freeplane.view.swing.map.MapView;
import org.freeplane.view.swing.map.NodeView;

class EditAction extends FreeplaneAction {
	private static final Pattern HTML_HEAD = Pattern.compile("\\s*<head>.*</head>", Pattern.DOTALL);
	private EditNodeBase mCurrentEditDialog = null;

	public EditAction() {
		super("edit_node");
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * freemind.controller.actions.ActorXml#act(freemind.controller.actions.
	 * generated.instance.XmlAction)
	 */
	public void actionPerformed(final ActionEvent arg0) {
		getModeController().getMapController().getSelectedNode();
		edit(null, false, false);
	}

	public void edit(final KeyEvent e, final boolean addNew, final boolean editLong) {
		final NodeView selectedNodeView = Controller.getController().getMapView().getSelected();
		if (selectedNodeView != null) {
			if (e == null || !addNew) {
				edit(selectedNodeView, selectedNodeView, e, false, false, editLong);
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

	public void edit(final NodeView node, final NodeView prevSelected, final KeyEvent firstEvent,
	                 final boolean isNewNode, final boolean parentFolded, final boolean editLong) {
		if (node == null) {
			return;
		}
		final MapView map = node.getMap();
		map.getComponent().validate();
		map.getComponent().invalidate();
		if (!node.focused()) {
			node.requestFocus();
		}
		stopEditing();
		getModeController().setBlocked(true);
		String text = node.getModel().toString();
		final String htmlEditingOption = Controller.getResourceController().getProperty(
		    "html_editing_option");
		final boolean editDefinitivelyLong = node.getIsLong() || editLong;
		final boolean isHtmlNode = HtmlTools.isHtmlNode(text);
		String useRichTextInNewLongNodes = "true";
		if (!isHtmlNode && editDefinitivelyLong) {
			final int showResult = new OptionalDontShowMeAgainDialog(Controller.getController()
			    .getViewController().getJFrame(), getModeController().getMapController()
			    .getSelectedView(), "edit.edit_rich_text", "edit.decision",
			    new OptionalDontShowMeAgainDialog.StandardPropertyHandler(
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
			final EditNodeWYSIWYG editNodeWYSIWYG = new EditNodeWYSIWYG(node, text, firstEvent,
			    getModeController(), new EditNodeBase.IEditControl() {
				    public void cancel() {
					    getModeController().setBlocked(false);
					    mCurrentEditDialog = null;
				    }

				    public void ok(final String newText) {
					    setHtmlText(node, newText);
					    cancel();
				    }

				    public void split(final String newText, final int position) {
					    ((MTextController) TextController.getController(Controller
					        .getModeController())).splitNode(node.getModel(), position, newText);
					    Controller.getController().getViewController().obtainFocusForSelected();
					    cancel();
				    }
			    });
			mCurrentEditDialog = editNodeWYSIWYG;
			editNodeWYSIWYG.show();
			return;
		}
		if (editExternal) {
			final EditNodeExternalApplication editNodeExternalApplication = new EditNodeExternalApplication(
			    node, text, firstEvent, getModeController(), new EditNodeBase.IEditControl() {
				    public void cancel() {
					    getModeController().setBlocked(false);
					    mCurrentEditDialog = null;
				    }

				    public void ok(final String newText) {
					    setHtmlText(node, newText);
					    cancel();
				    }

				    public void split(final String newText, final int position) {
					    ((MTextController) TextController.getController(Controller
					        .getModeController())).splitNode(node.getModel(), position, newText);
					    Controller.getController().getViewController().obtainFocusForSelected();
					    cancel();
				    }
			    });
			mCurrentEditDialog = editNodeExternalApplication;
			editNodeExternalApplication.show();
			return;
		}
		if (editDefinitivelyLong) {
			final EditNodeDialog nodeEditDialog = new EditNodeDialog(node, text, firstEvent,
			    getModeController(), new EditNodeBase.IEditControl() {
				    public void cancel() {
					    getModeController().setBlocked(false);
					    mCurrentEditDialog = null;
				    }

				    public void ok(final String newText) {
					    setNodeText(node.getModel(), newText);
					    cancel();
				    }

				    public void split(final String newText, final int position) {
					    ((MTextController) TextController.getController(Controller
					        .getModeController())).splitNode(node.getModel(), position, newText);
					    Controller.getController().getViewController().obtainFocusForSelected();
					    cancel();
				    }
			    });
			mCurrentEditDialog = nodeEditDialog;
			nodeEditDialog.show();
			return;
		}
		final EditNodeTextField textfield = new EditNodeTextField(node, text, firstEvent,
		    getModeController(), new EditNodeBase.IEditControl() {
			    public void cancel() {
				    if (isNewNode) {
					    Controller.getController().getMapView().selectAsTheOnlyOneSelected(node);
					    ((MModeController) getModeController()).undo();
					    getModeController().getMapController().select(prevSelected);
					    if (parentFolded) {
						    getModeController().getMapController().setFolded(
						        prevSelected.getModel(), true);
					    }
				    }
				    endEdit();
			    }

			    private void endEdit() {
				    Controller.getController().getViewController().obtainFocusForSelected();
				    getModeController().setBlocked(false);
				    mCurrentEditDialog = null;
			    }

			    public void ok(final String newText) {
				    setNodeText(node.getModel(), newText);
				    endEdit();
			    }

			    public void split(final String newText, final int position) {
			    }
		    });
		mCurrentEditDialog = textfield;
		textfield.show();
	}

	private void setHtmlText(final NodeView node, final String newText) {
		final String body = EditAction.HTML_HEAD.matcher(newText).replaceFirst("");
		setNodeText(node.getModel(), body);
	}

	public void setNodeText(final NodeModel node, final String newText) {
		final String oldText = node.toString();
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
