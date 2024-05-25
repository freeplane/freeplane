/*
 * Created on 4 May 2024
 *
 * author dimitry
 */
package org.freeplane.features.icon;

import java.io.StringReader;
import java.util.Scanner;

import javax.swing.tree.DefaultMutableTreeNode;

import org.assertj.core.api.Assertions;
import org.junit.Test;

public class TagCategoriesTest {
    @Test
        public void testRegisterTagCategories() {
            TagCategories tagCategories = new TagCategories(
                    new DefaultMutableTreeNode("tags"), "::");
            DefaultMutableTreeNode parentNode = new DefaultMutableTreeNode();
            String input = "tag1\n"
                    + " tag2\n"
                    + "  tag3\n"
                    + "tag4";
            Scanner scanner = new Scanner(new StringReader(input));

            tagCategories.readTagCategories(parentNode, parentNode.getChildCount(), scanner);

            Assertions.assertThat(parentNode.getChildCount()).isEqualTo(2);

            DefaultMutableTreeNode firstChild = (DefaultMutableTreeNode) parentNode.getChildAt(0);
            Assertions.assertThat(((Tag) firstChild.getUserObject()).getContent()).isEqualTo("tag1");

            DefaultMutableTreeNode secondChild = (DefaultMutableTreeNode) parentNode.getChildAt(1);
            Assertions.assertThat(((Tag) secondChild.getUserObject()).getContent()).isEqualTo("tag4");

            DefaultMutableTreeNode grandChild = (DefaultMutableTreeNode) firstChild.getChildAt(0);
            Assertions.assertThat(((Tag) grandChild.getUserObject()).getContent()).isEqualTo("tag2");

            DefaultMutableTreeNode greatGrandChild = (DefaultMutableTreeNode) grandChild.getChildAt(0);
            Assertions.assertThat(((Tag) greatGrandChild.getUserObject()).getContent()).isEqualTo(
                    "tag3");
        }
}
