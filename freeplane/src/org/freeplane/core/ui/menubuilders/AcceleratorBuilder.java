package org.freeplane.core.ui.menubuilders;

import org.freeplane.core.util.Compat;

public class AcceleratorBuilder implements Builder{

	private static final String ACCELERATOR = "accelerator";
	private IDefaultAcceleratorMap map;

	public AcceleratorBuilder(IDefaultAcceleratorMap map) {
		this.map = map;
	}

	public void build(Entry entry) {
		if(entry.getAction() != null){
			String accelerator = (String) entry.getAttribute(ACCELERATOR);
			if(accelerator != null) {
				if (isMacOsX()) {
			        accelerator = accelerator.replaceFirst("CONTROL", "META").replaceFirst("control", "meta");
			    }
				map.setDefaultAccelerator(entry.getName(), accelerator);
			}
		}
	}

	protected boolean isMacOsX() {
		return Compat.isMacOsX();
	}

	@Override
	public void destroy(Entry target) {
		// TODO Auto-generated method stub
		
	}

}
