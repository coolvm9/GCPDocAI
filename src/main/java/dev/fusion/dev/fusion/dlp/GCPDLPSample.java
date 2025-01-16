package dev.fusion.dev.fusion.dlp;

import com.google.cloud.dlp.v2.DlpServiceClient;
import com.google.privacy.dlp.v2.ContentItem;
import com.google.privacy.dlp.v2.InspectContentRequest;
import com.google.privacy.dlp.v2.InspectContentResponse;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Base64;

public class GCPDLPSample {
    public static void main(String[] args) throws IOException {
        // Replace with your GCP Project ID and Inspect Template resource name
        String projectId = "neat-vent-381323";
        String inspectTemplateName = "projects/neat-vent-381323/locations/global/inspectTemplates/test_inspect"; // Replace with your Inspect Template ID

        // Path to your sample PDF file in resources
        File file = new File(GCPDLPSample.class.getClassLoader().getResource("sample_sensitive_data_test.txt").getFile());
        String filePath = file.getAbsolutePath();
        System.out.println("File Path: " + filePath);

        // Call the processLargeFile method to process the file in chunks
        processLargeFile(filePath, projectId, inspectTemplateName);
    }

    /**
     * Processes a large file in chunks using Google Cloud DLP.
     *
     * @param filePath           Path to the file to process.
     * @param projectId          GCP Project ID.
     * @param inspectTemplateName Inspect Template resource name.
     * @throws IOException If an error occurs during processing.
     */
    private static void processLargeFile(String filePath, String projectId, String inspectTemplateName) throws IOException {
        // Initialize DLP Service Client
        try (DlpServiceClient dlpServiceClient = DlpServiceClient.create()) {
            // Read the file in chunks
            try (InputStream inputStream = Files.newInputStream(Path.of(filePath))) {
                byte[] buffer = new byte[5 * 1024 * 1024]; // 5 MB chunk size
                int bytesRead;
                int chunkNumber = 1;

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    // Encode the chunk into Base64
                    byte[] encodedBytes = Base64.getEncoder().encode(Arrays.copyOfRange(buffer, 0, bytesRead));
                    String base64EncodedChunk = new String(encodedBytes);

                    // Create the content item
                    ContentItem contentItem = ContentItem.newBuilder()
                            .setValue(base64EncodedChunk)
                            .build();

                    // Create the InspectContentRequest
                    InspectContentRequest request = InspectContentRequest.newBuilder()
                            .setParent("projects/" + projectId)
                            .setInspectTemplateName(inspectTemplateName) // Reference the template
                            .setItem(contentItem)
                            .build();

                    // Make the API call to DLP
                    InspectContentResponse response = dlpServiceClient.inspectContent(request);
                    System.out.println( response.getResult().getFindingsCount());

                    // Print findings for the current chunk
                    System.out.printf("Findings for Chunk %d:%n", chunkNumber++);
                    response.getResult().getFindingsList().forEach(finding -> {
                        System.out.printf("InfoType: %s, Likelihood: %s, Quote: %s%n",
                                finding.getInfoType().getName(),
                                finding.getLikelihood(),
                                finding.getQuote());
                    });
                }
            }
        }
    }
}