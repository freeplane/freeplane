package org.pushingpixels.flamingo.api.ribbon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.pushingpixels.flamingo.api.ribbon.resize.CoreRibbonResizePolicies;
import org.pushingpixels.flamingo.api.ribbon.resize.RibbonBandResizePolicy;

public class MutableRibbonTask extends RibbonTask {

	/**
	 * List of all bands.
	 */
	private ArrayList<AbstractRibbonBand<?>> bands;
	
	public MutableRibbonTask(String title) {
		super(title, new DummyRibbonBand());
		this.bands = new ArrayList<AbstractRibbonBand<?>>();
	}

	@Override
	public int getBandCount() {
		return this.bands.size();
	}

	@Override
	public AbstractRibbonBand<?> getBand(int index) {
		return this.bands.get(index);
	}

	@Override
	public List<AbstractRibbonBand<?>> getBands() {
		return Collections.unmodifiableList(this.bands);
	}
	
	public void addBand(AbstractRibbonBand<?> band) {
		if(band != null) {
			band.setRibbonTask(this);
			this.bands.add(band);
			fireTaskChanged();
		}
	}

	private void fireTaskChanged() {
		this.setTitle(getTitle());
	}
}

class DummyRibbonBand extends JRibbonBand {

	private static final long serialVersionUID = 5078526271057446504L;

	public DummyRibbonBand() {
		super("dummy", null);
		List<RibbonBandResizePolicy> policies = new ArrayList<>();
		policies.add(new CoreRibbonResizePolicies.None(this.getControlPanel()));
		setResizePolicies(policies);
	}
	
}