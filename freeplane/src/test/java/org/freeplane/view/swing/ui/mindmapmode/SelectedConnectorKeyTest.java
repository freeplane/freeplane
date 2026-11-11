package org.freeplane.view.swing.ui.mindmapmode;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import org.junit.Test;

public class SelectedConnectorKeyTest {
	@Test
	public void escapeDeselectsConnector() {
		assertThat(SelectedConnectorKey.of(KeyEvent.VK_ESCAPE, 0)).isEqualTo(SelectedConnectorKey.DESELECT);
	}

	@Test
	public void escapeWithModifierIsIgnored() {
		assertThat(SelectedConnectorKey.of(KeyEvent.VK_ESCAPE, InputEvent.SHIFT_DOWN_MASK)).isEqualTo(SelectedConnectorKey.IGNORE);
	}
}
