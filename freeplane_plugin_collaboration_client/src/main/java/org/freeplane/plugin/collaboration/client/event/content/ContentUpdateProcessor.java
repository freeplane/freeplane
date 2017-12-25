package org.freeplane.plugin.collaboration.client.event.content;

import java.util.Arrays;
import java.util.Collection;

import org.freeplane.core.extension.IExtension;
import org.freeplane.features.icon.AccumulatedIcons;
import org.freeplane.features.map.FirstGroupNodeFlag;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.SummaryNodeFlag;
import org.freeplane.features.map.mindmapmode.NodeContentManipulator;
import org.freeplane.features.styles.MapStyleModel;
import org.freeplane.plugin.collaboration.client.event.UpdateProcessor;

public class ContentUpdateProcessor implements UpdateProcessor<ContentUpdated> {
	private static final Collection<Class<? extends IExtension>> exclusions = 
			Arrays.asList(AccumulatedIcons.class, SummaryNodeFlag.class, FirstGroupNodeFlag.class,
					MapStyleModel.class);

	private final NodeContentManipulator updater;

	public ContentUpdateProcessor(NodeContentManipulator updater) {
		super();
		this.updater = updater;
	}

	@Override
	public void onUpdate(MapModel map, ContentUpdated event) {
		updater.updateContent(map.getNodeForID(event.nodeId()), event.content(), exclusions);
	}

	@Override
	public Class<ContentUpdated> eventClass() {
		return ContentUpdated.class;
	}
}
