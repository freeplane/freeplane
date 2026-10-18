package org.freeplane.features.treetable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.freeplane.api.LengthUnit;
import org.freeplane.api.Quantity;
import org.junit.Test;

public class TreeTableColumnsTest {

	private static TreeTableColumn column(final String attributeKey) {
		return new TreeTableColumn(attributeKey, new Quantity<LengthUnit>(50, LengthUnit.pt));
	}

	private static String[] attributeKeysOf(final TreeTableColumns columns) {
		final String[] keys = new String[columns.size()];
		for (int index = 0; index < columns.size(); index++)
			keys[index] = columns.get(index).getAttributeKey();
		return keys;
	}

	@Test
	public void emptyColumnsHaveNoEntry() {
		assertThat(TreeTableColumns.empty().size()).isZero();
		assertThat(TreeTableColumns.empty().isEmpty()).isTrue();
	}

	@Test
	public void keepsGivenColumnOrder() {
		final TreeTableColumns columns = TreeTableColumns.of(column("Pour"), column("Contre"));

		assertThat(attributeKeysOf(columns)).containsExactly("Pour", "Contre");
	}

	@Test
	public void rejectsDuplicateAttributeKeys() {
		assertThatThrownBy(() -> TreeTableColumns.of(column("Pour"), column("Pour")))
			.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public void renamesColumnKeepingItsPositionAndWidth() {
		final TreeTableColumn contre = new TreeTableColumn("Contre", new Quantity<LengthUnit>(80, LengthUnit.pt));
		final TreeTableColumns columns = TreeTableColumns.of(column("Pour"), contre, column("Neutre"));

		final TreeTableColumns changed = columns.renamed("Contre", "Oppose");

		assertThat(attributeKeysOf(changed)).containsExactly("Pour", "Oppose", "Neutre");
		assertThat(changed.get(1).getWidth()).isEqualTo(contre.getWidth());
	}

	@Test
	public void rejectsRenamingOfUnknownColumn() {
		final TreeTableColumns columns = TreeTableColumns.of(column("Pour"));

		assertThatThrownBy(() -> columns.renamed("Contre", "Oppose"))
			.isInstanceOf(IllegalArgumentException.class);
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
	public void insertsColumnLeftOfGivenIndex() {
		final TreeTableColumns columns = TreeTableColumns.of(column("Pour"), column("Contre"));

		final TreeTableColumns changed = columns.insertedLeftOf(1, column("Neutre"));

		assertThat(attributeKeysOf(changed)).containsExactly("Pour", "Neutre", "Contre");
	}

	@Test
	public void insertsColumnRightOfGivenIndex() {
		final TreeTableColumns columns = TreeTableColumns.of(column("Pour"), column("Contre"));

		final TreeTableColumns changed = columns.insertedRightOf(0, column("Neutre"));

		assertThat(attributeKeysOf(changed)).containsExactly("Pour", "Neutre", "Contre");
	}

	@Test
	public void insertsColumnRightOfLastIndex() {
		final TreeTableColumns columns = TreeTableColumns.of(column("Pour"), column("Contre"));

		final TreeTableColumns changed = columns.insertedRightOf(1, column("Neutre"));

		assertThat(attributeKeysOf(changed)).containsExactly("Pour", "Contre", "Neutre");
	}

	@Test
	public void leavesOriginalColumnsUnchangedOnInsert() {
		final TreeTableColumns columns = TreeTableColumns.of(column("Pour"));

		columns.appended(column("Contre"));

		assertThat(attributeKeysOf(columns)).containsExactly("Pour");
	}

	@Test
	public void rejectsInsertingDuplicateAttributeKey() {
		final TreeTableColumns columns = TreeTableColumns.of(column("Pour"));

		assertThatThrownBy(() -> columns.appended(column("Pour")))
			.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public void rejectsInsertBeyondEnd() {
		final TreeTableColumns columns = TreeTableColumns.of(column("Pour"));

		assertThatThrownBy(() -> columns.insertedAt(2, column("Contre")))
			.isInstanceOf(IndexOutOfBoundsException.class);
	}

	@Test
	public void removesColumnAtGivenIndex() {
		final TreeTableColumns columns = TreeTableColumns.of(column("Pour"), column("Neutre"), column("Contre"));

		final TreeTableColumns changed = columns.removedAt(1);

		assertThat(attributeKeysOf(changed)).containsExactly("Pour", "Contre");
	}

	@Test
	public void findsIndexOfAttributeKey() {
		final TreeTableColumns columns = TreeTableColumns.of(column("Pour"), column("Contre"));

		assertThat(columns.indexOfAttributeKey("Contre")).isEqualTo(1);
		assertThat(columns.containsAttributeKey("Pour")).isTrue();
	}

	@Test
	public void reportsMissingAttributeKey() {
		final TreeTableColumns columns = TreeTableColumns.of(column("Pour"));

		assertThat(columns.indexOfAttributeKey("Absente")).isEqualTo(-1);
		assertThat(columns.containsAttributeKey("Absente")).isFalse();
	}

	@Test
	public void movesColumnToEarlierPosition() {
		final TreeTableColumns columns = TreeTableColumns.of(column("Pour"), column("Neutre"), column("Contre"));

		final TreeTableColumns changed = columns.movedFromTo(2, 0);

		assertThat(attributeKeysOf(changed)).containsExactly("Contre", "Pour", "Neutre");
	}

	@Test
	public void movesColumnToLaterPosition() {
		final TreeTableColumns columns = TreeTableColumns.of(column("Pour"), column("Neutre"), column("Contre"));

		final TreeTableColumns changed = columns.movedFromTo(0, 2);

		assertThat(attributeKeysOf(changed)).containsExactly("Neutre", "Contre", "Pour");
	}

	@Test
	public void keepsAtLeastOneColumn() {
		assertThat(TreeTableColumns.of(column("Pour"), column("Contre")).canRemove()).isTrue();
		assertThat(TreeTableColumns.of(column("Pour")).canRemove()).isFalse();
		assertThat(TreeTableColumns.empty().canRemove()).isFalse();
	}

	@Test
	public void cannotMoveFirstColumnLeft() {
		final TreeTableColumns columns = TreeTableColumns.of(column("Pour"), column("Neutre"), column("Contre"));

		assertThat(columns.canMoveLeft(0)).isFalse();
		assertThat(columns.canMoveLeft(1)).isTrue();
		assertThat(columns.canMoveLeft(2)).isTrue();
	}

	@Test
	public void cannotMoveLastColumnRight() {
		final TreeTableColumns columns = TreeTableColumns.of(column("Pour"), column("Neutre"), column("Contre"));

		assertThat(columns.canMoveRight(0)).isTrue();
		assertThat(columns.canMoveRight(1)).isTrue();
		assertThat(columns.canMoveRight(2)).isFalse();
	}

	@Test
	public void cannotMoveColumnOutsideRange() {
		final TreeTableColumns columns = TreeTableColumns.of(column("Pour"), column("Contre"));

		assertThat(columns.canMoveLeft(5)).isFalse();
		assertThat(columns.canMoveRight(-1)).isFalse();
		assertThat(TreeTableColumns.of(column("Pour")).canMoveLeft(0)).isFalse();
		assertThat(TreeTableColumns.of(column("Pour")).canMoveRight(0)).isFalse();
	}

	@Test
	public void replacesColumnAtGivenIndex() {
		final TreeTableColumns columns = TreeTableColumns.of(column("Pour"), column("Contre"));

		final TreeTableColumns changed = columns.replacedAt(0, column("Avantages"));

		assertThat(attributeKeysOf(changed)).containsExactly("Avantages", "Contre");
	}

	@Test
	public void treatsSameColumnsAsEqual() {
		assertThat(TreeTableColumns.of(column("Pour"))).isEqualTo(TreeTableColumns.of(column("Pour")));
	}

	@Test
	public void exposesColumnsAsUnmodifiableList() {
		final TreeTableColumns columns = TreeTableColumns.of(column("Pour"));

		assertThatThrownBy(() -> columns.asList().add(column("Contre")))
			.isInstanceOf(UnsupportedOperationException.class);
	}

	@Test
	public void resizesColumnKeepingItsKeyAndPosition() {
		final TreeTableColumns columns = TreeTableColumns.of(column("Pour"), column("Contre"));
		final Quantity<LengthUnit> width = new Quantity<LengthUnit>(90, LengthUnit.pt);

		final TreeTableColumns changed = columns.resized("Contre", width);

		assertThat(attributeKeysOf(changed)).containsExactly("Pour", "Contre");
		assertThat(changed.get(1).getWidth()).isEqualTo(width);
		assertThat(changed.get(0).getWidth()).isEqualTo(columns.get(0).getWidth());
	}

	@Test
	public void rejectsResizingOfUnknownColumn() {
		final TreeTableColumns columns = TreeTableColumns.of(column("Pour"));

		assertThatThrownBy(() -> columns.resized("Contre", new Quantity<LengthUnit>(90, LengthUnit.pt)))
			.isInstanceOf(IllegalArgumentException.class);
	}
}
