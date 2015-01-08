package org.freeplane.core.ui.menubuilders;

import java.util.HashMap;

public class CombinedMenuStructureBuilder implements Builder{

	final private HashMap<String, Builder> builders = new HashMap<String, Builder>();
	private Builder defaultBuilder = Builder.ILLEGAL_BUILDER;

	public void addBuilder(String name, Builder builder) {
		builders.put(name, builder);
	}

	@Override
	public void build(Entry target) {
		for(String builderName :  target.builders()) {
			final Builder explicitBuilder = builders.get(builderName);
			if(explicitBuilder != null){
				explicitBuilder.build(target);
				return;
			}
		}
		defaultBuilder.build(target);
	}

	public void setDefaultBuilder(Builder defaultBuilder) {
		this.defaultBuilder = defaultBuilder;
		
	}

}
