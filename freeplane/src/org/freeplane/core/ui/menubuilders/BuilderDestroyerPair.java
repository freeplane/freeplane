package org.freeplane.core.ui.menubuilders;


public class BuilderDestroyerPair {
	enum VisitorType {
		BUILDER, DESTROYER
	};
	final private EntryVisitor[] visitors;
	public BuilderDestroyerPair(EntryVisitor builder, EntryVisitor destroyer) {
		visitors = new EntryVisitor[] { builder, destroyer };
	}

	public EntryVisitor get(VisitorType visitorType) {
		return visitors[visitorType.ordinal()];
	}

}
