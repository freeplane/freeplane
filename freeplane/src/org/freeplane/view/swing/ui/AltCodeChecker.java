package org.freeplane.view.swing.ui;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Recognizes characters typed using alt+1..alt+9
 * on numeric key pad.
 * @author Dimitry
 *
 */
public class AltCodeChecker {
	private static Set<Character> altCodes;
	static {
		altCodes = new HashSet<Character>(7);
		altCodes.addAll(Arrays.asList(
/* Alt + 7, 8, 9*/	'\u2022', '\u25d8', '\u25cb', 
/* Alt + 4, 5, 6*/	'\u2666', '\u2663', '\u2660', 
/* Alt + 1, 2, 3*/	'\u263a', '\u263b', '\u2665'));
	}
	static boolean isAltCode(char keyChar) {
		return altCodes.contains(keyChar);
	}
}