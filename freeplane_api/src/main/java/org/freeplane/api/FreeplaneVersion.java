package org.freeplane.api;

public interface FreeplaneVersion extends Comparable<FreeplaneVersion>{

	boolean isFinal();

	boolean isNewerThan(FreeplaneVersion freeplaneVersion);

	boolean isOlderThan(FreeplaneVersion freeplaneVersion);

	String numberToString();

	String getRevision();

	String getType();

	int getNum();

	int getMin();

	int getMid();

	int getMaj();
}
