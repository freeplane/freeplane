package org.freeplane.core.ui.menubuilders.generic;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import java.util.Arrays;

import org.hamcrest.CoreMatchers;
import org.junit.Test;

public class EntryTest {

	@Test
	public void equalEntriesWithChild() {
		Entry firstStructureWithEntry = new Entry();
		final Entry firstEntry = new Entry();
		firstStructureWithEntry.addChild(firstEntry);

		Entry otherStructureWithEntry = new Entry();
		final Entry otherEntry = new Entry();
		otherStructureWithEntry.addChild(otherEntry);
		
		assertThat(firstStructureWithEntry, equalTo(otherStructureWithEntry));
	}

	@Test
	public void equalEntriesWithDifferentBuilders() {
		Entry firstStructureWithEntry = new Entry();
		firstStructureWithEntry.setBuilders(Arrays.asList("builder"));

		Entry otherStructureWithEntry = new Entry();
		assertThat(firstStructureWithEntry, CoreMatchers.not(otherStructureWithEntry));
	}

	@Test
	public void knowsParent() {
		Entry structureWithEntry = new Entry();
		final Entry child = new Entry();
		structureWithEntry.addChild(child);
		
		assertThat(child.getParent(), equalTo(structureWithEntry));
	}


	@Test
	public void selfIsRoot() {
		final Entry entry = new Entry();
		assertThat(entry.getRoot(), equalTo(entry));
	}

	@Test
	public void parentIsRoot() {
		Entry structureWithEntry = new Entry();
		final Entry child = new Entry();
		structureWithEntry.addChild(child);
		assertThat(child.getRoot(), equalTo(structureWithEntry));
	}

	@Test
	public void parentIsNotRoot() {
		Entry root = new Entry();
		final Entry parent = new Entry();
		root.addChild(parent);
		final Entry child = new Entry();
		parent.addChild(child);
		assertThat(child.getRoot(), equalTo(root));
	}

	@Test
	public void rootEntryPathIsSlashifiedName() {
		Entry entry = new Entry();
		entry.setName("name");
		assertThat(entry.getPath(), equalTo("/name"));
	}


	@Test
	public void childEntryPathIsSlashifiedNameAfterParentName() {
		Entry entry = new Entry();
		entry.setName("parent");
		Entry child = new Entry();
		child.setName("child");
		entry.addChild(child);
		assertThat(child.getPath(), equalTo("/parent/child"));
	}

	@Test
	public void iteratesOverSingleChild() {
		Entry firstStructureWithEntry = new Entry();
		final Entry firstEntry = new Entry();
		firstStructureWithEntry.addChild(firstEntry);
		Entry child = null;
		for(Entry childInLoop : firstStructureWithEntry.children())
			child = childInLoop;
		assertThat(firstEntry, equalTo(child));
	}
	
	@Test 
	public void findsParentComponent(){
		Object component = mock(Object.class);
		Entry structureWithEntry = new Entry();
		new EntryAccessor().setComponent(structureWithEntry, component);
		final Entry child = new Entry();
		structureWithEntry.addChild(child);
		
		assertThat(new EntryAccessor().getAncestorComponent(child), equalTo(component));
	}
	
	@Test 
	public void returnsNullIfAncestorComponentIsNotAvailable(){
		Entry structureWithEntry = new Entry();
		final Entry child = new Entry();
		structureWithEntry.addChild(child);
		
		assertThat(new EntryAccessor().getAncestorComponent(child), CoreMatchers.<Object>nullValue());
	}
	
	@Test 
	public void findsAncestorComponent(){
		Object component = mock(Object.class);
		Entry structureWithEntry = new Entry();
		new EntryAccessor().setComponent(structureWithEntry, component);
		final Entry level1child = new Entry();
		structureWithEntry.addChild(level1child);
		
		final Entry level2child = new Entry();
		level1child.addChild(level2child);

		assertThat(new EntryAccessor().getAncestorComponent(level2child), equalTo(component));
	}

	@Test 
	public void removesChildEntries(){
		Entry structureWithEntry = new Entry();
		final Entry child = new Entry();
		structureWithEntry.addChild(child);
		structureWithEntry.removeChildren();
		
		assertThat(structureWithEntry, is(new Entry()));
	}
	
	@Test(expected=AttributeAlreadySetException.class)
	public void cannotSetDifferentAttributeValue() {
		Entry entry = new Entry();
		entry.setAttribute("key", "value");
		entry.setAttribute("key", "value2");
	}

	@Test
	public void canRemoveAttribute() {
		Entry entry = new Entry();
		entry.setAttribute("key", "value1");
		entry.removeAttribute("key");
		entry.setAttribute("key", "value2");
		assertThat(entry.getAttribute("key"), CoreMatchers.<Object>equalTo("value2"));
	}

	@Test
	public void hasChildren() {
		Entry firstStructureWithEntry = new Entry();
		final Entry firstEntry = new Entry();
		firstStructureWithEntry.addChild(firstEntry);
		
		assertThat(firstStructureWithEntry.hasChildren(), equalTo(true));
	}


	@Test
	public void isLeaf() {
		Entry firstStructureWithEntry = new Entry();
		assertThat(firstStructureWithEntry.isLeaf(), equalTo(true));
	}

	@Test
	public void getsChildrenWithOneIndex() {
		Entry top = new Entry();
		Entry middle = new Entry();
		top.addChild(middle);
		middle.setName("middle");
		
		assertThat(top.getChild(0), equalTo(middle));
	}

	@Test
	public void getsChildrenWithTwoIndices() {
		Entry top = new Entry();
		Entry middle = new Entry();
		top.addChild(middle);
		Entry down = new Entry();
		middle.addChild(down);
		down.setName("down");
		
		assertThat(top.getChild(0, 0), equalTo(down));
	}

	@Test
	public void returnsChildWithName() throws Exception {
		Entry top = new Entry();
		Entry middle = new Entry();
		top.addChild(middle);
		middle.setName("name");
		assertThat(top.getChild("name"), equalTo(middle));
	}

	@Test
	public void returnsChildWithNameDeeply() throws Exception {
		Entry top = new Entry();
		Entry middle = new Entry();
		top.addChild(middle);
		Entry down = new Entry();
		middle.addChild(down);
		down.setName("name");
		assertThat(top.getChild("name"), equalTo(down));
	}

	@Test
	public void getsChildrenWithTwoNames() {
		Entry top = new Entry();
		Entry middle = new Entry();
		top.addChild(middle);
		middle.setName("middle");
		Entry down = new Entry();
		middle.addChild(down);
		down.setName("down");
		assertThat(top.getChildByPath("middle", "down"), equalTo(down));
	}

	@Test
	public void dhildrenWithTwoNamesNotFound() {
		Entry top = new Entry();
		Entry middle = new Entry();
		top.addChild(middle);
		Entry down = new Entry();
		middle.addChild(down);
		down.setName("down");
		assertThat(top.getChildByPath("middle", "down"), equalTo(null));
	}
}
