package org.freeplane.plugin.script.proxy;

import java.io.File;
import java.net.URL;
import java.util.List;

import org.freeplane.api.Convertible;
import org.freeplane.api.FreeplaneVersion;
import org.freeplane.api.NodeCondition;
import org.freeplane.core.ui.LengthUnits;
import org.freeplane.core.util.Quantity;
import org.freeplane.features.edge.EdgeStyle;
import org.freeplane.features.filter.condition.ICondition;
import org.freeplane.features.link.ArrowType;
import org.freeplane.features.styles.IStyle;

import groovy.lang.Closure;

/**
 * This interface alone defines the api for accessing the internal state of the Freeplane. All read-write methods
 * and properties (with rare, documented exceptions in {@link Controller} and {@link Map}) support undo and
 * rollback on exceptions.
 * <p>
 * Every Proxy subinterface comes in two variants:
 * <ul>
 * <li>A read-only interface, like {@link NodeRO}. This collects only the methods that don't change the
 *     underlying object (in case of <code>NodeRO</code> this would be <code>NodeModel</code>.
 * <li>A read-write interface, like {@link Node}. This inherits from the respective read-only interface all its
 *     methods and properties and adds write access to the underlying object.
 * </ul>
 * The main point of this distinction are formulas: <em>Only the methods defined in the read-only interfaces are
 * supported in Formulas!</em>. Changing values in a Formula are against the Formula concept and lead to corruption
 * of the caching mechanism for Formulas.
 */
public interface Proxy {
	interface AttributesRO extends org.freeplane.api.AttributesRO{
		/**
		 * returns the values of all attributes for which the closure returns true. The fact that the values are
		 * returned as a list of {@link Convertible} enables conversion. The following formula sums all attributes
		 * whose names are not equal to 'TOTAL':
		 * <pre>{@code
		 *  = attributes.findValues{key, val -> key != 'TOTAL'}.sum(0){it.num0}
		 * }</pre>
		 * @param closure A closure that accepts two arguments (String key, Object value) and returns boolean/Boolean.
		 * @return the values of all attributes for which the closure returns true.
		 * @since 1.2 */
		List<? extends Convertible> findValues(Closure<Boolean> closure);
	}
	interface Attributes extends AttributesRO, org.freeplane.api.Attributes { }
    interface Cloud extends org.freeplane.api.Cloud { }

	interface ConnectorRO extends org.freeplane.api.ConnectorRO {
		/**
		 * @return the end {@link ArrowType}
		 * @deprecated since 1.2 - use {@link #hasEndArrow()} instead */
		@Deprecated
		ArrowType getEndArrow();

		/**
		 * @return the start {@link ArrowType}
		 * @deprecated since 1.2 - use {@link #hasStartArrow()} instead */
		@Deprecated
		ArrowType getStartArrow();
	}

	interface Connector extends ConnectorRO, org.freeplane.api.Connector {
	    /**
			 * @param arrowType type of arrow
			 * @deprecated since 1.2 - use {@link #setEndArrow(boolean)} instead */
		@Deprecated
		void setEndArrow(ArrowType arrowType);

	    /**
			 * @param arrowType type of arrow
			 * @deprecated since 1.2 - use {@link #setStartArrow(boolean)} instead */
		@Deprecated
		void setStartArrow(ArrowType arrowType);
	}

	interface ControllerRO extends org.freeplane.api.ControllerRO {
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
		List<? extends org.freeplane.api.Node> find(Closure<Boolean> closure);

		/** Starting from root node, recursively searches for nodes for which <code>condition.check(node)</code>
		 * returns true and adds their ancestor or descendant nodes if required.
		 *
		 * @since 1.7.4
		 *
		 * See {@link Controller#find(NodeCondition)} for details. */

		List<? extends org.freeplane.api.Node> find(boolean withAncestors, boolean withDescendants, Closure<Boolean> closure);

		/** Starting from the root node, recursively searches for nodes for which
		 * <code>condition.checkNode(node)</code> returns true.
		 * @param condition condition to match the search.
		 * @return the nodes which match the condition.
		 * @deprecated since 1.2 use {@link #find(NodeCondition)} instead. */
		@Deprecated
		List<? extends org.freeplane.api.Node> find(ICondition condition);

	}

	interface Controller extends ControllerRO , org.freeplane.api.Controller{
		@Override
		org.freeplane.api.Node getSelected();

		@Override
		List<? extends org.freeplane.api.Node> getSelecteds();

		@Override
		List<? extends org.freeplane.api.Node> getSortedSelection(boolean differentSubtrees);

		@Override
		FreeplaneVersion getFreeplaneVersion();

		@Override
		File getUserDirectory();

		@Override
		List<? extends org.freeplane.api.Node> find(NodeCondition condition);

		@Override
		List<? extends org.freeplane.api.Node> find(boolean withAncestors, boolean withDescendants, NodeCondition condition);

		@Override
		List<? extends org.freeplane.api.Node> findAll();

		@Override
		List<? extends org.freeplane.api.Node> findAllDepthFirst();

		@Override
		float getZoom();

		@Override
		boolean isInteractive();

		@Override
		List<String> getExportTypeDescriptions();

		@Override
		void export(org.freeplane.api.Map map, File destinationFile, String exportTypeDescription,
					boolean overwriteExisting);

		@Override
		Proxy.Loader mapLoader(File file);

		@Override
		Proxy.Loader mapLoader(URL file);

		@Override
		Proxy.Loader mapLoader(String file);
	}

	interface Loader extends org.freeplane.api.Loader{
		@Override
		Proxy.Map load();
	}


	interface EdgeRO extends org.freeplane.api.EdgeRO {
		@Override
		EdgeStyle getType();
	}

	interface Edge extends EdgeRO, org.freeplane.api.Edge {
		void setType(EdgeStyle type);
	}

	interface ExternalObjectRO extends org.freeplane.api.ExternalObjectRO { }

	interface ExternalObject extends ExternalObjectRO, org.freeplane.api.ExternalObject { }

	interface FontRO extends org.freeplane.api.FontRO { }

	interface Font extends FontRO, org.freeplane.api.Font { }

	interface IconsRO extends org.freeplane.api.IconsRO {
	}

	interface Icons extends IconsRO, org.freeplane.api.Icons { }

	interface LinkRO extends org.freeplane.api.LinkRO { }

	interface Link extends LinkRO, org.freeplane.api.Link { }

	interface MapRO extends org.freeplane.api.MapRO {	}

	interface Map extends MapRO , org.freeplane.api.Map {	}

	interface NodeRO extends org.freeplane.api.NodeRO {

		/** Starting from this node, recursively searches for nodes for which <code>closure.call(node)</code>
		 * returns true.
		 *
		 * See {@link Controller#find(Closure)} for details. */
		List<? extends org.freeplane.api.Node> find(Closure<Boolean> closure);

		/** Starting from this node, recursively searches for nodes for which <code>closure.call(node)</code>
		 * returns true.
		 *
		 * @since 1.7.4
		 *
		 * See {@link Controller#find(Closure)} for details. */
		List<? extends org.freeplane.api.Node> find(boolean withAncestors, boolean withDescendants, Closure<Boolean> closure);

		/** Starting from this node, recursively searches for nodes for which
		 * <code>condition.checkNode(node)</code> returns true.
		 *
		 * @deprecated since 1.2 use {@link #find(NodeCondition)} instead. */
		@Deprecated
		List<? extends org.freeplane.api.Node> find(ICondition condition);

	}

	interface Node extends NodeRO, org.freeplane.api.Node {

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

	    /**@since 1.5.6 */
		void setVerticalShift(Quantity<LengthUnits> verticalShift);

	    /**@since 1.5.6 */
		void setMinimalDistanceBetweenChildren(Quantity<LengthUnits> verticalShift);

	    /**@since 1.5.6 */
		void setHorizontalShift(Quantity<LengthUnits> verticalShift);


	}

	interface NodeStyleRO extends org.freeplane.api.NodeStyleRO {
		IStyle getStyle();
	}

	interface NodeStyle extends NodeStyleRO, org.freeplane.api.NodeStyle {
		void setStyle(IStyle style);

	    /** Set to null to restore default
	     * @since 1.5.6 */
	    void setMinNodeWidth(Quantity<LengthUnits> width);

	    /** Set to null to restore default
	     * @since 1.5.6 */
	    void setMaxNodeWidth(Quantity<LengthUnits> width);
	}

    public interface Properties extends org.freeplane.api.Properties { }
    interface ReminderRO extends org.freeplane.api.ReminderRO { }

    interface Reminder extends ReminderRO, org.freeplane.api.Reminder {

    }

    interface DependencyLookup extends org.freeplane.api.DependencyLookup {}
}
