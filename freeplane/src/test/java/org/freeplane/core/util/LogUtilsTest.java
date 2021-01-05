package org.freeplane.core.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class LogUtilsTest {
    @Test
    public void filtersExceptionMessages() throws Exception {
        assertTrue(LogUtils.matchesExceptionMessage(IllegalAccessException.class.getName()));
        assertFalse(LogUtils.matchesExceptionMessage("other message"));
        assertFalse(LogUtils.matchesExceptionMessage(" " + IllegalAccessException.class.getName()));
        assertTrue(LogUtils.matchesExceptionMessage(IllegalAccessException.class.getName() + ": message"));
        assertTrue(LogUtils.matchesExceptionMessage(IllegalAccessError.class.getName()));
        assertTrue(LogUtils.matchesExceptionMessage(IllegalAccessError.class.getName() + ": message"));
    }
}
