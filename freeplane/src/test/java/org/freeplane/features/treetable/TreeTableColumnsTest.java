package org.freeplane.features.treetable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.Test;

public class TreeTableColumnsTest {

	private static TreeTableColumn column(final String attributeKey) {
		return new TreeTableColumn(attributeKey);
	}

	@Test
	public void emptyColumnsHaveNoEntry() {
		assertThat(TreeTableColumns.empty().size()).isZero();
		assertThat(TreeTableColumns.empty().isEmpty()).isTrue();
	}

	@Test
	public void keepsGivenColumnOrder() {
		final TreeTableColumns columns = TreeTableColumns.of(column("Pour"), column("Contre"));

		assertThat(columns.get(0).getAttributeKey()).isEqualTo("Pour");
		assertThat(columns.get(1).getAttributeKey()).isEqualTo("Contre");
	}

	@Test
	public void rejectsDuplicateAttributeKeys() {
		assertThatThrownBy(() -> TreeTableColumns.of(column("Pour"), column("Pour")))
			.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public void rejectsBlankAttributeKey() {
		assertThatThrownBy(() -> column(" ")).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public void findsAttributeKey() {
		final TreeTableColumns columns = TreeTableColumns.of(column("Pour"), column("Contre"));

		assertThat(columns.containsAttributeKey("Contre")).isTrue();
		assertThat(columns.containsAttributeKey("Absente")).isFalse();
	}

	@Test
	public void acceptsRenamingToUnusedNonBlankKey() {
		final TreeTableColumns columns = TreeTableColumns.of(column("Pour"), column("Contre"));

		assertThat(columns.acceptsRenaming("Contre", "Oppose")).isTrue();
	}

	@Test
	public void refusesRenamingToBlankOrUsedKey() {
		final TreeTableColumns columns = TreeTableColumns.of(column("Pour"), column("Contre"));

		assertThat(columns.acceptsRenaming("Contre", " ")).isFalse();
		assertThat(columns.acceptsRenaming("Contre", null)).isFalse();
		assertThat(columns.acceptsRenaming("Contre", "Pour")).isFalse();
		assertThat(columns.acceptsRenaming("Contre", "Contre")).isFalse();
	}

	@Test
	public void refusesRenamingOfUnknownColumn() {
		final TreeTableColumns columns = TreeTableColumns.of(column("Pour"));

		assertThat(columns.acceptsRenaming("Contre", "Oppose")).isFalse();
	}

	@Test
	public void treatsSameColumnsAsEqual() {
		assertThat(TreeTableColumns.of(column("Pour"))).isEqualTo(TreeTableColumns.of(column("Pour")));
	}
}
