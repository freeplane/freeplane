package org.freeplane.core.ui.menubuilders;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.StringReader;
import java.util.Arrays;

import org.hamcrest.CoreMatchers;
import org.hamcrest.core.IsNull;
import org.junit.Ignore;
import org.junit.Test;

public class EntryStructureBuilderTest {
	private Entry buildMenuStructure(String xmlWithoutContent) {
		EntryStructureBuilder builder = new EntryStructureBuilder(new StringReader(xmlWithoutContent));
		Entry initialMenuStructure = new Entry();
		Entry builtMenuStructure = builder.build(initialMenuStructure);
		return builtMenuStructure;
	}

	@Test
	public void givenXmlWithourContent_createsEmptyStructure() {
		String xmlWithoutContent = "<freeplaneUIEntries/>";

		Entry builtMenuStructure = buildMenuStructure(xmlWithoutContent);
		
		Entry menuStructureWithChildEntry = new Entry();
		assertThat(builtMenuStructure, equalTo(menuStructureWithChildEntry));
	}

	@Test
	public void givenXmlWithChildEntryWithBuilderSpecificAttribute_createsStructureWithChildEntry() {
		String xmlWithoutContent = "<freeplaneUIEntries><entry builderSpecificAttribute='Value'/></freeplaneUIEntries>";

		Entry builtMenuStructure = buildMenuStructure(xmlWithoutContent);

		Entry menuStructureWithChildEntry = new Entry();
		final Entry homeEntry = new Entry();
		homeEntry.setAttribute("builderSpecificAttribute", "Value");
		menuStructureWithChildEntry.addChild(homeEntry);

		assertThat(builtMenuStructure, equalTo(menuStructureWithChildEntry));
	}

	@Test
	public void givenXmlWithChildEntryWithOneBuilder_createsStructureWithChildEntry() {
		String xmlWithoutContent = "<freeplaneUIEntries><entry builder='builder'/></freeplaneUIEntries>";

		Entry builtMenuStructure = buildMenuStructure(xmlWithoutContent);

		Entry menuStructureWithChildEntry = new Entry();
		final Entry homeEntry = new Entry();
		homeEntry.setBuilders(asList("builder"));
		menuStructureWithChildEntry.addChild(homeEntry);

		assertThat(builtMenuStructure, equalTo(menuStructureWithChildEntry));
	}
	
	@Test
	public void givenXmlWithChildEntryWithTwoBuilders_createsStructureWithChildEntry() {
		String xmlWithoutContent = "<freeplaneUIEntries><entry builder='builder1, builder2'/></freeplaneUIEntries>";

		Entry builtMenuStructure = buildMenuStructure(xmlWithoutContent);

		Entry menuStructureWithChildEntry = new Entry();
		final Entry homeEntry = new Entry();
		homeEntry.setBuilders(asList("builder1", "builder2"));
		menuStructureWithChildEntry.addChild(homeEntry);

		assertThat(builtMenuStructure, equalTo(menuStructureWithChildEntry));
	}
}
