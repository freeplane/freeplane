package org.freeplane.plugin.script.proxy;

import java.util.List;

import org.freeplane.api.Convertible;
import org.freeplane.api.NodeCondition;
import org.freeplane.features.filter.condition.ICondition;

import groovy.lang.Closure;

public interface Proxy {
	interface AttributesRO extends org.freeplane.api.Proxy.AttributesRO{
		/** returns the values of all attributes for which the closure returns true. The fact that the values are
		 * returned as a list of {@link Convertible} enables conversion. The following formula sums all attributes
		 * whose names are not equal to 'TOTAL':
		 * <pre>{@code
		 *  = attributes.findValues{key, val -> key != 'TOTAL'}.sum(0){it.num0}
		 * }</pre>
		 * @param closure A closure that accepts two arguments (String key, Object value) and returns boolean/Boolean.
		 * @since 1.2 */
		List<? extends Convertible> findValues(Closure<Boolean> closure);
	}
	interface Attributes extends AttributesRO, org.freeplane.api.Proxy.Attributes { }
    interface Cloud extends org.freeplane.api.Proxy.Cloud { }

	interface ConnectorRO extends org.freeplane.api.Proxy.ConnectorRO {	}

	interface Connector extends ConnectorRO, org.freeplane.api.Proxy.Connector { }

	interface ControllerRO extends org.freeplane.api.Proxy.ControllerRO {
		/**
		 * Starting from the root node, recursively searches for nodes (in breadth-first sequence) for which
		 * <code>closure.call(node)</code> returns true.
		 * <p>
		 * A find method that uses a Groovy closure ("block") for simple custom searches. As this closure
		 * will be called with a node as an argument (to be referenced by <code>it</code>) the search can
		 * evaluate every node property, like attributes, icons, node text or notes.
		 * <p>
		 * Examples:
		 * <pre>
		 *    def nodesWithNotes = c.find{ it.noteText != null }
		 *
		 *    def matchingNodes = c.find{ it.text.matches(".*\\d.*") }
		 *    def texts = matchingNodes.collect{ it.text }
		 *    print "node texts containing numbers:\n " + texts.join("\n ")
		 * </pre>
		 * See {@link Node#find(Closure)} for searches on subtrees.
		 * @param closure a Groovy closure that returns a boolean value. The closure will receive
		 *        a NodeModel as an argument which can be tested for a match.
		 * @return all nodes for which <code>closure.call(NodeModel)</code> returns true.
		 */
		List<? extends org.freeplane.api.Proxy.Node> find(Closure<Boolean> closure);

		/** Starting from the root node, recursively searches for nodes for which
		 * <code>condition.checkNode(node)</code> returns true.
		 * @deprecated since 1.2 use {@link #find(NodeCondition)} instead. */
		@Deprecated
		List<? extends org.freeplane.api.Proxy.Node> find(ICondition condition);

	}

	interface Controller extends ControllerRO , org.freeplane.api.Proxy.Controller{	}

	interface EdgeRO extends org.freeplane.api.Proxy.EdgeRO { }

	interface Edge extends EdgeRO, org.freeplane.api.Proxy.Edge { }

	interface ExternalObjectRO extends org.freeplane.api.Proxy.ExternalObjectRO { }

	interface ExternalObject extends ExternalObjectRO, org.freeplane.api.Proxy.ExternalObject { }

	interface FontRO extends org.freeplane.api.Proxy.FontRO { }

	interface Font extends FontRO, org.freeplane.api.Proxy.Font { }

	interface IconsRO extends org.freeplane.api.Proxy.IconsRO {
	}

	interface Icons extends IconsRO, org.freeplane.api.Proxy.Icons { }

	interface LinkRO extends org.freeplane.api.Proxy.LinkRO { }

	interface Link extends LinkRO, org.freeplane.api.Proxy.Link { }

	interface MapRO extends org.freeplane.api.Proxy.MapRO {	}

	interface Map extends MapRO , org.freeplane.api.Proxy.Map {	}

	interface NodeRO extends org.freeplane.api.Proxy.NodeRO {

		/** Starting from this node, recursively searches for nodes for which <code>closure.call(node)</code>
		 * returns true. See {@link Controller#find(Closure)} for details. */
		List<? extends org.freeplane.api.Proxy.Node> find(Closure<Boolean> closure);

		/** Starting from this node, recursively searches for nodes for which
		 * <code>condition.checkNode(node)</code> returns true.
		 * @deprecated since 1.2 use {@link #find(NodeCondition)} instead. */
		@Deprecated
		List<? extends org.freeplane.api.Proxy.Node> find(ICondition condition);

	}

	interface Node extends NodeRO, org.freeplane.api.Proxy.Node {

    	/**
    	 * A sort method that uses the result of the Groovy closure ("block") for comparison. As this closure
    	 * will be called with a node as an argument (to be referenced by <code>it</code>) the search can
    	 * evaluate every node property, like attributes, icons, node text or notes.
    	 * <p>
    	 * Examples:
    	 * <pre>
    	 *    // sort by details text
    	 *    node.sortChildrenBy{ it.details.to.plain }
    	 *    // sort numerically
    	 *    node.sortChildrenBy{ it.to.num0 }
    	 * </pre>
    	 * @param closure a Groovy closure that returns a Comparable value like a String. The closure will receive
    	 *        a NodeModel as an argument.
    	 * @since 1.4.1
    	 */
		void sortChildrenBy(Closure<Comparable<Object>> closure);
	}

	interface NodeStyleRO extends org.freeplane.api.Proxy.NodeStyleRO { }

	interface NodeStyle extends NodeStyleRO, org.freeplane.api.Proxy.NodeStyle { }

    public interface Properties extends org.freeplane.api.Proxy.Properties { }
    interface ReminderRO extends org.freeplane.api.Proxy.ReminderRO { }

    interface Reminder extends ReminderRO, org.freeplane.api.Proxy.Reminder {

    }
}
