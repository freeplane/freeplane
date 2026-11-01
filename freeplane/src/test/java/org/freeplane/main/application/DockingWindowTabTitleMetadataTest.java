package org.freeplane.main.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

public class DockingWindowTabTitleMetadataTest {

    @Test
    public void shouldRoundTripTabAndGroupTitles() throws IOException {
        List<DockingWindowTabTitleMetadata.Entry> entries = Arrays.asList(
                new DockingWindowTabTitleMetadata.Entry("0/1", "view:map-a", "Roadmap"),
                new DockingWindowTabTitleMetadata.Entry("0", "group:map-a,map-b", "Planning / Review"));

        List<DockingWindowTabTitleMetadata.Entry> decoded =
                DockingWindowTabTitleMetadata.decode(DockingWindowTabTitleMetadata.encode(entries));

        assertThat(decoded).extracting(entry -> entry.path).containsExactly("0/1", "0");
        assertThat(decoded).extracting(entry -> entry.windowSignature)
                .containsExactly("view:map-a", "group:map-a,map-b");
        assertThat(decoded).extracting(entry -> entry.title)
                .containsExactly("Roadmap", "Planning / Review");
    }

    @Test
    public void shouldRoundTripEmptyMetadata() throws IOException {
        assertThat(DockingWindowTabTitleMetadata.decode(
                DockingWindowTabTitleMetadata.encode(Collections.emptyList()))).isEmpty();
    }

    @Test
    public void shouldRejectUnknownMetadataFormat() {
        assertThatThrownBy(() -> DockingWindowTabTitleMetadata.decode("not-base64"))
                .isInstanceOf(IOException.class);
    }
}
