/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Dimitry Polivaev
 *
 *  This file author is Dimitry Polivaev
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
package org.freeplane.features.common.text;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.filter.FilterController;
import org.freeplane.core.filter.condition.ISelectableCondition;
import org.freeplane.core.io.ReadManager;
import org.freeplane.core.io.WriteManager;
import org.freeplane.core.modecontroller.MapController;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.NodeModel;

/**
 * @author Dimitry Polivaev
 */
public class TextController implements IExtension {
	public enum Direction {
		BACK, BACK_N_FOLD, FORWARD, FORWARD_N_FOLD
	}

	public static TextController getController(final ModeController modeController) {
		return (TextController) modeController.getExtension(TextController.class);
	}

	public static void install(final Controller controller) {
		FilterController.getController(controller).getConditionFactory().addConditionController(0,
		    new NodeConditionController());
	}

	public static void install(final ModeController modeController, final TextController textController) {
		modeController.addExtension(TextController.class, textController);
		modeController.getController();
	}

	final private ModeController modeController;

	public TextController(final ModeController modeController) {
		super();
		this.modeController = modeController;
		createActions(modeController);
		final MapController mapController = modeController.getMapController();
		final ReadManager readManager = mapController.getReadManager();
		final WriteManager writeManager = mapController.getWriteManager();
		final NodeTextBuilder textBuilder = new NodeTextBuilder();
		textBuilder.registerBy(readManager, writeManager);
	}

	/**
	 * @param modeController
	 */
	private void createActions(final ModeController modeController) {
		Controller controller = modeController.getController();
		if(controller.getAction(FindAction.KEY) != null){
			return;
		}
		final FindAction find = new FindAction(controller);
		controller.addAction(find);
		controller.addAction(new FindNextAction(modeController, find));
	}

	public ModeController getModeController() {
		return modeController;
	}

	NodeModel findNext(final NodeModel from, final NodeModel end, final Direction direction,
	                   final ISelectableCondition condition) {
		NodeModel next = from;
		for (;;) {
			do {
				switch (direction) {
					case FORWARD:
					case FORWARD_N_FOLD:
						next = getNext(direction, next, end);
						break;
					case BACK:
					case BACK_N_FOLD:
						next = getPrevious(direction, next, end);
						break;
				}
				if (next == null) {
					return null;
				}
			} while (!next.isVisible());
			if (next == from) {
				break;
			}
			if (condition == null || condition.checkNode(next)) {
				return next;
			}
		}
		return null;
	}

	private NodeModel getNext(final Direction direction, NodeModel current, final NodeModel end) {
		if (current.getChildCount() != 0) {
			final NodeModel next = (NodeModel) current.getChildAt(0);
			if (next.equals(end)) {
				return null;
			}
			return next;
		}
		for (;;) {
			final NodeModel parentNode = current.getParentNode();
			if (parentNode == null) {
				return current;
			}
			final int index = parentNode.getIndex(current) + 1;
			final int childCount = parentNode.getChildCount();
			if (index < childCount) {
				if (direction == Direction.FORWARD_N_FOLD) {
					getModeController().getMapController().setFolded(current, true);
				}
				final NodeModel next = (NodeModel) parentNode.getChildAt(index);
				if (next.equals(end)) {
					return null;
				}
				return next;
			}
			current = parentNode;
			if (current.equals(end)) {
				return null;
			}
		}
	}

	private NodeModel getPrevious(final Direction direction, NodeModel current, final NodeModel end) {
		for (;;) {
			final NodeModel parentNode = current.getParentNode();
			if (parentNode == null) {
				break;
			}
			if (direction == Direction.BACK_N_FOLD) {
				getModeController().getMapController().setFolded(current, true);
			}
			final int index = parentNode.getIndex(current) - 1;
			if (index < 0) {
				if (direction == Direction.BACK_N_FOLD) {
					getModeController().getMapController().setFolded(parentNode, true);
				}
				if (parentNode.equals(end)) {
					return null;
				}
				return parentNode;
			}
			current = (NodeModel) parentNode.getChildAt(index);
			if (current.equals(end)) {
				return null;
			}
			break;
		}
		for (;;) {
			if (current.getChildCount() == 0) {
				if (current.equals(end)) {
					return null;
				}
				return current;
			}
			current = (NodeModel) current.getChildAt(current.getChildCount() - 1);
		}
	}
}
