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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UpdateEventFactorySpec {
	@Mock 
	private MapModel map;
	
	private UpdateEventFactory uut = new UpdateEventFactory();

	private NodeModel parent;

	private NodeModel child;

	private NodeModel child2;
	
	@Before
	public void setup() {
		parent = new NodeModel(map);
		parent.setID(TestData.PARENT_NODE_ID);
		child = new NodeModel(map);
		child.setID(TestData.CHILD_NODE_ID);
		child2 = new NodeModel(map);
		child2.setID(TestData.CHILD_NODE_ID2);

	}
	
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
