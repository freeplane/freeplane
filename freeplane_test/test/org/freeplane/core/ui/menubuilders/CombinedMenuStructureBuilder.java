package org.freeplane.core.ui.menubuilders;

import java.util.HashMap;

public class CombinedMenuStructureBuilder implements Builder{

	final private HashMap<String, Builder> builders = new HashMap<String, Builder>();

	public void addBuilder(String name, Builder builder) {
		builders.put(name, builder);
	}

	@Override
	public void build(Entry target) {
		for(String builderName :  target.builders())
			builders.get(builderName).build(target);
	}

}
