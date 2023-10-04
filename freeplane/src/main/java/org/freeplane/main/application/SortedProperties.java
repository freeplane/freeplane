/*
 * Created on 16 Sept 2023
 *
 * author dimitry
 */
package org.freeplane.main.application;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
/* As suggested in https://stackoverflow.com/a/39043903/1833472 */
class SortedProperties extends Properties {
    private static class StripFirstLineStream extends FilterOutputStream {

        private boolean firstlineseen = false;

        public StripFirstLineStream(final OutputStream out) {
            super(out);
        }

        @Override
        public void write(final int b) throws IOException {
            if (firstlineseen) {
                super.write(b);
            } else if (b == '\n') {
                firstlineseen = true;
            }
        }

    }

    private static final long serialVersionUID = 7567765340218227372L;

    SortedProperties(Properties defaults) {
        super(defaults);
    }

    @Override
    public synchronized Enumeration<Object> keys() {
        return Collections.enumeration(new TreeSet<>(super.keySet()));
    }



    @Override
    public Set<Entry<Object, Object>> entrySet() {
        @SuppressWarnings("unchecked")
        TreeSet<java.util.Map.Entry<Object, Object>> treeSet = new TreeSet<Entry<Object, Object>>(
                Comparator.comparing(e -> ((Comparable<Object>)e.getKey())));
        treeSet.addAll(super.entrySet());
        return treeSet;
    }

    @Override
    public void store(final OutputStream out, final String comments) throws IOException {
        super.store(new StripFirstLineStream(out), null);
    }
}