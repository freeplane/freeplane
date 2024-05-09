// Copyright (C) 2024  Dimitry Polivaev, macmarrum (at) outlook (dot) ie
// SPDX-License-Identifier: GPL-2.0-or-later
package org.freeplane.plugin.script.proxy;

import java.util.*;
import java.util.stream.Collectors;

import javax.swing.Icon;

import org.freeplane.features.icon.IconController;
import org.freeplane.features.icon.Tag;
import org.freeplane.features.icon.mindmapmode.MIconController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.plugin.script.ScriptContext;

public class TagsProxy  extends AbstractProxy<NodeModel> implements Proxy.Tags {

    TagsProxy(NodeModel delegate, ScriptContext scriptContext) {
        super(delegate, scriptContext);
    }

    @Override
    public List<String> getKeywords() {
        MIconController iconController = iconController();
        return iconController.getTags(getDelegate()).stream()
                .map(Tag::getContent)
                .collect(Collectors.toList());
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
    public void setKeywords(Collection<String> tags) {
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
        return getDelegate().getMap().getIconRegistry().createTag(keyword);
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
    public boolean contains(String keyword) {
        return getKeywords().contains(keyword);
    }

    @Override
    public boolean containsAny(Collection<String> keywords) {
        Set<String> set = new HashSet<>(getKeywords());
        return keywords.stream().anyMatch(set::contains);
    }

    @Override
    public boolean containsAll(Collection<String> keywords) {
        return new HashSet<>(getKeywords()).containsAll(keywords);
    }
}
