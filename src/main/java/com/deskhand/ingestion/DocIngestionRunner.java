package com.deskhand.ingestion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * One-shot ingestion entry point: reads the markdown docs under {@code docs/sample-policies/},
 * chunks them ({@link MarkdownH2Chunker}), and indexes them into Azure AI Search
 * ({@link AzureSearchIndexer}). Gated behind a {@code --ingest} command-line flag so the same jar
 * can either seed the index once or serve the REST API, without a second module or main class -
 * e.g. {@code java -jar deskhand-variant.jar --ingest}.
 */
@Component
public class DocIngestionRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DocIngestionRunner.class);
    private static final Path DOCS_DIR = Path.of("docs", "sample-policies");

    private final AzureSearchIndexer indexer;

    public DocIngestionRunner(AzureSearchIndexer indexer) {
        this.indexer = indexer;
    }

    @Override
    public void run(ApplicationArguments args) throws IOException {
        if (!args.containsOption("ingest")) {
            return;
        }

        List<MarkdownH2Chunker.ChunkedSection> sections = new ArrayList<>();
        try (Stream<Path> files = Files.list(DOCS_DIR)) {
            for (Path file : files.filter(p -> p.toString().endsWith(".md")).toList()) {
                String markdown = Files.readString(file);
                sections.addAll(MarkdownH2Chunker.chunk(file.getFileName().toString(), markdown));
            }
        }

        indexer.indexAll(sections);
        log.info("Indexed {} chunks from {}", sections.size(), DOCS_DIR);
    }
}
