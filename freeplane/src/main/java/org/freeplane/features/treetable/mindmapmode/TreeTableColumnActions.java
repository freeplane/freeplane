package org.freeplane.features.treetable.mindmapmode;

import org.freeplane.features.mode.ModeController;
import org.freeplane.features.treetable.TreeTableHeader;

public class TreeTableColumnActions {

	public static void addTo(final ModeController modeController) {
		modeController.addAction(new TreeTableColumnAction("AddTreeTableColumnAction",
			TreeTableHeader::isBelowHeaderRoot, TreeTableEditing::addColumnRightOf));
		modeController.addAction(new TreeTableColumnAction("DeleteTreeTableColumnAction",
			TreeTableHeader::canRemoveColumn, TreeTableEditing::confirmAndDeleteColumn));
		modeController.addAction(new TreeTableColumnAction("MoveTreeTableColumnLeftAction",
			TreeTableHeader::canMoveColumnLeft, TreeTableEditing::moveColumnLeft));
		modeController.addAction(new TreeTableColumnAction("MoveTreeTableColumnRightAction",
			TreeTableHeader::canMoveColumnRight, TreeTableEditing::moveColumnRight));
	}

	private TreeTableColumnActions() {
	}
}
