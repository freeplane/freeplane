package org.freeplane.core.ui.menubuilders;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.junit.Test;

public class XmlEntryStructureBuilderTest {
	@Test
	public void givenXmlWithourContent_createsEmptyStructure() {
		String xmlWithoutContent = "<FreeplaneUIEntries/>";

		Entry builtMenuStructure = XmlEntryStructureBuilder.buildMenuStructure(xmlWithoutContent);
		
		Entry menuStructureWithChildEntry = new Entry();
		assertThat(builtMenuStructure, equalTo(menuStructureWithChildEntry));
	}

	@Test
	public void givenXmlWithChildEntryWithBuilderSpecificAttribute_createsStructureWithChildEntry() {
		String xmlWithoutContent = "<FreeplaneUIEntries><Entry builderSpecificAttribute='Value'/></FreeplaneUIEntries>";

		Entry builtMenuStructure = XmlEntryStructureBuilder.buildMenuStructure(xmlWithoutContent);

		Entry menuStructureWithChildEntry = new Entry();
		final Entry childEntry = new Entry();
		childEntry.setAttribute("builderSpecificAttribute", "Value");
		menuStructureWithChildEntry.addChild(childEntry);

		assertThat(builtMenuStructure, equalTo(menuStructureWithChildEntry));
	}

	@Test
	public void givenXmlWithChildEntryWithOneBuilder_createsStructureWithChildEntry() {
		String xmlWithoutContent = "<FreeplaneUIEntries><Entry builder='builder'/></FreeplaneUIEntries>";

		Entry builtMenuStructure = XmlEntryStructureBuilder.buildMenuStructure(xmlWithoutContent);

		Entry menuStructureWithChildEntry = new Entry();
		final Entry childEntry = new Entry();
		childEntry.setBuilders(asList("builder"));
		menuStructureWithChildEntry.addChild(childEntry);

		assertThat(builtMenuStructure, equalTo(menuStructureWithChildEntry));
	}
	
	@Test
	public void givenXmlWithChildEntryWithTwoBuilders_createsStructureWithChildEntry() {
		String xmlWithoutContent = "<FreeplaneUIEntries><Entry builder='builder1, builder2'/></FreeplaneUIEntries>";

		Entry builtMenuStructure = XmlEntryStructureBuilder.buildMenuStructure(xmlWithoutContent);

		Entry menuStructureWithChildEntry = new Entry();
		final Entry childEntry = new Entry();
		childEntry.setBuilders(asList("builder1", "builder2"));
		menuStructureWithChildEntry.addChild(childEntry);

		assertThat(builtMenuStructure, equalTo(menuStructureWithChildEntry));
	}

	@Test
	public void givenXmlWithChildEntryWithName_createsStructureWithNamedChildEntry() {
		String xmlWithoutContent = "<FreeplaneUIEntries><Entry name='entry'/></FreeplaneUIEntries>";

		Entry builtMenuStructure = XmlEntryStructureBuilder.buildMenuStructure(xmlWithoutContent);

		Entry menuStructureWithChildEntry = new Entry();
		final Entry childEntry = new Entry();
		childEntry.setName("entry");
		menuStructureWithChildEntry.addChild(childEntry);

		assertThat(builtMenuStructure, equalTo(menuStructureWithChildEntry));
	}
	
	@Test
	public void givenXmlWithDifferentChildLevels_createsStructure() {
		String xmlWithoutContent = "<FreeplaneUIEntries><Entry name='level1'>"
				+ "<Entry name='level2'/>"
				+ "</Entry></FreeplaneUIEntries>";

		Entry builtMenuStructure = XmlEntryStructureBuilder.buildMenuStructure(xmlWithoutContent);

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
		String xmlWithoutContent = "<FreeplaneUIEntries><Entry name='level1'/>"
				+ "<Entry name='level2'/>"
				+ "</FreeplaneUIEntries>";

		Entry builtMenuStructure = XmlEntryStructureBuilder.buildMenuStructure(xmlWithoutContent);

		Entry menuStructureWithChildEntry = new Entry();
		final Entry childEntry = new Entry();
		childEntry.setName("level1");
		menuStructureWithChildEntry.addChild(childEntry);

		final Entry child2Entry = new Entry();
		child2Entry.setName("level2");
		menuStructureWithChildEntry.addChild(child2Entry);

		assertThat(builtMenuStructure, equalTo(menuStructureWithChildEntry));
	}
	
	@Test
	public void givenXmlWithChildEntryWithFalse_createsBooleanObject() {
		String xmlWithoutContent = "<FreeplaneUIEntries><Entry builderSpecificAttribute='FAlse'/></FreeplaneUIEntries>";

		Entry builtMenuStructure = XmlEntryStructureBuilder.buildMenuStructure(xmlWithoutContent);

		Entry menuStructureWithChildEntry = new Entry();
		final Entry childEntry = new Entry();
		childEntry.setAttribute("builderSpecificAttribute", false);
		menuStructureWithChildEntry.addChild(childEntry);

		assertThat(builtMenuStructure, equalTo(menuStructureWithChildEntry));
	}

	@Test
	public void givenXmlWithChildEntryWithTrue_createsBooleanObject() {
		String xmlWithoutContent = "<FreeplaneUIEntries><Entry builderSpecificAttribute='true'/></FreeplaneUIEntries>";

		Entry builtMenuStructure = XmlEntryStructureBuilder.buildMenuStructure(xmlWithoutContent);

		Entry menuStructureWithChildEntry = new Entry();
		final Entry childEntry = new Entry();
		childEntry.setAttribute("builderSpecificAttribute", true);
		menuStructureWithChildEntry.addChild(childEntry);

		assertThat(builtMenuStructure, equalTo(menuStructureWithChildEntry));
	}


}
