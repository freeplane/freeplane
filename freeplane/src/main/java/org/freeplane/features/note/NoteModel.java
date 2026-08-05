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
package org.freeplane.features.note;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.freeplane.core.extension.IExtension;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.text.RichTextModel;

/**
 * @author Dimitry Polivaev
 */
public class NoteModel extends RichTextModel implements IExtension {
	public static final String EDITING_PURPOSE = "Note";
	public static final String DEFAULT_TAB_NAME = "note1";

	public static class Tab extends RichTextModel {
		private String name;

		public Tab(String name) {
			this.name = name;
		}

		public Tab(String name, String contentType, String text, String xml) {
			super(contentType, text, xml);
			this.name = name;
		}

		public String getName() { return name; }
		public void setName(String name) { this.name = name; }
		private Tab copy() { return new Tab(name, getContentType(), getText(), getXml()); }
	}

	private List<Tab> tabs;

	public static NoteModel createNote(final NodeModel node) {
		NoteModel note = NoteModel.getNote(node);
		if (note == null) {
			note = new NoteModel();
			node.addExtension(note);
		}
		return note;
	}

	public static NoteModel getNote(final NodeModel node) {
		final NoteModel extension = node.getExtension(NoteModel.class);
		return extension;
	}

	public static String getNoteText(final NodeModel node) {
		final NoteModel extension = NoteModel.getNote(node);
		return extension != null ? extension.getText() : null;
	}

    public static String getNoteContentType(final NodeModel node) {
        final NoteModel extension = NoteModel.getNote(node);
        return extension != null ? extension.getContentType() : null;
    }

    public NoteModel() {
    }
    
    public NoteModel(String contentType, String text, String xml) {
        super(contentType, text, xml);
    }

	public List<Tab> getTabs() {
		return tabs == null ? Collections.emptyList() : Collections.unmodifiableList(tabs);
	}

	public boolean hasTabs() { return tabs != null; }

	@Override
	public String getText() {
		return tabs == null ? super.getText() : tabs.isEmpty() ? null : tabs.get(0).getText();
	}

	@Override
	public String getXml() {
		return tabs == null ? super.getXml() : tabs.isEmpty() ? null : tabs.get(0).getXml();
	}

	@Override
	public String getContentType() {
		return tabs == null ? super.getContentType() : tabs.isEmpty() ? null : tabs.get(0).getContentType();
	}

	@Override
	public void setContentType(String format) {
		if (tabs == null) super.setContentType(format);
		else if (!tabs.isEmpty()) tabs.get(0).setContentType(format);
	}

	public Tab getTab(String name) {
		if (tabs == null)
			return DEFAULT_TAB_NAME.equals(name) ? new Tab(DEFAULT_TAB_NAME, getContentType(), getText(), getXml()) : null;
		for (Tab tab : tabs)
			if (tab.getName().equals(name)) return tab;
		return null;
	}

	public Tab addTab(String name) {
		ensureTabs();
		Tab tab = new Tab(name);
		tabs.add(tab);
		return tab;
	}

	public void ensureTabs() {
		if (tabs == null) {
			tabs = new ArrayList<>();
			tabs.add(new Tab(DEFAULT_TAB_NAME, getContentType(), getText(), getXml()));
		}
	}

	public void removeTab(String name) {
		if (tabs != null) tabs.removeIf(tab -> tab.getName().equals(name));
	}
    
    public NoteModel copy() {
		NoteModel copy = new NoteModel(getContentType(), getText(), getXml());
		if (tabs != null) {
			copy.tabs = new ArrayList<>();
			for (Tab tab : tabs) copy.tabs.add(tab.copy());
		}
		return copy;
    }

	@Override
	public boolean isEmpty() {
		if (tabs != null) return tabs.isEmpty();
		return super.isEmpty();
	}
}
