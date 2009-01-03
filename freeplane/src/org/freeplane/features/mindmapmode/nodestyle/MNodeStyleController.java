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
package org.freeplane.features.mindmapmode.nodestyle;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.ListIterator;

import org.freeplane.core.map.ModeController;
import org.freeplane.core.map.NodeModel;
import org.freeplane.core.ui.MultipleNodeAction;
import org.freeplane.core.undo.IUndoableActor;
import org.freeplane.map.nodestyle.NodeStyleController;
import org.freeplane.map.nodestyle.NodeStyleModel;

/**
 * @author Dimitry Polivaev
 */
public class MNodeStyleController extends NodeStyleController {
	FontFamilyAction fontFamilyAction;
	FontSizeAction fontSizeAction;

	public MNodeStyleController(final ModeController modeController) {
		super(modeController);
		modeController.addAction("bold", new BoldAction());
		modeController.addAction("italic", new ItalicAction());
		fontSizeAction = new FontSizeAction();
		modeController.addAction("fontSize", fontSizeAction);
		final MultipleNodeAction increaseNodeFont = new MultipleNodeAction(
		    "increase_node_font_size") {
			@Override
			protected void actionPerformed(final ActionEvent e, final NodeModel node) {
				increaseFontSize(node, 1);
			}
		};
		modeController.addAction("increaseNodeFont", increaseNodeFont);
		final MultipleNodeAction decreaseNodeFont = new MultipleNodeAction(
		    "decrease_node_font_size") {
			@Override
			protected void actionPerformed(final ActionEvent e, final NodeModel node) {
				increaseFontSize(node, -1);
			}
		};
		modeController.addAction("decreaseNodeFont", decreaseNodeFont);
		fontFamilyAction = new FontFamilyAction();
		modeController.addAction("fontFamily", fontFamilyAction);
		modeController.addAction("nodeColor", new NodeColorAction());
		modeController.addAction("nodeColorBlend", new NodeColorBlendAction());
		modeController.addAction("nodeBackgroundColor", new NodeBackgroundColorAction());
		modeController
		    .addAction("removeNodeBackgroundColor", new RemoveNodeBackgroundColorAction());
		modeController.addAction("fork", new NodeShapeAction(modeController,
		    NodeStyleModel.STYLE_FORK));
		modeController.addAction("bubble", new NodeShapeAction(modeController,
		    NodeStyleModel.STYLE_BUBBLE));
		final MToolbarContributor menuContributor = new MToolbarContributor(this);
		modeController.addMenuContributor(menuContributor);
		modeController.getMapController().addNodeChangeListener(menuContributor);
		modeController.getMapController().addNodeSelectionListener(menuContributor);
	}

	public void copyStyle(final NodeModel source, final NodeModel target) {
		final NodeStyleModel sourceStyleModel = NodeStyleModel.getModel(source);
		if (sourceStyleModel != null) {
			setColor(target, sourceStyleModel.getColor());
			setBackgroundColor(target, sourceStyleModel.getBackgroundColor());
			setShape(target, sourceStyleModel.getShape());
			setFontFamily(target, sourceStyleModel.getFontFamilyName());
			setFontSize(target, sourceStyleModel.getFontSize());
			setBold(target, sourceStyleModel.isBold());
			setItalic(target, sourceStyleModel.isItalic());
		}
	}

	private NodeStyleModel createOwnFont(final NodeModel node) {
		{
			final NodeStyleModel font = NodeStyleModel.getModel(node);
			if (font != null) {
				return font;
			}
		}
		final ModeController modeController = node.getModeController();
		final IUndoableActor actor = new IUndoableActor() {
			public void act() {
				node.addExtension(new NodeStyleModel());
			}

			public String getDescription() {
				return null;
			}

			public void undo() {
				node.removeExtension(NodeStyleModel.class);
			}
		};
		modeController.execute(actor);
		return NodeStyleModel.getModel(node);
	}

	/**
	*
	*/
	public void increaseFontSize(final NodeModel node, final int increment) {
		final int newSize = getFontSize(node) + increment;
		if (newSize > 0) {
			setFontSize(node, newSize);
		}
	}

	public void setBackgroundColor(final NodeModel node, final Color color) {
		final ModeController modeController = node.getModeController();
		final Color oldColor = NodeStyleModel.getBackgroundColor(node);
		final IUndoableActor actor = new IUndoableActor() {
			public void act() {
				NodeStyleModel.setBackgroundColor(node, color);
				node.getModeController().getMapController().nodeChanged(node);
			}

			public String getDescription() {
				return "setBackgroundColor";
			}

			public void undo() {
				NodeStyleModel.setBackgroundColor(node, oldColor);
				node.getModeController().getMapController().nodeChanged(node);
			}
		};
		modeController.execute(actor);
	}

	public void setBold(final NodeModel node, final boolean bold) {
		if (bold == getFont(node).isBold()) {
			return;
		}
		toggleBold(node);
	}

	public void setColor(final NodeModel node, final Color color) {
		final ModeController modeController = node.getModeController();
		final Color oldColor = NodeStyleModel.getColor(node);
		if (oldColor == color || oldColor != null && oldColor.equals(color)) {
			return;
		}
		final IUndoableActor actor = new IUndoableActor() {
			public void act() {
				NodeStyleModel.setColor(node, color);
				node.getModeController().getMapController().nodeChanged(node);
			}

			public String getDescription() {
				return "setColor";
			}

			public void undo() {
				NodeStyleModel.setColor(node, oldColor);
				node.getModeController().getMapController().nodeChanged(node);
			}
		};
		modeController.execute(actor);
	}

	/**
	 * @param fontFamily
	 */
	public void setFontFamily(final NodeModel node, final String fontFamily) {
		final String oldFontFamily = getFont(node).getFamily();
		if (fontFamily.equals(oldFontFamily)) {
			return;
		}
		createOwnFont(node);
		final ModeController modeController = node.getModeController();
		final IUndoableActor actor = new IUndoableActor() {
			public void act() {
				final NodeStyleModel font = NodeStyleModel.getModel(node);
				font.setFontFamilyName(fontFamily);
				node.getModeController().getMapController().nodeChanged(node);
			}

			public String getDescription() {
				return "setFontSize";
			}

			public void undo() {
				final NodeStyleModel font = NodeStyleModel.getModel(node);
				font.setFontFamilyName(oldFontFamily);
				node.getModeController().getMapController().nodeChanged(node);
			}
		};
		modeController.execute(actor);
	}

	public void setFontFamily(final String fontFamily) {
		for (final ListIterator it = getModeController().getMapController().getSelectedNodes()
		    .listIterator(); it.hasNext();) {
			final NodeModel selected = (NodeModel) it.next();
			setFontFamily(selected, fontFamily);
		}
	}

	public void setFontSize(final int size) {
		for (final ListIterator it = getModeController().getMapController().getSelectedNodes()
		    .listIterator(); it.hasNext();) {
			final NodeModel selected = (NodeModel) it.next();
			setFontSize(selected, size);
		}
	}

	public void setFontSize(final NodeModel node, final int fontSize) {
		final int oldFontSize = getFont(node).getSize();
		if (fontSize == oldFontSize) {
			return;
		}
		createOwnFont(node);
		final ModeController modeController = node.getModeController();
		final IUndoableActor actor = new IUndoableActor() {
			public void act() {
				final NodeStyleModel font = NodeStyleModel.getModel(node);
				font.setFontSize(fontSize);
				node.getModeController().getMapController().nodeChanged(node);
			}

			public String getDescription() {
				return "setFontSize";
			}

			public void undo() {
				final NodeStyleModel font = NodeStyleModel.getModel(node);
				font.setFontSize(oldFontSize);
				node.getModeController().getMapController().nodeChanged(node);
			}
		};
		modeController.execute(actor);
	}

	public void setItalic(final NodeModel node, final boolean italic) {
		if (italic == getFont(node).isItalic()) {
			return;
		}
		toggleItalic(node);
	}

	public void setShape(final NodeModel node, final String shape) {
		final ModeController modeController = node.getModeController();
		final String oldShape = NodeStyleModel.getShape(node);
		final IUndoableActor actor = new IUndoableActor() {
			public void act() {
				NodeStyleModel.setShape(node, shape);
				modeController.getMapController().nodeChanged(node);
				nodeShapeRefresh(node);
			}

			public String getDescription() {
				return "setShape";
			}

			private void nodeShapeRefresh(final NodeModel node) {
				final ListIterator childrenFolded = modeController.getMapController()
				    .childrenFolded(node);
				while (childrenFolded.hasNext()) {
					final NodeModel child = (NodeModel) childrenFolded.next();
					if (NodeStyleModel.getShape(child) == null) {
						modeController.getMapController().nodeRefresh(child);
						nodeShapeRefresh(child);
					}
				}
			}

			public void undo() {
				NodeStyleModel.setShape(node, oldShape);
				modeController.getMapController().nodeChanged(node);
				nodeShapeRefresh(node);
			}
		};
		modeController.execute(actor);
	}

	public void toggleBold(final NodeModel node) {
		createOwnFont(node);
		final ModeController modeController = node.getModeController();
		final IUndoableActor actor = new IUndoableActor() {
			public void act() {
				toggleBold(node);
			}

			public String getDescription() {
				return "setBold";
			}

			private void toggleBold(final NodeModel node) {
				final Font font = getFont(node);
				final NodeStyleModel style = NodeStyleModel.getModel(node);
				style.setBold(!font.isBold());
				node.getModeController().getMapController().nodeChanged(node);
			}

			public void undo() {
				toggleBold(node);
			}
		};
		modeController.execute(actor);
	}

	public void toggleItalic(final NodeModel node) {
		createOwnFont(node);
		final ModeController modeController = node.getModeController();
		final IUndoableActor actor = new IUndoableActor() {
			public void act() {
				toggleItalic(node);
			}

			public String getDescription() {
				return "setItalic";
			}

			private void toggleItalic(final NodeModel node) {
				final Font font = getFont(node);
				final NodeStyleModel style = NodeStyleModel.getModel(node);
				style.setItalic(!font.isItalic());
				node.getModeController().getMapController().nodeChanged(node);
			}

			public void undo() {
				toggleItalic(node);
			}
		};
		modeController.execute(actor);
	}
}
