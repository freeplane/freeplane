package org.freeplane.core.io;

import org.freeplane.api.LengthUnit;
import org.freeplane.api.Quantity;
import org.freeplane.features.map.MapWriter;

public class BackwardCompatibleQuantityWriter {

	final private ITreeWriter writer;
	final private boolean makeCompatible;
	
	static public BackwardCompatibleQuantityWriter forWriter(ITreeWriter writer){
		final Object hint = writer.getHint(BackwardCompatibleQuantityWriter.class);
		if(Boolean.FALSE.equals(hint)){
			final boolean makeCompatible = Boolean.TRUE.equals(writer.getHint(MapWriter.WriterHint.FORCE_FORMATTING));
			final BackwardCompatibleQuantityWriter quantityWriter = new BackwardCompatibleQuantityWriter(writer, makeCompatible);
			writer.setHint(BackwardCompatibleQuantityWriter.class, quantityWriter);
			return quantityWriter;
		}
		return (BackwardCompatibleQuantityWriter) hint;
	}

	public BackwardCompatibleQuantityWriter(ITreeWriter writer, boolean makeCompatible) {
		this.writer = writer;
		this.makeCompatible = makeCompatible;
	}

	public void writeQuantity(String name, final Quantity<LengthUnit> value) {
		if(makeCompatible){
			writer.addAttribute(name, value.toBaseUnitsRounded());
			writer.addAttribute(name+"_QUANTITY", value.toString());
		}
		else
			writer.addAttribute(name, value.toString());
	}

}
