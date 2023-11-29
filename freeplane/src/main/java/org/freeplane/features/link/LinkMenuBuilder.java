package org.freeplane.features.link;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.Action;

import org.freeplane.core.extension.Configurable;
import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryAccessor;
import org.freeplane.core.ui.menubuilders.generic.EntryVisitor;
import org.freeplane.features.map.IMapSelection;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;

class LinkMenuBuilder implements EntryVisitor {
	private final ModeController modeController;
	private final LinkController linkController;

	LinkMenuBuilder(ModeController modeController, LinkController linkController) {
		this.modeController = modeController;
		this.linkController = linkController;
	}

	@Override
	public void visit(Entry entry) {
		Controller controller = modeController.getController();
		final IMapSelection selection = controller.getSelection();
		if (selection == null)
			return;
		final NodeModel node = selection.getSelected();
		Configurable mapViewComponent = controller.getMapViewManager().getMapViewConfiguration();
		Set<String> links = linkController.getLinksFrom(node, mapViewComponent).stream().map(NodeLinkModel::getTargetID).collect(Collectors.toCollection(LinkedHashSet::new));
		linkController.getLinksTo(node, mapViewComponent).stream().map(NodeLinkModel::getSource).map(NodeModel::createID).forEach(links::add);
		links.remove(node.getID());
		boolean firstAction = true;
		for (String targetID : links) {
		    NodeModel target = node.getMap().getNodeForID(targetID);
		    if(target ==  null)
		        continue;
			final GotoLinkNodeAction gotoLinkNodeAction = new GotoLinkNodeAction(linkController, target);
			gotoLinkNodeAction.configureText("follow_graphical_link", target);

			if (firstAction) {
				entry.addChild(new Entry().setBuilders("separator"));
				firstAction = false;
			}
			modeController.addActionIfNotAlreadySet(gotoLinkNodeAction);
			new EntryAccessor().addChildAction(entry, gotoLinkNodeAction);
		}
	}

	@Override
	public boolean shouldSkipChildren(Entry entry) {
		return true;
	}
}
