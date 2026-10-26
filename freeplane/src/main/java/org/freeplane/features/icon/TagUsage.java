package org.freeplane.features.icon;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.freeplane.features.map.NodeModel;

/**
 * Immutable snapshot of how many distinct nodes use each tag in a map.
 *
 * A node contributes at most once per tag, even if its tag-reference list
 * contains the same tag more than once.
 */
public class TagUsage {
    private static final TagUsage NONE = new TagUsage(Collections.emptyMap());

    private final Map<String, Integer> nodeCountByTagContent;

    private TagUsage(Map<String, Integer> nodeCountByTagContent) {
        this.nodeCountByTagContent = nodeCountByTagContent;
    }

    public static TagUsage none() {
        return NONE;
    }

    /**
     * Counts tag usage for the complete subtree rooted at {@code root}.
     *
     * An iterative traversal avoids using the call stack for deeply nested maps.
     */
    public static TagUsage countIn(NodeModel root) {
        if (root == null) {
            return NONE;
        }
        Map<String, Integer> counts = new HashMap<>();
        Deque<NodeModel> pending = new ArrayDeque<>();
        pending.push(root);
        while (!pending.isEmpty()) {
            NodeModel node = pending.pop();
            // Count each tag once per node, then include all descendants in the snapshot.
            for (String content : distinctTagContents(node)) {
                counts.merge(content, 1, Integer::sum);
            }
            for (NodeModel child : node.getChildren()) {
                pending.push(child);
            }
        }
        return new TagUsage(counts);
    }

    /**
     * Filters empty and removed references and de-duplicates the remaining tag contents
     * before they are added to the map-wide count.
     */
    private static Set<String> distinctTagContents(NodeModel node) {
        Set<String> contents = new HashSet<>();
        for (TagReference reference : Tags.getExistingTagReferences(node)) {
            if (reference != null && !reference.isEmpty()) {
                contents.add(reference.getContent());
            }
        }
        return contents;
    }

    public int of(String tagContent) {
        return nodeCountByTagContent.getOrDefault(tagContent, 0);
    }

    public int of(Tag tag) {
        return of(tag.getContent());
    }
}
