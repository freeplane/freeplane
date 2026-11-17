package org.freeplane.features.treetable;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class TreeTableColumnNamesTest {

	@Test
	public void numbersFirstColumnFromOne() {
		assertThat(TreeTableColumnNames.unusedName("Colonne", TreeTableColumns.empty())).isEqualTo("Colonne 1");
	}

	@Test
	public void numbersNextColumnAfterExistingOnes() {
		final TreeTableColumns columns = TreeTableColumns.of(new TreeTableColumn("Colonne 1"));

		assertThat(TreeTableColumnNames.unusedName("Colonne", columns)).isEqualTo("Colonne 2");
	}

	@Test
	public void skipsNamesAlreadyUsedByRenamedColumns() {
		final TreeTableColumns columns = TreeTableColumns.of(new TreeTableColumn("Colonne 2"));

		assertThat(TreeTableColumnNames.unusedName("Colonne", columns)).isEqualTo("Colonne 3");
	}
}
