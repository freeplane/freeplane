package org.freeplane.features.icon.mindmapmode;

import static org.assertj.core.api.Assertions.assertThat;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

import org.freeplane.features.icon.TagCategories;
import org.freeplane.features.icon.TagCategoriesTest;
import org.junit.Test;

public class TagCellRendererTest {
    @Test
    public void appendsUsageCountToTheDisplayedTag() {
        TagCategories tagCategories = TagCategoriesTest.tagCategories("urgent\n");
        DefaultMutableTreeNode tagNode = (DefaultMutableTreeNode) tagCategories.getRootNode().getChildAt(0);
        TagCellRenderer renderer = new TagCellRenderer(tagCategories, node -> 2);

        renderer.getTreeCellRendererComponent(new JTree(tagCategories.getNodes()), tagNode,
            false, false, true, 0, false);

        // TagIcon renders the tag name; the cell text contributes the usage suffix.
        assertThat(renderer.getText()).isEqualTo(" (2)");

    }

    @Test
    public void fadesUnusedTags() {
        TagCategories tagCategories = TagCategoriesTest.tagCategories("unused\n");
        DefaultMutableTreeNode tagNode = (DefaultMutableTreeNode) tagCategories.getRootNode().getChildAt(0);
        TagCellRenderer renderer = new TagCellRenderer(tagCategories, node -> 0);

        renderer.getTreeCellRendererComponent(new JTree(tagCategories.getNodes()), tagNode,
            false, false, true, 0, false);

        // Unused tags remain visible, but their label and icon are rendered with reduced opacity.
        assertThat(renderer.getText()).isEqualTo(" (0)");
        assertThat(renderer.getForeground().getAlpha()).isEqualTo(100);
    }
}
