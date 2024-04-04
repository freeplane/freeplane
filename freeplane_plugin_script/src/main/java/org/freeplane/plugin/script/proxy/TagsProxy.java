/*
 * Created on 4 Apr 2024
 *
 * author dimitry
 */
package org.freeplane.plugin.script.proxy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.Icon;

import org.freeplane.features.icon.IconController;
import org.freeplane.features.icon.Tag;
import org.freeplane.features.icon.Tags;
import org.freeplane.features.icon.mindmapmode.MIconController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.plugin.script.ScriptContext;

public class TagsProxy  extends AbstractProxy<NodeModel> implements Proxy.Tags {

    TagsProxy(NodeModel delegate, ScriptContext scriptContext) {
        super(delegate, scriptContext);
    }

    @Override
    public List<String> getStrings() {
        return Tags.getTags(getDelegate()).stream()
                .map(Tag::getContent)
                .collect(Collectors.toList());

    }

    @Override
    public List<? extends Icon> getIcons() {
        return (iconController().getTagIcons(getDelegate()));
    }

    private MIconController iconController() {
        return (MIconController) getModeController().getExtension(IconController.class);
    }

    @Override
    public void setTags(Collection<String> tags) {
        List<Tag> tagList = freeplaneTags(tags);
        iconController().setTags(getDelegate(),
                tagList);
    }

    private List<Tag> freeplaneTags(Collection<String> tags) {
        return tags.stream()
        .map(Tag::new)
        .collect(Collectors.toList());
    }

    @Override
    public void addTag(String tag) {
        ArrayList<Tag> tagList = new ArrayList<>(Tags.getTags(getDelegate()));
        tagList.add(new Tag(tag));
        iconController().setTags(getDelegate(), tagList);
    }

    @Override
    public void addTag(int index, String tag) {
        ArrayList<Tag> tagList = new ArrayList<>(Tags.getTags(getDelegate()));
        tagList.add(index, new Tag(tag));
        iconController().setTags(getDelegate(), tagList);
    }

    @Override
    public void addAllTags(Collection<String> tags) {
        ArrayList<Tag> tagList = new ArrayList<>(Tags.getTags(getDelegate()));
        tagList.addAll(freeplaneTags(tags));
        iconController().setTags(getDelegate(), tagList);
    }

    @Override
    public boolean removeTag(String tag) {
        ArrayList<Tag> tagList = new ArrayList<>(Tags.getTags(getDelegate()));
        boolean result = tagList.remove(new Tag(tag));
        iconController().setTags(getDelegate(), tagList);
        return result;
    }

    @Override
    public String removeTag(int index) {
        ArrayList<Tag> tagList = new ArrayList<>(Tags.getTags(getDelegate()));
        Tag result = tagList.remove(index);
        iconController().setTags(getDelegate(), tagList);
        return result == null ? null : result.getContent();
    }

    @Override
    public boolean removeAllTags(Collection<String> tags) {
        ArrayList<Tag> tagList = new ArrayList<>(Tags.getTags(getDelegate()));
        boolean result = tagList.removeAll(freeplaneTags(tags));
        iconController().setTags(getDelegate(), tagList);
        return result;
    }

    @Override
    public boolean contains(String tag) {
        return getStrings().contains(tag);

    }

    @Override
    public boolean containsAny(Collection<String> tags) {
        Set<String> set = new HashSet<>(getStrings());
        return tags.stream().anyMatch(set::contains);

    }

    @Override
    public boolean containsAll(Collection<String> tags) {
        return getStrings().containsAll(tags);
    }
}
