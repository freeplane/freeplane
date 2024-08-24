// Copyright (C) 2024  Dimitry Polivaev, macmarrum (at) outlook (dot) ie
// SPDX-License-Identifier: GPL-2.0-or-later
package org.freeplane.plugin.script.proxy;

import java.util.*;
import java.util.stream.Collectors;

import javax.swing.Icon;

import org.freeplane.features.icon.IconController;
import org.freeplane.features.icon.IconRegistry;
import org.freeplane.features.icon.Tag;
import org.freeplane.features.icon.TagCategories;
import org.freeplane.features.icon.mindmapmode.MIconController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.plugin.script.ScriptContext;

public class TagsProxy  extends AbstractProxy<NodeModel> implements Proxy.Tags {

    TagsProxy(NodeModel delegate, ScriptContext scriptContext) {
        super(delegate, scriptContext);
    }

    @Override
    public List<String> getTags() {
        MIconController iconController = iconController();
        return iconController.getTags(getDelegate()).stream()
                .map(Tag::getContent)
                .collect(Collectors.toList());
    }


    @Override
    public List<String> getCategorizedTags() {
        MIconController iconController = iconController();
        final NodeModel node = getDelegate();
        final IconRegistry iconRegistry = node.getMap().getIconRegistry();
        final TagCategories tagCategories = iconRegistry.getTagCategories();
        return iconController.extendCategories(iconController.getTags(node), tagCategories).stream()
                .map(tag -> tag.getContent())
                .collect(Collectors.toList());
    }

    @Override
    public SortedSet<String> getCategories() {
        MIconController iconController = iconController();
        final NodeModel node = getDelegate();
        final IconRegistry iconRegistry = node.getMap().getIconRegistry();
        final TagCategories tagCategories = iconRegistry.getTagCategories();
        String tagCategorySeparator = tagCategories.getTagCategorySeparator();
        return iconController.extendCategories(iconController.getTags(node), tagCategories).stream()
                .map(tag -> tag.categoryTags(tagCategorySeparator))
                .flatMap(Collection::stream)
                .map(Tag::getContent)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    @Override
    public List<? extends Icon> getIcons() {
        MIconController iconController = iconController();
        return (iconController.getTagIcons(getDelegate()));
    }

    private MIconController iconController() {
        return (MIconController) getModeController().getExtension(IconController.class);
    }

    @Override
    public void setTags(Collection<String> tags) {
        List<Tag> tagList = freeplaneTags(tags);
        MIconController iconController = iconController();
        iconController.setTags(getDelegate(),
                tagList, false);
    }

    private List<Tag> freeplaneTags(Collection<String> keywords) {
        return keywords.stream()
        .map(this::createTag)
        .collect(Collectors.toList());
    }

    @Override
    public void add(String keyword) {
        MIconController iconController = iconController();
        ArrayList<Tag> tagList = new ArrayList<>(iconController.getTags(getDelegate()));
        tagList.add(createTag(keyword));
        iconController.setTags(getDelegate(), tagList, false);
    }

    private Tag createTag(String keyword) {
        return new Tag(keyword);
    }

    @Override
    public void add(int index, String keyword) {
        MIconController iconController = iconController();
        ArrayList<Tag> tagList = new ArrayList<>(iconController.getTags(getDelegate()));
        tagList.add(index, createTag(keyword));
        iconController.setTags(getDelegate(), tagList, false);
    }

    @Override
    public void add(Collection<String> keywords) {
        MIconController iconController = iconController();
        ArrayList<Tag> tagList = new ArrayList<>(iconController.getTags(getDelegate()));
        tagList.addAll(freeplaneTags(keywords));
        iconController.setTags(getDelegate(), tagList, false);
    }

    @Override
    public boolean remove(String keyword) {
        MIconController iconController = iconController();
        ArrayList<Tag> tagList = new ArrayList<>(iconController.getTags(getDelegate()));
        boolean result = tagList.remove(createTag(keyword));
        iconController.setTags(getDelegate(), tagList, false);
        return result;
    }

    @Override
    public String remove(int index) {
        MIconController iconController = iconController();
        ArrayList<Tag> tagList = new ArrayList<>(iconController.getTags(getDelegate()));
        Tag result = tagList.remove(index);
        iconController.setTags(getDelegate(), tagList, false);
        return result == null ? null : result.getContent();
    }

    @Override
    public boolean remove(Collection<String> keywords) {
        MIconController iconController = iconController();
        ArrayList<Tag> tagList = new ArrayList<>(iconController.getTags(getDelegate()));
        boolean result = tagList.removeAll(freeplaneTags(keywords));
        iconController.setTags(getDelegate(), tagList, false);
        return result;
    }

    @Override
    public boolean contains(String searched) {
        return getTags().contains(searched);
    }

    @Override
    public boolean containsAny(Collection<String> searched) {
        Set<String> set = new HashSet<>(getTags());
        return searched.stream().anyMatch(set::contains);
    }

    @Override
    public boolean containsAll(Collection<String> searched) {
        return new HashSet<>(getTags()).containsAll(searched);
    }
    @Override
    public boolean containsCategory(String searched) {
        return getCategories().contains(searched);
    }

    @Override
    public boolean containsAnyCategory(Collection<String> searched) {
        final SortedSet<String> categories = getCategories();
        return searched.stream().anyMatch(categories::contains);
    }

    @Override
    public boolean containsAllCategories(Collection<String> searched) {
        return getCategories().containsAll(searched);
    }
}
