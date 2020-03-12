package org.freeplane.plugin.script.proxy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.freeplane.api.MindMap;
import org.freeplane.api.Node;
import org.freeplane.core.util.HtmlUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.filter.Filter;
import org.freeplane.features.icon.factory.IconStoreFactory;
import org.freeplane.features.icon.mindmapmode.MIconController;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.text.NodeContainsCondition;
import org.freeplane.features.text.TextController;
import org.freeplane.features.ui.ViewController;

public class ScriptApiTest {
	ControllerProxy c;
	NodeProxy node;
	private MindMap map;
	private Comparator<NodeProxy> nodeComparator = new Comparator<NodeProxy>() {
		@Override
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
		final long startMillis = System.currentTimeMillis();
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
		double seconds = (System.currentTimeMillis() - startMillis) / 1000.;
		String message = null;
		String iconKey = null;
		if (errors + failures == 0) {
			message = "success! " + pass + " tests passed in " + seconds + " seconds";
			iconKey = "button_ok";
		}
		else {
			message = "error! test result: " + errors + " errors, " + failures + " failures; " + pass
			        + " tests passed in " + seconds + " seconds";
			iconKey = "button_cancel";
		}
		LogUtils.info(message);
		c.setStatusInfo(ViewController.STANDARD_STATUS_INFO_KEY, message, iconKey);
	}

	public void tearDown() {
		if (map != null)
			map.close(true, false);
	}

	@SuppressWarnings("deprecation")
	public void test_AttributesRO_get_String_name() {
		map = setupMapWithSomeAttributes();
		assertEquals("first value should be found", "va1", map.getRoot().getAttributes().get("a1"));
		assertEquals("should return null for non-existing attributes", null, map.getRoot().getAttributes().get("x"));
	}

	private MindMap setupMapWithSomeAttributes() {
		createTestMap();
		map.getRoot().getAttributes().add("a1", "va1");
		map.getRoot().getAttributes().add("a1", "va2");
		map.getRoot().getAttributes().add("a1", "va3");
		map.getRoot().getAttributes().add("b1", "vb1");
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

	private MindMap createTestMap() {
		map = c.newMindMap();
		map.getRoot().createChild("first node");
		map.getRoot().createChild("second node");
		return map;
	}

	private Node firstChild(final Node node) {
		return node.getChildren().get(0);
	}

	private Node firstChild() {
		return firstChild(map.getRoot());
	}

	public void test_AttributesRO_getAll_String_name() {
		map = setupMapWithSomeAttributes();
		assertEquals("all values should be found", list("va1", "va2", "va3"), map.getRoot().getAttributes().getAll(
		    "a1"));
		assertEquals("all values should be found", Collections.singletonList("vb1"), map.getRoot().getAttributes()
		    .getAll("b1"));
		assertEquals("should return empty list for non-existing attributes", Collections.EMPTY_LIST, map.getRoot()
		    .getAttributes().getAll("x"));
	}

	@SuppressWarnings("deprecation")
    public void test_AttributesRO_getAttributeNames() {
		map = setupMapWithSomeAttributes();
		assertEquals("all names should be found", list("a1", "a1", "a1", "b1"), map.getRoot().getAttributes()
		    .getAttributeNames());
	}

	public void test_AttributesRO_get_int_index() {
		map = setupMapWithSomeAttributes();
		assertEquals("find by index", "va1", map.getRoot().getAttributes().get(0));
		assertEquals("find by index", "va2", map.getRoot().getAttributes().get(1));
		assertEquals("find by index", "va3", map.getRoot().getAttributes().get(2));
		assertEquals("find by index", "vb1", map.getRoot().getAttributes().get(3));
		try {
			map.getRoot().getAttributes().get(4);
			fail("expect IndexOutOfBoundsException on get(int) with illegal index");
		}
		catch (IndexOutOfBoundsException e) {
			// OK - expected
		}
	}

	public void test_AttributesRO_findAttribute_String_name() {
		map = setupMapWithSomeAttributes();
		assertEquals("first matching attribute should be found", 0, map.getRoot().getAttributes().findFirst("a1"));
	}

	public void test_AttributesRO_size() {
		map = setupMapWithSomeAttributes();
		assertEquals("count attributes", 4, map.getRoot().getAttributes().size());
	}

	public void test_Attributes_set_int_index_String_value() {
		map = setupMapWithSomeAttributes();
		map.getRoot().getAttributes().set(3, "VB1");
		map.getRoot().getAttributes().set(1, "VA2");
		assertEquals("values should be updated", list("va1", "VA2", "va3"), map.getRoot().getAttributes().getAll(
		    "a1"));
		assertEquals("values should be updated", list("VB1"), map.getRoot().getAttributes().getAll("b1"));
		try {
			map.getRoot().getAttributes().set(4, "xx");
			fail("expect IndexOutOfBoundsException on set(int, value) with illegal index");
		}
		catch (IndexOutOfBoundsException e) {
			// OK - expected
		}
	}

	public void test_Attributes_set_int_index_String_name_String_value() {
		map = setupMapWithSomeAttributes();
		map.getRoot().getAttributes().set(1, "c1", "VC1");
		assertEquals("values should be updated", list("va1", "va3"), map.getRoot().getAttributes().getAll("a1"));
		assertEquals("values should be updated", 1, map.getRoot().getAttributes().findFirst("c1"));
		assertEquals("values should be updated", "VC1", map.getRoot().getAttributes().get(1));
		try {
			map.getRoot().getAttributes().set(4, "xx", "yy");
			fail("expect IndexOutOfBoundsException on set(int, name, value) with illegal index");
		}
		catch (IndexOutOfBoundsException e) {
			// OK - expected
		}
	}

	@SuppressWarnings("deprecation")
	public void test_Attributes_remove_String_name() {
		map = setupMapWithSomeAttributes();
		map.getRoot().getAttributes().remove("a1");
		assertEquals("first matching attribute should be removed", list("va2", "va3"), map.getRoot()
		    .getAttributes().getAll("a1"));
		assertEquals("first matching attribute should be removed", 3, map.getRoot().getAttributes().size());
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
		map.getRoot().getAttributes().removeAll("a1");
		assertEquals("all matching attributes should be removed", list(), map.getRoot().getAttributes()
		    .getAll("a1"));
		assertEquals("all matching attribute should be removed", 1, map.getRoot().getAttributes().size());
	}

	public void test_Attributes_remove_int_index() {
		map = setupMapWithSomeAttributes();
		// remove from the rear to keep the index stable (otherwise remove(1) twice)
		map.getRoot().getAttributes().remove(2);
		map.getRoot().getAttributes().remove(1);
		assertEquals("attribute should be removed", list("va1"), map.getRoot().getAttributes().getAll("a1"));
		assertEquals("attribute should be removed", 2, map.getRoot().getAttributes().size());
	}

	public void test_Attributes_set_String_name_String_value() {
		map = setupMapWithSomeAttributes();
		map.getRoot().getAttributes().set("a1", "VA1");
		map.getRoot().getAttributes().set("c1", "vc1");
		assertEquals("first matching attribute should be changed", list("VA1", "va2", "va3"), map.getRoot()
		    .getAttributes().getAll("a1"));
		assertEquals("attribute should be added", list("vc1"), map.getRoot().getAttributes().getAll("c1"));
		assertEquals("attribute should be added", 5, map.getRoot().getAttributes().size());
	}

	public void test_Attributes_add_String_name_String_value() {
		map = setupMapWithSomeAttributes();
		map.getRoot().getAttributes().add("a1", "va1");
		map.getRoot().getAttributes().set("c1", "vc1");
		assertEquals("attribute should be added", list("va1", "va2", "va3", "va1"), map.getRoot().getAttributes()
		    .getAll("a1"));
		assertEquals("attribute should be added", list("vc1"), map.getRoot().getAttributes().getAll("c1"));
		assertEquals("attribute should be added", 6, map.getRoot().getAttributes().size());
	}

	//
	//	public void test_ConnectorRO_getColor() {
	//
	//	}
	//
	//	public void test_ConnectorRO_getEndArrow() {
	//
	//	}
	//
	//	public void test_ConnectorRO_getMiddleLabel() {
	//
	//	}
	//
	//	public void test_ConnectorRO_getSource() {
	//
	//	}
	//
	//	public void test_ConnectorRO_getSourceLabel() {
	//
	//	}
	//
	//	public void test_ConnectorRO_getStartArrow() {
	//
	//	}
	//
	//	public void test_ConnectorRO_getTarget() {
	//
	//	}
	//
	//	public void test_ConnectorRO_getTargetLabel() {
	//
	//	}
	//
	//	public void test_ConnectorRO_simulatesEdge() {
	//
	//	}
	//
	//	public void test_Connector_setColor_Color_color() {
	//
	//	}
	//
	//	public void test_Connector_setEndArrow_ArrowType_arrowType() {
	//
	//	}
	//
	//	public void test_Connector_setMiddleLabel_String_label() {
	//
	//	}
	//
	//	public void test_Connector_setSimulatesEdge_boolean_simulatesEdge() {
	//
	//	}
	//
	//	public void test_Connector_setSourceLabel_String_label() {
	//
	//	}
	//
	//	public void test_Connector_setStartArrow_ArrowType_arrowType() {
	//
	//	}
	//
	//	public void test_Connector_setTargetLabel_String_label() {
	//
	//	}
	public void test_ControllerRO_getSelected() {
		map = c.newMindMap();
		assertEquals("new root node should be selected", map.getRoot(), c.getSelected());
		final Node firstChild = map.getRoot().createChild("child 1");
		final Node secondChild = map.getRoot().createChild("child 2");
		// FIXME: why aren't the new node selected?
		assertEquals("root node should still be selected after adding nodes", map.getRoot(), c.getSelected());
		c.selectMultipleNodes(list(firstChild, secondChild));
		// what's the rule?
		assertEquals("last selected node should be returned", secondChild, c.getSelected());
	}

	public void test_ControllerRO_getSelecteds() {
		map = c.newMindMap();
		assertEquals("new root node should be selected", map.getRoot(), c.getSelected());
		final Node firstChild = map.getRoot().createChild("child 1");
		final Node secondChild = map.getRoot().createChild("child 2");
		assertEquals("root node should still be selected after adding nodes", set(map.getRoot()), set(c
		    .getSelecteds()));
		c.selectMultipleNodes(list(firstChild, secondChild));
		assertEquals("only the nodes selected via selectMultipleNodes should be returned",
		    set(firstChild, secondChild), set(c.getSelecteds()));
	}

	public void test_ControllerRO_getSortedSelection_boolean_differentSubtrees() {

	}

	@SuppressWarnings("deprecation")
    public void test_ControllerRO_find_ICondition_condition() {
		map = c.newMindMap();
		@SuppressWarnings("unused")
		final Node firstChild = map.getRoot().createChild("child 1");
		final Node secondChild = map.getRoot().createChild("child 2");
		final List<? extends Node> found = c.find(new NodeContainsCondition(TextController.FILTER_NODE, "child 2", true, false));
		assertEquals("one matching node should be found", list(secondChild), found);
	}

	public void test_Controller_centerOnNode_Node_center() {
		map = c.newMindMap();
		final Node firstChild = map.getRoot().createChild("child 1");
		// no actual test
		c.centerOnNode(firstChild);
	}

	public void test_Controller_select_Node_toSelect() {
		map = c.newMindMap();
		final Node firstChild = map.getRoot().createChild("child 1");
		final Node secondChild = map.getRoot().createChild("child 2");
		final Node thirdChild = map.getRoot().createChild("child 3");
		c.select(secondChild);
		final Set<Node> set = set(secondChild);
		final Set<? extends Node> selected = set(c.getSelecteds());
		assertEquals("one node should be selected", set, selected);
		c.select(firstChild);
		assertEquals("one node should be selected", set(firstChild), set(c.getSelecteds()));
		c.select(thirdChild);
		assertEquals("one node should be selected", set(thirdChild), set(c.getSelecteds()));
	}

	public void test_Controller_selectBranch_Node_branchRoot() {
		map = c.newMindMap();
		final Node child1 = map.getRoot().createChild("child 1");
		final Node child2 = map.getRoot().createChild("child 2");
		final Node grandchild1 = child1.createChild("child 1.1");
		final Node grandchild2 = child1.createChild("child 1.2");
		final Node grandGrandChild = child1.createChild("child 1.1.1");
		c.selectBranch(child1);
		assertEquals("all node of the branch should be selected",
		    set(child1, grandchild1, grandchild2, grandGrandChild), set(c.getSelecteds()));
		c.selectBranch(child2);
		assertEquals("one node should be selected", set(child2), set(c.getSelecteds()));
	}

	public void test_Controller_selectMultipleNodes_List_Node_toSelect() {
		// see test_ControllerRO_getSelected()
	}

	@SuppressWarnings("deprecation")
	public void test_Controller_undo_redo_stuff() {
		map = c.newMindMap();
		map.getRoot().createChild("child 1");
		assertFalse("node should be there before undo", c.find(new NodeContainsCondition(TextController.FILTER_NODE, "child 1", true, false)).isEmpty());
		c.undo();
		assertTrue("node should be away after undo", c.find(new NodeContainsCondition(TextController.FILTER_NODE, "child 1", true, false)).isEmpty());
		c.redo();
		assertFalse("node should be there after redo", c.find(new NodeContainsCondition(TextController.FILTER_NODE, "child 1", true, false)).isEmpty());
		c.deactivateUndo();
		c.undo();
		assertTrue("node should still be there after undo since undo is deactivated", c.find(
		    new NodeContainsCondition(TextController.FILTER_NODE, "child 1", true, false)).isEmpty());
	}

	public void test_Controller_setStatusInfo_String_info() {
		// no actual test
		c.setStatusInfo("test statusinfo");
		// no actual test - info should be removed
		c.setStatusInfo(null);
	}

	public void test_Controller_setStatusInfo_String_infoPanelKey_String_info() {
		// no actual test
		final String infoPanelKey = "testStatusPanel";
		c.setStatusInfo(infoPanelKey, "someid");
		// no actual test - info should be removed
		c.setStatusInfo(infoPanelKey, (String) null);
	}

	@SuppressWarnings("deprecation")
	public void test_Controller_setStatusInfo_String_infoPanelKey_Icon_icon() {
		final List<String> keys = MIconController.listStandardIconKeys();
		assertTrue("find out about the available icons with FreeplaneIconUtils.listStandardIconKeys() -> " + keys,
		    keys.contains("button_ok"));
		// no actual test
		c.setStatusInfo("standard", IconStoreFactory.ICON_STORE.getUIIcon("button_ok").getIcon());
		// no actual test - info should be removed
		c.setStatusInfo(null);
	}

	public void test_Controller_setStatusInfo_String_infoPanelKey_String_info_String_iconKey() {
		final List<String> keys = MIconController.listStandardIconKeys();
		final String iconKey = "button_ok";
		assertTrue("check if FreeplaneIconUtils.listStandardIconKeys() contains '" + iconKey + "'; list content: "
		        + keys, keys.contains(iconKey));
		// no actual test
		final String infoPanelKey = "standard";
		c.setStatusInfo(infoPanelKey, "hi there!", iconKey);
		// no actual test - info should be removed
		c.setStatusInfo(infoPanelKey, (String) null);
	}

	public void test_Controller_newMap() {
		map = c.newMindMap();
	}

	//
	//	public void test_EdgeRO_getColor() {
	//
	//	}
	//
	//	public void test_EdgeRO_getType() {
	//
	//	}
	//
	//	public void test_EdgeRO_getWidth() {
	//
	//	}
	//
	//	public void test_Edge_setColor_Color_color() {
	//
	//	}
	//
	//	public void test_Edge_setType_EdgeStyle_type() {
	//
	//	}
	//
	//	public void test_Edge_setWidth_int_width() {
	//
	//	}
	//
	//	public void test_ExternalObjectRO_getURI() {
	//
	//	}
	//
	//	public void test_ExternalObjectRO_getZoom() {
	//
	//	}
	//
	//	public void test_ExternalObject_setURI_String_uri() {
	//
	//	}
	//
	//	public void test_ExternalObject_setZoom_float_zoom() {
	//
	//	}
	//
	//	public void test_FontRO_getName() {
	//
	//	}
	//
	//	public void test_FontRO_getSize() {
	//
	//	}
	//
	//	public void test_FontRO_isBold() {
	//
	//	}
	//
	//	public void test_FontRO_isBoldSet() {
	//
	//	}
	//
	//	public void test_FontRO_isItalic() {
	//
	//	}
	//
	//	public void test_FontRO_isItalicSet() {
	//
	//	}
	//
	//	public void test_FontRO_isStrikedThrough() {
	//
	//	}
	//
	//	public void test_FontRO_isStrikedThroughSet() {
	//
	//	}
	//
	//	public void test_FontRO_isNameSet() {
	//
	//	}
	//
	//	public void test_FontRO_isSizeSet() {
	//
	//	}
	//
	//	public void test_Font_resetBold() {
	//
	//	}
	//
	//	public void test_Font_resetItalic() {
	//
	//	}
	//
	//	public void test_Font_resetStrikedThrough() {
	//
	//	}
	//
	//	public void test_Font_resetName() {
	//
	//	}
	//
	//	public void test_Font_resetSize() {
	//
	//	}
	//
	//	public void test_Font_setBold_boolean_bold() {
	//
	//	}
	//
	//	public void test_Font_setItalic_boolean_italic() {
	//
	//	}
	//
	//	public void test_Font_setStrikedThrough_boolean_strikedThrough() {
	//
	//	}
	//
	//	public void test_Font_setName_String_name() {
	//
	//	}
	//
	//	public void test_Font_setSize_int_size() {
	//
	//	}
	//
	//	public void test_IconsRO_getIcons() {
	//
	//	}
	//
	//	public void test_Icons_addIcon_String_name() {
	//
	//	}
	//
	//	public void test_Icons_removeIcon_String_iconID() {
	//
	//	}
	//
	//	public void test_LinkRO_get() {
	//
	//	}
	//
	//	public void test_Link_set_String_target() {
	//
	//	}
	public void test_MapRO_getRoot() {
		map = c.newMindMap();
		assertEquals("the root node shouldn't have a parent", null, map.getRoot().getParent());
	}

	public void test_MapRO_node_String_id() {
		map = c.newMindMap();
		final Node firstChild = map.getRoot().createChild("child 1");
		final String id = firstChild.getId();
		assertEquals("get by id returned wrong node", firstChild, map.node(id));
	}

	public void test_MapRO_getFile() {
		map = c.newMindMap();
		assertTrue("the file of a new map should be null", map.getFile() == null);
	}

	public void test_Map_close() {
		MindMap originalMap = node.getMindMap();
		map = c.newMindMap();
		map.getRoot().createChild("child 1");
		assertFalse("a new map should have been opened", originalMap.equals(map));
		map.close(true, false);
		assertEquals("the original map should be selected again", originalMap.getName(), c.getSelected().getMindMap()
		    .getName());
		// let tearDown() some work to do...
		map = c.newMindMap();
	}

	public void test_Map_save() {

	}

	public void test_NodeRO_getAttributes() {
		createTestMap();
		map.getRoot().getAttributes().add("a1", "va1");
		assertEquals("value should be found", "va1", map.getRoot().getAttributes().get(0));
	}

	public void test_NodeRO_getChildPosition_Node_childNode() {
		map = c.newMindMap();
		final Node child1 = map.getRoot().createChild("child 1");
		final Node child2 = map.getRoot().createChild("child 2");
		assertEquals("wrong position", 0, map.getRoot().getChildPosition(child1));
		assertEquals("wrong position", 1, map.getRoot().getChildPosition(child2));
	}

	public void test_NodeRO_getChildren() {
		map = c.newMindMap();
		final Node child1 = map.getRoot().createChild("child 1");
		final Node child2 = map.getRoot().createChild("child 2");
		final List<? extends Node> children = map.getRoot().getChildren();
		assertEquals("wrong children count", 2, children.size());
		assertEquals("wrong order", child1, children.get(0));
		assertEquals("wrong order", child2, children.get(1));
	}

	//
	//	public void test_NodeRO_getConnectorsIn() {
	//
	//	}
	//
	//	public void test_NodeRO_getConnectorsOut() {
	//
	//	}
	//
	//	public void test_NodeRO_getExternalObject() {
	//
	//	}
	public void test_NodeRO_getIcons() {
		map = c.newMindMap();
		final Node root = map.getRoot();
		assertTrue("by default a node has no icons", root.getIcons().getIcons().isEmpty());
		root.getIcons().add("bee");
		assertEquals("one icon added", 1, root.getIcons().getIcons().size());
	}

	public void test_NodeRO_getLink() {
		map = c.newMindMap();
		final Node root = map.getRoot();
		assertEquals("by default a node has no links", null, root.getLink().getText());
		final String url = "file://blabla.txt";
		root.getLink().setText(url);
		assertEquals("a link should have been added", url, root.getLink().getText());
	}

	public void test_NodeRO_getMap() {
		map = c.newMindMap();
		final Node root = map.getRoot();
		assertEquals("???", map, root.getMindMap());
	}

	public void test_NodeRO_getId() {
		map = c.newMindMap();
		final Node root = map.getRoot();
		assertTrue("unknown node id pattern in '" + root.getId() + "'", root.getId().matches("ID_[1-9]\\d+"));
	}

	@SuppressWarnings("deprecation")
    public void test_NodeRO_getNodeID() {
		map = c.newMindMap();
		final Node root = map.getRoot();
		assertTrue("unknown node id pattern in '" + root.getNodeID() + "'", root.getNodeID().matches("ID_[1-9]\\d+"));
	}

    public void test_NodeRO_getNodeLevel_boolean_countHidden() {
		createTestMap();
		assertEquals("root is level 0", 0, map.getRoot().getNodeLevel(true));
		final Node child = firstChild();
		assertEquals("children are at level 1", 1, child.getNodeLevel(false));
		final Node grandchild = child.createChild();
		assertEquals("grandchildren are at level 2", 2, grandchild.getNodeLevel(false));
		assertEquals("grandchildren are at level 2 - countHidden only matters if there are hidden nodes" //
		    , 2, grandchild.getNodeLevel(true));
		// seems that the countHidden flag isn't testable here since it's not possible to filter nodes (and it
		// doesn't make sense to extent the API for that), right?
	}

	public void test_NodeRO_getNote_getPlain(){
		map = c.newMindMap();
		final Node rootNode = map.getRoot();
		final String plainText = " xx\nx ";
		rootNode.setNote(plainText);
		assertEquals("", HtmlUtils.plainToHTML(plainText), rootNode.getNoteText());
		assertEquals("", HtmlUtils.plainToHTML(plainText), rootNode.getNote().getText());
		assertEquals("", plainText, rootNode.getNote().getPlain());
		final String xml = "<x> yyy </x>";
		rootNode.setNote(xml);
		assertEquals("", HtmlUtils.plainToHTML(xml), rootNode.getNoteText());
		// in Groovy also assert HtmlUtils.plainToHTML(" xxx ") == root.note would be OK
		assertEquals("", HtmlUtils.plainToHTML(xml), rootNode.getNote().getText());
		assertEquals("", xml, rootNode.getNote().getPlain());
		rootNode.setNote("<html> <em>zzzzz</em> </hmtl>");
		assertEquals("", "zzzzz", rootNode.getNote().getPlain());
	}

	public void test_NodeRO_getNote() {
		map = c.newMindMap();
		final Node root = map.getRoot();
		root.setNote(" xxx ");
		// in Groovy also assert HtmlUtils.plainToHTML(" xxx ") == root.note would be OK
		assertEquals("", HtmlUtils.plainToHTML(" xxx "), root.getNote().getText());
		assertEquals("", HtmlUtils.plainToHTML(" xxx "), root.getNoteText());
		root.setNote(" x\nxx ");
		// in Groovy also assert HtmlUtils.plainToHTML(" xxx ") == root.note would be OK
		assertEquals("", HtmlUtils.plainToHTML(" x\nxx "), root.getNote().getText());
		assertEquals("", HtmlUtils.plainToHTML(" x\nxx "), root.getNoteText());
	}

	public void test_NodeRO_getParent() {
		createTestMap();
		final Node root = map.getRoot();
		assertEquals("root has no parent", null, root.getParent());
		final Node child = firstChild(root);
		assertEquals("", root, child.getParent());
	}

	//	public void test_NodeRO_getStyle() {
	//
	//	}
	//
	public void test_NodeRO_getPlainText() {
		map = c.newMindMap();
		final Node root = map.getRoot();
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
		map = c.newMindMap();
		final Node root = map.getRoot();
		root.setText(" xxx ");
		assertEquals("", " xxx ", root.getText());
		root.setText(" x\nxx ");
		assertEquals("", " x\nxx ", root.getText());
	}

	public void test_NodeRO_isDescendantOf_Node_p() {
		createTestMap();
		final Node root = map.getRoot();
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
		createTestMap();
		final Node root = map.getRoot();
		final Node child = firstChild();
		final Node grandchild = child.createChild("grandchild");
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
		grandchild.createChild("grandgrandchild");
		grandchild.setFolded(true);
		assertTrue("node should be folded now", grandchild.isFolded());
		c.undo();
		assertFalse("folding should be undone now", grandchild.isFolded());
	}

	public void test_NodeRO_isLeaf() {
		map = c.newMindMap();
		final Node root = map.getRoot();
		assertTrue("even root is a leaf, if single", root.isLeaf());
		root.createChild("child");
		assertFalse("root is never a leaf, even without children", root.isLeaf());
		assertTrue("child without children should be leaf", firstChild(root).isLeaf());
		firstChild(root).createChild("grandchild");
		assertFalse("child with children is not a leaf", firstChild(root).isLeaf());
	}

	//
	//	public void test_NodeRO_isLeft() {
	//
	//	}
	//
	public void test_NodeRO_isRoot() {
		createTestMap();
		assertTrue("root has no parent", map.getRoot().getParent() == null);
	}

	public void test_NodeRO_isVisible() {
		map = c.newMindMap();
		map.getRoot().createChild("first node");
		map.getRoot().createChild("second node");
		assertTrue("initially all nodes should be visible", firstChild().isVisible());
		new Filter(new NodeContainsCondition(TextController.FILTER_NODE, "first", true, false), false, true, true, true).applyFilter(this, Controller
		    .getCurrentController().getMap(), true);
		assertTrue("first node should be  matched by the filter", firstChild().isVisible());
		assertFalse("second node should not be matched by the filter", map.getRoot().getChildren().get(1).isVisible());
		c.setStatusInfo("filter", (String) null);
	}

	/** copy of {@link #test_ControllerRO_find_ICondition_condition()}. */
	@SuppressWarnings("deprecation")
	public void test_NodeRO_find_ICondition_condition() {
		map = c.newMindMap();
		@SuppressWarnings("unused")
		final Node firstChild = map.getRoot().createChild("child 1");
		final Node secondChild = map.getRoot().createChild("child 2");
		final List<? extends Node> found = c.find(new NodeContainsCondition(TextController.FILTER_NODE, "child 2", true, false));
		assertEquals("one matching node should be found", list(secondChild), found);
	}

	//	public void test_NodeRO_find_Closure_closure() {
	//
	//	}
	public void test_NodeRO_getLastModifiedAt() {
		map = c.newMindMap();
		final Node child = map.getRoot().createChild("a node");
		final Date initialLastModifiedAt = child.getLastModifiedAt();
		long diff = System.currentTimeMillis() - initialLastModifiedAt.getTime();
		// one second should be enough
		assertTrue("lastModifiedAt seems to be set incorrectly. It says it's " + diff + " ms ago", //
		    diff >= 0 && diff < 1000L);
		// createChild() initially set both timestamps to the same value and changes modifiedAt directly
		// afterwards in setText()
		diff = initialLastModifiedAt.getTime() - child.getCreatedAt().getTime();
		assertTrue("modifiedAt and createdAt should be set nearly to the same timestamp initially but modifiedAt = "
		        + initialLastModifiedAt.getTime() + ", createdAt = " + child.getCreatedAt().getTime(), //
		    diff >= 0 && diff < 50);
		final Date epoch = new Date(0);
		child.setLastModifiedAt(epoch);
		child.setText("changed");
		assertTrue("lastModifiedAt should be changed after changing the node text", //
		    child.getLastModifiedAt().after(epoch));
	}

	public void test_NodeRO_getCreatedAt() {
		map = c.newMindMap();
		final Node child = map.getRoot().createChild("a node");
		final Date initialCreatedAt = child.getCreatedAt();
		final long diff = System.currentTimeMillis() - initialCreatedAt.getTime();
		// one second should be enough
		assertTrue("createdAt seems to be set incorrectly. It says it's " + diff + " ms ago", //
		    diff >= 0 && diff < 1000L);
		final Date epoch = new Date(0);
		child.setCreatedAt(epoch);
		child.setText("changed");
		assertEquals("createdAt should not be changed after changing the node text", //
		    epoch, child.getCreatedAt());
	}

	//	public void test_Node_addConnectorTo_Node_target() {
	//
	//	}
	//
	//	public void test_Node_addConnectorTo_String_targetNodeID() {
	//
	//	}
	public void test_Node_createChild() {
		map = c.newMindMap();
		assertEquals("", 0, map.getRoot().getChildren().size());
		map.getRoot().createChild();
		assertEquals("child should be created", 1, map.getRoot().getChildren().size());
	}

	public void test_Node_createChild_int_position() {
		map = c.newMindMap();
		final Node root = map.getRoot();
		final Node child1 = root.createChild("child 1");
		final Node child2 = root.createChild("child 2");
		assertEquals("wrong position", 0, root.getChildPosition(child1));
		assertEquals("wrong position", 1, root.getChildPosition(child2));
		final Node child3 = root.createChild(0);
		assertEquals("wrong insert position", 0, root.getChildPosition(child3));
		assertEquals("node should be shifted", 1, root.getChildPosition(child1));
		assertEquals("node should be shifted", 2, root.getChildPosition(child2));
		final Node child4 = root.createChild(3);
		assertEquals("wrong insert position", 3, root.getChildPosition(child4));
		assertEquals("node should be shifted", 0, root.getChildPosition(child3));
		assertEquals("node should be shifted", 1, root.getChildPosition(child1));
		try {
			root.createChild(-1);
			fail("a negative position should lead to an exception");
		}
		catch (Throwable e) {
		}
		try {
			root.createChild(5);
			fail("too large positions should lead to an exception");
		}
		catch (Throwable e) {
		}
	}

	public void test_Node_delete() {
		map = c.newMindMap();
		final Node root = map.getRoot();
		final Node child1 = root.createChild("child 1");
		final Node child2 = root.createChild("child 2");
		assertEquals("", 2, root.getChildren().size());
		child1.delete();
		assertEquals("deletion failed", 1, root.getChildren().size());
		assertEquals("wrong node deleted", child2, root.getChildren().get(0));
	}

	public void test_Node_moveTo_Node_parentNode() {
		map = c.newMindMap();
		final Node root = map.getRoot();
		final Node child1 = root.createChild("child 1");
		final Node child2 = root.createChild("child 2");
		final Node grandchild = child1.createChild("grandchild");
		assertEquals("child2 should have no children", 0, child2.getChildren().size());
		grandchild.moveTo(child2);
		assertEquals("grandchild should be a child of child2 now", child2, grandchild.getParent());
	}

	public void test_Node_moveTo_Node_parentNode_int_position() {
		map = c.newMindMap();
		final Node root = map.getRoot();
		final Node child1 = root.createChild("child 1");
		final Node child2 = root.createChild("child 2");
		final Node grandchild = child1.createChild("grandchild");
		assertEquals("wrong count of children", 2, root.getChildren().size());
		grandchild.moveTo(root, 1);
		assertEquals("wrong position", child1, root.getChildren().get(0));
		assertEquals("wrong position", grandchild, root.getChildren().get(1));
		assertEquals("wrong position", child2, root.getChildren().get(2));
	}

	//
	//	public void test_Node_removeConnector_Connector_connectorToBeRemoved() {
	//
	//	}
	//
	public void test_Node_setFolded_boolean_folded() {
		createTestMap();
		final Node child = firstChild();
		child.createChild("grandchild");
		child.setFolded(true);
		assertTrue("node should be folded now", child.isFolded());
		child.setFolded(false);
		assertFalse("node should be unfolded again", child.isFolded());
	}

	public void test_Node_setPlainNote_String_text() {
		// see test_NodeRO_getPlainNote()
	}

	public void test_Node_setNoteText_String_text() {
		// see test_NodeRO_getNote()
	}

	public void test_Node_setText_String_text() {
		// see test_NodeRO_getText()
	}

	public void test_Node_setLastModifiedAt_Date_date() {
		// see test_NodeRO_getLastModifiedAt()
	}

	public void test_Node_setCreatedAt_Date_date() {
		// see test_NodeRO_getCreatedAt()
	}
	//	public void test_NodeStyleRO_getStyle() {
	//
	//	}
	//
	//	public void test_NodeStyleRO_getStyleNode() {
	//
	//	}
	//
	//	public void test_NodeStyleRO_getBackgroundColor() {
	//
	//	}
	//
	//	public void test_NodeStyleRO_getEdge() {
	//
	//	}
	//
	//	public void test_NodeStyleRO_getFont() {
	//
	//	}
	//
	//	public void test_NodeStyleRO_getNodeTextColor() {
	//
	//	}
	//
	//	public void test_NodeStyle_setStyle_Object_key() {
	//
	//	}
	//
	//	public void test_NodeStyle_setBackgroundColor_Color_color() {
	//
	//	}
	//
	//	public void test_NodeStyle_setNodeTextColor_Color_color() {
	//
	//	}
}
