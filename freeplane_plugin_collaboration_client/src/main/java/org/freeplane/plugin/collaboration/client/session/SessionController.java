package org.freeplane.plugin.collaboration.client.session;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.freeplane.collaboration.event.batch.Credentials;
import org.freeplane.collaboration.event.batch.ImmutableMapCreateRequest;
import org.freeplane.collaboration.event.batch.MapDescription;
import org.freeplane.collaboration.event.batch.MapId;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.link.mindmapmode.MLinkController;
import org.freeplane.features.map.IMapLifeCycleListener;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.MapWriter;
import org.freeplane.features.map.mindmapmode.MMapController;
import org.freeplane.features.map.mindmapmode.MMapModel;
import org.freeplane.features.map.mindmapmode.NodeContentManipulator;
import org.freeplane.features.map.mindmapmode.SingleNodeStructureManipulator;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.features.text.TextController;
import org.freeplane.features.text.mindmapmode.MTextController;
import org.freeplane.plugin.collaboration.client.VisibleForTesting;
import org.freeplane.plugin.collaboration.client.event.UpdateEventGenerator;
import org.freeplane.plugin.collaboration.client.event.UpdateProcessorChain;
import org.freeplane.plugin.collaboration.client.event.batch.UpdatesAccessor;
import org.freeplane.plugin.collaboration.client.event.children.MapStructureEventGenerator;
import org.freeplane.plugin.collaboration.client.event.children.NodeFactory;
import org.freeplane.plugin.collaboration.client.event.children.NodeInsertedProcessor;
import org.freeplane.plugin.collaboration.client.event.children.NodeMovedProcessor;
import org.freeplane.plugin.collaboration.client.event.children.NodeRemovedProcessor;
import org.freeplane.plugin.collaboration.client.event.children.RootNodeIdUpdatedProcessor;
import org.freeplane.plugin.collaboration.client.event.children.SpecialNodeTypeProcessor;
import org.freeplane.plugin.collaboration.client.event.content.ContentUpdateGenerators;
import org.freeplane.plugin.collaboration.client.event.content.core.CoreUpdateGenerator;
import org.freeplane.plugin.collaboration.client.event.content.core.CoreUpdateProcessor;
import org.freeplane.plugin.collaboration.client.event.content.links.ConnectorAdditionProcessor;
import org.freeplane.plugin.collaboration.client.event.content.links.ConnectorRemovalProcessor;
import org.freeplane.plugin.collaboration.client.event.content.links.ConnectorUpdateProcessor;
import org.freeplane.plugin.collaboration.client.event.content.links.HyperlinkUpdateProcessor;
import org.freeplane.plugin.collaboration.client.event.content.links.LinkUpdateGenerator;
import org.freeplane.plugin.collaboration.client.event.content.other.ContentUpdateGenerator;
import org.freeplane.plugin.collaboration.client.event.content.other.MapContentUpdateProcessor;
import org.freeplane.plugin.collaboration.client.event.content.other.NodeContentUpdateProcessor;
import org.freeplane.plugin.collaboration.client.server.Server;

public class SessionController {
//	One client
//	Connect to server (web socket session)
//	Create a new map on server and get its uuid
//	Send updates to server and receive them back
//	Subscribes to updates for a given map id
//	Receives all updates from server for a given map id
//	From the beginning
//	From given map revision
//	Optimize: server skips overwritten updates of the same element (later)

	private final UpdateProcessorChain updateProcessor;
	private final Map<MapId, Session> sessions;
	private final UpdateEventGenerator updateEventGenerator;

	@VisibleForTesting
	SessionController(UpdateEventGenerator updateEventGenerator, UpdateProcessorChain processor,
	                Map<MapId, Session> sessions) {
		super();
		this.updateEventGenerator = updateEventGenerator;
		this.updateProcessor = processor;
		this.sessions = sessions;
	}

	public SessionController() {
		this(createUpdateEventGenerator(), createUpdateProcessor(), new HashMap<>());
		final ModeController modeController = Controller.getCurrentModeController();
		final MapController mapController = modeController.getMapController();
		mapController.addMapLifeCycleListener(new IMapLifeCycleListener() {
			@Override
			public void onRemove(MapModel map) {
				Session session = map.getExtension(Session.class);
				if(session != null) {
					session.terminate();
					sessions.remove(session.getMapId());
				}
			}

			@Override
			public void onCreate(MapModel map) {
			}
		});
	}

	static private UpdateEventGenerator createUpdateEventGenerator() {
		UpdatesAccessor updatеsAccessor = new UpdatesAccessor();
		final ModeController modeController = Controller.getCurrentModeController();
		final MapController mapController = modeController.getMapController();
		final MapWriter mapWriter = mapController.getMapWriter();
		ContentUpdateGenerator contentUpdateGenerator = new ContentUpdateGenerator(updatеsAccessor, mapWriter);
		CoreUpdateGenerator coreUpdateGenerator = new CoreUpdateGenerator(updatеsAccessor, mapWriter);
		LinkUpdateGenerator linkUpdateGenerator = new LinkUpdateGenerator(updatеsAccessor,
		    modeController.getExtension(LinkController.class));
		ContentUpdateGenerators contentGenerators = new ContentUpdateGenerators(
		    Arrays.asList(contentUpdateGenerator),
		    Arrays.asList(coreUpdateGenerator, contentUpdateGenerator, linkUpdateGenerator));
		MapStructureEventGenerator mapStructureEventGenerator = new MapStructureEventGenerator(updatеsAccessor,
		    contentGenerators);
		UpdateEventGenerator updateEventGenerator = new UpdateEventGenerator(mapStructureEventGenerator,
		    contentGenerators);
		mapController.addMapChangeListener(updateEventGenerator);
		mapController.addNodeChangeListener(updateEventGenerator);
		return updateEventGenerator;
	}

	static private UpdateProcessorChain createUpdateProcessor() {
		final NodeFactory nodeFactory = new NodeFactory();
		final Controller controller = Controller.getCurrentController();
		final ModeController modeController = controller
		    .getModeController(MModeController.MODENAME);
		MMapController mapController = (MMapController) modeController.getMapController();
		final SingleNodeStructureManipulator singleNodeStructureManipulator = new SingleNodeStructureManipulator(
		    mapController);
		final RootNodeIdUpdatedProcessor rootNodeIdUpdatedProcessor = new RootNodeIdUpdatedProcessor();
		final SpecialNodeTypeProcessor specialNodeTypeProcessor = new SpecialNodeTypeProcessor();
		NodeContentManipulator updater = new NodeContentManipulator(mapController);
		final MLinkController linkController = (MLinkController) MLinkController.getController(modeController);
		UpdateProcessorChain processor = new UpdateProcessorChain(controller).add(rootNodeIdUpdatedProcessor).add(specialNodeTypeProcessor)
		    .add(new NodeInsertedProcessor(singleNodeStructureManipulator, nodeFactory))
		    .add(new NodeMovedProcessor(singleNodeStructureManipulator))
		    .add(new NodeRemovedProcessor(singleNodeStructureManipulator))
		    .add(new MapContentUpdateProcessor(updater)).add(new NodeContentUpdateProcessor(updater))
		    .add(new CoreUpdateProcessor((MTextController) TextController.getController(modeController)))
		    .add(new ConnectorAdditionProcessor(linkController))
		    .add(new ConnectorUpdateProcessor(linkController))
		    .add(new ConnectorRemovalProcessor(linkController))
		    .add(new HyperlinkUpdateProcessor(linkController));
		return processor;
	}


	public void startSession(Server server, Credentials credentials, MMapModel map, MapDescription mapDescription) {
		final MapId mapId = server.createNewMap(ImmutableMapCreateRequest.of(credentials, mapDescription));
		joinSession(server, credentials, map, mapId);
		updateEventGenerator.onNewMap(map);
	}

	public void joinSession(Server server, Credentials credentials, MMapModel map, MapId mapId) {
		Session session = new Session(server, credentials, updateProcessor, mapId, map);
		sessions.put(mapId, session);
	}

	public void stopSession(Session session) {
		session.terminate();
		sessions.remove(session.getMapId());
	}

}
