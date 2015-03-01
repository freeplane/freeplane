package org.freeplane.core.ui.menubuilders;

import org.freeplane.main.headlessmode.FreeplaneHeadlessStarter;

public class HeadlessFreeplaneRunner {
	static {
		new FreeplaneHeadlessStarter().run(new String[]{});
	}
}
