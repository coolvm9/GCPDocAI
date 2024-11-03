package dev.fusion.v1;

import com.google.cloud.documentai.v1.*;
import com.google.protobuf.ByteString;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class GCPDocAILayoutProcessorSample {

    public static void main(String[] args) throws IOException {
        String projectId = "13092500545";
        String location = "us";
        String processorId = "1afab172b21cf028";

        File file = new File(GCPDocAILayoutProcessorSample.class.getClassLoader().getResource("sample-layout.pdf").getFile());
        Path filePath = file.toPath();
        System.out.println("File Path: " + filePath);

        ByteString pdfBytes = ByteString.readFrom(new FileInputStream(file));

        Document document = processDocument(pdfBytes, projectId, location, processorId);
        System.out.println("Document Text: " + document.getDocumentLayout());

        if (document.hasDocumentLayout()) {
            System.out.println("Document Layout Info:");
            iterateBlocks(document.getDocumentLayout().getBlocksList(), 0);
        } else {
            System.out.println("No document layout found.");
        }

        // Iterate over chunks of text in the document
        if (document.hasChunkedDocument()) {
            System.out.println("Document Chunk Info:");
            Document.ChunkedDocument chunkedDocument = document.getChunkedDocument();
            iterateChunks(chunkedDocument);
        } else {
            System.out.println("No document layout found.");
        }
    }

    // New method to iterate over chunks in ChunkedDocument
    private static void iterateChunks(Document.ChunkedDocument chunkedDocument) {
        for (Document.ChunkedDocument.Chunk chunk : chunkedDocument.getChunksList()) {
            System.out.println("Chunk ID: " + chunk.getChunkId());
            System.out.println("Chunk Text: " + chunk.getContent());
            System.out.println("Chunk Page Span: " + chunk.getPageSpan().getPageStart() + " to " + chunk.getPageSpan().getPageEnd());
            System.out.println("------------------------------");
        }
    }

    private static void iterateBlocks(List<Document.DocumentLayout.DocumentLayoutBlock> blocks, int depth) {
        String indent = "  ".repeat(depth);

        for (Document.DocumentLayout.DocumentLayoutBlock block : blocks) {
            System.out.println(indent + "Block ID: " + block.getBlockId());

            if (block.hasTextBlock()) {
                Document.DocumentLayout.DocumentLayoutBlock.LayoutTextBlock textBlock = block.getTextBlock();
                System.out.println(indent + "Text: " + textBlock.getText());
                System.out.println(indent + "Type: " + textBlock.getType());

                if (textBlock.getBlocksCount() > 0) {
                    System.out.println(indent + "Nested Blocks:");
                    iterateBlocks(textBlock.getBlocksList(), depth + 1);
                }
            }

            if (block.hasTableBlock()) {
                Document.DocumentLayout.DocumentLayoutBlock.LayoutTableBlock tableBlock = block.getTableBlock();
                System.out.println(indent + "Table Block:");

                // Process header rows if present
                System.out.println(indent + "Header Rows:");
                for (Document.DocumentLayout.DocumentLayoutBlock.LayoutTableRow headerRow : tableBlock.getHeaderRowsList()) {
                    System.out.print(indent + "  Header Row: ");
                    for (Document.DocumentLayout.DocumentLayoutBlock.LayoutTableCell cell : headerRow.getCellsList()) {
                        // Get text from each cell in the header row
                        String cellText = extractCellText(cell);
                        System.out.print(cellText + " | ");
                    }
                    System.out.println(); // End of header row
                }

                // Process body rows
                System.out.println(indent + "Body Rows:");
                for (Document.DocumentLayout.DocumentLayoutBlock.LayoutTableRow bodyRow : tableBlock.getBodyRowsList()) {
                    System.out.print(indent + "  Body Row: ");
                    for (Document.DocumentLayout.DocumentLayoutBlock.LayoutTableCell cell : bodyRow.getCellsList()) {
                        // Get text from each cell in the body row
                        String cellText = extractCellText(cell);
                        System.out.print(cellText + " | ");
                    }
                    System.out.println(); // End of body row
                }
            }

            System.out.println(indent + "Page Span: " + block.getPageSpan().getPageStart() + " to " + block.getPageSpan().getPageEnd());
            System.out.println(indent + "------------------------------");
        }
    }

    // Helper method to extract text from each table cell
    private static String extractCellText(Document.DocumentLayout.DocumentLayoutBlock.LayoutTableCell cell) {
        StringBuilder cellText = new StringBuilder();
        for (Document.DocumentLayout.DocumentLayoutBlock block : cell.getBlocksList()) {
            if (block.hasTextBlock()) {
                cellText.append(block.getTextBlock().getText()).append(" ");
            }
        }
        return cellText.toString().trim();
    }

    public static Document processDocument(ByteString pdfBytes, String projectId, String location, String processorId) throws IOException {
        try (DocumentProcessorServiceClient client = DocumentProcessorServiceClient.create()) {
            ProcessorName processorName = ProcessorName.of(projectId, location, processorId);

            ProcessOptions processOptions = ProcessOptions.newBuilder()
                    .setLayoutConfig(ProcessOptions.LayoutConfig.newBuilder()
                            .setChunkingConfig(ProcessOptions.LayoutConfig.ChunkingConfig.newBuilder()
                                    .setChunkSize(50)
                                    .setIncludeAncestorHeadings(true)
                                    .build())
                            .build())
                    .build();

            ProcessRequest request = ProcessRequest.newBuilder()
                    .setName(processorName.toString())
                    .setProcessOptions(processOptions)
                    .setRawDocument(RawDocument.newBuilder()
                            .setContent(pdfBytes)
                            .setMimeType("application/pdf")
                            .build())
                    .build();

            ProcessResponse response = client.processDocument(request);
            return response.getDocument();
        }
    }
}