package org.freeplane.api;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

public enum Dash {
	SOLID(new int[] {}),
	CLOSE_DOTS(new int[]{3, 3}),
	DASHES(new int[]{7, 7}),
	DISTANT_DOTS(new int[]{2, 7}),
	DOTS_AND_DASHES(new int[]{2, 7, 7, 7});

	public static Dash DEFAULT = Dash.SOLID;
	public final int[] pattern;

	static public Optional<Dash> of(int[] pattern) {
	    return Stream.of(values())
	        .filter(self -> Arrays.equals(self.pattern, pattern))
	        .findAny();
	}

	private Dash(int[] variant) {
		this.pattern = variant;
	}
}
