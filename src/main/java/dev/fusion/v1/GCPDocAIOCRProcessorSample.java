package dev.fusion.v1;

import com.google.cloud.documentai.v1.*;
import com.google.protobuf.ByteString;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;

public class GCPDocAIOCRProcessorSample {

    public static void main(String[] args) throws IOException {
        // Path to your PDF file

//        https://us-documentai.googleapis.com/v1/projects/13092500545/locations/us/processors/1afab172b21cf028:process

        // Your Google Cloud Project ID, Location, and Processor ID
        String projectId = "13092500545";
        String location = "us"; // e.g., "us" or "eu"
        String processorId = "179ee6fc6cdfa20b";

        File file = new File(GCPDocAIOCRProcessorSample.class.getClassLoader().getResource("sample-layout.pdf").getFile());
        Path filePath = file.toPath();
        System.out.println("File Path: " + filePath.toString());


        // Load the PDF file into a ByteString
        ByteString pdfBytes = ByteString.readFrom(new FileInputStream(file));

        // Call the processDocument method
        processDocument(pdfBytes, projectId, location, processorId);
    }

    public static void processDocument(ByteString pdfBytes, String projectId, String location, String processorId) throws IOException {
        // Initialize DocumentProcessorServiceClient
        try (DocumentProcessorServiceClient client = DocumentProcessorServiceClient.create()) {
            // Define the processor name
            ProcessorName processorName = ProcessorName.of(projectId, location, processorId);

            // Build the ProcessRequest with the RawDocument input
            ProcessRequest request = ProcessRequest.newBuilder()
                    .setName(processorName.toString())
                    .setRawDocument(RawDocument.newBuilder()
                            .setContent(pdfBytes)
                            .setMimeType("application/pdf") // MIME type for PDF
                            .build())
                    .build();

            // Process the document and get the response
            ProcessResponse response = client.processDocument(request);

            // Extract and print out document text and layout information
            Document document = response.getDocument();
            System.out.println("Document Text: " + document.getText());

            // Iterate over each page for layout analysis (e.g., tables, paragraphs)
            for (Document.Page page : document.getPagesList()) {
                System.out.println("Page Number: " + page.getPageNumber());
                for (Document.Page.Block block : page.getBlocksList()) {
                    System.out.println("Block Text: " + block.toString());
                    System.out.println("Confidence: " + block.getLayout().getConfidence());
                }
            }
            Document.DocumentLayout layout = response.getDocument().getDocumentLayout();
            for (Document.DocumentLayout.DocumentLayoutBlock block : layout.getBlocksList()) {
                System.out.println("Block Number: " + block.getBlockId());
                System.out.println("Block type: " + block.getBlockId());
                System.out.println("Block confidence: " + block.getTextBlock().getText());

            }
        }
    }
}