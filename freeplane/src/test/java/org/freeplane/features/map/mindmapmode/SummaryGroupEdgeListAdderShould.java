package org.freeplane.features.map.mindmapmode;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;

import org.freeplane.api.ChildNodesLayout;
import org.freeplane.features.layout.LayoutController;
import org.freeplane.features.map.FirstGroupNodeFlag;
import org.freeplane.features.map.MapFake;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.NodeModel.Side;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SummaryGroupEdgeListAdderShould {
	private MapFake mapFake;
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

	@Test
	public void forEmptyList_returnEmptyList() throws Exception {
		final SummaryGroupEdgeListAdder summaryEdgeFinder = new SummaryGroupEdgeListAdder(Collections.<NodeModel>emptyList());
		assertThat(summaryEdgeFinder.addSummaryEdgeNodes(), equalTo(Collections.<NodeModel>emptyList()));
	}

	@Test
	public void forGroupBeginNode_returnsNothing() throws Exception {
		final NodeModel node = mapFake.addGroupBeginNode();
		final SummaryGroupEdgeListAdder summaryEdgeFinder = new SummaryGroupEdgeListAdder(Arrays.asList(node));
		assertThat(summaryEdgeFinder.addSummaryEdgeNodes(), equalTo(Arrays.asList()));
	}

	@Test
	public void forSummaryNode_returnSummaryNode() throws Exception {
		final NodeModel node = mapFake.addSummaryNode();
		final SummaryGroupEdgeListAdder summaryEdgeFinder = new SummaryGroupEdgeListAdder(Arrays.asList(node));
		assertThat(summaryEdgeFinder.addSummaryEdgeNodes(), equalTo(Arrays.asList(node)));
	}

	@Test
	public void forListWithoutSummaryNodes_returnSameList() throws Exception {
		final NodeModel node = mapFake.addNode("1");
		final SummaryGroupEdgeListAdder summaryEdgeFinder = new SummaryGroupEdgeListAdder(Arrays.asList(node));
		assertThat(summaryEdgeFinder.addSummaryEdgeNodes(), equalTo(Arrays.asList(node)));
	}

	@Test
	public void forRootNode_returnSameList() throws Exception {
		final NodeModel root = mapFake.getRoot();
		final SummaryGroupEdgeListAdder summaryEdgeFinder = new SummaryGroupEdgeListAdder(Arrays.asList(root));
		assertThat(summaryEdgeFinder.addSummaryEdgeNodes(), equalTo(Arrays.asList(root)));
	}

	@Test
	public void forListWithSingleSummarizedNode_returnListWithEdgeNodes() throws Exception {
		final NodeModel firstEdge = mapFake.addGroupBeginNode();
		final NodeModel summarized = mapFake.addNode("1");
		final NodeModel summaryNode = mapFake.addSummaryNode();

		final SummaryGroupEdgeListAdder summaryEdgeFinder = new SummaryGroupEdgeListAdder(Arrays.asList(summarized));
		assertThat(summaryEdgeFinder.addSummaryEdgeNodes(), equalTo(Arrays.asList(firstEdge, summarized, summaryNode)));
	}


	@Test
	public void forListWithSingleSummarizedNodeWithEdgeNodes_returnListWithEdgeNodes() throws Exception {
		final NodeModel firstEdge = mapFake.addGroupBeginNode();
		final NodeModel summarized = mapFake.addNode("1");
		final NodeModel summaryNode = mapFake.addSummaryNode();

		final SummaryGroupEdgeListAdder summaryEdgeFinder = new SummaryGroupEdgeListAdder(Arrays.asList(firstEdge, summarized, summaryNode));
		assertThat(summaryEdgeFinder.addSummaryEdgeNodes(), equalTo(Arrays.asList(firstEdge, summarized, summaryNode)));
	}

	@Test
	public void forListWithTwoSummarizedNodes_returnListWithEdgeNodes() throws Exception {
		final NodeModel firstEdge = mapFake.addGroupBeginNode();
		final NodeModel summarized1 = mapFake.addNode("1");
		final NodeModel summarized2 = mapFake.addNode("2");
		final NodeModel summaryNode = mapFake.addSummaryNode();

		final SummaryGroupEdgeListAdder summaryEdgeFinder = new SummaryGroupEdgeListAdder(Arrays.asList(summarized1, summarized2));
		assertThat(summaryEdgeFinder.addSummaryEdgeNodes(), equalTo(Arrays.asList(firstEdge, summarized1, summarized2, summaryNode)));
	}

	@Test
	public void forListWithThreeSummarizedNodes_returnListWithEdgeNodes() throws Exception {
		final NodeModel firstEdge = mapFake.addGroupBeginNode();
		final NodeModel summarized1 = mapFake.addNode("1");
		final NodeModel summarized2 = mapFake.addNode("2");
		final NodeModel summarized3 = mapFake.addNode("3");
		final NodeModel summaryNode = mapFake.addSummaryNode();

		final SummaryGroupEdgeListAdder summaryEdgeFinder = new SummaryGroupEdgeListAdder(Arrays.asList(summarized1, summarized2, summarized3));
		assertThat(summaryEdgeFinder.addSummaryEdgeNodes(), equalTo(Arrays.asList(firstEdge, summarized1, summarized2, summarized3, summaryNode)));
	}

	@Test
	public void forPartialListWith_returnListWithoutEdgeNodes() throws Exception {
		mapFake.addGroupBeginNode();
		final NodeModel summarized1 = mapFake.addNode("1");
		mapFake.addNode("2");
		mapFake.addSummaryNode();

		final SummaryGroupEdgeListAdder summaryEdgeFinder = new SummaryGroupEdgeListAdder(Arrays.asList(summarized1));
		assertThat(summaryEdgeFinder.addSummaryEdgeNodes(), equalTo(Arrays.asList(summarized1)));
	}

	@Test
	public void forListWithSingleSummarizedNodeAndTwoSummaryLevels_returnListWithEdgeNodes() throws Exception {
		final NodeModel firstEdge = mapFake.addGroupBeginNode();
		final NodeModel summarized = mapFake.addNode("1");
		final NodeModel summaryNode = mapFake.addSummaryNode();
		summaryNode.addExtension(FirstGroupNodeFlag.FIRST_GROUP);
		final NodeModel secondLevelSummaryNode = mapFake.addSummaryNode();

		final SummaryGroupEdgeListAdder summaryEdgeFinder = new SummaryGroupEdgeListAdder(Arrays.asList(summarized));
		assertThat(summaryEdgeFinder.addSummaryEdgeNodes(), equalTo(Arrays.asList(firstEdge, summarized, summaryNode, secondLevelSummaryNode)));
	}


	@Test
	public void forListWithSingleSummarizedNodeAndThreeSummaryLevels_returnListWithEdgeNodes() throws Exception {
		final NodeModel firstEdge = mapFake.addGroupBeginNode();
		final NodeModel summarized = mapFake.addNode("1");
		final NodeModel summaryNode = mapFake.addSummaryNode();
		summaryNode.addExtension(FirstGroupNodeFlag.FIRST_GROUP);
		final NodeModel secondLevelSummaryNode = mapFake.addSummaryNode();
		secondLevelSummaryNode.addExtension(FirstGroupNodeFlag.FIRST_GROUP);
		final NodeModel thirdLevelSummaryNode = mapFake.addSummaryNode();

		final SummaryGroupEdgeListAdder summaryEdgeFinder = new SummaryGroupEdgeListAdder(Arrays.asList(summarized));
		assertThat(summaryEdgeFinder.addSummaryEdgeNodes(), equalTo(Arrays.asList(firstEdge, summarized, summaryNode, secondLevelSummaryNode, thirdLevelSummaryNode)));
	}

	@Test
	public void ignoreNodesOnWrongSide() throws Exception {
		final NodeModel firstEdge = mapFake.addGroupBeginNode();
		final NodeModel summarized1 = mapFake.addNode("1");
		final NodeModel leftNode = mapFake.addNode("2");
		leftNode.setSide(Side.TOP_OR_LEFT);
		final NodeModel summaryNode = mapFake.addSummaryNode();

		final SummaryGroupEdgeListAdder summaryEdgeFinder = new SummaryGroupEdgeListAdder(Arrays.asList(summarized1));
		assertThat(summaryEdgeFinder.addSummaryEdgeNodes(), equalTo(Arrays.asList(firstEdge, summarized1, summaryNode)));
	}


	@Test
	public void multipleParents() throws Exception {
		final NodeModel parent1 = mapFake.addNode("parent1");
		final NodeModel begin1 = mapFake.createGroupBeginNode();
		parent1.insert(begin1);
		final NodeModel summarized1 = mapFake.createNode("1");
		parent1.insert(summarized1);
		final NodeModel summary1 = mapFake.createSummaryNode();
		parent1.insert(summary1);

		final NodeModel parent2 = mapFake.addNode("parent2");
		final NodeModel begin2 = mapFake.createGroupBeginNode();
		parent2.insert(begin2);
		final NodeModel summarized2 = mapFake.createNode("2");
		parent2.insert(summarized2);
		final NodeModel summary2 = mapFake.createSummaryNode();
		parent2.insert(summary2);

		final SummaryGroupEdgeListAdder summaryEdgeFinder = new SummaryGroupEdgeListAdder(Arrays.asList(summarized1, summarized2));
		assertThat(summaryEdgeFinder.addSummaryEdgeNodes(), equalTo(Arrays.asList(begin1, summarized1, summary1, begin2, summarized2, summary2)));
	}
}
