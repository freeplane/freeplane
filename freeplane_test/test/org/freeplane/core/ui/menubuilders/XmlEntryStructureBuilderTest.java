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

public class XmlEntryStructureBuilderTest {
	private Entry buildMenuStructure(String xmlWithoutContent) {
		XmlEntryStructureBuilder builder = new XmlEntryStructureBuilder(new StringReader(xmlWithoutContent));
		Entry initialMenuStructure = new Entry();
		builder.build(initialMenuStructure);
		return initialMenuStructure;
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
		final Entry childEntry = new Entry();
		childEntry.setAttribute("builderSpecificAttribute", "Value");
		menuStructureWithChildEntry.addChild(childEntry);

		assertThat(builtMenuStructure, equalTo(menuStructureWithChildEntry));
	}

	@Test
	public void givenXmlWithChildEntryWithOneBuilder_createsStructureWithChildEntry() {
		String xmlWithoutContent = "<freeplaneUIEntries><entry builder='builder'/></freeplaneUIEntries>";

		Entry builtMenuStructure = buildMenuStructure(xmlWithoutContent);

		Entry menuStructureWithChildEntry = new Entry();
		final Entry childEntry = new Entry();
		childEntry.setBuilders(asList("builder"));
		menuStructureWithChildEntry.addChild(childEntry);

		assertThat(builtMenuStructure, equalTo(menuStructureWithChildEntry));
	}
	
	@Test
	public void givenXmlWithChildEntryWithTwoBuilders_createsStructureWithChildEntry() {
		String xmlWithoutContent = "<freeplaneUIEntries><entry builder='builder1, builder2'/></freeplaneUIEntries>";

		Entry builtMenuStructure = buildMenuStructure(xmlWithoutContent);

		Entry menuStructureWithChildEntry = new Entry();
		final Entry childEntry = new Entry();
		childEntry.setBuilders(asList("builder1", "builder2"));
		menuStructureWithChildEntry.addChild(childEntry);

		assertThat(builtMenuStructure, equalTo(menuStructureWithChildEntry));
	}

	@Test
	public void givenXmlWithChildEntryWithName_createsStructureWithNamedChildEntry() {
		String xmlWithoutContent = "<freeplaneUIEntries><entry name='entry'/></freeplaneUIEntries>";

		Entry builtMenuStructure = buildMenuStructure(xmlWithoutContent);

		Entry menuStructureWithChildEntry = new Entry();
		final Entry childEntry = new Entry();
		childEntry.setName("entry");
		menuStructureWithChildEntry.addChild(childEntry);

		assertThat(builtMenuStructure, equalTo(menuStructureWithChildEntry));
	}
	
	@Test
	public void givenXmlWithDifferentChildLevels_createsStructure() {
		String xmlWithoutContent = "<freeplaneUIEntries><entry name='level1'>"
				+ "<entry name='level2'/>"
				+ "</entry></freeplaneUIEntries>";

		Entry builtMenuStructure = buildMenuStructure(xmlWithoutContent);

		Entry menuStructureWithChildEntry = new Entry();
		final Entry childEntry = new Entry();
		childEntry.setName("level1");
		menuStructureWithChildEntry.addChild(childEntry);

		final Entry child2Entry = new Entry();
		child2Entry.setName("level2");
		childEntry.addChild(child2Entry);

		assertThat(builtMenuStructure, equalTo(menuStructureWithChildEntry));
	}

	@Test
	public void givenXmlWithSameChildLevels_createsStructure() {
		String xmlWithoutContent = "<freeplaneUIEntries><entry name='level1'/>"
				+ "<entry name='level2'/>"
				+ "</freeplaneUIEntries>";

		Entry builtMenuStructure = buildMenuStructure(xmlWithoutContent);

		Entry menuStructureWithChildEntry = new Entry();
		final Entry childEntry = new Entry();
		childEntry.setName("level1");
		menuStructureWithChildEntry.addChild(childEntry);

		final Entry child2Entry = new Entry();
		child2Entry.setName("level2");
		menuStructureWithChildEntry.addChild(child2Entry);

		assertThat(builtMenuStructure, equalTo(menuStructureWithChildEntry));
	}
}
