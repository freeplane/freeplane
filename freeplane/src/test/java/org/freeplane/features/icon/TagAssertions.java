/*
 * Created on 13 Sept 2024
 *
 * author dimitry
 */
package org.freeplane.features.icon;

import java.util.Collection;

import org.assertj.core.api.AbstractCollectionAssert;
import org.assertj.core.api.AbstractStringAssert;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ObjectAssert;

public class TagAssertions {
    public static AbstractCollectionAssert<?, Collection<? extends Tag>, Tag, ObjectAssert<Tag>> assertThatReferencedTags(TagCategories tc) {
        return Assertions.assertThat(tc.referencedTags());
    }

    public static AbstractStringAssert<?> assertThatSerialized(TagCategories tc) {
        return Assertions.assertThat(serialized(tc));
    }

    public static AbstractStringAssert<?> assertThatSerializedWithoutColors(TagCategories tc) {
        return Assertions.assertThat(serializeWithoutColors(tc));
    }

    private static String serialized(TagCategories tc) {
        return tc.serialize().replace(System.lineSeparator(), "\n");
    }

    private static String serializeWithoutColors(TagCategories tagCategories) {
        return serialized(tagCategories).replaceAll("#.*", "");
    }

}
