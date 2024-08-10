/*
 * Created on 2 Oct 2023
 *
 * author dimitry
 */
package org.freeplane.api;

import java.awt.ComponentOrientation;

public enum TextWritingDirection {
    LEFT_TO_RIGHT(ComponentOrientation.LEFT_TO_RIGHT, '\u202d', '\u202a', '\u2066', '\u200e' ),
    RIGHT_TO_LEFT(ComponentOrientation.RIGHT_TO_LEFT, '\u202e', '\u202b', '\u2067', '\u200f' );

    private final static char popDirection = '\u202c';
    private final static char popIsolation = '\u2069';

    public final ComponentOrientation componentOrientation;
    private final char embedded;
    private final char overwritten;
    private final char isolated;
    private final char mark;



    private TextWritingDirection(ComponentOrientation componentOrientation, char overwritten, char embedded, char isolated, char mark) {
        this.componentOrientation = componentOrientation;
        this.overwritten = overwritten;
        this.embedded = embedded;
        this.isolated = isolated;
        this.mark = mark;
    }

    public static final TextWritingDirection DEFAULT = LEFT_TO_RIGHT;

    public String overwritten(String text) {
        return addControlCharacters(text, overwritten, popDirection);
    }
    public String embedded(String text) {
        return addControlCharacters(text, embedded, popDirection);
    }

    public String isolated(String text) {
        return addControlCharacters(text, isolated, popIsolation);
    }

    public String marked(String text) {
        return new StringBuilder(text.length() + 1)
                .append(mark)
                .append(text)
                .toString();
    }

    private String addControlCharacters(String text, char start, char end) {
        return new StringBuilder(text.length() + 3)
                .append(start)
                .append(text)
                .append(end)
                .append(mark)
                .toString();
    }
}
