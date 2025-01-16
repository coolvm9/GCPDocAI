package dev.fusion.dev.fusion.dlp;

import com.google.api.gax.longrunning.OperationFuture;
import com.google.api.gax.paging.Page;
import com.google.cloud.documentai.v1.*;
import com.google.cloud.storage.*;
import com.google.protobuf.util.JsonFormat;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class GCPLayoutParserSubmitBatch {
    private final String projectId;
    private final String location;
    private final String processorId;
    private final String outputBucketName;
    private final String outputPrefix;

    // Constructor to initialize configuration details
    public GCPLayoutParserSubmitBatch(String projectId, String location, String processorId, String outputBucketName, String outputPrefix) {
        this.projectId = projectId;
        this.location = location;
        this.processorId = processorId;
        this.outputBucketName = outputBucketName;
        this.outputPrefix = outputPrefix;
    }

    // Submits the batch process and returns the operation name
    public String submitBatchProcess(String gcsInputUri) throws IOException, InterruptedException, ExecutionException, TimeoutException {
        String endpoint = String.format("%s-documentai.googleapis.com:443", location);
        DocumentProcessorServiceSettings settings =
                DocumentProcessorServiceSettings.newBuilder().setEndpoint(endpoint).build();

        try (DocumentProcessorServiceClient client = DocumentProcessorServiceClient.create(settings)) {
            String name = String.format("projects/%s/locations/%s/processors/%s", projectId, location, processorId);
           
            // Prepare input configuration
            GcsDocument gcsDocument = GcsDocument.newBuilder().setGcsUri(gcsInputUri).setMimeType("application/pdf").build();
            GcsDocuments gcsDocuments = GcsDocuments.newBuilder().addDocuments(gcsDocument).build();
            BatchDocumentsInputConfig inputConfig = BatchDocumentsInputConfig.newBuilder().setGcsDocuments(gcsDocuments).build();

            // Prepare output configuration
            String fullGcsPath = String.format("gs://%s/%s/", outputBucketName, outputPrefix);
            DocumentOutputConfig.GcsOutputConfig gcsOutputConfig = DocumentOutputConfig.GcsOutputConfig.newBuilder().setGcsUri(fullGcsPath).build();
            DocumentOutputConfig documentOutputConfig = DocumentOutputConfig.newBuilder().setGcsOutputConfig(gcsOutputConfig).build();

            // Prepare batch process request
            BatchProcessRequest request = BatchProcessRequest.newBuilder()
                    .setName(name)
                    .setInputDocuments(inputConfig)
                    .setDocumentOutputConfig(documentOutputConfig)
                    .build();

            // Submit the batch process
            OperationFuture<BatchProcessResponse, BatchProcessMetadata> future = client.batchProcessDocumentsAsync(request);
            String operationName = future.getName();
            System.out.println("Batch process submitted. Operation Name: " + operationName);
            return operationName; // Return the operation name
        }
    }
}
//    // Main method for testing
//    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException, TimeoutException {
//        // Replace with your GCP configurations
//        String projectId = "13092500545";
//        String location = "us";
//        String processorId = "1afab172b21cf028";
//        String outputBucketName = "neat_vent_dlp_test_1";
//        String outputPrefix = "output-prefix";
//
//        // Create an instance of GCPLayoutParserSubmitBatch
//        GCPLayoutParserSubmitBatch parserBatch =
//                new GCPLayoutParserSubmitBatch(projectId,
//                        location,
//                        processorId,
//                        outputBucketName,
//                        outputPrefix);
//
//        // Input file URI in GCS
//        String inputGcsUri = "gs://neat_vent_dlp_test_1/uploaded-file.pdf";
//
//        // Process the document
//        parserBatch.processDocuments(inputGcsUri);
//    }
