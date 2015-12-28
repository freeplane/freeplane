package org.freeplane.features.map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

public class SummaryLevelsShould {
	protected MapFake mapFake;
	@Before
	public void setup(){
		mapFake = new MapFake();
	}
	
	public static class FindSummaryNodeIndex extends SummaryLevelsShould{

		@Test
		public void returnNull_IfSummaryNodeDoesNotExist() throws Exception {
			mapFake.addNode("1");
			final SummaryLevels summaryLevels = new SummaryLevels(mapFake.getRoot());
			assertThat(summaryLevels.findSummaryNodeIndex(0), equalTo(-1));

		}

		@Test
		public void returnSummaryNodeLevel1AfterItem() throws Exception {
			mapFake.addNode("1");
			final NodeModel summaryNode = mapFake.addSummaryNode();
			final SummaryLevels summaryLevels = new SummaryLevels(mapFake.getRoot());
			assertThat(summaryLevels.findSummaryNodeIndex(0), equalTo(summaryNode.getIndex()));

		}
	}
	public static class FindSummaryNode extends SummaryLevelsShould{

		@Test
		public void returnNull_IfSummaryNodeDoesNotExist() throws Exception {
			mapFake.addNode("1");
			final SummaryLevels summaryLevels = new SummaryLevels(mapFake.getRoot());
			assertThat(summaryLevels.findSummaryNode(0), equalTo((NodeModel)null));

		}

		@Test
		public void returnSummaryNodeLevel1AfterItem() throws Exception {
			mapFake.addNode("1");
			final NodeModel summaryNode = mapFake.addSummaryNode();
			final SummaryLevels summaryLevels = new SummaryLevels(mapFake.getRoot());
			assertThat(summaryLevels.findSummaryNode(0), equalTo(summaryNode));

		}

		@Test
		public void returnSummaryNodeLevel1AfterTwoItems() throws Exception {
			mapFake.addNode("1");
			mapFake.addNode("2");
			final NodeModel summaryNode = mapFake.addSummaryNode();
			final SummaryLevels summaryLevels = new SummaryLevels(mapFake.getRoot());
			assertThat(summaryLevels.findSummaryNode(0), equalTo(summaryNode));
		}
		@Test
		public void returnSummaryOfSummaryNodeLevel1AfterTwoItems() throws Exception {
			mapFake.addGroupBeginNode();
			mapFake.addNode("1");
			mapFake.addSummaryNode();
			mapFake.addGroupBeginNode();
			mapFake.addNode("2");
			mapFake.addSummaryNode();
			NodeModel summaryNode = mapFake.addSummaryNode();
			final SummaryLevels summaryLevels = new SummaryLevels(mapFake.getRoot());
			assertThat(summaryLevels.findSummaryNode(2), equalTo(summaryNode));
		}

		@Test
		public void ignoreNodesOnDifferentSides() throws Exception {
			mapFake.addNode("1").setLeft(true);
			mapFake.addNode("2");
			mapFake.addSummaryNode();
			final SummaryLevels summaryLevels = new SummaryLevels(mapFake.getRoot());
			assertThat(summaryLevels.findSummaryNode(0), equalTo((NodeModel)null));

		}
		
		@Test
		public void returnNull_IfGroupBefinNodeIsFound() throws Exception {
			mapFake.addNode("1");
			mapFake.addGroupBeginNode();
			mapFake.addNode("2");
			mapFake.addSummaryNode();
			final SummaryLevels summaryLevels = new SummaryLevels(mapFake.getRoot());
			assertThat(summaryLevels.findSummaryNode(0), equalTo((NodeModel)null));

		}
	}
	public static class FindGroupBeginNodeIndex extends SummaryLevelsShould{

		@Test
		public void returnNull_IfGroupBeginNodeDoesNotExist() throws Exception {
			final NodeModel node = mapFake.addNode("1");
			final SummaryLevels summaryLevels = new SummaryLevels(mapFake.getRoot());
			assertThat(summaryLevels.findGroupBeginNodeIndex(node.getIndex()), equalTo(node.getIndex()));
		}

		@Test
		public void returnGroupBeginNodeLevel0_itself() throws Exception {
			mapFake.addNode("1");
			final NodeModel groupBeginNode = mapFake.addGroupBeginNode();
			final SummaryLevels summaryLevels = new SummaryLevels(mapFake.getRoot());
			assertThat(summaryLevels.findGroupBeginNodeIndex(groupBeginNode.getIndex()), equalTo(groupBeginNode.getIndex()));
		}
		
		@Test
		public void returnGroupBeginNodeLevel0AfterItem() throws Exception {
			final NodeModel groupBeginNode = mapFake.addGroupBeginNode();
			final NodeModel node = mapFake.addNode("1");
			final SummaryLevels summaryLevels = new SummaryLevels(mapFake.getRoot());
			assertThat(summaryLevels.findGroupBeginNodeIndex(node.getIndex()), equalTo(groupBeginNode.getIndex()));
		}
	}
	public static class FindGroupBeginNode extends SummaryLevelsShould{

		@Test
		public void returnFirstNode_IfGroupBeginNodeDoesNotExist() throws Exception {
			final NodeModel node = mapFake.addNode("1");
			final SummaryLevels summaryLevels = new SummaryLevels(mapFake.getRoot());
			assertThat(summaryLevels.findGroupBeginNode(node.getIndex()), equalTo(node));
		}

		@Test
		public void returnGroupBeginNodeLevel0AfterItem() throws Exception {
			final NodeModel groupBeginNode = mapFake.addGroupBeginNode();
			final NodeModel node = mapFake.addNode("1");
			final SummaryLevels summaryLevels = new SummaryLevels(mapFake.getRoot());
			assertThat(summaryLevels.findGroupBeginNode(node.getIndex()), equalTo(groupBeginNode));

		}

		@Test
		public void returnGroupBeginNodeLevel0AfterTwoItems() throws Exception {
			final NodeModel groupBeginNode = mapFake.addGroupBeginNode();
			mapFake.addNode("1");
			final NodeModel node = mapFake.addNode("2");
			final SummaryLevels summaryLevels = new SummaryLevels(mapFake.getRoot());
			assertThat(summaryLevels.findGroupBeginNode(node.getIndex()), equalTo(groupBeginNode));
		}
		
		@Test
		public void returnSummaryOfSummaryNodeLevel1AfterTwoItems() throws Exception {
			mapFake.addGroupBeginNode();
			mapFake.addNode("1");
			final NodeModel summaryGroupBeginNode = mapFake.addSummaryGroupBeginNode();
			mapFake.addGroupBeginNode();
			mapFake.addNode("2");
			final NodeModel secondSummaryNode = mapFake.addSummaryNode();
			mapFake.addSummaryNode();
			final SummaryLevels summaryLevels = new SummaryLevels(mapFake.getRoot());
			assertThat(summaryLevels.findGroupBeginNode(secondSummaryNode.getIndex()), equalTo(summaryGroupBeginNode));
		}


		@Test
		public void ignoreNodesOnDifferentSides() throws Exception {
			mapFake.addGroupBeginNode();
			final NodeModel leftNode = mapFake.addNode("1");
			leftNode.setLeft(true);
			mapFake.addNode("2");
			final SummaryLevels summaryLevels = new SummaryLevels(mapFake.getRoot());
			assertThat(summaryLevels.findGroupBeginNode(leftNode.getIndex()), equalTo(leftNode));

		}

		
		@Test
		public void returnNull_IfSummaryNodeIsFound() throws Exception {
			mapFake.addGroupBeginNode();
			mapFake.addNode("1");
			mapFake.addSummaryNode();
			final NodeModel node = mapFake.addNode("2");
			final SummaryLevels summaryLevels = new SummaryLevels(mapFake.getRoot());
			assertThat(summaryLevels.findGroupBeginNode(node.getIndex()), equalTo(node));
		}
	}
}
