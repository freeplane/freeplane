/*
 * Created on 2 Apr 2024
 *
 * author dimitry
 */
package org.freeplane.features.icon;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.freeplane.core.extension.IExtension;
import org.freeplane.features.map.NodeModel;

public class Tags implements IExtension {
    private List<TagReference> tags;

    private Tags(List<TagReference> tags) {
        super();
        this.tags = tags;
    }

    static List<TagReference> getTagReferences(NodeModel node){
        Tags tags = node.getExtension(Tags.class);
        return (tags == null) ? Collections.emptyList() : tags.tags;
    }

    public static void setTagReferences(NodeModel node, List<TagReference> newTags) {
        Tags extension = node.getExtension(Tags.class);
        if(extension == null) {
            extension = new Tags(newTags);
            node.addExtension(extension);
        }
        else
            extension.tags = newTags;
    }

    public List<TagReference> getTagReferencess(){
        return Collections.unmodifiableList(tags);
    }

    public List<Tag> getTags() {
        return tags
                .stream()
                .map(TagReference::getTag)
                .filter(x -> x != Tag.REMOVED_TAG)
                .collect(Collectors.toList());
    }

}
