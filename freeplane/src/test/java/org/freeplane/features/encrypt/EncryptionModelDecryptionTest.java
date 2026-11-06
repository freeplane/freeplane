package org.freeplane.features.encrypt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.freeplane.core.io.ReadManager;
import org.freeplane.core.io.UnknownElements;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.features.map.EncryptionModel;
import org.freeplane.features.map.IEncrypter;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.MapReader;
import org.freeplane.features.map.MapWriter;
import org.freeplane.features.map.MapWriter.Mode;
import org.freeplane.features.map.NodeBuilder;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.clipboard.MapClipboardController;
import org.freeplane.features.map.clipboard.MapClipboardController.CopiedNodeSet;
import org.freeplane.features.mode.Controller;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class EncryptionModelDecryptionTest {
    private final List<IEncrypter> encrypters = new ArrayList<>();
    private Controller previousController;
    private MapController mapController;
    private MapReader mapReader;
    private NodeModel node;

    @Before
    public void setUp() {
        previousController = Controller.getCurrentController();
        Controller controller = mock(Controller.class);
        ResourceController resources = mock(ResourceController.class);
        when(controller.getResourceController()).thenReturn(resources);
        when(resources.getProperty("load_folding")).thenReturn("always_unfold_all_after_load");
        Controller.setCurrentController(controller);

        ReadManager readManager = new ReadManager();
        readManager.addAttributeHandler(NodeBuilder.XML_NODE, "TEXT",
                (element, value) -> ((NodeModel) element).setUserObject(value));
        mapReader = new MapReader(readManager);
        mapController = mock(MapController.class);
        when(mapController.getMapReader()).thenReturn(mapReader);
        doAnswer(invocation -> {
            NodeModel child = invocation.getArgument(0);
            NodeModel parent = invocation.getArgument(1);
            int index = invocation.getArgument(2);
            parent.insert(child, index);
            return null;
        }).when(mapController).insertNodeIntoWithoutUndo(any(NodeModel.class), any(NodeModel.class), anyInt());

        MapModel map = new MapModel((source, targetMap, withChildren) -> null, null, null);
        node = new NodeModel("encrypted parent", map);
        map.setRoot(node);
    }

    @After
    public void tearDown() {
        encrypters.forEach(IEncrypter::destroy);
        Controller.setCurrentController(previousController);
    }

    @Test
    public void rejectsWrongDesPasswordEvenWhenPaddingIsValid() {
        IEncrypter wrongEncrypter = track(spy(new SingleDesEncrypter(new StringBuilder(LegacyDesTestData.WRONG_PASSWORD))));
        EncryptionModel model = new EncryptionModel(node, LegacyDesTestData.ENCRYPTED_CONTENT);
        assertThat(wrongEncrypter.decrypt(LegacyDesTestData.ENCRYPTED_CONTENT)).isNotNull();

        assertThat(model.decrypt(mapController, wrongEncrypter)).isFalse();

        assertThat(model.isLocked()).isTrue();
        assertThat(model.getEncryptedContent()).isEqualTo(LegacyDesTestData.ENCRYPTED_CONTENT);
        assertThat(node.getChildren()).isEmpty();
        model.destroy();
        verify(wrongEncrypter, never()).destroy();
        verify(mapController, never()).insertNodeIntoWithoutUndo(any(NodeModel.class), any(NodeModel.class), anyInt());
    }

    @Test
    public void acceptsCorrectDesPasswordAfterRejectedPassword() {
        IEncrypter wrongEncrypter = track(new SingleDesEncrypter(new StringBuilder(LegacyDesTestData.WRONG_PASSWORD)));
        IEncrypter correctEncrypter = track(spy(new SingleDesEncrypter(new StringBuilder(LegacyDesTestData.CORRECT_PASSWORD))));
        EncryptionModel model = new EncryptionModel(node, LegacyDesTestData.ENCRYPTED_CONTENT);

        assertThat(model.decrypt(mapController, wrongEncrypter)).isFalse();
        assertThat(model.decrypt(mapController, correctEncrypter)).isTrue();
        model.unlock();

        assertThat(model.isAccessible()).isTrue();
        assertThat(node.getChildren()).extracting(NodeModel::getUserObject).containsExactly("test content");
        verify(correctEncrypter, times(1)).decrypt(LegacyDesTestData.ENCRYPTED_CONTENT);
    }

    @Test
    public void rejectsWrongDesPasswordWhenChildrenAreCached() throws Exception {
        IEncrypter correctEncrypter = track(spy(new SingleDesEncrypter(new StringBuilder(LegacyDesTestData.CORRECT_PASSWORD))));
        // Select the fixture's salt when locking, while keeping real DES decryption.
        doReturn(LegacyDesTestData.ENCRYPTED_CONTENT).when(correctEncrypter).encrypt(LegacyDesTestData.PLAINTEXT);
        NodeModel child = new NodeModel("test content", node.getMap());
        node.insert(child);
        EncryptionModel model = new EncryptionModel(node, correctEncrypter);
        MapWriter mapWriter = mock(MapWriter.class);
        doAnswer(invocation -> {
            Writer writer = invocation.getArgument(0);
            writer.write(LegacyDesTestData.PLAINTEXT);
            return null;
        }).when(mapWriter).writeNodeAsXml(any(Writer.class), eq(child), eq(Mode.FILE),
                eq(CopiedNodeSet.ALL_NODES), eq(true), eq(false));
        model.lock(mapWriter);
        IEncrypter wrongEncrypter = track(new SingleDesEncrypter(new StringBuilder(LegacyDesTestData.WRONG_PASSWORD)));
        assertThat(wrongEncrypter.decrypt(model.getEncryptedContent())).isNotNull();

        assertThat(model.decrypt(mapController, wrongEncrypter)).isFalse();

        assertThat(model.isLocked()).isTrue();
        assertThat(model.getEncryptedContent()).isEqualTo(LegacyDesTestData.ENCRYPTED_CONTENT);
        assertThat(node.getChildren()).isEmpty();
        assertThat(model.decrypt(mapController, correctEncrypter)).isTrue();
        model.unlock();
        assertThat(node.getChildren()).containsExactly(child);
        verify(mapController, never()).insertNodeIntoWithoutUndo(any(NodeModel.class), any(NodeModel.class), anyInt());
    }

    @Test
    public void reusesCachedChildrenWhenMalformedXmlMatchesNodeStartAndEnd() throws Exception {
        IEncrypter encrypter = track(new Aes256Encrypter(new StringBuilder("correct")));
        NodeModel child = new NodeModel("cached child", node.getMap());
        node.insert(child);
        EncryptionModel model = new EncryptionModel(node, encrypter);
        MapWriter mapWriter = mock(MapWriter.class);
        doAnswer(invocation -> {
            Writer writer = invocation.getArgument(0);
            writer.write("<node><child></node>");
            return null;
        }).when(mapWriter).writeNodeAsXml(any(Writer.class), eq(child), eq(Mode.FILE),
                eq(CopiedNodeSet.ALL_NODES), eq(true), eq(false));
        model.lock(mapWriter);

        assertThat(model.decrypt(mapController, encrypter)).isTrue();
        model.unlock();

        assertThat(node.getChildren()).containsExactly(child);
        verify(mapController, never()).getMapReader();
    }

    @Test
    public void rejectsCiphertextWhenDecryptionFails() {
        IEncrypter encrypter = track(new Aes256Encrypter(new StringBuilder("correct")));
        EncryptionModel model = new EncryptionModel(node, "not-encrypted-data");

        assertThat(model.decrypt(mapController, encrypter)).isFalse();

        assertThat(model.isLocked()).isTrue();
        assertThat(model.getEncryptedContent()).isEqualTo("not-encrypted-data");
        assertThat(node.getChildren()).isEmpty();
    }

    @Test
    public void rejectsDecryptedTextThatIsNotXml() {
        IEncrypter encrypter = track(new Aes256Encrypter(new StringBuilder("correct")));
        String encrypted = encrypter.encrypt("not node XML");
        EncryptionModel model = new EncryptionModel(node, encrypted);

        assertThat(model.decrypt(mapController, encrypter)).isFalse();

        assertThat(model.isLocked()).isTrue();
        assertThat(model.getEncryptedContent()).isEqualTo(encrypted);
        assertThat(node.getChildren()).isEmpty();
    }

    @Test
    public void rejectsNodeXmlWithoutAnEnd() {
        IEncrypter encrypter = track(new Aes256Encrypter(new StringBuilder("correct")));
        String encrypted = encrypter.encrypt("<node>");
        EncryptionModel model = new EncryptionModel(node, encrypted);

        assertThat(model.decrypt(mapController, encrypter)).isFalse();

        assertThat(model.isLocked()).isTrue();
        assertThat(model.getEncryptedContent()).isEqualTo(encrypted);
        assertThat(node.getChildren()).isEmpty();
    }

    @Test
    public void rejectsXmlWithoutANodeRoot() {
        IEncrypter encrypter = track(new Aes256Encrypter(new StringBuilder("correct")));
        String encrypted = encrypter.encrypt("<other/>");
        EncryptionModel model = new EncryptionModel(node, encrypted);

        assertThat(model.decrypt(mapController, encrypter)).isFalse();

        assertThat(model.isLocked()).isTrue();
        assertThat(node.getChildren()).isEmpty();
    }

    @Test
    public void rejectsTextAfterNodeXml() {
        IEncrypter encrypter = track(new Aes256Encrypter(new StringBuilder("correct")));
        EncryptionModel model = new EncryptionModel(node, encrypter.encrypt("<node/>garbage"));

        assertThat(model.decrypt(mapController, encrypter)).isFalse();

        assertThat(node.getChildren()).isEmpty();
    }

    @Test
    public void rejectsNodeNameWithExtraCharacters() {
        IEncrypter encrypter = track(new Aes256Encrypter(new StringBuilder("correct")));
        EncryptionModel model = new EncryptionModel(node, encrypter.encrypt("<nodeSomething/>"));

        assertThat(model.decrypt(mapController, encrypter)).isFalse();

        assertThat(node.getChildren()).isEmpty();
    }

    @Test
    public void rejectsSlashAfterNodeNameUnlessItClosesTheStartTag() {
        IEncrypter encrypter = track(new Aes256Encrypter(new StringBuilder("correct")));
        EncryptionModel model = new EncryptionModel(node, encrypter.encrypt("<node/child/>"));

        assertThat(model.decrypt(mapController, encrypter)).isFalse();

        assertThat(node.getChildren()).isEmpty();
    }

    @Test
    public void rejectsControlCharactersBeforeNodeStart() {
        IEncrypter encrypter = track(new Aes256Encrypter(new StringBuilder("correct")));
        EncryptionModel model = new EncryptionModel(node, encrypter.encrypt("\0<node/>"));

        assertThat(model.decrypt(mapController, encrypter)).isFalse();

        assertThat(node.getChildren()).isEmpty();
    }

    @Test
    public void rejectsEmptyChildFragments() {
        IEncrypter encrypter = track(new Aes256Encrypter(new StringBuilder("correct")));
        String childXml = "<node/>" + MapClipboardController.NODESEPARATOR
                + MapClipboardController.NODESEPARATOR + "<node/>";
        EncryptionModel model = new EncryptionModel(node, encrypter.encrypt(childXml));

        assertThat(model.decrypt(mapController, encrypter)).isFalse();

        assertThat(node.getChildren()).isEmpty();
    }

    @Test
    public void rejectsWhitespaceWithoutAnyNode() {
        IEncrypter encrypter = track(new Aes256Encrypter(new StringBuilder("correct")));
        EncryptionModel model = new EncryptionModel(node, encrypter.encrypt(" \n\t"));

        assertThat(model.decrypt(mapController, encrypter)).isFalse();

        assertThat(node.getChildren()).isEmpty();
    }

    @Test
    public void rejectsContentBeforeNodeStart() {
        IEncrypter encrypter = track(new Aes256Encrypter(new StringBuilder("correct")));
        String childXml = "<!DOCTYPE node [<!ENTITY external SYSTEM \"file:///must-not-be-read\">]>"
                + "<node TEXT=\"&external;\"/>";
        EncryptionModel model = new EncryptionModel(node, encrypter.encrypt(childXml));

        assertThat(model.decrypt(mapController, encrypter)).isFalse();

        assertThat(node.getChildren()).isEmpty();
    }

    @Test
    public void doesNotInsertEarlierChildrenWhenLaterFragmentIsMalformed() {
        IEncrypter encrypter = track(new Aes256Encrypter(new StringBuilder("correct")));
        NodeModel existingChild = new NodeModel("existing child", node.getMap());
        node.insert(existingChild);
        String childXml = "<node TEXT=\"first\"/>" + MapClipboardController.NODESEPARATOR + "<node><child></node>";
        String encrypted = encrypter.encrypt(childXml);
        EncryptionModel model = new EncryptionModel(node, encrypted);

        assertThat(model.decrypt(mapController, encrypter)).isFalse();

        assertThat(model.getEncryptedContent()).isEqualTo(encrypted);
        assertThat(node.getChildren()).containsExactly(existingChild);
        verify(mapController, never()).insertNodeIntoWithoutUndo(any(NodeModel.class), any(NodeModel.class), anyInt());
    }

    @Test
    public void doesNotInsertEarlierChildrenWhenLaterNodeCannotBeRead() throws Exception {
        IEncrypter encrypter = track(new Aes256Encrypter(new StringBuilder("correct")));
        String childXml = "<node TEXT=\"first\"/>" + MapClipboardController.NODESEPARATOR + "<node TEXT=\"second\"/>";
        String encrypted = encrypter.encrypt(childXml);
        EncryptionModel model = new EncryptionModel(node, encrypted);
        MapReader failingReader = spy(mapReader);
        doCallRealMethod().doThrow(new IOException("Cannot read second child"))
                .when(failingReader).createNodeTreeFromXml(eq(node.getMap()), any(Reader.class), eq(Mode.FILE));
        when(mapController.getMapReader()).thenReturn(failingReader);

        assertThat(model.decrypt(mapController, encrypter)).isFalse();

        assertThat(model.getEncryptedContent()).isEqualTo(encrypted);
        assertThat(node.getChildren()).isEmpty();
        verify(mapController, never()).insertNodeIntoWithoutUndo(any(NodeModel.class), any(NodeModel.class), anyInt());
    }

    @Test
    public void decryptsEmptyDesChildContent() {
        IEncrypter encrypter = track(new SingleDesEncrypter(new StringBuilder("correct")));
        EncryptionModel model = new EncryptionModel(node, encrypter.encrypt(""));

        assertThat(model.decrypt(mapController, encrypter)).isTrue();
        model.unlock();

        assertThat(node.getChildren()).isEmpty();
        node.insert(new NodeModel("new child", node.getMap()));
        assertThat(node.getChildCount()).isEqualTo(1);
    }

    @Test
    public void decryptsEmptyAesChildContent() {
        IEncrypter encrypter = track(new Aes256Encrypter(new StringBuilder("correct")));
        EncryptionModel model = new EncryptionModel(node, encrypter.encrypt(""));

        assertThat(model.decrypt(mapController, encrypter)).isTrue();
        model.unlock();

        assertThat(node.getChildren()).isEmpty();
        node.insert(new NodeModel("new child", node.getMap()));
        assertThat(node.getChildCount()).isEqualTo(1);
    }

    @Test
    public void decryptsSelfClosingDesNode() {
        IEncrypter encrypter = track(new SingleDesEncrypter(new StringBuilder("correct")));
        EncryptionModel model = new EncryptionModel(node, encrypter.encrypt("\n<node TEXT=\"child\"/> \t\r\n"));

        assertThat(model.decrypt(mapController, encrypter)).isTrue();
        model.unlock();

        assertThat(node.getChildren()).extracting(NodeModel::getUserObject).containsExactly("child");
    }

    @Test
    public void decryptsSelfClosingAesNode() {
        IEncrypter encrypter = track(new Aes256Encrypter(new StringBuilder("correct")));
        EncryptionModel model = new EncryptionModel(node, encrypter.encrypt("<node TEXT=\"child\"/>"));

        assertThat(model.decrypt(mapController, encrypter)).isTrue();
        model.unlock();

        assertThat(node.getChildren()).extracting(NodeModel::getUserObject).containsExactly("child");
    }

    @Test
    public void decryptsNestedDesNodes() {
        IEncrypter encrypter = track(new SingleDesEncrypter(new StringBuilder("correct")));
        String childXml = "<node TEXT=\"child\"><node TEXT=\"grandchild\"/></node>";
        EncryptionModel model = new EncryptionModel(node, encrypter.encrypt(childXml));

        assertThat(model.decrypt(mapController, encrypter)).isTrue();
        model.unlock();

        assertThat(node.getChildren()).extracting(NodeModel::getUserObject).containsExactly("child");
        assertThat(node.getChildAt(0).getChildren()).extracting(NodeModel::getUserObject).containsExactly("grandchild");
    }

    @Test
    public void decryptsNestedAesNodes() {
        IEncrypter encrypter = track(new Aes256Encrypter(new StringBuilder("correct")));
        String childXml = "<node TEXT=\"child\"><node TEXT=\"grandchild\"/></node>";
        EncryptionModel model = new EncryptionModel(node, encrypter.encrypt(childXml));

        assertThat(model.decrypt(mapController, encrypter)).isTrue();
        model.unlock();

        assertThat(node.getChildren()).extracting(NodeModel::getUserObject).containsExactly("child");
        assertThat(node.getChildAt(0).getChildren()).extracting(NodeModel::getUserObject).containsExactly("grandchild");
    }

    @Test
    public void decryptsMultipleDesChildren() {
        IEncrypter encrypter = track(new SingleDesEncrypter(new StringBuilder("correct")));
        String childXml = "<node TEXT=\"first\"/>" + MapClipboardController.NODESEPARATOR + "<node TEXT=\"second\"/>";
        EncryptionModel model = new EncryptionModel(node, encrypter.encrypt(childXml));

        assertThat(model.decrypt(mapController, encrypter)).isTrue();
        model.unlock();

        assertThat(node.getChildren()).extracting(NodeModel::getUserObject).containsExactly("first", "second");
    }

    @Test
    public void decryptsMultipleAesChildren() {
        IEncrypter encrypter = track(new Aes256Encrypter(new StringBuilder("correct")));
        String childXml = "<node TEXT=\"first\"/>" + MapClipboardController.NODESEPARATOR + "<node TEXT=\"second\"/>";
        EncryptionModel model = new EncryptionModel(node, encrypter.encrypt(childXml));

        assertThat(model.decrypt(mapController, encrypter)).isTrue();
        model.unlock();

        assertThat(node.getChildren()).extracting(NodeModel::getUserObject).containsExactly("first", "second");
    }

    @Test
    public void decryptsNodeWithRichContentAndEscapedCharacters() {
        IEncrypter encrypter = track(new Aes256Encrypter(new StringBuilder("correct")));
        String childXml = "<node TEXT=\"child &amp; sibling\"><richcontent TYPE=\"NODE\">"
                + "<html><head/><body><p>text &amp; ü</p></body></html></richcontent></node>";
        EncryptionModel model = new EncryptionModel(node, encrypter.encrypt(childXml));

        assertThat(model.decrypt(mapController, encrypter)).isTrue();
        model.unlock();

        NodeModel child = node.getChildAt(0);
        assertThat(child.getUserObject()).isEqualTo("child & sibling");
        UnknownElements unknownElements = child.getExtension(UnknownElements.class);
        assertThat(unknownElements.getUnknownElements().getFirstChildNamed("richcontent")
                .getFirstChildNamed("html").getFirstChildNamed("body").getFirstChildNamed("p").getContent())
                .isEqualTo("text & ü");
    }

    private <T extends IEncrypter> T track(T encrypter) {
        encrypters.add(encrypter);
        return encrypter;
    }
}
