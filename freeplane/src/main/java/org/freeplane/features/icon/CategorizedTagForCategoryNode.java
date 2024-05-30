/*
 * Created on 4 May 2024
 *
 * author dimitry
 */
package org.freeplane.features.icon;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.tree.DefaultMutableTreeNode;

public class CategorizedTagForCategoryNode implements CategorizedTag {
    private final DefaultMutableTreeNode categoryNode;
    private final Tag registeredTag;

    public CategorizedTagForCategoryNode(DefaultMutableTreeNode categoryNode) {
        this(categoryNode, Optional.empty());
    }

    public CategorizedTagForCategoryNode(DefaultMutableTreeNode categoryNode, Optional<Tag> registeredTag) {
        this.categoryNode = categoryNode;
        this.registeredTag = registeredTag.orElseGet(this::nodeTag).copy();
    }

    @Override
    public Tag tag() {
        return registeredTag;
    }

    private Tag nodeTag() {
        final Object userObject = categoryNode.getUserObject();
        if(userObject instanceof Tag)
            return (Tag) userObject;
        else
            return Tag.EMPTY_TAG;
    }

   @Override
   public List<Tag> categoryTags() {
       if((DefaultMutableTreeNode) categoryNode.getParent() == null)
           return Collections.emptyList();
       else
      return Stream.of(categoryNode.getUserObjectPath())
               .skip(1)
               .map(Tag.class::cast)
               .collect(Collectors.toList());
   }

   @Override
   public String toString() {
       return "CategorizedTagForCategoryNode [getContent()=" + getContent("::") + "]";
   }
}
