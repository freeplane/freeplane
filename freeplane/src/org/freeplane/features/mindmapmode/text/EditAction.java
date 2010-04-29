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

import org.apache.commons.lang.StringUtils;
import org.freeplane.core.controller.Controller;
import org.freeplane.core.frame.ViewController;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.util.HtmlTools;
import org.freeplane.features.common.text.TextController;
import org.freeplane.features.mindmapmode.MMapController;
import org.freeplane.features.mindmapmode.MModeController;

class EditAction extends AFreeplaneAction {
	private static final Pattern HTML_HEAD = Pattern.compile("\\s*<head>.*</head>", Pattern.DOTALL);
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private EditNodeBase mCurrentEditDialog = null;

	public EditAction(final Controller controller) {
		super("EditAction", controller);
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
		final Controller controller = getController();
		final NodeModel selectedNode = controller.getSelection().getSelected();
		if (selectedNode != null) {
			if (e == null || !addNew) {
				edit(selectedNode, selectedNode, e, false, false, editLong);
			}
			else if (!getModeController().isBlocked()) {
				((MMapController) getModeController().getMapController()).addNewNode(MMapController.NEW_SIBLING_BEHIND,
				    e);
			}
			if (e != null) {
				e.consume();
			}
		}
	}

	void edit(final NodeModel nodeModel, final NodeModel prevSelectedModel, final KeyEvent firstEvent,
	          final boolean isNewNode, final boolean parentFolded, final boolean editLong) {
		if (nodeModel == null || mCurrentEditDialog != null) {
			return;
		}
		final Controller controller = getController();
		if (controller.getMap() != nodeModel.getMap()) {
			return;
		}
		final ViewController viewController = controller.getViewController();
		final Component map = viewController.getMapView();
		map.validate();
		map.invalidate();
		final Component node = viewController.getComponent(nodeModel);
		if (node == null) {
			return;
		}
		node.requestFocus();
		stopEditing();
		getModeController().setBlocked(true);
		String text = nodeModel.toString();
		final String htmlEditingOption = ResourceController.getResourceController().getProperty("html_editing_option");
		final boolean isHtmlNode = HtmlTools.isHtmlNode(text);
		final boolean editDefinitivelyLong = editLong;
		boolean useRichTextInNewLongNodes = true;
		if (!isHtmlNode && editDefinitivelyLong) {
			final MTextController textController = (MTextController) TextController.getController(getModeController());
			useRichTextInNewLongNodes = textController.useRichTextInNewLongNodes();
		}
		final boolean editHtml = isHtmlNode || (editDefinitivelyLong && useRichTextInNewLongNodes);
		final boolean editInternalWysiwyg = editHtml && StringUtils.equals(htmlEditingOption, "internal-wysiwyg");
		final boolean editExternal = editHtml && StringUtils.equals(htmlEditingOption, "external");
		if (editHtml && !isHtmlNode) {
			text = HtmlTools.plainToHTML(text);
		}
		if (editInternalWysiwyg) {
			final EditNodeWYSIWYG editNodeWYSIWYG = new EditNodeWYSIWYG(nodeModel, text, firstEvent,
			    getModeController(), new EditNodeBase.IEditControl() {
				    public void cancel() {
					    getModeController().setBlocked(false);
					    mCurrentEditDialog = null;
				    }

				    public void ok(final String newText) {
					    setHtmlText(nodeModel, newText);
					    cancel();
				    }

				    public void split(final String newText, final int position) {
					    ((MTextController) TextController.getController(controller.getModeController())).splitNode(
					        nodeModel, position, newText);
					    viewController.obtainFocusForSelected();
					    cancel();
				    }
			    });
			mCurrentEditDialog = editNodeWYSIWYG;
			editNodeWYSIWYG.show(controller.getViewController().getFrame());
			return;
		}
		if (editExternal) {
			final EditNodeExternalApplication editNodeExternalApplication = new EditNodeExternalApplication(nodeModel,
			    text, firstEvent, getModeController(), new EditNodeBase.IEditControl() {
				    public void cancel() {
					    getModeController().setBlocked(false);
					    mCurrentEditDialog = null;
				    }

				    public void ok(final String newText) {
					    setHtmlText(nodeModel, newText);
					    cancel();
				    }

				    public void split(final String newText, final int position) {
					    ((MTextController) TextController.getController(controller.getModeController())).splitNode(
					        nodeModel, position, newText);
					    viewController.obtainFocusForSelected();
					    cancel();
				    }
			    });
			mCurrentEditDialog = editNodeExternalApplication;
			editNodeExternalApplication.show();
			return;
		}
		if (editDefinitivelyLong) {
			final EditNodeDialog nodeEditDialog = new EditNodeDialog(nodeModel, text, firstEvent, getModeController(),
			    new EditNodeBase.IEditControl() {
				    public void cancel() {
					    getModeController().setBlocked(false);
					    mCurrentEditDialog = null;
				    }

				    public void ok(final String newText) {
					    setNodeText(nodeModel, newText);
					    cancel();
				    }

				    public void split(final String newText, final int position) {
					    ((MTextController) TextController.getController(controller.getModeController())).splitNode(
					        nodeModel, position, newText);
					    viewController.obtainFocusForSelected();
					    cancel();
				    }
			    });
			mCurrentEditDialog = nodeEditDialog;
			nodeEditDialog.show(JOptionPane.getFrameForComponent(map));
			return;
		}
		final INodeTextFieldCreator textFieldCreator = (INodeTextFieldCreator) controller.getMapViewManager();
		final AbstractEditNodeTextField textfield = textFieldCreator.createNodeTextField(nodeModel, text, firstEvent,
		    getModeController(), new EditNodeBase.IEditControl() {
			    public void cancel() {
				    if (isNewNode) {
					    controller.getSelection().selectAsTheOnlyOneSelected(nodeModel);
					    ((MModeController) getModeController()).undo();
					    getModeController().getMapController().select(prevSelectedModel);
					    if (parentFolded) {
						    getModeController().getMapController().setFolded(prevSelectedModel, true);
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
				    if (nodeModel.getMap().equals(getController().getMap())) {
					    setNodeText(nodeModel, newText);
				    }
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
		final ModeController modeController = getController().getModeController(MModeController.MODENAME);
		final MTextController textController = (MTextController) TextController.getController(modeController);
		textController.setNodeText(node, newText.replaceFirst("\\s+$", ""));
	}

	public void stopEditing() {
		if (mCurrentEditDialog != null) {
			mCurrentEditDialog.closeEdit();
			mCurrentEditDialog = null;
		}
	}
}
