/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
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

import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JPopupMenu;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.AMultipleNodeAction;
import org.freeplane.core.ui.IMouseWheelEventHandler;
import org.freeplane.core.ui.IUserInputListenerFactory;
import org.freeplane.features.filter.Filter;
import org.freeplane.features.filter.FilterController;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;

/**
 * @author foltin
 */
public class FoldingController implements IMouseWheelEventHandler, IExtension {

    @SuppressWarnings("serial")
    private class FoldAllAction extends AMultipleNodeAction {


        public FoldAllAction() {
            super("FoldAllAction");
        }

        @Override
        public void actionPerformed(final ActionEvent e, final NodeModel node) {
            Filter filter = FilterController.getFilter(node.getMap());
            NodeModel selectionRoot = Controller.getCurrentController().getSelection().getSelectionRoot();
            new FoldLevelChanger(selectionRoot, filter).foldAll(node);
        }
    }

    @SuppressWarnings("serial")
    private class FoldOneLevelAction extends AMultipleNodeAction {


        public FoldOneLevelAction() {
            super("FoldOneLevelAction");
        }

        @Override
        public void actionPerformed(final ActionEvent e, final NodeModel node) {
            NodeModel selectionRoot = Controller.getCurrentController().getSelection().getSelectionRoot();
            Filter filter = FilterController.getFilter(node.getMap());
            new FoldLevelChanger(selectionRoot, filter).foldOneStage(node);
        }
    }

    @SuppressWarnings("serial")
    private class UnfoldAllAction extends AMultipleNodeAction {


        public UnfoldAllAction() {
            super("UnfoldAllAction");
        }

        @Override
        public void actionPerformed(final ActionEvent e, final NodeModel node) {
            IMapSelection selection = Controller.getCurrentController().getSelection();
            NodeModel selectionRoot = selection.getSelectionRoot();
            Filter filter = FilterController.getFilter(node.getMap());
            new FoldLevelChanger(selectionRoot, filter).unfoldAll(node);
            if(selection.size() == 1 ) {
                final MapController mapController = Controller.getCurrentModeController().getMapController();
                mapController.scrollNodeTreeAfterUnfold(node);
            }
        }
    }

    @SuppressWarnings("serial")
    private class UnfoldOneLevelAction extends AMultipleNodeAction {

        public UnfoldOneLevelAction() {
            super("UnfoldOneLevelAction");
        }

        @Override
        public void actionPerformed(final ActionEvent e, final NodeModel node) {
            IMapSelection selection = Controller.getCurrentController().getSelection();
            NodeModel selectionRoot = selection.getSelectionRoot();
            Filter filter = FilterController.getFilter(node.getMap());
            new FoldLevelChanger(selectionRoot, filter).unfoldOneStage(node);
            if(selection.size() == 1 ) {
                final MapController mapController = Controller.getCurrentModeController().getMapController();
                mapController.scrollNodeTreeAfterUnfold(node);
            }

        }
    }

    protected static Insets nullInsets = new Insets(0, 0, 0, 0);

    @SuppressWarnings("serial")
    private class FoldingPopupMenu extends JPopupMenu{
        final private NodeModel node;
        FoldingPopupMenu(NodeModel node){
            this.node = node;
            addAction(new UnfoldOneLevelPopupAction());
            addAction(new FoldOneLevelPopupAction());
            addAction(new UnfoldAllPopupAction());
            addAction(new FoldAllPopupAction());
        }

        private JButton addAction(Action a) {
            final JButton menuItem = new JButton(a);
            menuItem.setToolTipText(menuItem.getText());
            menuItem.setText(null);
            add(menuItem);
            menuItem.setMargin(nullInsets);
            return menuItem;
        }

        @SuppressWarnings("serial")
        private class FoldAllPopupAction extends FoldAllAction{
            @Override
            public void actionPerformed(final ActionEvent e){
                actionPerformed(e, node);
            }
        }

        @SuppressWarnings("serial")
        private class FoldOneLevelPopupAction extends FoldOneLevelAction{
            @Override
            public void actionPerformed(final ActionEvent e){
                actionPerformed(e, node);
            }
        }

        @SuppressWarnings("serial")
        private class UnfoldAllPopupAction extends UnfoldAllAction{
            @Override
            public void actionPerformed(final ActionEvent e){
                actionPerformed(e, node);
            }
        }

        @SuppressWarnings("serial")
        private class UnfoldOneLevelPopupAction extends UnfoldOneLevelAction{
            @Override
            public void actionPerformed(final ActionEvent e){
                actionPerformed(e, node);
            }
        }
    }



    // // 	final private Controller controller;

    public static void install( final FoldingController foldingController) {
        Controller.getCurrentModeController().addExtension(FoldingController.class, foldingController);
    }
    public FoldingController() {
        super();
        if(!GraphicsEnvironment.isHeadless()){
            final ModeController modeController = Controller.getCurrentModeController();
            final IUserInputListenerFactory userInputListenerFactory = modeController.getUserInputListenerFactory();
            userInputListenerFactory.addMouseWheelEventHandler(this);
            for (final AFreeplaneAction annotatedAction : getAnnotatedActions()) {
                modeController.addAction(annotatedAction);
            }
        }
    }

    private List<AMultipleNodeAction> getAnnotatedActions() {
        final ArrayList<AMultipleNodeAction> result = new ArrayList<AMultipleNodeAction>();
        result.add(new UnfoldAllAction());
        result.add(new FoldAllAction());
        result.add(new UnfoldOneLevelAction());
        result.add(new FoldOneLevelAction());
        return result;
    }


    public JPopupMenu createFoldingPopupMenu(NodeModel node){
        return new FoldingPopupMenu(node);
    }

    @Override
    public boolean handleMouseWheelEvent(final MouseWheelEvent e) {
        if ((e.getModifiers() & InputEvent.ALT_MASK) != 0) {
            Controller controller = Controller.getCurrentController();
            final IMapSelection selection = controller.getSelection();
            final NodeModel node = selection.getSelected();
            NodeModel selectionRoot = selection.getSelectionRoot();
            Filter filter = FilterController.getFilter(node.getMap());
            if (e.getWheelRotation() > 0) {
                new FoldLevelChanger(selectionRoot, filter).unfoldOneStage(node);
            }
            else {
                new FoldLevelChanger(selectionRoot, filter).foldOneStage(node);
            }
            return true;
        }
        return false;
    }

    static class FoldLevelChanger {
        final private Filter filter;
        final private NodeModel selectionRoot;

        public FoldLevelChanger(NodeModel selectionRoot, Filter filter) {
            super();
            this.selectionRoot = selectionRoot;
            this.filter = filter;
        }

        private void foldAll(NodeModel node) {
            foldAll(node, depth(node));
        }

        private void foldAll(final NodeModel node, int depth) {
            if(filter.isFoldable(node)) {
                if(depth > 0)
                    setFolded(node, true);
                for (NodeModel child : node.getChildren()) {
                    foldAll(child, depth > 0 ? depth : depth(child, depth));
                }
            }
        }

        private void foldOneStage(final NodeModel node) {
            if(filter.isFoldable(node)) {
                int maxDepth = getMaxDepth(node);
                if(maxDepth > 1)
                    foldStageN(node, maxDepth - 1);
            }
        }

        private void foldStageN(final NodeModel node, final int stage) {
            if(filter.isFoldable(node)) {
                final int k = depth(node);
                if (k < stage) {
                    setFolded(node, false);
                    for (final NodeModel child : node.getChildren()) {
                        foldStageN(child, stage);
                    }
                }
                else {
                    foldAll(node);
                }
            }
        }

        private int getMaxDepth(final NodeModel node) {
            final MapController mapController = Controller.getCurrentModeController().getMapController();
            if (mapController.isFolded(node)
                    || !node.hasChildren()
                    || ! filter.isFoldable(node)) {
                return depth(node);
            }
            int maxDepth = 0;
            for (final NodeModel child : node.getChildren()) {
                final int childMaxDepth = getMaxDepth(child);
                if (childMaxDepth > maxDepth) {
                    maxDepth = childMaxDepth;
                }
            }
            return maxDepth;
        }

        private int getMinDepth(final NodeModel node) {
            final EncryptionModel encryptionModel = EncryptionModel.getModel(node);
            if (encryptionModel != null && !encryptionModel.isAccessible() ) {
                return Integer.MAX_VALUE;
            }
            final MapController mapController = Controller.getCurrentModeController().getMapController();
            Filter filter = Controller.getCurrentController().getSelection().getFilter();
            if (!node.hasChildren()) {
                return Integer.MAX_VALUE;
            }
            if(node.hasVisibleContent(filter)) {
                if (mapController.isFolded(node)) {
                    return depth(node);
                }
                if (AlwaysUnfoldedNode.isAlwaysUnfolded(node)) {
                    return Integer.MAX_VALUE;
                }
            }
            int minDepth = Integer.MAX_VALUE;
            for (final NodeModel child : node.getChildren()) {
                final int childMinDepth = getMinDepth(child);
                if (childMinDepth < minDepth) {
                    minDepth = childMinDepth;
                }
            }
            return minDepth;
        }

        private void setFolded(final NodeModel node, final boolean state) {
            if (! node.isRoot()) {
                final MapController mapController = Controller.getCurrentModeController().getMapController();
                mapController.setFolded(node, state, filter);
            }
        }

        private void unfoldAll(final NodeModel node) {
            if(filter.isFoldable(node)) {
                setFolded(node, false);
                for (final NodeModel child : node.getChildren()) {
                    unfoldAll(child);
                }
            }
        }

        private void unfoldOneStage(final NodeModel node) {
            if(filter.isFoldable(node)) {
                int minDepth = getMinDepth(node);
                if (minDepth < Integer.MAX_VALUE) {
                    minDepth++;
                }
                unfoldStageN(node, minDepth);
            }
        }

        private void unfoldStageN(final NodeModel node, final int stage) {
            if(filter.isFoldable(node)) {
                final int k = depth(node);
                if (k < stage) {
                    setFolded(node, false);
                    for (final NodeModel child : node.getChildren()) {
                        unfoldStageN(child, stage);
                    }
                }
                else {
                    foldAll(node);
                }
            }
        }

        private int depth(NodeModel node) {
            if (node == selectionRoot || node.isRoot())
                return 0;
            final int parentDepth = depth(node.getParentNode());
            return depth(node, parentDepth);
        }

        private int depth(NodeModel node, final int parentDepth) {
            if (! node.hasVisibleContent(filter) || AlwaysUnfoldedNode.isAlwaysUnfolded(node)) {
                return parentDepth;
            }
            else
                return parentDepth + 1;
        }
    }
}
