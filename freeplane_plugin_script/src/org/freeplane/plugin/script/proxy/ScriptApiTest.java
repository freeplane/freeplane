package org.freeplane.plugin.script.proxy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.freeplane.core.util.LogUtils;
import org.freeplane.features.common.icon.factory.MindIconFactory;
import org.freeplane.features.common.text.NodeContainsCondition;
import org.freeplane.plugin.script.proxy.Proxy.Map;
import org.freeplane.plugin.script.proxy.Proxy.Node;

public class ScriptApiTest {
	ControllerProxy c;
	NodeProxy node;
	private Map map;
	private Comparator<NodeProxy> nodeComparator = new Comparator<NodeProxy>() {
		public int compare(NodeProxy o1, NodeProxy o2) {
			return new Integer(System.identityHashCode(o1.getDelegate())).compareTo(System.identityHashCode(o2
			    .getDelegate()));
		}
	};

	@SuppressWarnings("serial")
	public static class TestException extends RuntimeException {
		public TestException(String message, Throwable cause) {
			super(message, cause);
		}

		public TestException(String message) {
			super(message);
		}
	}

	public ScriptApiTest(ControllerProxy c, NodeProxy node) {
		this.c = c;
		this.node = node;
	}

	public static void runAll(ControllerProxy c, NodeProxy node) {
		int failures = 0;
		int errors = 0;
		int pass = 0;
		Method tearDown = null;
		try {
			tearDown = ScriptApiTest.class.getMethod("tearDown");
		}
		catch (Exception e) {
			// ignore
		}
		for (Method method : ScriptApiTest.class.getMethods()) {
			if (!method.getName().startsWith("test"))
				continue;
			// new fixture
			final ScriptApiTest instance = new ScriptApiTest(c, node);
			try {
				method.invoke(instance);
				LogUtils.info(method.getName() + ": pass");
				pass++;
			}
			catch (InvocationTargetException e) {
				if (e.getCause() instanceof TestException) {
					LogUtils.warn(method.getName() + ": failure: " + e.getCause().getMessage(), e.getCause());
					failures++;
				}
				else {
					LogUtils.warn(method.getName() + ": error invoking test: " + e.getCause().getMessage(), e
					    .getCause());
					errors++;
				}
			}
			catch (Throwable e) {
				LogUtils.warn(method.getName() + ": other error: " + e.getMessage(), e);
				errors++;
			}
			finally {
				try {
					if (tearDown != null)
						tearDown.invoke(instance);
				}
				catch (Throwable e) {
					LogUtils.warn("failure executing tearDown after " + method.getName(), e);
				}
			}
		}
		if (errors + failures == 0)
			LogUtils.info("success! " + pass + " tests passed");
		else
			LogUtils.severe("error! test result: " + errors + " errors, " + failures + " failures; " + pass
			        + " tests passed");
	}

	public void tearDown() {
		if (map != null)
			map.close(true, false);
	}

	@SuppressWarnings("deprecation")
	public void test_AttributesRO_get_String_name() {
		map = setupMapWithSomeAttributes();
		assertEquals("first value should be found", "va1", map.getRootNode().getAttributes().get("a1"));
		assertEquals("should return null for non-existing attributes", null, map.getRootNode().getAttributes().get("x"));
	}

	private Map setupMapWithSomeAttributes() {
		map = createTestMap();
		map.getRootNode().getAttributes().add("a1", "va1");
		map.getRootNode().getAttributes().add("a1", "va2");
		map.getRootNode().getAttributes().add("a1", "va3");
		map.getRootNode().getAttributes().add("b1", "vb1");
		return map;
	}

	private void assertEquals(String message, Object expected, Object actual) {
		message = (message == null || message.length() == 0) ? "Failure" : message;
		boolean isEqual = expected == null && actual == null || expected != null && actual != null
		        && expected.equals(actual);
		if (!isEqual) {
			fail(message + ". Expected: " + expected + ", but was: " + actual);
		}
	}

	private void assertTrue(String message, boolean test) {
		if (!test)
			fail(message);
	}

	private void assertFalse(String message, boolean test) {
		if (test)
			fail(message);
	}

	private void fail(final String m) {
		throw new TestException(m);
	}

	private Map createTestMap() {
		map = c.newMap();
		addChild(map.getRootNode(), "first node");
		addChild(map.getRootNode(), "second node");
		return map;
	}

	private Node addChild(Node aNode, String text) {
		final Node child = aNode.createChild();
		child.setText(text);
		return child;
	}

	private Node firstChild(final Node node) {
		return node.getChildren().get(0);
	}

	public void test_AttributesRO_getAll_String_name() {
		map = setupMapWithSomeAttributes();
		assertEquals("all values should be found", list("va1", "va2", "va3"), map.getRootNode().getAttributes().getAll(
		    "a1"));
		assertEquals("all values should be found", Collections.singletonList("vb1"), map.getRootNode().getAttributes()
		    .getAll("b1"));
		assertEquals("should return empty list for non-existing attributes", Collections.EMPTY_LIST, map.getRootNode()
		    .getAttributes().getAll("x"));
	}

	public void test_AttributesRO_getAttributeNames() {
		map = setupMapWithSomeAttributes();
		assertEquals("all names should be found", list("a1", "a1", "a1", "b1"), map.getRootNode().getAttributes()
		    .getAttributeNames());
	}

	public void test_AttributesRO_get_int_index() {
		map = setupMapWithSomeAttributes();
		assertEquals("find by index", "va1", map.getRootNode().getAttributes().get(0));
		assertEquals("find by index", "va2", map.getRootNode().getAttributes().get(1));
		assertEquals("find by index", "va3", map.getRootNode().getAttributes().get(2));
		assertEquals("find by index", "vb1", map.getRootNode().getAttributes().get(3));
		try {
			map.getRootNode().getAttributes().get(4);
			fail("expect IndexOutOfBoundsException on get(int) with illegal index");
		}
		catch (IndexOutOfBoundsException e) {
			// OK - expected
		}
	}

	public void test_AttributesRO_findAttribute_String_name() {
		map = setupMapWithSomeAttributes();
		assertEquals("first matching attribute should be found", 0, map.getRootNode().getAttributes().findAttribute(
		    "a1"));
	}

	public void test_AttributesRO_size() {
		map = setupMapWithSomeAttributes();
		assertEquals("count attributes", 4, map.getRootNode().getAttributes().size());
	}

	public void test_Attributes_set_int_index_String_value() {
		map = setupMapWithSomeAttributes();
		map.getRootNode().getAttributes().set(3, "VB1");
		map.getRootNode().getAttributes().set(1, "VA2");
		assertEquals("values should be updated", list("va1", "VA2", "va3"), map.getRootNode().getAttributes().getAll(
		    "a1"));
		assertEquals("values should be updated", list("VB1"), map.getRootNode().getAttributes().getAll("b1"));
		try {
			map.getRootNode().getAttributes().set(4, "xx");
			fail("expect IndexOutOfBoundsException on set(int, value) with illegal index");
		}
		catch (IndexOutOfBoundsException e) {
			// OK - expected
		}
	}

	public void test_Attributes_set_int_index_String_name_String_value() {
		map = setupMapWithSomeAttributes();
		map.getRootNode().getAttributes().set(1, "c1", "VC1");
		assertEquals("values should be updated", list("va1", "va3"), map.getRootNode().getAttributes().getAll("a1"));
		assertEquals("values should be updated", 1, map.getRootNode().getAttributes().findAttribute("c1"));
		assertEquals("values should be updated", "VC1", map.getRootNode().getAttributes().get(1));
		try {
			map.getRootNode().getAttributes().set(4, "xx", "yy");
			fail("expect IndexOutOfBoundsException on set(int, name, value) with illegal index");
		}
		catch (IndexOutOfBoundsException e) {
			// OK - expected
		}
	}

	@SuppressWarnings("deprecation")
	public void test_Attributes_remove_String_name() {
		map = setupMapWithSomeAttributes();
		map.getRootNode().getAttributes().remove("a1");
		assertEquals("first matching attribute should be removed", list("va2", "va3"), map.getRootNode()
		    .getAttributes().getAll("a1"));
		assertEquals("first matching attribute should be removed", 3, map.getRootNode().getAttributes().size());
	}

	private <T> List<T> list(T... args) {
		return Arrays.asList(args);
	}

	private <T> Set<T> set(T... args) {
		return set(Arrays.asList(args));
	}

	@SuppressWarnings("unchecked")
	private <T> Set<T> set(List<T> list) {
		TreeSet<T> set = null;
		if (list.size() > 0 && list.get(0) instanceof Node)
			set = new TreeSet<T>((Comparator<T>) nodeComparator);
		else
			set = new TreeSet<T>();
		set.addAll(list);
		return set;
	}

	public void test_Attributes_removeAll_String_name() {
		map = setupMapWithSomeAttributes();
		map.getRootNode().getAttributes().removeAll("a1");
		assertEquals("all matching attributes should be removed", list(), map.getRootNode().getAttributes()
		    .getAll("a1"));
		assertEquals("all matching attribute should be removed", 1, map.getRootNode().getAttributes().size());
	}

	public void test_Attributes_remove_int_index() {
		map = setupMapWithSomeAttributes();
		// remove from the rear to keep the index stable (otherwise remove(1) twice)
		map.getRootNode().getAttributes().remove(2);
		map.getRootNode().getAttributes().remove(1);
		assertEquals("attribute should be removed", list("va1"), map.getRootNode().getAttributes().getAll("a1"));
		assertEquals("attribute should be removed", 2, map.getRootNode().getAttributes().size());
	}

	public void test_Attributes_set_String_name_String_value() {
		map = setupMapWithSomeAttributes();
		map.getRootNode().getAttributes().set("a1", "VA1");
		map.getRootNode().getAttributes().set("c1", "vc1");
		assertEquals("first matching attribute should be changed", list("VA1", "va2", "va3"), map.getRootNode()
		    .getAttributes().getAll("a1"));
		assertEquals("attribute should be added", list("vc1"), map.getRootNode().getAttributes().getAll("c1"));
		assertEquals("attribute should be added", 5, map.getRootNode().getAttributes().size());
	}

	public void test_Attributes_add_String_name_String_value() {
		map = setupMapWithSomeAttributes();
		map.getRootNode().getAttributes().add("a1", "va1");
		map.getRootNode().getAttributes().set("c1", "vc1");
		assertEquals("attribute should be added", list("va1", "va2", "va3", "va1"), map.getRootNode().getAttributes()
		    .getAll("a1"));
		assertEquals("attribute should be added", list("vc1"), map.getRootNode().getAttributes().getAll("c1"));
		assertEquals("attribute should be added", 6, map.getRootNode().getAttributes().size());
	}

	//
	//	public void test_ConnectorRO_getColor() {
	//		// TODO
	//	}
	//
	//	public void test_ConnectorRO_getEndArrow() {
	//		// TODO
	//	}
	//
	//	public void test_ConnectorRO_getMiddleLabel() {
	//		// TODO
	//	}
	//
	//	public void test_ConnectorRO_getSource() {
	//		// TODO
	//	}
	//
	//	public void test_ConnectorRO_getSourceLabel() {
	//		// TODO
	//	}
	//
	//	public void test_ConnectorRO_getStartArrow() {
	//		// TODO
	//	}
	//
	//	public void test_ConnectorRO_getTarget() {
	//		// TODO
	//	}
	//
	//	public void test_ConnectorRO_getTargetLabel() {
	//		// TODO
	//	}
	//
	//	public void test_ConnectorRO_simulatesEdge() {
	//		// TODO
	//	}
	//
	//	public void test_Connector_setColor_Color_color() {
	//		// TODO
	//	}
	//
	//	public void test_Connector_setEndArrow_ArrowType_arrowType() {
	//		// TODO
	//	}
	//
	//	public void test_Connector_setMiddleLabel_String_label() {
	//		// TODO
	//	}
	//
	//	public void test_Connector_setSimulatesEdge_boolean_simulatesEdge() {
	//		// TODO
	//	}
	//
	//	public void test_Connector_setSourceLabel_String_label() {
	//		// TODO
	//	}
	//
	//	public void test_Connector_setStartArrow_ArrowType_arrowType() {
	//		// TODO
	//	}
	//
	//	public void test_Connector_setTargetLabel_String_label() {
	//		// TODO
	//	}
	public void test_ControllerRO_getSelected() {
		map = c.newMap();
		assertEquals("new root node should be selected", map.getRootNode(), c.getSelected());
		final Node firstChild = addChild(map.getRootNode(), "child 1");
		final Node secondChild = addChild(map.getRootNode(), "child 2");
		// FIXME: why aren't the new node selected?
		assertEquals("root node should still be selected after adding nodes", map.getRootNode(), c.getSelected());
		c.selectMultipleNodes(list(firstChild, secondChild));
		// what's the rule?
		assertEquals("last selected node should be returned", secondChild, c.getSelected());
	}

	public void test_ControllerRO_getSelecteds() {
		map = c.newMap();
		assertEquals("new root node should be selected", map.getRootNode(), c.getSelected());
		final Node firstChild = addChild(map.getRootNode(), "child 1");
		final Node secondChild = addChild(map.getRootNode(), "child 2");
		assertEquals("root node should still be selected after adding nodes", set(map.getRootNode()), set(c
		    .getSelecteds()));
		c.selectMultipleNodes(list(firstChild, secondChild));
		assertEquals("only the nodes selected via selectMultipleNodes should be returned",
		    set(firstChild, secondChild), set(c.getSelecteds()));
	}

	public void test_ControllerRO_getSortedSelection_boolean_differentSubtrees() {
		// TODO
	}

	public void test_ControllerRO_find_ICondition_condition() {
		map = c.newMap();
		@SuppressWarnings("unused")
		final Node firstChild = addChild(map.getRootNode(), "child 1");
		final Node secondChild = addChild(map.getRootNode(), "child 2");
		final List<Node> found = c.find(new NodeContainsCondition("child 2"));
		assertEquals("one matching node should be found", list(secondChild), found);
	}

	public void test_ControllerRO_find_Closure_closure() {
		// TODO
	}

	public void test_Controller_centerOnNode_Node_center() {
		map = c.newMap();
		final Node firstChild = addChild(map.getRootNode(), "child 1");
		// no actual test
		c.centerOnNode(firstChild);
	}

	public void test_Controller_select_Node_toSelect() {
		map = c.newMap();
		final Node firstChild = addChild(map.getRootNode(), "child 1");
		final Node secondChild = addChild(map.getRootNode(), "child 2");
		final Node thirdChild = addChild(map.getRootNode(), "child 3");
		c.select(secondChild);
		final Set<Node> set = set(secondChild);
		final Set<Node> selected = set(c.getSelecteds());
		assertEquals("one node should be selected", set, selected);
		c.select(firstChild);
		assertEquals("one node should be selected", set(firstChild), set(c.getSelecteds()));
		c.select(thirdChild);
		assertEquals("one node should be selected", set(thirdChild), set(c.getSelecteds()));
	}

	public void test_Controller_selectBranch_Node_branchRoot() {
		map = c.newMap();
		final Node child1 = addChild(map.getRootNode(), "child 1");
		final Node child2 = addChild(map.getRootNode(), "child 2");
		final Node grandchild1 = addChild(child1, "child 1.1");
		final Node grandchild2 = addChild(child1, "child 1.2");
		final Node grandGrandChild = addChild(child1, "child 1.1.1");
		c.selectBranch(child1);
		assertEquals("all node of the branch should be selected",
		    set(child1, grandchild1, grandchild2, grandGrandChild), set(c.getSelecteds()));
		c.selectBranch(child2);
		assertEquals("one node should be selected", set(child2), set(c.getSelecteds()));
	}

	public void test_Controller_selectMultipleNodes_List_Node_toSelect() {
		// see test_ControllerRO_getSelected()
	}

	public void test_Controller_undo_redo_stuff() {
		map = c.newMap();
		addChild(map.getRootNode(), "child 1");
		assertFalse("node should be there before undo", c.find(new NodeContainsCondition("child 1")).isEmpty());
		c.undo();
		assertTrue("node should be away after undo", c.find(new NodeContainsCondition("child 1")).isEmpty());
		c.redo();
		assertFalse("node should be there after redo", c.find(new NodeContainsCondition("child 1")).isEmpty());
		c.deactivateUndo();
		c.undo();
		assertTrue("node should still be there after undo since undo is deactivated", c.find(
		    new NodeContainsCondition("child 1")).isEmpty());
	}

	public void test_Controller_setStatusInfo_String_info() {
		// no actual test
		c.setStatusInfo("test statusinfo");
		// no actual test - info should be removed
		c.setStatusInfo(null);
	}

	public void test_Controller_setStatusInfo_String_key_String_info() {
		// no actual test
		c.setStatusInfo("display_node_id", "someid");
		// no actual test - info should be removed
		c.setStatusInfo("display_node_id", (String) null);
	}

	public void test_Controller_setStatusInfo_String_key_Icon_icon() {
		// no actual test
		c.setStatusInfo("test statusinfo", MindIconFactory.create("user_icon").getIcon());
		// no actual test - info should be removed
		c.setStatusInfo(null);
	}

	public void test_Controller_newMap() {
		map = c.newMap();
	}

	//
	//	public void test_EdgeRO_getColor() {
	//		// TODO
	//	}
	//
	//	public void test_EdgeRO_getType() {
	//		// TODO
	//	}
	//
	//	public void test_EdgeRO_getWidth() {
	//		// TODO
	//	}
	//
	//	public void test_Edge_setColor_Color_color() {
	//		// TODO
	//	}
	//
	//	public void test_Edge_setType_EdgeStyle_type() {
	//		// TODO
	//	}
	//
	//	public void test_Edge_setWidth_int_width() {
	//		// TODO
	//	}
	//
	//	public void test_ExternalObjectRO_getURI() {
	//		// TODO
	//	}
	//
	//	public void test_ExternalObjectRO_getZoom() {
	//		// TODO
	//	}
	//
	//	public void test_ExternalObject_setURI_String_uri() {
	//		// TODO
	//	}
	//
	//	public void test_ExternalObject_setZoom_float_zoom() {
	//		// TODO
	//	}
	//
	//	public void test_FontRO_getName() {
	//		// TODO
	//	}
	//
	//	public void test_FontRO_getSize() {
	//		// TODO
	//	}
	//
	//	public void test_FontRO_isBold() {
	//		// TODO
	//	}
	//
	//	public void test_FontRO_isBoldSet() {
	//		// TODO
	//	}
	//
	//	public void test_FontRO_isItalic() {
	//		// TODO
	//	}
	//
	//	public void test_FontRO_isItalicSet() {
	//		// TODO
	//	}
	//
	//	public void test_FontRO_isNameSet() {
	//		// TODO
	//	}
	//
	//	public void test_FontRO_isSizeSet() {
	//		// TODO
	//	}
	//
	//	public void test_Font_resetBold() {
	//		// TODO
	//	}
	//
	//	public void test_Font_resetItalic() {
	//		// TODO
	//	}
	//
	//	public void test_Font_resetName() {
	//		// TODO
	//	}
	//
	//	public void test_Font_resetSize() {
	//		// TODO
	//	}
	//
	//	public void test_Font_setBold_boolean_bold() {
	//		// TODO
	//	}
	//
	//	public void test_Font_setItalic_boolean_italic() {
	//		// TODO
	//	}
	//
	//	public void test_Font_setName_String_name() {
	//		// TODO
	//	}
	//
	//	public void test_Font_setSize_int_size() {
	//		// TODO
	//	}
	//
	//	public void test_IconsRO_getIcons() {
	//		// TODO
	//	}
	//
	//	public void test_Icons_addIcon_String_name() {
	//		// TODO
	//	}
	//
	//	public void test_Icons_removeIcon_String_iconID() {
	//		// TODO
	//	}
	//
	//	public void test_LinkRO_get() {
	//		// TODO
	//	}
	//
	//	public void test_Link_set_String_target() {
	//		// TODO
	//	}
	public void test_MapRO_getRootNode() {
		map = c.newMap();
		assertEquals("the root node shouldn't have a parent", null, map.getRootNode().getParentNode());
	}

	public void test_MapRO_node_String_id() {
		map = c.newMap();
		final Node firstChild = addChild(map.getRootNode(), "child 1");
		final String id = firstChild.getNodeID();
		assertEquals("get by id returned wrong node", firstChild, map.node(id));
	}

	public void test_MapRO_getFile() {
		map = c.newMap();
		assertTrue("the file of a new map should be null", map.getFile() == null);
	}

	public void test_Map_close() {
		Map originalMap = node.getMap();
		map = c.newMap();
		addChild(map.getRootNode(), "child 1");
		assertFalse("a new map should have been opened", originalMap.equals(map));
		map.close(true, false);
		assertEquals("the original map should be selected again", originalMap.getName(), c.getSelected().getMap()
		    .getName());
		// let tearDown() some work to do...
		map = c.newMap();
	}

	public void test_Map_save() {
		// TODO
	}

	public void test_NodeRO_getAttributes() {
		map = createTestMap();
		map.getRootNode().getAttributes().add("a1", "va1");
		assertEquals("value should be found", "va1", map.getRootNode().getAttributes().get(0));
	}

	public void test_NodeRO_getChildPosition_Node_childNode() {
		map = c.newMap();
		final Node child1 = addChild(map.getRootNode(), "child 1");
		final Node child2 = addChild(map.getRootNode(), "child 2");
		assertEquals("wrong position", 0, map.getRootNode().getChildPosition(child1));
		assertEquals("wrong position", 1, map.getRootNode().getChildPosition(child2));
	}

	public void test_NodeRO_getChildren() {
		map = c.newMap();
		final Node child1 = addChild(map.getRootNode(), "child 1");
		final Node child2 = addChild(map.getRootNode(), "child 2");
		final List<Node> children = map.getRootNode().getChildren();
		assertEquals("wrong children count", 2, children.size());
		assertEquals("wrong order", child1, children.get(0));
		assertEquals("wrong order", child2, children.get(1));
	}

	//
	//	public void test_NodeRO_getConnectorsIn() {
	//		// TODO
	//	}
	//
	//	public void test_NodeRO_getConnectorsOut() {
	//		// TODO
	//	}
	//
	//	public void test_NodeRO_getExternalObject() {
	//		// TODO
	//	}
	public void test_NodeRO_getIcons() {
		map = c.newMap();
		final Node root = map.getRootNode();
		assertTrue("by default a node has no icons", root.getIcons().getIcons().isEmpty());
		root.getIcons().addIcon("bee");
		assertEquals("one icon added", 1, root.getIcons().getIcons().size());
	}

	public void test_NodeRO_getLink() {
		map = c.newMap();
		final Node root = map.getRootNode();
		assertEquals("by default a node has no links", null, root.getLink().get());
		final String url = "file://blabla.txt";
		root.getLink().set(url);
		assertEquals("a link should have been added", url, root.getLink().get());
	}

	public void test_NodeRO_getMap() {
		map = c.newMap();
		final Node root = map.getRootNode();
		assertEquals("???", map, root.getMap());
	}

	public void test_NodeRO_getNodeID() {
		map = c.newMap();
		final Node root = map.getRootNode();
		assertTrue("unknown node id pattern in '" + root.getNodeID() + "'", root.getNodeID().matches("ID_[1-9]\\d+"));
	}

	public void test_NodeRO_getNodeLevel_boolean_countHidden() {
		map = createTestMap();
		assertEquals("root is level 0", 0, map.getRootNode().getNodeLevel(true));
		final Node child = firstChild(map.getRootNode());
		assertEquals("children are at level 1", 1, child.getNodeLevel(false));
		final Node grandchild = child.createChild();
		assertEquals("grandchildren are at level 2", 2, grandchild.getNodeLevel(false));
		assertEquals("grandchildren are at level 2 - countHidden only matters if there are hidden nodes" //
		    , 2, grandchild.getNodeLevel(true));
		// seems that the countHidden flag isn't testable here since it's not possible to filter nodes (and it
		// doesn't make sense to extent the API for that), right?
	}

	public void test_NodeRO_getPlainNoteText() {
		map = c.newMap();
		final Node rootNode = map.getRootNode();
		final String plainText = " xx\nx ";
		rootNode.setNoteText(plainText);
		assertEquals("", plainText, rootNode.getNoteText());
		assertEquals("", plainText, rootNode.getPlainNoteText());
		final String xml = "<x> yyy </x>";
		rootNode.setNoteText(xml);
		assertEquals("", xml, rootNode.getPlainNoteText());
		rootNode.setNoteText("<html> <em>zzzzz</em> </hmtl>");
		assertEquals("", "zzzzz", rootNode.getPlainNoteText());
	}

	public void test_NodeRO_getNoteText() {
		map = c.newMap();
		final Node root = map.getRootNode();
		root.setNoteText(" xxx ");
		assertEquals("", " xxx ", root.getNoteText());
		root.setNoteText(" x\nxx ");
		assertEquals("", " x\nxx ", root.getNoteText());
	}

	public void test_NodeRO_getParentNode() {
		map = createTestMap();
		final Node root = map.getRootNode();
		assertEquals("root has no parent", null, root.getParentNode());
		final Node child = firstChild(root);
		assertEquals("", root, child.getParentNode());
	}

	//	public void test_NodeRO_getStyle() {
	//		// TODO
	//	}
	//
	public void test_NodeRO_getPlainText() {
		map = c.newMap();
		final Node root = map.getRootNode();
		final String plainText = " xxx ";
		root.setText(plainText);
		assertEquals("", plainText, root.getText());
		assertEquals("plain text should be kept untouched", plainText, root.getPlainText());
		final String xml = "<x> yyy </x>";
		root.setText(xml);
		assertEquals("xml tags are not stripped", xml, root.getPlainText());
		root.setText("<html> <em>zzzzz</em> </hmtl>");
		assertEquals("html tags should be stripped", "zzzzz", root.getPlainText());
	}

	public void test_NodeRO_getPlainTextContent() {
		// getPlainTextContent() is deprecated - see #test_NodeRO_getPlainText()
	}

	public void test_NodeRO_getText() {
		map = c.newMap();
		final Node root = map.getRootNode();
		root.setText(" xxx ");
		assertEquals("", " xxx ", root.getText());
		root.setText(" x\nxx ");
		assertEquals("", " x\nxx ", root.getText());
	}

	public void test_NodeRO_isDescendantOf_Node_p() {
		map = createTestMap();
		final Node root = map.getRootNode();
		assertTrue("a node is its own descendant", root.isDescendantOf(root));
		assertFalse("siblings aren't descendants of each other", firstChild(root).isDescendantOf(
		    root.getChildren().get(1)));
		assertFalse("siblings aren't descendants of each other", root.getChildren().get(1).isDescendantOf(
		    firstChild(root)));
		assertTrue("children are descendants of their parents", firstChild(root).isDescendantOf(root));
		final Node grandchild = firstChild(root).createChild();
		assertTrue("grandchildren are descendants of their parents", firstChild(root).isDescendantOf(root));
		assertTrue("grandchildren are descendants of their grandparents", grandchild.isDescendantOf(root));
	}

	public void test_NodeRO_isFolded() {
		map = createTestMap();
		final Node root = map.getRootNode();
		final Node child = firstChild(map.getRootNode());
		final Node grandchild = addChild(child, "grandchild");
		assertFalse("initially nothing should be folded", root.isFolded());
		assertFalse("initially nothing should be folded", child.isFolded());
		root.setFolded(true);
		assertFalse("root isn't foldable", root.isFolded());
		child.setFolded(true);
		assertTrue("node should be folded now", child.isFolded());
		assertFalse("folding is not recursive in terms of isFolded()", grandchild.isFolded());
		child.setFolded(false);
		assertFalse("node should be unfolded again", child.isFolded());
		grandchild.setFolded(true);
		assertFalse("a node without children is not foldable", grandchild.isFolded());
		// test undo of folding - give the new node a child first to make it foldable
		addChild(grandchild, "grandgrandchild");
		grandchild.setFolded(true);
		assertTrue("node should be folded now", grandchild.isFolded());
		c.undo();
		assertFalse("folding should be undone now", grandchild.isFolded());
	}

	public void test_NodeRO_isLeaf() {
		map = c.newMap();
		final Node root = map.getRootNode();
		assertTrue("even root is a leaf, if single", root.isLeaf());
		addChild(root, "child");
		assertFalse("root is never a leaf, even without children", root.isLeaf());
		assertTrue("child without children should be leaf", firstChild(root).isLeaf());
		addChild(firstChild(root), "grandchild");
		assertFalse("child with children is not a leaf", firstChild(root).isLeaf());
	}

	//
	//	public void test_NodeRO_isLeft() {
	//		// TODO
	//	}
	//
	public void test_NodeRO_isRoot() {
		map = createTestMap();
		assertTrue("root has no parent", map.getRootNode().getParentNode() == null);
	}

	//	public void test_NodeRO_isVisible() {
	//		// TODO
	//	}
	//
	/** copy of {@link #test_ControllerRO_find_ICondition_condition()}. */
	public void test_NodeRO_find_ICondition_condition() {
		map = c.newMap();
		@SuppressWarnings("unused")
		final Node firstChild = addChild(map.getRootNode(), "child 1");
		final Node secondChild = addChild(map.getRootNode(), "child 2");
		final List<Node> found = c.find(new NodeContainsCondition("child 2"));
		assertEquals("one matching node should be found", list(secondChild), found);
	}

	//	public void test_NodeRO_find_Closure_closure() {
	//		// TODO
	//	}
	//
	//	public void test_NodeRO_getLastModifiedAt() {
	//		// TODO
	//	}
	//
	//	public void test_NodeRO_getCreatedAt() {
	//		// TODO
	//	}
	//
	//	public void test_Node_addConnectorTo_Node_target() {
	//		// TODO
	//	}
	//
	//	public void test_Node_addConnectorTo_String_targetNodeID() {
	//		// TODO
	//	}
	public void test_Node_createChild() {
		map = c.newMap();
		assertEquals("", 0, map.getRootNode().getChildren().size());
		map.getRootNode().createChild();
		assertEquals("child should be created", 1, map.getRootNode().getChildren().size());
	}
	//
	//	public void test_Node_createChild_int_position() {
	//		// TODO
	//	}
	//
	//	public void test_Node_delete() {
	//		// TODO
	//	}
	//
	//	public void test_Node_moveTo_Node_parentNode() {
	//		// TODO
	//	}
	//
	//	public void test_Node_moveTo_Node_parentNode_int_position() {
	//		// TODO
	//	}
	//
	//	public void test_Node_removeConnector_Connector_connectorToBeRemoved() {
	//		// TODO
	//	}
	//
	//	public void test_Node_setFolded_boolean_folded() {
	//		// TODO
	//	}
	//
	//	public void test_Node_setPlainNoteText_String_text() {
	//		// TODO
	//	}
	//
	//	public void test_Node_setNoteText_String_text() {
	//		// TODO
	//	}
	//
	//	public void test_Node_setText_String_text() {
	//		// TODO
	//	}
	//
	//	public void test_Node_setLastModifiedAt_Date_date() {
	//		// TODO
	//	}
	//
	//	public void test_Node_setCreatedAt_Date_date() {
	//		// TODO
	//	}
	//
	//	public void test_NodeStyleRO_getStyle() {
	//		// TODO
	//	}
	//
	//	public void test_NodeStyleRO_getStyleNode() {
	//		// TODO
	//	}
	//
	//	public void test_NodeStyleRO_getBackgroundColor() {
	//		// TODO
	//	}
	//
	//	public void test_NodeStyleRO_getEdge() {
	//		// TODO
	//	}
	//
	//	public void test_NodeStyleRO_getFont() {
	//		// TODO
	//	}
	//
	//	public void test_NodeStyleRO_getNodeTextColor() {
	//		// TODO
	//	}
	//
	//	public void test_NodeStyle_setStyle_Object_key() {
	//		// TODO
	//	}
	//
	//	public void test_NodeStyle_setBackgroundColor_Color_color() {
	//		// TODO
	//	}
	//
	//	public void test_NodeStyle_setNodeTextColor_Color_color() {
	//		// TODO
	//	}
}
