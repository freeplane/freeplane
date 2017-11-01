package org.freeplane.plugin.collaboration.client;

import org.freeplane.plugin.collaboration.client.event.children.ImmutableChild;
import org.freeplane.plugin.collaboration.client.event.children.ChildrenUpdated.Child;
import org.freeplane.plugin.collaboration.client.event.children.ChildrenUpdated.Side;

public interface TestData {

	String PARENT_NODE_ID = "id_parent";
	String PARENT_NODE_ID2 = "id_parent2";
	String CHILD_NODE_ID = "id_child";
	String CHILD_NODE_ID2 = "id_child2";
	
	public static final ImmutableChild RIGHT_CHILD = Child.builder().side(Side.RIGHT).id(TestData.CHILD_NODE_ID).build();
	public static final ImmutableChild LEFT_CHILD = Child.builder().side(Side.LEFT).id(TestData.CHILD_NODE_ID).build();
	public static final ImmutableChild CHILD = Child.builder().id(TestData.CHILD_NODE_ID).build();
	public static final ImmutableChild CHILD2 = Child.builder().id(TestData.CHILD_NODE_ID2).build();



}
