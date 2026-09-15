package com.deskhand.ingestion;

import com.deskhand.ingestion.MarkdownH2Chunker.ChunkedSection;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pure-logic tests for {@link MarkdownH2Chunker} - no mocking needed. Covers one-chunk-per-H2
 * section, heading text retained in chunk content, and a bare H1-only preamble (no H2 yet) being
 * skipped, matching the original chunker's regex-split behavior.
 */
class MarkdownH2ChunkerTest {

    @Test
    void splitsOnH2HeadingsAndDropsPreamble() {
        String markdown = """
                # Document Title

                Some preamble text before any H2 heading - should be dropped.

                ## First Section

                Body of the first section.
                More text in the first section.

                ## Second Section

                Body of the second section.
                """;

        List<ChunkedSection> sections = MarkdownH2Chunker.chunk("test.md", markdown);

        assertThat(sections).hasSize(2);
        assertThat(sections.get(0).heading()).isEqualTo("First Section");
        assertThat(sections.get(0).sourceDocument()).isEqualTo("test.md");
        assertThat(sections.get(0).content()).contains("## First Section");
        assertThat(sections.get(0).content()).contains("Body of the first section.");
        assertThat(sections.get(0).content()).doesNotContain("preamble");
        assertThat(sections.get(1).heading()).isEqualTo("Second Section");
        assertThat(sections.get(1).content()).contains("Body of the second section.");
    }

    @Test
    void ignoresH1AndH3HeadingsAsSectionBoundaries() {
        String markdown = """
                ## Only Section

                ### A Sub-heading

                Sub-section text stays attached to the parent H2 section.
                """;

        List<ChunkedSection> sections = MarkdownH2Chunker.chunk("test.md", markdown);

        assertThat(sections).hasSize(1);
        assertThat(sections.get(0).content()).contains("Sub-section text stays attached");
    }

    @Test
    void documentWithNoH2HeadingsProducesNoSections() {
        String markdown = "# Just a title\n\nAnd a paragraph, no H2 anywhere.\n";

        List<ChunkedSection> sections = MarkdownH2Chunker.chunk("test.md", markdown);

        assertThat(sections).isEmpty();
    }
}
