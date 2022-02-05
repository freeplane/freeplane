/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at
       http://www.apache.org/licenses/LICENSE-2.0
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.freeplane.core.ui.svgicons;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class ReplacingInputStream extends FilterInputStream {

    // while matching, this is where the bytes go.
    final int[] buf;
    private int matchedIndex;
    private int unbufferIndex;
    private int replacedIndex;

    private final byte[] pattern;
    private final byte[] replacement;
    private State state=State.NOT_MATCHED;

    // simple state machine for keeping track of what we are doing
    private enum State {
        NOT_MATCHED,
        MATCHING,
        REPLACING,
        UNBUFFER
    }

    /**
     * Replace occurrences of pattern in the input. Note: input is assumed to be UTF-8 encoded. If not the case use byte[] based pattern and replacement.
     * @param in input
     * @param pattern pattern to replace.
     * @param replacement the replacement or null
     */
    public ReplacingInputStream(InputStream in, String pattern, String replacement) {
        this(in, pattern.getBytes(UTF_8), replacement==null ? null : replacement.getBytes(UTF_8));
    }

    /**
     * Replace occurrences of pattern in the input.<p>
     *
     * If you want to normalize line endings DOS/MAC (\n\r | \r) to UNIX (\n), you can call the following:<br>
     * {@code new ReplacingInputStream(new ReplacingInputStream(is, "\n\r", "\n"), "\r", "\n")}
     *
     * @param in input
     * @param pattern pattern to replace
     * @param replacement the replacement or null
     */
    public ReplacingInputStream(InputStream in, byte[] pattern, byte[] replacement) {
        super(in);
        if (pattern == null || pattern.length == 0) {
            throw new IllegalArgumentException("pattern length should be > 0");
        }
        this.pattern = pattern;
        this.replacement = replacement;
        // we will never match more than the pattern length
        buf = new int[pattern.length];
    }
    
    public ReplacingInputStream and(String pattern, String replacement) {
    	return new ReplacingInputStream(this, pattern, replacement);
    }
    
    public static InputStream  replace(InputStream in, String replacementSpecification) {
    	String[]replacements = replacementSpecification.split(replacementSpecification.substring(0, 1));
    	if(replacements.length < 2)
    		return in;
    	ReplacingInputStream replacingStream = new ReplacingInputStream(in, replacements[1], 
    			replacements.length > 2 ? replacements[2] : "");
    	for(int i = 3; i < replacements.length ; i+=2) {
    		String pattern = replacements[i];
    		if(! pattern.isEmpty())
    			replacingStream = replacingStream.and(pattern, replacements.length > i + 1 ? replacements[i + 1] : "");
    	}
    	return replacingStream;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        // copy of parent logic; we need to call our own read() instead of super.read(), which delegates instead of calling our read
        if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        }

        int c = read();
        if (c == -1) {
            return -1;
        }
        b[off] = (byte)c;

        int i = 1;
        for (; i < len ; i++) {
            c = read();
            if (c == -1) {
                break;
            }
            b[off + i] = (byte)c;
        }
        return i;

    }

    @Override
    public int read(byte[] b) throws IOException {
        // call our own read
        return read(b, 0, b.length);
    }

    @Override
    public int read() throws IOException {
        // use a simple state machine to figure out what we are doing
        int next;
        switch (state) {
        default:
        case NOT_MATCHED:
            // we are not currently matching, replacing, or unbuffering
            next=super.read();
            if (pattern[0] != next) {
                return next;
            }

            // clear whatever was there
            Arrays.fill(buf, 0);
            // make sure we start at 0
            matchedIndex=0;

            buf[matchedIndex++]=next;
            if (pattern.length == 1) {
                // edge-case when the pattern length is 1 we go straight to replacing
                state=State.REPLACING;
                // reset replace counter
                replacedIndex=0;
            } else {
                // pattern of length 1
                state=State.MATCHING;
            }
            // recurse to continue matching
            return read();

        case MATCHING:
            // the previous bytes matched part of the pattern
            next=super.read();
            if (pattern[matchedIndex]==next) {
                buf[matchedIndex++]=next;
                if (matchedIndex==pattern.length) {
                    // we've found a full match!
                    if (replacement==null || replacement.length==0) {
                        // the replacement is empty, go straight to NOT_MATCHED
                        state=State.NOT_MATCHED;
                        matchedIndex=0;
                    } else {
                        // start replacing
                        state=State.REPLACING;
                        replacedIndex=0;
                    }
                }
            } else {
                // mismatch -> unbuffer
                buf[matchedIndex++]=next;
                state=State.UNBUFFER;
                unbufferIndex=0;
            }
            return read();

        case REPLACING:
            // we've fully matched the pattern and are returning bytes from the replacement
            next=replacement[replacedIndex++];
            if (replacedIndex==replacement.length) {
                state=State.NOT_MATCHED;
                replacedIndex=0;
            }
            return next;

        case UNBUFFER:
            // we partially matched the pattern before encountering a non matching byte
            // we need to serve up the buffered bytes before we go back to NOT_MATCHED
            next=buf[unbufferIndex++];
            if (unbufferIndex==matchedIndex) {
                state=State.NOT_MATCHED;
                matchedIndex=0;
            }
            return next;
        }
    }

    @Override
    public String toString() {
        return state.name() + " " + matchedIndex + " " + replacedIndex + " " + unbufferIndex;
    }

}