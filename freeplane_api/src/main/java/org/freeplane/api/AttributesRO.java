package org.freeplane.api;

import java.util.List;

/** Node's attribute table: <code>node.attributes</code> - read-only.
 * <p>
 * Attributes are name - value pairs assigned to a node. A node may have multiple attributes
 * with the same name.
 */
public interface AttributesRO {
	/** alias for {@link #getFirst(String)}.
	 * @deprecated before 1.1 - use {@link #get(int)}, {@link #getFirst(String)} or {@link #getAll(String)} instead. */
	@Deprecated
	Object get(final String name);

	/** returns the <em>first</em> value of an attribute with the given name or null otherwise.
	 * @since 1.2 */
	Object getFirst(final String name);

	/** @return true if there is any attribute with key <a>name</a>.
	 * @since 1.4 */
	boolean containsKey(final String name);

	/** @return all values for the attribute name. */
	List<Object> getAll(final String name);

	/** returns all attribute names in the proper sequence. The number of names returned
	 * is equal to the number of attributes.
	 * <pre>
	 *   // rename attribute
	 *   int i = 0;
	 *   for (String name : attributes.getNames()) {
	 *       if (name.equals("xy"))
	 *           attributes.set(i, "xyz", attributes.get(i));
	 *       ++i;
	 *   }
	 * </pre> */
	List<String> getNames();

	/** @deprecated since 1.2 use #getNames() instead. */
	@Deprecated
	List<String> getAttributeNames();

	/** returns all values as a list of {@link Convertible}.
	 * @since 1.2 */
	List<? extends Convertible> getValues();

	/** returns all attributes as a map. Note that this will erase duplicate keys.
	 * <code>node.attributes = otherNode.attributes.map</code>
	 * @since 1.2 */
	java.util.Map<String, Object> getMap();

	/** returns the attribute value at the given index.
	 * @throws IndexOutOfBoundsException if index is out of range, i. e. {@code index < 0 || index >= size()}.*/
	Object get(final int index);

	/** returns the attribute key at the given index.
	 * @throws IndexOutOfBoundsException if index is out of range, i. e. {@code index < 0 || index >= size()}.*/
	String getKey(final int index);

	/** @deprecated since 1.2 - use {@link #findFirst(String)} instead. */
	@Deprecated
	int findAttribute(final String name);

	/** returns the index of the first attribute with the given name if one exists or -1 otherwise.
	 * For searches for <em>all</em> attributes with a given name <code>getAttributeNames()</code>
	 * must be used.
	 * @since 1.2*/
	int findFirst(final String name);

	/** returns the values of all attributes for which the closure returns true. The fact that the values are
	 * returned as a list of {@link Convertible} enables conversion. The following formula sums all attributes
	 * whose names are not equal to 'TOTAL':
	 * <pre>{@code
	 *  = attributes.findValues{key, val -> key != 'TOTAL'}.sum(0){it.num0}
	 * }</pre>
	 * @param condition A closure that accepts two arguments (String key, Object value) and returns boolean/Boolean.
	 * @since 1.2 */
	List<? extends Convertible> findValues(AttributeCondition condition);

	/** the number of attributes. It is <code>size() == getAttributeNames().size()</code>. */
	int size();

	/** returns <code>getAttributeNames().isEmpty()</code>.
     * @since 1.2 */
	boolean isEmpty();

	/** @since 1.7.1 */
	Attributes getTransformed();
}