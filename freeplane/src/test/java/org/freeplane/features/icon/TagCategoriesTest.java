/*
 * Created on 4 May 2024
 *
 * author dimitry
 */
package org.freeplane.features.icon;

import static org.assertj.core.api.Assertions.assertThat;

import javax.swing.tree.DefaultMutableTreeNode;

import org.freeplane.features.icon.mindmapmode.MIconController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class TagCategoriesTest {
    public static TagCategories tagCategories(String input) {
        TagCategories tagCategories = new TagCategories(
                new DefaultMutableTreeNode("tags"),
                new DefaultMutableTreeNode("uncategorized_tags"), "::");
        tagCategories.load(input);
        return tagCategories;
    }

    public static String serializeWithoutColors(TagCategories tagCategories) {
        return tagCategories.serialize()
                .replaceAll("#.*", "")
                .replace(System.lineSeparator(), "\n");
    }


    @Test
    public void testRegisterTagReferenceCategories() {
        TagCategories tagCategories = tagCategories("tag1\n"
                + " tag2\n"
                + "  tag3\n"
                + "tag4\n");

        final DefaultMutableTreeNode rootNode = tagCategories.getRootNode();
        assertThat(rootNode.getChildCount()).isEqualTo(3);

        DefaultMutableTreeNode firstChild = (DefaultMutableTreeNode) rootNode.getChildAt(0);
        assertThat((tagCategories.categorizedTag(firstChild).getContent())).isEqualTo("tag1");

        DefaultMutableTreeNode secondChild = (DefaultMutableTreeNode) rootNode.getChildAt(1);
        assertThat((tagCategories.categorizedTag(secondChild).getContent())).isEqualTo("tag4");

        DefaultMutableTreeNode grandChild = (DefaultMutableTreeNode) firstChild.getChildAt(0);
        assertThat((tagCategories.categorizedTag(grandChild).getContent())).isEqualTo("tag1::tag2");

        DefaultMutableTreeNode greatGrandChild = (DefaultMutableTreeNode) grandChild.getChildAt(0);
        assertThat((tagCategories.categorizedTag(greatGrandChild).getContent())).isEqualTo("tag1::tag2::tag3");

        assertThat(serializeWithoutColors(tagCategories))
        .isEqualTo("tag1\n"
                + " tag2\n"
                + "  tag3\n"
                + "tag4\n");
    }

}
