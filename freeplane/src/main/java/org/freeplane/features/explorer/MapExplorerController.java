package org.freeplane.features.explorer;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.io.IAttributeHandler;
import org.freeplane.core.io.IAttributeWriter;
import org.freeplane.core.io.ITreeWriter;
import org.freeplane.core.io.ReadManager;
import org.freeplane.core.io.WriteManager;
import org.freeplane.core.util.HtmlUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.MapReader;
import org.freeplane.features.map.NodeBuilder;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.ModeController;

public class MapExplorerController  implements IExtension{
	private static final String TRUE = "true";
	private static final String GLOBALLY_VISIBLE = "GLOBALLY_VISIBLE";
	private static final String ALIAS = "ALIAS";
	public static final String GLOBAL_NODES = "GLOBAL_NODES";

	public static void install(ModeController modeController) {
		final MapController mapController = modeController.getMapController();
		final ReadManager readManager = mapController.getReadManager();
		final WriteManager writeManager = mapController.getWriteManager();
		final MapReader mapReader = mapController.getMapReader();
		readManager.addAttributeHandler(NodeBuilder.XML_NODE, ALIAS, new IAttributeHandler() {
			@Override
			public void setAttribute(final Object node, final String value) {
				NodeAlias.setAlias((NodeModel) node, value);
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
	protected final ModeController modeController;


	public MapExplorerController(ModeController modeController) {
		super();
		this.modeController = modeController;
	}

	public boolean isGlobal(final NodeModel node) {
		return GlobalNodes.isGlobal(node);
	}

	public MapExplorer getMapExplorer(NodeModel start, String path, AccessedNodes accessedNodes) {
		return new MapExplorer(start, path, accessedNodes);
	}

	public String getNodeReferenceSuggestion(NodeModel node) {
		final StringBuilder sb = new StringBuilder();
		if(isGlobal(node))
			sb.append(':');
		final String alias = getAlias(node);
		if(!alias.isEmpty())
			sb.append('~').append(alias);
		else
			sb.append('\'').append(TextUtils.getShortText(HtmlUtils.htmlToPlain(node.getText()), 10, "...")).append('\'');
		return sb.toString();
	}

	public String getAlias(final NodeModel node) {
		return NodeAlias.getAlias(node);
	}
	public NodeModel getNodeAt(NodeModel start, String reference) {
		return getNodeAt(start, reference, AccessedNodes.IGNORE);
	}

	public NodeModel getNodeAt(NodeModel start, String reference, AccessedNodes accessedNodes) {
		if(reference.startsWith("ID"))
			return  start.getMap().getNodeForID(reference);
		else if(start != null && reference.startsWith("at(") && reference.endsWith(")")){
			String path = reference.substring(3, reference.length() - 1);
			try {
				return new MapExplorer(start, path, accessedNodes).getNode();
			}
			catch (IllegalStateException|IllegalArgumentException e) {
				return null;
			}
		}
		else {
			LogUtils.warn("Invalid reference format in" + reference);
			return null;
		}
	}


}
