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
package org.freeplane.map.nodestyle.mindmapmode;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.ListIterator;

import org.freeplane.controller.Freeplane;
import org.freeplane.map.nodestyle.NodeStyleController;
import org.freeplane.map.nodestyle.NodeStyleModel;
import org.freeplane.map.tree.NodeModel;
import org.freeplane.modes.mindmapmode.MModeController;
import org.freeplane.undo.IUndoableActor;

import deprecated.freemind.modes.mindmapmode.actions.undo.MultipleNodeAction;

/**
 * @author Dimitry Polivaev
 */
public class MNodeStyleController extends NodeStyleController {
	static private boolean actionsCreated = false;
	FontFamilyAction fontFamilyAction;
	FontSizeAction fontSizeAction;

	public MNodeStyleController(final MModeController modeController) {
		super(modeController);
		if (!actionsCreated) {
			actionsCreated = true;
			Freeplane.getController().addAction("bold",
			    new BoldAction(modeController));
			Freeplane.getController().addAction("italic",
			    new ItalicAction(modeController));
			fontSizeAction = new FontSizeAction(modeController);
			Freeplane.getController().addAction("fontSize", fontSizeAction);
			final MultipleNodeAction increaseNodeFont = new MultipleNodeAction(
			    modeController, "increase_node_font_size") {
				@Override
				protected void actionPerformed(final ActionEvent e,
				                               final NodeModel node) {
					increaseFontSize(node, 1);
				}
			};
			Freeplane.getController().addAction("increaseNodeFont",
			    increaseNodeFont);
			final MultipleNodeAction decreaseNodeFont = new MultipleNodeAction(
			    modeController, "decrease_node_font_size") {
				@Override
				protected void actionPerformed(final ActionEvent e,
				                               final NodeModel node) {
					increaseFontSize(node, -1);
				}
			};
			Freeplane.getController().addAction("decreaseNodeFont",
			    decreaseNodeFont);
			fontFamilyAction = new FontFamilyAction(modeController);
			Freeplane.getController().addAction("fontFamily", fontFamilyAction);
			Freeplane.getController().addAction("nodeColor",
			    new NodeColorAction(modeController));
			Freeplane.getController().addAction("nodeColorBlend",
			    new NodeColorBlendAction(modeController));
			Freeplane.getController().addAction("nodeBackgroundColor",
			    new NodeBackgroundColorAction(modeController));
			Freeplane.getController().addAction("removeNodeBackgroundColor",
			    new RemoveNodeBackgroundColorAction(modeController));
			Freeplane.getController().addAction("fork",
			    new NodeShapeAction(modeController, NodeStyleModel.STYLE_FORK));
			Freeplane.getController()
			    .addAction(
			        "bubble",
			        new NodeShapeAction(modeController,
			            NodeStyleModel.STYLE_BUBBLE));
		}
		final MToolbarContributor menuContributor = new MToolbarContributor(
		    this);
		modeController.addMenuContributor(menuContributor);
		modeController.addNodeChangeListener(menuContributor);
		modeController.addNodeSelectionListener(menuContributor);
	}

	private Font createOwnFont(final NodeModel node) {
		{
			final Font font = node.getFont();
			if (font != null) {
				return font;
			}
		}
		final MModeController modeController = (MModeController) node
		    .getModeController();
		final IUndoableActor actor = new IUndoableActor() {
			public void act() {
				final Font font = getFont(node);
				final Font fontCopy = new Font(font.getFamily(), font
				    .getStyle(), font.getSize());
				node.setFont(fontCopy);
			}

			public String getDescription() {
				return null;
			}

			public void undo() {
				node.setFont(null);
			}
		};
		modeController.execute(actor);
		return node.getFont();
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
		final MModeController modeController = (MModeController) node
		    .getModeController();
		final Color oldColor = node.getBackgroundColor();
		final IUndoableActor actor = new IUndoableActor() {
			public void act() {
				node.setBackgroundColor(color);
				node.getModeController().getMapController().nodeChanged(node);
			}

			public String getDescription() {
				return "setBackgroundColor";
			}

			public void undo() {
				node.setBackgroundColor(oldColor);
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
		final MModeController modeController = (MModeController) node
		    .getModeController();
		final Color oldColor = node.getColor();
		if (oldColor == color || oldColor != null && oldColor.equals(color)) {
			return;
		}
		final IUndoableActor actor = new IUndoableActor() {
			public void act() {
				node.setColor(color);
				node.getModeController().getMapController().nodeChanged(node);
			}

			public String getDescription() {
				return "setColor";
			}

			public void undo() {
				node.setColor(oldColor);
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
		final MModeController modeController = (MModeController) node
		    .getModeController();
		final IUndoableActor actor = new IUndoableActor() {
			public void act() {
				final Font font = node.getFont();
				final Font newFont = new Font(fontFamily, font.getStyle(), font
				    .getSize());
				node.setFont(newFont);
				node.getModeController().getMapController().nodeChanged(node);
			}

			public String getDescription() {
				return "setFontSize";
			}

			public void undo() {
				final Font font = node.getFont();
				final Font newFont = new Font(oldFontFamily, font.getStyle(),
				    font.getSize());
				node.setFont(newFont);
				node.getModeController().getMapController().nodeChanged(node);
			}
		};
		modeController.execute(actor);
	}

	public void setFontFamily(final String fontFamily) {
		for (final ListIterator it = getModeController().getSelectedNodes()
		    .listIterator(); it.hasNext();) {
			final NodeModel selected = (NodeModel) it.next();
			setFontFamily(selected, fontFamily);
		}
	}

	public void setFontSize(final int size) {
		for (final ListIterator it = getModeController().getSelectedNodes()
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
		final MModeController modeController = (MModeController) node
		    .getModeController();
		final IUndoableActor actor = new IUndoableActor() {
			public void act() {
				final Font font = node.getFont();
				final Font newFont = new Font(font.getFamily(),
				    font.getStyle(), fontSize);
				node.setFont(newFont);
				node.getModeController().getMapController().nodeChanged(node);
			}

			public String getDescription() {
				return "setFontSize";
			}

			public void undo() {
				final Font font = node.getFont();
				final Font newFont = new Font(font.getFamily(),
				    font.getStyle(), oldFontSize);
				node.setFont(newFont);
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
		final MModeController modeController = (MModeController) node
		    .getModeController();
		final String oldShape = node.getShape();
		final IUndoableActor actor = new IUndoableActor() {
			public void act() {
				node.setShape(shape);
				nodeStyleChanged(node);
			}

			public String getDescription() {
				return "setShape";
			}

			private void nodeStyleChanged(final NodeModel node) {
				modeController.getMapController().nodeChanged(node);
				final ListIterator childrenFolded = modeController
				    .getMapController().childrenFolded(node);
				while (childrenFolded.hasNext()) {
					final NodeModel child = (NodeModel) childrenFolded.next();
					if (!(child.getShape() != null)) {
						nodeStyleChanged(child);
					}
				}
			}

			public void undo() {
				node.setShape(oldShape);
				nodeStyleChanged(node);
			}
		};
		modeController.execute(actor);
	}

	public void toggleBold(final NodeModel node) {
		createOwnFont(node);
		final MModeController modeController = (MModeController) node
		    .getModeController();
		final IUndoableActor actor = new IUndoableActor() {
			public void act() {
				toggleBold(node);
			}

			public String getDescription() {
				return "setBold";
			}

			private void toggleBold(final NodeModel node) {
				final Font font = node.getFont();
				final Font newFont = new Font(font.getFamily(), font.getStyle()
				        ^ Font.BOLD, font.getSize());
				node.setFont(newFont);
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
		final MModeController modeController = (MModeController) node
		    .getModeController();
		final IUndoableActor actor = new IUndoableActor() {
			public void act() {
				toggleItalic(node);
			}

			public String getDescription() {
				return "setItalic";
			}

			private void toggleItalic(final NodeModel node) {
				final Font font = node.getFont();
				final Font newFont = new Font(font.getFamily(), font.getStyle()
				        ^ Font.ITALIC, font.getSize());
				node.setFont(newFont);
				node.getModeController().getMapController().nodeChanged(node);
			}

			public void undo() {
				toggleItalic(node);
			}
		};
		modeController.execute(actor);
	}
}
