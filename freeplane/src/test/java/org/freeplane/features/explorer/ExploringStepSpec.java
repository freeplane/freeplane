package org.freeplane.features.explorer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.junit.Test;

public class ExploringStepSpec {

	private AccessedNodes accessedNodes =mock(AccessedNodes.class);

	@Test
	public void findsParent() throws Exception {
		NodeMatcher matcher = mock(NodeMatcher.class);
		NodeModel node = new NodeModel(null);
		NodeModel parent = new NodeModel(null);
		parent.insert(node);
		assertThat(ExploringStep.PARENT.getNodes(node, matcher, accessedNodes).get(0)).isSameAs(parent);
		assertThat(ExploringStep.PARENT.getNodes(node, matcher, accessedNodes)).containsExactly(parent);
		verifyNoMoreInteractions(matcher);
	}

	@Test
	public void findsRoot() throws Exception {
		NodeMatcher matcher = mock(NodeMatcher.class);
		MapModel map = mock(MapModel.class);
		NodeModel node = new NodeModel(map);
		NodeModel root = new NodeModel(map);
		when(map.getRootNode()).thenReturn(root);
		assertThat(ExploringStep.ROOT.getNodes(node, matcher, accessedNodes).get(0)).isSameAs(root);
		assertThat(ExploringStep.ROOT.getNodes(node, matcher, accessedNodes)).containsExactly(root);
		verifyNoMoreInteractions(matcher);
	}
}
