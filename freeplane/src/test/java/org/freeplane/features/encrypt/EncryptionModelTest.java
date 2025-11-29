/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2025 Freeplane team
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.freeplane.features.encrypt;

import static org.assertj.core.api.Assertions.assertThat;

import org.freeplane.features.map.EncryptionModel;
import org.freeplane.features.map.IEncrypter;
import org.freeplane.features.map.NodeModel;
import org.junit.After;
import org.junit.Test;

/**
 * Tests for EncryptionModel - high-level encryption model functionality.
 * 
 * @author Freeplane team
 */
public class EncryptionModelTest {
	private IEncrypter encrypter;

	@After
	public void cleanup() {
		if (encrypter != null) {
			encrypter.destroy();
			encrypter = null;
		}
	}

	@Test
	public void createEncryptionModelWithEncrypter() {
		final StringBuilder password = new StringBuilder("test123");
		encrypter = new Aes256Encrypter(password);
		
		final NodeModel node = new NodeModel("test", null);
		final EncryptionModel model = new EncryptionModel(node, encrypter);
		
		assertThat(model).isNotNull();
		assertThat(model.isAccessible()).as("Model should be accessible when created with encrypter").isTrue();
		assertThat(model.isLocked()).as("Model should not be locked when created with encrypter").isFalse();
	}

	@Test
	public void createEncryptionModelWithEncryptedContent() {
		final NodeModel node = new NodeModel("test", null);
		final String encryptedContent = "encrypted-data";
		final EncryptionModel model = new EncryptionModel(node, encryptedContent);
		
		assertThat(model).isNotNull();
		assertThat(model.isAccessible()).as("Model should not be accessible when created with encrypted content").isFalse();
		assertThat(model.isLocked()).as("Model should be locked when created with encrypted content").isTrue();
	}

	@Test
	public void getModelReturnsNullForNodeWithoutEncryption() {
		final NodeModel node = new NodeModel("test", null);
		final EncryptionModel model = EncryptionModel.getModel(node);
		
		assertThat(model).isNull();
	}

	@Test
	public void getModelReturnsEncryptionModelForEncryptedNode() {
		final NodeModel node = new NodeModel("test", null);
		final String encryptedContent = "encrypted-data";
		final EncryptionModel model = new EncryptionModel(node, encryptedContent);
		node.addExtension(model);
		
		final EncryptionModel retrieved = EncryptionModel.getModel(node);
		
		assertThat(retrieved).isNotNull();
		assertThat(retrieved).isEqualTo(model);
	}

	@Test
	public void newModelWithEncrypterIsAccessible() {
		final StringBuilder password = new StringBuilder("test123");
		encrypter = new Aes256Encrypter(password);
		
		final NodeModel node = new NodeModel("test", null);
		final EncryptionModel model = new EncryptionModel(node, encrypter);
		
		assertThat(model.isAccessible()).isTrue();
		assertThat(model.isLocked()).isFalse();
	}

	@Test
	public void newModelWithEncryptedContentIsLocked() {
		final NodeModel node = new NodeModel("test", null);
		final EncryptionModel model = new EncryptionModel(node, "encrypted");
		
		assertThat(model.isAccessible()).isFalse();
		assertThat(model.isLocked()).isTrue();
	}

	@Test
	public void getEncryptedContentReturnsNullForAccessibleModel() {
		final StringBuilder password = new StringBuilder("test123");
		encrypter = new Aes256Encrypter(password);
		
		final NodeModel node = new NodeModel("test", null);
		final EncryptionModel model = new EncryptionModel(node, encrypter);
		
		assertThat(model.getEncryptedContent()).isNull();
	}

	@Test
	public void getEncryptedContentReturnsContentForLockedModel() {
		final NodeModel node = new NodeModel("test", null);
		final String encryptedContent = "encrypted-data";
		final EncryptionModel model = new EncryptionModel(node, encryptedContent);
		
		assertThat(model.getEncryptedContent()).isEqualTo(encryptedContent);
	}

	@Test
	public void destroyMethodCanBeCalled() {
		final StringBuilder password = new StringBuilder("test123");
		final IEncrypter localEncrypter = new Aes256Encrypter(password);
		
		final NodeModel node = new NodeModel("test", null);
		final EncryptionModel model = new EncryptionModel(node, localEncrypter);
		
		// Should not throw exception
		model.destroy();
	}

	@Test
	public void destroyMethodCanBeCalledMultipleTimes() {
		final StringBuilder password = new StringBuilder("test123");
		final IEncrypter localEncrypter = new Aes256Encrypter(password);
		
		final NodeModel node = new NodeModel("test", null);
		final EncryptionModel model = new EncryptionModel(node, localEncrypter);
		
		// Should not throw exception even when called multiple times
		model.destroy();
		model.destroy();
		model.destroy();
	}

	@Test
	public void destroyOnModelWithEncryptedContentDoesNotThrow() {
		final NodeModel node = new NodeModel("test", null);
		final EncryptionModel model = new EncryptionModel(node, "encrypted");
		
		// Should not throw exception
		model.destroy();
	}

	@Test
	public void aes256EncryptedContentIsDetectable() {
		final StringBuilder password = new StringBuilder("test123");
		final IEncrypter aesEncrypter = new Aes256Encrypter(password);
		final String encrypted = aesEncrypter.encrypt("<node TEXT=\"test\"/>");
		aesEncrypter.destroy();
		
		assertThat(encrypted.startsWith(EncryptionHeader.PREFIX_AES256))
			.as("AES-256 content should be detectable")
			.isTrue();
	}

	@Test
	public void legacyDesContentIsNotDetectedAsAes256() {
		final StringBuilder password = new StringBuilder("test123");
		final IEncrypter desEncrypter = new SingleDesEncrypter(password);
		final String encrypted = desEncrypter.encrypt("<node TEXT=\"test\"/>");
		desEncrypter.destroy();
		
		assertThat(encrypted.startsWith(EncryptionHeader.PREFIX_AES256))
			.as("Legacy DES content should not be detected as AES-256")
			.isFalse();
	}

	@Test
	public void aes256EncryptionProducesCorrectFormat() {
		final StringBuilder password = new StringBuilder("test123");
		encrypter = EncryptionHelper.createEncrypter(password);
		
		final String plaintext = "<node TEXT=\"test\"/>";
		final String encrypted = encrypter.encrypt(plaintext);
		
		assertThat(encrypted.startsWith(EncryptionHeader.PREFIX_AES256))
			.as("New encryption should use AES-256")
			.isTrue();
	}

	@Test
	public void legacyDesContentDoesNotHaveAes256Marker() {
		final StringBuilder password = new StringBuilder("test123");
		final IEncrypter desEncrypter = new SingleDesEncrypter(password);
		
		final String plaintext = "<node TEXT=\"test\"/>";
		final String encrypted = desEncrypter.encrypt(plaintext);
		desEncrypter.destroy();
		
		assertThat(encrypted.startsWith("FP-AES256-V1:"))
			.as("Legacy DES content should not have AES-256 marker")
			.isFalse();
	}

	@Test
	public void encryptEmptyXmlContent() {
		final StringBuilder password = new StringBuilder("test123");
		encrypter = new Aes256Encrypter(password);
		
		final String plaintext = "";
		final String encrypted = encrypter.encrypt(plaintext);
		final String decrypted = encrypter.decrypt(encrypted);
		
		assertThat(decrypted).isEqualTo(plaintext);
	}

	@Test
	public void multipleNodesCanBeEncryptedIndependently() {
		final StringBuilder password = new StringBuilder("test123");
		
		final NodeModel node1 = new NodeModel("node1", null);
		final IEncrypter encrypter1 = new Aes256Encrypter(password);
		final EncryptionModel model1 = new EncryptionModel(node1, encrypter1);
		node1.addExtension(model1);
		
		final NodeModel node2 = new NodeModel("node2", null);
		final IEncrypter encrypter2 = new Aes256Encrypter(password);
		final EncryptionModel model2 = new EncryptionModel(node2, encrypter2);
		node2.addExtension(model2);
		
		assertThat(EncryptionModel.getModel(node1)).isNotNull();
		assertThat(EncryptionModel.getModel(node2)).isNotNull();
		assertThat(EncryptionModel.getModel(node1)).isEqualTo(model1);
		assertThat(EncryptionModel.getModel(node2)).isEqualTo(model2);
	}

	@Test
	public void encryptSimpleNodeXml() {
		final StringBuilder password = new StringBuilder("test123");
		encrypter = new Aes256Encrypter(password);
		
		final String plaintext = "<node TEXT=\"My Secret\" ID=\"ID_123\"/>";
		final String encrypted = encrypter.encrypt(plaintext);
		final String decrypted = encrypter.decrypt(encrypted);
		
		assertThat(decrypted).isEqualTo(plaintext);
	}

	@Test
	public void encryptNestedNodeXml() {
		final StringBuilder password = new StringBuilder("test123");
		encrypter = new Aes256Encrypter(password);
		
		final String plaintext = "<node TEXT=\"Parent\" ID=\"ID_1\">" +
			"<node TEXT=\"Child\" ID=\"ID_2\"/>" +
			"</node>";
		final String encrypted = encrypter.encrypt(plaintext);
		final String decrypted = encrypter.decrypt(encrypted);
		
		assertThat(decrypted).isEqualTo(plaintext);
	}

	@Test
	public void encryptComplexNodeXmlWithAttributes() {
		final StringBuilder password = new StringBuilder("test123");
		encrypter = new Aes256Encrypter(password);
		
		final String plaintext = "<node TEXT=\"Complex Node\" " +
			"ID=\"ID_123\" " +
			"CREATED=\"1234567890\" " +
			"MODIFIED=\"1234567891\" " +
			"POSITION=\"right\" " +
			"STYLE_REF=\"default\" " +
			"FOLDED=\"true\">" +
			"<node TEXT=\"Child 1\" ID=\"ID_124\"/>" +
			"<node TEXT=\"Child 2\" ID=\"ID_125\">" +
			"<node TEXT=\"Grandchild\" ID=\"ID_126\"/>" +
			"</node>" +
			"</node>";
		final String encrypted = encrypter.encrypt(plaintext);
		final String decrypted = encrypter.decrypt(encrypted);
		
		assertThat(decrypted).isEqualTo(plaintext);
	}

	@Test
	public void encryptNodeWithSpecialXmlCharacters() {
		final StringBuilder password = new StringBuilder("test123");
		encrypter = new Aes256Encrypter(password);
		
		final String plaintext = "<node TEXT=\"Test &lt;&gt;&amp;&quot;&apos; special\"/>";
		final String encrypted = encrypter.encrypt(plaintext);
		final String decrypted = encrypter.decrypt(encrypted);
		
		assertThat(decrypted).isEqualTo(plaintext);
	}

	@Test
	public void aes256AlgorithmDescriptionIsCorrect() {
		final StringBuilder password = new StringBuilder("test123");
		final IEncrypter aesEncrypter = new Aes256Encrypter(password);
		final String encrypted = aesEncrypter.encrypt("test");
		aesEncrypter.destroy();
		
		final String description = EncryptionHelper.getEncryptionAlgorithmDescription(encrypted);
		assertThat(description).isEqualTo("AES-256");
	}

	@Test
	public void legacyDesAlgorithmDescriptionIsCorrect() {
		final StringBuilder password = new StringBuilder("test123");
		final IEncrypter desEncrypter = new SingleDesEncrypter(password);
		final String encrypted = desEncrypter.encrypt("test");
		desEncrypter.destroy();
		
		final String description = EncryptionHelper.getEncryptionAlgorithmDescription(encrypted);
		assertThat(description).isEqualTo("DES");
	}

	@Test
	public void canDecryptLegacyDesContentWithFallback() {
		final StringBuilder password = new StringBuilder("test123");
		
		// Create legacy DES encrypted content
		final IEncrypter desEncrypter = new SingleDesEncrypter(password);
		final String plaintext = "<node TEXT=\"legacy\"/>";
		final String encrypted = desEncrypter.encrypt(plaintext);
		desEncrypter.destroy();
		
		// Try to decrypt with helper (should work via fallback)
		final String decrypted = EncryptionHelper.createDecrypter(password, encrypted).decrypt(encrypted);
		
		assertThat(decrypted).isEqualTo(plaintext);
	}

	@Test
	public void canDecryptNewAes256ContentDirectly() {
		final StringBuilder password = new StringBuilder("test123");
		
		// Create new AES-256 encrypted content
		final IEncrypter aesEncrypter = new Aes256Encrypter(password);
		final String plaintext = "<node TEXT=\"new\"/>";
		final String encrypted = aesEncrypter.encrypt(plaintext);
		aesEncrypter.destroy();
		
		// Try to decrypt with helper (should work directly)
		final String decrypted = EncryptionHelper.createDecrypter(password, encrypted).decrypt(encrypted);
		
		assertThat(decrypted).isEqualTo(plaintext);
	}
}

