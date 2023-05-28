package org.freeplane.features.map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.freeplane.api.ChildNodesLayout;
import org.freeplane.features.layout.LayoutController;
import org.freeplane.features.map.NodeModel.Side;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SummaryLevelsShould {
	protected MapFake mapFake;
	Controller backupController;
	@Before
	public void setup(){
		mapFake = new MapFake();
		Controller controllerMock = mock(Controller.class);
		ModeController modeControllerMock = mock(ModeController.class);
		LayoutController layoutControllerMock = mock(LayoutController.class);
		backupController = Controller.getCurrentController();
		Controller.setCurrentController(controllerMock);
		when(controllerMock.getModeController()).thenReturn(modeControllerMock);
		when(modeControllerMock.getExtension(LayoutController.class)).thenReturn(layoutControllerMock);
		when(layoutControllerMock.getEffectiveChildNodesLayout(any())).thenReturn(ChildNodesLayout.AUTO);
		when(layoutControllerMock.sidesOf(any(), any())).thenAnswer(invocation -> {
		    NodeModel parentNode = invocation.getArgument(0);
		    NodeModel root = invocation.getArgument(1);
		    return parentNode == root ? LayoutController.BOTH_SIDES : parentNode.isTopOrLeft(root) ? LayoutController.LEFT_SIDE : LayoutController.RIGHT_SIDE;
		});
	}

	@After
	public void tearDown() {
	    Controller.setCurrentController(backupController);
	}

	public static class FindSummaryNodeIndex extends SummaryLevelsShould{

		@Test
		public void returnNull_IfSummaryNodeDoesNotExist() throws Exception {
			mapFake.addNode("1");
			final SummaryLevels summaryLevels = new SummaryLevels(mapFake.getRoot(), mapFake.getRoot());
			assertThat(summaryLevels.findSummaryNodeIndex(0), equalTo(-1));

		}

		@Test
		public void returnSummaryNodeLevel1AfterItem() throws Exception {
			mapFake.addNode("1");
			final NodeModel summaryNode = mapFake.addSummaryNode();
			final SummaryLevels summaryLevels = new SummaryLevels(mapFake.getRoot(), mapFake.getRoot());
			assertThat(summaryLevels.findSummaryNodeIndex(0), equalTo(summaryNode.getIndex()));

		}
	}
	public static class FindSummaryNode extends SummaryLevelsShould{

		@Test
		public void returnNull_IfSummaryNodeDoesNotExist() throws Exception {
			mapFake.addNode("1");
			final SummaryLevels summaryLevels = new SummaryLevels(mapFake.getRoot(), mapFake.getRoot());
			assertThat(summaryLevels.findSummaryNode(0), equalTo((NodeModel)null));

		}

		@Test
		public void returnSummaryNodeLevel1AfterItem() throws Exception {
			mapFake.addNode("1");
			final NodeModel summaryNode = mapFake.addSummaryNode();
			final SummaryLevels summaryLevels = new SummaryLevels(mapFake.getRoot(), mapFake.getRoot());
			assertThat(summaryLevels.findSummaryNode(0), equalTo(summaryNode));

		}

		@Test
		public void returnSummaryNodeLevel1AfterTwoItems() throws Exception {
			mapFake.addNode("1");
			mapFake.addNode("2");
			final NodeModel summaryNode = mapFake.addSummaryNode();
			final SummaryLevels summaryLevels = new SummaryLevels(mapFake.getRoot(), mapFake.getRoot());
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
			final SummaryLevels summaryLevels = new SummaryLevels(mapFake.getRoot(), mapFake.getRoot());
			assertThat(summaryLevels.findSummaryNode(2), equalTo(summaryNode));
		}

		@Test
		public void ignoreNodesOnDifferentSides() throws Exception {
			mapFake.addNode("1").setSide(Side.TOP_OR_LEFT);
			mapFake.addNode("2");
			mapFake.addSummaryNode();
			final SummaryLevels summaryLevels = new SummaryLevels(mapFake.getRoot(), mapFake.getRoot());
			assertThat(summaryLevels.findSummaryNode(0), equalTo((NodeModel)null));

		}

		@Test
		public void returnNull_IfGroupBefinNodeIsFound() throws Exception {
			mapFake.addNode("1");
			mapFake.addGroupBeginNode();
			mapFake.addNode("2");
			mapFake.addSummaryNode();
			final SummaryLevels summaryLevels = new SummaryLevels(mapFake.getRoot(), mapFake.getRoot());
			assertThat(summaryLevels.findSummaryNode(0), equalTo((NodeModel)null));

		}
	}
	public static class FindGroupBeginNodeIndex extends SummaryLevelsShould{

		@Test
		public void returnNull_IfGroupBeginNodeDoesNotExist() throws Exception {
			final NodeModel node = mapFake.addNode("1");
			final SummaryLevels summaryLevels = new SummaryLevels(mapFake.getRoot(), mapFake.getRoot());
			assertThat(summaryLevels.findGroupBeginNodeIndex(node.getIndex()), equalTo(node.getIndex()));
		}

		@Test
		public void returnGroupBeginNodeLevel0_itself() throws Exception {
			mapFake.addNode("1");
			final NodeModel groupBeginNode = mapFake.addGroupBeginNode();
			final SummaryLevels summaryLevels = new SummaryLevels(mapFake.getRoot(), mapFake.getRoot());
			assertThat(summaryLevels.findGroupBeginNodeIndex(groupBeginNode.getIndex()), equalTo(groupBeginNode.getIndex()));
		}

		@Test
		public void returnGroupBeginNodeLevel0AfterItem() throws Exception {
			final NodeModel groupBeginNode = mapFake.addGroupBeginNode();
			final NodeModel node = mapFake.addNode("1");
			final SummaryLevels summaryLevels = new SummaryLevels(mapFake.getRoot(), mapFake.getRoot());
			assertThat(summaryLevels.findGroupBeginNodeIndex(node.getIndex()), equalTo(groupBeginNode.getIndex()));
		}
	}
	public static class FindGroupBeginNode extends SummaryLevelsShould{

		@Test
		public void returnFirstNode_IfGroupBeginNodeDoesNotExist() throws Exception {
			final NodeModel node = mapFake.addNode("1");
			final SummaryLevels summaryLevels = new SummaryLevels(mapFake.getRoot(), mapFake.getRoot());
			assertThat(summaryLevels.findGroupBeginNode(node.getIndex()), equalTo(node));
		}

		@Test
		public void returnGroupBeginNodeLevel0AfterItem() throws Exception {
			final NodeModel groupBeginNode = mapFake.addGroupBeginNode();
			final NodeModel node = mapFake.addNode("1");
			final SummaryLevels summaryLevels = new SummaryLevels(mapFake.getRoot(), mapFake.getRoot());
			assertThat(summaryLevels.findGroupBeginNode(node.getIndex()), equalTo(groupBeginNode));

		}

		@Test
		public void returnGroupBeginNodeLevel0AfterTwoItems() throws Exception {
			final NodeModel groupBeginNode = mapFake.addGroupBeginNode();
			mapFake.addNode("1");
			final NodeModel node = mapFake.addNode("2");
			final SummaryLevels summaryLevels = new SummaryLevels(mapFake.getRoot(), mapFake.getRoot());
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
			final SummaryLevels summaryLevels = new SummaryLevels(mapFake.getRoot(), mapFake.getRoot());
			assertThat(summaryLevels.findGroupBeginNode(secondSummaryNode.getIndex()), equalTo(summaryGroupBeginNode));
		}


		@Test
		public void ignoreNodesOnDifferentSides() throws Exception {
			mapFake.addGroupBeginNode();
			final NodeModel leftNode = mapFake.addNode("1");
			leftNode.setSide(Side.TOP_OR_LEFT);
			mapFake.addNode("2");
			final SummaryLevels summaryLevels = new SummaryLevels(mapFake.getRoot(), mapFake.getRoot());
			assertThat(summaryLevels.findGroupBeginNode(leftNode.getIndex()), equalTo(leftNode));

		}


		@Test
		public void returnNull_IfSummaryNodeIsFound() throws Exception {
			mapFake.addGroupBeginNode();
			mapFake.addNode("1");
			mapFake.addSummaryNode();
			final NodeModel node = mapFake.addNode("2");
			final SummaryLevels summaryLevels = new SummaryLevels(mapFake.getRoot(), mapFake.getRoot());
			assertThat(summaryLevels.findGroupBeginNode(node.getIndex()), equalTo(node));
		}
	}
}
