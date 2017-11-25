package org.freeplane.plugin.collaboration.client.event.children;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.plugin.collaboration.client.TestData;
import org.freeplane.plugin.collaboration.client.event.MapUpdated;
import org.freeplane.plugin.collaboration.client.event.children.ChildrenUpdated.Child;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UpdateEventFactorySpec {
	private UpdateEventFactory uut = new UpdateEventFactory();
	
	final private TestObjects testObjects = new TestObjects();
	final private MapModel map = testObjects.map;
	final private NodeModel parent = testObjects.parent;
	final private NodeModel child = testObjects.child;
	final private NodeModel child2 = testObjects.child2;
	
	@Test
	public void createsUpdateForLeafNode() throws Exception {
		MapUpdated result = uut.createChildrenUpdatedEvent(parent);

		MapUpdated expected = ImmutableChildrenUpdated.builder()
				.nodeId(parent.getID())
				.content(Collections.<Child>emptyList())
				.build();
		assertThat(result).isEqualTo(expected);
	}
	
	@Test
	public void createsUpdateForNodeWithOneChild() throws Exception {
		parent.insert(child);
		
		MapUpdated result = uut.createChildrenUpdatedEvent(parent);
		
		MapUpdated expected = ImmutableChildrenUpdated.builder()
				.nodeId(parent.getID())
				.content(asList(TestData.CHILD)).build();
		assertThat(result).isEqualTo(expected);
	}
	
	@Test
	public void createsUpdateForNodeWithTwoChildren() throws Exception {
		parent.insert(child);
		parent.insert(child2);
		
		MapUpdated result = uut.createChildrenUpdatedEvent(parent);
		
		MapUpdated expected = ImmutableChildrenUpdated.builder()
				.nodeId(parent.getID())
				.content(asList(TestData.CHILD,TestData.CHILD2)).build();
		assertThat(result).isEqualTo(expected);
	}

	@Test
	public void addsRightSideForRootNodeChildren() throws Exception {
		when(map.getRootNode()).thenReturn(parent);
		parent.insert(child);
		
		MapUpdated result = uut.createChildrenUpdatedEvent(parent);
		
		MapUpdated expected = ImmutableChildrenUpdated.builder()
				.nodeId(parent.getID())
				.content(asList(TestData.RIGHT_CHILD)).build();
		assertThat(result).isEqualTo(expected);
	}


	@Test
	public void addsLeftSideForRootNodeChildren() throws Exception {
		when(map.getRootNode()).thenReturn(parent);
		child.setLeft(true);
		parent.insert(child);
		
		MapUpdated result = uut.createChildrenUpdatedEvent(parent);
		
		MapUpdated expected = ImmutableChildrenUpdated.builder()
				.nodeId(parent.getID())
				.content(asList(TestData.LEFT_CHILD)).build();
		assertThat(result).isEqualTo(expected);
	}
}
