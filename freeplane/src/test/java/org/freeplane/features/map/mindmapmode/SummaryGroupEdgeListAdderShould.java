package org.freeplane.features.map.mindmapmode;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;

import org.freeplane.features.map.FirstGroupNode.FirstGroupNodeFlag;
import org.freeplane.features.map.MapFake;
import org.freeplane.features.map.NodeModel;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class SummaryGroupEdgeListAdderShould {
	private MapFake mapFake;
	@Before
	public void setup(){
		mapFake = new MapFake();
	}

	@Test
	public void forEmptyList_returnEmptyList() throws Exception {
		final SummaryGroupEdgeListAdder summaryEdgeFinder = new SummaryGroupEdgeListAdder(Collections.<NodeModel>emptyList());
		assertThat(summaryEdgeFinder.addSummaryEdgeNodes(), equalTo(Collections.<NodeModel>emptyList()));
	}

	@Test
	public void forListWithoutSummaryNodes_returnSameList() throws Exception {
		final NodeModel node = mapFake.addNode("1");
		final SummaryGroupEdgeListAdder summaryEdgeFinder = new SummaryGroupEdgeListAdder(Arrays.asList(node));
		assertThat(summaryEdgeFinder.addSummaryEdgeNodes(), equalTo(Arrays.asList(node)));
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
		leftNode.setLeft(true);
		final NodeModel summaryNode = mapFake.addSummaryNode();
		
		final SummaryGroupEdgeListAdder summaryEdgeFinder = new SummaryGroupEdgeListAdder(Arrays.asList(summarized1));
		assertThat(summaryEdgeFinder.addSummaryEdgeNodes(), equalTo(Arrays.asList(firstEdge, summarized1, summaryNode)));
	}


}
