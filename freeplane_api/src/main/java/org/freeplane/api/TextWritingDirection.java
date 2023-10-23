/*
 * Created on 2 Oct 2023
 *
 * author dimitry
 */
package org.freeplane.api;

import java.awt.ComponentOrientation;

public enum TextWritingDirection {
    LEFT_TO_RIGHT(ComponentOrientation.LEFT_TO_RIGHT), RIGHT_TO_LEFT(ComponentOrientation.RIGHT_TO_LEFT);

    public final ComponentOrientation componentOrientation;



    private TextWritingDirection(ComponentOrientation componentOrientation) {
        this.componentOrientation = componentOrientation;
    }

    public static final TextWritingDirection DEFAULT = LEFT_TO_RIGHT;
}
