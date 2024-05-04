/*
 * Created on 4 May 2024
 *
 * author dimitry
 */
package org.freeplane.features.icon.mindmapmode;

import java.io.File;
import java.io.StringReader;
import java.util.Scanner;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.assertj.core.api.Assertions;
import org.freeplane.features.icon.Tag;
import org.junit.Test;

public class TagCategoriesTest {
    @Test
    public void testReadTagCategories() {
        TagCategories tagCategories = new TagCategories(
                new DefaultTreeModel(new DefaultMutableTreeNode("tags")),
                new File("testFile"));
        DefaultMutableTreeNode parentNode = new DefaultMutableTreeNode();
        String input = "tag1\n"
                + " tag2\n"
                + "  tag3\n"
                + "tag4";
        Scanner scanner = new Scanner(new StringReader(input));

        tagCategories.readTagCategories(parentNode, scanner);

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
