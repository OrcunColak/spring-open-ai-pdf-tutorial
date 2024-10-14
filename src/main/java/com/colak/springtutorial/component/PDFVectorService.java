package com.colak.springtutorial.component;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

import java.util.List;

// Convert local PDFs to vector
@Component
@RequiredArgsConstructor
public class PDFVectorService {

    private final VectorStore vectorStore;

    @Value("classpath:document.pdf")
    private Resource pdfResource;

    @PostConstruct
    public void init() {
        Resource[] array = {pdfResource};
        textEmbeddings(array);
    }

    // Reads the content of the PDFs, splits the text into manageable pieces (chunks), and stores them in
    // vector store for efficient retrieval later.
    private void textEmbeddings(Resource[] pdfs) {
        PdfDocumentReaderConfig pdfDocumentReaderConfig = PdfDocumentReaderConfig.builder()
                .withPageExtractedTextFormatter(
                        new ExtractedTextFormatter.Builder()
                                .build())
                .build();

        TokenTextSplitter textSplitter = new TokenTextSplitter();

        for (Resource pdf : pdfs) {
            PagePdfDocumentReader pagePdfDocumentReader = new PagePdfDocumentReader(pdf, pdfDocumentReaderConfig);
            List<Document> documents = pagePdfDocumentReader.get();

            List<Document> chunkDocs = textSplitter.apply(documents);
            // Each text chunk is passed through an embedding generation model, which converts the text into numerical vectors (embeddings) that represent the meaning of the text.
            // These embeddings are stored in a vector database for efficient searching later.
            vectorStore.accept(chunkDocs);
        }
    }
}
