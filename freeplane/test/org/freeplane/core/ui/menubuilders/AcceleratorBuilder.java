package org.freeplane.core.ui.menubuilders;

public class AcceleratorBuilder implements Builder{

	private static final String ACCELERATOR = "accelerator";
	private IDefaultAcceleratorMap map;

	public AcceleratorBuilder(IDefaultAcceleratorMap map) {
		this.map = map;
	}

	public void build(Entry entry) {
		if(entry.getAction() != null){
			String accelerator = (String) entry.getAttribute(ACCELERATOR);
			if(accelerator != null)
				map.setDefaultAccelerator(entry.getName(), accelerator);
		}
	}

}
