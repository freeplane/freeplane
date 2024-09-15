/*
 * Created on 4 May 2024
 *
 * author dimitry
 */
package org.freeplane.features.icon;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.Color;

import javax.swing.tree.DefaultMutableTreeNode;

import org.junit.Test;

public class TagCategoriesTest {
    public static TagCategories tagCategories(String input) {
        TagCategories tagCategories = new TagCategories(
                new DefaultMutableTreeNode("tags"),
                new DefaultMutableTreeNode("uncategorized_tags"), "::");
        tagCategories.load(input);
        return tagCategories;
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

        TagAssertions.assertThatSerializedWithoutColors(tagCategories)
        .isEqualTo("tag1\n"
                + " tag2\n"
                + "  tag3\n"
                + "tag4\n");
    }

    @Test
    public void modifiesColor() {
        TagCategories uut = new TagCategories(
                new DefaultMutableTreeNode("tags"),
                new DefaultMutableTreeNode("uncategorized_tags"), "::");
        uut.load("AA#11223344\n"
                + " BB#22334455\n");
        uut.setTagColor("AA::BB", Color.BLACK);
        TagAssertions.assertThatSerialized(uut).isEqualTo("AA#11223344\n"
                + " BB#000000ff\n");
        assertThat(uut.getTag(new Tag("AA::BB")).get().getColor())
        .isEqualTo(Color.BLACK);
    }


    @Test
    public void modifiesColorOfTagExistingBeforeLoading() {
        TagCategories uut = new TagCategories(
                new DefaultMutableTreeNode("tags"),
                new DefaultMutableTreeNode("uncategorized_tags"), "::");
        uut.createTag("AA::BB");
        uut.load("AA#11223344\n"
                + " BB#22334455\n");
        uut.setTagColor("AA::BB", Color.BLACK);
        TagAssertions.assertThatSerialized(uut).isEqualTo("AA#11223344\n"
                + " BB#000000ff\n");
        assertThat(uut.getTag(new Tag("AA::BB")).get().getColor())
        .isEqualTo(Color.BLACK);
    }
}
