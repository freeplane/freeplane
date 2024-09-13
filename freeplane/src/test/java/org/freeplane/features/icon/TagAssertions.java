/*
 * Created on 13 Sept 2024
 *
 * author dimitry
 */
package org.freeplane.features.icon;

import java.util.Collection;

import org.assertj.core.api.AbstractCollectionAssert;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ObjectAssert;

public class TagAssertions {
    public static AbstractCollectionAssert<?, Collection<? extends Tag>, Tag, ObjectAssert<Tag>> assertThatReferencedTags(TagCategories tc) {
        return Assertions.assertThat(tc.referencedTags());
    }
}
