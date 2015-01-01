package org.freeplane.core.util;

import static org.freeplane.core.util.HtmlUtils.join;
import static org.junit.Assert.*;

import org.junit.Test;

public class HtmlUtilsTest {
    private final String text1 = "foo";
    private final String text2 = "bla";

    @Test
    public void plainToHTML_wraps_text_in_paragraph() {
        // plainToHTML is used in HtmlUtils.join() which assumes that text is wrapped in <p>
        assertEquals(body(e("p", "a") + '\n' + e("p", "b")), HtmlUtils.plainToHTML("a\nb"));
    }
    @Test
    public void plainToHTML_creates_two_paragraphs_from_one_newline() {
        assertEquals(body(e("p", "") + '\n' + e("p", "")), HtmlUtils.plainToHTML("\n"));
    }

    @Test
    public void testJoin() {
        assertEquals(body(e("p", text1) + '\n' + e("p", text2)), join(text1, text2));
        assertEquals(body(text1 + '\n' + e("p", text2)), join(body(text1), text2));
        assertEquals(body(e("p", text1) + '\n' + text2), join(text1, body(text2)));
        assertEquals(body(text1 + '\n' + text2), join(body(text1), body(text2)));
    }

    @Test
    public void use_empty_strings_to_inject_paragraphs() {
        assertEquals(body(text1 + "\n<p></p>\n" + text2), join(body(text1), "", body(text2)));
    }
    
    @Test
    public void use_newline_to_inject_two_paragraphs() {
        assertEquals(body(text1 + "\n<p></p>\n<p></p>\n" + text2), join(body(text1), "\n", body(text2)));
    }
    
    private String body(String body) {
        return e("html", e("body", body));
    }

    private String e(String tag, String body) {
        return String.format("<%s>%s</%s>", tag, body, tag);
    }
}
