package org.freeplane.api;

public interface Properties {
    /** Provides map-like access to properties. Note that the returned type is a
     * {@link Convertible}, not a String as in the basic storage. Nevertheless it behaves like a String in almost
     * all respects, that is, in Groovy scripts it understands all String methods like lenght(), matches() etc.
     * <br>
     * Note that unlike Attributes.getAt() this method will return <em>null</em> if the property is not set!
     * @since 1.3.6 */
    Convertible getAt(String key);

    /**
     * Allows to set and to change properties.
     * @param value An object for conversion to String. Works well for all types that {@link Convertible}
     *        handles, particularly {@link Convertible}s itself. Use null to unset an attribute.
     * @return the new value
     * @since 1.3.6 */
    Convertible putAt(String key, Object value);

    /** returns the names of all attributes.
     * @since 1.3.6 */
    java.util.Set<String> keySet();
}