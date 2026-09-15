package com.deskhand.ingestion;

import org.commonmark.node.Block;
import org.commonmark.node.Document;
import org.commonmark.node.Heading;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.markdown.MarkdownRenderer;

import java.util.ArrayList;
import java.util.List;

/**
 * Chunks a markdown document by its H2 (##) headings, one chunk per section, with the heading
 * text kept as part of the chunk's content - the exact strategy the original uses
 * ({@code re.split(r"\n(?=## )", text.strip())}), reimplemented here using commonmark-java's AST
 * instead of a regex, so each chunk is self-describing when retrieved out of context. No overlap,
 * no token-based splitting, no recursive chunking - matching the original's simplicity.
 * <p>
 * A document's H1 title and any content before the first H2 (a bare preamble) are skipped, same as
 * the original: {@code re.split} on H2 boundaries drops content that has no H2 heading of its own.
 */
public final class MarkdownH2Chunker {

    private static final Parser PARSER = Parser.builder().build();
    private static final MarkdownRenderer RENDERER = MarkdownRenderer.builder().build();

    private MarkdownH2Chunker() {
    }

    public static List<ChunkedSection> chunk(String sourceDocument, String markdown) {
        Document document = (Document) PARSER.parse(markdown);
        List<ChunkedSection> sections = new ArrayList<>();

        String currentHeading = null;
        StringBuilder currentBody = new StringBuilder();

        for (Node node = document.getFirstChild(); node != null; node = node.getNext()) {
            if (node instanceof Heading heading && heading.getLevel() == 2) {
                flush(sections, sourceDocument, currentHeading, currentBody);
                currentHeading = renderPlainText(heading);
                currentBody = new StringBuilder();
                currentBody.append("## ").append(currentHeading).append('\n');
            } else if (currentHeading != null && node instanceof Block block) {
                currentBody.append(RENDERER.render(block)).append('\n');
            }
            // Content before the first H2 (H1 title, preamble) has no heading yet and is dropped,
            // matching the original's regex-split behavior.
        }
        flush(sections, sourceDocument, currentHeading, currentBody);

        return sections;
    }

    private static void flush(List<ChunkedSection> sections, String sourceDocument, String heading, StringBuilder body) {
        if (heading != null && !body.isEmpty()) {
            sections.add(new ChunkedSection(sourceDocument, heading, body.toString().strip()));
        }
    }

    private static String renderPlainText(Heading heading) {
        StringBuilder text = new StringBuilder();
        for (Node child = heading.getFirstChild(); child != null; child = child.getNext()) {
            text.append(RENDERER.render(child));
        }
        return text.toString().strip();
    }

    /**
     * One H2 section of a source document, prior to embedding - the input to
     * {@link AzureSearchIndexer}.
     */
    public record ChunkedSection(String sourceDocument, String heading, String content) {
    }
}
