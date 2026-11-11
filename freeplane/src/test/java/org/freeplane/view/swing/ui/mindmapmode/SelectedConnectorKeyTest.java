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
	public void deleteRemovesConnector() {
		assertThat(SelectedConnectorKey.of(KeyEvent.VK_DELETE, 0)).isEqualTo(SelectedConnectorKey.REMOVE);
	}

	@Test
	public void deleteOrEscapeWithModifierDeselectsBeforeTheirUsualAction() {
		assertThat(SelectedConnectorKey.of(KeyEvent.VK_DELETE, InputEvent.CTRL_DOWN_MASK)).isEqualTo(SelectedConnectorKey.DESELECT_AND_PASS);
		assertThat(SelectedConnectorKey.of(KeyEvent.VK_ESCAPE, InputEvent.SHIFT_DOWN_MASK)).isEqualTo(SelectedConnectorKey.DESELECT_AND_PASS);
	}

	@Test
	public void otherKeysDeselectBeforeTheirUsualAction() {
		assertThat(SelectedConnectorKey.of(KeyEvent.VK_DOWN, 0)).isEqualTo(SelectedConnectorKey.DESELECT_AND_PASS);
		assertThat(SelectedConnectorKey.of(KeyEvent.VK_A, 0)).isEqualTo(SelectedConnectorKey.DESELECT_AND_PASS);
		assertThat(SelectedConnectorKey.of(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK)).isEqualTo(SelectedConnectorKey.DESELECT_AND_PASS);
	}

	@Test
	public void modifierKeysAloneKeepConnectorSelected() {
		assertThat(SelectedConnectorKey.of(KeyEvent.VK_SHIFT, InputEvent.SHIFT_DOWN_MASK)).isEqualTo(SelectedConnectorKey.IGNORE);
		assertThat(SelectedConnectorKey.of(KeyEvent.VK_CONTROL, InputEvent.CTRL_DOWN_MASK)).isEqualTo(SelectedConnectorKey.IGNORE);
	}
}
