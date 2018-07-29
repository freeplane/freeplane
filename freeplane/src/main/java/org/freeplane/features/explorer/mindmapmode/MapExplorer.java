package org.freeplane.features.explorer.mindmapmode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;

import org.freeplane.features.map.NodeModel;
import org.freeplane.features.text.TextController;

public class MapExplorer {

	private final TextController textController;

	private final NodeModel start;
	private final List<Command> path;

	private List<Command> split(String path) {
		if(path.isEmpty())
			return Collections.emptyList();

		final ArrayList<Command> commands = new ArrayList<>();
		final Matcher matcher = ExploringStep.matcher(path);

		int lastAddedCharacterPosition = 0;
		int lastOperatorEnd = 0;

		while(matcher.find()) {
			final int operatorStart = matcher.start();
			if(lastAddedCharacterPosition < operatorStart) {
				final String operatorSubstring = path.substring(lastAddedCharacterPosition, lastOperatorEnd);
				final String searchedString = path.substring(lastOperatorEnd, operatorStart);
				commands.add(new Command(textController, start, ExploringStep.of(operatorSubstring), searchedString));
			}
			lastAddedCharacterPosition = operatorStart;
			lastOperatorEnd = matcher.end();

		}
		if(lastAddedCharacterPosition < path.length()) {
			final String operatorSubstring = path.substring(lastAddedCharacterPosition, lastOperatorEnd);
			final String searchedString = path.substring(lastOperatorEnd);
			commands.add(new Command(textController, start, ExploringStep.of(operatorSubstring), searchedString));
		}

		return commands;

	}

	public MapExplorer(TextController textController, NodeModel start, String path) {
		this.textController = textController;
		this.start = start;
		this.path = split(path);
	}



	MapExplorer(TextController textController, NodeModel start, List<Command> path) {
		this.textController = textController;
		this.start = start;
		this.path = path;
	}

	public NodeModel getNode() {
		NodeModel node = start;
		for(Command command : path) {
			node = command.getSingleNode(node);
		}
		return node;
	}

	public List<? extends NodeModel> getNodes() {
		List<NodeModel> nodes = Arrays.asList(start);
		for(Command command : path) {
			if(nodes.isEmpty())
				return nodes;
			List<NodeModel> nextNodes = new ArrayList<>();
			for(NodeModel from:nodes) {
				nextNodes.addAll(command.getAllNodes(from));
			}
			nodes = nextNodes;
		}
		return nodes;
	}
}
