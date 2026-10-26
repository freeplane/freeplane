package org.freeplane.features.icon;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.junit.Test;

public class TagUsageTest {
    private final MapModel map = null;

    // Builds a node whose tag references mirror the data stored on real map nodes.
    private NodeModel node(String text, String... tagContents) {
        NodeModel node = new NodeModel(text, map);
        if (tagContents.length > 0) {
            Tags.setTagReferences(node, Arrays.stream(tagContents)
                    .map(content -> new TagReference(new Tag(content)))
                    .collect(Collectors.toList()));
        }
        return node;
    }

    // Keeps test maps shallow by default; individual tests add deeper branches when needed.
    private NodeModel rootWith(NodeModel... children) {
        NodeModel root = node("root");
        for (NodeModel child : children) {
            root.insert(child);
        }
        return root;
    }

    @Test
    public void nullRootHasNoUsage() {
        assertThat(TagUsage.countIn(null).of("a")).isZero();
    }

    @Test
    public void mapWithoutTagsHasNoUsage() {
        NodeModel root = rootWith(node("x"), node("y"));

        assertThat(TagUsage.countIn(root).of("a")).isZero();
    }

    @Test
    public void countsNodesUsingTag() {
        NodeModel root = rootWith(node("x", "a"), node("y", "a", "b"), node("z", "b"));

        TagUsage usage = TagUsage.countIn(root);

        assertThat(usage.of("a")).isEqualTo(2);
        assertThat(usage.of("b")).isEqualTo(2);
    }

    @Test
    public void countsTagsOnRootAndDeepDescendants() {
        NodeModel deep = node("deep", "a");
        NodeModel middle = node("middle");
        middle.insert(deep);
        NodeModel root = rootWith(middle);
        Tags.setTagReferences(root, Arrays.asList(new TagReference(new Tag("a"))));

        assertThat(TagUsage.countIn(root).of("a")).isEqualTo(2);
    }

    @Test
    public void countsNodeOnceWhenItHoldsSameTagTwice() {
        // A usage count represents nodes using a tag, not duplicate references on one node.
        NodeModel root = rootWith(node("x", "a", "a"));

        assertThat(TagUsage.countIn(root).of("a")).isEqualTo(1);
    }

    @Test
    public void ignoresRemovedAndEmptyTags() {
        NodeModel removed = node("x");
        Tags.setTagReferences(removed, Arrays.asList(
                new TagReference(Tag.REMOVED_TAG),
                new TagReference(Tag.EMPTY_TAG),
                new TagReference(new Tag("a"))));
        NodeModel root = rootWith(removed);

        TagUsage usage = TagUsage.countIn(root);

        assertThat(usage.of("a")).isEqualTo(1);
        assertThat(usage.of(Tag.EMPTY_TAG)).isZero();
        assertThat(usage.of(Tag.REMOVED_TAG)).isZero();
    }

    @Test
    public void distinguishesCategorizedTagsByFullContent() {
        NodeModel root = rootWith(node("x", "project::alpha"), node("y", "project"));

        TagUsage usage = TagUsage.countIn(root);

        assertThat(usage.of("project::alpha")).isEqualTo(1);
        assertThat(usage.of("project")).isEqualTo(1);
        assertThat(usage.of("alpha")).isZero();
    }

    @Test
    public void ofTagUsesTagContent() {
        NodeModel root = rootWith(node("x", "a"));

        assertThat(TagUsage.countIn(root).of(new Tag("a"))).isEqualTo(1);
    }

    @Test
    public void noneReportsZeroForEverything() {
        assertThat(TagUsage.none().of("a")).isZero();
    }
}
