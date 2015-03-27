package org.freeplane.core.ui.menubuilders.generic;



public class BuilderDestroyerPair {
	enum VisitorType {
		BUILDER, DESTROYER
	};
	final private EntryVisitor[] visitors;
	public BuilderDestroyerPair(EntryVisitor builder, EntryVisitor destroyer) {
		visitors = new EntryVisitor[] { builder, destroyer };
	}

	public BuilderDestroyerPair(EntryVisitor builder) {
		this(builder, EntryVisitor.ILLEGAL);
	}

	public EntryVisitor get(VisitorType visitorType) {
		return visitors[visitorType.ordinal()];
	}

}
