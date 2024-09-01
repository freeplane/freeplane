/*
 * Created on 5 Feb 2024
 *
 * author dimitry
 */
package org.freeplane.plugin.codeexplorer.map;

import org.freeplane.features.attribute.ManagedAttribute;

import com.tngtech.archunit.core.domain.JavaEnumConstant;

class AnnotationAttribute extends ManagedAttribute{
    AnnotationAttribute(String name, Object value) {
        super(name, unwrap(value));
    }

    public static Object unwrap(Object value) {
        return (value instanceof JavaEnumConstant) ? ((JavaEnumConstant)value).name() : value;
    }
}
