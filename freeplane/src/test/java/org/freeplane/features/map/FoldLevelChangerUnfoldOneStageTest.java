/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2026 Freeplane contributors
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.freeplane.features.map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.freeplane.features.filter.Filter;
import org.freeplane.features.map.FoldingController.FoldLevelChanger;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

public class FoldLevelChangerUnfoldOneStageTest {

    private Filter filter;
    private IMapSelection selection;
    private MapController mapController;
    private MockedStatic<Controller> controllerStatic;

    private final List<NodeModel> foldedNodes = new ArrayList<>();
    private final List<NodeModel> unfoldedNodes = new ArrayList<>();

    private NodeModel root;
    private NodeModel alwaysUnfolded;
    private NodeModel level1;
    private NodeModel level2;
    private NodeModel level3;

    @Before
    public void setUp() throws Exception {
        filter = mock(Filter.class);
        when(filter.isFoldable(any())).thenReturn(true);

        root = node("root", false);
        alwaysUnfolded = node("alwaysUnfolded", true);
        level1 = node("level1", false);
        level2 = node("level2", false);
        level3 = node("level3", false);
        when(root.isRoot()).thenReturn(true);

        children(root, alwaysUnfolded);
        children(alwaysUnfolded, level1);
        children(level1, level2);
        children(level2, level3);

        selection = mock(IMapSelection.class);
        when(selection.getSelectionRoot()).thenReturn(root);
        when(selection.getFilter()).thenReturn(filter);
        when(selection.size()).thenReturn(1);
        markFolded(level1, level2);

        mapController = mock(MapController.class);
        doAnswer(invocation -> {
            NodeModel node = invocation.getArgument(0);
            boolean folded = invocation.getArgument(1);
            (folded ? foldedNodes : unfoldedNodes).add(node);
            return null;
        }).when(mapController).setFolded(any(), anyBoolean(), any());

        Controller controller = mock(Controller.class);
        when(controller.getSelection()).thenReturn(selection);
        ModeController modeController = mock(ModeController.class);
        when(modeController.getMapController()).thenReturn(mapController);

        controllerStatic = mockStatic(Controller.class);
        controllerStatic.when(Controller::getCurrentController).thenReturn(controller);
        controllerStatic.when(Controller::getCurrentModeController).thenReturn(modeController);
    }

    @After
    public void tearDown() {
        if (controllerStatic != null) {
            controllerStatic.close();
        }
    }

    @Test
    public void unfoldsOnlyOneLevelWhenSelectedNodeIsAlwaysUnfolded() throws Exception {
        unfoldOneStage(alwaysUnfolded);

        assertThat(unfoldedNodes).containsExactlyInAnyOrder(alwaysUnfolded, level1);
        assertThat(unfoldedNodes).doesNotContain(level2, level3);
    }

    @Test
    public void unfoldsOnlyOneLevelWhenSelectedNodeIsAnOrdinaryFoldedNode() throws Exception {
        unfoldOneStage(level1);

        assertThat(unfoldedNodes).containsExactly(level1);
        assertThat(unfoldedNodes).doesNotContain(level2, level3);
    }

    private void unfoldOneStage(NodeModel node) throws Exception {
        FoldLevelChanger changer = new FoldLevelChanger(selection, filter);
        Method method = FoldLevelChanger.class.getDeclaredMethod("unfoldOneStage", NodeModel.class);
        method.setAccessible(true);
        method.invoke(changer, node);
    }

    private NodeModel node(String name, boolean alwaysUnfolded) {
        NodeModel node = mock(NodeModel.class, name);
        when(node.hasVisibleContent(any())).thenReturn(true);
        when(node.containsExtension(AlwaysUnfoldedNode.class)).thenReturn(alwaysUnfolded);
        when(node.getChildren()).thenReturn(Collections.<NodeModel>emptyList());
        when(node.hasChildren()).thenReturn(false);
        return node;
    }

    private void children(NodeModel parent, NodeModel... kids) {
        List<NodeModel> list = Arrays.asList(kids);
        when(parent.getChildren()).thenReturn(list);
        when(parent.hasChildren()).thenReturn(kids.length > 0);
        for (NodeModel kid : kids) {
            when(kid.getParentNode()).thenReturn(parent);
        }
    }

    private void markFolded(NodeModel... nodes) {
        for (NodeModel node : nodes) {
            when(selection.isFolded(node)).thenReturn(true);
        }
    }
}
