package org.freeplane.features.link;

import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryAccessor;
import org.freeplane.core.ui.menubuilders.generic.EntryVisitor;
import org.freeplane.core.extension.Configurable;
import org.freeplane.features.map.IMapSelection;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;

import javax.swing.Action;
import java.util.LinkedHashSet;
import java.util.Set;

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
		Set<NodeLinkModel> links = new LinkedHashSet<NodeLinkModel>(linkController.getLinksFrom(node, mapViewComponent));
		links.addAll(linkController.getLinksTo(node, mapViewComponent));
		boolean firstAction = true;
		for (NodeLinkModel link : links) {
			final String targetID = link.getTargetID();
			final NodeModel target;
			if (node.getID().equals(targetID)) {
				if (link instanceof ConnectorModel) {
					ConnectorModel cm = (ConnectorModel) link;
					target = cm.getSource();
					if (node.equals(target))
						continue;
				}
				else
					continue;
			}
			else
				target = node.getMap().getNodeForID(targetID);
			final GotoLinkNodeAction gotoLinkNodeAction = new GotoLinkNodeAction(linkController, target);
			gotoLinkNodeAction.configureText("follow_graphical_link", target);
			if (!(link instanceof ConnectorModel)) {
				gotoLinkNodeAction.putValue(Action.SMALL_ICON, LinkController.LinkType.LOCAL.icon);
			}
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
