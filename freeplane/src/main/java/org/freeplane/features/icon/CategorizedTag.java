/*
 * Created on 4 May 2024
 *
 * author dimitry
 */
package org.freeplane.features.icon;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.freeplane.features.icon.mindmapmode.UncategorizedTag;

public interface CategorizedTag extends Comparable<CategorizedTag> {
    CategorizedTag EMPTY_TAG = new UncategorizedTag(Tag.EMPTY_TAG);
    Tag tag();
    List<Tag> categoryTags();
    default boolean isEmpty() {
        return tag().isEmpty();
    }
    default Tag categorizedTag(String tagCategorySeparator) {
        final Tag tag = tag();
        String content = getContent(tagCategorySeparator);
        return new Tag(content, tag.getColor());
    }
    default String getContent(String tagCategorySeparator) {
        return categoryTags().stream()
                .map(Tag::getContent)
                .collect(Collectors.joining(tagCategorySeparator));
    }

    @Override
    default int compareTo(CategorizedTag other) {
        final List<Tag> categoryTags = categoryTags();
        final List<Tag> otherTags = other.categoryTags();
        return IntStream.range(0, Math.min(categoryTags.size(), otherTags.size()))
                .map(i -> categoryTags.get(i).compareTo(otherTags.get(i)))
                .filter(result -> result != 0)
                .findFirst()
                .orElse(Integer.compare(categoryTags.size(), otherTags.size()));
    }
}
