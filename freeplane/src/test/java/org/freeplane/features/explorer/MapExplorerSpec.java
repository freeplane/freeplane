package org.freeplane.features.explorer;


import static org.mockito.Mockito.mock;

import java.util.List;

import org.freeplane.features.map.NodeModel;
import org.freeplane.features.text.TextController;
import org.mockito.Mockito;

public class MapExplorerSpec {

	static TextController textController = mock(TextController.class);
	static AccessedNodes accessedNodes = mock(AccessedNodes.class);


	static class ReferenceMapExplorer {
		NodeModel start;
		List<TestCommand> path;
		public ReferenceMapExplorer(NodeModel start, List<TestCommand> commands) {
			this.start = start;
			this.path = commands;
		}

	}

	static class TestCommand {
			TextController textController = MapExplorerSpec.textController;
			AccessedNodes accessedNodes = MapExplorerSpec.accessedNodes;
			String searchedString;
			ExploringStep operator;
			public TestCommand(ExploringStep operator, String searchedString) {
				super();
				this.operator = operator;
				this.searchedString = searchedString;
			}
	}

	NodeModel node = Mockito.mock(NodeModel.class);
}
