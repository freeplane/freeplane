package org.freeplane.api;

import java.util.Iterator;
import java.util.Map.Entry;

/** Node's attribute table: <code>node.attributes</code> - read-write.
 * <p>
 * <b>Notes on attribute setters:</b><ul>
 * <li> All setter methods try to convert strings to dates, numbers or URIs.
 * <li> All setter methods apply a default formatting (for display) of the value for dates and numbers.
 * <li> Attributes don't have style properties so the value objects must know about the right formatting for
 *      themselves.
 * <li> To enforce a certain formatting use format(): <pre>node['creationDate'] = format(new Date(), 'MM/yyyy')</pre>
 * </ul>
 * <p>
 * <b>Examples:</b>
 * <pre>
 *   // == text
 *   node["attribute name"] = "a value"
 *   assert node["attribute name"].text == "a value"
 *   assert node.attributes.getFirst("attribute name") == "a value" // the same
 *
 *   // == numbers and others
 *   // converts numbers and other stuff with toString()
 *   node["a number"] = 1.2
 *   assert node["a number"].text == "1.2"
 *   assert node["a number"].num == 1.2d
 *
 *     *   // == dates
 *   def date = new Date()
 *   node["a date"] = date
 *   assert node["a date"].object.getClass().simpleName == "FormattedDate"
 *   assert node["a date"].date == format(date)
 *
 *   // == enforce formats on attribute values
 *   node["another date"] = format(date, 'yyyy|MM|dd')
 *   assert node["another date"].date == format(date, 'yyyy|MM|dd')
 *
 *   // change the date while keeping the silly format
 *   def index = node.attributes.findAttribute("another date")
 *   node.attributes.set(index, new Date(0L))
 *
 *   // == URIs
 *   def uri = new URI("http://www.freeplane.org")
 *   node["uri"] = uri
 *   assert node["uri"].object.getClass().simpleName == "URI"
 *   assert node["uri"].object == uri
 *
 *   // == remove an attribute
 *   node["removed attribute"] = "to be removed"
 *   assert node["removed attribute"] == "to be removed"
 *   node["removed attribute"] = null
 *   assert node.attributes.findFirst("removed attribute") == -1
 * </pre>
 */
public interface Attributes extends AttributesRO {
	/** sets the value of the attribute at an index. This method will not create new attributes.
	 * @throws IndexOutOfBoundsException if index is out of range, i. e. {@code index < 0 || index >= size()}.*/
	void set(final int index, final Object value);

	/** sets name and value of the attribute at the given index. This method will not create new attributes.
	 * @throws IndexOutOfBoundsException if index is out of range, i. e. {@code index < 0 || index >= size()}.*/
	void set(final int index, final String name, final Object value);

	/** sets name, value and format pattern of the attribute at the given index. This method will not create new attributes.
	 * @throws IndexOutOfBoundsException if index is out of range, i. e. {@code index < 0 || index >= size()}.*/
	public void set(final int index, final String name, final Object value, String format);

	/** sets format pattern to  the attribute at the given index.
	 * @throws IndexOutOfBoundsException if index is out of range, i. e. {@code index < 0 || index >= size()}.*/
	public void setFormat(final int index, String format);

	/** removes the <em>first</em> attribute with this name.
	 * @return true on removal of an existing attribute and false otherwise.
	 * @deprecated before 1.1 - use {@link #remove(int)} or {@link #removeAll(String)} instead. */
	@Deprecated
	boolean remove(final String name);

	/** removes <em>all</em> attributes with this name.
	 * @return true on removal of an existing attribute and false otherwise. */
	boolean removeAll(final String name);

	/** removes the attribute at the given index.
	 * @throws IndexOutOfBoundsException if index is out of range, i. e. {@code index < 0 || index >= size()}.*/
	void remove(final int index);

	/** adds an attribute if there is no attribute with the given name or changes
	 * the value <em>of the first</em> attribute with the given name. */
	void set(final String name, final Object value);

	/** adds an attribute no matter if an attribute with the given name already exists. */
	void add(final String name, final Object value);

	/** adds an attribute with formatting pattern no matter if an attribute with the given name already exists. */
	public void add(final String name, final Object value, String format);

	/** removes all attributes.
	 * @since 1.2 */
	void clear();

	/** allows application of Groovy collection methods like each(), collect(), ...
	 * <pre>
	 *   def keyList = node.attributes.collect { it.key }
     *   def values = node.attributes.collect { it.value }
     *   node.attributes.each {
     *       if (it.key =~ /.*day/)
     *           it.value += ' days'
     *   }
	 * </pre>
	 * @since 1.3.2 */
	Iterator<java.util.Map.Entry<String, Object>> iterator();

	/** optimize widths of attribute view columns according to contents.
	 * @since 1.4 */
	void optimizeWidths();
}