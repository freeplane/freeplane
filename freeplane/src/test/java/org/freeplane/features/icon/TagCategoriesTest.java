/*
 * Created on 4 May 2024
 *
 * author dimitry
 */
package org.freeplane.features.icon;

import static org.assertj.core.api.Assertions.assertThat;

import javax.swing.tree.DefaultMutableTreeNode;

import org.junit.Test;

public class TagCategoriesTest {
    @Test
        public void testRegisterTagReferenceCategories() {
            final DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("tags");
            TagCategories tagCategories = new TagCategories(
                    rootNode,
                    new DefaultMutableTreeNode("uncategorized_tags"), "::");
            String input = "tag1\n"
                    + " tag2\n"
                    + "  tag3\n"
                    + "tag4\n";

            tagCategories.load(input);

            assertThat(rootNode.getChildCount()).isEqualTo(3);

            DefaultMutableTreeNode firstChild = (DefaultMutableTreeNode) rootNode.getChildAt(0);
            assertThat(((Tag) firstChild.getUserObject()).getContent()).isEqualTo("tag1");

            DefaultMutableTreeNode secondChild = (DefaultMutableTreeNode) rootNode.getChildAt(1);
            assertThat(((Tag) secondChild.getUserObject()).getContent()).isEqualTo("tag4");

            DefaultMutableTreeNode grandChild = (DefaultMutableTreeNode) firstChild.getChildAt(0);
            assertThat(((Tag) grandChild.getUserObject()).getContent()).isEqualTo("tag1::tag2");

            DefaultMutableTreeNode greatGrandChild = (DefaultMutableTreeNode) grandChild.getChildAt(0);
            assertThat(((Tag) greatGrandChild.getUserObject()).getContent()).isEqualTo("tag1::tag2::tag3");

            assertThat(tagCategories.serialize()
            		.replaceAll("#.*", "")
            		.replace(System.lineSeparator(), "\n"))
            .isEqualTo(input);
        }
}
