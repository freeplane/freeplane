package org.freeplane.core.ui.menubuilders;

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
		structureWithEntry.setComponent(component);
		final Entry child = new Entry();
		structureWithEntry.addChild(child);
		
		assertThat(child.getAncestorComponent(), equalTo(component));
	}
	
	@Test 
	public void returnsNullIfAncestorComponentIsNotAvailable(){
		Entry structureWithEntry = new Entry();
		final Entry child = new Entry();
		structureWithEntry.addChild(child);
		
		assertThat(child.getAncestorComponent(), CoreMatchers.<Object>nullValue());
	}
	
	@Test 
	public void findsAncestorComponent(){
		Object component = mock(Object.class);
		Entry structureWithEntry = new Entry();
		structureWithEntry.setComponent(component);
		final Entry level1child = new Entry();
		structureWithEntry.addChild(level1child);
		
		final Entry level2child = new Entry();
		level1child.addChild(level2child);

		assertThat(level2child.getAncestorComponent(), equalTo(component));
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
	public void getsChildrenWithNoIndices() {
		Entry top = new Entry();
		top.setName("top");
		
		assertThat(top.getChild(), equalTo(top));
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
}
