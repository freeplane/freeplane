package org.freeplane.api;

import java.util.Map;

/**
 * Calculates dependencies related to node value or attribute.
 *
 * @since 1.7.2
 */
public interface DependencyLookup {
	/**
	 * Calculates dependencies related to node value.
	 * @since 1.7.2
	 */
	Map <? extends NodeRO, Dependencies> ofNode();

	/**
	 * Calculates dependencies related to attribute given by index.
	 * @since 1.7.2
	 */
	Map <? extends NodeRO, Dependencies> ofAttribute(int attributeIndex);

	/**
	 * Calculates dependencies related to first attribute with given name.
	 * @since 1.7.2
	 */
	Map <? extends NodeRO, Dependencies> ofAttribute(String attributeName);
}
