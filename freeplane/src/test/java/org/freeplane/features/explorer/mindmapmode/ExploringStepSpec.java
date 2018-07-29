package org.freeplane.features.explorer.mindmapmode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.freeplane.features.explorer.mindmapmode.ExploringStep;
import org.freeplane.features.explorer.mindmapmode.NodeMatcher;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.junit.Test;

public class ExploringStepSpec {

	@Test
	public void findsParent() throws Exception {
		NodeMatcher matcher = mock(NodeMatcher.class);
		NodeModel node = new NodeModel(null);
		NodeModel parent = new NodeModel(null);
		parent.insert(node);
		assertThat(ExploringStep.PARENT.getSingleNode(node, matcher)).isSameAs(parent);
		assertThat(ExploringStep.PARENT.getAllNodes(node, matcher)).containsExactly(parent);
		verifyNoMoreInteractions(matcher);
	}

	@Test
	public void findsRoot() throws Exception {
		NodeMatcher matcher = mock(NodeMatcher.class);
		MapModel map = mock(MapModel.class);
		NodeModel node = new NodeModel(map);
		NodeModel root = new NodeModel(map);
		when(map.getRootNode()).thenReturn(root);
		assertThat(ExploringStep.ROOT.getSingleNode(node, matcher)).isSameAs(root);
		assertThat(ExploringStep.ROOT.getAllNodes(node, matcher)).containsExactly(root);
		verifyNoMoreInteractions(matcher);
	}
}
