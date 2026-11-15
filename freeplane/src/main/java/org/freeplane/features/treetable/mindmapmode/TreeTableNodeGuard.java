package org.freeplane.features.treetable.mindmapmode;

import java.util.Collection;

import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.mindmapmode.INodeOperationGuard;
import org.freeplane.features.treetable.TreeTableHeader;

class TreeTableNodeGuard implements INodeOperationGuard {

	@Override
	public String refusalOfDeletion(final Collection<NodeModel> nodes) {
		final String key = TreeTableHeader.collisionAfterDeleting(nodes);
		return key == null ? null : TextUtils.format("treetable.cannot_delete_column", TreeTableHeader.nameOfKey(key));
	}

	@Override
	public String refusalOfTextChange(final NodeModel node, final String newText) {
		final String key = TreeTableHeader.collisionAfterRenaming(node, newText);
		return key == null ? null : TextUtils.format("treetable.cannot_rename_column", TreeTableHeader.nameOfKey(key));
	}
}
