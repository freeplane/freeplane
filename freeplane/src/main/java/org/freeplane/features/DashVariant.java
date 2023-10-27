package org.freeplane.features;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

public enum DashVariant{
	SOLID(new int[] {}),
	CLOSE_DOTS(new int[]{3, 3}),
	DASHES(new int[]{7, 7}),
	DISTANT_DOTS(new int[]{2, 7}),
	DOTS_AND_DASHES(new int[]{2, 7, 7, 7});

	public static DashVariant DEFAULT = DashVariant.SOLID;
	public final int[] variant;

	static public Optional<DashVariant> of(int[] variant) {
	    return Stream.of(values())
	        .filter(self -> Arrays.equals(self.variant, variant))
	        .findAny();
	}

	private DashVariant(int[] variant) {
		this.variant = variant;
	}
}
