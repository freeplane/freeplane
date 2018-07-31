package org.freeplane.features.explorer.mindmapmode;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.io.IAttributeHandler;
import org.freeplane.core.io.IAttributeWriter;
import org.freeplane.core.io.ITreeWriter;
import org.freeplane.core.io.ReadManager;
import org.freeplane.core.io.WriteManager;
import org.freeplane.core.undo.IActor;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.MapReader;
import org.freeplane.features.map.NodeBuilder;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.text.TextController;

public class MapExplorerController implements IExtension{


	private static final String TRUE = "true";
	private static final String GLOBALLY_VISIBLE = "GLOBALLY_VISIBLE";
	private static final String ALIAS = "ALIAS";
	private final TextController textController;
	private final ModeController modeController;

	public static void install(ModeController modeController, TextController textController) {
		modeController.addExtension(MapExplorerController.class, new MapExplorerController(modeController, textController));
		final MapController mapController = modeController.getMapController();
		final ReadManager readManager = mapController.getReadManager();
		final WriteManager writeManager = mapController.getWriteManager();
		final MapReader mapReader = mapController.getMapReader();
		readManager.addAttributeHandler(NodeBuilder.XML_NODE, ALIAS, new IAttributeHandler() {
			@Override
			public void setAttribute(final Object node, final String value) {
				((NodeModel) node).addExtension(new NodeAlias(value));
			}
		});
		readManager.addAttributeHandler(NodeBuilder.XML_NODE, GLOBALLY_VISIBLE, new IAttributeHandler() {
			@Override
			public void setAttribute(final Object node, final String value) {
				if(Boolean.parseBoolean(value)) {
					final MapModel map = mapReader.getCurrentNodeTreeCreator().getCreatedMap();
					GlobalNodes nodes = GlobalNodes.writeableOf(map);
					nodes.makeGlobal((NodeModel)node);
				}
			}
		});

		writeManager.addAttributeWriter(NodeBuilder.XML_NODE, new IAttributeWriter() {
			@Override
			public void writeAttributes(ITreeWriter writer, Object userObject, String tag) {
				NodeModel node = (NodeModel) userObject;
				if(GlobalNodes.isGlobal(node))
					writer.addAttribute(GLOBALLY_VISIBLE, TRUE);
				final NodeAlias alias = node.getExtension(NodeAlias.class);
				if(alias != null)
					writer.addAttribute(ALIAS, alias.value);
			}
		});
	}

	private MapExplorerController(ModeController modeController, TextController textController) {
		super();
		this.modeController = modeController;
		this.textController = textController;
	}

	public void makeGlobal(final NodeModel node, final boolean isGlobal) {
		if(isGlobal == isGlobal(node))
			return;
		final IActor actor = new IActor() {
			@Override
			public void act() {
				GlobalNodes.writeableOf(node.getMap()).makeGlobal(node, isGlobal);
				modeController.getMapController().nodeChanged(node);
			}

			@Override
			public void undo() {
				GlobalNodes.writeableOf(node.getMap()).makeGlobal(node, !isGlobal);
				modeController.getMapController().nodeChanged(node);
			}

			@Override
			public String getDescription() {
				return "setGlobal";
			}

		};
		modeController.execute(actor, node.getMap());
	}

	public String getAlias(final NodeModel node) {
		return NodeAlias.getAlias(node);
	}

	public void setAlias(final NodeModel node, final String alias) {
		final String oldAlias = NodeAlias.getAlias(node);
		if(oldAlias == alias || oldAlias != null && oldAlias.equals(alias))
			return;
		final IActor actor = new IActor() {
			@Override
			public void act() {
				NodeAlias.setAlias(node, alias);
				modeController.getMapController().nodeChanged(node);
			}

			@Override
			public void undo() {
				NodeAlias.setAlias(node, oldAlias);
				modeController.getMapController().nodeChanged(node);
			}

			@Override
			public String getDescription() {
				return "setAlias";
			}

		};
		modeController.execute(actor, node.getMap());
	}

	public boolean isGlobal(final NodeModel node) {
		return GlobalNodes.isGlobal(node);
	}

	public MapExplorer getMapExplorer(NodeModel start, String path, AccessedNodes accessedNodes) {
		return new MapExplorer(textController, start, path, accessedNodes);
	}

	public String getNodeReferenceSuggestion(NodeModel node) {
		final String alias = getAlias(node);
		if(alias != null)
			return alias;
		final String shortPlainText = textController.getShortPlainText(node, 10, "...");
		return shortPlainText;
	}

}
