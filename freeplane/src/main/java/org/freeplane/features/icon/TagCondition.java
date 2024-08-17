/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Dimitry Polivaev
 *
 *  This file author is Dimitry Polivaev
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.freeplane.features.icon;

import java.util.List;

import org.freeplane.features.filter.condition.StringConditionAdapter;
import org.freeplane.features.map.NodeModel;
import org.freeplane.n3.nanoxml.XMLElement;

/**
 * @author Dimitry Polivaev
 */
abstract class TagCondition extends StringConditionAdapter {
    static final String SEARCH_ACROSS_ALL_CATEGORIES = "SEARCH_ACROSS_ALL_CATEGORIES";

	final private boolean searchesAcrossAllCategories;
	final private String comparedValue;

    /**
	 */
    TagCondition(final String comparedValue, final boolean matchCase,
			final boolean matchApproximately,
			final boolean matchWordwise, boolean ignoreDiacritics,
			boolean searchesAcrossAllCategories) {
		super(matchCase, matchApproximately, matchWordwise, ignoreDiacritics);
        this.comparedValue = comparedValue;
        this.searchesAcrossAllCategories = searchesAcrossAllCategories;
	}

	@Override
	public void fillXML(final XMLElement child) {
		super.fillXML(child);
		if(searchesAcrossAllCategories())
		    child.setAttribute(SEARCH_ACROSS_ALL_CATEGORIES, "true");
	}

    /*
     * (non-Javadoc)
     * @see
     * freeplane.controller.filter.condition.Condition#checkNode(freeplane.modes
     * .MindMapNode)
     */
	@Override
	public boolean checkNode(final NodeModel node) {
	    final IconController iconController = IconController.getController();
	    final List<Tag> tags = iconController.getTags(node);
	    if(searchesAcrossAllCategories()) {
	        final TagCategories tagCategories = node.getMap().getIconRegistry().getTagCategories();
	        final String tagCategorySeparator = tagCategories.getTagCategorySeparator();
	        final List<CategorizedTag> categorizedTags = iconController.getCategorizedTags(tags, node.getMap().getIconRegistry().getTagCategories());
	        for (CategorizedTag tag : categorizedTags) {
	            if (checkTag(tag, tagCategorySeparator))
	                return true;
	        }
	    }
	    else {
	        for (Tag tag : tags) {
	            if (checkTag(tag, ""))
	                return true;

	        }
	    }
	    return false;
	}

    protected boolean checkTag(Tag tag, String tagCategorySeparator) {
        final String tagContent = tag.getContent();
        return checkText(tagContent) || ! tagCategorySeparator.isEmpty()
                && tagContent.contains(tagCategorySeparator)
                && tag.categoryTags(tagCategorySeparator).stream()
                .map(Tag::getContent)
                .anyMatch(this::checkText);
    }

    @SuppressWarnings("unused")
    protected boolean checkTag(CategorizedTag categorizedTag, String tagCategorySeparatorForMap) {
        return categorizedTag.categoryTags().stream().anyMatch(tag -> checkText(tag.getContent()));
    }

    protected abstract boolean checkText(String content);

    @Override
    protected Object conditionValue() {
        return comparedValue;
    }

    public boolean searchesAcrossAllCategories() {
        return searchesAcrossAllCategories;
    }
}
